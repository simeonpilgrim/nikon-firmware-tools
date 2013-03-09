package com.nikonhacker.emu.peripherials.dmaController.tx;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.dmaController.DmaController;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public class TxDmaController implements DmaController {
    private static final int RSR_REQS0_MASK = 0b00000000_00000000_00000000_00000001;
    private static final int RSR_REQS4_MASK = 0b00000000_00000000_00000000_00010000;

    private int rsr;
    private int dhr;

    private TxDmaChannel channels[] = new TxDmaChannel[8];

    public TxDmaController(InterruptController interruptController) {
        for (int i = 0; i < 8; i++) {
            channels[i] = new TxDmaChannel(i, interruptController);
        }
    }

    public int getDcr() {
        // throw new RuntimeException("DCR register of DMA controller should not be read");
        // This "get" can be due to a bset of bclr operation
        return 0;
    }

    public void setDcr(int dcr) {
        if ((dcr & 0x80000000) != 0) {
            // Rstall
            for (TxDmaChannel channel : channels) {
                channel.reset();
            }
        }
        else {
            // individual Rst
            if ((dcr & 0x01) != 0) { channels[0].reset(); clearRsrResS0(); }
            if ((dcr & 0x02) != 0)   channels[1].reset();
            if ((dcr & 0x04) != 0)   channels[2].reset();
            if ((dcr & 0x08) != 0)   channels[3].reset();
            if ((dcr & 0x10) != 0) { channels[4].reset(); clearRsrResS4(); }
            if ((dcr & 0x20) != 0)   channels[5].reset();
            if ((dcr & 0x40) != 0)   channels[6].reset();
            if ((dcr & 0x80) != 0)   channels[7].reset();
        }
    }

    public int getRsr() {
        System.err.println("RSR register of DMA controller has no effect for now");
        return rsr;
    }

    public void setRsr(int rsr) {
        this.rsr = rsr;
        System.err.println("RSR register of DMA controller has no effect for now (set to 0x" + Format.asHex(rsr, 8) + ")");
    }

    public int getDhr() {
        System.err.println("DHR register of DMA controller has no effect for now");
        return dhr;
    }

    public void setDhr(int dhr) {
        this.dhr = dhr;
        System.err.println("DHR register of DMA controller has no effect for now (set to 0x" + Format.asHex(dhr, 8) + ")");
    }


    public boolean isRsrResS0() {
        return (rsr & RSR_REQS0_MASK) != 0;
    }
    public void setRsrResS0() {
        rsr |= RSR_REQS0_MASK;
    }
    public void clearRsrResS0() {
        rsr &= ~RSR_REQS0_MASK;
    }


    public boolean isRsrResS4() {
        return (rsr & RSR_REQS4_MASK) != 0;
    }
    public void setRsrResS4() {
        rsr |= RSR_REQS4_MASK;
    }
    public void clearRsrResS4() {
        rsr &= ~RSR_REQS4_MASK;
    }


    public TxDmaChannel getChannel(int channelNumber) {
        return channels[channelNumber];
    }
}
