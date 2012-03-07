package com.nikonhacker.emu;

import com.nikonhacker.Format;

public class InterruptRequest {
    private int interruptNumber;
    private boolean isNMI;
    private int icr;

    /**
     * Create an Interrupt Request
     * @param interruptNumber interrupt number to call
     * @param NMI true if Non Maskable Interrupt
     * @param icr Interrupt Control Register (priority : lower is better)
     */
    public InterruptRequest(int interruptNumber, boolean NMI, int icr) {
        this.interruptNumber = interruptNumber;
        isNMI = NMI;
        this.icr = icr;
    }

    public int getInterruptNumber() {
        return interruptNumber;
    }

    public void setInterruptNumber(int interruptNumber) {
        this.interruptNumber = interruptNumber;
    }

    public boolean isNMI() {
        return isNMI;
    }

    public void setNMI(boolean NMI) {
        isNMI = NMI;
    }

    public int getICR() {
        return icr;
    }

    public void setICR(int icr) {
        this.icr = icr;
    }

    @Override
    public String toString() {
        return (isNMI?"Non-maskable ":"") + "InterruptRequest #" + interruptNumber + " with ICR=0b" + Format.asBinary(icr,5);
    }
}
