package com.nikonhacker.itron;

import com.nikonhacker.Format;

public class EventFlagInformation extends ITronObject {

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

    public String getWaitTaskInformationHex() {
        if (waitTaskInformation == 0) {
            return "(none)";
        }
        return "0x" + Format.asHex(waitTaskInformation, 2);
    }

    public int getFlagPattern() {
        return flagPattern;
    }

    public String getFlagPatternHex() {
        return "0x" + Format.asHex(flagPattern, 8);
    }

    @Override
    public String toString() {
        if (getErrorCode() != ErrorCode.E_OK) {
            return getErrorCode().toString();
        }
        return "EventFlag 0x" + Format.asHex(objectId, 2) + ": Pattern 0b" + Format.asBinary(flagPattern,32) + ", " + ((waitTaskInformation==0)?"no waiting task":"first waiting task=0x" + Format.asHex(waitTaskInformation, 2)) + ", extendedInformation=0x" + Format.asHex(getExtendedInformation(), 8);
    }
}
