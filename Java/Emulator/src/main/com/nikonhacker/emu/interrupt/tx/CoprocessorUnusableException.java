package com.nikonhacker.emu.interrupt.tx;

public class CoprocessorUnusableException extends TxInterruptRequest {

    public CoprocessorUnusableException(int coprocessorNumber) {
        super(Type.COPROCESSOR_UNUSABLE_EXCEPTION);
        this.coprocessorNumber = coprocessorNumber;
    }
}
