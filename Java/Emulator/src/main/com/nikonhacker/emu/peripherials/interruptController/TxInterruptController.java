package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.interrupt.tx.TxInterruptRequest;
import com.nikonhacker.emu.interrupt.tx.Type;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.memory.listener.tx.TxIoListener;

import java.util.Collections;

/**
 * This is based on the Toshiba hardware specification for TMP19A44FDA/FE/F10XBG
 * Available at http://www.semicon.toshiba.co.jp/info/docget.jsp?type=datasheet&lang=en&pid=TMP19A44FEXBG
 */
public class TxInterruptController extends AbstractInterruptController {

    public static final int ADDRESS_INTERRUPT_BEV0_IV0 = 0x8000_0180;
    public static final int ADDRESS_INTERRUPT_BEV0_IV1 = 0x8000_0200;
    public static final int ADDRESS_INTERRUPT_BEV1_IV0 = 0xBFC0_0380;
    public static final int ADDRESS_INTERRUPT_BEV1_IV1 = 0xBFC0_0400;

    // Register fields
    // Ilev
    public final static int Ilev_Mlev_pos        = 31;
    public final static int Ilev_Cmask_mask      = 0b00000000_00000000_00000000_00000111;

    private TxCPUState cpuState;
    private Memory memory;


    private int ilev;
    private int ivr;

    public TxInterruptController(TxCPUState cpuState, Memory memory) {
        this.cpuState = cpuState;
        this.memory = memory;
    }

    /**
     * Request a hardware interrupt with the given number
     * @param interruptNumber See spec section 6.5.1.5
     * @return
     */
    @Override
    public boolean request(int interruptNumber) {
        return request(new TxInterruptRequest(Type.HARDWARE_INTERRUPT, interruptNumber, getRequestLevel(interruptNumber)));
    }


    // IMC
    private int getRequestLevel(int interruptNumber) {
        int imc = getIMCRegisterForInterrupt(interruptNumber);
        return getImcIl(getImcSection(imc, interruptNumber % 4));
    }

    /**
     * Returns the IMCxx register value for the given interrupt #
     * @param interruptNumber See spec section 6.5.1.5
     * @return
     */
    private int getIMCRegisterForInterrupt(int interruptNumber) {
        return memory.load32(TxIoListener.REGISTER_IMC00 + interruptNumber >> 4);
    }

    /**
     * Returns the "sectionNumber"th part (8 bits) of the given IMCxx register value
     * @param imc
     * @param sectionNumber
     * @return
     */
    private int getImcSection(int imc, int sectionNumber) {
        return (imc >> (8 * sectionNumber)) & 0xFF;
    }

    /**
     * Returns the IL part of a given IMCxx section
     * @param imcSection one of the four 8-bit sections in an IMCxx register
     * @return
     */
    private int getImcIl(int imcSection) {
        return imcSection & 0b111;
    }

    /**
     * Indicates if the DM part of a given IMCxx section is set
     * @param imcSection one of the four 8-bit sections in an IMCxx register
     * @return
     */
    private boolean isImcDmSet(byte imcSection) {
        return (imcSection & 0b10000) != 0;
    }

    /**
     * Returns the EIM part of a given IMCxx section
     * @param imcSection one of the four 8-bit sections in an IMCxx register
     * @return
     */
    private int getImcEim(byte imcSection) {
        return (imcSection & 0b1100000) >> 5;
    }


    /**
     * Request a custom interrupt request
     * @param interruptRequest
     * @return
     */
    public boolean request(InterruptRequest interruptRequest) {
        if (cpuState.getPowerMode() != TxCPUState.PowerMode.RUN) {
            // See if this interrupt can clear standby state
            // TODO Check CG register to see if CPU must be woken up
        }
        TxInterruptRequest newInterruptRequest = (TxInterruptRequest) interruptRequest;
        synchronized (interruptRequestQueue) {
            // See if it's not already in queue
            for (InterruptRequest currentInterruptRequest : interruptRequestQueue) {
                TxInterruptRequest currentTxInterruptRequest = (TxInterruptRequest) currentInterruptRequest;
                if (currentTxInterruptRequest.getType() == newInterruptRequest.getType()) {
                    // Same type. Only HW interrupt can have multiple instances
                    if (newInterruptRequest.getType() != Type.HARDWARE_INTERRUPT) {
                        // ignore new interrupt of same type as an already waiting one
                        return false;
                    }
                    else {
                        // 2 HW interrupts can coexist if they have different numbers
                        if (newInterruptRequest.getInterruptNumber() == currentTxInterruptRequest.getInterruptNumber()) {
                            // check priority and keep the highest one (whatever that means :-))
                            if ((newInterruptRequest.getPriority() < currentTxInterruptRequest.getPriority())) {
                                // New is better. Remove old one then go on adding
                                interruptRequestQueue.remove(currentTxInterruptRequest);
                                break;
                            }
                            else {
                                // Old is better. No change to the list. Exit
                                return false;
                            }
                        }
                    }
                }
            }
            interruptRequestQueue.add(newInterruptRequest);
            Collections.sort(interruptRequestQueue);
            return true;
        }
    }


    // ----------------------- Field accessors

    public int getIlev() {
        return ilev;
    }

    public int getIlevCmask() {
        return ilev & Ilev_Cmask_mask;
    }

    public void setIlev(int newIlev) {
        if (Format.isBitSet(newIlev, Ilev_Mlev_pos)) {
            // MLEV = 1 : shift up
            ilev = ilev << 4 | newIlev & Ilev_Cmask_mask;
        }
        else {
            // MLEV = 0 : shift down
            ilev = ilev >>> 4;
        }
    }

    public int getIvr() {
        return ivr;
    }

    public void setIvr(int ivr) {
        this.ivr = ivr;
    }

    public void pushIlevCmask(int cmask) {
        setIlev(0b10000000_00000000_00000000_00000000 | cmask);
    }

}
