package com.nikonhacker.emu.memory.listener.tx;

import com.nikonhacker.emu.clock.TxClockGenerator;
import com.nikonhacker.emu.memory.listener.IoActivityListener;
import com.nikonhacker.emu.peripherials.interruptController.TxInterruptController;
import com.nikonhacker.emu.peripherials.programmableTimer.TxTimer;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;

/**
 * This is based on the Toshiba hardware specification for TMP19A44FDA/FE/F10XBG
 * Available at http://www.semicon.toshiba.co.jp/info/docget.jsp?type=datasheet&lang=en&pid=TMP19A44FEXBG
 */
public class TxIoListener implements IoActivityListener {
    public static final int IO_PAGE = 0xFF00;

    // See section 22 for addresses

    // Interrupt Controller
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


    // Clock Generator.
    // Note: section 6.6.1 lists FF0017xx while section 22 lists FF0019xx
    public static final int REGISTER_SYSCR   =    0xFF00_1900; // System Control Registers
    public static final int REGISTER_OSCCR   =    0xFF00_1904; //
    public static final int REGISTER_SDBYCR  =    0xFF00_1908; //
    public static final int REGISTER_PLLSEL  =    0xFF00_190C; //
    public static final int REGISTER_SCKSEL  =    0xFF00_1910; //
    public static final int REGISTER_ICRCG   =    0xFF00_1914; // CG interrupt request clear register
    public static final int REGISTER_NMIFLG  =    0xFF00_1918; // NMI flag register
    public static final int REGISTER_RSTFLG  =    0xFF00_191C; // Reset flag register
    public static final int REGISTER_IMCGA   =    0xFF00_1920; // CG interrupt mode control register A
    public static final int REGISTER_IMCGB   =    0xFF00_1924; // CG interrupt mode control register B
    public static final int REGISTER_IMCGC   =    0xFF00_1928; // CG interrupt mode control register C
    public static final int REGISTER_IMCGD   =    0xFF00_192C; // CG interrupt mode control register D
    public static final int REGISTER_IMCGE   =    0xFF00_1930; // CG interrupt mode control register E
    public static final int REGISTER_IMCGF   =    0xFF00_1934; // CG interrupt mode control register F
    public static final int REGISTER_IMCG10  =    0xFF00_1938; // CG interrupt mode control register 10
    public static final int REGISTER_IMCG11  =    0xFF00_193C; // CG interrupt mode control register 11

    // Timer
    private static final int NB_TIMER          = 18;
    private static final int TIMER_OFFSET_BITS = 6;
    private static final int TIMER_OFFSET      = 1 << TIMER_OFFSET_BITS;
    private static final int REGISTER_TB0EN   =    0xFF00_4500; // Timer enable register
    private static final int REGISTER_TB0RUN  =    0xFF00_4504; // Timer RUN register
    private static final int REGISTER_TB0CR   =    0xFF00_4508; // Timer control register
    private static final int REGISTER_TB0MOD  =    0xFF00_450C; // Timer mode register
    private static final int REGISTER_TB0FFCR =    0xFF00_4510; // Timer flip-flop control register
    private static final int REGISTER_TB0ST   =    0xFF00_4514; // Timer status register
    private static final int REGISTER_TB0IM   =    0xFF00_4518; // Timer Interrupt mask register
    private static final int REGISTER_TB0UC   =    0xFF00_451C; // Timer up counter register
    private static final int REGISTER_TB0RG0  =    0xFF00_4520; // Timer register low word
    private static final int REGISTER_TB0RG1  =    0xFF00_4524; // Timer register hi word
    private static final int REGISTER_TB0CP0  =    0xFF00_4528; // Timer Capture register lo word
    private static final int REGISTER_TB0CP1  =    0xFF00_452C; // Timer Capture register hi word

    private final TxClockGenerator clockGenerator;
    private final TxInterruptController interruptController;

    private final TxTimer[] timers;
    private final SerialInterface[] serialInterfaces;

