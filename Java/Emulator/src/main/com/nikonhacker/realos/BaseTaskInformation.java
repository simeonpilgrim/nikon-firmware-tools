package com.nikonhacker.realos;

public class BaseTaskInformation extends RealOsObject {

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

    protected int taskPriority;
    protected TaskState taskState;

    public BaseTaskInformation(int objectId, ErrorCode errorCode, int extendedInformation, int taskPriority, int stateValue) {
        super(objectId, errorCode, extendedInformation);
        this.taskPriority = taskPriority;
        this.taskState = TaskState.fromValue(stateValue);
    }

    public int getTaskPriority() {
        return taskPriority;
    }

    public TaskState getTaskState() {
        return taskState;
    }

}
