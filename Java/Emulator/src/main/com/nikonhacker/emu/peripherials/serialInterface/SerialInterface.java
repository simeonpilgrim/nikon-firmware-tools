package com.nikonhacker.emu.peripherials.serialInterface;

public class SerialInterface {
    private int scrIbcr;
    private int smr;
    private int ssr;
    private int escrIbsr;
    private int tdr;
    private int bgr1;
    private int bgr0;
    private int ismk;
    private int isba;
    private int fcr1;
    private int fcr0;
    private int fbyte2;
    private int fbyte1;

    public void setScrIbcr(int scrIbcr) {
        this.scrIbcr = scrIbcr;
    }

    public void setSmr(int smr) {
        this.smr = smr;
    }

    public void setSsr(int ssr) {
        this.ssr = ssr;
    }

    public void setEscrIbsr(int escrIbsr) {
        this.escrIbsr = escrIbsr;
    }

    public void setTdr(int tdr) {
        this.tdr = tdr;
    }

    public void setBgr1(int bgr1) {
        this.bgr1 = bgr1;
    }

    public void setBgr0(int bgr0) {
        this.bgr0 = bgr0;
    }

    public void setIsmk(int ismk) {
        this.ismk = ismk;
    }

    public void setIsba(int isba) {
        this.isba = isba;
    }

    public void setFcr1(int fcr1) {
        this.fcr1 = fcr1;
    }

    public void setFcr0(int fcr0) {
        this.fcr0 = fcr0;
    }

    public void setFbyte2(int fbyte2) {
        this.fbyte2 = fbyte2;
    }

    public void setFbyte1(int fbyte1) {
        this.fbyte1 = fbyte1;
    }
}
