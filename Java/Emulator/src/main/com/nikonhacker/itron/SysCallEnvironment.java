package com.nikonhacker.itron;

import com.nikonhacker.emu.MasterClock;
import com.nikonhacker.emu.Platform;

/**
 * This class represents and environment suitable for system calls and presents an API for the most useful calls
 */
public abstract class SysCallEnvironment {

    protected final Platform syscallPlatform;

    public SysCallEnvironment(Platform platform) {
        // Using a separate platform, but sharing memory and interruptcontroller
        MasterClock syscallMasterClock = platform.getMasterClock();
        syscallPlatform = new Platform(syscallMasterClock);
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
