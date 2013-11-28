package com.nikonhacker.itron;


public enum ErrorCode {
    E_OK    (0x00000000,       0, "Normal end"),
    E_SYS   (Constants.NONE,  -5, "System error"),
    E_NOSPT (0xffffffef,      -9, "Unsupported function"),
    E_RSFN  (0xffffffec,     -10, "Reserved function code"),
    E_RSATR (0xffffffe8,     -11, "Reserved attribute"),
    E_PAR   (0xffffffdf,     -17, "Parameter error"),
    E_ID    (0xffffffdd,     -18, "Invalid ID number"),
    E_CTX   (0xffffffbb,     -25, "Context error"),
    E_MACV  (Constants.NONE, -26, "Memory access violation"),
    E_OACV  (Constants.NONE, -27, "Object access violation"),
    E_ILUSE (Constants.NONE, -28, "Illegal service call use"),
    E_NOMEM (0xfffffff6,     -33, "Insufficient memory"),
    E_NOID  (Constants.NONE, -34, "No ID number available"),
    E_OBJ   (0xffffffc1,     -41, "Object state error"),
    E_NOEXS (0xffffffcc,     -42, "Non existent object"),
    E_QOVR  (0xffffffb7,     -43, "Queue overflow"),
    E_RLWAI (0xffffffaa,     -49, "Forced release from waiting"),
    E_TMOUT (0xffffffab,     -50, "Polling failure or timeout"),
    E_DLT   (Constants.NONE, -51, "Waiting object deleted"),
    E_CLS   (Constants.NONE, -52, "Waiting object state changed"),
    E_WBLK  (Constants.NONE, -57, "Non-blocking call accepted"),
    E_BOVR  (Constants.NONE, -58, "Buffer overflow"),

    E_EMULATOR(0xffffffff,    -1, "Emulator exception");

    private final int frValue;
    private final int txValue;
    private final String comment;

    ErrorCode(int frValue, int txValue, String comment) {
        this.frValue = frValue;
        this.txValue = txValue;
        this.comment = comment;
    }

    public int getFrValue() {
        return frValue;
    }

    public int getTxValue() {
        return frValue;
    }

    public String getComment() {
        return comment;
    }

    public static ErrorCode fromFrValue(int frValue) {
        for (ErrorCode error : values()) {
            if (error.frValue == frValue) {
                return error;
            }
        }
        return null;
    }

    public static ErrorCode fromTxValue(int txValue) {
        for (ErrorCode error : values()) {
            if (error.txValue == txValue) {
                return error;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Error " + name() + " (" + comment + ')';
    }

    private static class Constants {
        public static final int NONE = 0x11111111;
    }
}
