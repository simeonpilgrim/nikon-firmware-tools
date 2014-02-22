package com.nikonhacker.encoding;

public class FirmwareFormatException extends Exception {
    public FirmwareFormatException() {
    }

    public FirmwareFormatException(String message) {
        super(message);
    }

    public FirmwareFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public FirmwareFormatException(Throwable cause) {
        super(cause);
    }
}
