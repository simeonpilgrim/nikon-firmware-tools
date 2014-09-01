package com.nikonhacker.disassembly.tx;

public class InvalidRegisterAccessException extends Exception {
    public InvalidRegisterAccessException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public InvalidRegisterAccessException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public InvalidRegisterAccessException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public InvalidRegisterAccessException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected InvalidRegisterAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
