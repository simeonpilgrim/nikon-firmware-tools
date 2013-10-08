package com.nikonhacker.emu.peripherials.serialInterface.flashCharger;

import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;

/**
 * This implementation is just a dummy stub. We don't have the spec for this proprietary Nikon chip
 */
public class Nhhs2 extends SerialInterface {

    public static final byte DUMMY_BYTE = 0x0;
    private final String name;


    private int numBits = 8;

    public Nhhs2(String name) {
        super(0, null, true);
        this.name = name;
    }

    public Integer read() {
        // This clock is due to the command being received. Return a dummy byte
        return (int) DUMMY_BYTE;
    }

    public void write(Integer value) {
            int byteValue = value & 0xFF;
//            if (!selected) {
//                throw new RuntimeException("Nhhs2.write(0x" + Format.asHex(byteValue & 0xFF, 2) + ") called while chip is not SELECTed !");
//            }

            // Writing a value to flash driver means clock is ticking, so a value has to be transmitted back synchronously
            targetDevice.write(read());
    }

    public int getNumBits() {
        return numBits;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
        this.numBits = numBits;
        System.out.println("St950x0.onBitNumberChange not implemented");
    }

    public String toString() {
        return name;
    }

}
