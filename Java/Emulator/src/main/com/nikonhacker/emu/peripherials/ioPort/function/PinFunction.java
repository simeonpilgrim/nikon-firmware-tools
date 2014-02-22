package com.nikonhacker.emu.peripherials.ioPort.function;

public interface PinFunction {
    public void setLogPinMessages(boolean logPinMessages);
    public String getShortName();
    public String getFullName();
}
