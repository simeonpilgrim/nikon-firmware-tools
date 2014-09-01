package com.nikonhacker.itron.fr;

import com.nikonhacker.disassembly.CodeStructure;
import com.nikonhacker.disassembly.Function;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.itron.ReturnStackEntry;

import java.util.LinkedList;

public class FrItronTaskTable {
    private CodeStructure codeStructure;
    private Platform platform;

    private int numTasks;
    private int [] nextPc;
    private int [] context;
    private int currentTask;

    public FrItronTaskTable(Platform platform) {
        this.platform = platform;
    }

    public int read(CodeStructure codeStructure) {
        if (codeStructure == null) {
            System.err.println("Next PC/Context not available! Code must be disassembled with 'structure' option first");
            return 0;
        }
        this.codeStructure = codeStructure;
        // address of task constant data table
        do {
            String label;
            if (codeStructure.tblTaskData==null) {
                label = "tblTaskData";
            } else if (codeStructure.pCurrentTCB==null) {
                label = "pCurrentTCB";
            } else
                break;
            System.err.println("Next PC/Context not available! No label called '" + label + "' was found in disassembly");
            return 0;
        } while(false);

        final Memory memory = platform.getMemory();
        // check if reasonable size of element
        final int elementSize = memory.loadUnsigned16(codeStructure.tblTaskData+2);
        if (elementSize<1 || elementSize>2047)
            return 0;
        int i = memory.loadUnsigned16(codeStructure.tblTaskData);
        // if reasonable
        if (i<=0 || i>127)
            return 0;
        if (numTasks != i) {
            numTasks = i;
            nextPc = new int[numTasks];
            context = new int[numTasks];
        }
        int taskData = codeStructure.tblTaskData+4;
        currentTask = -1;
        for (i=0; i<numTasks; i++, taskData += elementSize) {

            final int TCB = memory.load32(taskData);

            // if it is current TCB, context may be not set yet
            if (TCB==memory.load32(codeStructure.pCurrentTCB)) {
                nextPc[i] = platform.getCpuState().getPc();
                currentTask = i;
            }
            else {
                final int contextAddr = memory.load32(TCB+0x18);

                // is context reasonable pointer?
                if (contextAddr!=0 && contextAddr!=-1) {
                    // load return-instruction-address from task context
                    nextPc[i] = memory.load32(contextAddr);
                    context[i] = contextAddr;
                    continue;
                }
                else if (contextAddr==0) {
                    // if DORMANT state look for start address in constants
                    if ((memory.load32(TCB+8)&0x1F) == 0x10) {
                        nextPc[i] = memory.load32(taskData+0x10);
                    }
                } else
                    nextPc[i] = 0;
            }
            context[i] = 0;
        }
        return numTasks;
    }

    public FrCPUState getUserCpuState(int index) {
        if (index<1 || index>numTasks)
            return null;
        index--;

        int addrContext = context[index];
        if (addrContext==0) {
            // no valid context in stack

            if (index == currentTask) {
                final FrCPUState currentState = (FrCPUState)platform.getCpuState();
                if ( currentState.getS()==1)
                    return currentState.createCopy();
            }
            return null;
        }

        FrCPUState state = new FrCPUState();
        // it is already fetched
        state.setPc(nextPc[index]);

        final Memory memory = platform.getMemory();
        state.setRegisterDefined(FrCPUState.USP);
        state.setReg(FrCPUState.USP, addrContext + 5*4 + 15*4);
        state.setRegisterDefined(FrCPUState.PS);
        state.setPS(memory.load32(addrContext+4),false);
        state.setRegisterDefined(FrCPUState.RP);
        state.setReg(FrCPUState.RP, memory.load32(addrContext+8));
        state.setRegisterDefined(FrCPUState.MDL);
        state.setReg(FrCPUState.MDL, memory.load32(addrContext+0xC));
        state.setRegisterDefined(FrCPUState.MDH);
        state.setReg(FrCPUState.MDH, memory.load32(addrContext+0x10));
        for (int i=0; i<15; i++) {
            state.setRegisterDefined(i);
            state.setReg(i, memory.load32(addrContext+0x14+i*4));
        }
        return state;
    }

    public LinkedList<ReturnStackEntry> getUserReturnStack(FrCPUState state) {
        if (state==null)
            return null;

        if (!state.isRegisterDefined(FrCPUState.USP) || !state.isRegisterDefined(FrCPUState.RP))
            return null;

        // TODO we do not know if PS was set, becuase isRegisterDefined(FrCPUState.PS) doesn't work
        if (state.getS()!=1)
            return null;

        LinkedList<ReturnStackEntry> array = new LinkedList<ReturnStackEntry>();
        final Memory memory = platform.getMemory();
        int sp = state.getReg(FrCPUState.USP);
        int rp = state.getPc();
        for (boolean firstRun = true; ;firstRun = false) {
            Function function = codeStructure.findFunctionIncluding(rp);
            if (function==null)
                break;

            array.addFirst(new ReturnStackEntry(rp, sp, function));

            // parse function prolog and locate RP in stack
            int rpOffset = -1;
            int stackFrame=0;
            // each iteration is one CPU instruction
            // found ST RP,@-R15 being on 8th place in function, so use empirical limit 12,
            for (int i=12, addr = function.getAddress(); i>0; i--) {

                // if parsed up to return point
                if (addr>=rp)
                    break;

                // process all "stack save" commands and calculate stack frame
                final int opcode = memory.loadUnsigned16(addr);
                switch (opcode&0xFF00) {
                    case 0x9B00:    // LDI:20
                        addr += 2;
                        break;
                    case 0x0F00:    // ENTER
                        stackFrame += (opcode& 0xFF)*4;
                        i = 0;
                        break;
                    case 0x8E00:    // STM0
                    case 0x8F00:    // STM1
                        stackFrame += Integer.bitCount(opcode&0xFF)*4;
                        break;
                    default:
                        switch (opcode&0xFFF0) {
                            case 0x9F80:    // LDI:32
                                addr += 4;
                                break;
                            case 0x1780:    // ST Rs,@-R15
                                if (opcode==0x1781) {   // ST RP,@-R15
                                    rpOffset = stackFrame;
                                }
                            case 0x1700:    // ST Ri,@-R15
                            case 0x1790:    // ST PS,@-R15
                                stackFrame += 4;
                                break;
                        }
                }
                addr += 2;
            }
            sp += stackFrame;
            if (rpOffset==-1) {
                // may be RP still valid because not changed
                if (firstRun && state.isRegisterDefined(FrCPUState.RP)) {
                    rp = state.getReg(FrCPUState.RP);
                    continue;
                }
                break;
            }
            rp = memory.load32(sp - rpOffset - 4);
        }
        return array;
    }

    public Integer getContext(int index) {
        if (index>0 && index<=numTasks) {
            index--;
            if (context[index]!=0) {
                return context[index];
            }
        }
        return null;
    }

    public Integer getNextPc(int index) {
        if (index>0 && index<=numTasks) {
            index--;
            if (nextPc[index]!=0) {
                return nextPc[index];
            }
        }
        return null;
    }

    // 0 - idle
    public int getCurrentTask() {
        return currentTask + 1;
    }
}
