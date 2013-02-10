package com.nikonhacker.realos.fr;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.FrEmulator;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.condition.BreakPointCondition;
import com.nikonhacker.realos.*;

/**
 * This environment is the implementation for the FR CPU
 * Here, calls are implemented thanks to INT40 software interrupt, which has an indirection table,
 * so they are called by their ID and only require declaration of the interrupt vector in dfr.txt file
 *
 * This implementations adheres to the µITRON3.0 specification
 */
public class FrSysCallEnvironment extends SysCallEnvironment {
    private static final int BASE_ADDRESS_SYSCALL = 0xFFFFFF00;

    private final FrEmulator emulator;

    public FrSysCallEnvironment(Platform platform) {
        super(platform);
        // Use a separate emulator, but sharing memory and interrupt controller
        emulator = new FrEmulator();
        emulator.setMemory(platform.getMemory());
        emulator.setInterruptController(platform.getInterruptController());
    }

    public TaskInformation getTaskInformation(int chip, int objId) {
        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall(ITron3.SYSCALL_NUMBER_REF_TSK, pk_robj, objId);

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new FrTaskInformation(objId, errorCode);
        }
        else {
            Memory memory = platform.getMemory();
            return new FrTaskInformation(objId, errorCode,
                    memory.load32(pk_robj + 8),
                    memory.load32(pk_robj + 4),
                    memory.load32(pk_robj));
        }
    }

    public SemaphoreInformation getSemaphoreInformation(int chip, int objId) {
        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall(ITron3.SYSCALL_NUMBER_REF_SEM, pk_robj, objId);

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new SemaphoreInformation(objId, errorCode);
        }
        else {
            Memory memory = platform.getMemory();
            return new SemaphoreInformation(objId, errorCode, memory.load32(pk_robj), memory.load32(pk_robj + 4), memory.load32(pk_robj + 8));
        }
    }

    public EventFlagInformation getEventFlagInformation(int chip, int objId) {
        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall(ITron3.SYSCALL_NUMBER_REF_FLG, pk_robj, objId);

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
        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall(ITron3.SYSCALL_NUMBER_REF_MBX, pk_robj, objId);

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new MailboxInformation(objId, errorCode, 0, 0, 0);
        }
        else {
            return new MailboxInformation(objId, errorCode, platform.getMemory().load32(pk_robj), platform.getMemory().load32(pk_robj + 4), platform.getMemory().load32(pk_robj + 8));
        }
    }

    public ErrorCode setFlagIdPattern(int chip, int flagId, int pattern) {
        // Set bits
        ErrorCode errorCode = runSysCall(ITron3.SYSCALL_NUMBER_SET_FLG, flagId, pattern);
        if (errorCode == ErrorCode.E_OK) {
            // Clr bits
            errorCode = runSysCall(ITron3.SYSCALL_NUMBER_CLR_FLG, flagId, pattern);
        }

        return errorCode;
    }

    private ErrorCode runSysCall(int syscallNumber, int r4, int r5) {
        // Create alternate cpuState
        FrCPUState tmpCpuState = ((FrCPUState)platform.getCpuState()).createCopy();

        // Tweak alt cpuState
        tmpCpuState.I = 0; // prevent interrupts
        tmpCpuState.setILM(0, false);
        tmpCpuState.pc = BASE_ADDRESS_SYSCALL; // point to the new code

        // Set params for call
        tmpCpuState.setReg(4, r4);
        tmpCpuState.setReg(5, r5);
        tmpCpuState.setReg(12, BinaryArithmetics.signExtend(8, syscallNumber));

        emulator.setCpuState(tmpCpuState);

        // Prepare code
        Memory memory = platform.getMemory();
        memory.store16(BASE_ADDRESS_SYSCALL, 0x1F40);                      // 1F40    INT     #0x40; R12=sys_xxx_xxx(r4=R4, r5=R5)
        memory.store16(BASE_ADDRESS_SYSCALL + 2, 0xE0FF);                  // HALT, infinite loop

        // Put a breakpoint on the instruction after the call
        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(BASE_ADDRESS_SYSCALL + 2, null));

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

    public Class getTaskInformationClass() {
        return FrTaskInformation.class;
    }

    public String[] getTaskPropertyNames() {
        return new String[]{"objectIdHex", "taskState", "taskPriority", "extendedInformationHex"};
    }

    public String[] getTaskColumnLabels() {
        return new String[]{"Task Id", "State", "Priority", "Extended Information"};
    }
}
