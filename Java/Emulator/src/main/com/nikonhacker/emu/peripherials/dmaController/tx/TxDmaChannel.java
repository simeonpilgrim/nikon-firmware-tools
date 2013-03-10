package com.nikonhacker.emu.peripherials.dmaController.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.Memory;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;

public class TxDmaChannel {
    private static final int CCR_SIO_MASK  = 0b00000000_00000000_00000010_00000000;
    private static final int CCR_RELEN_MASK= 0b00000000_00000000_00000100_00000000;
    private static final int CCR_SREQ_MASK = 0b00000000_00000000_00001000_00000000;
    private static final int CCR_LEV_MASK  = 0b00000000_00000000_00010000_00000000;
    private static final int CCR_POSE_MASK = 0b00000000_00000000_00100000_00000000;
    private static final int CCR_EXR_MASK  = 0b00000000_00000000_01000000_00000000;
    private static final int CCR_BIG_MASK  = 0b00000000_00000010_00000000_00000000;
    private static final int CCR_ABLEN_MASK= 0b00000000_01000000_00000000_00000000;
    private static final int CCR_NIEN_MASK = 0b00000000_10000000_00000000_00000000;
    private static final int CCR_STR_MASK  = 0b10000000_00000010_00000000_00000000;

    private static final int CSR_CONF_MASK = 0b00000000_00000100_00000000_00000000;
    private static final int CSR_BED_MASK  = 0b00000000_00001000_00000000_00000000;
    private static final int CSR_BES_MASK  = 0b00000000_00010000_00000000_00000000;
    private static final int CSR_ABC_MASK  = 0b00000000_01000000_00000000_00000000;
    private static final int CSR_NC_MASK   = 0b00000000_10000000_00000000_00000000;
    private static final int CSR_ACT_MASK  = 0b10000000_00000000_00000000_00000000;

    private int channelNumber;
    private TxDmaController txDmaController;

    // registers
    private int ccr;
    private int csr;
    private int sar;
    private int dar;
    private int bcr;
    private int dtcr;

    public TxDmaChannel(int channelNumber, TxDmaController txDmaController) {
        this.channelNumber = channelNumber;
        this.txDmaController = txDmaController;
        reset();
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }



    public int getCcr() {
        return ccr & 0x7FFFFFFF;
    }

    public void setCcr(int ccr) {
        this.ccr = ccr;

        if (isCcrStart()) {
            // Str: Start transfer
            // First check if a termination bit is set
            if (isCsrAbnormalCompletion() || isCsrNormalCompletion()) {
                System.err.println("DMA channel " + getChannelNumber() + " error: setting CCR:Str to 1 while CSR still indicates a Completion state: 0x" + Format.asHex(csr, 8) + ". Setting abnormal completion");
                clearCsrNormalCompletion();
                setCsrAbnormalCompletion();
            }
            else {
                requestStart(false, false);
            }
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

    // TODO single/continuous is not implemented
    public boolean isCcrSioSingle() {
        return (ccr & CCR_SIO_MASK) != 0;
    }

    public boolean isCcrRelEn() {
        return (ccr & CCR_RELEN_MASK) != 0;
    }

    public boolean isCcrSReqSnoop() {
        return (ccr & CCR_SREQ_MASK) != 0;
    }

    public boolean isCcrLevelMode() {
        return (ccr & CCR_LEV_MASK) != 0;
    }

    public boolean isCcrPosEdge() {
        return (ccr & CCR_POSE_MASK) != 0;
    }


    public boolean isCcrExternalRequest() {
        return (ccr & CCR_EXR_MASK) != 0;
    }
    public void setCcrExternalRequest() {
        ccr |= CCR_EXR_MASK;
    }
    public void clearCcrExternalRequest() {
        ccr &= ~CCR_EXR_MASK;
    }


    public boolean isCcrBigEndian() {
        return (ccr & CCR_BIG_MASK) != 0;
    }

    public boolean isCcrAbnormalInterruptEnable() {
        return (ccr & CCR_ABLEN_MASK) != 0;
    }

    public boolean isCcrNormalInterruptEnable() {
        return (ccr & CCR_NIEN_MASK) != 0;
    }

    public boolean isCcrStart() {
        return (ccr & CCR_STR_MASK) != 0;
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


    public void requestStart(boolean externalRequest, boolean externalRequestByPin) {
        // Check consistence of call wrt configuration
        boolean mustStart = false;
        if (externalRequest) {
            // This request comes from interrupt or pin
            if (!isCcrExternalRequest()) {
                // But channel configured for request by software
                System.out.println("DMA channel " + getChannelNumber() + " error: received external request while configured for software requests") ;
            }
            else {
                // OK, request coming from interrupt or pin on channel configured as such
                if (externalRequestByPin){
                    // This request comes from a pin
                    if(channelNumber == 0) {
                        // Possible pin channel
                        if (!txDmaController.isRsrReqS0()) {
                            // But external configured for request by interrupts
                            System.out.println("DMA channel " + getChannelNumber() + " error: received external request by pin while configured for external by interrupt") ;
                        }
                        else {
                            mustStart = true;
                        }
                    }
                    else if(channelNumber == 4) {
                        // Possible pin channel
                        if (!txDmaController.isRsrReqS4()) {
                            // But external configured for request by interrupts
                            System.out.println("DMA channel " + getChannelNumber() + " error: received external request by pin while configured for external by interrupt") ;
                        }
                        else {
                            mustStart = true;
                        }
                    }
                    else {
                        // Impossible pin channel
                        System.out.println("DMA channel " + getChannelNumber() + " error: received external request by pin on channel other than 0 or 4" ) ;
                    }
                }
                else {
                    // This request comes from an interrupt
                    if(channelNumber == 0) {
                        // Possible pin channel
                        if (txDmaController.isRsrReqS0()) {
                            // But external configured for request by pin
                            System.out.println("DMA channel " + getChannelNumber() + " error: received external request by interrupt while configured for external by pin") ;
                        }
                        else {
                            mustStart = true;
                        }
                    }
                    else if(channelNumber == 4) {
                        // Possible pin channel
                        if (!txDmaController.isRsrReqS4()) {
                            // But external configured for request by pin
                            System.out.println("DMA channel " + getChannelNumber() + " error: received external request by interrupt while configured for external by pin") ;
                        }
                        else {
                            mustStart = true;
                        }
                    }
                    else {
                        // No other test required for other channels
                        mustStart = true;
                    }
                }
            }
            if (!isCcrStart()) {
                System.out.println("DMA channel " + getChannelNumber() + " received external request but CCR:Str is false");
                mustStart = false;
            }
        }
        else {
            // This request comes from software
            if (isCcrExternalRequest()) {
                // But channel configured for request by interrupt or pin
                System.out.println("DMA channel " + getChannelNumber() + " error: received software request while configured for external requests") ;
            }
            else {
                mustStart = true;
            }
        }

        if (mustStart) {
            if (txDmaController.getPrefs().isDmaSynchronous(Constants.CHIP_TX)) {
                // SYNC
                performTransfer();
            }
            else {
                // ASYNC
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        performTransfer();
                    }
                }).start();
            }
        }
    }

