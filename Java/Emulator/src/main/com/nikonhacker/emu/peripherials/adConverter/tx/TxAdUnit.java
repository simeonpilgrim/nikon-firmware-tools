package com.nikonhacker.emu.peripherials.adConverter.tx;

public class TxAdUnit {
    private final char unitName;
    private final int numChannels;

    private int clk;

    private int mod0;
    private int mod1;
    private int mod2;
    private int mod3;
    private int mod4;
    private int mod5;

    private int reg[];

    private int regSp;

    private int comReg0;
    private int comReg1;
    private boolean isEoc = false, isBusy = false;
    private boolean isPrioEoc = false, isPrioBusy = false;
    public TxAdUnit(char unitName, int numChannels) {
        this.unitName = unitName;
        this.numChannels = numChannels;
        reg = new int[numChannels];
    }

    public char getUnitName() {
        return unitName;
    }

    public int getNumChannels() {
        return numChannels;
    }

    public int getClk() {
        return clk;
    }

    public void setClk(int clk) {
        this.clk = clk;
    }

    public int getMod0() {
        return  (isEoc?0b10000000:0) | (isBusy?0b01000000:0) | (mod0 & 0b00011110);
    }

    public void setMod0(int mod0) {
        this.mod0 = mod0 & 0b00011111;
        if ((mod0 & 0b1) != 0) {
            System.out.println(toString() + " started");
            //TODO start conversion
        }
    }

    public int getMod1() {
        return mod1;
    }

    public void setMod1(int mod1) {
        this.mod1 = mod1;
    }

    public int getMod2() {
        return (isPrioEoc?0b10000000:0) | (isPrioBusy?0b01000000:0) | (mod2 & 0b00111111);
    }

    public void setMod2(int mod2) {
        this.mod2 = mod2 & 0b0011111;
    }

    public int getMod3() {
        return mod3 & 0b00111111;
    }

    public void setMod3(int mod3) {
        this.mod3 = mod3 & 0b10111111;
    }

    public int getMod4() {
        return mod4 & 0b11110000;
    }

    public void setMod4(int mod4) {
        // if ADRST goes from 10 to 01, perform a Software reset
        if ((this.mod4 & 0b11) == 0b10 && (mod4 & 0b11)== 0b01) {
            // TODO reset
        }
        this.mod4 = mod4 & 0b11110011;
    }

    public int getMod5() {
        return mod5 & 0b00111111;
    }

    public void setMod5(int mod5) {
        this.mod5 = mod5 & 0b10111111;
    }

    public int getReg(int regNumber) {
        return reg[regNumber];
    }

    public void setReg(int regNumber, int reg) {
        this.reg[regNumber] = reg;
    }

    public int getRegSp() {
        return regSp;
    }

    public void setRegSp(int regSp) {
        this.regSp = regSp;
    }

    public int getComReg0() {
        return comReg0;
    }

    public void setComReg0(int comReg0) {
        this.comReg0 = comReg0;
    }

    public int getComReg1() {
        return comReg1;
    }

    public void setComReg1(int comReg1) {
        this.comReg1 = comReg1;
    }

    @Override
    public String toString() {
        return "A/D Unit " + unitName;
    }
}
