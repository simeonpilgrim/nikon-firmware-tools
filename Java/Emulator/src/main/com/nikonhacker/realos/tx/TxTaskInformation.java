package com.nikonhacker.realos.tx;

import com.nikonhacker.Format;
import com.nikonhacker.realos.ErrorCode;
import com.nikonhacker.realos.TaskInformation;

public class TxTaskInformation extends TaskInformation {

    public TxTaskInformation(int objectId, ErrorCode errorCode, int extendedInformation, int taskPriority, int stateValue) {
        super(objectId, errorCode, extendedInformation, taskPriority, stateValue);
    }

    @Override
    public String toString() {
        if (getErrorCode() != ErrorCode.E_OK) {
            return getErrorCode().toString();
        }
        return "Task 0x" + Format.asHex(objectId, 2) + ": State " + taskState.name() + ", priority=" + taskPriority;
    }
}
