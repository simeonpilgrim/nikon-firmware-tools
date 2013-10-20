package com.nikonhacker.emu.memory.listener.tx;

import com.nikonhacker.Format;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.IoActivityListener;
import com.nikonhacker.emu.peripherials.adConverter.tx.TxAdConverter;
import com.nikonhacker.emu.peripherials.adConverter.tx.TxAdUnit;
import com.nikonhacker.emu.peripherials.clock.tx.TxClockGenerator;
import com.nikonhacker.emu.peripherials.dmaController.tx.TxDmaChannel;
import com.nikonhacker.emu.peripherials.dmaController.tx.TxDmaController;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.tx.TxIoPort;
import com.nikonhacker.emu.peripherials.keyCircuit.tx.TxKeyCircuit;
import com.nikonhacker.emu.peripherials.programmableTimer.tx.TxInputCaptureTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.tx.TxTimer;
import com.nikonhacker.emu.peripherials.realtimeClock.tx.TxRealtimeClock;
import com.nikonhacker.emu.peripherials.serialInterface.tx.TxSerialInterface;
import org.apache.commons.lang3.StringUtils;

/**
 * This is based on the Toshiba hardware specification for TMP19A44FDA/FE/F10XBG
 * Available at http://www.semicon.toshiba.co.jp/info/docget.jsp?type=datasheet&lang=en&pid=TMP19A44FEXBG
 */
public class TxIoListener extends IoActivityListener {
    private static final boolean DEBUG_SERIAL = false;

    // See section 22 for addresses

    // Interrupt Controller
    public static final int REGISTER_IMC00 = 0xFF00_1000; // Interrupt mode control register 00
    public static final int REGISTER_IMC01 = 0xFF00_1004; // Interrupt mode control register 01
    public static final int REGISTER_IMC02 = 0xFF00_1008; // Interrupt mode control register 02
    public static final int REGISTER_IMC03 = 0xFF00_100C; // Interrupt mode control register 03
    public static final int REGISTER_IMC04 = 0xFF00_1010; // Interrupt mode control register 04
    public static final int REGISTER_IMC05 = 0xFF00_1014; // Interrupt mode control register 05
    public static final int REGISTER_IMC06 = 0xFF00_1018; // Interrupt mode control register 06
    public static final int REGISTER_IMC07 = 0xFF00_101C; // Interrupt mode control register 07
    public static final int REGISTER_IMC08 = 0xFF00_1020; // Interrupt mode control register 08
    public static final int REGISTER_IMC09 = 0xFF00_1024; // Interrupt mode control register 09
    public static final int REGISTER_IMC0A = 0xFF00_1028; // Interrupt mode control register 0A
    public static final int REGISTER_IMC0B = 0xFF00_102C; // Interrupt mode control register 0B
    public static final int REGISTER_IMC0C = 0xFF00_1030; // Interrupt mode control register 0C
    public static final int REGISTER_IMC0D = 0xFF00_1034; // Interrupt mode control register 0D
    public static final int REGISTER_IMC0E = 0xFF00_1038; // Interrupt mode control register 0E
    public static final int REGISTER_IMC0F = 0xFF00_103C; // Interrupt mode control register 0F
    public static final int REGISTER_IMC10 = 0xFF00_1040; // Interrupt mode control register 10
    public static final int REGISTER_IMC11 = 0xFF00_1044; // Interrupt mode control register 11
    public static final int REGISTER_IMC12 = 0xFF00_1048; // Interrupt mode control register 12
    public static final int REGISTER_IMC13 = 0xFF00_104C; // Interrupt mode control register 13
    public static final int REGISTER_IMC14 = 0xFF00_1050; // Interrupt mode control register 14
    public static final int REGISTER_IMC15 = 0xFF00_1054; // Interrupt mode control register 15
    public static final int REGISTER_IMC16 = 0xFF00_1058; // Interrupt mode control register 16
    public static final int REGISTER_IMC17 = 0xFF00_105C; // Interrupt mode control register 17
    public static final int REGISTER_IMC18 = 0xFF00_1060; // Interrupt mode control register 18
    public static final int REGISTER_IMC19 = 0xFF00_1064; // Interrupt mode control register 19

    public static final int REGISTER_IVR     = 0xFF00_1080; // Interrupt vector register
    public static final int REGISTER_INTCLR  = 0xFF00_10C0; // Interrupt request clear register
    public static final int REGISTER_DREQFLG = 0xFF00_10C4; // DMA request clear flag register
    public static final int REGISTER_ILEV    = 0xFF00_110C; // Interrupt level register

    // DMA controller
    public static final  int NUM_DMA_CHANNEL          = 8;
    private static final int DMA_CHANNEL_OFFSET_SHIFT = 5; // 1 << 5 = 0x20 bytes per channel
    private static final int REGISTER_CCR0            = 0xFF00_1200; // Channel control register
    private static final int REGISTER_CSR0            = 0xFF00_1204; // Channel status register
    private static final int REGISTER_SAR0            = 0xFF00_1208; // Source address register
    private static final int REGISTER_DAR0            = 0xFF00_120C; // Destination address register
    private static final int REGISTER_BCR0            = 0xFF00_1210; // Byte count register
    private static final int REGISTER_DTCR0           = 0xFF00_1218; // DMA transfer control register

    private static final int REGISTER_DCR = 0xFF00_1300; // Destination address register
    private static final int REGISTER_RSR = 0xFF00_1304; // Byte count register
    private static final int REGISTER_DHR = 0xFF00_130C; // DMA transfer control register

    // Real Time Clock (RTC)
    public static final int REGISTER_HOURR  = 0xFF00_1500; // Hour column register
    public static final int REGISTER_MINR   = 0xFF00_1502; // Minute column register
    public static final int REGISTER_SECR   = 0xFF00_1503; // Second column register
    public static final int REGISTER_YEARR  = 0xFF00_1504; // Year column register
    public static final int REGISTER_MONTHR = 0xFF00_1505; // Month column register
    public static final int REGISTER_DATER  = 0xFF00_1506; // Date column register
    public static final int REGISTER_DAYR   = 0xFF00_1507; // Day of week column register
    public static final int REGISTER_PAGER  = 0xFF00_1508; // PAGE register
    public static final int REGISTER_RESTR  = 0xFF00_150C; // Reset register

    // Hi speed Serial ports
    public static final  int NUM_HSERIAL_IF       = 3;
    private static final int HSERIAL_OFFSET_SHIFT = 4; // 1 << 4 = 0x10 bytes per interface
    // Note: Big endian byte encoding
    private static final int REGISTER_HSC0BUF     = 0xFF00_1803; // TX/RX buffer register
    private static final int REGISTER_HSC0EN      = 0xFF00_1804; // Enable register
    private static final int REGISTER_HSC0MOD2    = 0xFF00_1805; // Mode control register 2
    private static final int REGISTER_HSC0MOD1    = 0xFF00_1806; // Mode control register 1
    private static final int REGISTER_HBR0ADD     = 0xFF00_1807; // Baud rate generator control register 2
    private static final int REGISTER_HSC0TST     = 0xFF00_1808; // Transmit FIFO status register
    private static final int REGISTER_HSC0RST     = 0xFF00_1809; // Receive FIFO status register
    private static final int REGISTER_HSC0TFC     = 0xFF00_180A; // Transmit FIFO control register
    private static final int REGISTER_HSC0RFC     = 0xFF00_180B; // Receive FIFO control register
    private static final int REGISTER_HBR0CR      = 0xFF00_180C; // Baud rate generator control register
    private static final int REGISTER_HSC0MOD0    = 0xFF00_180D; // Mode control register 0
    private static final int REGISTER_HSC0CR      = 0xFF00_180E; // Control register
    private static final int REGISTER_HSC0FCNF    = 0xFF00_180F; // FIFO configuration register


    // Clock Generator.
    // Note: section 6.6.1 lists addresses in the range FF0017xx while section 22 lists FF0019xx, which seems correct
    public static final int REGISTER_SYSCR  = 0xFF00_1900; // System Control Register
    public static final int REGISTER_OSCCR  = 0xFF00_1904; // Oscillator Control Register
    public static final int REGISTER_SDBYCR = 0xFF00_1908; // Standby Control Register
    public static final int REGISTER_PLLSEL = 0xFF00_190C; // PLL Select Register
    public static final int REGISTER_SCKSEL = 0xFF00_1910; // System Clock Select Register
    public static final int REGISTER_ICRCG  = 0xFF00_1914; // CG interrupt request clear register
    public static final int REGISTER_NMIFLG = 0xFF00_1918; // NMI flag register
    public static final int REGISTER_RSTFLG = 0xFF00_191C; // Reset flag register
    public static final int REGISTER_IMCGA  = 0xFF00_1920; // CG interrupt mode control register A
    public static final int REGISTER_IMCGB  = 0xFF00_1924; // CG interrupt mode control register B
    public static final int REGISTER_IMCGC  = 0xFF00_1928; // CG interrupt mode control register C
    public static final int REGISTER_IMCGD  = 0xFF00_192C; // CG interrupt mode control register D
    public static final int REGISTER_IMCGE  = 0xFF00_1930; // CG interrupt mode control register E
    public static final int REGISTER_IMCGF  = 0xFF00_1934; // CG interrupt mode control register F
    public static final int REGISTER_IMCG10 = 0xFF00_1938; // CG interrupt mode control register 10
    public static final int REGISTER_IMCG11 = 0xFF00_193C; // CG interrupt mode control register 11

    // Key-on Wakeup
    public static final  int NUM_KEY           = 32;
    private static final int KEY_OFFSET_SHIFT  = 2; // 1 << 2 = 4 bytes per key
    private static final int REGISTER_KWUPST00 = 0xFF00_1A00; // Key Status register
    private static final int REGISTER_PKEY     = 0xFF00_1A80; // Key State register
    private static final int REGISTER_KWUPCNT  = 0xFF00_1A84; // Key Control register
    private static final int REGISTER_KWUPCLR  = 0xFF00_1A88; // Key Interrupt clear register
    private static final int REGISTER_KWUPINT  = 0xFF00_1A8C; // Key Interrupt register

    // I/O Port
    public static final  int NUM_PORT          = 20;
    private static final int PORT_OFFSET_SHIFT = 6; // 1 << 6 = 0x40 bytes per port
    private static final int REGISTER_PORT0    = 0xFF00_4000; // Port register (value)
    private static final int REGISTER_PORT0CR  = 0xFF00_4004; // Port control register
    private static final int REGISTER_PORT0FC1 = 0xFF00_4008; // Port function register 1
    private static final int REGISTER_PORT0FC2 = 0xFF00_400C; // Port function register 2
    private static final int REGISTER_PORT0FC3 = 0xFF00_4010; // Port function register 3
    private static final int REGISTER_PORT0ODE = 0xFF00_4028; // Port open-drain control register
    private static final int REGISTER_PORT0PUP = 0xFF00_402C; // Port pull-up control register
    private static final int REGISTER_PORT0PIE = 0xFF00_4038; // Port input enable control register

    // 16-bit Timer
    public static final  int NUM_16B_TIMER      = 18;
    private static final int TIMER_OFFSET_SHIFT = 6; // 1 << 6 = 0x40 bytes per timer
    private static final int REGISTER_TB0EN     = 0xFF00_4500; // Timer enable register
    private static final int REGISTER_TB0RUN    = 0xFF00_4504; // Timer RUN register
    private static final int REGISTER_TB0CR     = 0xFF00_4508; // Timer control register
    private static final int REGISTER_TB0MOD    = 0xFF00_450C; // Timer mode register
    private static final int REGISTER_TB0FFCR   = 0xFF00_4510; // Timer flip-flop control register
    private static final int REGISTER_TB0ST     = 0xFF00_4514; // Timer status register
    private static final int REGISTER_TB0IM     = 0xFF00_4518; // Timer Interrupt mask register
    private static final int REGISTER_TB0UC     = 0xFF00_451C; // Timer up counter register
    private static final int REGISTER_TB0RG0    = 0xFF00_4520; // Timer compare value 0 register
    private static final int REGISTER_TB0RG1    = 0xFF00_4524; // Timer compare value 1 register
    private static final int REGISTER_TB0CP0    = 0xFF00_4528; // Timer captured value 0 register
    private static final int REGISTER_TB0CP1    = 0xFF00_452C; // Timer captured value 1 register

