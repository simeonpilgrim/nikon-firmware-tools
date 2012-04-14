package com.nikonhacker.emu.interruptController;

import com.nikonhacker.emu.InterruptRequest;
import com.nikonhacker.emu.memory.Memory;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Based on Fujitsu documentation hm91660-cm71-10146-3e.pdf (and for some better understanding hm90360-cm44-10136-1e.pdf)
 */
public class InterruptController {

    public static final int INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET = 0x10;
    public static final int ICR0_BASE_ADDRESS = 0x400;
    private final SortedSet<InterruptRequest> interruptRequestQueue = new TreeSet<InterruptRequest>();
    private Memory memory;

    public InterruptController(Memory memory) {
        this.memory = memory;
    }

    public Set<InterruptRequest> getInterruptRequestQueue() {
        return interruptRequestQueue;
    }

    /**
     * This is the standard way to request an interrupt.
     * This method determines if it is a NMI and the respective levels and created the actual request
     * TODO : Note that it means that once an interrupt has been requested, its level cannot change,
     * TODO : although a real CPU would recompute priority if the ICRxx change in between
     * TODO : The listener on the ICR should update the InterruptRequest and re-sort the queue
     * @param interruptNumber The number of the interrupt to request
     * @return
     */
    public boolean request(int interruptNumber) {
        int icr;
        boolean isNMI = false;
        if (interruptNumber == 0xF) {
            icr = 0xF;
            isNMI = true;
        }
        else {
            int irNumber = interruptNumber - INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET;
            int icrAddress = irNumber + ICR0_BASE_ADDRESS;
            // only the 5 LSB are significant, but bit4 is always 1
            // (see hm91660-cm71-10146-3e.pdf, page 257, §10.3.1)
            icr = memory.loadUnsigned8(icrAddress) & 0x1F | 0x10;
        }
        if (icr == 0x1F) {
            /* ICR = 0b11111 is Interrupt disabled. See page 259 */
            return false;
        }
        else {
            return request(new InterruptRequest(interruptNumber, isNMI, icr));
        }
    }

    /**
     * This is a way to cause completely custom (or even bogus) InterruptRequests
     * @param newInterruptRequest
     * @return
     */
    public boolean request(InterruptRequest newInterruptRequest) {
        synchronized (interruptRequestQueue) {
            for (InterruptRequest currentInterruptRequest : interruptRequestQueue) {
                if (currentInterruptRequest.getInterruptNumber() == newInterruptRequest.getInterruptNumber()) {
                    // Same number. Keep highest priority one
                    if ((newInterruptRequest.isNMI() && !currentInterruptRequest.isNMI())
                            || (newInterruptRequest.getICR() < currentInterruptRequest.getICR())) {
                        // New is better. Remove old one then go on adding
                        interruptRequestQueue.remove(currentInterruptRequest);
                        break;
                    }
                    else {
                        // Old is better. No change to the list. Exit
                        return false;
                    }
                }
            }
            interruptRequestQueue.add(newInterruptRequest);
            return true;
        }
    }

    /**
     * This is the way to remove a request by number
     * @param requestNumber
     */
    public void removeRequest(int requestNumber) {
        synchronized (interruptRequestQueue) {
            InterruptRequest requestToRemove = null;
            for (InterruptRequest interruptRequest : interruptRequestQueue) {
                if (interruptRequest.getInterruptNumber() == requestNumber) {
                    requestToRemove = interruptRequest;
                    break;
                }
            }
            if (requestToRemove != null) {
                interruptRequestQueue.remove(requestToRemove);
            }
        }
    }

    /**
     * This is the way to remove a specific request object
     * @param interruptRequest
     */
    public void removeRequest(InterruptRequest interruptRequest) {
        synchronized (interruptRequestQueue) {
            interruptRequestQueue.remove(interruptRequest);
        }
    }

    /**
     * Determine if there is at least one request pending
     * @return
     */
    public boolean hasPendingRequests() {
        return !interruptRequestQueue.isEmpty();
    }

    /**
     * Return the highest priority request in queue
     * @return
     */
    public InterruptRequest getNextRequest() {
        synchronized (interruptRequestQueue) {
            if (interruptRequestQueue.isEmpty()) {
                return null;
            }
            else {
                return interruptRequestQueue.first();
            }
        }
    }

}
