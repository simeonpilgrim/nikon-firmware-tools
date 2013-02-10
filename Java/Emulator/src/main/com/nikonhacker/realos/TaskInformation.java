package com.nikonhacker.realos;

public class TaskInformation extends RealOsObject {

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

    protected int taskPriority = 0;
    protected TaskState taskState = TaskState.NONE;

    public TaskInformation(int objectId, ErrorCode errorCode) {
        super(objectId, errorCode, 0);
    }

    public TaskInformation(int objectId, ErrorCode errorCode, int stateValue, int taskPriority, int extendedInformation) {
        super(objectId, errorCode, extendedInformation);
        this.taskState = TaskState.fromValue(stateValue);
        this.taskPriority = taskPriority;
    }

    public int getTaskPriority() {
        return taskPriority;
    }

    public TaskState getTaskState() {
        return taskState;
    }

}
