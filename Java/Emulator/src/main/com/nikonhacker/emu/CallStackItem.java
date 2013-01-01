package com.nikonhacker.emu;

import com.nikonhacker.Format;

public class CallStackItem {
    private int address;
    private int sp;
    private String instruction;
    private String targetAddress;

    public CallStackItem(int address, int sp, String instruction, String targetAddress) {
        this.address = address;
        this.sp = sp;
        this.instruction = instruction;
        this.targetAddress = targetAddress;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getSp() {
        return sp;
    }

    public void setSp(int sp) {
        this.sp = sp;
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    @Override
    public String toString() {
        return "0x" + Format.asHex(address,8) + " " + (instruction==null?"":instruction);
    }
    
}