    public TxIoListener(TxClockGenerator clockGenerator, TxInterruptController interruptController, TxTimer[] timers, SerialInterface[] serialInterfaces) {
        this.clockGenerator = clockGenerator;
        this.interruptController = interruptController;
        this.timers = timers;
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

        // Timer configuration registers
        if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + NB_TIMER * TIMER_OFFSET) {
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_BITS;
            switch (addr - (timerNr << TIMER_OFFSET_BITS)) {
                case REGISTER_TB0EN:
                    return (byte) timers[timerNr].getEn();
                case REGISTER_TB0RUN:
                    return (byte) timers[timerNr].getRun();
                case REGISTER_TB0CR:
                    return (byte) timers[timerNr].getCr();
                case REGISTER_TB0MOD:
                    return (byte) timers[timerNr].getMod();
                case REGISTER_TB0FFCR:
                    throw new RuntimeException("The TBnFFCR register cannot be accessed by 8-bit for now");
                case REGISTER_TB0ST:
                    return (byte) timers[timerNr].getSt();
                case REGISTER_TB0IM:
                    return (byte) timers[timerNr].getIm();
                case REGISTER_TB0UC:
                    throw new RuntimeException("The TBnUC register cannot be accessed by 8-bit");
                case REGISTER_TB0RG0:
                    throw new RuntimeException("The TBnRG0 register cannot be accessed by 8-bit for now");
                case REGISTER_TB0RG0 + 1:
                    throw new RuntimeException("The TBnRG0 register cannot be accessed by 8-bit for now");
                case REGISTER_TB0RG1:
                    throw new RuntimeException("The TBnRG1 register cannot be accessed by 8-bit for now");
                case REGISTER_TB0RG1 + 1:
                    throw new RuntimeException("The TBnRG1 register cannot be accessed by 8-bit for now");
                case REGISTER_TB0CP0:
                    return (byte) timers[timerNr].getCp0();
                case REGISTER_TB0CP1:
                    return (byte) timers[timerNr].getCp1();
            }
        }

        switch (addr) {
            case REGISTER_SYSCR:
                throw new RuntimeException("The highest byte of SYSCR register can not be accessed by 8-bit for now");
            case REGISTER_SYSCR + 1:
                return clockGenerator.getSysCr2();
            case REGISTER_SYSCR + 2:
                return clockGenerator.getSysCr1();
            case REGISTER_SYSCR + 3:
                return clockGenerator.getSysCr0();
        }

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

