package com.nikonhacker.realos;

public enum ErrorCode {
    E_OK    (0x00000000, "Normal end"),
    E_NOMEM (0xfffffff6, "Not enough memory"),
    E_NOSPT (0xffffffef, "Unsupported function"),
    E_RSFN  (0xffffffec, "Reserved function code number"),
    E_RSATR (0xffffffe8, "Reserved attribute"),
    E_PAR   (0xffffffdf, "General parameter error"),
    E_ID    (0xffffffdd, "Illegal ID number"),
    E_NOEXS (0xffffffcc, "Object does not exist"),
    E_OBJ   (0xffffffc1, "Other error regarding status of object"),
    E_CTX   (0xffffffbb, "Context error"),
    E_QOVR  (0xffffffb7, "Queuing or nesting  overflow"),
    E_TMOUT (0xffffffab, "Polling failure or timeout"),
    E_RLWAI (0xffffffaa, "WAIT state forced release"),

    E_FREMU (0xffffffff, "FrEmulator exception"),;

    private final int value;
    private final String comment;

    ErrorCode(int value, String comment) {

        this.value = value;
        this.comment = comment;
    }

    public int getValue() {
        return value;
    }

    public String getComment() {
        return comment;
    }

    public static ErrorCode fromValue(int value) {
        for (ErrorCode error : values()) {
            if (error.value == value) {
                return error;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Error " + name() + " (" + comment + ')';
    }
}
