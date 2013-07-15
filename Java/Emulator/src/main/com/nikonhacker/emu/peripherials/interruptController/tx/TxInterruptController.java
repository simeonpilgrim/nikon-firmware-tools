package com.nikonhacker.emu.peripherials.interruptController.tx;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.Instruction;
import com.nikonhacker.disassembly.StatementContext;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.interrupt.tx.TxInterruptRequest;
import com.nikonhacker.emu.interrupt.tx.Type;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.dmaController.tx.TxDmaController;
import com.nikonhacker.emu.peripherials.interruptController.AbstractInterruptController;

import java.util.Collections;

/**
 * This is based on the Toshiba hardware specification for TMP19A44FDA/FE/F10XBG
 * Available at http://www.semicon.toshiba.co.jp/info/docget.jsp?type=datasheet&lang=en&pid=TMP19A44FEXBG
 */
public class TxInterruptController extends AbstractInterruptController {

    private static final int ADDRESS_INTERRUPT_BEV0_IV0 = 0x8000_0180;
    private static final int ADDRESS_INTERRUPT_BEV0_IV1 = 0x8000_0200;
    private static final int ADDRESS_INTERRUPT_BEV1_IV0 = 0xBFC0_0380;
    private static final int ADDRESS_INTERRUPT_BEV1_IV1 = 0xBFC0_0400;

    private static final int NULL_SECTION  = -1;
    private static final int NULL_REGISTER = -1;

    public static final  int ACTIVE_LOW     = 0b00;
    public static final  int ACTIVE_HIGH    = 0b01;
    public static final  int ACTIVE_FALLING = 0b10;
    public static final  int ACTIVE_RISING  = 0b00;

    // Register fields
    // Ilev
    private int ilev;
    private int ivr;
    private int intClr  = 0x0;
    private int dreqflg = 0x000000FF;

    private final static int Ilev_Mlev_pos   = 31;
    private final static int Ilev_Cmask_mask = 0b00000000_00000000_00000000_00000111;


    public final static  InterruptDescription[] hardwareInterruptDescription = new InterruptDescription[128];

    private static final int NONE = 0;
    private static final int SW   = 1;

    public static final int INT0     = 2;
    public static final int INT1     = 3;
    public static final int INT2     = 4;
    public static final int INT3     = 5;
    public static final int INT4     = 6;
    public static final int INT5     = 7;
    public static final int INT6     = 8;
    public static final int INT7     = 9;
    public static final int INT8     = 10;
    public static final int INT9     = 11;
    public static final int INTA     = 12;
    public static final int INTB     = 13;
    public static final int INTC     = 14;
    public static final int INTD     = 15;
    public static final int INTE     = 16;
    public static final int INTF     = 17;
    public static final int KWUP     = 18;
    public static final int INT10    = 19;
    public static final int INT11    = 20;
    public static final int INT12    = 21;
    public static final int INT13    = 22;
    public static final int INT14    = 23;
    public static final int INT15    = 24;
    public static final int INT16    = 25;
    public static final int INT17    = 26;
    public static final int INT18    = 27;
    public static final int INT19    = 28;
    public static final int INT1A    = 29;
    public static final int INT1B    = 30;
    public static final int INT1C    = 31;
    public static final int INT1D    = 32;
    public static final int INT1E    = 33;
    public static final int INT1F    = 34;
    public static final int INTRX0   = 35;
    public static final int INTTX0   = 36;
    public static final int INTRX1   = 37;
    public static final int INTTX1   = 38;
    public static final int INTRX2   = 39;
    public static final int INTTX2   = 40;
    public static final int HINTRX0  = 41;
    public static final int HINTTX0  = 42;
    public static final int HINTRX1  = 43;
    public static final int HINTTX1  = 44;
    public static final int HINTRX2  = 45;
    public static final int HINTTX2  = 46;
    public static final int INTSBI0  = 47;
    public static final int INTADHPA = 48;
    public static final int INTADMA  = 49;
    public static final int INTADHPB = 50;
    public static final int INTADMB  = 51;
    public static final int INTADHPC = 52;
    public static final int INTADMC  = 53;
    public static final int INTTB0   = 54;
    public static final int INTTB1   = 55;
    public static final int INTTB2   = 56;
    public static final int INTTB3   = 57;
    public static final int INTTB4   = 58;
    public static final int INTTB5   = 59;
    public static final int INTTB6   = 60;
    public static final int INTTB7   = 61;
    public static final int INTTB8   = 62;
    public static final int INTTB9   = 63;
    public static final int INTTBA   = 64;
    public static final int INTTBB   = 65;
    public static final int INTTBC   = 66;
    public static final int INTTBD   = 67;
    public static final int INTTBE   = 68;
    public static final int INTTBF   = 69;
    public static final int INTADA   = 70;
    public static final int INTADB   = 71;
    public static final int INTADC   = 72;
    public static final int INTTB10  = 73;
    public static final int INTTB11  = 74;
    public static final int PHCNT0   = 75;
    public static final int PHCNT1   = 76;
    public static final int PHCNT2   = 77;
    public static final int PHCNT3   = 78;
    public static final int PHCNT4   = 79;
    public static final int PHCNT5   = 80;
    public static final int INTCAP0  = 81;
    public static final int INTCAP1  = 82;
    public static final int INTCAP2  = 83;
    public static final int INTCAP3  = 84;
    public static final int INTCMP0  = 85;
    public static final int INTCMP1  = 86;
    public static final int INTCMP2  = 87;
    public static final int INTCMP3  = 88;
    public static final int INTCMP4  = 89;
    public static final int INTCMP5  = 90;
    public static final int INTCMP6  = 91;
    public static final int INTCMP7  = 92;
    public static final int INTTBT   = 93;
    public static final int INTRTC   = 94;
    public static final int INTDMA0  = 95;
    public static final int INTDMA1  = 96;
    public static final int INTDMA2  = 97;
    public static final int INTDMA3  = 98;
    public static final int INTDMA4  = 99;
    public static final int INTDMA5  = 100;
    public static final int INTDMA6  = 101;
    public static final int INTDMA7  = 102;
    public static final int SOFT     = 103;

