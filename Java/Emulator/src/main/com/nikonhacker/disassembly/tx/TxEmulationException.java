package com.nikonhacker.disassembly.tx;

import com.nikonhacker.disassembly.Statement;
import com.nikonhacker.emu.EmulationException;

public class TxEmulationException extends EmulationException {
    private Statement statement = null;
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

    public TxEmulationException(Statement statement, String message, int exceptionType) {
        super(message);
        this.statement = statement;
        this.exceptionType = exceptionType;
    }

    public TxEmulationException(Statement statement, Exception e) {
        super(e);
        this.statement = statement;
    }

    public int getExceptionType() {
        return exceptionType;
    }
}
