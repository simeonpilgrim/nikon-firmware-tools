package com.nikonhacker.emu;

import com.nikonhacker.Format;

public class CallStackItem {
    int address;
    int sp;
    String customLabel;

    public CallStackItem(int address, int sp) {
        this.address = address;
        this.sp = sp;
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

    public String getCustomLabel() {
        return customLabel;
    }

    public void setCustomLabel(String customLabel) {
        this.customLabel = customLabel;
    }

    @Override
    public String toString() {
        if (customLabel != null) {
            return customLabel;
        }
        else {
            return "0x" + Format.asHex(address,8) + " - SP=0x" + Format.asHex(sp,8);
        }
    }
    
}
