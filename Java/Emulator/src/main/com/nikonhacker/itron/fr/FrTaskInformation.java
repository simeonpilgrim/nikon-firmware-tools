package com.nikonhacker.itron.fr;

import com.nikonhacker.Format;
import com.nikonhacker.itron.ErrorCode;
import com.nikonhacker.itron.TaskInformation;

public class FrTaskInformation extends TaskInformation {

    public FrTaskInformation(int objectId, ErrorCode errorCode) {
        super(objectId, errorCode);
    }

    public FrTaskInformation(int objectId, ErrorCode errorCode, int stateValue, int taskPriority, int extendedInformation,
                             Integer pc, Integer context) {
        super(objectId, errorCode, stateValue, taskPriority, extendedInformation, pc, context);
    }

    @Override
    public String toString() {
        if (getErrorCode() != ErrorCode.E_OK) {
            return getErrorCode().toString();
        }
        return "Task 0x" + Format.asHex(objectId, 2) + ": State " + taskState.name() + ", priority=" + taskPriority +
                ", extendedInformation=0x" + Format.asHex(getExtendedInformation(), 8) +
                ", context=" + getAddrContextHex() +
                ", nextPC=" + getNextPcHex();
    }

}
