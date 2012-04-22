package com.nikonhacker.emu.interruptController;

public class InterruptControllerException extends RuntimeException {
    public InterruptControllerException() {
    }

    public InterruptControllerException(String message) {
        super(message);
    }

    public InterruptControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterruptControllerException(Throwable cause) {
        super(cause);
    }
}
