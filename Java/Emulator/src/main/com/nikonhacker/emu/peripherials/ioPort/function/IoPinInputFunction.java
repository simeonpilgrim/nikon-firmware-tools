package com.nikonhacker.emu.peripherials.ioPort.function;

public class IoPinInputFunction extends AbstractInputPinFunction implements PinFunction {
    private String pinName;

    public IoPinInputFunction(String componentName, String pinName) {
        super(componentName);
        this.pinName = pinName;
    }

    @Override
    public void setValue(int value) {
        // do nothing
    }

    @Override
    public String getShortName() {
        return pinName + "IN";
    }

    @Override
    public String getFullName() {
        return componentName + " Input Pin " + pinName;
    }
}
