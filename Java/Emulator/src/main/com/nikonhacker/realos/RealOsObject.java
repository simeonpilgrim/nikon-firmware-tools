package com.nikonhacker.realos;

public abstract class RealOsObject {
    protected int objectId;
    protected ErrorCode errorCode;
    protected int extendedInformation;

    protected RealOsObject() {
    }

    public RealOsObject(int objectId, ErrorCode errorCode, int extendedInformation) {
        this.objectId = objectId;
        this.extendedInformation = extendedInformation;
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getExtendedInformation() {
        return extendedInformation;
    }

    public int getObjectId() {
        return objectId;
    }
}
