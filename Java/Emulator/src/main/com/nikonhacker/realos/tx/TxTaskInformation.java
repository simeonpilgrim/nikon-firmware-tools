package com.nikonhacker.realos.tx;

import com.nikonhacker.Format;
import com.nikonhacker.realos.ErrorCode;
import com.nikonhacker.realos.TaskInformation;

public class TxTaskInformation extends TaskInformation {

    public static enum WaitReason {

        NONE(0x0), // pseudo reason
        SLEEP(0x1),
        DELAYED(0x2),
        SEMAPHORE(0x4),
        EVENTFLAG(0x8),
        SND_QUEUE(0x10),
        RCV_QUEUE(0x20),
        MAILBOX(0x40),
        MUTEX(0x80),
        SND_MSGBUF(0x100),
        RCV_MSGBUF(0x200),
        CALLING_RV(0x400),
        ACCEPT_RV(0x800),
        TERMIN_RV(0x1000),
        FIXED_MEM(0x2000),
        VAR_MEM(0x4000);

        private int value;

        WaitReason(int value) {
            this.value = value;
        }

        public static WaitReason fromValue(int value) {
            for(WaitReason state : values()) {
                if (state.value == value) {
                    return state;
                }
            }
            return null;
        }
    }


    private int taskBasePriority;
    private WaitReason reasonForWaiting;
    private int objectIdWaiting;
    private int timeLeft;
    private int actRequestCount;
    private int wuRequestCount;
    private int suspendCount;

    public TxTaskInformation(int objectId, ErrorCode errorCode) {
        super(objectId, errorCode);
    }

    public TxTaskInformation(int objectId, ErrorCode errorCode, int stateValue, int taskPriority, int taskBasePriority, int reasonForWaiting, int objectIdWaiting, int timeLeft, int actRequestCount, int wuRequestCount, int suspendCount) {
        super(objectId, errorCode, stateValue, taskPriority, 0);

        this.taskBasePriority = taskBasePriority;
        this.reasonForWaiting = WaitReason.fromValue(reasonForWaiting);
        this.objectIdWaiting = objectIdWaiting;
        this.timeLeft = timeLeft;
        this.actRequestCount = actRequestCount;
        this.wuRequestCount = wuRequestCount;
        this.suspendCount = suspendCount;
    }

    public int getTaskBasePriority() {
        return taskBasePriority;
    }

    public WaitReason getReasonForWaiting() {
        return reasonForWaiting;
    }

    public int getObjectIdWaiting() {
        return objectIdWaiting;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public int getActRequestCount() {
        return actRequestCount;
    }

    public int getWuRequestCount() {
        return wuRequestCount;
    }

    public int getSuspendCount() {
        return suspendCount;
    }

    @Override
    public String toString() {
        if (getErrorCode() != ErrorCode.E_OK) {
            return getErrorCode().toString();
        }
        return "Task 0x" + Format.asHex(objectId, 2) + ": State " + taskState.name() +
                ", priority=" + taskPriority +
                ", taskBasePriority=" + taskBasePriority +
                ", reasonForWaiting=" + reasonForWaiting.name() +
                ", objectIdWaiting=" + objectIdWaiting +
                ", timeLeft=" + timeLeft +
                ", actRequestCount=" + actRequestCount +
                ", wuRequestCount=" + wuRequestCount +
                ", suspendCount=" + suspendCount;
    }
}
