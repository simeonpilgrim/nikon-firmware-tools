package com.nikonhacker.emu.interrupt.fr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.interrupt.InterruptRequest;

public class FrInterruptRequest extends InterruptRequest {
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

    /**
     * Returns an absolute priority for this request, for comparison
     * Lower number = higher priority
     * NMI is the most important (normally only one), then lower icr, then lower interruptnumber
     * @return
     */
    public int getPriority() {
        return (isNMI?-0x10000:0) + (icr << 8) + interruptNumber;
    }
}
