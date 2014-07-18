package com.nikonhacker.emu.peripherials.serialInterface.imageSensor;

import com.nikonhacker.emu.peripherials.ioPort.Pin;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;
import com.nikonhacker.emu.Platform;

public class Imx071 extends SerialInterface {

    private final static String name = "IMX071";

    private byte[] memory = new byte[256];

    private Pin xce;
    private Pin sck;
    private Pin sdi;
    private Pin xvs;
    private Pin xhs;

    private Integer address = null;
    private Integer chipId = null;
    private boolean xceHigh;
    private boolean sckHigh;
    private boolean sdiHigh;
    // ------------------------ Safe methods

    public Imx071(Platform  platform) {
        super(0, platform, true);

        xce = new Pin(this.getClass().getSimpleName() + " XCE pin") {
            @Override
            public final void setInputValue(int value) {
                setXce(value);
            }
        };
        // which one exactly is SDI or SCK is not known
        sck = new Pin(this.getClass().getSimpleName() + " SCK pin") {
            @Override
            public final void setInputValue(int value) {
                setSck(value);
            }
        };
        sdi = new Pin(this.getClass().getSimpleName() + " SDI pin") {
            @Override
            public final void setInputValue(int value) {
                setSdi(value);
            }
        };
        xvs = new Pin(this.getClass().getSimpleName() + " XVS pin") {
            @Override
            public final void setInputValue(int value) {
            }
        };
        xhs = new Pin(this.getClass().getSimpleName() + " XHS pin") {
            @Override
            public final void setInputValue(int value) {
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
        throw new RuntimeException("Imx071.onBitNumberChange not possible");
    }

    public String toString() {
        return name;
    }

    public final Pin getXcePin() {
        return xce;
    }

    public final Pin getSckPin() {
        return sck;
    }

    public final Pin getSdiPin() {
        return sdi;
    }

    public final Pin getXvsPin() {
        return xvs;
    }

    public final Pin getXhsPin() {
        return xhs;
    }

    public byte[] getMemory() {
        return memory;
    }

    private final boolean isCommunicationEnabled() {
        if (!xceHigh && sckHigh && sdiHigh)
            return true;
        return false;
    }

    public Integer read() {
        throw new RuntimeException("Imx071.read not possible");
    }

    // ------------------------ Start of synchron methods

    public void write(Integer value) {
        if (isCommunicationEnabled()) {
            if (chipId == null) {
                chipId = value & 0xFF;
            } else if (address == null) {
                address = value & 0xFF;
            } else {
                if (chipId==2)
                    memory[address] = (byte)(value & 0xFF);
                else
                    System.out.println("IMX071: unsupported chipID=" + chipId);
                address++;
            }
        } else {
            System.out.println("IMX071: receive byte with disabled communication");
        }
    }

    private final void setXce(int value) {
        xceHigh = (value == 1 ? true : false);
        if (xceHigh) {
            address = null;
            chipId = null;
        }
    }
    private final void setSck(int value) {
        sckHigh = (value == 1 ? true : false);
    }

    private final void setSdi(int value) {
        sdiHigh = (value == 1 ? true : false);
    }

    // ------------------------ End of synchron methods

}