    // 32-bit Capture input timer
    public static final  int NUM_32B_TIMER     = 1;
    private static final int REGISTER_TCEN     = 0xFF00_4A00; // Timer Enable register
    private static final int REGISTER_TBTRUN   = 0xFF00_4A04; // Timer Run register
    private static final int REGISTER_TBTCR    = 0xFF00_4A08; // Timer Control Register
    private static final int REGISTER_TBTCAP   = 0xFF00_4A0C; // Software Capture Register
    /** Software Capture Register. Note that the spec doesn't give any description of this register.
     * The Japanese v1.4 version of the spec has a sentence at the end of section 12.2.3 that translates as:
     * "(2) Lead Capture
     * This counter is counting up, capture value when reading it TBTRDCAP register lead capture possible is possible"
     * So I think reading this register just returns the current value of the counter
     */
    private static final int REGISTER_TBTRDCAP = 0xFF00_4A10; // Software Read Capture Register.

    public static final  int   NUM_COMPARE_CHANNEL        = 8;
    private static final short INPUT_COMPARE_OFFSET_SHIFT = 4; // 1 << 4 = 0x10 bytes per compare channel
    private static final int   REGISTER_CMPCTL0           = 0xFF00_4A20; // Compare Control Register
    private static final int   REGISTER_TCCMP0            = 0xFF00_4A24; // Compare value register
    public static final  int   NUM_CAPTURE_CHANNEL        = 4;
    private static final short INPUT_CAPTURE_OFFSET_SHIFT = 4; // 1 << 4 = 0x10 bytes per capture channel
    private static final int   REGISTER_CAPCR0            = 0xFF00_4AA0; // Capture control register
    private static final int   REGISTER_TCCAP0            = 0xFF00_4AA4; // Capture register

    // Normal serial ports
    public static final  int NUM_SERIAL_IF       = 3;
    private static final int SERIAL_OFFSET_SHIFT = 6; // 1 << 6 = 0x40 bytes per interface
    private static final int REGISTER_SC0EN      = 0xFF00_4C00; // Enable register
    private static final int REGISTER_SC0BUF     = 0xFF00_4C04; // TX/RX buffer register
    private static final int REGISTER_SC0CR      = 0xFF00_4C08; // Control register
    private static final int REGISTER_SC0MOD0    = 0xFF00_4C0C; // Mode control register 0
    private static final int REGISTER_BR0CR      = 0xFF00_4C10; // Baud rate generator control register
    private static final int REGISTER_BR0ADD     = 0xFF00_4C14; // Baud rate generator control register 2
    private static final int REGISTER_SC0MOD1    = 0xFF00_4C18; // Mode control register 1
    private static final int REGISTER_SC0MOD2    = 0xFF00_4C1C; // Mode control register 2
    private static final int REGISTER_SC0RFC     = 0xFF00_4C20; // Receive FIFO control register
    private static final int REGISTER_SC0TFC     = 0xFF00_4C24; // Transmit FIFO control register
    private static final int REGISTER_SC0RST     = 0xFF00_4C28; // Receive FIFO status register
    private static final int REGISTER_SC0TST     = 0xFF00_4C2C; // Transmit FIFO status register
    private static final int REGISTER_SC0FCNF    = 0xFF00_4C30; // FIFO configuration register

    // A/D converter
    public static final  int NUM_AD_UNIT          = 3;
    private static final int AD_UNIT_OFFSET_SHIFT = 7; // 1 << 7 = 0x80 bytes per interface
    private static final int REGISTER_ADACLK      = 0xFF00_4D00; // register
    private static final int REGISTER_ADAMOD0     = 0xFF00_4D04; // register
    private static final int REGISTER_ADAMOD1     = 0xFF00_4D08; // register
    private static final int REGISTER_ADAMOD2     = 0xFF00_4D0C; // register
    private static final int REGISTER_ADAMOD3     = 0xFF00_4D10; // register
    private static final int REGISTER_ADAMOD4     = 0xFF00_4D14; // register
    private static final int REGISTER_ADAMOD5     = 0xFF00_4D18; // register
    private static final int REGISTER_ADAREG0     = 0xFF00_4D30; // register
    private static final int REGISTER_ADAREGSP    = 0xFF00_4D50; // register
    private static final int REGISTER_ADACOMREG0  = 0xFF00_4D54; // register
    private static final int REGISTER_ADACOMREG1  = 0xFF00_4D58; // register


    public TxIoListener(Platform platform, boolean logRegisterMessages) {
        super(platform, logRegisterMessages);
    }

    @Override
    public boolean matches(int address) {
        return address >>> 16 == 0xFF00;
    }

