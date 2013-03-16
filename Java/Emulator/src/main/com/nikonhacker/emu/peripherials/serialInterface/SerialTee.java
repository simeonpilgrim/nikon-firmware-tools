package com.nikonhacker.emu.peripherials.serialInterface;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is a "T" connector that allows several devices to be connected to the same SerialDevice
 * This class links one "A" device and zero or more "B" devices.
 * Bytes sent by "A" are forwarded to all "B"
 * Bytes sent by any "B" are forwarded to "A"
 *
 * Use case:
 * - before (no tee):
 * St950x0 eeprom = new St95040("Eeprom");
 * SerialInterface txSerialInterfaceH2 = platform[Constants.CHIP_TX].getSerialInterfaces()[TxIoListener.NUM_SERIAL_IF + 2];
 *
 * eeprom.disconnectSerialDevice();
 * txSerialInterfaceH2.disconnectSerialDevice();
 *
 * txSerialInterfaceH2.connectSerialDevice(eeprom);
 * eeprom.connectSerialDevice(txSerialInterfaceH2);
 *
 * - after:
 * SerialInterface txSerialInterfaceH2 = platform[Constants.CHIP_TX].getSerialInterfaces()[TxIoListener.NUM_SERIAL_IF + 2]; // A
 * St950x0 eeprom = new St95040("Eeprom"); // B
 * TestDevice testDevice = new TestDevice(); // B
 *
 * eeprom.disconnectSerialDevice();
 * txSerialInterfaceH2.disconnectSerialDevice();
 * testDevice.disconnectSerialDevice();
 *
 * SerialTee tee = new SerialTee("tee", txSerialInterfaceH2);
 * tee.addTargetDevice(eeprom)
 * tee.addTargetDevice(testDevice);
 * tee.connect();
 */
public class SerialTee {
    private String teeName;
    private SerialDevice aDevice;
    private Set<SerialDevice> bDevices;
    private SerialDevice internalAPartner;
    private Set<SerialDevice> internalBPartners;

    public SerialTee(String teeName, SerialDevice aDevice) {
        this.teeName = teeName;
        this.aDevice = aDevice;
        this.bDevices = new HashSet<SerialDevice>();
    }

    public void addBDevice(SerialDevice bDevice) {
        if (internalAPartner != null) {
            throw new RuntimeException("Tee is already connected. Cannot add more devices");
        }
        bDevices.add(bDevice);
    }


    public void connect() {
        internalAPartner = new InternalAPartner();
        internalAPartner.connectSerialDevice(aDevice);
        aDevice.connectSerialDevice(internalAPartner);
        this.internalBPartners = new HashSet<SerialDevice>(bDevices.size());
        for (SerialDevice bDevice : bDevices) {
            InternalBPartner bPartner = new InternalBPartner();
            bPartner.connectSerialDevice(bDevice);
            bDevice.connectSerialDevice(bPartner);
            internalBPartners.add(bPartner);
        }
    }

    public String getTeeName() {
        return teeName;
    }

    private class InternalAPartner implements SerialDevice {
        private SerialDevice aDevice;

        public InternalAPartner() {
        }

        @Override
        public void write(Integer value) {
            for (SerialDevice targetDevice : bDevices) {
                targetDevice.write(value);
            }
        }

        @Override
        public void connectSerialDevice(SerialDevice aDevice) {
            this.aDevice = aDevice;
        }

        @Override
        public void disconnectSerialDevice() {
            this.aDevice = new DummySerialDevice();
        }

        @Override
        public SerialDevice getConnectedSerialDevice() {
            return aDevice;
        }

        @Override
        public void onBitNumberChange(SerialDevice serialDevice, int nbBits) {
            System.out.println("SerialTee$InternalAPartner.onBitNumberChange");
        }
    }

    private class InternalBPartner implements SerialDevice {

        private SerialDevice bDevice;

        public InternalBPartner() {
        }

        @Override
        public void write(Integer value) {
            aDevice.write(value);
        }

        @Override
        public void connectSerialDevice(SerialDevice bDevice) {
            this.bDevice = bDevice;
        }

        @Override
        public void disconnectSerialDevice() {
            this.bDevice = new DummySerialDevice();
        }

        @Override
        public SerialDevice getConnectedSerialDevice() {
            return bDevice;
        }

        @Override
        public void onBitNumberChange(SerialDevice serialDevice, int nbBits) {
            System.out.println("SerialTee$InternalBPartner.onBitNumberChange");
        }
    }
}
