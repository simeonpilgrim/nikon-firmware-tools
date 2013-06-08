package com.nikonhacker.emu.peripherials.ioPort.function;

public class IoPinOutputFunction extends AbstractOutputPinFunction {
    private String pinName;

    public IoPinOutputFunction(String componentName, String pinName) {
        super(componentName);
        this.pinName = pinName;
    }

    @Override
    public Integer getValue(Integer defaultOutputValue) {
        return defaultOutputValue;
    }

    @Override
    public String getShortName() {
        return pinName + "OUT";
    }

    @Override
    public String getFullName() {
        return componentName + " Output Pin " + pinName;
    }
}
