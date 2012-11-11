package com.nikonhacker.emu.peripherials.programmableTimer;

import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

public class TxTimer extends ProgrammableTimer {
    int timerNumber;
    InterruptController interruptController;

    // Registers
    int en;
    int run;
    int cr;
    int mod;
    int ffcr;
    int st;
    int im;
    int uc;
    int rg0;
    int rg1;
    int cp0;
    int cp1;

    public TxTimer(int timerNumber, InterruptController interruptController) {
        this.timerNumber = timerNumber;
        this.interruptController = interruptController;
    }

    public int getEn() {
        return en;
    }

    public void setEn(int en) {
        this.en = en;
    }

    public int getRun() {
        return run;
    }

    public void setRun(int run) {
        this.run = run;
    }

    public int getCr() {
        return cr;
    }

    public void setCr(int cr) {
        this.cr = cr;
    }

    public int getMod() {
        return mod;
    }

    public void setMod(int mod) {
        this.mod = mod;
    }

    public int getFfcr() {
        return ffcr;
    }

    public void setFfcr(int ffcr) {
        this.ffcr = ffcr;
    }

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }

    public int getIm() {
        return im;
    }

    public void setIm(int im) {
        this.im = im;
    }

    public int getUc() {
        return uc;
    }

    public void setUc(int uc) {
        this.uc = uc;
    }

    public int getRg0() {
        return rg0;
    }

    public void setRg0(int rg0) {
        this.rg0 = rg0;
    }

    public int getRg1() {
        return rg1;
    }

    public void setRg1(int rg1) {
        this.rg1 = rg1;
    }

    public int getCp0() {
        return cp0;
    }

    public void setCp0(int cp0) {
        this.cp0 = cp0;
    }

    public int getCp1() {
        return cp1;
    }

    public void setCp1(int cp1) {
        this.cp1 = cp1;
    }
}
