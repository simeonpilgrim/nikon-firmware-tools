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

    public final static InterruptDescription[] hardwareInterruptDescription = new InterruptDescription[128];
    private static final int NULL_SECTION = -1;
    private static final int NULL_REGISTER = -1;

    private TxCPUState cpuState;
    private Memory memory;


    private int ilev;
    private int ivr;
    private int intClr;

    // This is a transcript of table 6.5, section 6.5.1.5 of the Toshiba hardware specification
    static {
        hardwareInterruptDescription[0] = new InterruptDescription(null, "(No interrupt factor)", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[1] = new InterruptDescription("SW", "Software interrupt", NULL_REGISTER, NULL_SECTION, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[2] = new InterruptDescription("INT0", "", TxIoListener.REGISTER_IMC00, 2, TxIoListener.REGISTER_IMCGA, 0);
        hardwareInterruptDescription[3] = new InterruptDescription("INT1", "", TxIoListener.REGISTER_IMC00, 3, TxIoListener.REGISTER_IMCGA, 1);
        hardwareInterruptDescription[4] = new InterruptDescription("INT2", "", TxIoListener.REGISTER_IMC01, 0, TxIoListener.REGISTER_IMCGA, 2);
        hardwareInterruptDescription[5] = new InterruptDescription("INT3", "", TxIoListener.REGISTER_IMC01, 1, TxIoListener.REGISTER_IMCGA, 3);
        hardwareInterruptDescription[6] = new InterruptDescription("INT4", "", TxIoListener.REGISTER_IMC01, 2, TxIoListener.REGISTER_IMCGB, 0);
        hardwareInterruptDescription[7] = new InterruptDescription("INT5", "", TxIoListener.REGISTER_IMC01, 3, TxIoListener.REGISTER_IMCGB, 1);
        hardwareInterruptDescription[8] = new InterruptDescription("INT6", "", TxIoListener.REGISTER_IMC02, 0, TxIoListener.REGISTER_IMCGB, 2);
        hardwareInterruptDescription[9] = new InterruptDescription("INT7", "", TxIoListener.REGISTER_IMC02, 1, TxIoListener.REGISTER_IMCGB, 3);
        hardwareInterruptDescription[10] = new InterruptDescription("INT8", "", TxIoListener.REGISTER_IMC02, 2, TxIoListener.REGISTER_IMCGC, 0);
        hardwareInterruptDescription[11] = new InterruptDescription("INT9", "", TxIoListener.REGISTER_IMC02, 3, TxIoListener.REGISTER_IMCGC, 1);
        hardwareInterruptDescription[12] = new InterruptDescription("INTA", "", TxIoListener.REGISTER_IMC03, 0, TxIoListener.REGISTER_IMCGC, 2);
        hardwareInterruptDescription[13] = new InterruptDescription("INTB", "", TxIoListener.REGISTER_IMC03, 1, TxIoListener.REGISTER_IMCGC, 3);
        hardwareInterruptDescription[14] = new InterruptDescription("INTC", "", TxIoListener.REGISTER_IMC03, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[15] = new InterruptDescription("INTD", "", TxIoListener.REGISTER_IMC03, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[16] = new InterruptDescription("INTE", "", TxIoListener.REGISTER_IMC04, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[17] = new InterruptDescription("INTF", "", TxIoListener.REGISTER_IMC04, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[18] = new InterruptDescription("KWUP", "Key-on wake-up", TxIoListener.REGISTER_IMC04, 2, TxIoListener.REGISTER_IMCGD, 0);
        hardwareInterruptDescription[19] = new InterruptDescription("INT10", "", TxIoListener.REGISTER_IMC04, 3, TxIoListener.REGISTER_IMCGF, 0);
        hardwareInterruptDescription[20] = new InterruptDescription("INT11", "", TxIoListener.REGISTER_IMC05, 0, TxIoListener.REGISTER_IMCGF, 1);
        hardwareInterruptDescription[21] = new InterruptDescription("INT12", "", TxIoListener.REGISTER_IMC05, 1, TxIoListener.REGISTER_IMCGF, 2);
        hardwareInterruptDescription[22] = new InterruptDescription("INT13", "", TxIoListener.REGISTER_IMC05, 2, TxIoListener.REGISTER_IMCGF, 3);
        hardwareInterruptDescription[23] = new InterruptDescription("INT14", "", TxIoListener.REGISTER_IMC05, 3, TxIoListener.REGISTER_IMCG10, 0);
        hardwareInterruptDescription[24] = new InterruptDescription("INT15", "", TxIoListener.REGISTER_IMC06, 0, TxIoListener.REGISTER_IMCG10, 1);
        hardwareInterruptDescription[25] = new InterruptDescription("INT16", "", TxIoListener.REGISTER_IMC06, 1, TxIoListener.REGISTER_IMCG10, 2);
        hardwareInterruptDescription[26] = new InterruptDescription("INT17", "", TxIoListener.REGISTER_IMC06, 2, TxIoListener.REGISTER_IMCG10, 3);
        hardwareInterruptDescription[27] = new InterruptDescription("INT18", "", TxIoListener.REGISTER_IMC06, 3, TxIoListener.REGISTER_IMCG11, 0);
        hardwareInterruptDescription[28] = new InterruptDescription("INT19", "", TxIoListener.REGISTER_IMC07, 0, TxIoListener.REGISTER_IMCG11, 1);
        hardwareInterruptDescription[29] = new InterruptDescription("INT1A", "", TxIoListener.REGISTER_IMC07, 1, TxIoListener.REGISTER_IMCG11, 2);
        hardwareInterruptDescription[30] = new InterruptDescription("INT1B", "", TxIoListener.REGISTER_IMC07, 2, TxIoListener.REGISTER_IMCG11, 3);
        hardwareInterruptDescription[31] = new InterruptDescription("INT1C", "", TxIoListener.REGISTER_IMC07, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[32] = new InterruptDescription("INT1D", "", TxIoListener.REGISTER_IMC08, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[33] = new InterruptDescription("INT1E", "", TxIoListener.REGISTER_IMC08, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[34] = new InterruptDescription("INT1F", "", TxIoListener.REGISTER_IMC08, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[35] = new InterruptDescription("INTRX0", "Serial reception (channel.0)", TxIoListener.REGISTER_IMC08, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[36] = new InterruptDescription("INTTX0", "Serial transmission (channel.0)", TxIoListener.REGISTER_IMC09, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[37] = new InterruptDescription("INTRX1", "Serial reception (channel.1)", TxIoListener.REGISTER_IMC09, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[38] = new InterruptDescription("INTTX1", "Serial transmission (channel.1)", TxIoListener.REGISTER_IMC09, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[39] = new InterruptDescription("INTRX2", "Serial reception (channel.2)", TxIoListener.REGISTER_IMC09, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[40] = new InterruptDescription("INTTX2", "Serial transmission (channel.2)", TxIoListener.REGISTER_IMC0A, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[41] = new InterruptDescription("HINTRX0", "High-speed serial reception (Hchannel.0)", TxIoListener.REGISTER_IMC0A, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[42] = new InterruptDescription("HINTTX0", "High-speed serial transmission (Hchannel.0)", TxIoListener.REGISTER_IMC0A, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[43] = new InterruptDescription("HINTRX1", "43 HINTRX1 : High-speed serial reception (Hchannel.1)", TxIoListener.REGISTER_IMC0A, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[44] = new InterruptDescription("HINTTX1", "44 HINTTX1 : High-speed serial transmission (Hchannel.1)", TxIoListener.REGISTER_IMC0B, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[45] = new InterruptDescription("HINTRX2", "45 HINTRX2 : High-speed serial reception (Hchannel.2)", TxIoListener.REGISTER_IMC0B, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[46] = new InterruptDescription("HINTTX2", "46 HINTTX2 : High-speed serial transmission (Hchannel.2)", TxIoListener.REGISTER_IMC0B, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[47] = new InterruptDescription("INTSBI0", "47 INTSBI0 : Serial bus interface 0 0x0BC", TxIoListener.REGISTER_IMC0B, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[48] = new InterruptDescription("INTADHPA", "48 INTADHPA : Highest priority AD conversion complete interrupt A", TxIoListener.REGISTER_IMC0C, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[49] = new InterruptDescription("INTADMA", "49 INTADMA : AD conversion monitoring function interrupt A", TxIoListener.REGISTER_IMC0C, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[50] = new InterruptDescription("INTADHPB", "50 INTADHPB : Highest priority AD conversion complete interrupt B", TxIoListener.REGISTER_IMC0C, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[51] = new InterruptDescription("INTADMB", "51 INTADMB : AD conversion monitoring function interrupt B", TxIoListener.REGISTER_IMC0C, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[52] = new InterruptDescription("INTADHPC", "52 INTADHPC : Highest priority AD conversion complete interrupt C", TxIoListener.REGISTER_IMC0D, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[53] = new InterruptDescription("INTADMC", "53 INTADMC : AD conversion monitoring function interrupt C", TxIoListener.REGISTER_IMC0D, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[54] = new InterruptDescription("INTTB0", "16bitTMRB 0", TxIoListener.REGISTER_IMC0D, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[55] = new InterruptDescription("INTTB1", "16bitTMRB 1", TxIoListener.REGISTER_IMC0D, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[56] = new InterruptDescription("INTTB2", "16bitTMRB 2", TxIoListener.REGISTER_IMC0E, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[57] = new InterruptDescription("INTTB3", "16bitTMRB 3", TxIoListener.REGISTER_IMC0E, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[58] = new InterruptDescription("INTTB4", "16bitTMRB 4", TxIoListener.REGISTER_IMC0E, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[59] = new InterruptDescription("INTTB5", "16bitTMRB 5", TxIoListener.REGISTER_IMC0E, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[60] = new InterruptDescription("INTTB6", "16bitTMRB 6", TxIoListener.REGISTER_IMC0F, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[61] = new InterruptDescription("INTTB7", "16bitTMRB 7", TxIoListener.REGISTER_IMC0F, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[62] = new InterruptDescription("INTTB8", "16bitTMRB 8", TxIoListener.REGISTER_IMC0F, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[63] = new InterruptDescription("INTTB9", "16bitTMRB 9", TxIoListener.REGISTER_IMC0F, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[64] = new InterruptDescription("INTTBA", "16bitTMRB A", TxIoListener.REGISTER_IMC10, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[65] = new InterruptDescription("INTTBB", "16bitTMRB B", TxIoListener.REGISTER_IMC10, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[66] = new InterruptDescription("INTTBC", "16bitTMRB C", TxIoListener.REGISTER_IMC10, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[67] = new InterruptDescription("INTTBD", "16bitTMRB D", TxIoListener.REGISTER_IMC10, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[68] = new InterruptDescription("INTTBE", "16bitTMRB E", TxIoListener.REGISTER_IMC11, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[69] = new InterruptDescription("INTTBF", "16bitTMRB F", TxIoListener.REGISTER_IMC11, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[70] = new InterruptDescription("INTADA", "A/D conversion completion A", TxIoListener.REGISTER_IMC11, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[71] = new InterruptDescription("INTADB", "A/D conversion completion B", TxIoListener.REGISTER_IMC11, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[72] = new InterruptDescription("INTADC", "A/D conversion completion C", TxIoListener.REGISTER_IMC12, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[73] = new InterruptDescription("INTTB10", "16bitTMRB 10", TxIoListener.REGISTER_IMC12, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[74] = new InterruptDescription("INTTB11", "16bitTMRB 11", TxIoListener.REGISTER_IMC12, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[75] = new InterruptDescription("PHCNT0", "Two-phase pulse input counter 0", TxIoListener.REGISTER_IMC12, 3, TxIoListener.REGISTER_IMCGD, 2);
        hardwareInterruptDescription[76] = new InterruptDescription("PHCNT1", "Two-phase pulse input counter 1", TxIoListener.REGISTER_IMC13, 0, TxIoListener.REGISTER_IMCGD, 3);
        hardwareInterruptDescription[77] = new InterruptDescription("PHCNT2", "Two-phase pulse input counter 2", TxIoListener.REGISTER_IMC13, 1, TxIoListener.REGISTER_IMCGE, 0);
        hardwareInterruptDescription[78] = new InterruptDescription("PHCNT3", "Two-phase pulse input counter 3", TxIoListener.REGISTER_IMC13, 2, TxIoListener.REGISTER_IMCGE, 1);
        hardwareInterruptDescription[79] = new InterruptDescription("PHCNT4", "Two-phase pulse input counter 4", TxIoListener.REGISTER_IMC13, 3, TxIoListener.REGISTER_IMCGE, 2);
        hardwareInterruptDescription[80] = new InterruptDescription("PHCNT5", "Two-phase pulse input counter 5", TxIoListener.REGISTER_IMC14, 0, TxIoListener.REGISTER_IMCGE, 3);
        hardwareInterruptDescription[81] = new InterruptDescription("INTCAP0", "Input capture 0", TxIoListener.REGISTER_IMC14, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[82] = new InterruptDescription("INTCAP1", "Input capture 1", TxIoListener.REGISTER_IMC14, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[83] = new InterruptDescription("INTCAP2", "Input capture 2", TxIoListener.REGISTER_IMC14, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[84] = new InterruptDescription("INTCAP3", "Input capture 3", TxIoListener.REGISTER_IMC15, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[85] = new InterruptDescription("INTCMP0", "Compare 0", TxIoListener.REGISTER_IMC15, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[86] = new InterruptDescription("INTCMP1", "Compare 1", TxIoListener.REGISTER_IMC15, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[87] = new InterruptDescription("INTCMP2", "Compare 2", TxIoListener.REGISTER_IMC15, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[88] = new InterruptDescription("INTCMP3", "Compare 3", TxIoListener.REGISTER_IMC16, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[89] = new InterruptDescription("INTCMP4", "Compare 4", TxIoListener.REGISTER_IMC16, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[90] = new InterruptDescription("INTCMP5", "Compare 5", TxIoListener.REGISTER_IMC16, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[91] = new InterruptDescription("INTCMP6", "Compare 6", TxIoListener.REGISTER_IMC16, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[92] = new InterruptDescription("INTCMP7", "Compare 7", TxIoListener.REGISTER_IMC17, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[93] = new InterruptDescription("INTTBT", "Overflow", TxIoListener.REGISTER_IMC17, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[94] = new InterruptDescription("INTRTC", "Real time clock timer", TxIoListener.REGISTER_IMC17, 2, TxIoListener.REGISTER_IMCGD, 1);
        hardwareInterruptDescription[95] = new InterruptDescription("INTDMA0", "DMA transfer completion (channel.0)", TxIoListener.REGISTER_IMC17, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[96] = new InterruptDescription("INTDMA1", "DMA transfer completion (channel.1)", TxIoListener.REGISTER_IMC18, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[97] = new InterruptDescription("INTDMA2", "DMA transfer completion (channel.2)", TxIoListener.REGISTER_IMC18, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[98] = new InterruptDescription("INTDMA3", "DMA transfer completion (channel.3)", TxIoListener.REGISTER_IMC18, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[99] = new InterruptDescription("INTDMA4", "DMA transfer completion (channel.4)", TxIoListener.REGISTER_IMC18, 3, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[100] = new InterruptDescription("INTDMA5", "DMA transfer completion (channel.5)", TxIoListener.REGISTER_IMC19, 0, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[101] = new InterruptDescription("INTDMA6", "DMA transfer completion (channel.6)", TxIoListener.REGISTER_IMC19, 1, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[102] = new InterruptDescription("INTDMA7", "DMA transfer completion (channel.7)", TxIoListener.REGISTER_IMC19, 2, NULL_REGISTER, NULL_SECTION);
        hardwareInterruptDescription[103] = new InterruptDescription("Software set", "Software set", TxIoListener.REGISTER_IMC19, 3, NULL_REGISTER, NULL_SECTION);
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
        InterruptDescription description = hardwareInterruptDescription[interruptNumber];
        int imc = memory.load32(description.intcImcCtrlRegAddr);
        return getImcIl(getSection(imc, description.intcImcCtrlRegSection));
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
        InterruptDescription description = hardwareInterruptDescription[interruptNumber];
        if (description.cgCtrlRegAddr == NULL_REGISTER) {
            throw new RuntimeException("No IMCGxx register found for interrupt #" + interruptNumber);
        }

        int registerValue = memory.load32(description.cgCtrlRegAddr);
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
}
