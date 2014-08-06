package com.nikonhacker.emu.memory.listener.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.IoActivityListener;

import java.util.*;

public class ExpeedProxyIoListener extends IoActivityListener {

    private final HashMap<Integer, IoActivityListener> listeners = new HashMap<Integer, IoActivityListener>();

    public ExpeedProxyIoListener(Platform platform, boolean logRegisterMessages) {
        super(platform, logRegisterMessages);

        IoActivityListener listener;

        // Standard FR registers
        listeners.put(0x0000, new ExpeedIoListener(platform, logRegisterMessages));
        // Image processor main component 0x4006
        listeners.put(0x4006, new Expeed4006IoListener(platform, logRegisterMessages));
        // Pin I/O port register
        listeners.put(0x5000, new ExpeedPinIoListener(platform, logRegisterMessages));
        // 63000XXX and 64000XXX
        listener = new Expeed6300IoListener(platform, logRegisterMessages);
        listeners.put(0x6300, listener);
        listeners.put(0x6400, listener);
        // 6B0000XX interrupt sharing macro in ASIC
        listeners.put(0x6B00, new Expeed6B00IoListener(platform, logRegisterMessages));
        // JPEG codec 0x40X3
        listener = new Expeed40X3IoListener(platform, logRegisterMessages);
        listeners.put(0x4003, listener);
        listeners.put(0x4013, listener);
        // Resolution converter 0x40XF and 0x4002
        listener = new Expeed4002IoListener(platform, logRegisterMessages);
        listeners.put(0x4002, listener);
        listeners.put(0x400F, listener);
        listeners.put(0x401F, listener);
        // Image Transfer 0x4018
        listeners.put(0x4018, new Expeed4018IoListener(platform, logRegisterMessages));
    }

    @Override
    public final boolean matches(int address) {
        return true;
    }

    @Override
    public final Byte onLoadData8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        final IoActivityListener listener = listeners.get(address>>>16);
        if (listener!=null)
            return listener.onLoadData8(pageData, address, value, accessSource);
        return null;
    }

    @Override
    public final Integer onLoadData16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        final IoActivityListener listener = listeners.get(address>>>16);
        if (listener!=null)
            return listener.onLoadData16(pageData, address, value, accessSource);
        return null;
    }

    @Override
    public final Integer onLoadData32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        final IoActivityListener listener = listeners.get(address>>>16);
        if (listener!=null)
            return listener.onLoadData32(pageData, address, value, accessSource);
        return null;
    }


    @Override
    public final void onStore8(byte[] pageData, int address, byte value, DebuggableMemory.AccessSource accessSource) {
        final IoActivityListener listener = listeners.get(address>>>16);
        if (listener!=null)
            listener.onStore8(pageData, address, value, accessSource);
    }

    @Override
    public final void onStore16(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        final IoActivityListener listener = listeners.get(address>>>16);
        if (listener!=null)
            listener.onStore16(pageData, address, value, accessSource);
    }

    @Override
    public final void onStore32(byte[] pageData, int address, int value, DebuggableMemory.AccessSource accessSource) {
        final IoActivityListener listener = listeners.get(address>>>16);
        if (listener!=null)
            listener.onStore32(pageData, address, value, accessSource);
    }
}
