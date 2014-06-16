package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.Format;

import java.util.Timer;
import java.util.TimerTask;

public class TimerDevice extends SerialDevice {
    private int i = 0;
    private String deviceName;

    public TimerDevice(String name, long interval) {
        this.deviceName = name;
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (targetDevice == null) {
                    System.out.println(deviceName + " cannot send value 0x" + Format.asHex(i, 2));
                }
                else {
                    System.out.println(deviceName + " sends 0x" + Format.asHex(i, 2));
                    targetDevice.write(i);
                }
                i++;
            }
        }, interval, interval);
    }

    @Override
    public void write(Integer value) {
        System.out.println("        " + deviceName + " receives " + ((value == null)?"null":("0x" + Format.asHex(value, 2))));
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
    }

    @Override
    public String toString() {
        return deviceName;
    }
}
