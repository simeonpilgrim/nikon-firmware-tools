package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.Format;
import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.memory.Memory;

public class TxInterruptController extends AbstractInterruptController implements InterruptController {

    public static final int ADDRESS_INTERRUPT_BEV0_IV0 = 0x80000180;
    public static final int ADDRESS_INTERRUPT_BEV0_IV1 = 0x80000200;
    public static final int ADDRESS_INTERRUPT_BEV1_IV0 = 0xBFC00380;
    public static final int ADDRESS_INTERRUPT_BEV1_IV1 = 0xBFC00400;

    public static final int IVR_REGISTER_ADDRESS     =    0xFF00_1080; // Interrupt vector register         
    public static final int ILEV_REGISTER_ADDRESS    =    0xFF00_110C; // Interrupt level register          
    public static final int IMC00_REGISTER_ADDRESS   =    0xFF00_1000; // Interrupt mode control register 00
    public static final int IMC01_REGISTER_ADDRESS   =    0xFF00_1004; // Interrupt mode control register 01
    public static final int IMC02_REGISTER_ADDRESS   =    0xFF00_1008; // Interrupt mode control register 02
    public static final int IMC03_REGISTER_ADDRESS   =    0xFF00_100C; // Interrupt mode control register 03
    public static final int IMC04_REGISTER_ADDRESS   =    0xFF00_1010; // Interrupt mode control register 04
    public static final int IMC05_REGISTER_ADDRESS   =    0xFF00_1014; // Interrupt mode control register 05
    public static final int IMC06_REGISTER_ADDRESS   =    0xFF00_1018; // Interrupt mode control register 06
    public static final int IMC07_REGISTER_ADDRESS   =    0xFF00_101C; // Interrupt mode control register 07
    public static final int IMC08_REGISTER_ADDRESS   =    0xFF00_1020; // Interrupt mode control register 08
    public static final int IMC09_REGISTER_ADDRESS   =    0xFF00_1024; // Interrupt mode control register 09
    public static final int IMC0A_REGISTER_ADDRESS   =    0xFF00_1028; // Interrupt mode control register 0A
    public static final int IMC0B_REGISTER_ADDRESS   =    0xFF00_102C; // Interrupt mode control register 0B
    public static final int IMC0C_REGISTER_ADDRESS   =    0xFF00_1030; // Interrupt mode control register 0C
    public static final int IMC0D_REGISTER_ADDRESS   =    0xFF00_1034; // Interrupt mode control register 0D
    public static final int IMC0E_REGISTER_ADDRESS   =    0xFF00_1038; // Interrupt mode control register 0E
    public static final int IMC0F_REGISTER_ADDRESS   =    0xFF00_103C; // Interrupt mode control register 0F
    public static final int IMC10_REGISTER_ADDRESS   =    0xFF00_1040; // Interrupt mode control register 10
    public static final int IMC11_REGISTER_ADDRESS   =    0xFF00_1044; // Interrupt mode control register 11
    public static final int IMC12_REGISTER_ADDRESS   =    0xFF00_1048; // Interrupt mode control register 12
    public static final int IMC13_REGISTER_ADDRESS   =    0xFF00_104C; // Interrupt mode control register 13
    public static final int IMC14_REGISTER_ADDRESS   =    0xFF00_1050; // Interrupt mode control register 14
    public static final int IMC15_REGISTER_ADDRESS   =    0xFF00_1054; // Interrupt mode control register 15
    public static final int IMC16_REGISTER_ADDRESS   =    0xFF00_1058; // Interrupt mode control register 16
    public static final int IMC17_REGISTER_ADDRESS   =    0xFF00_105C; // Interrupt mode control register 17
    public static final int IMC18_REGISTER_ADDRESS   =    0xFF00_1060; // Interrupt mode control register 18
    public static final int IMC19_REGISTER_ADDRESS   =    0xFF00_1064; // Interrupt mode control register 19
    public static final int INTCLR_REGISTER_ADDRESS  =    0xFF00_10C0; // Interrupt request clear register  
    public static final int DREQFLG_REGISTER_ADDRESS =    0xFF00_10C4; // DMA request clear flag register   

    // Register fields
    // Ilev
    public final static int Ilev_Mlev_pos        = 31;
    public final static int Ilev_Cmask_mask      = 0b00000000000000000000000000000111;

    private Memory memory;

    public TxInterruptController(Memory memory) {
        this.memory = memory;
    }

    @Override
    public boolean request(int interruptNumber) {
        return false;  //TODO
    }

    @Override
    public void removeRequest(int requestNumber) {
        //TODO
    }

    public boolean request(InterruptRequest interruptRequest) {
        return false;  //TODO
    }


    // ----------------------- Field accessors

    public int getIlev() {
        return memory.load32(ILEV_REGISTER_ADDRESS);
    }

    public void setIlev(int newIlev) {
        int ilev = memory.load32(ILEV_REGISTER_ADDRESS);
        if (Format.isBitSet(newIlev, Ilev_Mlev_pos)) {
            // MLEV = 1 : shift up
            ilev = ilev << 4 | newIlev & Ilev_Cmask_mask;
        }
        else {
            // MLEV = 0 : shift down
            ilev = ilev >>> 4;
        }
        memory.store32(ILEV_REGISTER_ADDRESS, ilev);
    }

    public void pushIlevCmask(int cmask) {
        setIlev(0b10000000000000000000000000000000 | cmask);
    }

}
