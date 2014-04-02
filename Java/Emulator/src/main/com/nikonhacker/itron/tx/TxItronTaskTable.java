package com.nikonhacker.itron.tx;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.CodeStructure;
import com.nikonhacker.disassembly.Function;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.itron.ReturnStackEntry;

import java.util.LinkedList;

public class TxItronTaskTable {

    private final static int MAXTASKS = 0xD; // it is hardcoded in a640m010100

    private CodeStructure codeStructure;
    private Memory memory;
    private int [] nextPc = new int[MAXTASKS];
    private int [] context = new int[MAXTASKS];
    private int currentTask;

    public TxItronTaskTable(Memory memory) {
        this.memory = memory;
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
            } else if (codeStructure.tblTCB==null) {
                label = "tblTCB";
            } else
                break;
            System.err.println("Next PC/Context not available! No label called '" + label + "' was found in disassembly");
            return 0;
        } while(false);

        final int currentTCB = memory.load32(codeStructure.pCurrentTCB);

        // check if enough elements in task table
        currentTask = -1;
        for (int objId=0; objId<MAXTASKS; objId++) {

            int TCB = codeStructure.tblTCB + objId * 0x10;

            // if it is current TCB, context may be not set yet and it is not easy to detect
            if (TCB!=currentTCB) {
                final int addrContext = memory.load32(TCB+0xC);

                // is context reasonable pointer?
                if (addrContext!=0 && addrContext!=-1) {
                    if ((addrContext&1)!=0) {
                        // task was never started, look for start address TaskData
                        if (codeStructure.tblTaskData != null) {
                            nextPc[objId] = memory.load32(codeStructure.tblTaskData + objId *0x14 + 4);
                        }
                    } else {
                        // load return-instruction-address from task context
                        nextPc[objId] = memory.load32(addrContext + 4);
                    }
                    context[objId] = addrContext;
                    continue;
                }
            } else {
                currentTask = objId;
            }
            context[objId] = 0;
            nextPc[objId] = 0;
        }
        return MAXTASKS;
    }

    public TxCPUState getUserCpuState(int index) {
        if (index<1 || index>MAXTASKS)
            return null;
        index--;

        int addrContext = context[index];
        if (addrContext==0) {
            // no valid context
            return null;
        }
        TxCPUState state = new TxCPUState();
        // it is already fetched
        state.setPc(nextPc[index]);

        // if complete context available
        if ((addrContext&1)==0) {

            state.setRegisterDefined(TxCPUState.Status);
            state.setReg(TxCPUState.Status, memory.load32(addrContext));

            for (int i=16; i<24; i++) {
                state.setRegisterDefined(i);
                state.setReg(i, memory.load32(addrContext + 8 + (i-16)*4));
            }
            state.setRegisterDefined(30);
            state.setReg(30, memory.load32(addrContext + 28));
            addrContext += 0x30;

            // if not system task
            if (state.isStatusIESet()) {
                // if FPU was enabled
                if ((state.getStatusCU() & 2)!=0) {
                    state.setRegisterDefined(TxCPUState.FCSR);
                    state.setReg(TxCPUState.FCSR, memory.load32(addrContext + 0));

                    for (int i=0; i<32; i+=2) {
                        state.setRegisterDefined(i);
                        state.setReg(i, memory.load32(addrContext + 4 + i*2));
                    }
                    addrContext += 0x44;
                }

                state.setRegisterDefined(TxCPUState.LO);
                state.setReg(TxCPUState.LO, memory.load32(addrContext + 0x4C));
                state.setRegisterDefined(TxCPUState.HI);
                state.setReg(TxCPUState.HI, memory.load32(addrContext + 0x48));

                state.setRegisterDefined(31);
                state.setReg(31, memory.load32(addrContext + 0x44));
                state.setRegisterDefined(25);
                state.setReg(25, memory.load32(addrContext + 0x40));
                state.setRegisterDefined(24);
                state.setReg(24, memory.load32(addrContext + 0x3C));

                for (int i=1; i<16; i++) {
                    state.setRegisterDefined(i);
                    state.setReg(i, memory.load32(addrContext + (i-1)*4));
                }
                addrContext += 0x50;
            }
            // set stack pointer
            state.setRegisterDefined(29);
            state.setReg(29, addrContext);
        }
        return state;
    }

    public LinkedList<ReturnStackEntry> getUserReturnStack(TxCPUState state) {
        if (state==null)
            return null;

        if (!state.isRegisterDefined(TxCPUState.SP))
            return null;

        LinkedList<ReturnStackEntry> array = new LinkedList<ReturnStackEntry>();
        int sp = state.getReg(TxCPUState.SP);
        int ra = state.getPc() | (state.is16bitIsaMode ? 1 : 0);

        while (sp<0) {
            Function function = codeStructure.findFunctionIncluding(ra & 0xFFFFFFFE);
            if (function==null)
                break;

            array.addFirst(new ReturnStackEntry(ra, sp, function));

            if ((ra&1)==0) {
                //  32-bit path

                // each Task is called with return address to 32-bit system function
                // so use it as end condition
                break;
            }
            //  16-bit path
            int args;
            final int opcode = memory.load32(function.getAddress());

            // F70064F6 save    r31,r16,r17,r18-r23,r30, 0x30
            if ((opcode& 0xF800FFC0)==0xF00064C0) {

                // add stack frame
                sp += (((opcode>>16) & 0xF) + (opcode & 0xF))<<3;
                // calc size of saved args
                switch ((opcode>>16)&0xF) {
                    case 0b0000: case 0b0001: case 0b0010: case 0b0011: case 0b1011: args = 0; break;
                    case 0b0100: case 0b0101: case 0b0110: case 0b0111: args = 1; break;
                    case 0b1000: case 0b1001: case 0b1010: args = 2; break;
                    case 0b1100: case 0b1101: args = 3; break;
                    case 0b1110: args = 4; break;
                    default:
                        sp = 0; continue;
                }
                ra = memory.load32(sp - 4 - args*4);

            // 64C1     save    r31, 0x08
            } else if ((opcode & 0xFFC00000)==0x64C00000) {
                final int frameSize = (opcode>>16) & 0xF;
                sp += (frameSize==0 ? 128 : frameSize<<3);
                args = 0;
            } else {
                break;
            }
            ra = memory.load32(sp - 4 - args*4);
        }
        return array;
    }

    public Integer getContext(int index) {
        if (index>0 && index<=MAXTASKS) {
            index--;
            if (context[index]!=0) {
                return context[index];
            }
        }
        return null;
    }

    public Integer getNextPc(int index) {
        if (index>0 && index<=MAXTASKS) {
            index--;
            if (context[index]!=0) {
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
