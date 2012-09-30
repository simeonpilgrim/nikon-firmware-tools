package com.nikonhacker.realos;

import com.nikonhacker.Format;

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

    public String getExtendedInformationHex() {
        return "0x" + Format.asHex(extendedInformation, 8);
    }

    public int getObjectId() {
        return objectId;
    }

    public String getObjectIdHex() {
        return "0x" + Format.asHex(objectId, 2);
    }

}
