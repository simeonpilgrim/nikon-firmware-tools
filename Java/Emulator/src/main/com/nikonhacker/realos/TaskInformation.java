package com.nikonhacker.realos;

import com.nikonhacker.Format;

public class TaskInformation extends RealOsObject {

    public static enum TaskState {
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

    private int taskPriority;
    private TaskState taskState;

    public TaskInformation(ErrorCode errorCode, int extendedInformation, int taskPriority, int stateValue) {
        super(extendedInformation, errorCode);
        this.taskPriority = taskPriority;
        this.taskState = TaskState.fromValue(stateValue);
    }

    public int getTaskPriority() {
        return taskPriority;
    }

    public TaskState getTaskState() {
        return taskState;
    }

    @Override
    public String toString() {
        if (getErrorCode() != ErrorCode.E_OK) {
            return getErrorCode().toString();
        }
        return "State " + taskState.name() + ", priority=" + taskPriority + ", extendedInformation=0x" + Format.asHex(getExtendedInformation(), 8);
    }
}
