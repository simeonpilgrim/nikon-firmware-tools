package com.nikonhacker.disassembly;

public class DisassemblyException extends Exception {
    public DisassemblyException() {
        super();   
    }

    public DisassemblyException(String message) {
        super(message);   
    }

    public DisassemblyException(String message, Throwable cause) {
        super(message, cause);   
    }

    public DisassemblyException(Throwable cause) {
        super(cause);   
    }

    protected DisassemblyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);   
    }
}
