package com.nikonhacker.emu.interrupt.tx;

public class AddressErrorException extends TxInterruptRequest {

    public AddressErrorException(Type type, int badVAddr) {
        super(type);
        this.badVAddr = badVAddr;
    }
}
