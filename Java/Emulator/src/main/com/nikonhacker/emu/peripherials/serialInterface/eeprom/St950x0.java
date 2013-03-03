package com.nikonhacker.emu.peripherials.serialInterface.eeprom;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.DummySerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;

import java.util.LinkedList;
import java.util.Queue;

public class St950x0 implements SerialDevice {

    private String name;
    private SerialDevice connectedDevice;

    public enum Command {WREN, WRDI, RDSR, WRSR, READ0, READ1, WRITE0, WRITE1}

    private Command command;
    private Integer writeAddress, readAddress;
    private boolean clockEnabled;

    private Queue<Integer> outputQueue = new LinkedList<Integer>();

    int statusRegister = 0b1111_0000;
    private byte[] memory = new byte[512];

    public St950x0() {
        this.name = this.getClass().getName();
    }
    public St950x0(String name) {
        this.name = name;
    }

    public Integer read() {
        if (command == Command.RDSR) {
            command = null; // RDSR can only return one byte
            return statusRegister;
        }
        else if (command == Command.READ0 || command == Command.READ1) {
            int value = memory[readAddress];
            // Prepare next read by incrementing address
            readAddress++;
            if (readAddress % 16 == 0) {
                // We've reached a new page. Wrap
                readAddress -= 16;
            }
            return value;
        }
        else {
            System.err.println("Attempt to read from " + this.getClass().getName() + " while no READ command has been issued");
            return null;
        }
    }

    public void write(Integer value) {
        if (value == null) {
            System.out.println("St950x0.write(null)");
        }
        else {
            if (!outputQueue.isEmpty()) {
                // If clock ticks while there are values waiting, use the clock to output value
                connectedDevice.write(outputQueue.poll());
            }
            if (command == null) {
                // first byte is a new command
                switch (value) {
                    case 0b0000_0110:
                        // WREN Set Write Enable Latch
                        // TODO
                        break;
                    case 0b0000_0100:
                        // WRDI Reset Write Enable Latch
                        // TODO
                        break;
                    case 0b0000_0101:
                        // RDSR Read Status Register
                        outputQueue.add(statusRegister);
                        break;
                    case 0b0000_0001:
                        // WRSR Write Status Register
                        // TODO
                        break;
                    case 0b0000_0011:
                        // READ Read Data from Memory Array (0)
                        command = Command.READ0;
                        break;
                    case 0b0000_1011:
                        // READ Read Data from Memory Array (1)
                        command = Command.READ1;
                        break;
                    case 0b0000_0010:
                        // WRITE Write Data to Memory Array (0)
                        command = Command.WRITE0;
                        break;
                    case 0b0000_1010:
                        // WRITE Write Data to Memory Array (1)
                        command = Command.WRITE1;
                        break;
                    default:
                        System.err.println("Unknown command : 0b" + Format.asBinary(value, 8));
                }
            }
            else {
                // Command READ or WRITE .
                if (writeAddress == null) {
                    // "decode" 2nd byte as an address
                    if (command == Command.READ0) {
                        // Read from page 0
                        readAddress = value;
                    }
                    else if (command == Command.READ1) {
                        // Read from page 1
                        readAddress = 0x100 & value;
                    }
                }
                else {
                    // 3rd byte is the value to store
                    switch (command) {
                        case READ0:
                        case READ1:
                            System.err.println("Error : value received (0x" + Format.asHex(value, 2) + ") while in READ command.");
                            break;
                        case WRITE0:
                            // Write to page 0
                            performWrite(writeAddress, value);
                            break;
                        case WRITE1:
                            // Write to page 1
                            performWrite(0x100 & writeAddress, value);
                            break;
                    }
                }
            }
        }
    }

    private void performWrite(Integer address, int value) {
        memory[address] = (byte) value;
    }

    public int getNumBits() {
        return 8;
    }

    public boolean isClockEnabled() {
        return clockEnabled;
    }

    public void setClockEnabled(boolean enabled) {
        this.clockEnabled = enabled;
        if (!enabled) {
            command = null;
            readAddress = null;
            writeAddress = null;
        }
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
        System.out.println("St950x0.onBitNumberChange not implemented");
    }

    public String toString() {
        return name;
    }

}
