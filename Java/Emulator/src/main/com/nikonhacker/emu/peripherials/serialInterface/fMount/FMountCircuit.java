package com.nikonhacker.emu.peripherials.serialInterface.fMount;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.ioPort.Pin;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;

public class FMountCircuit extends SerialInterface {

    private final String name;
    
    private Pin toTXPin;
    private FromTXPin fromTXPin;
    private LensDevice lens;
    private int lensPin2,txPin2;
    
    // ------------------------ Safe methods

    public FMountCircuit(String name) {
        super(0, null, true);
        this.name = name;
        lensPin2 = 1;
        toTXPin = new Pin(this.getClass().getSimpleName() + " ~TOTXPIN pin");
        fromTXPin = new FromTXPin(this.getClass().getSimpleName() + " ~FROMTXPIN pin");
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
        throw new RuntimeException("FMountCircuit.onBitNumberChange not possible");
    }

    public String toString() {
        return name;
    }

    public Pin getFromTXPin() {
        return fromTXPin;
    }

    public Pin getToTXPin() {
        return toTXPin;
    }

    public LensDevice getLensPlugin() {
        return lens;
    }
    
    // ------------------------ Start of synchron methods

    /*
        (can be called from other UI thread)
     */    
    public synchronized void setLensPlugin(LensDevice lens) {
        if (this.lens!=null && lens!=null) {
            throw new RuntimeException("FMountCircuit: can't plug-in new lens without unplug");
        }
        if (this.lens!=null) {
            this.lens.unplug();
        }
        this.lens = lens;
        lensPin2 = 1;
        toTXPin.setOutputValue( (lensPin2==0 || txPin2==0)? 0 : 1 );
    }

    public synchronized Integer read() {
        Integer value;
        if (lens!=null) {
            value = lens.read();
        } else {
            // always sample a value
            value = (int)0xFF;
        }
        return value;
    }

    @Override
    public void readHalfDuplex() {
        targetDevice.write(read());
    }

    public synchronized void write(Integer value) {
        // F-Mount serial bus is one-wire, so transmit same byte back
        if (lens!=null) {
            // invert byte
            lens.write((~value) & 0xFF);
        }
        targetDevice.write(value);
    }

    private synchronized void setTXReadWriteValue(int value) {
        // invertor
        value = ((~value)&1);
        if (value != txPin2) {
            txPin2 = value;
            if (lens!=null) {
                // lens can only measure this signal if it didn't assert it
                if (lensPin2!=0)
                    lens.setPin2Value(value);
            }
            toTXPin.setOutputValue((lensPin2==0 || txPin2==0)? 0 : 1);
        }
    }
    
    // ------------------------ End of synchron methods

    // ------------------------ Methods called from lens (safe by design)

    /**
        Called from lens
     */
    public void setPin2Value(int value) {
        // don't need synchronisation, because called only from lens code (that is already synchronized)
        
        if (value != lensPin2) {
            lensPin2 = value;
            toTXPin.setOutputValue( (lensPin2==0 || txPin2==0)? 0 : 1 );
        }
    }

    // ------------------------ Classes for TX connected pins
    private class FromTXPin extends Pin {
        
        public FromTXPin(String name) {
            super(name);
        }

        /**
         Set value from TX output
         */        
        @Override
        public void setInputValue(int value) {
            setTXReadWriteValue(value);
        }

    }
}
