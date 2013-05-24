package com.nikonhacker.emu.peripherials.serialInterface.lcd;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SpiSlaveDevice;

/**
 * This driver is used for the LCD of the viewfinder
 * Specification is at http://rohmfs.rohm.com/en/products/databook/datasheet/ic/driver/lcd_segment/bu9795afv-e.pdf
 */
public class LcdDriver extends SpiSlaveDevice {
    private static final int NUM_NIBBLES = 35;

    private static final int MODE_SET_MASK       = 0b01100000;
    private static final int MODE_SET_VALUE      = 0b01000000;

    private static final int ADSET_MASK          = 0b01100000;
    private static final int ADSET_VALUE         = 0b00000000;

    private static final int DISCTL_MASK         = 0b01100000;
    private static final int DISCTL_VALUE        = 0b00100000;

    private static final int ICSET_MASK          = 0b01111000;
    private static final int ICSET_VALUE         = 0b01101000;

    private static final int BLKCTL_MASK         = 0b01111000;
    private static final int BLKCTL_VALUE        = 0b01110000;

    private static final int APCTL_MASK          = 0b01111100;
    private static final int APCTL_VALUE         = 0b01111100;

    // if BU9795AKV/BU9795AKS2, all are used
    // if BU9795AFV, the first 4 nibbles and last 4 nibbles are ignored
    // if BU9795AGUW, the first 2 nibbles and last 2 nibbles are ignored
    // But no difference here, only the display should be different.

    private int mask = 0xFF; // 8 bits by default

    private SerialDevice connectedDevice;
    private String name;
    private boolean isCommandMode;
    private boolean isDisplayOn;
    private int blinkFreqHalfHz;
    private int nibbleAddress = 0;

    public enum Command {}

    /** Values are stored in bytes although the memory has a number of 4-bit nibbles */
    public byte values[];

    public LcdDriver(String name) {
        super();
        this.name = name;
        reset();
    }

    private void reset() {
        values = new byte[(NUM_NIBBLES - 1) / 2 + 1];
        isDisplayOn = false;
        isCommandMode = true;
        nibbleAddress = 0;
        blinkFreqHalfHz = 0;
    }

    public byte[] getValues() {
        return values;
    }

    public boolean isDisplayOn() {
        return isDisplayOn;
    }

    @Override
    public void write(Integer value) {
        if (!selected) {
            throw new RuntimeException("LcdDriver.write(0x" + Format.asHex(value & 0xFF, 2) + ") called while chip is not SELECTed !");
        }

        if (isCommandMode) {
            // Interpret command
            if ((value & MODE_SET_MASK) == MODE_SET_VALUE) {
                // Mode Set

                // Display on/off
                isDisplayOn = Format.isBitSet(value, 3);
                // Bias Level (ignored)
//                isBiasHalf = Format.isBitSet(value, 2);
            }
            else if ((value & ADSET_MASK) == ADSET_VALUE) {
                // Address Set

                nibbleAddress = (byte) ((nibbleAddress & 0b00100000) | (value & 0b00011111));
            }
            else if ((value & DISCTL_MASK) == DISCTL_VALUE) {
                // Display Control

                // Frame Frequency (ignored)
//                switch ((value & 0b00011000) >> 3) {
//                    case 0b00:
//                        frameFrequencyHz = 80;
//                        break;
//                    case 0b01:
//                        frameFrequencyHz = 71;
//                        break;
//                    case 0b10:
//                        frameFrequencyHz = 64;
//                        break;
//                    case 0b11:
//                        frameFrequencyHz = 53;
//                        break;
//                }
                // LCD drive waveform (ignored)
//                isFrameInversion = Format.isBitSet(value, 2);
                // Power save mode (ignored)
//                switch ((value & 0b00000011)) {
//                    case 0b00:
//                        powerMode = save1;
//                        break;
//                    case 0b01:
//                        powerMode = save2;
//                        break;
//                    case 0b10:
//                        powerMode = normal;
//                        break;
//                    case 0b11:
//                        powerMode = high;
//                        break;
//                }
            }
            else if ((value & ICSET_MASK) == ICSET_VALUE) {
                // Set IC operation

                // Address set
                nibbleAddress = (byte) (((value << 3) & 0b00100000) | (nibbleAddress & 0b00011111));
                // Software reset
                if (Format.isBitSet(value, 1)) {
                    reset();
                    return;
                }
                // Select clock (ignored)
//                isExternalClock = Format.isBitSet(value, 0);
            }
            else if ((value & BLKCTL_MASK) == BLKCTL_VALUE) {
                // Blink Control
                switch ((value & 0b00000011)) {
                    case 0b00:
                        blinkFreqHalfHz = 0;
                        break;
                    case 0b01:
                        blinkFreqHalfHz = 1;
                        break;
                    case 0b10:
                        blinkFreqHalfHz = 2;
                        break;
                    case 0b11:
                        blinkFreqHalfHz = 4;
                        break;
                }
            }
            else if ((value & APCTL_MASK) == APCTL_VALUE) {
                // All pixel control

                // All display set ON
                if (Format.isBitSet(value, 1)) {
                    for (int i = 0; i < values.length; i++) {
                        values[i] = 1;
                    }
                }
                // All display set OFF (higher priority if both are set)
                if (Format.isBitSet(value, 0)) {
                    for (int i = 0; i < values.length; i++) {
                        values[i] = 0;
                    }
                }
            }

            // D7 (MSB) is bit for command or data judgment, to indicate if next byte will be command again, or data
            isCommandMode = Format.isBitSet(value, 7);
        }
        else {
            /*
             * Note: We work on a nibble basis because the spec says that writing wraps if it continues
             * after the last nibble, and the number of nibbles can be odd (35, 27, ...)
             * In that case, after wrap, hi-nibble ends up on the right and lo-nibble on the left...
             */
            setNextNibbleFrom(value); // Hi nibble
            setNextNibbleFrom(value); // Lo nibble
        }

        // System.out.println("LCD driver received 0x" + Format.asHex(value & mask, 2));
    }

    private void setNextNibbleFrom(int nibble) {
        int byteNumber = nibbleAddress/2;
        if (nibble % 2 == 0) {
            // Even nibble number => replace Hi
            values[byteNumber] = (byte) ((nibble & 0xF0) | (values[byteNumber] & 0x0F));
        }
        else {
            // Odd nibble number => replace Lo
            values[byteNumber] = (byte) ((values[byteNumber] & 0xF0) | (nibble & 0x0F));
        }
        // Increment nibble number
        nibbleAddress++;
        // Wrap if max reached
        if (nibbleAddress == NUM_NIBBLES) {
            nibbleAddress = 0;
        }
    }

    @Override
    public void connectSerialDevice(SerialDevice connectedDevice) {
        this.connectedDevice = connectedDevice;
    }

    @Override
    public void disconnectSerialDevice() {
        this.connectedDevice = null;
    }

    @Override
    public SerialDevice getConnectedSerialDevice() {
        return connectedDevice;
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
        mask = (1 << numBits) - 1;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void setSelected(boolean selected) {
        if (!this.selected && selected) {
            // transition to selected state : back to command mode
            isCommandMode = true;
            nibbleAddress = 0;
        }
        super.setSelected(selected);
    }
}
