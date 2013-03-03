package com.nikonhacker.realos;

import com.nikonhacker.Format;

public class SemaphoreInformation extends RealOsObject {

    private int waitTaskInformation;
    private int semaphoreCount;

    public SemaphoreInformation(int objectId, ErrorCode errorCode) {
        super(objectId, errorCode, 0);
    }

    public SemaphoreInformation(int objectId, ErrorCode errorCode, int extendedInformation, int waitTaskInformation, int semaphoreCount) {
        super(objectId, errorCode, extendedInformation);
        this.waitTaskInformation = waitTaskInformation;
        this.semaphoreCount = semaphoreCount;
    }

    public int getWaitTaskInformation() {
        return waitTaskInformation;
    }

    public String getWaitTaskInformationHex() {
        if (waitTaskInformation == 0) {
            return "(none)";
        }
        return "0x" + Format.asHex(waitTaskInformation, 2);
    }

    public int getSemaphoreCount() {
        return semaphoreCount;
    }

    @Override
    public String toString() {
        if (getErrorCode() != ErrorCode.E_OK) {
            return getErrorCode().toString();
        }
        return "Semaphore 0x" + Format.asHex(objectId, 2) + ": Count " + semaphoreCount + ", " + ((waitTaskInformation==0)?"no waiting task":"first waiting task=0x" + Format.asHex(waitTaskInformation, 2)) + ", extendedInformation=0x" + Format.asHex(getExtendedInformation(), 8);
    }
}
