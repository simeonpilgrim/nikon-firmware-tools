package com.nikonhacker.itron;

import com.nikonhacker.Format;

public class TaskInformation extends ITronObject {

    public static enum TaskState {
        NONE(0x0), // pseudo state
        RUN(0x1),
        READY(0x2),
        WAIT(0x4),
        SUSPEND(0x8),
        WAIT_SUSPEND(0xC),
        DORMANT(0x10);

        private int value;

        TaskState(int value) {
            this.value = value;
        }

        public static TaskState fromValue(int value) {
            for(TaskState state : values()) {
                if (state.value == value) {
                    return state;
                }
            }
            return null;
        }
    }

    protected int       taskPriority = 0;
    protected TaskState taskState    = TaskState.NONE;
    protected Integer   nextPc       = null;
    protected Integer   addrContext  = null;

    public TaskInformation(int objectId, ErrorCode errorCode) {
        super(objectId, errorCode, 0);
    }

    public TaskInformation(int objectId, ErrorCode errorCode, int stateValue, int taskPriority, int extendedInformation,
                           Integer nextPc, Integer context) {
        super(objectId, errorCode, extendedInformation);
        this.taskState = TaskState.fromValue(stateValue);
        this.taskPriority = taskPriority;
        this.nextPc = nextPc;
        this.addrContext = context;
    }

    public int getTaskPriority() {
        return taskPriority;
    }

    public TaskState getTaskState() {
        return taskState;
    }

    public int getNextPc() {
        return nextPc;
    }

    /** Helper method required for GlazedList display */
    public String getNextPcHex() {
        return (nextPc ==null)?"?":"0x" + Format.asHex(nextPc, 8);
    }

    public int getAddrContext() {
        return addrContext;
    }

    /** Helper method required for GlazedList display */
    public String getAddrContextHex() {
        return (addrContext==null)?"?":"0x" + Format.asHex(addrContext, 8);
    }
}