    // This is a transcript of table 6.5, section 6.5.1.5 of the Toshiba hardware specification
    static {
        hardwareInterruptDescription[NONE] = new InterruptDescription(null, "(No interrupt factor)", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[SW] = new InterruptDescription("SW", "Software interrupt", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INT0] = new InterruptDescription("INT0", "", TxIoListener.REGISTER_IMC00, 2, TxIoListener.REGISTER_IMCGA, 0);
        hardwareInterruptDescription[INT1] = new InterruptDescription("INT1", "", TxIoListener.REGISTER_IMC00, 3, TxIoListener.REGISTER_IMCGA, 1);
        hardwareInterruptDescription[INT2] = new InterruptDescription("INT2", "", TxIoListener.REGISTER_IMC01, 0, TxIoListener.REGISTER_IMCGA, 2);
        hardwareInterruptDescription[INT3] = new InterruptDescription("INT3", "", TxIoListener.REGISTER_IMC01, 1, TxIoListener.REGISTER_IMCGA, 3);
        hardwareInterruptDescription[INT4] = new InterruptDescription("INT4", "", TxIoListener.REGISTER_IMC01, 2, TxIoListener.REGISTER_IMCGB, 0);
        hardwareInterruptDescription[INT5] = new InterruptDescription("INT5", "", TxIoListener.REGISTER_IMC01, 3, TxIoListener.REGISTER_IMCGB, 1);
        hardwareInterruptDescription[INT6] = new InterruptDescription("INT6", "", TxIoListener.REGISTER_IMC02, 0, TxIoListener.REGISTER_IMCGB, 2);
        hardwareInterruptDescription[INT7] = new InterruptDescription("INT7", "", TxIoListener.REGISTER_IMC02, 1, TxIoListener.REGISTER_IMCGB, 3);
        hardwareInterruptDescription[INT8] = new InterruptDescription("INT8", "", TxIoListener.REGISTER_IMC02, 2, TxIoListener.REGISTER_IMCGC, 0);
        hardwareInterruptDescription[INT9] = new InterruptDescription("INT9", "", TxIoListener.REGISTER_IMC02, 3, TxIoListener.REGISTER_IMCGC, 1);
        hardwareInterruptDescription[INTA] = new InterruptDescription("INTA", "", TxIoListener.REGISTER_IMC03, 0, TxIoListener.REGISTER_IMCGC, 2);
        hardwareInterruptDescription[INTB] = new InterruptDescription("INTB", "", TxIoListener.REGISTER_IMC03, 1, TxIoListener.REGISTER_IMCGC, 3);
        hardwareInterruptDescription[INTC] = new InterruptDescription("INTC", "", TxIoListener.REGISTER_IMC03, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTD] = new InterruptDescription("INTD", "", TxIoListener.REGISTER_IMC03, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTE] = new InterruptDescription("INTE", "", TxIoListener.REGISTER_IMC04, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTF] = new InterruptDescription("INTF", "", TxIoListener.REGISTER_IMC04, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[KWUP] = new InterruptDescription("KWUP", "Key-on wake-up", TxIoListener.REGISTER_IMC04, 2, TxIoListener.REGISTER_IMCGD, 0);
        hardwareInterruptDescription[INT10] = new InterruptDescription("INT10", "", TxIoListener.REGISTER_IMC04, 3, TxIoListener.REGISTER_IMCGF, 0);
        hardwareInterruptDescription[INT11] = new InterruptDescription("INT11", "", TxIoListener.REGISTER_IMC05, 0, TxIoListener.REGISTER_IMCGF, 1);
        hardwareInterruptDescription[INT12] = new InterruptDescription("INT12", "", TxIoListener.REGISTER_IMC05, 1, TxIoListener.REGISTER_IMCGF, 2);
        hardwareInterruptDescription[INT13] = new InterruptDescription("INT13", "", TxIoListener.REGISTER_IMC05, 2, TxIoListener.REGISTER_IMCGF, 3);
        hardwareInterruptDescription[INT14] = new InterruptDescription("INT14", "", TxIoListener.REGISTER_IMC05, 3, TxIoListener.REGISTER_IMCG10, 0);
        hardwareInterruptDescription[INT15] = new InterruptDescription("INT15", "", TxIoListener.REGISTER_IMC06, 0, TxIoListener.REGISTER_IMCG10, 1);
        hardwareInterruptDescription[INT16] = new InterruptDescription("INT16", "", TxIoListener.REGISTER_IMC06, 1, TxIoListener.REGISTER_IMCG10, 2);
        hardwareInterruptDescription[INT17] = new InterruptDescription("INT17", "", TxIoListener.REGISTER_IMC06, 2, TxIoListener.REGISTER_IMCG10, 3);
        hardwareInterruptDescription[INT18] = new InterruptDescription("INT18", "", TxIoListener.REGISTER_IMC06, 3, TxIoListener.REGISTER_IMCG11, 0);
        hardwareInterruptDescription[INT19] = new InterruptDescription("INT19", "", TxIoListener.REGISTER_IMC07, 0, TxIoListener.REGISTER_IMCG11, 1);
        hardwareInterruptDescription[INT1A] = new InterruptDescription("INT1A", "", TxIoListener.REGISTER_IMC07, 1, TxIoListener.REGISTER_IMCG11, 2);
        hardwareInterruptDescription[INT1B] = new InterruptDescription("INT1B", "", TxIoListener.REGISTER_IMC07, 2, TxIoListener.REGISTER_IMCG11, 3);
        hardwareInterruptDescription[INT1C] = new InterruptDescription("INT1C", "", TxIoListener.REGISTER_IMC07, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INT1D] = new InterruptDescription("INT1D", "", TxIoListener.REGISTER_IMC08, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INT1E] = new InterruptDescription("INT1E", "", TxIoListener.REGISTER_IMC08, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INT1F] = new InterruptDescription("INT1F", "", TxIoListener.REGISTER_IMC08, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTRX0] = new InterruptDescription("INTRX0", "Serial reception (channel.0)", TxIoListener.REGISTER_IMC08, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTX0] = new InterruptDescription("INTTX0", "Serial transmission (channel.0)", TxIoListener.REGISTER_IMC09, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTRX1] = new InterruptDescription("INTRX1", "Serial reception (channel.1)", TxIoListener.REGISTER_IMC09, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTX1] = new InterruptDescription("INTTX1", "Serial transmission (channel.1)", TxIoListener.REGISTER_IMC09, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTRX2] = new InterruptDescription("INTRX2", "Serial reception (channel.2)", TxIoListener.REGISTER_IMC09, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTX2] = new InterruptDescription("INTTX2", "Serial transmission (channel.2)", TxIoListener.REGISTER_IMC0A, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[HINTRX0] = new InterruptDescription("HINTRX0", "High-speed serial reception (Hchannel.0)", TxIoListener.REGISTER_IMC0A, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[HINTTX0] = new InterruptDescription("HINTTX0", "High-speed serial transmission (Hchannel.0)", TxIoListener.REGISTER_IMC0A, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[HINTRX1] = new InterruptDescription("HINTRX1", "High-speed serial reception (Hchannel.1)", TxIoListener.REGISTER_IMC0A, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[HINTTX1] = new InterruptDescription("HINTTX1", "High-speed serial transmission (Hchannel.1)", TxIoListener.REGISTER_IMC0B, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[HINTRX2] = new InterruptDescription("HINTRX2", "High-speed serial reception (Hchannel.2)", TxIoListener.REGISTER_IMC0B, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[HINTTX2] = new InterruptDescription("HINTTX2", "High-speed serial transmission (Hchannel.2)", TxIoListener.REGISTER_IMC0B, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTSBI0] = new InterruptDescription("INTSBI0", "Serial bus interface 0 0x0BC", TxIoListener.REGISTER_IMC0B, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTADHPA] = new InterruptDescription("INTADHPA", "Highest priority AD conversion complete interrupt A", TxIoListener.REGISTER_IMC0C, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTADMA] = new InterruptDescription("INTADMA", "AD conversion monitoring function interrupt A", TxIoListener.REGISTER_IMC0C, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTADHPB] = new InterruptDescription("INTADHPB", "Highest priority AD conversion complete interrupt B", TxIoListener.REGISTER_IMC0C, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTADMB] = new InterruptDescription("INTADMB", "AD conversion monitoring function interrupt B", TxIoListener.REGISTER_IMC0C, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTADHPC] = new InterruptDescription("INTADHPC", "Highest priority AD conversion complete interrupt C", TxIoListener.REGISTER_IMC0D, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTADMC] = new InterruptDescription("INTADMC", "AD conversion monitoring function interrupt C", TxIoListener.REGISTER_IMC0D, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTB0] = new InterruptDescription("INTTB0", "16bitTMRB 0", TxIoListener.REGISTER_IMC0D, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTB1] = new InterruptDescription("INTTB1", "16bitTMRB 1", TxIoListener.REGISTER_IMC0D, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTB2] = new InterruptDescription("INTTB2", "16bitTMRB 2", TxIoListener.REGISTER_IMC0E, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTB3] = new InterruptDescription("INTTB3", "16bitTMRB 3", TxIoListener.REGISTER_IMC0E, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTB4] = new InterruptDescription("INTTB4", "16bitTMRB 4", TxIoListener.REGISTER_IMC0E, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTB5] = new InterruptDescription("INTTB5", "16bitTMRB 5", TxIoListener.REGISTER_IMC0E, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTB6] = new InterruptDescription("INTTB6", "16bitTMRB 6", TxIoListener.REGISTER_IMC0F, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTB7] = new InterruptDescription("INTTB7", "16bitTMRB 7", TxIoListener.REGISTER_IMC0F, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTB8] = new InterruptDescription("INTTB8", "16bitTMRB 8", TxIoListener.REGISTER_IMC0F, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTB9] = new InterruptDescription("INTTB9", "16bitTMRB 9", TxIoListener.REGISTER_IMC0F, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTBA] = new InterruptDescription("INTTBA", "16bitTMRB A", TxIoListener.REGISTER_IMC10, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTBB] = new InterruptDescription("INTTBB", "16bitTMRB B", TxIoListener.REGISTER_IMC10, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTBC] = new InterruptDescription("INTTBC", "16bitTMRB C", TxIoListener.REGISTER_IMC10, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTBD] = new InterruptDescription("INTTBD", "16bitTMRB D", TxIoListener.REGISTER_IMC10, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTBE] = new InterruptDescription("INTTBE", "16bitTMRB E", TxIoListener.REGISTER_IMC11, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTBF] = new InterruptDescription("INTTBF", "16bitTMRB F", TxIoListener.REGISTER_IMC11, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTADA] = new InterruptDescription("INTADA", "A/D conversion completion A", TxIoListener.REGISTER_IMC11, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTADB] = new InterruptDescription("INTADB", "A/D conversion completion B", TxIoListener.REGISTER_IMC11, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTADC] = new InterruptDescription("INTADC", "A/D conversion completion C", TxIoListener.REGISTER_IMC12, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTB10] = new InterruptDescription("INTTB10", "16bitTMRB 10", TxIoListener.REGISTER_IMC12, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTB11] = new InterruptDescription("INTTB11", "16bitTMRB 11", TxIoListener.REGISTER_IMC12, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[PHCNT0] = new InterruptDescription("PHCNT0", "Two-phase pulse input counter 0", TxIoListener.REGISTER_IMC12, 3, TxIoListener.REGISTER_IMCGD, 2);
        hardwareInterruptDescription[PHCNT1] = new InterruptDescription("PHCNT1", "Two-phase pulse input counter 1", TxIoListener.REGISTER_IMC13, 0, TxIoListener.REGISTER_IMCGD, 3);
        hardwareInterruptDescription[PHCNT2] = new InterruptDescription("PHCNT2", "Two-phase pulse input counter 2", TxIoListener.REGISTER_IMC13, 1, TxIoListener.REGISTER_IMCGE, 0);
        hardwareInterruptDescription[PHCNT3] = new InterruptDescription("PHCNT3", "Two-phase pulse input counter 3", TxIoListener.REGISTER_IMC13, 2, TxIoListener.REGISTER_IMCGE, 1);
        hardwareInterruptDescription[PHCNT4] = new InterruptDescription("PHCNT4", "Two-phase pulse input counter 4", TxIoListener.REGISTER_IMC13, 3, TxIoListener.REGISTER_IMCGE, 2);
        hardwareInterruptDescription[PHCNT5] = new InterruptDescription("PHCNT5", "Two-phase pulse input counter 5", TxIoListener.REGISTER_IMC14, 0, TxIoListener.REGISTER_IMCGE, 3);
        hardwareInterruptDescription[INTCAP0] = new InterruptDescription("INTCAP0", "Input capture 0", TxIoListener.REGISTER_IMC14, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTCAP1] = new InterruptDescription("INTCAP1", "Input capture 1", TxIoListener.REGISTER_IMC14, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTCAP2] = new InterruptDescription("INTCAP2", "Input capture 2", TxIoListener.REGISTER_IMC14, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTCAP3] = new InterruptDescription("INTCAP3", "Input capture 3", TxIoListener.REGISTER_IMC15, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTCMP0] = new InterruptDescription("INTCMP0", "Compare 0", TxIoListener.REGISTER_IMC15, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTCMP1] = new InterruptDescription("INTCMP1", "Compare 1", TxIoListener.REGISTER_IMC15, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTCMP2] = new InterruptDescription("INTCMP2", "Compare 2", TxIoListener.REGISTER_IMC15, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTCMP3] = new InterruptDescription("INTCMP3", "Compare 3", TxIoListener.REGISTER_IMC16, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTCMP4] = new InterruptDescription("INTCMP4", "Compare 4", TxIoListener.REGISTER_IMC16, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTCMP5] = new InterruptDescription("INTCMP5", "Compare 5", TxIoListener.REGISTER_IMC16, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTCMP6] = new InterruptDescription("INTCMP6", "Compare 6", TxIoListener.REGISTER_IMC16, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTCMP7] = new InterruptDescription("INTCMP7", "Compare 7", TxIoListener.REGISTER_IMC17, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTTBT] = new InterruptDescription("INTTBT", "Overflow", TxIoListener.REGISTER_IMC17, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTRTC] = new InterruptDescription("INTRTC", "Real time clock timer", TxIoListener.REGISTER_IMC17, 2, TxIoListener.REGISTER_IMCGD, 1);
        hardwareInterruptDescription[INTDMA0] = new InterruptDescription("INTDMA0", "DMA transfer completion (channel.0)", TxIoListener.REGISTER_IMC17, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTDMA1] = new InterruptDescription("INTDMA1", "DMA transfer completion (channel.1)", TxIoListener.REGISTER_IMC18, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTDMA2] = new InterruptDescription("INTDMA2", "DMA transfer completion (channel.2)", TxIoListener.REGISTER_IMC18, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTDMA3] = new InterruptDescription("INTDMA3", "DMA transfer completion (channel.3)", TxIoListener.REGISTER_IMC18, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTDMA4] = new InterruptDescription("INTDMA4", "DMA transfer completion (channel.4)", TxIoListener.REGISTER_IMC18, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTDMA5] = new InterruptDescription("INTDMA5", "DMA transfer completion (channel.5)", TxIoListener.REGISTER_IMC19, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTDMA6] = new InterruptDescription("INTDMA6", "DMA transfer completion (channel.6)", TxIoListener.REGISTER_IMC19, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[INTDMA7] = new InterruptDescription("INTDMA7", "DMA transfer completion (channel.7)", TxIoListener.REGISTER_IMC19, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[SOFT] = new InterruptDescription("Software set", "Software set", TxIoListener.REGISTER_IMC19, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[104] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[105] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[106] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[107] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[108] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[109] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[110] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[111] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[112] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[113] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[114] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[115] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[116] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[117] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[118] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[119] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[120] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[121] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[122] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[123] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[124] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[125] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[126] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[127] = new InterruptDescription(null, "Reserved", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
    }

    public TxInterruptController(Platform platform) {
        super(platform);
    }

    /**
     * Request a hardware interrupt with the given number
     * @param interruptNumber between 0 and 127d, See spec section 6.5.1.5
     * @return true if interrupt could be requested
     */
    @Override
    public boolean request(int interruptNumber) {
        int imcSection = getRequestImcSection(interruptNumber);
        int il = getRequestLevel(interruptNumber);
        if (isImcDmSet(imcSection)) {
            // DMA: il = dma channel to start
            // Request transfer
            dreqflg = Format.clearBit(dreqflg, il);
            // See if it can be started now
            ((TxDmaController) platform.getDmaController()).getChannel(il).startTransferIfConditionsOk();

            return true;
        }
        else {
            // No DMA: il = interrupt level
            //noinspection SimplifiableIfStatement
            if (il > 0) {
                return request(new TxInterruptRequest(Type.HARDWARE_INTERRUPT, interruptNumber, il));
            }
            else {
                return false;
            }
        }
    }

    /**
     * Request a custom interrupt request
     * @param interruptRequest
     * @return true if interrupt could be requested
     */
    public boolean request(InterruptRequest interruptRequest) {
        if (((TxCPUState)platform.getCpuState()).getPowerMode() != TxCPUState.PowerMode.RUN) {
            // See if this interrupt can clear standby state
            int interruptNumber = interruptRequest.getInterruptNumber();
            if (isImcgIntxen(getIMCGSectionForInterrupt(interruptNumber))) {
                ((TxCPUState)platform.getCpuState()).setPowerMode(TxCPUState.PowerMode.RUN);
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

    public void removeEdgeTriggeredRequest(InterruptRequest interruptRequest) {
        // TODO
        // TX interrupt controller is different, having no external interrupts. After having problems with general solution, the implementation was split.
        // Also I wanted to keep your code with call of interruptController.request(...) as much as possible the same. Then it is more sure to work correctly.
        // All TX interrupts are in fact configurable level/edge/etc. So generic solution was not possible.
        // Well, it is easy to test in parts and easy to do code review.
    }

    @Override
    public String getStatus() {
        return "IVR=0x" + Format.asHex(ivr, 8) + " - ILEV=0x" + Format.asHex(ilev, 8) + " - Current interrupt level: " + getIlevCmask();
    }

    // ----------------------- Field accessors

    public int getIlev() {
        return ilev;
    }

    /**
     * Returns the current mask
     * @return the last 3 bits of ILEV
     */
    public int getIlevCmask() {
        return ilev & Ilev_Cmask_mask;
    }

    /**
     * Shifts ILEV left by 4 bits, then puts the given cmask as the current mask position (last 3 bits)
     * @param cmask
     */
    public void pushIlevCmask(int cmask) {
        ilev = (ilev << 4) | (cmask & Ilev_Cmask_mask);
        //System.err.println("ILEV = " + ilev);
    }

    /**
     * Shifts ILEV right by 4 bits
     */
    private void popIlev() {
        // MLEV = 0 : shift down
        ilev = ilev >>> 4;
        //System.err.println("ILEV = " + ilev);
    }

    /**
     * Sets a new value to ILEV.
     * If first bit is 1, replaces the current CMASK. If 0, it shifts back the ILEV right by 4 bits (pop)
     * @param newIlev
     */
    public void setIlev(int newIlev) {
        if (Format.isBitSet(newIlev, Ilev_Mlev_pos)) {
            ilev = (ilev & ~Ilev_Cmask_mask) | newIlev & Ilev_Cmask_mask;
            //System.err.println("ILEV = " + ilev);
        }
        else {
            popIlev();
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

    public int getDreqflg() {
        return dreqflg;
    }

    public void setDreqflg(int dreqflg) {
        // Only writing 1 has the effect of clearing the request
        this.dreqflg = this.dreqflg | dreqflg;
    }

    public void clearRequest(int channelNumber) {
        dreqflg = Format.setBit(dreqflg, channelNumber);
    }


    // IMC
    public int getRequestLevel(int interruptNumber) {
        return getImcIl(getRequestImcSection(interruptNumber));
    }

    public int getRequestActiveState(int interruptNumber) {
        return getImcEim(getRequestImcSection(interruptNumber));
    }

    public int getRequestImcSection(int interruptNumber) {
        InterruptDescription description = hardwareInterruptDescription[interruptNumber];
        int imc = platform.getMemory().load32(description.intcImcCtrlRegAddr, DebuggableMemory.AccessSource.INT);
        return getSection(imc, description.intcImcCtrlRegSection);
    }

    /**
     * Returns the IL part of a given IMCx section
     * @param imcSection one of the four 8-bit sections in an IMCx register
     * @return
     */
    private int getImcIl(int imcSection) {
        return imcSection & 0b0000_0111;
    }

    /**
     * Indicates if the DM part of a given IMCx section is set
     * @param imcSection one of the four 8-bit sections in an IMCx register
     * @return
     */
    private boolean isImcDmSet(int imcSection) {
        return (imcSection & 0b0001_0000) != 0;
    }

    /**
     * Returns the EIM part of a given IMCx section
     * @param imcSection one of the four 8-bit sections in an IMCx register
     * @return
     */
    private int getImcEim(int imcSection) {
        return (imcSection & 0b0110_0000) >> 5;
    }


    // CG
    /**
     * Returns the part (8 bits) of IMCGx register value corresponding to the given interrupt #
     * @param interruptNumber See spec section 6.5.1.5
     * @return
     */
    private int getIMCGSectionForInterrupt(int interruptNumber) {
        InterruptDescription description = hardwareInterruptDescription[interruptNumber];
        if (description.cgCtrlRegAddr == NULL_REGISTER) {
            throw new RuntimeException("No IMCGxx register found for interrupt #" + interruptNumber);
        }

        int registerValue = platform.getMemory().load32(description.cgCtrlRegAddr, DebuggableMemory.AccessSource.INT);
        return getSection(registerValue, description.cgCtrlRegSection);
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


    /**
     * Returns the "sectionNumber"th part (8 bits) of the given 32-bit register value
     * @param register32
     * @param sectionNumber
     * @return
     */
    private int getSection(int register32, int sectionNumber) {
        return (register32 >> (8 * sectionNumber)) & 0xFF;
    }



    public static class InterruptDescription {
        public String symbolicName;
        public String description;
        private int intcImcCtrlRegAddr;
        private int intcImcCtrlRegSection;
        private int cgCtrlRegAddr;
        private int cgCtrlRegSection;

        private InterruptDescription(String symbolicName, String description, int intcImcCtrlRegAddr, int intcImcCtrlRegSection, int cgCtrlRegAddr, int cgCtrlRegSection) {
            this.symbolicName = symbolicName;
            this.description = description;
            this.intcImcCtrlRegAddr = intcImcCtrlRegAddr;
            this.intcImcCtrlRegSection = intcImcCtrlRegSection;
            this.cgCtrlRegAddr = cgCtrlRegAddr;
            this.cgCtrlRegSection = cgCtrlRegSection;
        }
    }

    public void processInterrupt(TxInterruptRequest interruptRequest, int pcToStore, StatementContext context) {
        TxCPUState txCPUState = (TxCPUState) platform.getCpuState();

        txCPUState.setSscrPSS(txCPUState.getSscrCSS());

        // This follows the graphs in the architecture document (Table 6.3), but then some info was added from the HW spec (ex : setting EXL or ERL)
        switch (interruptRequest.getType()) {
            case RESET_EXCEPTION:
                txCPUState.setStatusBEV();
                txCPUState.clearStatusNMI();
                txCPUState.setStatusERL();
                txCPUState.clearStatusRP();
                txCPUState.setReg(TxCPUState.ErrorEPC, pcToStore);
                // set PSS to CSS without changing CSS
                txCPUState.setSscrPSS(txCPUState.getSscrCSS());

                // Branch to reset routine
                txCPUState.setPc(TxCPUState.RESET_ADDRESS);
                break;
            case NMI:
                txCPUState.setStatusNMI();
                txCPUState.setStatusERL();
                // hardware spec section 6.2.2.1 says BD should be modified in this case,
                // but table 6.3 section 6.1.3.3 says it should not.
                // Architecture spec also says it shouldn't, so...
                // txCPUState.setCauseBD(context.inDelaySlot);
                txCPUState.setReg(TxCPUState.ErrorEPC, pcToStore);
                // set PSS to CSS without changing CSS
                txCPUState.setSscrPSS(txCPUState.getSscrCSS());

                //platform.getClockGenerator().setNmiFlg(1);

                // Branch to reset routine
                txCPUState.setPc(TxCPUState.RESET_ADDRESS);
                break;
            default:
                if (!txCPUState.isStatusEXLSet()) {
                    if (context.getStoredDelaySlotType() == Instruction.DelaySlotType.NONE) {
                        txCPUState.clearCauseBD();
                    }
                    else {
                        txCPUState.setCauseBD();
                    }
                }
                txCPUState.setStatusEXL();
                txCPUState.setCauseExcCode(interruptRequest.getCode());
                txCPUState.setReg(TxCPUState.EPC, pcToStore);

                if (interruptRequest.getType().isInterrupt()) {
                    // Interrupt
                    // set CSS to PSS and change CSS
                    txCPUState.pushSscrCssIfSwitchingEnabled(interruptRequest.getLevel());

                    // set ILEV
                    pushIlevCmask(interruptRequest.getLevel());

                    // Branch to handler
                    if (txCPUState.isStatusBEVSet()) {
                        if (txCPUState.isCauseIVSet()) {
                            // BEV=1 & IV=1
                            txCPUState.setPc(TxInterruptController.ADDRESS_INTERRUPT_BEV1_IV1);
                        }
                        else {
                            // BEV=1 & IV=0
                            txCPUState.setPc(TxInterruptController.ADDRESS_INTERRUPT_BEV1_IV0);
                        }
                    }
                    else {
                        if (txCPUState.isCauseIVSet()) {
                            // BEV=0 & IV=1
                            txCPUState.setPc(TxInterruptController.ADDRESS_INTERRUPT_BEV0_IV1);
                        }
                        else {
                            // BEV=0 & IV=0
                            txCPUState.setPc(TxInterruptController.ADDRESS_INTERRUPT_BEV0_IV0);
                        }
                    }

                    // IVR = 4 x interrupt_number
                    setIvr8_0(interruptRequest.getInterruptNumber() << 2);

                }
                else {
                    // Other Exceptions
                    if (interruptRequest.getType() == Type.COPROCESSOR_UNUSABLE_EXCEPTION) {
                        txCPUState.setCauseCE(interruptRequest.getCoprocessorNumber());
                    }

                    if (   (interruptRequest.getType() == Type.INSTRUCTION_ADDRESS_ERROR_EXCEPTION)
                            || (interruptRequest.getType() == Type.DATA_ADDRESS_ERROR_EXCEPTION)) {
                        txCPUState.setReg(TxCPUState.BadVAddr, interruptRequest.getBadVAddr());
                    }

                    // Branch to handler
                    if (txCPUState.isStatusBEVSet()) {
                        // BEV=1
                        txCPUState.setPc(TxInterruptController.ADDRESS_INTERRUPT_BEV1_IV0);
                    }
                    else {
                        // BEV=0
                        txCPUState.setPc(TxInterruptController.ADDRESS_INTERRUPT_BEV0_IV0);
                    }
                    // set PSS to CSS without changing CSS
                    txCPUState.setSscrPSS(txCPUState.getSscrCSS());
                }
        }
    }


}
