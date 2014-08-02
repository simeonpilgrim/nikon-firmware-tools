package com.nikonhacker.emu.peripherials.serialInterface.sensorBridge;

import com.nikonhacker.emu.peripherials.ioPort.Pin;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;
import com.nikonhacker.emu.Platform;

public class Ei155 extends SerialInterface {

    private final static String name = "EI155";

    private int[] memory = new int[0x30];

    private Pin cs;

    private Integer address = null;
    private Integer chipId = null;
    private boolean csHigh;
    private int startBit;

    // ------------------------ Safe methods

    public Ei155(Platform  platform) {
        super(0, platform, true);
        startBit = 16;

        cs = new Pin(this.getClass().getSimpleName() + " CS pin") {
            @Override
            public final void setInputValue(int value) {
                setCs(value);
            }
        };
    }

    public int getNumBits() {
        return 8;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onBitNumberChange(SerialDevice serialDevice, int numBits) {
        throw new RuntimeException(name+".onBitNumberChange not possible");
    }

    public String toString() {
        return name;
    }

    public final Pin getCsPin() {
        return cs;
    }

    public int[] getMemory() {
        return memory;
    }

    private final boolean isCommunicationEnabled() {
        if (!csHigh)
            return true;
        return false;
    }

    public Integer read() {
        throw new RuntimeException(name + ".read not possible");
    }

    // ------------------------ Start of synchron methods

    public void write(Integer value) {
        if (isCommunicationEnabled()) {
            if (chipId == null) {
                chipId = value & 0xFF;
            } else if (address == null) {
                address = value & 0xFF;
                memory[address] = 0;
            } else {
                if (chipId==0x45) {
                    memory[address] |= ((value & 0xFF)<< startBit);

                    if (startBit==0) {
                        address = null;
                        startBit = 16;
                    } else {
                        startBit -= 8;
                    }
                } else
                    System.out.println(name + ": unsupported chipID=" + chipId);
            }
        } else {
            System.out.println(name + ": receive byte with disabled communication");
        }
    }

    private final void setCs(int value) {
        csHigh = (value == 1 ? true : false);
        if (csHigh) {
            chipId = null;
            address = null;
            startBit = 16;
        }
    }
    // ------------------------ End of synchron methods

}
