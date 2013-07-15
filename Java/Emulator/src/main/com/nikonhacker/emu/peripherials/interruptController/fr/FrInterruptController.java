package com.nikonhacker.emu.peripherials.interruptController.fr;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.StatementContext;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.interrupt.fr.FrInterruptRequest;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.fr.ExpeedIoListener;
import com.nikonhacker.emu.peripherials.interruptController.AbstractInterruptController;
import com.nikonhacker.emu.peripherials.interruptController.InterruptControllerException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Behaviour is Based on Fujitsu documentation hm91660-cm71-10146-3e.pdf
 * (and for some better understanding hm90360-cm44-10136-1e.pdf)
 */
public class FrInterruptController extends AbstractInterruptController {

    public static final int INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET = 0x10;

    public static final int EXT_INTERRUPT_REQUEST_NR = 0x16;

    public static final int RELOAD_TIMER0_INTERRUPT_REQUEST_NR = 0x18;
    public static final int RELOAD_TIMER1_INTERRUPT_REQUEST_NR = 0x19;
    public static final int RELOAD_TIMER2_INTERRUPT_REQUEST_NR = 0x1A;

    public static final int SERIAL_IF_RX_REQUEST_NR = 0x1B;

    public static final int SERIAL_IF_TX_REQUEST_NR = 0x1C;

    public static final int DELAY_INTERRUPT_REQUEST_NR = 0x3F;
    public static final int NUM_EXT_INTERRUPT        = 8;

    public final static Map<Integer, String> interruptDescriptions = new HashMap<>();

    // external interrupt registers
    public int eirr = 0;
    public int elvr = 0;
    public int enir = 0;

    public int externalChannelValue[] = new int[NUM_EXT_INTERRUPT];

    static {
        interruptDescriptions.put(0x10, "EXTERNAL0");

        interruptDescriptions.put(0x18, "TIMER0");
        interruptDescriptions.put(0x19, "TIMER1");
        interruptDescriptions.put(0x1A, "TIMER2");

        interruptDescriptions.put(0x3F, "DELAY");
        interruptDescriptions.put(0x40, "RESERVED_REALOS_SYSCALL");
        interruptDescriptions.put(0x41, "RESERVED_REALOS");
        interruptDescriptions.put(0x42, "SW_INT0");
    }

    public static String getInterruptShortName(int interruptNumber) {
        String s = interruptDescriptions.get(interruptNumber);
        if (s == null) {
            s = "INT #0x" + Format.asHex(interruptNumber, 1);
        }
        return s;
    }

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
            icr = platform.getMemory().loadUnsigned8(icrAddress, DebuggableMemory.AccessSource.INT) & 0x1F | 0x10;
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

    /* typical usage for external interrupt registers:
    0x2ABA     written to ELVR0(0x00000042)               (@0x00101518)
                read from ELVR0(0x00000042) : 0x2ABA      (@0x00101522)
    0xAABA     written to ELVR0(0x00000042)               (@0x00101532)
    0x00       written to EIRR0(0x00000040)               (@0x001017CE)
    0x41       written to ENIR0(0x00000041)               (@0x001017D4)
                read from ENIR0(0x00000041) : 0x41        (@0x001A88AE)
    0x01       written to ENIR0(0x00000041)               (@0x001A88AE)
                read from ELVR0(0x00000042) : 0xAABA      (@0x001A88C0)
    0xAABA     written to ELVR0(0x00000042)               (@0x001A88D0)
    0xBF       written to EIRR0(0x00000040)               (@0x001A88DA)
    */

    /** change state of external interrupt input */
    public boolean setExternalInterruptChannelValue(int channel, int value) {
        if (channel < 0 || channel >= NUM_EXT_INTERRUPT)
            return false;
        if (value != externalChannelValue[channel]) {
            int mask = (1 << channel);

            // check if condition match
            switch ( (elvr >>(channel<<1)) & 0b0000_0011 ) {
                case 0b00: // "L"
                case 0b11: // falling edge
                    if (value!=0) {
                        mask = 0;
                    }
                    break;
                case 0b01: // "H"
                case 0b10: // rising edge
                    if (value==0) {
                        mask = 0;
                    }
                    break;
            }
            synchronized(this) {
                eirr |= mask;
                // save latched value
                externalChannelValue[channel] = value;
                if ((eirr & enir) != 0) {
                    request(EXT_INTERRUPT_REQUEST_NR);
                }
            }
        }
        return true;
    }

    public int getElvr() {
        return elvr;
    }

    /** set detection condition register */
    public void setElvr(int value) {
        elvr = value & 0xFFFF;
    }

    /** set low byte of detection condition register */
    public void setElvrLo(int value) {
        elvr = (elvr & 0xFF00) | value;
    }

    /** set high byte of detection condition register */
    public void setElvrHi(int value) {
        elvr = (elvr & 0x00FF) | (value<<8);
    }

    public int getEirr() {
        return eirr;
    }

    /** set external interrupt state register */
    public void setEirr(int value) {
        int condition = elvr;
        int mask = 1;

        synchronized(this) {
            // clear all possible inputs in EIRR register
            for (int i=0; i<NUM_EXT_INTERRUPT; i++) {
                if ((value & mask) == 0) {
                    // see if condition still present
                    if (externalChannelValue[i] == 0 && (condition & 0b11)==0) {
                        // still low level and "L" set - keep interrupt
                        value |= mask;
                    } else if (externalChannelValue[i]!=0 && (condition & 0b11)==1) {
                        // still high level and "H" set - keep interrupt
                        value |= mask;
                    }
                }
                mask <<= 1;
                condition >>= 2;
            }
            eirr &= value;
            if ((eirr & enir) == 0)
                removeRequest(EXT_INTERRUPT_REQUEST_NR);
        }
    }

    public int getEnir() {
        return enir;
    }

    /** set external interrupt enable register */
    public void setEnir(int value) {
        if (value != enir) {
            synchronized (this) {
                enir = value;
                // from datasheet:
                // Written value 0: The states of interrupt sources are maintained, but external interrupt requests are not output.
                if ((eirr & enir)==0) {
                    removeRequest(EXT_INTERRUPT_REQUEST_NR);
                }
                else {
                    request(EXT_INTERRUPT_REQUEST_NR);
                }
            }
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
