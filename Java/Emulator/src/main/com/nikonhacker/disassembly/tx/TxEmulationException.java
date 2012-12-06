package com.nikonhacker.disassembly.tx;

import com.nikonhacker.emu.EmulationException;

import java.io.FileNotFoundException;

public class TxEmulationException extends EmulationException {
    private TxStatement statement = null;
    private int exceptionType = Exceptions.NONE;

    public TxEmulationException(String message) {
        super(message);    
    }

    public TxEmulationException() {
        super();    
    }

    public TxEmulationException(String message, Throwable cause) {
        super(message, cause);    
    }

    public TxEmulationException(Throwable cause) {
        super(cause);    
    }

    public TxEmulationException(TxStatement statement, String message, int exceptionType) {
        super(message);
        this.statement = statement;
        this.exceptionType = exceptionType;
    }

    public TxEmulationException(TxStatement statement, Exception e) {
        super(e);
        this.statement = statement;
    }
}
