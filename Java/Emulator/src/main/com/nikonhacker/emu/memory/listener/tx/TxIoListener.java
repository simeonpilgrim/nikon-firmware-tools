package com.nikonhacker.emu.memory.listener.tx;

import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.memory.listener.IoActivityListener;
import com.nikonhacker.emu.peripherials.interruptController.TxInterruptController;
import com.nikonhacker.emu.peripherials.reloadTimer.ReloadTimer;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;

public class TxIoListener implements IoActivityListener {
    public static final int IO_PAGE = 0xFF00;

    public static final int REGISTER_IMC00   =    0xFF00_1000; // Interrupt mode control register 00
    public static final int REGISTER_IMC01   =    0xFF00_1004; // Interrupt mode control register 01
    public static final int REGISTER_IMC02   =    0xFF00_1008; // Interrupt mode control register 02
    public static final int REGISTER_IMC03   =    0xFF00_100C; // Interrupt mode control register 03
    public static final int REGISTER_IMC04   =    0xFF00_1010; // Interrupt mode control register 04
    public static final int REGISTER_IMC05   =    0xFF00_1014; // Interrupt mode control register 05
    public static final int REGISTER_IMC06   =    0xFF00_1018; // Interrupt mode control register 06
    public static final int REGISTER_IMC07   =    0xFF00_101C; // Interrupt mode control register 07
    public static final int REGISTER_IMC08   =    0xFF00_1020; // Interrupt mode control register 08
    public static final int REGISTER_IMC09   =    0xFF00_1024; // Interrupt mode control register 09
    public static final int REGISTER_IMC0A   =    0xFF00_1028; // Interrupt mode control register 0A
    public static final int REGISTER_IMC0B   =    0xFF00_102C; // Interrupt mode control register 0B
    public static final int REGISTER_IMC0C   =    0xFF00_1030; // Interrupt mode control register 0C
    public static final int REGISTER_IMC0D   =    0xFF00_1034; // Interrupt mode control register 0D
    public static final int REGISTER_IMC0E   =    0xFF00_1038; // Interrupt mode control register 0E
    public static final int REGISTER_IMC0F   =    0xFF00_103C; // Interrupt mode control register 0F
    public static final int REGISTER_IMC10   =    0xFF00_1040; // Interrupt mode control register 10
    public static final int REGISTER_IMC11   =    0xFF00_1044; // Interrupt mode control register 11
    public static final int REGISTER_IMC12   =    0xFF00_1048; // Interrupt mode control register 12
    public static final int REGISTER_IMC13   =    0xFF00_104C; // Interrupt mode control register 13
    public static final int REGISTER_IMC14   =    0xFF00_1050; // Interrupt mode control register 14
    public static final int REGISTER_IMC15   =    0xFF00_1054; // Interrupt mode control register 15
    public static final int REGISTER_IMC16   =    0xFF00_1058; // Interrupt mode control register 16
    public static final int REGISTER_IMC17   =    0xFF00_105C; // Interrupt mode control register 17
    public static final int REGISTER_IMC18   =    0xFF00_1060; // Interrupt mode control register 18
    public static final int REGISTER_IMC19   =    0xFF00_1064; // Interrupt mode control register 19
    public static final int REGISTER_INTCLR  =    0xFF00_10C0; // Interrupt request clear register  
    public static final int REGISTER_DREQFLG =    0xFF00_10C4; // DMA request clear flag register   
    public static final int REGISTER_IVR     =    0xFF00_1080; // Interrupt vector register
    public static final int REGISTER_ILEV    =    0xFF00_110C; // Interrupt level register

    public static final int REGISTER_ICRCG   =    0xFF00_1714; // CG interrupt request clear register
    public static final int REGISTER_NMIFLG  =    0xFF00_1718; // NMI flag register
    public static final int REGISTER_RSTFLG  =    0xFF00_171C; // Reset flag register
    public static final int REGISTER_IMCGA   =    0xFF00_1720; // CG interrupt mode control register A
    public static final int REGISTER_IMCGB   =    0xFF00_1724; // CG interrupt mode control register A
    public static final int REGISTER_IMCGC   =    0xFF00_1728; // CG interrupt mode control register A
    public static final int REGISTER_IMCGD   =    0xFF00_172C; // CG interrupt mode control register A
    public static final int REGISTER_IMCGE   =    0xFF00_1730; // CG interrupt mode control register A


    private final TxCPUState cpuState;
    private final TxInterruptController interruptController;

    private final ReloadTimer[] reloadTimers;
    private SerialInterface[] serialInterfaces;

    public TxIoListener(TxCPUState cpuState, TxInterruptController interruptController, ReloadTimer[] reloadTimers, SerialInterface[] serialInterfaces) {
        this.cpuState = cpuState;
        this.interruptController = interruptController;
        this.reloadTimers = reloadTimers;
        this.serialInterfaces = serialInterfaces;
    }

    @Override
    public int getIoPage() {
        return IO_PAGE;
    }

    /**
     * Called when reading 8-bit value from register address range
     * @param ioPage
     * @param addr
     * @param value
     * @return value to be returned, or null to return previously written value like normal memory
     */
    public Byte onIoLoad8(byte[] ioPage, int addr, byte value) {
//        if (addr == xxx) {
//            return yyy;
//        }
        return null;
    }

    /**
     * Called when reading 16-bit value from register address range
     *
     * @param ioPage
     * @param addr
     * @param value
     * @return value to be returned, or null to return previously written value like normal memory
     */
    public Integer onIoLoad16(byte[] ioPage, int addr, int value) {
//        if (addr == xxx) {
//            return yyy;
//        }
        return null;
    }

    /**
     * Called when reading 32-bit value from register address range
     *
     * @param ioPage
     * @param addr
     * @param value
     * @return value to be returned, or null to return previously written value like normal memory
     */
    public Integer onIoLoad32(byte[] ioPage, int addr, int value) {
        if (addr == REGISTER_ILEV) {
            return interruptController.getIlev();
        } else if (addr == REGISTER_IVR) {
            // TODO Until the IVR is read, no hardware interrupt from INTC is accepted (see HW spec section 6.4.1.4)
            return interruptController.getIvr();
        }

        return null;
    }

    public void onIoStore8(byte[] ioPage, int addr, byte value) {
//        if (addr == xxx) {
//            return yyy;
//        }
        //System.out.println("Setting register 0x" + Format.asHex(offset, 4) + " to 0x" + Format.asHex(value, 2));
    }

    public void onIoStore16(byte[] ioPage, int addr, int value) {
//        if (addr == xxx) {
//            return yyy;
//        }
        //System.out.println("Setting register 0x" + Format.asHex(offset, 4) + " to 0x" + Format.asHex(value, 2));
    }

    public void onIoStore32(byte[] ioPage, int addr, int value) {
        if (addr == REGISTER_ILEV) {
            interruptController.setIlev(value);
        } else if (addr == REGISTER_IVR) {
            interruptController.setIvr(value);
        }
        //System.out.println("Setting register 0x" + Format.asHex(offset, 4) + " to 0x" + Format.asHex(value, 2));
    }
}
