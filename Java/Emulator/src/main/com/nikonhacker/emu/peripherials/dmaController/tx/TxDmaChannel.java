package com.nikonhacker.emu.peripherials.dmaController.tx;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public class TxDmaChannel {
    private static final int CSR_CONF_MASK = 0b00000000_00000100_00000000_00000000;
    private static final int CSR_BED_MASK  = 0b00000000_00001000_00000000_00000000;
    private static final int CSR_BES_MASK  = 0b00000000_00010000_00000000_00000000;
    private static final int CSR_ABC_MASK  = 0b00000000_01000000_00000000_00000000;
    private static final int CSR_NC_MASK   = 0b00000000_10000000_00000000_00000000;
    private static final int CSR_ACT_MASK  = 0b10000000_00000000_00000000_00000000;

    private int channelNumber;
    private InterruptController interruptController;

    // registers
    private int ccr;
    private int csr;
    private int sar;
    private int dar;
    private int bcr;
    private int dtcr;

    public TxDmaChannel(int channelNumber, InterruptController interruptController) {
        this.channelNumber = channelNumber;
        this.interruptController = interruptController;
        reset();
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }



    public int getCcr() {
        return ccr;
    }

    public void setCcr(int ccr) {
        this.ccr = ccr & 0x7FFFFFFF;

        if ((ccr & 0x80000000) != 0) {
            // Perform transfer
            if (getCcrDpsBytes() != getCcrTrSizBytes()) {
                throw new RuntimeException("Error : Dps=" + getCcrDpsBytes() + "bytes while TrSiz=" + getCcrTrSizBytes() + "bytes");
            }
            throw new RuntimeException("DMA transfer not implemented");
        }
    }

    public int getCsr() {
        return csr;
    }

    public void setCsr(int csr) {
        this.csr = csr;
    }

    public int getSar() {
        return sar;
    }

    public void setSar(int sar) {
        this.sar = sar;
    }

    public int getDar() {
        return dar;
    }

    public void setDar(int dar) {
        this.dar = dar;
    }

    public int getBcr() {
        return bcr;
    }

    public void setBcr(int bcr) {
        this.bcr = bcr & 0x00FFFFFF;
    }

    public int getDtcr() {
        return dtcr;
    }

    public void setDtcr(int dtcr) {
        this.dtcr = dtcr;
    }



    public void reset() {
        // TODO if transfer implementation switches to async mode, stop current transfer, if any
        ccr = 0b00000000_11100010_00000000_00000000;
        csr = 0;
        sar = 0;
        dar = 0;
        bcr = 0;
        dtcr = 0;
    }

    // Individual fields utility accessors
    public int getCcrDpsBytes() {
        switch (ccr & 0b11) {
            case 0b11: return 1;
            case 0b10: return 2;
            default: return 4;
        }
    }

    public int getCcrTrSizBytes() {
        switch ((ccr >> 2) & 0b11) {
            case 0b11: return 1;
            case 0b10: return 2;
            default: return 4;
        }
    }

    public int getCcrDacIncrement() {
        switch ((ccr >> 4) & 0b11) {
            case 0b00: return 1;
            case 0b01: return -1;
            default: return 0;
        }
    }

    public int getCcrSacIncrement() {
        switch ((ccr >> 7) & 0b11) {
            case 0b00: return 1;
            case 0b01: return -1;
            default: return 0;
        }
    }

    public boolean isCcrSioSingle() {
        return ((ccr >> 9) & 0b1) != 0;
    }

    public boolean isCcrRelEn() {
        return ((ccr >> 10) & 0b1) != 0;
    }

    public boolean isCcrSReqSnoop() {
        return ((ccr >> 11) & 0b1) != 0;
    }

    public boolean isCcrLevelMode() {
        return ((ccr >> 12) & 0b1) != 0;
    }

    public boolean isCcrPosEdge() {
        return ((ccr >> 13) & 0b1) != 0;
    }

    public boolean isCcrExternalRequest() {
        return ((ccr >> 14) & 0b1) != 0;
    }

    public boolean isCcrBigEndian() {
        return ((ccr >> 17) & 0b1) != 0;
    }

    public boolean isCcrAbnormalInterruptEnable() {
        return ((ccr >> 22) & 0b1) != 0;
    }

    public boolean isCcrNormalInterruptEnable() {
        return ((ccr >> 23) & 0b1) != 0;
    }


    public boolean isCsrConfigurationError() {
        return (csr & CSR_CONF_MASK) != 0;
    }
    public void setCsrConfigurationError() {
        csr |= CSR_CONF_MASK;
    }
    public void clearCsrConfigurationError() {
        csr &= ~CSR_CONF_MASK;
    }

    public boolean isCsrBusErrorDestination() {
        return (csr & CSR_BED_MASK) != 0;
    }
    public void setCsrBusErrorDestination() {
        csr |= CSR_BED_MASK;
    }
    public void clearCsrBusErrorDestination() {
        csr &= ~CSR_BED_MASK;
    }

    public boolean isCsrBusErrorSource() {
        return (csr & CSR_BES_MASK) != 0;
    }
    public void setCsrBusErrorSource() {
        csr |= CSR_BES_MASK;
    }
    public void clearCsrBusErrorSource() {
        csr &= ~CSR_BES_MASK;
    }

    public boolean isCsrAbnormalCompletion() {
        return (csr & CSR_ABC_MASK) != 0;
    }
    public void setCsrAbnormalCompletion() {
        csr |= CSR_ABC_MASK;
    }
    public void clearCsrAbnormalCompletion() {
        csr &= ~CSR_ABC_MASK;
    }

    public boolean isCsrNormalCompletion() {
        return (csr & CSR_NC_MASK) != 0;
    }
    public void setCsrNormalCompletion() {
        csr |= CSR_NC_MASK;
    }
    public void clearCsrNormalCompletion() {
        csr &= ~CSR_NC_MASK;
    }

    public boolean isCsrActive() {
        return (csr & CSR_ACT_MASK) != 0;
    }
    public void setCsrActive() {
        csr |= CSR_ACT_MASK;
    }
    public void clearCsrActive() {
        csr &= ~CSR_ACT_MASK;
    }

    public int getDtcrDacmStartBit() {
        switch ((dtcr >> 3) & 0b111) {
            case 0b000:
                return 0;
            case 0b001:
                return 4;
            case 0b010:
                return 8;
            case 0b011:
                return 12;
            case 0b100:
                return 16;
            default:
                throw new RuntimeException("Invalid DTCR:DACM DTCR=0x" + Format.asHex(dtcr, 8));
        }
    }

    public int getDtcrSacmStartBit() {
        switch (dtcr & 0b111) {
            case 0b000:
                return 0;
            case 0b001:
                return 4;
            case 0b010:
                return 8;
            case 0b011:
                return 12;
            case 0b100:
                return 16;
            default:
                throw new RuntimeException("Invalid DTCR:SACM DTCR=0x" + Format.asHex(dtcr, 8));
        }
    }


}
