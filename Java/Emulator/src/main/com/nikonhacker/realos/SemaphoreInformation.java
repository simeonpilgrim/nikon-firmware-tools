package com.nikonhacker.realos;

import com.nikonhacker.Format;

public class SemaphoreInformation extends RealOsObject {

    private int waitTaskInformation;
    private int semaphoreCount;

    public SemaphoreInformation(ErrorCode errorCode, int extendedInformation, int waitTaskInformation, int semaphoreCount) {
        super(extendedInformation, errorCode);
        this.waitTaskInformation = waitTaskInformation;
        this.semaphoreCount = semaphoreCount;
    }

    public int getWaitTaskInformation() {
        return waitTaskInformation;
    }

    @Override
    public String toString() {
        if (getErrorCode() != ErrorCode.E_OK) {
            return getErrorCode().toString();
        }
        return "Count " + semaphoreCount + ", " + ((waitTaskInformation==0)?"no waiting task":"first waiting task=0x" + Format.asHex(waitTaskInformation, 2)) + ", extendedInformation=0x" + Format.asHex(getExtendedInformation(), 8);
    }
}
