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
    private int intClr;

    private RegisterSectionMapping[] imcgMapping = new RegisterSectionMapping[] {
            /* 00 */ null,
            /* 01 */ null,
            /* 02 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGA, 0),
            /* 03 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGA, 1),
            /* 04 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGA, 2),
            /* 05 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGA, 3),
            /* 06 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGB, 0),
            /* 07 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGB, 1),
            /* 08 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGB, 2),
            /* 09 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGB, 3),
            /* 10 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGC, 0),
            /* 11 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGC, 1),
            /* 12 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGC, 2),
            /* 13 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGC, 3),
            /* 14 */ null,
            /* 15 */ null,
            /* 16 */ null,
            /* 17 */ null,
            /* 18 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGD, 0),
            /* 19 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGF, 0),
            /* 20 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGF, 1),
            /* 21 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGF, 2),
            /* 22 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGF, 3),
            /* 23 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCG10, 0),
            /* 24 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCG10, 1),
            /* 25 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCG10, 2),
            /* 26 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCG10, 3),
            /* 27 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCG11, 0),
            /* 28 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCG11, 1),
            /* 29 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCG11, 2),
            /* 30 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCG11, 3),
            /* 31 */ null,
            /* 32 */ null,
            /* 33 */ null,
            /* 34 */ null,
            /* 35 */ null,
            /* 36 */ null,
            /* 37 */ null,
            /* 38 */ null,
            /* 39 */ null,
            /* 40 */ null,
            /* 41 */ null,
            /* 42 */ null,
            /* 43 */ null,
            /* 44 */ null,
            /* 45 */ null,
            /* 46 */ null,
            /* 47 */ null,
            /* 48 */ null,
            /* 49 */ null,
            /* 50 */ null,
            /* 51 */ null,
            /* 52 */ null,
            /* 53 */ null,
            /* 54 */ null,
            /* 55 */ null,
            /* 56 */ null,
            /* 57 */ null,
            /* 58 */ null,
            /* 59 */ null,
            /* 60 */ null,
            /* 61 */ null,
            /* 62 */ null,
            /* 63 */ null,
            /* 64 */ null,
            /* 65 */ null,
            /* 66 */ null,
            /* 67 */ null,
            /* 68 */ null,
            /* 69 */ null,
            /* 70 */ null,
            /* 71 */ null,
            /* 72 */ null,
            /* 73 */ null,
            /* 74 */ null,
            /* 75 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGD, 2),
            /* 76 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGD, 3),
            /* 77 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGE, 0),
            /* 78 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGE, 1),
            /* 79 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGE, 2),
            /* 80 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGE, 3),
            /* 81 */ null,
            /* 82 */ null,
            /* 83 */ null,
            /* 84 */ null,
            /* 85 */ null,
            /* 86 */ null,
            /* 87 */ null,
            /* 88 */ null,
            /* 89 */ null,
            /* 90 */ null,
            /* 91 */ null,
            /* 92 */ null,
            /* 93 */ null,
            /* 94 */ new RegisterSectionMapping(TxIoListener.REGISTER_IMCGD, 1),
            /* 95 */ null,
            /* 96 */ null,
            /* 97 */ null,
            /* 98 */ null,
            /* 99 */ null,
            /* 100 */ null,
            /* 101 */ null,
            /* 102 */ null,
            /* 103 */ null,
            /* 104 */ null,
            /* 105 */ null,
            /* 106 */ null,
            /* 107 */ null,
            /* 108 */ null,
            /* 109 */ null,
            /* 110 */ null,
            /* 111 */ null,
            /* 112 */ null,
            /* 113 */ null,
            /* 114 */ null,
            /* 115 */ null,
            /* 116 */ null,
            /* 117 */ null,
            /* 118 */ null,
            /* 119 */ null,
            /* 120 */ null,
            /* 121 */ null,
            /* 122 */ null,
            /* 123 */ null,
            /* 124 */ null,
            /* 125 */ null,
            /* 126 */ null,
            /* 127 */ null
    };

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

    /**
     * Request a custom interrupt request
     * @param interruptRequest
     * @return
     */
    public boolean request(InterruptRequest interruptRequest) {
        if (cpuState.getPowerMode() != TxCPUState.PowerMode.RUN) {
            // See if this interrupt can clear standby state
            int interruptNumber = interruptRequest.getInterruptNumber();
            if (isImcgIntxen(getIMCGSectionForInterrupt(interruptNumber))) {
                cpuState.setPowerMode(TxCPUState.PowerMode.RUN);
            }
            else {
                // CPU is asleep and cannot be woken up by this interrupt. Request cancelled
                return false;
            }
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

    public void setIvr31_9(int ivr31_9) {
        this.ivr = (ivr & 0x000001FF) | (ivr31_9 & 0xFFFFFE00) ;
    }

    public void setIvr8_0(int ivr8_0) {
        this.ivr = (ivr & 0xFFFFFE00) | (ivr8_0 & 0x000001FF) ;
    }

    public int getIntClr() {
        return intClr;
    }

    public void setIntClr(int intclr) {
        this.intClr = intclr & 0x1FF;
        removeRequest(this.intClr);
    }

    public void pushIlevCmask(int cmask) {
        setIlev(0b10000000_00000000_00000000_00000000 | cmask);
    }


    // IMC
    private int getRequestLevel(int interruptNumber) {
        int imc = getIMCRegisterForInterrupt(interruptNumber);
        return getImcIl(getImcSection(imc, interruptNumber % 4));
    }

    /**
     * Returns the IMCx register value for the given interrupt #
     * @param interruptNumber See spec section 6.5.1.5
     * @return
     */
    private int getIMCRegisterForInterrupt(int interruptNumber) {
        return memory.load32(TxIoListener.REGISTER_IMC00 + interruptNumber >> 4);
    }

    /**
     * Returns the "sectionNumber"th part (8 bits) of the given IMCx register value
     * @param imc
     * @param sectionNumber
     * @return
     */
    private int getImcSection(int imc, int sectionNumber) {
        return (imc >> (8 * sectionNumber)) & 0xFF;
    }

    /**
     * Returns the IL part of a given IMCx section
     * @param imcSection one of the four 8-bit sections in an IMCx register
     * @return
     */
    private int getImcIl(int imcSection) {
        return imcSection & 0b111;
    }

    /**
     * Indicates if the DM part of a given IMCx section is set
     * @param imcSection one of the four 8-bit sections in an IMCx register
     * @return
     */
    private boolean isImcDmSet(int imcSection) {
        return (imcSection & 0b10000) != 0;
    }

    /**
     * Returns the EIM part of a given IMCx section
     * @param imcSection one of the four 8-bit sections in an IMCx register
     * @return
     */
    private int getImcEim(int imcSection) {
        return (imcSection & 0b1100000) >> 5;
    }


    // CG
    /**
     * Returns the part (8 bits) of IMCGx register value corresponding to the given interrupt #
     * @param interruptNumber See spec section 6.5.1.5
     * @return
     */
    private int getIMCGSectionForInterrupt(int interruptNumber) {
        RegisterSectionMapping registerSectionMapping = imcgMapping[interruptNumber];
        if (registerSectionMapping == null) {
            throw new RuntimeException("No IMGC found for interrupt #" + interruptNumber);
        }
        int registerValue = memory.load32(registerSectionMapping.registerAddress);
        return (registerValue >> registerSectionMapping.sectionNumber) & 0xFF;
    }

    /**
     * Indicates if the INTxEN part of a given IMCGx section is set
     * @param imcgSection one of the four 8-bit sections in an IMCGx register
     * @return
     */
    private boolean isImcgIntxen(int imcgSection) {
        return (imcgSection & 0b1) != 0;
    }

    /**
     * Returns the EMST part of a given IMCGx section
     * @param imcgSection one of the four 8-bit sections in an IMCGx register
     * @return
     */
    private int getImcgEmst(int imcgSection) {
        return (imcgSection & 0b1100) >> 2;
    }

    /**
     * Returns the EMCG part of a given IMCGx section
     * @param imcgSection one of the four 8-bit sections in an IMCGx register
     * @return
     */
    private int getImcgEmxg(int imcgSection) {
        return (imcgSection & 0b1110000) >> 4;
    }


    public class RegisterSectionMapping {
        int registerAddress;
        int sectionNumber;

        public RegisterSectionMapping(int registerAddress, int sectionNumber) {
            this.registerAddress = registerAddress;
            this.sectionNumber = sectionNumber;
        }
    }
}
