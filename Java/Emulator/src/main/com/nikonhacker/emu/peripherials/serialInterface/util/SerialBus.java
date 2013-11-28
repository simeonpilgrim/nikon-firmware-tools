package com.nikonhacker.emu.peripherials.serialInterface.util;

import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SpiSlaveDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a "bus" connector that allows several slaves devices to be connected to the same master SerialDevice
 * This class links one master device and zero or more slave devices.
 * Bytes sent by master are forwarded to all slaves
 * Bytes sent by any slave are forwarded to the master
 *
 * Note: this implementation may seem overly complex, but the contract is that it should be possible to temporarily
 * replace devices at one or the other end (e.g. a ConsoleLoggerSerialWire for logging purpose) and remove them
 * without disturbing the normal behaviour
 *
 * Use case:
 * - before (no bus):
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
 * SerialInterface txSerialInterfaceH2 = platform[Constants.CHIP_TX].getSerialInterfaces()[TxIoListener.NUM_SERIAL_IF + 2]; // master
 * St950x0 eeprom = new St95040("Eeprom"); // slave 1
 * TestDevice testDevice = new TestDevice(); // slave 2
 *
 * eeprom.disconnectSerialDevice();
 * txSerialInterfaceH2.disconnectSerialDevice();
 * testDevice.disconnectSerialDevice();
 *
 * SerialBus bus = new SerialBus("bus", txSerialInterfaceH2);
 * bus.addBDevice(eeprom)
 * bus.addBDevice(testDevice);
 * bus.connect();
 */
public class SerialBus {
    private   String                    busName;
    private   SerialDevice              masterDevice;
    private   List<SerialDevice>        slaveDevices;
    private   InternalMasterPartner     internalMasterPartner;
    protected List<InternalSlavePartner> internalSlavePartners;

    public SerialBus(String busName, SerialDevice masterDevice) {
        this.busName = busName;
        this.masterDevice = masterDevice;
        this.slaveDevices = new ArrayList<>();
    }

    public void addSlaveDevice(SerialDevice slaveDevice) {
        if (internalMasterPartner != null) {
            throw new RuntimeException("Bus connection has already happened. Cannot add more slaves");
        }
        slaveDevices.add(slaveDevice);
    }


    public void connect() {
        internalMasterPartner = getInternalMasterPartner();
        internalMasterPartner.connectTargetDevice(masterDevice);
        masterDevice.connectTargetDevice(internalMasterPartner);

        this.internalSlavePartners = new ArrayList<InternalSlavePartner>();
        for (SerialDevice slaveDevice : slaveDevices) {
            InternalSlavePartner slavePartner = new InternalSlavePartner();
            slavePartner.connectTargetDevice(slaveDevice);
            slaveDevice.connectTargetDevice(slavePartner);
            internalSlavePartners.add(slavePartner);
        }
    }

    protected InternalMasterPartner getInternalMasterPartner() {
        return new InternalMasterPartner();
    }

    public String getBusName() {
        return busName;
    }

    /**
     * In this implementation, targetDevice is the master Device
     */
    protected class InternalMasterPartner extends SpiSlaveDevice {

        public InternalMasterPartner() {
        }

        /**
         * Values are forwarded to each of the attached slaves
         * @param value
         */
        @Override
        public void write(Integer value) {
            for (InternalSlavePartner internalSlavePartner : internalSlavePartners) {
                internalSlavePartner.reverseWrite(value);
            }
        }

        @Override
        public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
            System.out.println("SerialTee$InternalAPartner.onBitNumberChange");
        }

        public void reverseWrite(Integer value) {
            targetDevice.write(value);
        }
    }

    /**
     * In this implementation, targetDevice is the slave Device
     */
    protected class InternalSlavePartner extends SpiSlaveDevice {

        public InternalSlavePartner() {
        }

        /**
         * Values from each of the attached slaves are forwarded to the master
         * @param value
         */
        @Override
        public void write(Integer value) {
            internalMasterPartner.reverseWrite(value);
        }

        @Override
        public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
            System.out.println("SerialTee$InternalBPartner.onBitNumberChange");
        }

        public void reverseWrite(Integer value) {
            targetDevice.write(value);
        }
    }
}