    /**
     * Called when reading 8-bit value from register address range
     *
     * @param ioPage
     * @param addr
     * @param value
     * @param accessSource
     * @return value to be returned, or null to return previously written value like normal memory
     */
    public Byte onLoadData8(byte[] ioPage, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_IMC00 && addr < (REGISTER_IMC19+4)) {
            // IMC registers.
            return (byte)(((TxInterruptController)platform.getInterruptController()).getImc(addr-REGISTER_IMC00));
        }
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
        else if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + (NUM_16B_TIMER << TIMER_OFFSET_SHIFT)) {
            // Timer configuration registers
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_SHIFT;
            TxTimer txTimer = (TxTimer)platform.getProgrammableTimers()[timerNr];
            switch (addr - (timerNr << TIMER_OFFSET_SHIFT)) {
                case REGISTER_TB0EN + 3:
                    return (byte) txTimer.getTben();
                case REGISTER_TB0RUN + 3:
                    return (byte) txTimer.getTbrun();
                case REGISTER_TB0CR + 3:
                    return (byte) txTimer.getTbcr();
                case REGISTER_TB0MOD + 3:
                    return (byte) txTimer.getTbmod();
                case REGISTER_TB0FFCR + 3:
                    return (byte) txTimer.getTbffcr();
                case REGISTER_TB0ST + 3:
                    return (byte) txTimer.getTbst();
                case REGISTER_TB0IM + 3:
                    return (byte) txTimer.getTbim();
                case REGISTER_TB0UC + 2:
                case REGISTER_TB0UC + 3:
                    stop("The TBnUC register cannot be accessed by 8-bit");
                case REGISTER_TB0RG0 + 2:
                case REGISTER_TB0RG0 + 3:
                    stop("The TBnRG0 register cannot be accessed by 8-bit for now");
                case REGISTER_TB0RG1 + 2:
                case REGISTER_TB0RG1 + 3:
                    stop("The TBnRG1 register cannot be accessed by 8-bit for now");
                case REGISTER_TB0CP0 + 3:
                    return (byte) txTimer.getTbcp0();
                case REGISTER_TB0CP1 + 3:
                    return (byte) txTimer.getTbcp1();
            }
        }
        else if (addr >= REGISTER_TCEN && addr < REGISTER_CAPCR0 + (NUM_CAPTURE_CHANNEL << INPUT_CAPTURE_OFFSET_SHIFT )) {
            // Capture Input configuration registers
            TxInputCaptureTimer txInputCaptureTimer = (TxInputCaptureTimer)platform.getProgrammableTimers()[NUM_16B_TIMER];
            if (addr < REGISTER_CMPCTL0) {
                switch (addr) {
                    case REGISTER_TCEN + 3:
                        return (byte)txInputCaptureTimer.getTcen();
                    case REGISTER_TBTRUN + 3:
                        return (byte)txInputCaptureTimer.getTbtrun();
                    case REGISTER_TBTCR + 3:
                        return (byte)txInputCaptureTimer.getTbtcr();
                    case REGISTER_TBTCAP + 3:
                        return (byte)txInputCaptureTimer.getTbtcap();
                    case REGISTER_TBTRDCAP + 3:
                        return (byte)txInputCaptureTimer.getCurrentValue();
                }
            }
            else if (addr < REGISTER_CAPCR0) {
                int compareChannel = (addr - REGISTER_CMPCTL0) >> INPUT_COMPARE_OFFSET_SHIFT;
                switch (addr - (compareChannel << INPUT_COMPARE_OFFSET_SHIFT)) {
                    case REGISTER_CMPCTL0 + 3:
                        return (byte) txInputCaptureTimer.getCmpctl(compareChannel);
                    case REGISTER_TCCMP0 + 3:
                        return (byte) txInputCaptureTimer.getTccmp(compareChannel);
                }
            }
            else {
                int captureChannel = (addr - REGISTER_CAPCR0) >> INPUT_CAPTURE_OFFSET_SHIFT;
                switch (addr - (captureChannel << INPUT_CAPTURE_OFFSET_SHIFT)) {
                    case REGISTER_CAPCR0 + 3:
                        return (byte) txInputCaptureTimer.getCapcr(captureChannel);
                    case REGISTER_TCCAP0 + 3:
                        return (byte) txInputCaptureTimer.getTccap(captureChannel);
                }
            }
        }
        else if (addr >= REGISTER_SC0EN && addr < REGISTER_SC0EN + (NUM_SERIAL_IF << SERIAL_OFFSET_SHIFT)) {
            // Serial Interface configuration registers
            int serialInterfaceNr = (addr - REGISTER_SC0EN) >> SERIAL_OFFSET_SHIFT;
            TxSerialInterface txSerialInterface = (TxSerialInterface)platform.getSerialInterfaces()[serialInterfaceNr];
            switch (addr - (serialInterfaceNr << SERIAL_OFFSET_SHIFT)) {
                case REGISTER_SC0EN + 3:
                    return (byte) txSerialInterface.getEn();
                case REGISTER_SC0BUF + 3:
                    return (byte) txSerialInterface.getBuf();
                case REGISTER_SC0CR + 3:
                    return (byte) txSerialInterface.getCr();
                case REGISTER_SC0MOD0 + 3:
                    return (byte) txSerialInterface.getMod0();
                case REGISTER_SC0MOD1 + 3:
                    return (byte) txSerialInterface.getMod1();
                case REGISTER_SC0MOD2 + 3:
                    return (byte) txSerialInterface.getMod2();
                case REGISTER_BR0CR + 3:
                    return (byte) txSerialInterface.getBrcr();
                case REGISTER_BR0ADD + 3:
                    return (byte) txSerialInterface.getBradd();
                case REGISTER_SC0RFC + 3:
                    return (byte) txSerialInterface.getRfc();
                case REGISTER_SC0TFC + 3:
                    return (byte) txSerialInterface.getTfc();
                case REGISTER_SC0RST + 3:
                    return (byte) txSerialInterface.getRst();
                case REGISTER_SC0TST + 3:
                    return (byte) txSerialInterface.getTst();
                case REGISTER_SC0FCNF + 3:
                    return (byte) txSerialInterface.getFcnf();
            }
        }
        else if (addr >= REGISTER_HSC0BUF && addr < REGISTER_HSC0BUF + (NUM_HSERIAL_IF << HSERIAL_OFFSET_SHIFT)) {
            // Hi-speed Serial Interface configuration registers
            int hserialInterfaceNr = (addr - REGISTER_HSC0BUF) >> HSERIAL_OFFSET_SHIFT;
            TxSerialInterface txSerialInterface = (TxSerialInterface)platform.getSerialInterfaces()[NUM_SERIAL_IF + hserialInterfaceNr];
            switch (addr - (hserialInterfaceNr << HSERIAL_OFFSET_SHIFT)) {
                case REGISTER_HSC0BUF: // No +3. These are all 8-bit register (even if hsc0buf leaves 3 blank addresses)
                    return (byte) txSerialInterface.getBuf();
                case REGISTER_HBR0ADD:
                    return (byte) txSerialInterface.getBradd();
                case REGISTER_HSC0MOD1:
                    return (byte) txSerialInterface.getMod1();
                case REGISTER_HSC0MOD2:
                    return (byte) txSerialInterface.getMod2();
                case REGISTER_HSC0EN:
                    return (byte) txSerialInterface.getEn();
                case REGISTER_HSC0RFC:
                    return (byte) txSerialInterface.getRfc();
                case REGISTER_HSC0TFC:
                    return (byte) txSerialInterface.getTfc();
                case REGISTER_HSC0RST:
                    return (byte) txSerialInterface.getRst();
                case REGISTER_HSC0TST:
                    return (byte) txSerialInterface.getTst();
                case REGISTER_HSC0FCNF:
                    return (byte) txSerialInterface.getFcnf();
                case REGISTER_HSC0CR:
                    return (byte) txSerialInterface.getCr();
                case REGISTER_HSC0MOD0:
                    return (byte) txSerialInterface.getMod0();
                case REGISTER_HBR0CR:
                    return (byte) txSerialInterface.getBrcr();
            }
        }
        else if (addr >= REGISTER_CCR0 && addr < REGISTER_CCR0 + (NUM_DMA_CHANNEL << DMA_CHANNEL_OFFSET_SHIFT)) {
            // DMA channel configuration registers
            int dmaChannelNr = (addr - REGISTER_CCR0) >> DMA_CHANNEL_OFFSET_SHIFT;
            TxDmaChannel channel = ((TxDmaController)platform.getDmaController()).getChannel(dmaChannelNr);
            switch (addr - (dmaChannelNr << DMA_CHANNEL_OFFSET_SHIFT)) {
                case REGISTER_CCR0:
                case REGISTER_CCR0 + 1:
                case REGISTER_CCR0 + 2:
                case REGISTER_CCR0 + 3:
                    return (byte)(channel.getCcr() >> ((3 - (addr & 0b11)) * 8));
                case REGISTER_CSR0 + 3:
                    return (byte)channel.getCsr();
                case REGISTER_SAR0 + 3:
                    return (byte)channel.getSar();
                case REGISTER_DAR0 + 3:
                    return (byte)channel.getDar();
                case REGISTER_BCR0 + 3:
                    return (byte)channel.getBcr();
                case REGISTER_DTCR0 + 3:
                    return (byte)channel.getDtcr();

                default:
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a DMA register");
            }
        }
        else if (addr >= REGISTER_HOURR && addr < REGISTER_RESTR + 4) {
            // RTC registers
            TxRealtimeClock realtimeClock = ((TxRealtimeClock)platform.getRealtimeClock());
            switch (addr) {
                case REGISTER_HOURR + 1:
                    return realtimeClock.getHourr();
                case REGISTER_MINR:
                    return realtimeClock.getMinr();
                case REGISTER_SECR:
                    return realtimeClock.getSecr();

                case REGISTER_YEARR:
                    return realtimeClock.getYearr();
                case REGISTER_MONTHR:
                    return realtimeClock.getMonthr();
                case REGISTER_DATER:
                    return realtimeClock.getDater();
                case REGISTER_DAYR:
                    return realtimeClock.getDayr();

                case REGISTER_PAGER + 3:
                    return (byte)realtimeClock.getPager();

                case REGISTER_RESTR + 3:
                    return (byte)realtimeClock.getRestr();

                default:
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a RTC register");
            }
        }
        else if (addr >= REGISTER_KWUPST00 && addr < REGISTER_KWUPINT + 4) {
            // Key registers
            TxKeyCircuit keyCircuit = ((TxKeyCircuit)platform.getKeyCircuit());
            int keyNumber = (addr - REGISTER_KWUPST00) >> KEY_OFFSET_SHIFT;
            switch (addr) {
                case REGISTER_PKEY:
                case REGISTER_PKEY + 1:
                case REGISTER_PKEY + 2:
                case REGISTER_PKEY + 3:
                    return (byte)(keyCircuit.getPKEY() >> ((3 - (addr & 0b11)) * 8));
                case REGISTER_KWUPCNT + 3:
                    return (byte)keyCircuit.getKWUPCNT();
                case REGISTER_KWUPCLR + 3:
                    return (byte)keyCircuit.getKWUPCLR();
                case REGISTER_KWUPINT:
                case REGISTER_KWUPINT + 1:
                case REGISTER_KWUPINT + 2:
                case REGISTER_KWUPINT + 3:
                    return keyCircuit.getKWUPINTn(addr & 0b11);
                default:
                    if ((addr-REGISTER_KWUPST00) == (keyNumber << KEY_OFFSET_SHIFT)) {
                        return (byte)keyCircuit.keys[keyNumber].getKWUPST();
                    }
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a KEY register");
            }
        }
        else if (addr >= REGISTER_ADACLK && addr < REGISTER_ADACLK + (NUM_AD_UNIT << AD_UNIT_OFFSET_SHIFT)) {
            // AD unit configuration registers
            int adUnitNumber = (addr - REGISTER_ADACLK) >> AD_UNIT_OFFSET_SHIFT;
            TxAdUnit unit = ((TxAdConverter)platform.getAdConverter()).units[adUnitNumber];
            int shiftedAddress = addr - (adUnitNumber << AD_UNIT_OFFSET_SHIFT);
            if (shiftedAddress >= REGISTER_ADAREG0 && shiftedAddress < REGISTER_ADAREG0 + 32 ) {
                int channelNumber = (shiftedAddress - REGISTER_ADAREG0) / 4;
                if (channelNumber < unit.getNumChannels()) {
                    return (byte)unit.getReg(channelNumber);
                }
                else {
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a A/D converter channel register");
                }
            }
            else {
                switch (shiftedAddress) {
                    case REGISTER_ADACLK + 3:
                        return (byte)unit.getClk();
                    case REGISTER_ADAMOD0 + 3:
                        return (byte)unit.getMod0();
                    case REGISTER_ADAMOD1 + 3:
                        return (byte)unit.getMod1();
                    case REGISTER_ADAMOD2 + 3:
                        return (byte)unit.getMod2();
                    case REGISTER_ADAMOD3 + 3:
                        return (byte)unit.getMod3();
                    case REGISTER_ADAMOD4 + 3:
                        return (byte)unit.getMod4();
                    case REGISTER_ADAMOD5 + 3:
                        return (byte)unit.getMod5();
                    case REGISTER_ADAREGSP + 3:
                        return (byte)(unit.getRegSp());
                    case REGISTER_ADACOMREG0 + 3:
                        return (byte)(unit.getComReg0());
                    case REGISTER_ADACOMREG1 + 3:
                        return (byte)(unit.getComReg1());
                    default:
                        stop("Address 0x" + Format.asHex(addr, 8) + " is not a A/D converter register");
                }
            }
        } else if (addr >= REGISTER_IMCGA && addr < (REGISTER_IMCG11+4)) {
            // IMCG registers.
            return (byte)(((TxInterruptController)platform.getInterruptController()).getImcg(addr-REGISTER_IMCGA));
        }
        else switch (addr) {
            // Clock generator
            case REGISTER_SYSCR:
                return 0;
            case REGISTER_SYSCR + 1:
                return ((TxClockGenerator)platform.getClockGenerator()).getSyscr2();
            case REGISTER_SYSCR + 2:
                return ((TxClockGenerator)platform.getClockGenerator()).getSyscr1();
            case REGISTER_SYSCR + 3:
                return ((TxClockGenerator)platform.getClockGenerator()).getSyscr0();
            case REGISTER_OSCCR:
                return 0;
            case REGISTER_OSCCR + 1:
                return 0;
            case REGISTER_OSCCR + 2:
                return ((TxClockGenerator)platform.getClockGenerator()).getOsccr1();
            case REGISTER_OSCCR + 3:
                return ((TxClockGenerator)platform.getClockGenerator()).getOsccr0();
            case REGISTER_PLLSEL + 3:
                return (byte)((TxClockGenerator)platform.getClockGenerator()).getPllsel();
            case REGISTER_NMIFLG + 3:
                return (byte)((TxInterruptController)platform.getInterruptController()).readAndClearNmiFlag();
            case REGISTER_RSTFLG + 3:
                return (byte)((TxClockGenerator)platform.getClockGenerator()).getRstflg();

            case REGISTER_DREQFLG + 3:
                return (byte)((TxInterruptController)platform.getInterruptController()).getDreqflg();

            // DMA controller
            case REGISTER_DCR + 3:
                return (byte)((TxDmaController)platform.getDmaController()).getDcr();
            case REGISTER_RSR + 3:
                return (byte)((TxDmaController)platform.getDmaController()).getRsr();
            case REGISTER_DHR + 3:
                return (byte)((TxDmaController)platform.getDmaController()).getDhr();
        }

        if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr, 8) + ": Load8 is not supported yet");

        return null;
    }

    /**
     * Called when reading 16-bit value from register address range
     *
     *
     * @param ioPage
     * @param addr
     * @param value
     * @param accessSource
     * @return value to be returned, or null to return previously written value like normal memory
     */
    public Integer onLoadData16(byte[] ioPage, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_IMC00 && addr < (REGISTER_IMC19+4)) {
            // IMC registers.
            TxInterruptController intc = (TxInterruptController)platform.getInterruptController();
            
            return ((intc.getImc(addr-REGISTER_IMC00)<<8) | intc.getImc(addr-REGISTER_IMC00+1));
        }
        if (addr >= REGISTER_PORT0 && addr < REGISTER_PORT0 + (NUM_PORT << PORT_OFFSET_SHIFT)) {
            // Port configuration registers
            stop("The I/O port registers cannot be accessed by 16-bit for now");
        }
        else if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + (NUM_16B_TIMER << TIMER_OFFSET_SHIFT)) {
            // Timer configuration registers
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_SHIFT;
            TxTimer txTimer = (TxTimer)platform.getProgrammableTimers()[timerNr];
            switch (addr - (timerNr << TIMER_OFFSET_SHIFT)) {
                case REGISTER_TB0EN + 2:
                    return txTimer.getTben() & 0xFFFF;
                case REGISTER_TB0RUN + 2:
                    return txTimer.getTbrun() & 0xFFFF;
                case REGISTER_TB0CR + 2:
                    return txTimer.getTbcr() & 0xFFFF;
                case REGISTER_TB0MOD + 2:
                    return txTimer.getTbmod() & 0xFFFF;
                case REGISTER_TB0FFCR + 2:
                    return txTimer.getTbffcr() & 0xFFFF;
                case REGISTER_TB0ST + 2:
                    return txTimer.getTbst() & 0xFFFF;
                case REGISTER_TB0IM + 2:
                    return txTimer.getTbim() & 0xFFFF;
                case REGISTER_TB0UC + 2:
                    return txTimer.getTbuc() & 0xFFFF;
                case REGISTER_TB0RG0 + 2:
                    return txTimer.getTbrg0() & 0xFFFF;
                case REGISTER_TB0RG1 + 2:
                    return txTimer.getTbrg1() & 0xFFFF;
                case REGISTER_TB0CP0 + 2:
                    return txTimer.getTbcp0() & 0xFFFF;
                case REGISTER_TB0CP1 + 2:
                    return txTimer.getTbcp1() & 0xFFFF;
            }
        }
        else if (addr >= REGISTER_TCEN && addr < REGISTER_CAPCR0 + (NUM_CAPTURE_CHANNEL << INPUT_CAPTURE_OFFSET_SHIFT )) {
            // Capture Input configuration registers
            TxInputCaptureTimer txInputCaptureTimer = (TxInputCaptureTimer)platform.getProgrammableTimers()[NUM_16B_TIMER];
            if (addr < REGISTER_CMPCTL0) {
                switch (addr) {
                    case REGISTER_TCEN + 2:
                        return txInputCaptureTimer.getTcen() & 0xFFFF;
                    case REGISTER_TBTRUN + 2:
                        return txInputCaptureTimer.getTbtrun() & 0xFFFF;
                    case REGISTER_TBTCR + 2:
                        return txInputCaptureTimer.getTbtcr() & 0xFFFF;
                    case REGISTER_TBTCAP + 2:
                        return txInputCaptureTimer.getTbtcap() & 0xFFFF;
                    case REGISTER_TBTRDCAP + 2:
                        return txInputCaptureTimer.getCurrentValue() & 0xFFFF;
                }
            }
            else if (addr < REGISTER_CAPCR0) {
                int compareChannel = (addr - REGISTER_CMPCTL0) >> INPUT_COMPARE_OFFSET_SHIFT;
                switch (addr - (compareChannel << INPUT_COMPARE_OFFSET_SHIFT)) {
                    case REGISTER_CMPCTL0 + 2:
                        return  txInputCaptureTimer.getCmpctl(compareChannel) & 0xFFFF;
                    case REGISTER_TCCMP0 + 2:
                        return  txInputCaptureTimer.getTccmp(compareChannel) & 0xFFFF;
                }

            }
            else {
                int captureChannel = (addr - REGISTER_CAPCR0) >> INPUT_CAPTURE_OFFSET_SHIFT;
                switch (addr - (captureChannel << INPUT_CAPTURE_OFFSET_SHIFT)) {
                    case REGISTER_CAPCR0 + 2:
                        return  txInputCaptureTimer.getCapcr(captureChannel) & 0xFFFF;
                    case REGISTER_TCCAP0 + 2:
                        return  txInputCaptureTimer.getTccap(captureChannel) & 0xFFFF;
                }
            }
        }
        else if (addr >= REGISTER_HOURR && addr < REGISTER_RESTR + 4) {
            stop("The RTC registers cannot be accessed by 16-bit for now");
        }
        else if (addr >= REGISTER_KWUPST00 && addr < REGISTER_KWUPINT + 4) {
            stop("The KEY registers cannot be accessed by 16-bit for now");
        }
        else if (addr >= REGISTER_SC0EN && addr < REGISTER_SC0EN + (NUM_SERIAL_IF << SERIAL_OFFSET_SHIFT)) {
            // Serial Interface configuration registers
            int serialInterfaceNr = (addr - REGISTER_SC0EN) >> SERIAL_OFFSET_SHIFT;
            TxSerialInterface txSerialInterface = (TxSerialInterface)platform.getSerialInterfaces()[serialInterfaceNr];
            switch (addr - (serialInterfaceNr << SERIAL_OFFSET_SHIFT)) {
                case REGISTER_SC0EN + 2:
                    return txSerialInterface.getEn() & 0xFFFF;
                case REGISTER_SC0BUF + 2:
                    return txSerialInterface.getBuf() & 0xFFFF;
                case REGISTER_SC0CR + 2:
                    return txSerialInterface.getCr() & 0xFFFF;
                case REGISTER_SC0MOD0 + 2:
                    return txSerialInterface.getMod0() & 0xFFFF;
                case REGISTER_SC0MOD1 + 2:
                    return txSerialInterface.getMod1() & 0xFFFF;
                case REGISTER_SC0MOD2 + 2:
                    return txSerialInterface.getMod2() & 0xFFFF;
                case REGISTER_BR0CR + 2:
                    return txSerialInterface.getBrcr() & 0xFFFF;
                case REGISTER_BR0ADD + 2:
                    return txSerialInterface.getBradd() & 0xFFFF;
                case REGISTER_SC0RFC + 2:
                    return txSerialInterface.getRfc() & 0xFFFF;
                case REGISTER_SC0TFC + 2:
                    return txSerialInterface.getTfc() & 0xFFFF;
                case REGISTER_SC0RST + 2:
                    return txSerialInterface.getRst() & 0xFFFF;
                case REGISTER_SC0TST + 2:
                    return txSerialInterface.getTst() & 0xFFFF;
                case REGISTER_SC0FCNF + 2:
                    return txSerialInterface.getFcnf() & 0xFFFF;
            }
        }
        else if (addr >= REGISTER_HSC0BUF && addr < REGISTER_HSC0BUF + (NUM_HSERIAL_IF << HSERIAL_OFFSET_SHIFT)) {
            // Hi-speed Serial Interface configuration registers
            stop("Serial register 0x" + Format.asHex(addr, 8) + " can only be read by 8 bits");
        }
        else if (addr >= REGISTER_ADACLK && addr < REGISTER_ADACLK + (NUM_AD_UNIT << AD_UNIT_OFFSET_SHIFT)) {
            // AD unit configuration registers
            int adUnitNumber = (addr - REGISTER_ADACLK) >> AD_UNIT_OFFSET_SHIFT;
            TxAdUnit unit = ((TxAdConverter)platform.getAdConverter()).units[adUnitNumber];
            int shiftedAddress = addr - (adUnitNumber << AD_UNIT_OFFSET_SHIFT);
            if (shiftedAddress >= REGISTER_ADAREG0 && shiftedAddress < REGISTER_ADAREG0 + 32 ) {
                int channelNumber = (shiftedAddress - REGISTER_ADAREG0) / 4;
                if (channelNumber < unit.getNumChannels()) {
                    return unit.getReg(channelNumber) & 0xFFFF;
                }
                else {
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a A/D converter channel register");
                }
            }
            else {
                switch (shiftedAddress) {
                    case REGISTER_ADACLK + 2:
                        return unit.getClk() & 0xFFFF;
                    case REGISTER_ADAMOD0 + 2:
                        return unit.getMod0() & 0xFFFF;
                    case REGISTER_ADAMOD1 + 2:
                        return unit.getMod1() & 0xFFFF;
                    case REGISTER_ADAMOD2 + 2:
                        return unit.getMod2() & 0xFFFF;
                    case REGISTER_ADAMOD3 + 2:
                        return unit.getMod3() & 0xFFFF;
                    case REGISTER_ADAMOD4 + 2:
                        return unit.getMod4() & 0xFFFF;
                    case REGISTER_ADAMOD5 + 2:
                        return unit.getMod5() & 0xFFFF;
                    case REGISTER_ADAREGSP + 2:
                        return (unit.getRegSp()) & 0xFFFF;
                    case REGISTER_ADACOMREG0 + 2:
                        return (unit.getComReg0()) & 0xFFFF;
                    case REGISTER_ADACOMREG1 + 2:
                        return (unit.getComReg1()) & 0xFFFF;
                    default:
                        stop("Address 0x" + Format.asHex(addr, 8) + " is not a A/D converter register");
                }
            }
        } else if (addr >= REGISTER_IMCGA && addr < (REGISTER_IMCG11+4)) {
            // IMCG registers.
            TxInterruptController intc = (TxInterruptController)platform.getInterruptController();
            
            return ((intc.getImcg(addr-REGISTER_IMCGA)<<8) | intc.getImcg(addr-REGISTER_IMCGA+1));
        }
        else switch (addr){
            // Clock generator
            case REGISTER_SYSCR:
                stop("The SYSCR register can not be accessed by 16-bit for now");
            case REGISTER_SYSCR + 2:
                stop("The SYSCR register can not be accessed by 16-bit for now");
            case REGISTER_OSCCR:
                stop("The OSCCR register can not be accessed by 16-bit for now");
            case REGISTER_OSCCR + 2:
                stop("The OSCCR register can not be accessed by 16-bit for now");
            case REGISTER_PLLSEL + 2:
                return ((TxClockGenerator)platform.getClockGenerator()).getPllsel() & 0xFFFF;
            case REGISTER_NMIFLG + 2:
                return ((TxInterruptController)platform.getInterruptController()).readAndClearNmiFlag() & 0xFFFF;
            case REGISTER_RSTFLG + 2:
                return ((TxClockGenerator)platform.getClockGenerator()).getRstflg() & 0xFFFF;
            case REGISTER_DREQFLG + 2:
                return ((TxInterruptController)platform.getInterruptController()).getDreqflg() & 0xFFFF;
        }

        if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr, 8) + ": Load16 is not supported yet");

        return null;
    }

    /**
     * Called when reading 32-bit value from register address range
     *
     *
     * @param ioPage
     * @param addr
     * @param value
     * @param accessSource
     * @return value to be returned, or null to return previously written value like normal memory
     */
    public Integer onLoadData32(byte[] ioPage, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_IMC00 && addr < (REGISTER_IMC19+4)) {
            // IMC registers.
            TxInterruptController intc = (TxInterruptController)platform.getInterruptController();
            
            return ((intc.getImc(addr-REGISTER_IMC00)<<24) | 
                    (intc.getImc(addr-REGISTER_IMC00+1)<<16) | 
                    (intc.getImc(addr-REGISTER_IMC00+2)<<8) | 
                     intc.getImc(addr-REGISTER_IMC00+3));
        }
        else if (addr >= REGISTER_PORT0 && addr < REGISTER_PORT0 + (NUM_PORT << PORT_OFFSET_SHIFT)) {
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
        else if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + (NUM_16B_TIMER << TIMER_OFFSET_SHIFT)) {
            // Timer configuration registers
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_SHIFT;
            TxTimer txTimer = (TxTimer)platform.getProgrammableTimers()[timerNr];
            switch (addr - (timerNr << TIMER_OFFSET_SHIFT)) {
                case REGISTER_TB0EN:
                    return txTimer.getTben();
                case REGISTER_TB0RUN:
                    return txTimer.getTbrun();
                case REGISTER_TB0CR:
                    return txTimer.getTbcr();
                case REGISTER_TB0MOD:
                    return txTimer.getTbmod();
                case REGISTER_TB0FFCR:
                    return txTimer.getTbffcr();
                case REGISTER_TB0ST:
                    return txTimer.getTbst();
                case REGISTER_TB0IM:
                    return txTimer.getTbim();
                case REGISTER_TB0UC:
                    return txTimer.getTbuc();
                case REGISTER_TB0RG0:
                    return txTimer.getTbrg0();
                case REGISTER_TB0RG1:
                    return txTimer.getTbrg1();
                case REGISTER_TB0CP0:
                    return txTimer.getTbcp0();
                case REGISTER_TB0CP1:
                    return txTimer.getTbcp1();
            }
        }
        else if (addr >= REGISTER_TCEN && addr < REGISTER_CAPCR0 + (NUM_CAPTURE_CHANNEL << INPUT_CAPTURE_OFFSET_SHIFT )) {
            // Capture Input configuration registers
            TxInputCaptureTimer txInputCaptureTimer = (TxInputCaptureTimer)platform.getProgrammableTimers()[NUM_16B_TIMER];
            if (addr < REGISTER_CMPCTL0) {
                switch (addr) {
                    case REGISTER_TCEN:
                        return txInputCaptureTimer.getTcen();
                    case REGISTER_TBTRUN:
                        return txInputCaptureTimer.getTbtrun();
                    case REGISTER_TBTCR:
                        return txInputCaptureTimer.getTbtcr();
                    case REGISTER_TBTCAP:
                        return txInputCaptureTimer.getTbtcap();
                    case REGISTER_TBTRDCAP:
                        return txInputCaptureTimer.getCurrentValue();
                }
            }
            else if (addr < REGISTER_CAPCR0) {
                int compareChannel = (addr - REGISTER_CMPCTL0) >> INPUT_COMPARE_OFFSET_SHIFT;
                switch (addr - (compareChannel << INPUT_COMPARE_OFFSET_SHIFT)) {
                    case REGISTER_CMPCTL0:
                        return  txInputCaptureTimer.getCmpctl(compareChannel);
                    case REGISTER_TCCMP0:
                        return  txInputCaptureTimer.getTccmp(compareChannel);
                }

            }
            else {
                int captureChannel = (addr - REGISTER_CAPCR0) >> INPUT_CAPTURE_OFFSET_SHIFT;
                switch (addr - (captureChannel << INPUT_CAPTURE_OFFSET_SHIFT)) {
                    case REGISTER_CAPCR0:
                        return  txInputCaptureTimer.getCapcr(captureChannel);
                    case REGISTER_TCCAP0:
                        return  txInputCaptureTimer.getTccap(captureChannel);
                }
            }
        }
        else if (addr >= REGISTER_SC0EN && addr < REGISTER_SC0EN + (NUM_SERIAL_IF << SERIAL_OFFSET_SHIFT)) {
            // Serial Interface configuration registers
            int serialInterfaceNr = (addr - REGISTER_SC0EN) >> SERIAL_OFFSET_SHIFT;
            TxSerialInterface txSerialInterface = (TxSerialInterface)platform.getSerialInterfaces()[serialInterfaceNr];
            switch (addr - (serialInterfaceNr << SERIAL_OFFSET_SHIFT)) {
                case REGISTER_SC0EN:
                    return txSerialInterface.getEn();
                case REGISTER_SC0BUF:
                    return txSerialInterface.getBuf();
                case REGISTER_SC0CR:
                    return txSerialInterface.getCr();
                case REGISTER_SC0MOD0:
                    return txSerialInterface.getMod0();
                case REGISTER_SC0MOD1:
                    return txSerialInterface.getMod1();
                case REGISTER_SC0MOD2:
                    return txSerialInterface.getMod2();
                case REGISTER_BR0CR:
                    return txSerialInterface.getBrcr();
                case REGISTER_BR0ADD:
                    return txSerialInterface.getBradd();
                case REGISTER_SC0RFC:
                    return txSerialInterface.getRfc();
                case REGISTER_SC0TFC:
                    return txSerialInterface.getTfc();
                case REGISTER_SC0RST:
                    return txSerialInterface.getRst();
                case REGISTER_SC0TST:
                    return txSerialInterface.getTst();
                case REGISTER_SC0FCNF:
                    return txSerialInterface.getFcnf();
            }
        }
        else if (addr >= REGISTER_HSC0BUF && addr < REGISTER_HSC0BUF + (NUM_HSERIAL_IF << HSERIAL_OFFSET_SHIFT)) {
            // Hi-speed Serial Interface configuration registers
            stop("Serial register 0x" + Format.asHex(addr, 8) + " can only be read by 8 bits");
        }
        else if (addr >= REGISTER_CCR0 && addr < REGISTER_CCR0 + (NUM_DMA_CHANNEL << DMA_CHANNEL_OFFSET_SHIFT)) {
            // DMA channel configuration registers
            int dmaChannelNr = (addr - REGISTER_CCR0) >> DMA_CHANNEL_OFFSET_SHIFT;
            TxDmaChannel channel = ((TxDmaController)platform.getDmaController()).getChannel(dmaChannelNr);
            switch (addr - (dmaChannelNr << DMA_CHANNEL_OFFSET_SHIFT)) {
                case REGISTER_CCR0:
                    return channel.getCcr();
                case REGISTER_CSR0:
                    return channel.getCsr();
                case REGISTER_SAR0:
                    return channel.getSar();
                case REGISTER_DAR0:
                    return channel.getDar();
                case REGISTER_BCR0:
                    return channel.getBcr();
                case REGISTER_DTCR0:
                    return channel.getDtcr();
                default:
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a DMA register");
            }
        }
        else if (addr >= REGISTER_HOURR && addr < REGISTER_RESTR + 4) {
            // RTC registers
            TxRealtimeClock realtimeClock = ((TxRealtimeClock)platform.getRealtimeClock());
            switch (addr) {
                case REGISTER_HOURR:
                    return realtimeClock.getTimeReg32();
                case REGISTER_YEARR:
                    return realtimeClock.getDateReg32();
                case REGISTER_PAGER:
                    return realtimeClock.getPager();
                case REGISTER_RESTR:
                    return realtimeClock.getRestr();
                default:
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a RTC register");
            }
        }
        else if (addr >= REGISTER_KWUPST00 && addr < REGISTER_KWUPINT + 4) {
            // Key registers
            TxKeyCircuit keyCircuit = ((TxKeyCircuit)platform.getKeyCircuit());
            int keyNumber = (addr - REGISTER_KWUPST00) >> KEY_OFFSET_SHIFT;
            switch (addr) {
                case REGISTER_PKEY:
                    return keyCircuit.getPKEY();
                case REGISTER_KWUPCNT:
                    return keyCircuit.getKWUPCNT();
                case REGISTER_KWUPCLR:
                    return keyCircuit.getKWUPCLR();
                case REGISTER_KWUPINT:
                    return keyCircuit.getKWUPINT();
                default:
                    if ((addr-REGISTER_KWUPST00) == (keyNumber << KEY_OFFSET_SHIFT))
                        return keyCircuit.keys[keyNumber].getKWUPST();
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a KEY register");
            }
        }
        else if (addr >= REGISTER_ADACLK && addr < REGISTER_ADACLK + (NUM_AD_UNIT << AD_UNIT_OFFSET_SHIFT)) {
            // AD unit configuration registers
            int adUnitNumber = (addr - REGISTER_ADACLK) >> AD_UNIT_OFFSET_SHIFT;
            TxAdUnit unit = ((TxAdConverter)platform.getAdConverter()).units[adUnitNumber];
            int shiftedAddress = addr - (adUnitNumber << AD_UNIT_OFFSET_SHIFT);
            if (shiftedAddress >= REGISTER_ADAREG0 && shiftedAddress < REGISTER_ADAREG0 + 32 ) {
                int channelNumber = (shiftedAddress - REGISTER_ADAREG0) / 4;
                if (channelNumber < unit.getNumChannels()) {
                    return unit.getReg(channelNumber);
                }
                else {
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a A/D converter channel register");
                }
            }
            else {
                switch (shiftedAddress) {
                    case REGISTER_ADACLK:
                        return unit.getClk();
                    case REGISTER_ADAMOD0:
                        return unit.getMod0();
                    case REGISTER_ADAMOD1:
                        return unit.getMod1();
                    case REGISTER_ADAMOD2:
                        return unit.getMod2();
                    case REGISTER_ADAMOD3:
                        return unit.getMod3();
                    case REGISTER_ADAMOD4:
                        return unit.getMod4();
                    case REGISTER_ADAMOD5:
                        return unit.getMod5();
                    case REGISTER_ADAREGSP:
                        return (unit.getRegSp());
                    case REGISTER_ADACOMREG0:
                        return (unit.getComReg0());
                    case REGISTER_ADACOMREG1:
                        return (unit.getComReg1());
                    default:
                        stop("Address 0x" + Format.asHex(addr, 8) + " is not a A/D converter register");
                }
            }
        } else if (addr >= REGISTER_IMCGA && addr < (REGISTER_IMCG11+4)) {
            // IMCG registers.
            TxInterruptController intc = (TxInterruptController)platform.getInterruptController();
            
            return ((intc.getImcg(addr-REGISTER_IMCGA)<<24) | 
                    (intc.getImcg(addr-REGISTER_IMCGA+1)<<16) | 
                    (intc.getImcg(addr-REGISTER_IMCGA+2)<<8) | 
                     intc.getImcg(addr-REGISTER_IMCGA+3));
        }
        switch (addr) {
            // Clock generator
            case REGISTER_SYSCR:
                return ((TxClockGenerator) platform.getClockGenerator()).getSyscr();
            case REGISTER_OSCCR:
                return ((TxClockGenerator) platform.getClockGenerator()).getOsccr();
            case REGISTER_PLLSEL:
                return ((TxClockGenerator)platform.getClockGenerator()).getPllsel();
            // Interrupt Controller
            case REGISTER_ILEV:
                return ((TxInterruptController)platform.getInterruptController()).getIlev();
            case REGISTER_IVR:
                // TODO Until the IVR is read, no hardware interrupt from INTC is accepted (see HW spec section 6.4.1.4)
                return ((TxInterruptController)platform.getInterruptController()).getIvr();
            case REGISTER_NMIFLG:
                return ((TxInterruptController)platform.getInterruptController()).readAndClearNmiFlag();
            case REGISTER_RSTFLG:
                return ((TxClockGenerator)platform.getClockGenerator()).getRstflg();
            case REGISTER_DREQFLG:
                return ((TxInterruptController)platform.getInterruptController()).getDreqflg();
            // DMA controller
            case REGISTER_DCR:
                return ((TxDmaController)platform.getDmaController()).getDcr();
            case REGISTER_RSR:
                return ((TxDmaController)platform.getDmaController()).getRsr();
            case REGISTER_DHR:
                return ((TxDmaController)platform.getDmaController()).getDhr();
        }

        if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr, 8) + ": Load32 is not supported yet");

        return null;
    }

    public void onStore8(byte[] ioPage, int addr, byte value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_IMC00 && addr < (REGISTER_IMC19+4)) {
            // IMC registers.
            ((TxInterruptController)platform.getInterruptController()).setImc(addr-REGISTER_IMC00,value);
            return;
        }
        if (addr >= REGISTER_PORT0 && addr < REGISTER_PORT0 + (NUM_PORT << PORT_OFFSET_SHIFT)) {
            // Port configuration registers
            int portNr = (addr - REGISTER_PORT0) >> PORT_OFFSET_SHIFT;
            TxIoPort txIoPort = (TxIoPort) platform.getIoPorts()[portNr];
            switch (addr - (portNr << PORT_OFFSET_SHIFT)) {
                case REGISTER_PORT0 + 3:
                    txIoPort.setValue(value); break;
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
        else if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + (NUM_16B_TIMER << TIMER_OFFSET_SHIFT)) {
            // Timer configuration registers
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_SHIFT;
            TxTimer txTimer = (TxTimer)platform.getProgrammableTimers()[timerNr];
            switch (addr - (timerNr << TIMER_OFFSET_SHIFT)) {
                case REGISTER_TB0EN + 3:
                    txTimer.setTben(value); break;
                case REGISTER_TB0RUN + 3:
                    txTimer.setTbrun(value); break;
                case REGISTER_TB0CR + 3:
                    txTimer.setTbcr(value); break;
                case REGISTER_TB0MOD + 3:
                    txTimer.setTbmod(value); break;
                case REGISTER_TB0FFCR + 3:
                    txTimer.setTbffcr(value); break;
                case REGISTER_TB0ST + 3:
                    txTimer.setTbst(value); break;
                case REGISTER_TB0IM + 3:
                    txTimer.setTbim(value); break;
                case REGISTER_TB0UC + 3:
                    stop("The TBnUC register cannot be accessed by 8-bit");
                case REGISTER_TB0RG0 + 2:
                case REGISTER_TB0RG0 + 3:
                    /* TODO To write data to the TB0RG0H/L and TB0RG1H/L timer registers, either a 2-byte data transfer
                     * TODO instruction or a 1-byte data transfer instruction written twice in the order of low-order
                     * TODO 8 bits followed by high-order 8 bits can be used.
                     */
                    stop("The TBnRG0 register cannot be accessed by 8-bit for now");
                case REGISTER_TB0RG1 + 2:
                case REGISTER_TB0RG1 + 3:
                    stop("The TBnRG1 register cannot be accessed by 8-bit for now");
                case REGISTER_TB0CP0 + 3:
                    txTimer.setTbcp0(value); break;
                case REGISTER_TB0CP1 + 3:
                    txTimer.setTbcp1(value); break;
            }
        }
        else if (addr >= REGISTER_TCEN && addr < REGISTER_CAPCR0 + (NUM_CAPTURE_CHANNEL << INPUT_CAPTURE_OFFSET_SHIFT )) {
            // Capture Input configuration registers
            TxInputCaptureTimer txInputCaptureTimer = (TxInputCaptureTimer)platform.getProgrammableTimers()[NUM_16B_TIMER];
            if (addr < REGISTER_CMPCTL0) {
                switch (addr) {
                    case REGISTER_TCEN + 3:
                        txInputCaptureTimer.setTcen(value); break;
                    case REGISTER_TBTRUN + 3:
                        txInputCaptureTimer.setTbtrun(value); break;
                    case REGISTER_TBTCR + 3:
                        txInputCaptureTimer.setTbtcr(value); break;
                    case REGISTER_TBTCAP + 3:
                        txInputCaptureTimer.setTbtcap(value); break;
                    case REGISTER_TBTRDCAP + 3:
                        if (logRegisterMessages) warn("Writing " + value + " to TBTRDCAP register !");
                        txInputCaptureTimer.setCurrentValue(value);
                        break;
                }
            }
            else if (addr < REGISTER_CAPCR0) {
                int compareChannel = (addr - REGISTER_CMPCTL0) >> INPUT_COMPARE_OFFSET_SHIFT;
                switch (addr - (compareChannel << INPUT_COMPARE_OFFSET_SHIFT)) {
                    case REGISTER_CMPCTL0 + 3:
                        txInputCaptureTimer.setCmpctl(compareChannel, value); break;
                    case REGISTER_TCCMP0 + 3:
                        txInputCaptureTimer.setTccmp(compareChannel, value); break;
                }
            }
            else {
                int captureChannel = (addr - REGISTER_CAPCR0) >> INPUT_CAPTURE_OFFSET_SHIFT;
                switch (addr - (captureChannel << INPUT_CAPTURE_OFFSET_SHIFT)) {
                    case REGISTER_CAPCR0 + 3:
                        txInputCaptureTimer.setCapcr(captureChannel, value); break;
                    case REGISTER_TCCAP0 + 3:
                        stop("Cannot write to TCCAP register of channel " + captureChannel);
                }
            }
        }
        else if (addr >= REGISTER_SC0EN && addr < REGISTER_SC0EN + (NUM_SERIAL_IF << SERIAL_OFFSET_SHIFT)) {
            // Serial Interface configuration registers
            int serialInterfaceNr = (addr - REGISTER_SC0EN) >> SERIAL_OFFSET_SHIFT;
            TxSerialInterface txSerialInterface = (TxSerialInterface)platform.getSerialInterfaces()[serialInterfaceNr];
            if (DEBUG_SERIAL) System.err.println("Serial #" + serialInterfaceNr  + " - Storing @0x" + Format.asHex(addr, 8) + " : 0x" + StringUtils.right(Format.asHex(value, 2),2));
            switch (addr - (serialInterfaceNr << SERIAL_OFFSET_SHIFT)) {
                case REGISTER_SC0EN + 3:
                    txSerialInterface.setEn(value); break;
                case REGISTER_SC0BUF + 3:
                    txSerialInterface.setBuf(value); break;
                case REGISTER_SC0CR + 3:
                    txSerialInterface.setCr(value); break;
                case REGISTER_SC0MOD0 + 3:
                    txSerialInterface.setMod0(value); break;
                case REGISTER_SC0MOD1 + 3:
                    txSerialInterface.setMod1(value); break;
                case REGISTER_SC0MOD2 + 3:
                    txSerialInterface.setMod2(value); break;
                case REGISTER_BR0CR + 3:
                    txSerialInterface.setBrcr(value); break;
                case REGISTER_BR0ADD + 3:
                    txSerialInterface.setBradd(value); break;
                case REGISTER_SC0RFC + 3:
                    txSerialInterface.setRfc(value); break;
                case REGISTER_SC0TFC + 3:
                    txSerialInterface.setTfc(value); break;
                case REGISTER_SC0RST + 3:
                    txSerialInterface.setRst(value); break;
                case REGISTER_SC0TST + 3:
                    txSerialInterface.setTst(value); break;
                case REGISTER_SC0FCNF + 3:
                    txSerialInterface.setFcnf(value); break;
            }
        }
        else if (addr >= REGISTER_HSC0BUF && addr < REGISTER_HSC0BUF + (NUM_HSERIAL_IF << HSERIAL_OFFSET_SHIFT)) {
            // Hi-speed Serial Interface configuration registers
            int hserialInterfaceNr = (addr - REGISTER_HSC0BUF) >> HSERIAL_OFFSET_SHIFT;
            TxSerialInterface txSerialInterface = (TxSerialInterface)platform.getSerialInterfaces()[NUM_SERIAL_IF + hserialInterfaceNr];
            if (DEBUG_SERIAL) System.err.println("Serial H#" + hserialInterfaceNr + " - Storing @0x" + Format.asHex(addr, 8) + " : 0x" + StringUtils.right(Format.asHex(value, 2),2));
            switch (addr - (hserialInterfaceNr << HSERIAL_OFFSET_SHIFT)) {
                case REGISTER_HSC0BUF:
                    txSerialInterface.setBuf(value); break;
                case REGISTER_HBR0ADD:
                    txSerialInterface.setBradd(value); break;
                case REGISTER_HSC0MOD1:
                    txSerialInterface.setMod1(value); break;
                case REGISTER_HSC0MOD2:
                    txSerialInterface.setMod2(value); break;
                case REGISTER_HSC0EN:
                    txSerialInterface.setEn(value); break;
                case REGISTER_HSC0RFC:
                    txSerialInterface.setRfc(value); break;
                case REGISTER_HSC0TFC:
                    txSerialInterface.setTfc(value); break;
                case REGISTER_HSC0RST:
                    txSerialInterface.setRst(value); break;
                case REGISTER_HSC0TST:
                    txSerialInterface.setTst(value); break;
                case REGISTER_HSC0FCNF:
                    txSerialInterface.setFcnf(value); break;
                case REGISTER_HSC0CR:
                    txSerialInterface.setCr(value); break;
                case REGISTER_HSC0MOD0:
                    txSerialInterface.setMod0(value); break;
                case REGISTER_HBR0CR:
                    txSerialInterface.setBrcr(value); break;
            }
        }
        else if (addr >= REGISTER_CCR0 && addr < REGISTER_CCR0 + (NUM_DMA_CHANNEL << DMA_CHANNEL_OFFSET_SHIFT)) {
            // DMA channel configuration registers
            int dmaChannelNr = (addr - REGISTER_CCR0) >> DMA_CHANNEL_OFFSET_SHIFT;
            TxDmaChannel channel = ((TxDmaController)platform.getDmaController()).getChannel(dmaChannelNr);
            switch (addr - (dmaChannelNr << DMA_CHANNEL_OFFSET_SHIFT)) {
                case REGISTER_CCR0:
                    channel.setCcrByte3(value); break;
                case REGISTER_CCR0 + 1:
                    channel.setCcrByte2(value); break;
                case REGISTER_CCR0 + 2:
                    channel.setCcrByte1(value); break;
                case REGISTER_CCR0 + 3:
                    channel.setCcrByte0(value); break;
                case REGISTER_CSR0 + 3:
                    channel.setCsr(value); break;
                case REGISTER_SAR0 + 3:
                    channel.setSar(value); break;
                case REGISTER_DAR0 + 3:
                    channel.setDar(value); break;
                case REGISTER_BCR0 + 3:
                    channel.setBcr(value); break;
                case REGISTER_DTCR0 + 3:
                    channel.setDtcr(value); break;

                default:
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a DMA register");
            }
        }
        else if (addr >= REGISTER_HOURR && addr < REGISTER_RESTR + 4) {
            // RTC registers
            TxRealtimeClock realtimeClock = ((TxRealtimeClock)platform.getRealtimeClock());
            switch (addr) {
                case REGISTER_HOURR + 1:
                    realtimeClock.setHourr(value); break;
                case REGISTER_MINR:
                    realtimeClock.setMinr(value); break;
                case REGISTER_SECR:
                    realtimeClock.setSecr(value); break;

                case REGISTER_YEARR:
                    realtimeClock.setYearr(value); break;
                case REGISTER_MONTHR:
                    realtimeClock.setMonthr(value); break;
                case REGISTER_DATER:
                    realtimeClock.setDater(value); break;
                case REGISTER_DAYR:
                    realtimeClock.setDayr(value); break;

                case REGISTER_PAGER + 3:
                    realtimeClock.setPager(value); break;

                case REGISTER_RESTR + 3:
                    realtimeClock.setRestr(value); break;
                default:
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a RTC register");
            }
        }
        else if (addr >= REGISTER_KWUPST00 && addr < REGISTER_KWUPINT+4) {
            // Key registers
            TxKeyCircuit keyCircuit = ((TxKeyCircuit)platform.getKeyCircuit());
            int keyNumber = (addr - REGISTER_KWUPST00) >> KEY_OFFSET_SHIFT;
            switch (addr) {
                case REGISTER_KWUPCNT + 3:
                    keyCircuit.setKWUPCNT(value); break;
                case REGISTER_KWUPCLR + 3:
                    keyCircuit.setKWUPCLR(value); break;
                default:
                    if ((addr-REGISTER_KWUPST00) == (keyNumber << KEY_OFFSET_SHIFT)) {
                        keyCircuit.keys[keyNumber].setKWUPST(value); break;
                    }
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a KEY register");
            }
        }
        else if (addr >= REGISTER_ADACLK && addr < REGISTER_ADACLK + (NUM_AD_UNIT << AD_UNIT_OFFSET_SHIFT)) {
            // AD unit configuration registers
            int adUnitNumber = (addr - REGISTER_ADACLK) >> AD_UNIT_OFFSET_SHIFT;
            TxAdUnit unit = ((TxAdConverter)platform.getAdConverter()).units[adUnitNumber];
            int shiftedAddress = addr - (adUnitNumber << AD_UNIT_OFFSET_SHIFT);
            if (shiftedAddress >= REGISTER_ADAREG0 && shiftedAddress < REGISTER_ADAREG0 + 32 ) {
                int channelNumber = (shiftedAddress - REGISTER_ADAREG0) / 4;
                if (channelNumber < unit.getNumChannels()) {
                    unit.setReg(channelNumber, value);
                }
                else {
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a A/D converter channel register");
                }
            }
            else {
                switch (shiftedAddress) {
                    case REGISTER_ADACLK + 3:
                        unit.setClk(value); break;
                    case REGISTER_ADAMOD0 + 3:
                        unit.setMod0(value); break;
                    case REGISTER_ADAMOD1 + 3:
                        unit.setMod1(value); break;
                    case REGISTER_ADAMOD2 + 3:
                        unit.setMod2(value); break;
                    case REGISTER_ADAMOD3 + 3:
                        unit.setMod3(value); break;
                    case REGISTER_ADAMOD4 + 3:
                        unit.setMod4(value); break;
                    case REGISTER_ADAMOD5 + 3:
                        unit.setMod5(value); break;
                    case REGISTER_ADAREGSP + 3:
                        unit.setRegSp(value); break;
                    case REGISTER_ADACOMREG0 + 3:
                        unit.setComReg0(value); break;
                    case REGISTER_ADACOMREG1 + 3:
                        unit.setComReg1(value); break;
                    default:
                        stop("Address 0x" + Format.asHex(addr, 8) + " is not a A/D converter register");
                }
            }
        } else if (addr >= REGISTER_IMCGA && addr < (REGISTER_IMCG11+4)) {
            // IMCG registers.
            ((TxInterruptController)platform.getInterruptController()).setImcg(addr-REGISTER_IMCGA,value);
            return;
        }
        else switch (addr) {
            // Clock generator
            case REGISTER_SYSCR:
                break; // ignore
            case REGISTER_SYSCR + 1:
                ((TxClockGenerator)platform.getClockGenerator()).setSyscr2(value); break;
            case REGISTER_SYSCR + 2:
                ((TxClockGenerator)platform.getClockGenerator()).setSyscr1(value); break;
            case REGISTER_SYSCR + 3:
                ((TxClockGenerator)platform.getClockGenerator()).setSyscr0(value); break;
            case REGISTER_OSCCR:
                break; // ignore
            case REGISTER_OSCCR + 1:
                break; // ignore
            case REGISTER_OSCCR + 2:
                ((TxClockGenerator)platform.getClockGenerator()).setOsccr1(value); break;
            case REGISTER_OSCCR + 3:
                ((TxClockGenerator)platform.getClockGenerator()).setOsccr0(value); break;
            case REGISTER_PLLSEL + 3:
                ((TxClockGenerator)platform.getClockGenerator()).setPllsel(value); break;
            case REGISTER_NMIFLG + 3:
                ((TxInterruptController)platform.getInterruptController()).setNmiFlg(value); break;
            case REGISTER_RSTFLG + 3:
                ((TxClockGenerator)platform.getClockGenerator()).setRstflg(value); break;
            // Interrupt Controller
            case REGISTER_INTCLR + 3:
                stop("The INTCLR register can not be accessed by 8-bit");
            case REGISTER_DREQFLG + 3:
                ((TxInterruptController)platform.getInterruptController()).setDreqflg(value); break;
            // DMA controller
            case REGISTER_DCR + 3:
                ((TxDmaController)platform.getDmaController()).setDcr(value); break;
            case REGISTER_RSR + 3:
                ((TxDmaController)platform.getDmaController()).setRsr(value); break;
            case REGISTER_DHR + 3:
                ((TxDmaController)platform.getDmaController()).setDhr(value); break;
            default:
                if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr, 8) + ": Store8 value 0x" + Format.asHex(value, 2) + " is not supported yet");
        }
    }

    public void onStore16(byte[] ioPage, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_IMC00 && addr < (REGISTER_IMC19+4)) {
            // IMC registers.
            TxInterruptController intc = (TxInterruptController)platform.getInterruptController();
            
            intc.setImc(addr-REGISTER_IMC00,(value>>8)&0xFF);
            intc.setImc(addr-REGISTER_IMC00+1,value&0xFF);
            return;
        }
        if (addr >= REGISTER_PORT0 && addr < REGISTER_PORT0 + (NUM_PORT << PORT_OFFSET_SHIFT)) {
            // Port configuration registers
            stop("The I/O port registers cannot be accessed by 16-bit for now");
        }
        else if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + (NUM_16B_TIMER << TIMER_OFFSET_SHIFT)) {
            // Timer configuration registers
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_SHIFT;
            TxTimer txTimer = (TxTimer)platform.getProgrammableTimers()[timerNr];
            switch (addr - (timerNr << TIMER_OFFSET_SHIFT)) {
                case REGISTER_TB0EN + 2:
                    txTimer.setTben(value); break;
                case REGISTER_TB0RUN + 2:
                    txTimer.setTbrun(value); break;
                case REGISTER_TB0CR + 2:
                    txTimer.setTbcr(value); break;
                case REGISTER_TB0MOD + 2:
                    txTimer.setTbmod(value); break;
                case REGISTER_TB0FFCR + 2:
                    txTimer.setTbffcr(value); break;
                case REGISTER_TB0ST + 2:
                    txTimer.setTbst(value); break;
                case REGISTER_TB0IM + 2:
                    txTimer.setTbim(value); break;
                case REGISTER_TB0UC + 2:
                    txTimer.setTbuc(value); break;
                case REGISTER_TB0RG0 + 2:
                    txTimer.setTbrg0(value); break;
                case REGISTER_TB0RG1 + 2:
                    txTimer.setTbrg1(value); break;
                case REGISTER_TB0CP0 + 2:
                    txTimer.setTbcp0(value); break;
                case REGISTER_TB0CP1 + 2:
                    txTimer.setTbcp1(value); break;
            }
        }
        else if (addr >= REGISTER_TCEN && addr < REGISTER_CAPCR0 + (NUM_CAPTURE_CHANNEL << INPUT_CAPTURE_OFFSET_SHIFT )) {
            // Capture Input configuration registers
            TxInputCaptureTimer txInputCaptureTimer = (TxInputCaptureTimer)platform.getProgrammableTimers()[NUM_16B_TIMER];
            if (addr < REGISTER_CMPCTL0) {
                switch (addr) {
                    case REGISTER_TCEN + 2:
                        txInputCaptureTimer.setTcen(value); break;
                    case REGISTER_TBTRUN + 2:
                        txInputCaptureTimer.setTbtrun(value); break;
                    case REGISTER_TBTCR + 2:
                        txInputCaptureTimer.setTbtcr(value); break;
                    case REGISTER_TBTCAP + 2:
                        txInputCaptureTimer.setTbtcap(value); break;
                    case REGISTER_TBTRDCAP + 2:
                        if (logRegisterMessages) warn("Writing " + value + " to TBTRDCAP register !");
                        txInputCaptureTimer.setCurrentValue(value);
                        break;
                }
            }
            else if (addr < REGISTER_CAPCR0) {
                int compareChannel = (addr - REGISTER_CMPCTL0) >> INPUT_COMPARE_OFFSET_SHIFT;
                switch (addr - (compareChannel << INPUT_COMPARE_OFFSET_SHIFT)) {
                    case REGISTER_CMPCTL0 + 2:
                        txInputCaptureTimer.setCmpctl(compareChannel, value); break;
                    case REGISTER_TCCMP0 + 2:
                        txInputCaptureTimer.setTccmp(compareChannel, value); break;
                }

            }
            else {
                int captureChannel = (addr - REGISTER_CAPCR0) >> INPUT_CAPTURE_OFFSET_SHIFT;
                switch (addr - (captureChannel << INPUT_CAPTURE_OFFSET_SHIFT)) {
                    case REGISTER_CAPCR0 + 2:
                        txInputCaptureTimer.setCapcr(captureChannel, value); break;
                    case REGISTER_TCCAP0 + 2:
                        stop("Cannot write to TCCAP register of channel " + captureChannel);
                }
            }
        }
        else if (addr >= REGISTER_HOURR && addr < REGISTER_RESTR + 4) {
            stop("The RTC registers cannot be written by 16-bit for now");
        }
        else if (addr >= REGISTER_KWUPST00 && addr < REGISTER_KWUPINT + 4) {
            stop("The KEY registers cannot be accessed by 16-bit for now");
        }
        else if (addr >= REGISTER_SC0EN && addr < REGISTER_SC0EN + (NUM_SERIAL_IF << SERIAL_OFFSET_SHIFT)) {
            // Serial Interface configuration registers
            int serialInterfaceNr = (addr - REGISTER_SC0EN) >> SERIAL_OFFSET_SHIFT;
            TxSerialInterface txSerialInterface = (TxSerialInterface)platform.getSerialInterfaces()[serialInterfaceNr];
            if (DEBUG_SERIAL) System.err.println("Serial #" + serialInterfaceNr  + " - Storing @0x" + Format.asHex(addr, 8) + " : 0x" + StringUtils.right(Format.asHex(value, 4),4));
            switch (addr - (serialInterfaceNr << SERIAL_OFFSET_SHIFT)) {
                case REGISTER_SC0EN + 2:
                    txSerialInterface.setEn(value); break;
                case REGISTER_SC0BUF + 2:
                    txSerialInterface.setBuf(value); break;
                case REGISTER_SC0CR + 2:
                    txSerialInterface.setCr(value); break;
                case REGISTER_SC0MOD0 + 2:
                    txSerialInterface.setMod0(value); break;
                case REGISTER_SC0MOD1 + 2:
                    txSerialInterface.setMod1(value); break;
                case REGISTER_SC0MOD2 + 2:
                    txSerialInterface.setMod2(value); break;
                case REGISTER_BR0CR + 2:
                    txSerialInterface.setBrcr(value); break;
                case REGISTER_BR0ADD + 2:
                    txSerialInterface.setBradd(value); break;
                case REGISTER_SC0RFC + 2:
                    txSerialInterface.setRfc(value); break;
                case REGISTER_SC0TFC + 2:
                    txSerialInterface.setTfc(value); break;
                case REGISTER_SC0RST + 2:
                    txSerialInterface.setRst(value); break;
                case REGISTER_SC0TST + 2:
                    txSerialInterface.setTst(value); break;
                case REGISTER_SC0FCNF + 2:
                    txSerialInterface.setFcnf(value); break;
            }
        }
        else if (addr >= REGISTER_HSC0BUF && addr < REGISTER_HSC0BUF + (NUM_HSERIAL_IF << HSERIAL_OFFSET_SHIFT)) {
            // Hi-speed Serial Interface configuration registers
            stop("Serial register 0x" + Format.asHex(addr, 8) + " can only be written by 8 bits");
        }
        else if (addr >= REGISTER_IMCGA && addr < (REGISTER_IMCG11+4)) {
            // IMCG registers.
            TxInterruptController intc = (TxInterruptController)platform.getInterruptController();
            
            intc.setImcg(addr-REGISTER_IMCGA,(value>>8)&0xFF);
            intc.setImcg(addr-REGISTER_IMCGA+1,value&0xFF);
            return;
        }
        else switch (addr){
            // Clock generator
            case REGISTER_SYSCR:
            case REGISTER_SYSCR + 2:
                stop("The SYSCR register can not be accessed by 16-bit for now");
            case REGISTER_OSCCR:
            case REGISTER_OSCCR + 2:
                stop("The OSCCR register can not be accessed by 16-bit for now");
            case REGISTER_PLLSEL + 2:
                ((TxClockGenerator)platform.getClockGenerator()).setPllsel(value); break;
            case REGISTER_NMIFLG + 2:
                ((TxInterruptController)platform.getInterruptController()).setNmiFlg(value); break;
            case REGISTER_RSTFLG + 2:
                ((TxClockGenerator)platform.getClockGenerator()).setRstflg(value); break;
            // Interrupt Controller
            case REGISTER_INTCLR + 2:
                ((TxInterruptController)platform.getInterruptController()).setIntClr(value); break;
            case REGISTER_DREQFLG + 2:
                ((TxInterruptController)platform.getInterruptController()).setDreqflg(value); break;
            default:
                if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr, 8) + ": Store16 value 0x" + Format.asHex(value, 4) + " is not supported yet");
        }
    }

    public void onStore32(byte[] ioPage, int addr, int value, DebuggableMemory.AccessSource accessSource) {
        if (addr >= REGISTER_IMC00 && addr < (REGISTER_IMC19+4)) {
            // IMC registers.
            TxInterruptController intc = (TxInterruptController)platform.getInterruptController();
            
            intc.setImc(addr-REGISTER_IMC00,(value>>24)&0xFF);
            intc.setImc(addr-REGISTER_IMC00+1,(value>>16)&0xFF);
            intc.setImc(addr-REGISTER_IMC00+2,(value>>8)&0xFF);
            intc.setImc(addr-REGISTER_IMC00+3,value&0xFF);
            return;
        }
        if (addr >= REGISTER_PORT0 && addr < REGISTER_PORT0 + (NUM_PORT << PORT_OFFSET_SHIFT)) {
            // Port configuration registers
            int portNr = (addr - REGISTER_PORT0) >> PORT_OFFSET_SHIFT;
            TxIoPort txIoPort = (TxIoPort) platform.getIoPorts()[portNr];
            switch (addr - (portNr << PORT_OFFSET_SHIFT)) {
                case REGISTER_PORT0:
                    txIoPort.setValue((byte) value); break;
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
        else if (addr >= REGISTER_TB0EN && addr < REGISTER_TB0EN + (NUM_16B_TIMER << TIMER_OFFSET_SHIFT)) {
            // Timer configuration registers
            int timerNr = (addr - REGISTER_TB0EN) >> TIMER_OFFSET_SHIFT;
            TxTimer txTimer = (TxTimer)platform.getProgrammableTimers()[timerNr];
            switch (addr - (timerNr << TIMER_OFFSET_SHIFT)) {
                case REGISTER_TB0EN:
                    txTimer.setTben(value); break;
                case REGISTER_TB0RUN:
                    txTimer.setTbrun(value); break;
                case REGISTER_TB0CR:
                    txTimer.setTbcr(value); break;
                case REGISTER_TB0MOD:
                    txTimer.setTbmod(value); break;
                case REGISTER_TB0FFCR:
                    txTimer.setTbffcr(value); break;
                case REGISTER_TB0ST:
                    txTimer.setTbst(value); break;
                case REGISTER_TB0IM:
                    txTimer.setTbim(value); break;
                case REGISTER_TB0UC:
                    txTimer.setTbuc(value); break;
                case REGISTER_TB0RG0:
                    txTimer.setTbrg0(value); break;
                case REGISTER_TB0RG1:
                    txTimer.setTbrg1(value); break;
                case REGISTER_TB0CP0:
                    txTimer.setTbcp0(value); break;
                case REGISTER_TB0CP1:
                    txTimer.setTbcp1(value); break;
            }
        }
        else if (addr >= REGISTER_TCEN && addr < REGISTER_CAPCR0 + (NUM_CAPTURE_CHANNEL << INPUT_CAPTURE_OFFSET_SHIFT )) {
            // Capture Input configuration registers
            TxInputCaptureTimer txInputCaptureTimer = (TxInputCaptureTimer)platform.getProgrammableTimers()[NUM_16B_TIMER];
            if (addr < REGISTER_CMPCTL0) {
                switch (addr) {
                    case REGISTER_TCEN:
                        txInputCaptureTimer.setTcen(value); break;
                    case REGISTER_TBTRUN:
                        txInputCaptureTimer.setTbtrun(value); break;
                    case REGISTER_TBTCR:
                        txInputCaptureTimer.setTbtcr(value); break;
                    case REGISTER_TBTCAP:
                        txInputCaptureTimer.setTbtcap(value); break;
                    case REGISTER_TBTRDCAP:
                        if (logRegisterMessages) warn("Writing " + value + " to TBTRDCAP register !");
                        txInputCaptureTimer.setCurrentValue(value);
                        break;
                }
            }
            else if (addr < REGISTER_CAPCR0) {
                int compareChannel = (addr - REGISTER_CMPCTL0) >> INPUT_COMPARE_OFFSET_SHIFT;
                switch (addr - (compareChannel << INPUT_COMPARE_OFFSET_SHIFT)) {
                    case REGISTER_CMPCTL0:
                        txInputCaptureTimer.setCmpctl(compareChannel, value); break;
                    case REGISTER_TCCMP0:
                        txInputCaptureTimer.setTccmp(compareChannel, value); break;
                }

            }
            else {
                int captureChannel = (addr - REGISTER_CAPCR0) >> INPUT_CAPTURE_OFFSET_SHIFT;
                switch (addr - (captureChannel << INPUT_CAPTURE_OFFSET_SHIFT)) {
                    case REGISTER_CAPCR0:
                        txInputCaptureTimer.setCapcr(captureChannel, value); break;
                    case REGISTER_TCCAP0:
                        stop("Cannot write to TCCAP register of channel " + captureChannel);
                }
            }
        }
        else if (addr >= REGISTER_SC0EN && addr < REGISTER_SC0EN + (NUM_SERIAL_IF << SERIAL_OFFSET_SHIFT)) {
            // Serial Interface configuration registers
            int serialInterfaceNr = (addr - REGISTER_SC0EN) >> SERIAL_OFFSET_SHIFT;
            TxSerialInterface txSerialInterface = (TxSerialInterface)platform.getSerialInterfaces()[serialInterfaceNr];
            if (DEBUG_SERIAL) System.err.println("Serial #" + serialInterfaceNr  + " - Storing @0x" + Format.asHex(addr, 8) + " : 0x" + Format.asHex(value, 8));
            switch (addr - (serialInterfaceNr << SERIAL_OFFSET_SHIFT)) {
                case REGISTER_SC0EN:
                    txSerialInterface.setEn(value); break;
                case REGISTER_SC0BUF:
                    txSerialInterface.setBuf(value); break;
                case REGISTER_SC0CR:
                    txSerialInterface.setCr(value); break;
                case REGISTER_SC0MOD0:
                    txSerialInterface.setMod0(value); break;
                case REGISTER_SC0MOD1:
                    txSerialInterface.setMod1(value); break;
                case REGISTER_SC0MOD2:
                    txSerialInterface.setMod2(value); break;
                case REGISTER_BR0CR:
                    txSerialInterface.setBrcr(value); break;
                case REGISTER_BR0ADD:
                    txSerialInterface.setBradd(value); break;
                case REGISTER_SC0RFC:
                    txSerialInterface.setRfc(value); break;
                case REGISTER_SC0TFC:
                    txSerialInterface.setTfc(value); break;
                case REGISTER_SC0RST:
                    txSerialInterface.setRst(value); break;
                case REGISTER_SC0TST:
                    txSerialInterface.setTst(value); break;
                case REGISTER_SC0FCNF:
                    txSerialInterface.setFcnf(value); break;
            }
        }
        else if (addr >= REGISTER_HSC0BUF && addr < REGISTER_HSC0BUF + (NUM_HSERIAL_IF << HSERIAL_OFFSET_SHIFT)) {
            // Hi-speed Serial Interface configuration registers
            stop("Serial register 0x" + Format.asHex(addr, 8) + " can only be written by 8 bits");
        }
        else if (addr >= REGISTER_CCR0 && addr < REGISTER_CCR0 + (NUM_DMA_CHANNEL << DMA_CHANNEL_OFFSET_SHIFT)) {
            // DMA channel configuration registers
            int dmaChannelNr = (addr - REGISTER_CCR0) >> DMA_CHANNEL_OFFSET_SHIFT;
            TxDmaChannel channel = ((TxDmaController)platform.getDmaController()).getChannel(dmaChannelNr);
            switch (addr - (dmaChannelNr << DMA_CHANNEL_OFFSET_SHIFT)) {
                case REGISTER_CCR0:
                    channel.setCcr(value); break;
                case REGISTER_CSR0:
                    channel.setCsr(value); break;
                case REGISTER_SAR0:
                    channel.setSar(value); break;
                case REGISTER_DAR0:
                    channel.setDar(value); break;
                case REGISTER_BCR0:
                    channel.setBcr(value); break;
                case REGISTER_DTCR0:
                    channel.setDtcr(value); break;
                default:
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a DMA register");
            }
        }
        else if (addr >= REGISTER_HOURR && addr < REGISTER_RESTR + 4) {
            // RTC registers
            TxRealtimeClock realtimeClock = ((TxRealtimeClock)platform.getRealtimeClock());
            switch (addr) {
                case REGISTER_HOURR:
                    realtimeClock.setTimeReg32(value); break;
                case REGISTER_YEARR:
                    realtimeClock.setDateReg32(value); break;
                case REGISTER_PAGER:
                    realtimeClock.setPager(value); break;
                case REGISTER_RESTR:
                    realtimeClock.setRestr(value); break;
                default:
                    stop("Address " + Format.asHex(addr, 8) + " is not a RTC register");
            }
        }
        else if (addr >= REGISTER_KWUPST00 && addr < REGISTER_KWUPINT + 4) {
            // Key registers
            TxKeyCircuit keyCircuit = ((TxKeyCircuit)platform.getKeyCircuit());
            int keyNumber = (addr - REGISTER_KWUPST00) >> KEY_OFFSET_SHIFT;
            switch (addr) {
                case REGISTER_KWUPCNT:
                    keyCircuit.setKWUPCNT(value); break;
                case REGISTER_KWUPCLR:
                    keyCircuit.setKWUPCLR(value); break;
                default:
                    if ((addr-REGISTER_KWUPST00) == (keyNumber << KEY_OFFSET_SHIFT)) {
                        keyCircuit.keys[keyNumber].setKWUPST(value); break;
                    }
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a KEY register");
            }
        }
        else if (addr >= REGISTER_ADACLK && addr < REGISTER_ADACLK + (NUM_AD_UNIT << AD_UNIT_OFFSET_SHIFT)) {
            // AD unit configuration registers
            int adUnitNumber = (addr - REGISTER_ADACLK) >> AD_UNIT_OFFSET_SHIFT;
            TxAdUnit unit = ((TxAdConverter)platform.getAdConverter()).units[adUnitNumber];
            int shiftedAddress = addr - (adUnitNumber << AD_UNIT_OFFSET_SHIFT);
            if (shiftedAddress >= REGISTER_ADAREG0 && shiftedAddress < REGISTER_ADAREG0 + 32 ) {
                int channelNumber = (shiftedAddress - REGISTER_ADAREG0) / 4;
                if (channelNumber < unit.getNumChannels()) {
                    unit.setReg(channelNumber, value);
                }
                else {
                    stop("Address 0x" + Format.asHex(addr, 8) + " is not a A/D converter channel register");
                }
            }
            else {
                switch (shiftedAddress) {
                    case REGISTER_ADACLK:
                        unit.setClk(value); break;
                    case REGISTER_ADAMOD0:
                        unit.setMod0(value); break;
                    case REGISTER_ADAMOD1:
                        unit.setMod1(value); break;
                    case REGISTER_ADAMOD2:
                        unit.setMod2(value); break;
                    case REGISTER_ADAMOD3:
                        unit.setMod3(value); break;
                    case REGISTER_ADAMOD4:
                        unit.setMod4(value); break;
                    case REGISTER_ADAMOD5:
                        unit.setMod5(value); break;
                    case REGISTER_ADAREGSP:
                        unit.setRegSp(value); break;
                    case REGISTER_ADACOMREG0:
                        unit.setComReg0(value); break;
                    case REGISTER_ADACOMREG1:
                        unit.setComReg1(value); break;
                    default:
                        stop("Address 0x" + Format.asHex(addr, 8) + " is not a A/D converter register");
                }
            }
        } else if (addr >= REGISTER_IMCGA && addr < (REGISTER_IMCG11+4)) {
            // IMCG registers.
            TxInterruptController intc = (TxInterruptController)platform.getInterruptController();
            
            intc.setImcg(addr-REGISTER_IMCGA,(value>>24)&0xFF);
            intc.setImcg(addr-REGISTER_IMCGA+1,(value>>16)&0xFF);
            intc.setImcg(addr-REGISTER_IMCGA+2,(value>>8)&0xFF);
            intc.setImcg(addr-REGISTER_IMCGA+3,value&0xFF);
            return;
        }
        else switch(addr) {
            // Clock generator
            case REGISTER_SYSCR:
                ((TxClockGenerator)platform.getClockGenerator()).setSyscr(value); break;
            case REGISTER_OSCCR:
                ((TxClockGenerator)platform.getClockGenerator()).setOsccr(value); break;
            case REGISTER_NMIFLG:
                ((TxInterruptController)platform.getInterruptController()).setNmiFlg(value); break;
            case REGISTER_RSTFLG:
                ((TxClockGenerator)platform.getClockGenerator()).setRstflg(value); break;
            case REGISTER_PLLSEL:
                ((TxClockGenerator)platform.getClockGenerator()).setPllsel(value); break;
            // Interrupt Controller
            case REGISTER_ILEV:
                ((TxInterruptController)platform.getInterruptController()).setIlev(value); break;
            case REGISTER_IVR:
                ((TxInterruptController)platform.getInterruptController()).setIvr31_9(value); break;
            case REGISTER_INTCLR:
                ((TxInterruptController)platform.getInterruptController()).setIntClr(value); break;
            case REGISTER_DREQFLG:
                ((TxInterruptController)platform.getInterruptController()).setDreqflg(value); break;
            // DMA controller
            case REGISTER_DCR:
                ((TxDmaController)platform.getDmaController()).setDcr(value); break;
            case REGISTER_RSR:
                ((TxDmaController)platform.getDmaController()).setRsr(value); break;
            case REGISTER_DHR:
                ((TxDmaController)platform.getDmaController()).setDhr(value); break;
            default:
                // TODO if one interrupt has its active state set to "L", this should trigger a hardware interrupt
                // See section 6.5.1.2 , 3rd bullet
                if (logRegisterMessages) warn("Register 0x" + Format.asHex(addr, 8) + ": Store32 value 0x" + Format.asHex(value, 8) + " is not supported yet");
        }
    }
}
