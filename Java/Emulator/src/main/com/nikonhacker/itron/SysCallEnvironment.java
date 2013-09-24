package com.nikonhacker.itron;

import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.emu.Platform;

/**
 * This class represents and environment suitable for system calls and presents an API for the most useful calls
 */
public abstract class SysCallEnvironment {

    protected final Platform syscallPlatform;
    protected final CPUState originalCPUState;

    public SysCallEnvironment(Platform platform) {
        originalCPUState = platform.getCpuState();
        // Using a separate platform, but sharing memory and interruptController
        syscallPlatform = new Platform(null);
        syscallPlatform.setCpuState(platform.getCpuState());
        syscallPlatform.setMemory(platform.getMemory());
        syscallPlatform.setInterruptController(platform.getInterruptController());
    }

    public abstract TaskInformation getTaskInformation(int chip, int objId);

    public abstract SemaphoreInformation getSemaphoreInformation(int chip, int objId);

    public abstract EventFlagInformation getEventFlagInformation(int chip, int objId);

    public abstract MailboxInformation getMailboxInformation(int chip, int objId);

    public abstract ErrorCode setFlagIdPattern(int chip, int flagId, int pattern);

    public abstract Class getTaskInformationClass();

    public abstract String[] getTaskPropertyNames();

    public abstract String[] getTaskColumnLabels();

    public abstract int getTaskStateColumnNumber();

}
