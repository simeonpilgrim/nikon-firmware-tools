package com.nikonhacker.emu.peripherials.interruptController.fr;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.StatementContext;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.interrupt.fr.FrInterruptRequest;
import com.nikonhacker.emu.memory.listener.fr.ExpeedIoListener;
import com.nikonhacker.emu.peripherials.interruptController.AbstractInterruptController;
import com.nikonhacker.emu.peripherials.interruptController.InterruptControllerException;

import java.util.Collections;

/**
 * Behaviour is Based on Fujitsu documentation hm91660-cm71-10146-3e.pdf
 * (and for some better understanding hm90360-cm44-10136-1e.pdf)
 */
public class FrInterruptController extends AbstractInterruptController {

    public static final int INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET = 0x10;

    public static final int RELOAD_TIMER0_INTERRUPT_REQUEST_NR = 0x18;
    public static final int DELAY_INTERRUPT_REQUEST_NR = 0x3F;

    public FrInterruptController(Platform platform) {
        super(platform);
    }

    /**
     * This is the standard way to request an interrupt.
     * This method determines if it is a NMI and the respective levels and created the actual request
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
            icr = platform.getMemory().loadUnsigned8(icrAddress) & 0x1F | 0x10;
        }
        else {
            throw new InterruptControllerException("Cannot determine ICR value for interrupt 0x" + Format.asHex(interruptNumber, 2));
        }
        //noinspection SimplifiableIfStatement
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

    @Override
    public String getStatus() {
        return "Current interrupt level: " + ((FrCPUState)platform.getCpuState()).getILM();
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

    public void processInterrupt(int interruptNumber, int pcToStore, StatementContext context) {
        FrCPUState frCpuState = (FrCPUState) context.cpuState;
        frCpuState.setReg(FrCPUState.SSP, frCpuState.getReg(FrCPUState.SSP) - 4);
        context.memory.store32(frCpuState.getReg(FrCPUState.SSP), frCpuState.getPS());
        frCpuState.setReg(FrCPUState.SSP, frCpuState.getReg(FrCPUState.SSP) - 4);
        context.memory.store32(frCpuState.getReg(FrCPUState.SSP), pcToStore);
        frCpuState.setS(0);

        // Branch to handler
        frCpuState.pc = context.memory.load32(frCpuState.getReg(FrCPUState.TBR) + 0x3FC - interruptNumber * 4);
    }


}