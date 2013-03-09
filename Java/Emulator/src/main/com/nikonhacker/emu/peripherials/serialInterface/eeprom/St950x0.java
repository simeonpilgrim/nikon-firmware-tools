package com.nikonhacker.emu.peripherials.serialInterface.eeprom;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.DummySerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;

/**
 * This implementation is based on the ST95040 datasheet at
 * http://www.st.com/st-web-ui/static/active/en/resource/technical/document/datasheet/CD00001755.pdf
 */
public class St950x0 implements SerialDevice {

    private String name;
    private SerialDevice connectedDevice;

    public enum Command {WREN, WRDI, RDSR, WRSR, READ0, READ1, WRITE0, WRITE1}

    private Command command;
    private Integer writeAddress = 0, readAddress = 0;
    private boolean commandComplete = true;

    private boolean clockEnabled;

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
            commandComplete = true;
            return statusRegister; // will be returned over and over until next command
        }
        else if (command == Command.READ0 || command == Command.READ1) {
            int value = memory[readAddress];
            // Prepare next read by incrementing address, wrapping at 16
            readAddress = (readAddress + 1) & 0xF;
            return value;
        }
        else {
            System.err.println("Unknown state, returning 0x00");
            return 0x00;
        }
    }

    public void write(Integer value) {
        // Writing a value to serial eeprom means clock is ticking, so a value has to be transmitted back synchronously
        connectedDevice.write(read());

        if (value == null) {
            System.out.println("St950x0.write(null)");
        }
        else {
            if (commandComplete) {
                // first byte is a new command
                switch (value) {
                    case 0b0000_0110:
                        // WREN Set Write Enable Latch
                        // TODO
                        commandComplete = false;
                        break;
                    case 0b0000_0100:
                        // WRDI Reset Write Enable Latch
                        // TODO
                        commandComplete = false;
                        break;
                    case 0b0000_0101:
                        // RDSR Read Status Register
                        command = Command.RDSR;
                        commandComplete = false;
                        break;
                    case 0b0000_0001:
                        // WRSR Write Status Register
                        // TODO
                        commandComplete = false;
                        break;
                    case 0b0000_0011:
                        // READ Read Data from Memory Array (0)
                        command = Command.READ0;
                        commandComplete = false;
                        break;
                    case 0b0000_1011:
                        // READ Read Data from Memory Array (1)
                        command = Command.READ1;
                        commandComplete = false;
                        break;
                    case 0b0000_0010:
                        // WRITE Write Data to Memory Array (0)
                        command = Command.WRITE0;
                        commandComplete = false;
                        break;
                    case 0b0000_1010:
                        // WRITE Write Data to Memory Array (1)
                        command = Command.WRITE1;
                        commandComplete = false;
                        break;
                    default:
                        System.err.println("Unknown command : 0b" + Format.asBinary(value, 8));
                }
            }
            else {
                // Handle READ or WRITE
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
