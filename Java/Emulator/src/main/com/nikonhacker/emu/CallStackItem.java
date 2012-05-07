package com.nikonhacker.emu;

import com.nikonhacker.Format;

public class CallStackItem {
    int address;
    int sp;
    private String instruction;

    public CallStackItem(int address, int sp, String instruction) {
        this.address = address;
        this.sp = sp;
        this.instruction = instruction;
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


    @Override
    public String toString() {
        return "0x" + Format.asHex(address,8) + " " + (instruction==null?"":instruction);
    }
    
}
