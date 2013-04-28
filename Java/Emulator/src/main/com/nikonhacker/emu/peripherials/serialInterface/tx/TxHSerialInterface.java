package com.nikonhacker.emu.peripherials.serialInterface.tx;

import com.nikonhacker.emu.Emulator;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;

public class TxHSerialInterface extends TxSerialInterface {
    private static final int HSERIAL_RX_FIFO_SIZE = 32;

    public TxHSerialInterface(int serialInterfaceNumber, InterruptController interruptController, Emulator emulator) {
        super(serialInterfaceNumber, interruptController, emulator);
    }

    @Override
    public String getName() {
        return "Tx HSerial #" + serialInterfaceNumber;
    }

    /**
     * Compute Rx FIFO Fill Level to generate interrupts
     * Overridden because it is much larger on hi-speed than normal serial interfaces
     */
    @Override
    protected void computeRxFillLevel() {
        rxInterruptFillLevel = rfc & 0b11111; // RIL is on 5 bits, independent of Half/Full duplex
        if (rxInterruptFillLevel == 0) {
            // Special case
            rxInterruptFillLevel = 32;
        }
    }

    /**
     * Compute Tx FIFO Fill Level to generate interrupts
     * Overridden because it is much larger on hi-speed than normal serial interfaces
     */
    @Override
    protected void computeTxFillLevel() {
        // According to the specification, TIL is on 6 bits, independent of Half/Full duplex
        // txInterruptFillLevel = tfc & 0b111111; // 6 bits

        // However, the code writes 0xA0 to tfc for 2 bytes sent, which would mean 32 (not really meaningul).
        // Moreover, RIL is 5 bits, and the example at page 15-14, it says "HSC0TFC <5:0> = 00000" with 5 zeroes...
        // So let's consider it is 5 bits
        txInterruptFillLevel = tfc & 0b11111; // 5 bits, independent of Half/Full duplex
        if (txInterruptFillLevel > 32) {
            throw new RuntimeException(getName() + " error : HSC0TFC<TIL5:1> is more than 32 (" + txInterruptFillLevel + ")");
        }
    }

    @Override
    protected int getMaxFifoSize() {
        if (getMod1Fdpx() == 0b11) {
            return HSERIAL_RX_FIFO_SIZE / 2;
        }
        else {
            return HSERIAL_RX_FIFO_SIZE;
        }
    }


    @Override
    protected int getRxInterruptNumber() {
        return TxInterruptController.HINTRX0 + 2 * serialInterfaceNumber;
    }

    @Override
    protected int getTxInterruptNumber() {
        return TxInterruptController.HINTTX0 + 2 * serialInterfaceNumber;
    }
}