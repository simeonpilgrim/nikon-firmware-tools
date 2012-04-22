package com.nikonhacker.realos;

import com.nikonhacker.Format;

public class MailboxInformation extends RealOsObject {

    private int waitTaskInformation;
    private int pkMsg;

    public MailboxInformation(ErrorCode errorCode, int extendedInformation, int waitTaskInformation, int pkMsg) {
        super(extendedInformation, errorCode);
        this.waitTaskInformation = waitTaskInformation;
        this.pkMsg = pkMsg;
    }

    public int getWaitTaskInformation() {
        return waitTaskInformation;
    }

    @Override
    public String toString() {
        if (getErrorCode() != ErrorCode.E_OK) {
            return getErrorCode().toString();
        }
        return "pk_msg=0x" + Format.asHex(pkMsg, 8) + ", " + ((waitTaskInformation==0)?"no waiting task":"first waiting task=0x" + Format.asHex(waitTaskInformation, 2)) + ", extendedInformation=0x" + Format.asHex(getExtendedInformation(), 8);
    }
}
