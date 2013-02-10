package com.nikonhacker.realos;

import com.nikonhacker.emu.Platform;

/**
 * This class represents and environment suitable for system calls and presents an API for the most useful calls
 */
public abstract class SysCallEnvironment {

    protected final Platform platform;

    public SysCallEnvironment(Platform platform) {
        this.platform = platform;
    }

    public abstract TaskInformation getTaskInformation(int chip, int objId);

    public abstract SemaphoreInformation getSemaphoreInformation(int chip, int objId);

    public abstract EventFlagInformation getEventFlagInformation(int chip, int objId);

    public abstract MailboxInformation getMailboxInformation(int chip, int objId);

    public abstract ErrorCode setFlagIdPattern(int chip, int flagId, int pattern);
}
