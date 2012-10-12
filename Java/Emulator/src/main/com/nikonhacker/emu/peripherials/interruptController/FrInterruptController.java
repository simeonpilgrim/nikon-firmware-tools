package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.Format;
import com.nikonhacker.emu.FrInterruptRequest;
import com.nikonhacker.emu.InterruptRequest;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.memory.listener.ExpeedIoListener;

import java.util.Collections;

/**
 * Behaviour is Based on Fujitsu documentation hm91660-cm71-10146-3e.pdf
 * (and for some better understanding hm90360-cm44-10136-1e.pdf)
 */
public class FrInterruptController extends AbstractInterruptController implements InterruptController {

    public static final int INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET = 0x10;

    public static final int RELOAD_TIMER0_INTERRUPT_REQUEST_NR = 0x18;
    public static final int DELAY_INTERRUPT_REQUEST_NR = 0x3F;

    private Memory memory;

    public FrInterruptController(Memory memory) {
        this.memory = memory;
    }

    /**
     * This is the standard way to request an interrupt.
     * This method determines if it is a NMI and the respective levels and created the actual request
     * TODO : Note that it means that once an interrupt has been requested, its level cannot change,
     * TODO : although a real CPU would recompute priority if the ICRxx change in between
     * TODO : The listener on the ICR should update the InterruptRequest and re-sort the queue
     * @param interruptNumber The number of the interrupt to request (0x0F - 0x3F)
     * @return true if request was queued. false otherwise.
     */
    public boolean request(int interruptNumber) {
        int icr;
        boolean isNMI = false;
        if (interruptNumber == 0xF) {
            icr = 0xF;
            isNMI = true;
        }
        else if (interruptNumber >= 0x10 && interruptNumber <= 0x3F) {
            int irNumber = interruptNumber - INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET;
            int icrAddress = irNumber + ExpeedIoListener.REGISTER_ICR00;
            // only the 5 LSB are significant, but bit4 is always 1
            // (see hm91660-cm71-10146-3e.pdf, page 257, sect. 10.3.1)
            icr = memory.loadUnsigned8(icrAddress) & 0x1F | 0x10;
        }
        else {
            throw new InterruptControllerException("Cannot determine ICR value for interrupt 0x" + Format.asHex(interruptNumber, 2));
        }
        if (icr == 0x1F) {
            /* ICR = 0b11111 is Interrupt disabled. See page 259 */
            return false;
        }
        else {
            return request(new FrInterruptRequest(interruptNumber, isNMI, icr));
        }
    }

    /**
     * This is a way to cause completely custom (or even bogus) InterruptRequests
     * @param interruptRequest
     * @return
     */
    public boolean request(InterruptRequest interruptRequest) {
        FrInterruptRequest newInterruptRequest = (FrInterruptRequest) interruptRequest;
        synchronized (interruptRequestQueue) {
            for (InterruptRequest currentInterruptRequest : interruptRequestQueue) {
                FrInterruptRequest currentFrInterruptRequest = (FrInterruptRequest) currentInterruptRequest;
                if (currentFrInterruptRequest.getInterruptNumber() == newInterruptRequest.getInterruptNumber()) {
                    // Same number. Keep highest priority one
                    if ((newInterruptRequest.isNMI() && !currentFrInterruptRequest.isNMI())
                            || (newInterruptRequest.getICR() < currentFrInterruptRequest.getICR())) {
                        // New is better. Remove old one then go on adding
                        interruptRequestQueue.remove(currentFrInterruptRequest);
                        break;
                    }
                    else {
                        // Old is better. No change to the list. Exit
                        return false;
                    }
                }
            }
            interruptRequestQueue.add(newInterruptRequest);
            Collections.sort(interruptRequestQueue);
            return true;
        }
    }

    /**
     * This is the way to remove a request by number
     * @param requestNumber
     */
    public void removeRequest(int requestNumber) {
        synchronized (interruptRequestQueue) {
            FrInterruptRequest requestToRemove = null;
            for (InterruptRequest interruptRequest : interruptRequestQueue) {
                FrInterruptRequest frInterruptRequest = (FrInterruptRequest) interruptRequest;
                if (frInterruptRequest.getInterruptNumber() == requestNumber) {
                    requestToRemove = frInterruptRequest;
                    break;
                }
            }
            if (requestToRemove != null) {
                interruptRequestQueue.remove(requestToRemove);
            }
        }
    }

    public void updateRequestICR(int interruptNumber, byte icr) {
        synchronized (interruptRequestQueue) {
            for (InterruptRequest interruptRequest : interruptRequestQueue) {
                FrInterruptRequest frInterruptRequest = (FrInterruptRequest) interruptRequest;
                if (frInterruptRequest.getInterruptNumber() == interruptNumber) {
                    if (icr == 0x1F) {
                        System.err.println("Disabling interrupt 0x" + Format.asHex(interruptNumber, 2));
                    }
                    frInterruptRequest.setICR(icr & 0x1F | 0x10);
                    Collections.sort(interruptRequestQueue);
                    break;
                }
            }
        }
    }
}
