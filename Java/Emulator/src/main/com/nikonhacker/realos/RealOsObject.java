package com.nikonhacker.realos;

public class RealOsObject {
    protected ErrorCode errorCode;
    protected int extendedInformation;

    public RealOsObject(int extendedInformation, ErrorCode errorCode) {
        this.extendedInformation = extendedInformation;
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getExtendedInformation() {
        return extendedInformation;
    }
}
