package com.nikonhacker.emu.peripherials.dmaController.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.tx.TxIoPort;

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
    private static final int CCR_STR_MASK  = 0b10000000_00000000_00000000_00000000;

    private static final int CSR_CONF_MASK = 0b00000000_00000100_00000000_00000000;
    private static final int CSR_BED_MASK  = 0b00000000_00001000_00000000_00000000;
    private static final int CSR_BES_MASK  = 0b00000000_00010000_00000000_00000000;
    private static final int CSR_ABC_MASK  = 0b00000000_01000000_00000000_00000000;
    private static final int CSR_NC_MASK   = 0b00000000_10000000_00000000_00000000;
    private static final int CSR_ACT_MASK  = 0b10000000_00000000_00000000_00000000;
    // Writing 1 to NC and AbC has no effect
    private static final int CSR_NOT_SETTABLE_TO_ONE = CSR_NC_MASK | CSR_ABC_MASK;

    private int channelNumber;
    private TxDmaController txDmaController;
    private boolean isInStandBy = false;

    // registers
    private int ccr = 0b00000000_11100010_00000000_00000000;
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


    public int getCcr() {
        return ccr;
    }

    private boolean isCcrBig() {
        return (ccr & CCR_BIG_MASK) != 0;
    }

    private boolean isStartRequested(int newCcr) {
        return (newCcr & CCR_STR_MASK) != 0;
    }

    public void setCcr(int ccr) {
        this.ccr = ccr & 0x7FFFFFFF;
        if (isStartRequested(ccr)) {
            // First check if a termination bit is set
            if (isCsrNormalCompletion()) {
                System.err.println(toString() + ": Attempt to put the channel in standby while CSR:NC bit is still set. Switching to Abnormal Completion");
                clearCsrNormalCompletion();
                setCsrAbnormalCompletion();
            }
            else if (isCsrAbnormalCompletion()) {
                System.err.println(toString() + ": Attempt to put the channel in standby while CSR:AbC bit is still set. Ignoring");
            }
            else {
                isInStandBy = true;
            }
        }
        startTransferIfConditionsOk();
    }

    public int getCsr() {
        return ((isInStandBy)?CSR_ACT_MASK:0) | (csr & ~CSR_ACT_MASK);
    }

    public void setCsr(int csr) {
        // Set CSR, except for NC and AbC which can only be reset, not set
        this.csr = ((csr & ~CSR_NOT_SETTABLE_TO_ONE) | (csr & this.csr & CSR_NOT_SETTABLE_TO_ONE));
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

    public void setCcrMSByte(byte ccrMSByte) {
        setCcr((ccr & 0x80FFFFFF) | (ccrMSByte << 24));
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
                throw new RuntimeException(toString() + ": Invalid DTCR:DACM DTCR=0x" + Format.asHex(dtcr, 8));
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
                throw new RuntimeException(toString() + ": Invalid DTCR:SACM DTCR=0x" + Format.asHex(dtcr, 8));
        }
    }


    public void startTransferIfConditionsOk() {
        boolean mustStart = false;
        // 1st condition: channel must be in stand-by
        if (isInStandBy) {
            if (isCcrExternalRequest()) {
                // Channel configured for external requests
                if (channelNumber == 0 && txDmaController.isRsrReqS0()) {
                    // Channel 0 configured for external by pin. See if pin is set
                    TxIoPort portF = (TxIoPort) txDmaController.getPlatform().getIoPorts()[IoPort.PORT_F];
                    // TODO spec page 10.23 speaks about Port 5 and Port A. This is plain wrong. Port is F
                    int maskPin0 = 0b00000001;
                    if ((portF.getFunctionRegister2() & maskPin0) != 0) {
                        // pin PF0 is indeed configured as DREQ0
                        if ((portF.getValue() & maskPin0) != 0) {
                            // pin PF0 is set
                            // TODO pin DACK0
                            mustStart = true;
                        }
                    }
                }
                else if (channelNumber == 4 && txDmaController.isRsrReqS4()) {
                    // Channel 4 configured for external by pin. See if pin is set
                    TxIoPort portF = (TxIoPort) txDmaController.getPlatform().getIoPorts()[IoPort.PORT_F];
                    int maskPin2 = 0b00000100;
                    if ((portF.getFunctionRegister2() & maskPin2) != 0) {
                        // pin PF2 is indeed configured as DREQ4
                        if ((portF.getValue() & maskPin2) != 0) {
                            // pin PF2 is set
                            // TODO pin DACK4
                            mustStart = true;
                        }
                    }
                }
                else {
                    // External configured for external by interrupt. See if DREQFLG.DREQn is requested
                    int dreqflg = ((TxInterruptController) (txDmaController.getPlatform().getInterruptController())).getDreqflg();
                    int mask = 1 << channelNumber;
                    if ((dreqflg & mask) == 0) {
                        // Transfer is requested
                        // TODO set DACK bit
                        mustStart = true;
                    }
                }
            }
            else {
                // request by software: no other conditions, start now
                mustStart = true;
            }
        }
        if (mustStart) {
            start();
        }
    }

    public void start() {
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

    private void performTransfer() {
        int dpsBytes = getCcrDpsBytes();
        if (dpsBytes != getCcrTrSizBytes()) {
            System.out.println(toString() + " Error: Dps=" + dpsBytes + "bytes while TrSiz=" + getCcrTrSizBytes() + "bytes");
            // abnormal termination
            signalAbnormalCompletion();
        }
        else if (sar % dpsBytes != 0) {
            System.out.println(toString() + " Error: SAR=0x" + Format.asHex(sar, 8) + " is not a multiple of " + dpsBytes + "bytes");
            // abnormal termination
            signalAbnormalCompletion();
        }
        else if (dar % dpsBytes != 0) {
            System.out.println(toString() + " Error: DAR=0x" + Format.asHex(dar, 8) + " is not a multiple of " + dpsBytes + "bytes");
            // abnormal termination
            signalAbnormalCompletion();
        }
        else if (bcr % dpsBytes != 0) {
            System.out.println(toString() + " Error: BCR=0x" + Format.asHex(bcr, 8) + " is not a multiple of " + dpsBytes + "bytes");
            // abnormal termination
            signalAbnormalCompletion();
        }
        else {
            DebuggableMemory memory = txDmaController.getPlatform().getMemory();
            // Perform transfer
            int srcIncrement = getCcrSacIncrement() * ((getDtcrSacmStartBit()==0)?dpsBytes:(1 << getDtcrSacmStartBit()));
            int dstIncrement = getCcrDacIncrement() * ((getDtcrDacmStartBit()==0)?dpsBytes:(1 << getDtcrDacmStartBit()));
            int value;
            // transfer is continuous for internal requests, or if it is specified as continuous
            boolean isContinuous = !isCcrExternalRequest() || !isCcrSioSingle();
            boolean doLoop = true;
            while (bcr != 0 && doLoop) {
                switch (dpsBytes) {
                    case 1:
                        value = memory.loadUnsigned8(sar, DebuggableMemory.AccessSource.DMA);
                        txDmaController.setDhr(value);
                        memory.store8(dar, value, DebuggableMemory.AccessSource.DMA);
                        break;
                    case 2:
                        value = memory.loadUnsigned16(sar, DebuggableMemory.AccessSource.DMA);
                        if (!isCcrBig()) {
                            // Endian switchover function
                            value = Format.swap2bytes(value);
                        }
                        txDmaController.setDhr(value);
                        memory.store16(dar, value, DebuggableMemory.AccessSource.DMA);
                        break;
                    case 4:
                        value = memory.load32(sar, DebuggableMemory.AccessSource.DMA);
                        if (!isCcrBig()) {
                            // Endian switchover function
                            value = Format.swap4bytes(value);
                        }
                        txDmaController.setDhr(value);
                        memory.store32(dar, value, DebuggableMemory.AccessSource.DMA);
                        break;
                }
                sar += srcIncrement;
                dar += dstIncrement;
                bcr--;

                if (isCcrExternalRequest()) {
                    // "a request made to the DMAC is cleared after completion of each data transfer (transfer of the amount of data specified by TrSiz)" (p 10-19)
                    ((TxInterruptController)txDmaController.getPlatform().getInterruptController()).clearRequest(channelNumber);
                    // TODO this contradicts p 10.23, pt 1 : "INTDREQn is not cleared until the number of bytes transferred (value set in the BCRn register) becomes 0."
                }

                doLoop = isContinuous;
            }
            if (bcr == 0) {
                if (isCcrExternalRequest()) {
                    // "On the other hand, during a continuous transfer, the DACKn signal is asserted only when the number of bytes transferred (value set in the BCRn register) becomes 0" (p 10-19)
                    // TODO DACK ?
                }
                // "transfer operation is always put in a standby mode for the next transfer request if the number of bytes transferred (value set in the BCRn register) does not become 0" (p 10-19)
                isInStandBy = false;

                signalNormalCompletion();
            }
        }
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

    @Override
    public String toString() {
        return "TxDmaChannel #" + channelNumber;
    }
}
