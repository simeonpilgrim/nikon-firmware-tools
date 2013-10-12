package com.nikonhacker.emu.interrupt;

public abstract class InterruptRequest implements Comparable {

    /** only meaningful for TX software/hardware interrupts, or for FR interrupts */
    protected int interruptNumber;

    public int getInterruptNumber() {
        return interruptNumber;
    }

    public void setInterruptNumber(int interruptNumber) {
        this.interruptNumber = interruptNumber;
    }

    /**
     * Returns an absolute priority for this request, for comparison
     * Lower number = higher priority
     */
    public abstract int getPriority();

    /**
     * Comparing request priority. Most important comes first
     * @param o
     * @return
     */
    public int compareTo(Object o) {
        InterruptRequest i = (InterruptRequest) o;
        return getPriority() - i.getPriority();
    }
}
