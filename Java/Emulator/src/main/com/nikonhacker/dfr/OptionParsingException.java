package com.nikonhacker.dfr;

public class OptionParsingException extends Exception {
    public OptionParsingException() {
    }

    public OptionParsingException(String message) {
        super(message);
    }

    public OptionParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public OptionParsingException(Throwable cause) {
        super(cause);
    }
}
