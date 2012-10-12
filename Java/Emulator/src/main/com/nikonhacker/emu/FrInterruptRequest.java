package com.nikonhacker.emu;

import com.nikonhacker.Format;

public class FrInterruptRequest implements InterruptRequest {
    private int interruptNumber;
    private boolean isNMI;
    private int icr;

    /**
     * Create an Interrupt Request
     * @param interruptNumber interrupt number to call
     * @param NMI true if Non Maskable Interrupt
     * @param icr Interrupt Control Register (priority : lower is better)
     */
    public FrInterruptRequest(int interruptNumber, boolean NMI, int icr) {
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
        return (isNMI?"Non-maskable ":"") + "InterruptRequest 0x" + Format.asHex(interruptNumber,2) + " with ICR=0b" + Format.asBinary(icr,5);
    }

    public int compareTo(Object o) {
        FrInterruptRequest i = (FrInterruptRequest) o;
        // returns a negative number if this object has higher priority (NMI or lower ICR) than o and should appear first
        if (i.isNMI != this.isNMI) {
            // If only one is NMI (should be the case), it comes first
            return (isNMI ? -1 : 1);
        }
        else if (icr != i.icr) {
            // Lower ICR gets a higher priority
            return icr - i.icr;
        }
        else {
            return interruptNumber - i.interruptNumber;
        }
    }
}