    private void performTransfer() {
        int dpsBytes = getCcrDpsBytes();
        if (dpsBytes != getCcrTrSizBytes()) {
            System.out.println("Error: Dps=" + dpsBytes + "bytes while TrSiz=" + getCcrTrSizBytes() + "bytes");
            // abnormal termination
            signalAbnormalCompletion();
        }
        if (sar % dpsBytes != 0) {
            System.out.println("Error: SAR=0x" + Format.asHex(sar, 8) + " is not a multiple of " + dpsBytes + "bytes");
            // abnormal termination
            signalAbnormalCompletion();
        }
        if (dar % dpsBytes != 0) {
            System.out.println("Error: DAR=0x" + Format.asHex(dar, 8) + " is not a multiple of " + dpsBytes + "bytes");
            // abnormal termination
            signalAbnormalCompletion();
        }
        if (bcr % dpsBytes != 0) {
            System.out.println("Error: BCR=0x" + Format.asHex(bcr, 8) + " is not a multiple of " + dpsBytes + "bytes");
            // abnormal termination
            signalAbnormalCompletion();
        }
        Memory memory = txDmaController.getPlatform().getMemory();
        // Perform transfer
        int srcIncrement = getCcrSacIncrement() * ((getDtcrSacmStartBit()==0)?dpsBytes:(1 << getDtcrSacmStartBit()));
        int dstIncrement = getCcrDacIncrement() * ((getDtcrDacmStartBit()==0)?dpsBytes:(1 << getDtcrDacmStartBit()));
        int value;
        while (bcr != 0) {
            switch (dpsBytes) {
                case 1:
                    value = memory.loadUnsigned8(sar);
                    txDmaController.setDhr(value);
                    memory.store8(dar, value);
                    break;
                case 2:
                    value = memory.loadUnsigned16(sar);
                    txDmaController.setDhr(value);
                    memory.store16(dar, value);
                    break;
                case 4:
                    value = memory.load32(sar);
                    txDmaController.setDhr(value);
                    memory.store32(dar, value);
                    break;
            }
            sar += srcIncrement;
            dar += dstIncrement;
            bcr--;
        }
        // Clear the request
        if (isCcrExternalRequest()) {
            ((TxInterruptController)txDmaController.getPlatform().getInterruptController()).clearRequest(channelNumber);
        }
        signalNormalCompletion();
    }

    private void signalNormalCompletion() {
        setCsrNormalCompletion();
        // Interrupt if required
        if (isCcrNormalInterruptEnable()) {
            // Spec says "The DMA transfer completion interrupt comes in two types: INTDMA0 for 0ch through 3ch and INTDMA1 for 4ch through 7ch."
            // (bottom of page 10-27). I guess that is obsolete...
            txDmaController.getPlatform().getInterruptController().request(TxInterruptController.INTDMA0 + channelNumber);
        }
    }

    private void signalAbnormalCompletion() {
        setCsrAbnormalCompletion();
        // Interrupt if required
        if (isCcrAbnormalInterruptEnable() ) {
            // Spec says "The DMA transfer completion interrupt comes in two types: INTDMA0 for 0ch through 3ch and INTDMA1 for 4ch through 7ch."
            // (bottom of page 10-27). I guess that is obsolete...
            txDmaController.getPlatform().getInterruptController().request(TxInterruptController.INTDMA0 + channelNumber);
        }
    }
}
