package com.nikonhacker.dfr;


public class Jump {
    int source;
    int target;
    boolean isConditional;

    public Jump(int source, int target, boolean conditional) {
        this.source = source;
        this.target = target;
        isConditional = conditional;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

    public boolean isConditional() {
        return isConditional;
    }

    @Override
    public String toString() {
        return (isConditional?"Conditional":"Inconditional") + " jump from 0x" + Integer.toHexString(source) + " to 0x" + Integer.toHexString(target);
    }
}
