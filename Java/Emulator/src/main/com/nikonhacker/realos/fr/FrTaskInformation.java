package com.nikonhacker.realos.fr;

import com.nikonhacker.Format;
import com.nikonhacker.realos.BaseTaskInformation;
import com.nikonhacker.realos.ErrorCode;

public class FrTaskInformation extends BaseTaskInformation {

    public FrTaskInformation(int objectId, ErrorCode errorCode, int extendedInformation, int taskPriority, int stateValue) {
        super(objectId, errorCode, extendedInformation, taskPriority, stateValue);
    }

    @Override
    public String toString() {
        if (getErrorCode() != ErrorCode.E_OK) {
            return getErrorCode().toString();
        }
        return "Task 0x" + Format.asHex(objectId, 2) + ": State " + taskState.name() + ", priority=" + taskPriority + ", extendedInformation=0x" + Format.asHex(getExtendedInformation(), 8);
    }

}
