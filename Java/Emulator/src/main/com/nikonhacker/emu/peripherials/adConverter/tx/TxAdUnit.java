package com.nikonhacker.emu.peripherials.adConverter.tx;

public class TxAdUnit {
    private char unitName;

    private int mod0;
    private int mod1;
    private int mod2;
    private int mod3;
    private int mod4;

    private int reg[];

    private int regSp;
    private final int nbChannels;

    public TxAdUnit(char unitName, int nbChannels) {
        this.unitName = unitName;
        this.nbChannels = nbChannels;
        reg = new int[nbChannels];
    }

    public char getUnitName() {
        return unitName;
    }

    public int getNbChannels() {
        return nbChannels;
    }

    public int getMod0() {
        return mod0;
    }

    public void setMod0(int mod0) {
        this.mod0 = mod0;
    }

    public int getMod1() {
        return mod1;
    }

    public void setMod1(int mod1) {
        this.mod1 = mod1;
    }

    public int getMod2() {
        return mod2;
    }

    public void setMod2(int mod2) {
        this.mod2 = mod2;
    }

    public int getMod3() {
        return mod3;
    }

    public void setMod3(int mod3) {
        this.mod3 = mod3;
    }

    public int getMod4() {
        return mod4;
    }

    public void setMod4(int mod4) {
        this.mod4 = mod4;
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
}
