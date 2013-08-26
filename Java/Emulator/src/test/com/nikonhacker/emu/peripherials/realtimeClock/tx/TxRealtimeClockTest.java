package com.nikonhacker.emu.peripherials.realtimeClock.tx;

import com.nikonhacker.Format;
import com.nikonhacker.emu.MasterClock;
import com.nikonhacker.emu.Platform;
import junit.framework.TestCase;

public class TxRealtimeClockTest extends TestCase {

    public void testRealtimeClock() throws Exception {
        MasterClock masterClock = new MasterClock();

        Platform platform = new Platform(masterClock);
        TxRealtimeClock rtc = new TxRealtimeClock(platform);
        platform.setRealtimeClock(rtc);

        testRtcWrap(platform, true, 13,  8, 26, 10, 44, 12);
        testRtcWrap(platform, true, 13,  8, 26, 10, 44, 59);
        testRtcWrap(platform, true, 13,  8, 26, 10, 59, 59);
        testRtcWrap(platform, true, 13,  8, 26, 23, 59, 59);
        testRtcWrap(platform, true, 13,  8, 31, 23, 59, 59);
        testRtcWrap(platform, true, 13, 12, 31, 23, 59, 59);
        testRtcWrap(platform, true, 13,  2, 28, 23, 59, 59);
        testRtcWrap(platform, true, 16,  2, 28, 23, 59, 59);

        testRtcWrap(platform, false,13,  8, 26, 11, 59, 59);

        testRtcWrap(platform, true,  0,  1,  1,  0,  0,  1);
    }

    private void testRtcWrap(Platform platform, boolean mode24h, int year, int month, int date, int hour, int min, int sec) {
        TxRealtimeClock rtc = (TxRealtimeClock) platform.getRealtimeClock();
        System.out.println("Testing wrap of " + year + "-" + month + "-" + date + " " + hour + ":" + min + ":" + sec + " in " + (mode24h?"24":"12") + "h mode");
        setRtc(rtc, mode24h, year, month, date, hour, min, sec);

        System.out.print("  Exact :  ");
        platform.getMasterClock().setTotalElapsedTimePsForDebug(0);
        dumpRtc(rtc);

        System.out.print("  Plus 1s: ");
        platform.getMasterClock().setTotalElapsedTimePsForDebug(1000 * MasterClock.PS_PER_MS);
        dumpRtc(rtc);

        System.out.print("  Plus 2s: ");
        platform.getMasterClock().setTotalElapsedTimePsForDebug(2000 * MasterClock.PS_PER_MS);
        dumpRtc(rtc);

        platform.getMasterClock().setTotalElapsedTimePsForDebug(0);
    }

    private void setRtc(TxRealtimeClock rtc, boolean mode24h, int year, int month, int date, int hour, int min, int sec) {
        rtc.setPager(1);
        rtc.setMonthr((byte) (mode24h ? 1 : 0));
        rtc.setPager(0);
        rtc.setYearr((byte) Format.numberToBcd(year));
        rtc.setMonthr((byte) Format.numberToBcd(month));
        rtc.setDater((byte) Format.numberToBcd(date));
        rtc.setHourr((byte) Format.numberToBcd(hour));
        rtc.setMinr((byte) Format.numberToBcd(min));
        rtc.setSecr((byte) Format.numberToBcd(sec));
    }

    private void dumpRtc(TxRealtimeClock rtc) {
        System.out.println(
                Format.bcd2Number(rtc.getDater()) + "/" +
                Format.bcd2Number(rtc.getMonthr()) + "/" +
                Format.bcd2Number(rtc.getYearr()) + " " +
                Format.bcd2Number(rtc.getHourr()) + ":" +
                Format.bcd2Number(rtc.getMinr()) + ":" +
                Format.bcd2Number(rtc.getSecr())
        );
    }

}
