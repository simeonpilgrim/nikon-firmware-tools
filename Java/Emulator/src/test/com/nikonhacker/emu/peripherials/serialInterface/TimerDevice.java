package com.nikonhacker.emu.peripherials.serialInterface;

import com.nikonhacker.Format;

import java.util.Timer;
import java.util.TimerTask;

public class TimerDevice implements SerialDevice {
    private SerialDevice connectedDevice;
    private int i = 0;
    private String deviceName;

    public TimerDevice(String name, long interval) {
        this.deviceName = name;
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (connectedDevice == null) {
                    System.out.println(deviceName + " cannot send value 0x" + Format.asHex(i, 2));
                }
                else {
                    System.out.println(deviceName + " sends 0x" + Format.asHex(i, 2));
                    connectedDevice.write(i);
                }
                i++;
            }
        }, interval, interval);
    }

    @Override
    public void write(Integer value) {
        System.out.println("        " + deviceName + " received " + ((value == null)?"null":(" 0x" + Format.asHex(value, 2))));
    }

    @Override
    public void connectSerialDevice(SerialDevice connectedDevice) {
        this.connectedDevice = connectedDevice;
    }

    @Override
    public void disconnectSerialDevice() {
        this.connectedDevice = new DummySerialDevice();
    }

    @Override
    public SerialDevice getConnectedSerialDevice() {
        return connectedDevice;
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int nbBits) {

    }
}
