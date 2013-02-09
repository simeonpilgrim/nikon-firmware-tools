package com.nikonhacker.realos;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Constants;
import com.nikonhacker.disassembly.CodeStructure;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.condition.BreakPointCondition;

public class SysCallEnvironment {

    private static final int BASE_ADDRESS_SYSCALL[] = {0xFFFFFF00,0x10000000};

    private final Platform platform;
    private final Emulator emulator;

    private CodeStructure codeStructure;

    public SysCallEnvironment(Platform platform, Emulator emulator, CodeStructure codeStructure) {
        this.platform = platform;
        this.emulator = emulator;
        this.codeStructure = codeStructure;
    }

    public TaskInformation getTaskInformation(int chip, int objId) {
        int pk_robj = BASE_ADDRESS_SYSCALL[chip] + 0x20; // pointer to result structure

        ErrorCode errorCode;
        if (chip == Constants.CHIP_FR) {
            errorCode = runFrSysCall(ITron3.SYSCALL_NUMBER_REF_TSK, pk_robj, objId);
        }
        else {
            errorCode = runTxSysCall("sys_ref_tsk", pk_robj, objId);
        }

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new TaskInformation(objId, errorCode, 0, 0, 0);
        }
        else {
            Memory memory = platform.getMemory();
            return new TaskInformation(objId, errorCode, memory.load32(pk_robj), memory.load32(pk_robj + 4), memory.load32(pk_robj + 8));
        }
    }

    public SemaphoreInformation getSemaphoreInformation(int chip, int objId) {
        int pk_robj = BASE_ADDRESS_SYSCALL[chip] + 0x20; // pointer to result structure

        ErrorCode errorCode;
        if (chip == Constants.CHIP_FR) {
            errorCode = runFrSysCall(ITron3.SYSCALL_NUMBER_REF_SEM, pk_robj, objId);
        }
        else {
            errorCode = runTxSysCall("sys_ref_sem", pk_robj, objId);
        }

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new SemaphoreInformation(objId, errorCode, 0, 0, 0);
        }
        else {
            Memory memory = platform.getMemory();
            return new SemaphoreInformation(objId, errorCode, memory.load32(pk_robj), memory.load32(pk_robj + 4), memory.load32(pk_robj + 8));
        }
    }

    public EventFlagInformation getEventFlagInformation(int chip, int objId) {
        int pk_robj = BASE_ADDRESS_SYSCALL[chip] + 0x20; // pointer to result structure

        ErrorCode errorCode;
        if (chip == Constants.CHIP_FR) {
            errorCode = runFrSysCall(ITron3.SYSCALL_NUMBER_REF_FLG, pk_robj, objId);
        }
        else {
            errorCode = runTxSysCall("sys_ref_flg", pk_robj, objId);
        }

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new EventFlagInformation(objId, errorCode, 0, 0, 0);
        }
        else {
            Memory memory = platform.getMemory();
            return new EventFlagInformation(objId, errorCode, memory.load32(pk_robj), memory.load32(pk_robj + 4), memory.load32(pk_robj + 8));
        }
    }

    public MailboxInformation getMailboxInformation(int chip, int objId) {
        int pk_robj = BASE_ADDRESS_SYSCALL[chip] + 0x20; // pointer to result structure

        ErrorCode errorCode;
        if (chip == Constants.CHIP_FR) {
            errorCode = runFrSysCall(ITron3.SYSCALL_NUMBER_REF_MBX, pk_robj, objId);
        }
        else {
            errorCode = runTxSysCall("sys_ref_mbx", pk_robj, objId);
        }

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new MailboxInformation(objId, errorCode, 0, 0, 0);
        }
        else {
            return new MailboxInformation(objId, errorCode, platform.getMemory().load32(pk_robj), platform.getMemory().load32(pk_robj + 4), platform.getMemory().load32(pk_robj + 8));
        }
    }


    public ErrorCode setFlagIdPattern(int chip, int flagId, int pattern) {
        ErrorCode errorCode;
        if (chip == Constants.CHIP_FR) {
            // Set bits
            errorCode = runFrSysCall(ITron3.SYSCALL_NUMBER_SET_FLG, flagId, pattern);
            if (errorCode == ErrorCode.E_OK) {
                // Clr bits
                errorCode = runFrSysCall(ITron3.SYSCALL_NUMBER_CLR_FLG, flagId, pattern);
            }
        }
        else {
            // Set bits
            errorCode = runTxSysCall("sys_set_flg", flagId, pattern);
            if (errorCode == ErrorCode.E_OK) {
                // Clr bits
                errorCode = runTxSysCall("sys_clr_flg", flagId, pattern);
            }
        }

        return errorCode;
    }

    private ErrorCode runFrSysCall(int syscallNumber, int r4, int r5) {
        // Create alternate cpuState
        FrCPUState tmpCpuState = ((FrCPUState)platform.getCpuState()).createCopy();

        // Tweak alt cpuState
        tmpCpuState.I = 0; // prevent interrupts
        tmpCpuState.setILM(0, false);
        tmpCpuState.pc = BASE_ADDRESS_SYSCALL[Constants.CHIP_FR]; // point to the new code

        // Set params for call
        tmpCpuState.setReg(4, r4);
        tmpCpuState.setReg(5, r5);
        tmpCpuState.setReg(12, BinaryArithmetics.signExtend(8, syscallNumber));

        emulator.setCpuState(tmpCpuState);

        // Prepare code
        Memory memory = platform.getMemory();
        memory.store16(BASE_ADDRESS_SYSCALL[Constants.CHIP_FR], 0x1F40);                      // 1F40    INT     #0x40; R12=sys_xxx_xxx(r4=R4, r5=R5)
        memory.store16(BASE_ADDRESS_SYSCALL[Constants.CHIP_FR] + 2, 0xE0FF);                  // HALT, infinite loop

        // Put a breakpoint on the instruction after the call
        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(BASE_ADDRESS_SYSCALL[Constants.CHIP_FR] + 2, null));

        // Start emulator synchronously
        try {
            emulator.play();

            // Read error code
            return ErrorCode.fromValue(tmpCpuState.getReg(12));
        }
        catch (Throwable t) {
            t.printStackTrace();
            return ErrorCode.E_EMULATOR;
        }
    }

    private ErrorCode runTxSysCall(String sysCallName, int a0, int a1) {
        if (codeStructure == null) {
            System.err.println("Code must be disassembled with 'structure' option first to determine system calls");
        }
        else {
            Integer address = codeStructure.getAddressFromString(sysCallName);
            if (address == null) {
                System.err.println("No function called '" + sysCallName + "' was found in disassembly");
            }
            else {
                // Create alternate cpuState
                TxCPUState tmpCpuState = ((TxCPUState)platform.getCpuState()).createCopy();

                // Tweak alt cpuState
                // TODO prevent interrupts ?
                tmpCpuState.pc = BASE_ADDRESS_SYSCALL[Constants.CHIP_TX]; // point to the stub code
                tmpCpuState.is16bitIsaMode = true; // stub written in 16-bit ISA

                // Set params for call
                tmpCpuState.setReg(TxCPUState.A0, a0);
                tmpCpuState.setReg(TxCPUState.A1, a1);
                tmpCpuState.setReg(TxCPUState.V0, address + 1); // assume all Tx syscalls are in 16b ISA

                emulator.setCpuState(tmpCpuState);

                // Prepare code
                Memory memory = platform.getMemory();
                memory.store16(BASE_ADDRESS_SYSCALL[Constants.CHIP_TX], 0xEA40);                      // EA40  jalr    $v0
                memory.store16(BASE_ADDRESS_SYSCALL[Constants.CHIP_TX] + 2, 0xE0FF);                  // 6500   nop
                memory.store16(BASE_ADDRESS_SYSCALL[Constants.CHIP_TX] + 4, 0xE0FF);                  // 6500  nop

                // Put a breakpoint on the instruction after the call
                emulator.clearBreakConditions();
                emulator.addBreakCondition(new BreakPointCondition(BASE_ADDRESS_SYSCALL[Constants.CHIP_TX] + 4, null));

                // Start emulator synchronously
                try {
                    emulator.play();

                    // Read error code
                    return ErrorCode.fromValue(tmpCpuState.getReg(TxCPUState.V0));
                }
                catch (Throwable t) {
                    t.printStackTrace();
                    return ErrorCode.E_EMULATOR;
                }
            }
        }
        return ErrorCode.E_EMULATOR;
    }


}