        // Timer configuration registers
        if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + NB_TIMER * TIMER_OFFSET) {
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_BITS;
            switch (addr - (timerNr << TIMER_OFFSET_BITS)) {
                case REGISTER_TB0EN:
                    return timers[timerNr].getEn();
                case REGISTER_TB0RUN:
                    return timers[timerNr].getRun();
                case REGISTER_TB0CR:
                    return timers[timerNr].getCr();
                case REGISTER_TB0MOD:
                    return timers[timerNr].getMod();
                case REGISTER_TB0FFCR:
                    return timers[timerNr].getFfcr();
                case REGISTER_TB0ST:
                    return timers[timerNr].getSt();
                case REGISTER_TB0IM:
                    return timers[timerNr].getIm();
                case REGISTER_TB0UC:
                    return timers[timerNr].getUc();
                case REGISTER_TB0RG0:
                    return timers[timerNr].getRg0();
                case REGISTER_TB0RG1:
                    return timers[timerNr].getRg1();
                case REGISTER_TB0CP0:
                    return timers[timerNr].getCp0();
                case REGISTER_TB0CP1:
                    return timers[timerNr].getCp1();
            }
        }
        else switch (addr){
            case REGISTER_SYSCR:
                throw new RuntimeException("The SYSCR register can not be accessed by 16-bit for now");
            case REGISTER_SYSCR + 2:
                throw new RuntimeException("The SYSCR register can not be accessed by 16-bit for now");
        }
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
        // Timer configuration registers
        if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + NB_TIMER * TIMER_OFFSET) {
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_BITS;
            switch (addr - (timerNr << TIMER_OFFSET_BITS)) {
                case REGISTER_TB0EN:
                    return timers[timerNr].getEn();
                case REGISTER_TB0RUN:
                    return timers[timerNr].getRun();
                case REGISTER_TB0CR:
                    return timers[timerNr].getCr();
                case REGISTER_TB0MOD:
                    return timers[timerNr].getMod();
                case REGISTER_TB0FFCR:
                    return timers[timerNr].getFfcr();
                case REGISTER_TB0ST:
                    return timers[timerNr].getSt();
                case REGISTER_TB0IM:
                    return timers[timerNr].getIm();
                case REGISTER_TB0UC:
                    return timers[timerNr].getUc();
                case REGISTER_TB0RG0:
                    return timers[timerNr].getRg0();
                case REGISTER_TB0RG1:
                    return timers[timerNr].getRg1();
                case REGISTER_TB0CP0:
                    return timers[timerNr].getCp0();
                case REGISTER_TB0CP1:
                    return timers[timerNr].getCp1();
            }
        }
        switch (addr) {
            case REGISTER_SYSCR:
                return (clockGenerator.getSysCr2() << 16) |(clockGenerator.getSysCr1() << 8) | clockGenerator.getSysCr0();
            case REGISTER_ILEV:
                return interruptController.getIlev();
            case REGISTER_IVR:
                // TODO Until the IVR is read, no hardware interrupt from INTC is accepted (see HW spec section 6.4.1.4)
                return interruptController.getIvr();
            }
        return null;
    }

    public void onIoStore8(byte[] ioPage, int addr, byte value) {
        // Timer configuration registers
        if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + NB_TIMER * TIMER_OFFSET) {
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_BITS;
            switch (addr - (timerNr << TIMER_OFFSET_BITS)) {
                case REGISTER_TB0EN:
                    timers[timerNr].setEn(value); break;
                case REGISTER_TB0RUN:
                    timers[timerNr].setRun(value); break;
                case REGISTER_TB0CR:
                    timers[timerNr].setCr(value); break;
                case REGISTER_TB0MOD:
                    timers[timerNr].setMod(value); break;
                case REGISTER_TB0FFCR:
                    timers[timerNr].setFfcr(value); break;
                case REGISTER_TB0ST:
                    timers[timerNr].setSt(value); break;
                case REGISTER_TB0IM:
                    timers[timerNr].setIm(value); break;
                case REGISTER_TB0UC:
                    throw new RuntimeException("The TBnUC register cannot be accessed by 8-bit");
                case REGISTER_TB0RG0:
                    /* TODO To write data to the TB0RG0H/L and TB0RG1H/L timer registers, either a 2-byte data transfer
                     * TODO instruction or a 1-byte data transfer instruction written twice in the order of low-order
                     * TODO 8 bits followed by high-order 8 bits can be used.
                     */
                    throw new RuntimeException("The TBnRG0 register cannot be accessed by 8-bit for now");
                case REGISTER_TB0RG0 + 1:
                    throw new RuntimeException("The TBnRG0 register cannot be accessed by 8-bit for now");
                case REGISTER_TB0RG1:
                    throw new RuntimeException("The TBnRG1 register cannot be accessed by 8-bit for now");
                case REGISTER_TB0RG1 + 1:
                    throw new RuntimeException("The TBnRG1 register cannot be accessed by 8-bit for now");
                case REGISTER_TB0CP0:
                    timers[timerNr].setCp0(value); break;
                case REGISTER_TB0CP1:
                    timers[timerNr].setCp1(value); break;
            }
        }
        else switch (addr) {
            case REGISTER_INTCLR:
                throw new RuntimeException("The INTCLR register can not be accessed by 8-bit");
            case REGISTER_SYSCR:
                throw new RuntimeException("The highest byte of SYSCR register can not be accessed by 8-bit for now");
            case REGISTER_SYSCR + 1:
                clockGenerator.setSysCr2(value); break;
            case REGISTER_SYSCR + 2:
                clockGenerator.setSysCr1(value); break;
            case REGISTER_SYSCR + 3:
                clockGenerator.setSysCr0(value); break;
        }

        //System.out.println("Setting register 0x" + Format.asHex(offset, 4) + " to 0x" + Format.asHex(value, 2));
    }

    public void onIoStore16(byte[] ioPage, int addr, int value) {
        // Timer configuration registers
        if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + NB_TIMER * TIMER_OFFSET) {
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_BITS;
            switch (addr - (timerNr << TIMER_OFFSET_BITS)) {
                case REGISTER_TB0EN:
                    timers[timerNr].setEn(value); break;
                case REGISTER_TB0RUN:
                    timers[timerNr].setRun(value); break;
                case REGISTER_TB0CR:
                    timers[timerNr].setCr(value); break;
                case REGISTER_TB0MOD:
                    timers[timerNr].setMod(value); break;
                case REGISTER_TB0FFCR:
                    timers[timerNr].setFfcr(value); break;
                case REGISTER_TB0ST:
                    timers[timerNr].setSt(value); break;
                case REGISTER_TB0IM:
                    timers[timerNr].setIm(value); break;
                case REGISTER_TB0UC:
                    timers[timerNr].setUc(value); break;
                case REGISTER_TB0RG0:
                    timers[timerNr].setRg0(value); break;
                case REGISTER_TB0RG1:
                    timers[timerNr].setRg1(value); break;
                case REGISTER_TB0CP0:
                    timers[timerNr].setCp0(value); break;
                case REGISTER_TB0CP1:
                    timers[timerNr].setCp1(value); break;
            }
        }
        else switch (addr){
            case REGISTER_INTCLR:
                interruptController.setIntClr(value); break;
            case REGISTER_SYSCR:
                throw new RuntimeException("The SYSCR register can not be accessed by 16-bit for now");
            case REGISTER_SYSCR + 2:
                throw new RuntimeException("The SYSCR register can not be accessed by 16-bit for now");
        }
        //System.out.println("Setting register 0x" + Format.asHex(offset, 4) + " to 0x" + Format.asHex(value, 2));
    }

    public void onIoStore32(byte[] ioPage, int addr, int value) {
        // Timer configuration registers
        if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + NB_TIMER * TIMER_OFFSET) {
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_BITS;
            switch (addr - (timerNr << TIMER_OFFSET_BITS)) {
                case REGISTER_TB0EN:
                    timers[timerNr].setEn(value); break;
                case REGISTER_TB0RUN:
                    timers[timerNr].setRun(value); break;
                case REGISTER_TB0CR:
                    timers[timerNr].setCr(value); break;
                case REGISTER_TB0MOD:
                    timers[timerNr].setMod(value); break;
                case REGISTER_TB0FFCR:
                    timers[timerNr].setFfcr(value); break;
                case REGISTER_TB0ST:
                    timers[timerNr].setSt(value); break;
                case REGISTER_TB0IM:
                    timers[timerNr].setIm(value); break;
                case REGISTER_TB0UC:
                    timers[timerNr].setUc(value); break;
                case REGISTER_TB0RG0:
                    timers[timerNr].setRg0(value); break;
                case REGISTER_TB0RG1:
                    timers[timerNr].setRg1(value); break;
                case REGISTER_TB0CP0:
                    timers[timerNr].setCp0(value); break;
                case REGISTER_TB0CP1:
                    timers[timerNr].setCp1(value); break;
            }
        }
        else switch(addr) {
            case REGISTER_ILEV:
                interruptController.setIlev(value); break;
            case REGISTER_IVR:
                interruptController.setIvr31_9(value); break;
            case REGISTER_INTCLR:
                interruptController.setIntClr(value); break;
            case REGISTER_SYSCR:
                clockGenerator.setSysCr(value);
            default:
                // TODO if one interrupt has its active state set to "L", this should trigger a hardware interrupt
                // See section 6.5.1.2 , 3rd bullet
        }
        //System.out.println("Setting register 0x" + Format.asHex(offset, 4) + " to 0x" + Format.asHex(value, 2));
    }
}
