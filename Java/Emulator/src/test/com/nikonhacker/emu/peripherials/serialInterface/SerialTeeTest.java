package com.nikonhacker.emu.peripherials.serialInterface;

public class SerialTeeTest {
    public static void main(String[] args) {
        System.out.println("Starting...");
        TimerDevice d1 = new TimerDevice("CPU", 2000);
        TimerDevice d2 = new TimerDevice("EEPROM", 2200);
        TestDevice d3 = new TestDevice("56pin");

        SerialTee tee = new SerialTee("tee", d1);
        tee.addBDevice(d2);
        tee.addBDevice(d3);
        tee.connect();

        // Connection is OK. Now let's insert spies
        insertConsoleWire("Spy1", d1);
        insertConsoleWire("Spy2", d2);
        insertConsoleWire("Spy3", d3);

        System.out.println("Sleeping...");
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
        }
        System.out.println("Done...");
    }

    private static void insertConsoleWire(String name, SerialDevice device) {
        SerialDevice connectedSerialDevice = device.getConnectedSerialDevice();
        // 2. we replace the above device by a logger wire, forwarding data to the original device
        device.connectSerialDevice(new ConsoleLoggerSerialWire(name + " Rx of " + device.toString(), connectedSerialDevice));
        // 3. conversely, we connect a similar logger wire in the other direction.
        connectedSerialDevice.connectSerialDevice(new ConsoleLoggerSerialWire(name + " Tx of " + device.toString(), device));

    }
}
