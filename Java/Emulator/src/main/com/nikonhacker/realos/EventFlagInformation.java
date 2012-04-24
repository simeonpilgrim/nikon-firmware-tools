package com.nikonhacker.realos;

import com.nikonhacker.Format;

public class EventFlagInformation extends RealOsObject {

    private int waitTaskInformation;
    private int flagPattern;

    public EventFlagInformation(int objectId, ErrorCode errorCode, int extendedInformation, int waitTaskInformation, int flagPattern) {
        super(objectId, errorCode, extendedInformation);
        this.waitTaskInformation = waitTaskInformation;
        this.flagPattern = flagPattern;
    }

    public int getWaitTaskInformation() {
        return waitTaskInformation;
    }

    public int getFlagPattern() {
        return flagPattern;
    }

    @Override
    public String toString() {
        if (getErrorCode() != ErrorCode.E_OK) {
            return getErrorCode().toString();
        }
        return "EventFlag 0x" + Format.asHex(objectId, 2) + ": Pattern 0b" + Format.asBinary(flagPattern,32) + ", " + ((waitTaskInformation==0)?"no waiting task":"first waiting task=0x" + Format.asHex(waitTaskInformation, 2)) + ", extendedInformation=0x" + Format.asHex(getExtendedInformation(), 8);
    }
}
