package com.nikonhacker.emu.memory.listener.tx;

import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.clock.TxClockGenerator;
import com.nikonhacker.emu.memory.listener.IoActivityListener;
import com.nikonhacker.emu.peripherials.interruptController.TxInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.TxIoPort;
import com.nikonhacker.emu.peripherials.programmableTimer.TxInputCaptureTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.TxTimer;

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

    // I/O Port
    public static final int NUM_PORT = 20;
    private static final int PORT_OFFSET_SHIFT = 6; // 1 << 6 = 0x40 bytes per port
    private static final int REGISTER_PORT0    =    0xFF00_4000; // Port register (value)
    private static final int REGISTER_PORT0CR  =    0xFF00_4004; // Port control register
    private static final int REGISTER_PORT0FC1 =    0xFF00_4008; // Port function register 1
    private static final int REGISTER_PORT0FC2 =    0xFF00_400C; // Port function register 2
    private static final int REGISTER_PORT0FC3 =    0xFF00_4010; // Port function register 3
    private static final int REGISTER_PORT0ODE =    0xFF00_4028; // Port open-drain control register
    private static final int REGISTER_PORT0PUP =    0xFF00_402C; // Port pull-up control register
    private static final int REGISTER_PORT0PIE =    0xFF00_4038; // Port input enable control register

    // 16-bit Timer
    public static final int NUM_TIMER = 18;
    private static final int TIMER_OFFSET_SHIFT = 6; // 1 << 6 = 0x40 bytes per timer
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

    // 32-bit Capture input timer
    private static final int REGISTER_TCEN     =    0xFF00_4A00; // Timer Enable register
    private static final int REGISTER_TBTRUN   =    0xFF00_4A04; // Timer Run register
    private static final int REGISTER_TBTCR    =    0xFF00_4A08; // Timer Control Register
    private static final int REGISTER_TBTCAP   =    0xFF00_4A0C; // Software Capture Register
    private static final int REGISTER_TBTRDCAP =    0xFF00_4A10; // Software Capture Register (?)
    public static final int NUM_COMPARE_CHANNEL = 8;
    private static final short INPUT_COMPARE_OFFSET_SHIFT = 4; // 1 << 4 = 0x10 bytes per compare channel
    private static final int REGISTER_CMPCTL0  =    0xFF00_4A20; // Compare Control Register
    private static final int REGISTER_TCCMP0   =    0xFF00_4A24; // Compare value register
    public static final int NUM_CAPTURE_CHANNEL = 4;
    private static final short INPUT_CAPTURE_OFFSET_SHIFT = 4; // 1 << 4 = 0x10 bytes per capture channel
    private static final int REGISTER_CAPCR0   =    0xFF00_4AA0; // Capture control register
    private static final int REGISTER_TCCAP0   =    0xFF00_4AA4; // Capture register

    // Serial ports
    public static final int NUM_SERIAL_IF = 0;

    private final Platform platform;

    public TxIoListener(Platform platform) {
        this.platform = platform;
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

        if (addr >= REGISTER_PORT0 && addr < REGISTER_PORT0 + (NUM_PORT << PORT_OFFSET_SHIFT)) {
            // Port configuration registers
            int portNr = (addr - REGISTER_PORT0) >> PORT_OFFSET_SHIFT;
            TxIoPort txIoPort = (TxIoPort) platform.getIoPorts()[portNr];
            switch (addr - (portNr << PORT_OFFSET_SHIFT)) {
                case REGISTER_PORT0 + 3:
                    return txIoPort.getValue();
                case REGISTER_PORT0CR + 3:
                    return txIoPort.getControlRegister();
                case REGISTER_PORT0FC1 + 3:
                    return txIoPort.getFunctionRegister1();
                case REGISTER_PORT0FC2 + 3:
                    return txIoPort.getFunctionRegister2();
                case REGISTER_PORT0FC3 + 3:
                    return txIoPort.getFunctionRegister3();
                case REGISTER_PORT0ODE + 3:
                    return txIoPort.getOpenDrainControlRegister();
                case REGISTER_PORT0PUP + 3:
                    return txIoPort.getPullUpControlRegister();
                case REGISTER_PORT0PIE + 3:
                    return txIoPort.getInputEnableControlRegister();
            }
        }
        else if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + (NUM_TIMER << TIMER_OFFSET_SHIFT)) {
            // Timer configuration registers
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_SHIFT;
            TxTimer txTimer = (TxTimer)platform.getProgrammableTimers()[timerNr];
            switch (addr - (timerNr << TIMER_OFFSET_SHIFT)) {
                case REGISTER_TB0EN:
                    return (byte) txTimer.getEn();
                case REGISTER_TB0RUN:
                    return (byte) txTimer.getRun();
                case REGISTER_TB0CR:
                    return (byte) txTimer.getCr();
                case REGISTER_TB0MOD:
                    return (byte) txTimer.getMod();
                case REGISTER_TB0FFCR:
                    throw new RuntimeException("The TBnFFCR register cannot be accessed by 8-bit for now");
                case REGISTER_TB0ST:
                    return (byte) txTimer.getSt();
                case REGISTER_TB0IM:
                    return (byte) txTimer.getIm();
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
                    return (byte) txTimer.getCp0();
                case REGISTER_TB0CP1:
                    return (byte) txTimer.getCp1();
            }
        }
        else if (addr >= REGISTER_TCEN && addr < REGISTER_CAPCR0 + (NUM_CAPTURE_CHANNEL << INPUT_CAPTURE_OFFSET_SHIFT )) {
            // Capture Input configuration registers
            TxInputCaptureTimer txInputCaptureTimer = (TxInputCaptureTimer)platform.getProgrammableTimers()[NUM_TIMER];
            if (addr < REGISTER_CMPCTL0) {
                switch (addr) {
                    case REGISTER_TCEN + 3:
                        return (byte)txInputCaptureTimer.getEn();
                    case REGISTER_TBTRUN + 3:
                        return (byte)txInputCaptureTimer.getRun();
                    case REGISTER_TBTCR + 3:
                        return (byte)txInputCaptureTimer.getCr();
                    case REGISTER_TBTCAP + 3:
                        return (byte)txInputCaptureTimer.getTbtCap();
                    case REGISTER_TBTRDCAP + 3:
                        return (byte)txInputCaptureTimer.getCurrentValue(); // TODO is that really the value ??
                    }
            }
            else if (addr < REGISTER_CAPCR0) {
                int compareChannel = (addr - REGISTER_CMPCTL0) >> INPUT_COMPARE_OFFSET_SHIFT;
                switch (addr - (compareChannel << INPUT_COMPARE_OFFSET_SHIFT)) {
                    case REGISTER_CMPCTL0 + 3:
                        return (byte) txInputCaptureTimer.getCmpCtl(compareChannel);
                    case REGISTER_TCCMP0 + 3:
                        return (byte) txInputCaptureTimer.getTcCmp(compareChannel);
                }
            }
            else {
                int captureChannel = (addr - REGISTER_CAPCR0) >> INPUT_CAPTURE_OFFSET_SHIFT;
                switch (addr - (captureChannel << INPUT_CAPTURE_OFFSET_SHIFT)) {
                    case REGISTER_CAPCR0 + 3:
                        return (byte) txInputCaptureTimer.getCapCr(captureChannel);
                    case REGISTER_TCCAP0 + 3:
                        return (byte) txInputCaptureTimer.getTcCap(captureChannel);
                }
            }
        }
        else switch (addr) {
            // Clock generator
            case REGISTER_SYSCR:
                throw new RuntimeException("The highest byte of SYSCR register can not be accessed by 8-bit for now");
            case REGISTER_SYSCR + 1:
                return ((TxClockGenerator)platform.getClockGenerator()).getSysCr2();
            case REGISTER_SYSCR + 2:
                return ((TxClockGenerator)platform.getClockGenerator()).getSysCr1();
            case REGISTER_SYSCR + 3:
                return ((TxClockGenerator)platform.getClockGenerator()).getSysCr0();
            case REGISTER_NMIFLG + 3:
                return (byte)((TxClockGenerator)platform.getClockGenerator()).readAndClearNmiFlag();
            case REGISTER_RSTFLG + 3:
                return (byte)((TxClockGenerator)platform.getClockGenerator()).getRstFlg();
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

        if (addr >= REGISTER_PORT0 && addr < REGISTER_PORT0 + (NUM_PORT << PORT_OFFSET_SHIFT)) {
            // Port configuration registers
            throw new RuntimeException("The I/O port registers cannot be accessed by 16-bit for now");
        }
        else if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + (NUM_TIMER << TIMER_OFFSET_SHIFT)) {
            // Timer configuration registers
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_SHIFT;
            TxTimer txTimer = (TxTimer)platform.getProgrammableTimers()[timerNr];
            switch (addr - (timerNr << TIMER_OFFSET_SHIFT)) {
                case REGISTER_TB0EN:
                    return txTimer.getEn();
                case REGISTER_TB0RUN:
                    return txTimer.getRun();
                case REGISTER_TB0CR:
                    return txTimer.getCr();
                case REGISTER_TB0MOD:
                    return txTimer.getMod();
                case REGISTER_TB0FFCR:
                    return txTimer.getFfcr();
                case REGISTER_TB0ST:
                    return txTimer.getSt();
                case REGISTER_TB0IM:
                    return txTimer.getIm();
                case REGISTER_TB0UC:
                    return txTimer.getUc();
                case REGISTER_TB0RG0:
                    return txTimer.getRg0();
                case REGISTER_TB0RG1:
                    return txTimer.getRg1();
                case REGISTER_TB0CP0:
                    return txTimer.getCp0();
                case REGISTER_TB0CP1:
                    return txTimer.getCp1();
            }
        }
        else if (addr >= REGISTER_TCEN && addr < REGISTER_CAPCR0 + (NUM_CAPTURE_CHANNEL << INPUT_CAPTURE_OFFSET_SHIFT )) {
            // Capture Input configuration registers
            TxInputCaptureTimer txInputCaptureTimer = (TxInputCaptureTimer)platform.getProgrammableTimers()[NUM_TIMER];
            if (addr < REGISTER_CMPCTL0) {
                switch (addr) {
                    case REGISTER_TCEN + 2:
                        return txInputCaptureTimer.getEn();
                    case REGISTER_TBTRUN + 2:
                        return txInputCaptureTimer.getRun();
                    case REGISTER_TBTCR + 2:
                        return txInputCaptureTimer.getCr();
                    case REGISTER_TBTCAP + 2:
                        return txInputCaptureTimer.getTbtCap();
                    case REGISTER_TBTRDCAP + 2:
                        return txInputCaptureTimer.getCurrentValue(); // TODO is that really the value ??
                }
            }
            else if (addr < REGISTER_CAPCR0) {
                int compareChannel = (addr - REGISTER_CMPCTL0) >> INPUT_COMPARE_OFFSET_SHIFT;
                switch (addr - (compareChannel << INPUT_COMPARE_OFFSET_SHIFT)) {
                    case REGISTER_CMPCTL0 + 2:
                        return  txInputCaptureTimer.getCmpCtl(compareChannel);
                    case REGISTER_TCCMP0 + 2:
                        return  txInputCaptureTimer.getTcCmp(compareChannel);
                }

            }
            else {
                int captureChannel = (addr - REGISTER_CAPCR0) >> INPUT_CAPTURE_OFFSET_SHIFT;
                switch (addr - (captureChannel << INPUT_CAPTURE_OFFSET_SHIFT)) {
                    case REGISTER_CAPCR0 + 2:
                        return  txInputCaptureTimer.getCapCr(captureChannel);
                    case REGISTER_TCCAP0 + 2:
                        return  txInputCaptureTimer.getTcCap(captureChannel);
                }
            }
        }
        else switch (addr){
            // Clock generator
            case REGISTER_SYSCR:
                throw new RuntimeException("The SYSCR register can not be accessed by 16-bit for now");
            case REGISTER_SYSCR + 2:
                throw new RuntimeException("The SYSCR register can not be accessed by 16-bit for now");
            case REGISTER_NMIFLG + 2:
                return ((TxClockGenerator)platform.getClockGenerator()).readAndClearNmiFlag() & 0xFFFF;
            case REGISTER_RSTFLG + 2:
                return ((TxClockGenerator)platform.getClockGenerator()).getRstFlg() & 0xFFFF;
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
        if (addr >= REGISTER_PORT0 && addr < REGISTER_PORT0 + (NUM_PORT << PORT_OFFSET_SHIFT)) {
            // Port configuration registers
            int portNr = (addr - REGISTER_PORT0) >> PORT_OFFSET_SHIFT;
            TxIoPort txIoPort = (TxIoPort) platform.getIoPorts()[portNr];
            switch (addr - (portNr << PORT_OFFSET_SHIFT)) {
                case REGISTER_PORT0:
                    return (int) txIoPort.getValue();
                case REGISTER_PORT0CR:
                    return (int) txIoPort.getControlRegister();
                case REGISTER_PORT0FC1:
                    return (int) txIoPort.getFunctionRegister1();
                case REGISTER_PORT0FC2:
                    return (int) txIoPort.getFunctionRegister2();
                case REGISTER_PORT0FC3:
                    return (int) txIoPort.getFunctionRegister3();
                case REGISTER_PORT0ODE:
                    return (int) txIoPort.getOpenDrainControlRegister();
                case REGISTER_PORT0PUP:
                    return (int) txIoPort.getPullUpControlRegister();
                case REGISTER_PORT0PIE:
                    return (int) txIoPort.getInputEnableControlRegister();
            }
        }
        else if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + (NUM_TIMER << TIMER_OFFSET_SHIFT)) {
            // Timer configuration registers
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_SHIFT;
            TxTimer txTimer = (TxTimer)platform.getProgrammableTimers()[timerNr];
            switch (addr - (timerNr << TIMER_OFFSET_SHIFT)) {
                case REGISTER_TB0EN:
                    return txTimer.getEn();
                case REGISTER_TB0RUN:
                    return txTimer.getRun();
                case REGISTER_TB0CR:
                    return txTimer.getCr();
                case REGISTER_TB0MOD:
                    return txTimer.getMod();
                case REGISTER_TB0FFCR:
                    return txTimer.getFfcr();
                case REGISTER_TB0ST:
                    return txTimer.getSt();
                case REGISTER_TB0IM:
                    return txTimer.getIm();
                case REGISTER_TB0UC:
                    return txTimer.getUc();
                case REGISTER_TB0RG0:
                    return txTimer.getRg0();
                case REGISTER_TB0RG1:
                    return txTimer.getRg1();
                case REGISTER_TB0CP0:
                    return txTimer.getCp0();
                case REGISTER_TB0CP1:
                    return txTimer.getCp1();
            }
        }
        else if (addr >= REGISTER_TCEN && addr < REGISTER_CAPCR0 + (NUM_CAPTURE_CHANNEL << INPUT_CAPTURE_OFFSET_SHIFT )) {
            // Capture Input configuration registers
            TxInputCaptureTimer txInputCaptureTimer = (TxInputCaptureTimer)platform.getProgrammableTimers()[NUM_TIMER];
            if (addr < REGISTER_CMPCTL0) {
                switch (addr) {
                    case REGISTER_TCEN:
                        return txInputCaptureTimer.getEn();
                    case REGISTER_TBTRUN:
                        return txInputCaptureTimer.getRun();
                    case REGISTER_TBTCR:
                        return txInputCaptureTimer.getCr();
                    case REGISTER_TBTCAP:
                        return txInputCaptureTimer.getTbtCap();
                    case REGISTER_TBTRDCAP:
                        return txInputCaptureTimer.getCurrentValue(); // TODO is that really the value ??
                }
            }
            else if (addr < REGISTER_CAPCR0) {
                int compareChannel = (addr - REGISTER_CMPCTL0) >> INPUT_COMPARE_OFFSET_SHIFT;
                switch (addr - (compareChannel << INPUT_COMPARE_OFFSET_SHIFT)) {
                    case REGISTER_CMPCTL0:
                        return  txInputCaptureTimer.getCmpCtl(compareChannel);
                    case REGISTER_TCCMP0:
                        return  txInputCaptureTimer.getTcCmp(compareChannel);
                }

            }
            else {
                int captureChannel = (addr - REGISTER_CAPCR0) >> INPUT_CAPTURE_OFFSET_SHIFT;
                switch (addr - (captureChannel << INPUT_CAPTURE_OFFSET_SHIFT)) {
                    case REGISTER_CAPCR0:
                        return  txInputCaptureTimer.getCapCr(captureChannel);
                    case REGISTER_TCCAP0:
                        return  txInputCaptureTimer.getTcCap(captureChannel);
                }
            }
        }
        switch (addr) {
            // Clock generator
            case REGISTER_SYSCR:
                TxClockGenerator clockGenerator = (TxClockGenerator) platform.getClockGenerator();
                return (clockGenerator.getSysCr2() << 16) |(clockGenerator.getSysCr1() << 8) | clockGenerator.getSysCr0();
            // Interrupt Controller
            case REGISTER_ILEV:
                return ((TxInterruptController)platform.getInterruptController()).getIlev();
            case REGISTER_IVR:
                // TODO Until the IVR is read, no hardware interrupt from INTC is accepted (see HW spec section 6.4.1.4)
                return ((TxInterruptController)platform.getInterruptController()).getIvr();
            case REGISTER_NMIFLG:
                return ((TxClockGenerator)platform.getClockGenerator()).readAndClearNmiFlag();
            case REGISTER_RSTFLG:
                return ((TxClockGenerator)platform.getClockGenerator()).getRstFlg();
            }
        return null;
    }

    public void onIoStore8(byte[] ioPage, int addr, byte value) {
        if (addr >= REGISTER_PORT0 && addr < REGISTER_PORT0 + (NUM_PORT << PORT_OFFSET_SHIFT)) {
            // Port configuration registers
            int portNr = (addr - REGISTER_PORT0) >> PORT_OFFSET_SHIFT;
            TxIoPort txIoPort = (TxIoPort) platform.getIoPorts()[portNr];
            switch (addr - (portNr << PORT_OFFSET_SHIFT)) {
                case REGISTER_PORT0 + 3:
                    txIoPort.setInternalValue(value); break;
                case REGISTER_PORT0CR + 3:
                    txIoPort.setControlRegister(value); break;
                case REGISTER_PORT0FC1 + 3:
                    txIoPort.setFunctionRegister1(value); break;
                case REGISTER_PORT0FC2 + 3:
                    txIoPort.setFunctionRegister2(value); break;
                case REGISTER_PORT0FC3 + 3:
                    txIoPort.setFunctionRegister3(value); break;
                case REGISTER_PORT0ODE + 3:
                    txIoPort.setOpenDrainControlRegister(value); break;
                case REGISTER_PORT0PUP + 3:
                    txIoPort.setPullUpControlRegister(value); break;
                case REGISTER_PORT0PIE + 3:
                    txIoPort.setInputEnableControlRegister(value); break;
            }
        }
        else if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + (NUM_TIMER << TIMER_OFFSET_SHIFT)) {
            // Timer configuration registers
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_SHIFT;
            TxTimer txTimer = (TxTimer)platform.getProgrammableTimers()[timerNr];
            switch (addr - (timerNr << TIMER_OFFSET_SHIFT)) {
                case REGISTER_TB0EN:
                    txTimer.setEn(value); break;
                case REGISTER_TB0RUN:
                    txTimer.setRun(value); break;
                case REGISTER_TB0CR:
                    txTimer.setCr(value); break;
                case REGISTER_TB0MOD:
                    txTimer.setMod(value); break;
                case REGISTER_TB0FFCR:
                    txTimer.setFfcr(value); break;
                case REGISTER_TB0ST:
                    txTimer.setSt(value); break;
                case REGISTER_TB0IM:
                    txTimer.setIm(value); break;
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
                    txTimer.setCp0(value); break;
                case REGISTER_TB0CP1:
                    txTimer.setCp1(value); break;
            }
        }
        else if (addr >= REGISTER_TCEN && addr < REGISTER_CAPCR0 + (NUM_CAPTURE_CHANNEL << INPUT_CAPTURE_OFFSET_SHIFT )) {
            // Capture Input configuration registers
            TxInputCaptureTimer txInputCaptureTimer = (TxInputCaptureTimer)platform.getProgrammableTimers()[NUM_TIMER];
            if (addr < REGISTER_CMPCTL0) {
                switch (addr) {
                    case REGISTER_TCEN + 3:
                        txInputCaptureTimer.setEn(value); break;
                    case REGISTER_TBTRUN + 3:
                        txInputCaptureTimer.setRun(value); break;
                    case REGISTER_TBTCR + 3:
                        txInputCaptureTimer.setCr(value); break;
                    case REGISTER_TBTCAP + 3:
                        txInputCaptureTimer.setTbtCap(value); break;
                    case REGISTER_TBTRDCAP + 3:
                        txInputCaptureTimer.setCurrentValue(value); break; // TODO is that really the value ??
                }
            }
            else if (addr < REGISTER_CAPCR0) {
                int compareChannel = (addr - REGISTER_CMPCTL0) >> INPUT_COMPARE_OFFSET_SHIFT;
                switch (addr - (compareChannel << INPUT_COMPARE_OFFSET_SHIFT)) {
                    case REGISTER_CMPCTL0 + 3:
                         txInputCaptureTimer.setCmpCtl(compareChannel, value); break;
                    case REGISTER_TCCMP0 + 3:
                         txInputCaptureTimer.setTcCmp(compareChannel, value); break;
                }
            }
            else {
                int captureChannel = (addr - REGISTER_CAPCR0) >> INPUT_CAPTURE_OFFSET_SHIFT;
                switch (addr - (captureChannel << INPUT_CAPTURE_OFFSET_SHIFT)) {
                    case REGISTER_CAPCR0 + 3:
                         txInputCaptureTimer.setCapCr(captureChannel, value); break;
                    case REGISTER_TCCAP0 + 3:
                        throw new RuntimeException("Cannot write to TBTRDCAP register of channel " + captureChannel);
                }
            }
        }
        else switch (addr) {
            // Clock generator
            case REGISTER_SYSCR:
                throw new RuntimeException("The highest byte of SYSCR register can not be accessed by 8-bit for now");
            case REGISTER_SYSCR + 1:
                ((TxClockGenerator)platform.getClockGenerator()).setSysCr2(value); break;
            case REGISTER_SYSCR + 2:
                ((TxClockGenerator)platform.getClockGenerator()).setSysCr1(value); break;
            case REGISTER_SYSCR + 3:
                ((TxClockGenerator)platform.getClockGenerator()).setSysCr0(value); break;
            case REGISTER_NMIFLG + 3:
                ((TxClockGenerator)platform.getClockGenerator()).setNmiFlg(value); break;
            case REGISTER_RSTFLG + 3:
                ((TxClockGenerator)platform.getClockGenerator()).setRstFlg(value); break;
            // Interrupt Controller
            case REGISTER_INTCLR:
                throw new RuntimeException("The INTCLR register can not be accessed by 8-bit");
        }

        //System.out.println("Setting register 0x" + Format.asHex(offset, 4) + " to 0x" + Format.asHex(value, 2));
    }

    public void onIoStore16(byte[] ioPage, int addr, int value) {
        if (addr >= REGISTER_PORT0 && addr < REGISTER_PORT0 + (NUM_PORT << PORT_OFFSET_SHIFT)) {
            // Port configuration registers
            throw new RuntimeException("The I/O port registers cannot be accessed by 16-bit for now");
        }
        else if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + (NUM_TIMER << TIMER_OFFSET_SHIFT)) {
            // Timer configuration registers
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_SHIFT;
            TxTimer txTimer = (TxTimer)platform.getProgrammableTimers()[timerNr];
            switch (addr - (timerNr << TIMER_OFFSET_SHIFT)) {
                case REGISTER_TB0EN:
                    txTimer.setEn(value); break;
                case REGISTER_TB0RUN:
                    txTimer.setRun(value); break;
                case REGISTER_TB0CR:
                    txTimer.setCr(value); break;
                case REGISTER_TB0MOD:
                    txTimer.setMod(value); break;
                case REGISTER_TB0FFCR:
                    txTimer.setFfcr(value); break;
                case REGISTER_TB0ST:
                    txTimer.setSt(value); break;
                case REGISTER_TB0IM:
                    txTimer.setIm(value); break;
                case REGISTER_TB0UC:
                    txTimer.setUc(value); break;
                case REGISTER_TB0RG0:
                    txTimer.setRg0(value); break;
                case REGISTER_TB0RG1:
                    txTimer.setRg1(value); break;
                case REGISTER_TB0CP0:
                    txTimer.setCp0(value); break;
                case REGISTER_TB0CP1:
                    txTimer.setCp1(value); break;
            }
        }
        else if (addr >= REGISTER_TCEN && addr < REGISTER_CAPCR0 + (NUM_CAPTURE_CHANNEL << INPUT_CAPTURE_OFFSET_SHIFT )) {
            // Capture Input configuration registers
            TxInputCaptureTimer txInputCaptureTimer = (TxInputCaptureTimer)platform.getProgrammableTimers()[NUM_TIMER];
            if (addr < REGISTER_CMPCTL0) {
                switch (addr) {
                    case REGISTER_TCEN + 2:
                        txInputCaptureTimer.setEn(value); break;
                    case REGISTER_TBTRUN + 2:
                        txInputCaptureTimer.setRun(value); break;
                    case REGISTER_TBTCR + 2:
                        txInputCaptureTimer.setCr(value); break;
                    case REGISTER_TBTCAP + 2:
                        txInputCaptureTimer.setTbtCap(value); break;
                    case REGISTER_TBTRDCAP + 2:
                        txInputCaptureTimer.setCurrentValue(value); break; // TODO is that really the value ??
                }
            }
            else if (addr < REGISTER_CAPCR0) {
                int compareChannel = (addr - REGISTER_CMPCTL0) >> INPUT_COMPARE_OFFSET_SHIFT;
                switch (addr - (compareChannel << INPUT_COMPARE_OFFSET_SHIFT)) {
                    case REGISTER_CMPCTL0 + 2:
                        txInputCaptureTimer.setCmpCtl(compareChannel, value); break;
                    case REGISTER_TCCMP0 + 2:
                        txInputCaptureTimer.setTcCmp(compareChannel, value); break;
                }

            }
            else {
                int captureChannel = (addr - REGISTER_CAPCR0) >> INPUT_CAPTURE_OFFSET_SHIFT;
                switch (addr - (captureChannel << INPUT_CAPTURE_OFFSET_SHIFT)) {
                    case REGISTER_CAPCR0 + 2:
                        txInputCaptureTimer.setCapCr(captureChannel, value); break;
                    case REGISTER_TCCAP0 + 2:
                        throw new RuntimeException("Cannot write to TBTRDCAP register of channel " + captureChannel);
                }
            }
        }
        else switch (addr){
            // Clock generator
            case REGISTER_SYSCR:
                throw new RuntimeException("The SYSCR register can not be accessed by 16-bit for now");
            case REGISTER_SYSCR + 2:
                throw new RuntimeException("The SYSCR register can not be accessed by 16-bit for now");
            case REGISTER_NMIFLG + 2:
                ((TxClockGenerator)platform.getClockGenerator()).setNmiFlg(value); break;
            case REGISTER_RSTFLG + 2:
                ((TxClockGenerator)platform.getClockGenerator()).setRstFlg(value); break;
            // Interrupt Controller
            case REGISTER_INTCLR:
                ((TxInterruptController)platform.getInterruptController()).setIntClr(value); break;
        }
        //System.out.println("Setting register 0x" + Format.asHex(offset, 4) + " to 0x" + Format.asHex(value, 2));
    }

    public void onIoStore32(byte[] ioPage, int addr, int value) {
        if (addr >= REGISTER_PORT0 && addr < REGISTER_PORT0 + (NUM_PORT << PORT_OFFSET_SHIFT)) {
            // Port configuration registers
            int portNr = (addr - REGISTER_PORT0) >> PORT_OFFSET_SHIFT;
            TxIoPort txIoPort = (TxIoPort) platform.getIoPorts()[portNr];
            switch (addr - (portNr << PORT_OFFSET_SHIFT)) {
                case REGISTER_PORT0:
                    txIoPort.setInternalValue((byte) value); break;
                case REGISTER_PORT0CR:
                    txIoPort.setControlRegister((byte) value); break;
                case REGISTER_PORT0FC1:
                    txIoPort.setFunctionRegister1((byte) value); break;
                case REGISTER_PORT0FC2:
                    txIoPort.setFunctionRegister2((byte) value); break;
                case REGISTER_PORT0FC3:
                    txIoPort.setFunctionRegister3((byte) value); break;
                case REGISTER_PORT0ODE:
                    txIoPort.setOpenDrainControlRegister((byte) value); break;
                case REGISTER_PORT0PUP:
                    txIoPort.setPullUpControlRegister((byte) value); break;
                case REGISTER_PORT0PIE:
                    txIoPort.setInputEnableControlRegister((byte) value); break;
            }
        }
        else if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + (NUM_TIMER << TIMER_OFFSET_SHIFT)) {
            // Timer configuration registers
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_SHIFT;
            TxTimer txTimer = (TxTimer)platform.getProgrammableTimers()[timerNr];
            switch (addr - (timerNr << TIMER_OFFSET_SHIFT)) {
                case REGISTER_TB0EN:
                    txTimer.setEn(value); break;
                case REGISTER_TB0RUN:
                    txTimer.setRun(value); break;
                case REGISTER_TB0CR:
                    txTimer.setCr(value); break;
                case REGISTER_TB0MOD:
                    txTimer.setMod(value); break;
                case REGISTER_TB0FFCR:
                    txTimer.setFfcr(value); break;
                case REGISTER_TB0ST:
                    txTimer.setSt(value); break;
                case REGISTER_TB0IM:
                    txTimer.setIm(value); break;
                case REGISTER_TB0UC:
                    txTimer.setUc(value); break;
                case REGISTER_TB0RG0:
                    txTimer.setRg0(value); break;
                case REGISTER_TB0RG1:
                    txTimer.setRg1(value); break;
                case REGISTER_TB0CP0:
                    txTimer.setCp0(value); break;
                case REGISTER_TB0CP1:
                    txTimer.setCp1(value); break;
            }
        }
        else if (addr >= REGISTER_TCEN && addr < REGISTER_CAPCR0 + (NUM_CAPTURE_CHANNEL << INPUT_CAPTURE_OFFSET_SHIFT )) {
            // Capture Input configuration registers
            TxInputCaptureTimer txInputCaptureTimer = (TxInputCaptureTimer)platform.getProgrammableTimers()[NUM_TIMER];
            if (addr < REGISTER_CMPCTL0) {
                switch (addr) {
                    case REGISTER_TCEN:
                        txInputCaptureTimer.setEn(value); break;
                    case REGISTER_TBTRUN:
                        txInputCaptureTimer.setRun(value); break;
                    case REGISTER_TBTCR:
                        txInputCaptureTimer.setCr(value); break;
                    case REGISTER_TBTCAP:
                        txInputCaptureTimer.setTbtCap(value); break;
                    case REGISTER_TBTRDCAP:
                        txInputCaptureTimer.setCurrentValue(value); break; // TODO is that really the value ??
                }
            }
            else if (addr < REGISTER_CAPCR0) {
                int compareChannel = (addr - REGISTER_CMPCTL0) >> INPUT_COMPARE_OFFSET_SHIFT;
                switch (addr - (compareChannel << INPUT_COMPARE_OFFSET_SHIFT)) {
                    case REGISTER_CMPCTL0:
                        txInputCaptureTimer.setCmpCtl(compareChannel, value); break;
                    case REGISTER_TCCMP0:
                        txInputCaptureTimer.setTcCmp(compareChannel, value); break;
                }

            }
            else {
                int captureChannel = (addr - REGISTER_CAPCR0) >> INPUT_CAPTURE_OFFSET_SHIFT;
                switch (addr - (captureChannel << INPUT_CAPTURE_OFFSET_SHIFT)) {
                    case REGISTER_CAPCR0:
                        txInputCaptureTimer.setCapCr(captureChannel, value); break;
                    case REGISTER_TCCAP0:
                        throw new RuntimeException("Cannot write to TBTRDCAP register of channel " + captureChannel);
                }
            }
        }
        else switch(addr) {
            // Clock generator
            case REGISTER_SYSCR:
                ((TxClockGenerator)platform.getClockGenerator()).setSysCr(value); break;
            case REGISTER_NMIFLG:
                ((TxClockGenerator)platform.getClockGenerator()).setNmiFlg(value); break;
            case REGISTER_RSTFLG:
                ((TxClockGenerator)platform.getClockGenerator()).setRstFlg(value); break;
            // Interrupt Controller
            case REGISTER_ILEV:
                ((TxInterruptController)platform.getInterruptController()).setIlev(value); break;
            case REGISTER_IVR:
                ((TxInterruptController)platform.getInterruptController()).setIvr31_9(value); break;
            case REGISTER_INTCLR:
                ((TxInterruptController)platform.getInterruptController()).setIntClr(value); break;
            default:
                // TODO if one interrupt has its active state set to "L", this should trigger a hardware interrupt
                // See section 6.5.1.2 , 3rd bullet
        }
        //System.out.println("Setting register 0x" + Format.asHex(offset, 4) + " to 0x" + Format.asHex(value, 2));
    }
}
