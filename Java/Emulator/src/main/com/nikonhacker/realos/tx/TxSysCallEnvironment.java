package com.nikonhacker.realos.tx;

import com.nikonhacker.disassembly.CodeStructure;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.TxEmulator;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.trigger.condition.BreakPointCondition;
import com.nikonhacker.realos.*;

/**
 * This environment is the implementation for the TX CPU
 * Here, calls are standard functions, so they are found by name in the dtx.txt file
 * and require an "Analyse/Disassemble" phase
 *
 * This implementations adheres to the µITRON4.0 specification, but has some workarounds due to limitations of the implementation
 */
public class TxSysCallEnvironment extends SysCallEnvironment {
    private static final int BASE_ADDRESS_SYSCALL = 0x10000000;

    private static final int MAX_FLAG_ID    = 0xF;
    private static final int MAX_MAILBOX_ID = 0x3;
    private static final int MAX_SEM_ID     = 0xF;

    private final TxEmulator emulator;
    private final CodeStructure codeStructure;

    public TxSysCallEnvironment(Platform platform, CodeStructure codeStructure) {
        super(platform);
        this.codeStructure = codeStructure;
        // Use a separate emulator, but sharing memory
        this.emulator = new TxEmulator();
        this.emulator.setMemory(platform.getMemory());
    }

    public TaskInformation getTaskInformation(int chip, int objId) {
        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall("sys_ref_tsk", objId, pk_robj);

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new TxTaskInformation(objId, errorCode);
        }
        else {
            Memory memory = platform.getMemory();
            int stateValue = memory.load32(pk_robj);
            /* Strangely, the TX implementation always returns 0 (OK) as error code, even when task_id does not exist (optimization ?).
               Let's return "non existent object" if state is 0.
            */
            if (stateValue == 0) {
                return new TxTaskInformation(objId, ErrorCode.E_ID);
            }
            else {
                return new TxTaskInformation(objId, errorCode,
                        stateValue,
                        memory.load32(pk_robj + 4),
                        memory.load32(pk_robj + 8),
                        memory.load32(pk_robj + 12),
                        memory.load32(pk_robj + 16),
                        memory.load32(pk_robj + 20),
                        memory.load32(pk_robj + 24),
                        memory.load32(pk_robj + 28),
                        memory.load32(pk_robj + 32));
            }
        }
    }

    public SemaphoreInformation getSemaphoreInformation(int chip, int objId) {
        // In TX implementation, there's no way to determine the range of valid objects by looping until error.
        // So stop at hardcoded limit.
        if (objId > MAX_SEM_ID) {
            return new SemaphoreInformation(objId, ErrorCode.E_NOID, 0, 0, 0);
        }

        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall("sys_ref_sem", objId, pk_robj);

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new SemaphoreInformation(objId, errorCode, 0, 0, 0);
        }
        else {
            Memory memory = platform.getMemory();
            // Note: structure is different from RealOS 3
            return new SemaphoreInformation(objId, errorCode, 0, memory.load32(pk_robj), memory.load32(pk_robj + 4));
        }
    }

    public EventFlagInformation getEventFlagInformation(int chip, int objId) {
        // In TX implementation, there's no way to determine the range of valid objects by looping until error.
        // So stop at hardcoded limit.
        if (objId > MAX_FLAG_ID) {
            return new EventFlagInformation(objId, ErrorCode.E_NOID, 0, 0, 0);
        }

        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall("sys_ref_flg", objId, pk_robj);

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new EventFlagInformation(objId, errorCode, 0, 0, 0);
        }
        else {
            Memory memory = platform.getMemory();
            // Note: structure is different from RealOS 3
            return new EventFlagInformation(objId, errorCode, 0, memory.load32(pk_robj), memory.load32(pk_robj + 4));
        }
    }

    public MailboxInformation getMailboxInformation(int chip, int objId) {
        // In TX implementation, there's no way to determine the range of valid objects by looping until error.
        // So stop at hardcoded limit.
        if (objId > MAX_MAILBOX_ID) {
            return new MailboxInformation(objId, ErrorCode.E_NOID, 0, 0, 0);
        }

        int pk_robj = BASE_ADDRESS_SYSCALL + 0x20; // pointer to result structure

        ErrorCode errorCode = runSysCall("sys_ref_mbx", objId, pk_robj);

        // Interpret result
        if (errorCode != ErrorCode.E_OK) {
            return new MailboxInformation(objId, errorCode, 0, 0, 0);
        }
        else {
            Memory memory = platform.getMemory();
            return new MailboxInformation(objId, errorCode, 0, memory.load32(pk_robj), memory.load32(pk_robj + 4));
        }
    }

    public ErrorCode setFlagIdPattern(int chip, int flagId, int pattern) {
        // Set bits
        ErrorCode errorCode = runSysCall("sys_set_flg", flagId, pattern);  // TODO check order
        if (errorCode == ErrorCode.E_OK) {
            // Clr bits
            errorCode = runSysCall("sys_clr_flg", flagId, pattern);  // TODO check order
        }

        return errorCode;
    }


    private ErrorCode runSysCall(String sysCallName, int a0, int a1) {
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
                tmpCpuState.clearStatusIE(); // prevent interrupts
                tmpCpuState.pc = BASE_ADDRESS_SYSCALL; // point to the stub code
                tmpCpuState.is16bitIsaMode = true; // stub written in 16-bit ISA

                // Set params for call
                tmpCpuState.setReg(TxCPUState.A0, a0);
                tmpCpuState.setReg(TxCPUState.A1, a1);
                tmpCpuState.setReg(TxCPUState.V0, address + 1); // assume all Tx syscalls are in 16b ISA

                emulator.setCpuState(tmpCpuState);

                // Prepare code
                Memory memory = platform.getMemory();
                memory.store16(BASE_ADDRESS_SYSCALL, 0xEA40);                      // EA40  jalr    $v0
                memory.store16(BASE_ADDRESS_SYSCALL + 2, 0x6500);                  // 6500    nop
                memory.store16(BASE_ADDRESS_SYSCALL + 4, 0x6500);                  // 6500  nop

                // Put a breakpoint on the instruction after the call
                emulator.clearBreakConditions();
                emulator.addBreakCondition(new BreakPointCondition(BASE_ADDRESS_SYSCALL + 4, null));

                // Start emulator synchronously
                try {
                    emulator.play();

                    // Read error code
                    return ErrorCode.fromTxValue(tmpCpuState.getReg(TxCPUState.V0));
                }
                catch (Throwable t) {
                    t.printStackTrace();
                    return ErrorCode.E_EMULATOR;
                }
            }
        }
        return ErrorCode.E_EMULATOR;
    }

    public Class getTaskInformationClass() {
        return TxTaskInformation.class;
    }

    public String[] getTaskPropertyNames() {
        return new String[]{"objectIdHex", "taskState", "taskPriority", "taskBasePriority", "reasonForWaiting", "objectIdWaiting", "timeLeft", "actRequestCount", "wuRequestCount", "suspendCount"};
    }

    public String[] getTaskColumnLabels() {
        return new String[]{"Task Id", "State", "Priority", "Base Prio", "Wait Reason", "Wait Id", "Time Left", "ActReqCount", "WkUpReqCount", "SuspendCount"};
    }

    @Override
    public int getTaskStateColumnNumber() {
        return 1;
    }
}
