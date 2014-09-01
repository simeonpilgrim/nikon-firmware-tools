package com.nikonhacker.emu.peripherials.serialInterface.sensorBridge;

import com.nikonhacker.emu.peripherials.ioPort.Pin;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;
import com.nikonhacker.emu.Platform;

public class Ei155 extends SerialInterface {

    private final static String name = "EI155";

    private int[] memory = new int[0x30];

    private Pin cs;
    private Pin rdy;

    private Integer address = null;
    private Integer chipId = null;
    private boolean csHigh;
    private int startBit;
    private int writtenValue;

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
        rdy = new Pin(this.getClass().getSimpleName() + " RDY pin");
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

    public final Pin getRdyPin() {
        return rdy;
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
                writtenValue = 0;
            } else {
                if (chipId==0x45) {
                    writtenValue |= ((value & 0xFF)<< startBit);

                    if (startBit==0) {
                        memory[address] = writtenValue;
                        switch (address) {
                            // the real trigger is not known, INT 0x10 happens in reality 5ms after this command
                            case 0x12: rdy.setOutputValue(0); break;
                            case 0x13: rdy.setOutputValue(1); break;
                        }
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
