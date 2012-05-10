package com.nikonhacker.emu;

public class EmulationException extends Exception {
    public EmulationException(String message) {
        super(message);
    }

    public EmulationException() {
        super();
    }

    public EmulationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmulationException(Throwable cause) {
        super(cause);
    }
}
