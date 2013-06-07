package com.nikonhacker.emu.peripherials.ioPort.function.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.function.AbstractInputPinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.tx.TxTimer;

public class TxIoPinTimerInputFunction extends AbstractInputPinFunction implements PinFunction {
    private TxTimer timer;
    private int     inputNumber;
    private int     previousValue = -1;

    public TxIoPinTimerInputFunction(ProgrammableTimer timer, int inputNumber) {
        super(Constants.CHIP_LABEL[Constants.CHIP_TX]);
        this.timer = (TxTimer)timer;
        this.inputNumber = inputNumber;
    }

    public TxIoPinTimerInputFunction(ProgrammableTimer timerNumber) {
        this(timerNumber, -1);
    }

    @Override
    public String getFullName() {
        return componentName + " " + timer.toString() + ", input " + inputNumber;
    }

    @Override
    public String getShortName() {
        return "TB" + timer.getTimerNumber() + "IN" + (inputNumber == -1?"":(""+inputNumber));
    }

    @Override
    public void setValue(int value) {
        if (previousValue != value) {
            if (inputNumber == 0) {
                // IN0
                if (timer.getModClk() == 0b00) {
                    // Event counter mode
                    timer.increment();
                }
                else {
                    // Capture mode
                    int modCpm = timer.getModCpm();
                    if (value == 1) {
                        // Rising edge
                        if (modCpm == 0b01 || modCpm == 0b10) {
                            // Capture TBnCP0 on rising of TBnIN0
                            timer.capture0();
                        }
                    }
                    else {
                        // Falling edge
                        if (modCpm == 0b10) {
                            // Capture TBnCP1 on falling of TBnIN0
                            timer.capture1();
                        }
                    }
                }
            }
            else {
                // IN1
                if (value == 1) {
                    // Rising edge
                    if (timer.getModCpm() == 0b01) {
                        // Capture TBnCP1 on rising of TBnIN1
                        timer.capture0();
                    }
                }
            }
            previousValue = value;
        }
    }

}
