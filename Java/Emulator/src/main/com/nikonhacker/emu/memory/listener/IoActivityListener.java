package com.nikonhacker.emu.memory.listener;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;

/**
 * An IO Activity Listener is hooked to a page of memory addresses meant to contain IO registers
 */
public abstract class IoActivityListener implements MemoryActivityListener {
    protected final Platform platform;
    protected       boolean  logRegisterMessages;

    public IoActivityListener(Platform platform, boolean logRegisterMessages) {
        this.platform = platform;
        this.logRegisterMessages = logRegisterMessages;
    }

    /**
     * This method declares if this listener does only read the information, or if it can change the returned value
     * @return
     */
    @Override
    public boolean isReadOnly() {
        return false;
    }

    public void setLogRegisterMessages(boolean logRegisterMessages) {
        this.logRegisterMessages = logRegisterMessages;
    }

    @Override
    public void onLoadInstruction8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        // Do nothing. Loading instruction from register has no sense
    }

    @Override
    public void onLoadInstruction16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        // Do nothing. Loading instruction from register has no sense
    }

    @Override
    public void onLoadInstruction32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        // Do nothing. Loading instruction from register has no sense
    }

    protected void warn(String message) {
        System.err.println(message + " at 0x" + Format.asHex(platform.getCpuState().pc, 8));
    }

    protected void stop(String message) {
        throw new RuntimeException(message + " at 0x" + Format.asHex(platform.getCpuState().pc, 8));
    }
}
