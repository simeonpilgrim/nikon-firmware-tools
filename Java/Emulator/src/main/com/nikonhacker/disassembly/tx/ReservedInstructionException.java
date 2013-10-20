package com.nikonhacker.disassembly.tx;

import com.nikonhacker.disassembly.DisassemblyException;

public class ReservedInstructionException extends DisassemblyException {
    public ReservedInstructionException() {
        super();   
    }

    public ReservedInstructionException(String message) {
        super(message);   
    }

    public ReservedInstructionException(String message, Throwable cause) {
        super(message, cause);   
    }

    public ReservedInstructionException(Throwable cause) {
        super(cause);   
    }

    protected ReservedInstructionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);   
    }
}
