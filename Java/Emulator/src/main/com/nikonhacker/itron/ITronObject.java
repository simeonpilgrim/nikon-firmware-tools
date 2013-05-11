package com.nikonhacker.itron;

import com.nikonhacker.Format;

public abstract class ITronObject {
    protected int objectId;
    private ErrorCode errorCode;
    private int extendedInformation;

    protected ITronObject() {
    }

    public ITronObject(int objectId, ErrorCode errorCode, int extendedInformation) {
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
