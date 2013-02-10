package com.nikonhacker.emu;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.Instruction;

public class CallStackItem {
    private int address;
    private int sp;
    private String statementString;
    private String targetAddress;
    private Instruction instruction;

    public CallStackItem(int address, int sp, Instruction instruction, String statementString, String targetAddress) {
        this.address = address;
        this.sp = sp;
        this.instruction = instruction;
        this.statementString = statementString;
        this.targetAddress = targetAddress;
    }

    public int getAddress() {
        return address;
    }

    public int getSp() {
        return sp;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    @Override
    public String toString() {
        return "0x" + Format.asHex(address,8) + " " + (statementString ==null?"": statementString);
    }
    
}
