package com.nikonhacker.emu.peripherials.serialInterface.tx;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;

public class TxHSerialInterface extends TxSerialInterface {
    private static final int HSERIAL_RX_FIFO_SIZE = 32;

    public TxHSerialInterface(int serialInterfaceNumber, InterruptController interruptController) {
        super(serialInterfaceNumber, interruptController);
    }

    @Override
    public String getName() {
        return "HSerial #" + serialInterfaceNumber;
    }

    /**
     * Overridden because in TxHSerial, fill levels are independent of duplex mode, so no need to recompute them
     * @param mod1
     */
    @Override
    public void setMod1(int mod1) {
//        System.out.println(getName() + ".setMod1(0x" + Format.asHex(mod1, 8) + ")");
        boolean previousTxEnabled = isMod1TxeSet();
        this.mod1 = mod1;
        boolean currentTxEnabled = isMod1TxeSet();

        // Check if TXE was just enabled.
        if (currentTxEnabled && !previousTxEnabled) {
            // Signal if there are values waiting
            for (int i = 0; i < getNbTxValuesWaiting(); i++) {
                super.valueReady();
            }
        }        this.mod1 = mod1;
    }

    /**
     * Compute Rx FIFO Fill Level to generate interrupts
     * Overridden because it is much larger on hi-speed than normal serial interfaces
     */
    @Override
    protected void computeRxFillLevel() {
        rxInterruptFillLevel = rfc & 0b11111; // 5 bits, independent of Half/Full duplex
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
        txInterruptFillLevel = tfc & 0b111111; // 6 bits, independent of Half/Full duplex
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
