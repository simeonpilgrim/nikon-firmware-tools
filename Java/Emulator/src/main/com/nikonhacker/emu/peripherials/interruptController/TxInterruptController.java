package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.Format;
import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.memory.Memory;

public class TxInterruptController extends AbstractInterruptController implements InterruptController {

    public static final int ADDRESS_INTERRUPT_BEV0_IV0 = 0x80000180;
    public static final int ADDRESS_INTERRUPT_BEV0_IV1 = 0x80000200;
    public static final int ADDRESS_INTERRUPT_BEV1_IV0 = 0xBFC00380;
    public static final int ADDRESS_INTERRUPT_BEV1_IV1 = 0xBFC00400;

    // Register fields
    // Ilev
    public final static int Ilev_Mlev_pos        = 31;
    public final static int Ilev_Cmask_mask      = 0b00000000_00000000_00000000_00000111;

    private Memory memory;

    private int ilev;

    public TxInterruptController(Memory memory) {
        this.memory = memory;
    }

    @Override
    public boolean request(int interruptNumber) {
        return false;  //TODO
    }

    @Override
    public void removeRequest(int requestNumber) {
        //TODO
    }

    public boolean request(InterruptRequest interruptRequest) {
        return false;  //TODO
    }


    // ----------------------- Field accessors

    public int getIlev() {
        return ilev;
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

    public void pushIlevCmask(int cmask) {
        setIlev(0b10000000_00000000_00000000_00000000 | cmask);
    }

}
