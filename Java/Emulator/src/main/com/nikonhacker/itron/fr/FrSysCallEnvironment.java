package com.nikonhacker.itron.fr;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.disassembly.CodeStructure;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.FrEmulator;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.condition.BreakPointCondition;
import com.nikonhacker.itron.*;

/**
 * This environment is the implementation for the FR CPU
 * Here, calls are implemented thanks to INT40 software interrupt, which has an indirection table,
 * so they are called by their ID and only require declaration of the interrupt vector in dfr.txt file
 *
 * This implementations adheres to the µITRON3.0 specification
 */
public class FrSysCallEnvironment extends SysCallEnvironment {
    private static final int BASE_ADDRESS_SYSCALL = 0xFFFFFF00;

    private final FrEmulator    emulator;
    private final CodeStructure codeStructure;

    private final FrItronTaskTable taskTable;

    public FrSysCallEnvironment(Platform platform, CodeStructure codeStructure) {
        super(platform);

        // Using a separate emulator, but sharing memory and interrupt controller
        emulator = new FrEmulator(syscallPlatform);

        this.codeStructure = codeStructure;
        taskTable = new FrItronTaskTable(platform);
    }

    public TaskInformation getTaskInformation(int chip, int objId) {
        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall(ITron3.SYSCALL_NUMBER_REF_TSK, pk_robj, objId);

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new FrTaskInformation(objId, errorCode);
        }
        else {
            // empty context
            Integer addrContext = null;
            Integer nextPC = null;

            if (objId==1)
                taskTable.read(codeStructure);
            addrContext = taskTable.getContext(objId);
            nextPC = taskTable.getNextPc(objId);

            Memory memory = syscallPlatform.getMemory();
            return new FrTaskInformation(objId, errorCode,
                    memory.load32(pk_robj + 8),
                    memory.load32(pk_robj + 4),
                    memory.load32(pk_robj),
                    nextPC,
                    addrContext);
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
            Memory memory = syscallPlatform.getMemory();
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
            Memory memory = syscallPlatform.getMemory();
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
            return new MailboxInformation(objId, errorCode, syscallPlatform.getMemory().load32(pk_robj), syscallPlatform.getMemory().load32(pk_robj + 4), syscallPlatform.getMemory().load32(pk_robj + 8));
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
        FrCPUState tmpCpuState = ((FrCPUState)originalCPUState).createCopy();

        // Tweak alt cpuState
        tmpCpuState.I = 0; // prevent interrupts
        tmpCpuState.setS(0);
        tmpCpuState.setILM(0, false);
        tmpCpuState.pc = BASE_ADDRESS_SYSCALL; // point to the new code

        // Set params for call
        tmpCpuState.setReg(4, r4);
        tmpCpuState.setReg(5, r5);
        tmpCpuState.setReg(12, BinaryArithmetics.signExtend(8, syscallNumber));

        syscallPlatform.setCpuState(tmpCpuState);

        // Prepare code
        Memory memory = syscallPlatform.getMemory();
        memory.store16(BASE_ADDRESS_SYSCALL, 0x1F40);                      // 1F40    INT     #0x40; R12=sys_xxx_xxx(r4=R4, r5=R5)
        memory.store16(BASE_ADDRESS_SYSCALL + 2, 0xE0FF);                  // HALT, infinite loop

        // Put a breakpoint on the instruction after the call
        emulator.clearBreakConditions();
        emulator.addBreakCondition(new BreakPointCondition(BASE_ADDRESS_SYSCALL + 2, null));

        // Start emulator synchronously
        try {
            emulator.setContextFromPlatform(syscallPlatform);
            emulator.play();

            // Read error code
            return ErrorCode.fromFrValue(tmpCpuState.getReg(12));
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
        return new String[]{"objectIdHex", "taskState", "taskPriority", "nextPcHex", "addrContextHex", "extendedInformationHex"};
    }

    public String[] getTaskColumnLabels() {
        return new String[]{"Task Id", "State", "Priority", "Next PC", "Context addr", "Extended Information"};
    }

    @Override
    public int getTaskStateColumnNumber() {
        return 1;
    }
}
