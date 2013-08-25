package com.nikonhacker.emu.peripherials.realtimeClock.tx;

import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.realtimeClock.RealtimeClock;

import java.util.Calendar;
import java.util.GregorianCalendar;

/*
Possible usage at start of firmware
Starting listener for TX range 0xFF001500 - 0xFF00150F
Starting listener for TX range 0xFF001500 - 0xFF00150F
0x09       written to 0xFF00150B               (@0xBFC18B1C) switch to page 1

0x01       written to 0xFF001505               (@0xBFC18B20) 24h mode

0xC7       written to 0xFF00150F               (@0xBFC18B28) disable all ints

0x00       written to 0xFF001504               (@0xBFC18B2E) current year is a leap year

            read from 0xFF00150B : 0x09        (@0xBFC18B32)
0x08       written to 0xFF00150B               (@0xBFC18B38) switch to page 0

0x00       written to 0xFF001504               (@0xBFC18B3A) set date/time
0x00       written to 0xFF001505               (@0xBFC18B3C)
0x00       written to 0xFF001506               (@0xBFC18B42)
0x00       written to 0xFF001501               (@0xBFC18B4A)
0x00       written to 0xFF001502               (@0xBFC18B52)
0x00       written to 0xFF001503               (@0xBFC18B5A)

            read from 0xFF00150B : 0x08        (@0xBFC18C02)
0x08       written to 0xFF00150B               (@0xBFC18C08) switch to page 0

0xE7       written to 0xFF00150F               (@0xBFC18C10) reset clock

0x00       written to 0xFF001504               (@0xBFC18C18) set date/time
0x01       written to 0xFF001505               (@0xBFC18C20)
0x01       written to 0xFF001506               (@0xBFC18C2A)
0x00       written to 0xFF001501               (@0xBFC18C34)
0x00       written to 0xFF001502               (@0xBFC18C3E)
0x01       written to 0xFF001503               (@0xBFC18C48)

            read from 0xFF00150B : 0x08        (@0xBFC18C4E)
0x09       written to 0xFF00150B               (@0xBFC18C4E) switch to page 1

0x00       written to 0xFF001504               (@0xBFC18C5C) current year is a leap year

            read from 0xFF00150B : 0x09        (@0xBFC18C5E)
0x08       written to 0xFF00150B               (@0xBFC18C64) enable clock

            read from 0xFF001504 : 0x00        (@0xBFC18B7A)
            read from 0xFF001505 : 0x00        (@0xBFC18B84)
            read from 0xFF001506 : 0x00        (@0xBFC18B8E)
            read from 0xFF001501 : 0x00        (@0xBFC18B98)
            read from 0xFF001502 : 0x00        (@0xBFC18BA2)
            read from 0xFF001503 : 0x00        (@0xBFC18BAC)
*/    

public class TxRealtimeClock extends RealtimeClock {

    private static final byte MONTHR0_MO0_MASK = 0b00000001;

    private static final int PAGER_PAGE_MASK   = 0b00000001;
    private static final int PAGER_ENAALM_MASK = 0b00000100;
    private static final int PAGER_ADJUST_MASK = 0b00010000;
    private static final int PAGER_INTENA_MASK = 0b10000000;

    private static final int RESTR_DIS8HZ_MASK  = 0b00000001;
    private static final int RESTR_DIS4HZ_MASK  = 0b00000010;
    private static final int RESTR_DIS2HZ_MASK  = 0b00000100;
    private static final int RESTR_RSTALM_MASK  = 0b00010000;
    private static final int RESTR_RSTTMR_MASK  = 0b00100000;
    private static final int RESTR_DIS16HZ_MASK = 0b01000000;
    private static final int RESTR_DIS1HZ_MASK  = 0b10000000;

    private static GregorianCalendar leapTestCal = new GregorianCalendar();


    private int pager;
    private int restr = 0b11000111;

    // The next registers have two "pages": 0 for clock function and 1 for alarm function
    private byte secr[]   = new byte[2];
    private byte minr[]   = new byte[2];
    private byte hourr[]  = new byte[2];
    private byte dayr[]   = new byte[2];
    private byte dater[]  = new byte[2];
    private byte monthr[] = new byte[2];
    private byte yearr[]  = new byte[2];


    private long deltaMs; // difference between time stored in RTC and time elapsed since emulation was started

    public TxRealtimeClock(Platform platform) {
        super(platform);
    }

    // 32-bit registers

    public void setPager(int newPager) {
        // ignored fields
        newPager &= 0b10011101;

        if ((newPager & (PAGER_ENAALM_MASK | PAGER_INTENA_MASK)) != 0) {
            // TODO
            throw new RuntimeException("RTC interrupt/alarm are not implemented");
        }

        if ((newPager & PAGER_ADJUST_MASK) != 0) {
            // do seconds rounding
            Calendar adjTime = getCalendarFromRegisters();
            // if >29
            if (secr[0] > 0x29) {
                adjTime.add(Calendar.MINUTE, 1);
            }
            adjTime.set(Calendar.SECOND, 0);
            setRegistersFromCalendar(adjTime);
            newPager &= ~PAGER_ADJUST_MASK;
        }
        pager = newPager;
    }

    public int getPager() {
        return pager;
    }

    public int getPagerPage() {
        return pager & PAGER_PAGE_MASK;
    }

    public int getRestr() {
        return restr;
    }

    public void setRestr(int value) {
        // ignored fields
        value &= 0b11110111;
        restr = value;
        if ((value & RESTR_RSTTMR_MASK) != 0) {
            // Reset clock
            secr[0] = 0;
            minr[0] = 0;
            hourr[0] = 0;
            dayr[0] = 0;
            dater[0] = 0;
            monthr[0] = 0;
            yearr[0] = 0;
            updateDelta();
        }
        if ((value & RESTR_RSTALM_MASK) != 0) {
            // TODO reset alarm register should not only set to 0
            // TODO it should also put them in "don't care" mode, in which reading a page 1 register returns
            // TODO the value of the corresponding register in page 0
            // TODO this "don't care" mode is stored individually for each register and disabled by rewriting
            // TODO each register
            secr[1] = 0;
            minr[1] = 0;
            hourr[1] = 0;
            dayr[1] = 0;
            dater[1] = 0;
            monthr[1] = 0;
            yearr[1] = 0;
        }
    }

    // 8-bit registers

    public byte getSecr() {
        int page = getPagerPage();
        if (page == 0) updateRegisters();
        return secr[page];
    }

    public void setSecr(byte secr) {
        int page = getPagerPage();
        this.secr[page] = secr;
        if (page == 0) updateDelta();
    }

    public byte getMinr() {
        int page = getPagerPage();
        if (page == 0) updateRegisters();
        return minr[page];
    }

    public void setMinr(byte minr) {
        int page = getPagerPage();
        this.minr[page] = minr;
        if (page == 0) updateDelta();
    }

    public byte getHourr() {
        int page = getPagerPage();
        if (page == 0) updateRegisters();
        return hourr[page];
    }

    public void setHourr(byte hourr) {
        int page = getPagerPage();
        this.hourr[page] = hourr;
        if (page == 0) updateDelta();
    }

    public byte getDayr() {
        int page = getPagerPage();
        if (page == 0) updateRegisters();
        return dayr[page];
    }

    public void setDayr(byte dayr) {
        int page = getPagerPage();
        this.dayr[page] = dayr;
        if (page == 0) updateDelta();
    }

    public byte getDater() {
        int page = getPagerPage();
        if (page == 0) updateRegisters();
        return dater[page];
    }

    public void setDater(byte dater) {
        int page = getPagerPage();
        this.dater[page] = dater;
        if (page == 0) updateDelta();
    }

    public byte getMonthr() {
        int page = getPagerPage();
        if (page == 0) updateRegisters();
        return monthr[page];
    }

    public void setMonthr(byte monthr) {
        int page = getPagerPage();
        this.monthr[page] = monthr;
        if (page == 0) updateDelta();
    }

    public boolean isMonthr1Mo0Set() {
        return (monthr[1] & MONTHR0_MO0_MASK) != 0;
    }

    public byte getYearr() {
        int page = getPagerPage();
        if (page == 0) updateRegisters();
        return yearr[page];
    }

    public void setYearr(byte yearr) {
        int page = getPagerPage();
        this.yearr[page] = yearr;
        if (page == 0) updateDelta();
    }

    // 32-bit accessors to 8-bit registers

    public int getTimeReg32() {
        int page = getPagerPage();
        if (page == 0) updateRegisters();
        return (hourr[page] << 24) | (minr[page] << 8) | secr[page];
    }

    public void setTimeReg32(int value) {
        int page = getPagerPage();
        this.hourr[page] = (byte) ((value >> 24) & 0xFF);
        this.minr[page] = (byte) ((value >> 8) & 0xFF);
        this.secr[page] = (byte) (value & 0xFF);
        if (page == 0) updateDelta();
    }

    public int getDateReg32() {
        int page = getPagerPage();
        if (page == 0) updateRegisters();
        return (yearr[page] << 24) | (monthr[page] << 16) | (dater[page] << 8) | dayr[page];
    }

    public void setDateReg32(int value) {
        int page = getPagerPage();
        this.yearr[page] = (byte) ((value >> 24) & 0xFF);
        this.monthr[page] = (byte) ((value >> 16) & 0xFF);
        this.dater[page] = (byte) ((value >> 8) & 0xFF);
        this.dayr[page] = (byte) (value & 0xFF);
        if (page == 0) updateDelta();
    }

    /**
     * Updates all RTC registers based on the emulator elapsed time and delta
     */
    public void updateRegisters() {
        // calendar = elapsed + delta
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(platform.getMasterClock().getTotalElapsedTimePs() * 1_000_000_000 + deltaMs);

        // Convert calendar to registers
        setRegistersFromCalendar(c);
    }

    /**
     * Updates delta based on the emulator elapsed time and RTC registers
     */
    public void updateDelta() {
        // Convert registers to calendar
        Calendar c = getCalendarFromRegisters();

        // delta = calendar - elapsed
        deltaMs = c.getTimeInMillis() - platform.getMasterClock().getTotalElapsedTimePs() * 1_000_000_000;
    }

    private void setRegistersFromCalendar(Calendar c) {
        yearr[0] = (byte) num2bcd(c.get(Calendar.YEAR) % 100);
        yearr[1] = getLeapStatus(c.get(Calendar.YEAR));
        monthr[0] = (byte) num2bcd(c.get(Calendar.MONTH) + 1);
        dater[0] = (byte) num2bcd(c.get(Calendar.DAY_OF_MONTH));
        dayr[0] = (byte) num2bcd(c.get(Calendar.DAY_OF_WEEK) - 1);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        // Handle AM/PM mode
        if (!isMonthr1Mo0Set()) {
            // convert am/pm
            hour = (hour % 12) + (hour >= 12 ? 20 : 0);
        }

        hourr[0] = (byte) num2bcd(hour);
        minr[0] = (byte) num2bcd(c.get(Calendar.MINUTE));
        secr[0] = (byte) num2bcd(c.get(Calendar.SECOND));
    }

    private byte getLeapStatus(int year) {
        if (leapTestCal.isLeapYear(year)) return 0b00;
        if (leapTestCal.isLeapYear(year - 1)) return 0b01;
        if (leapTestCal.isLeapYear(year - 2)) return 0b10;
        if (leapTestCal.isLeapYear(year - 3)) return 0b11;
        // Questions : how about cases that are not covered ?
        // E.g if year is 2101: it is not leap, and none in the previous 3 are leap either.
        // Anyway, that shouldn't happen soon ;-)
        return 0b11;
    }

    private Calendar getCalendarFromRegisters() {
        Calendar c = Calendar.getInstance();

        int hour = bcd2num(hourr[0]);
        // Handle AM/PM mode
        if (!isMonthr1Mo0Set()) {
            // convert am/pm back
            hour = (hour % 20) + (hour >= 20 ? 12 : 0);
        }
        c.set(2000 + bcd2num(yearr[0]),
                bcd2num(monthr[0]) - 1,
                bcd2num(dater[0]),
                hour,
                bcd2num(minr[0]),
                bcd2num(secr[0])
        );
        return c;
    }


//    // --------------------- Read ---------------------
//
//
//    public int getTimeReg() {
//        if (getPagerPage() == 0) {
//            if ((pager & 0b1000) != 0) {
//                updateClock();
//            }
//        }
//        return timeRegisters[getPagerPage()];
//    }
//
//    // --------------------- Write with checks---------------------
//
//
//    public void setTimeReg(int value) {
//        if ((getPagerPage()) != 0) {
//            if (bcd2num(value & 0xFF) > 59)
//                throw new RuntimeException("RTC second is invalid");
//        }
//        else {
//            // not used
//            value &= 0xFFFFFF00;
//        }
//
//        if (bcd2num((value >> 8) & 0xFF) > 59)
//            throw new RuntimeException("RTC minute is invalid");
//
//        if ((dateRegisters[1] & 0x10000) == 0) {
//            // convert am/pm
//            int hour = bcd2num((value >> 16) & 0xFF);
//            if ((hour > 11 && hour < 20) || (hour > 31))
//                throw new RuntimeException("RTC hour is invalid");
//        }
//        else {
//            if (bcd2num((value >> 16) & 0xFF) > 23)
//                throw new RuntimeException("RTC hour is invalid");
//        }
//
//        timeRegisters[getPagerPage()] = value;
//
//        if ((getPagerPage()) == 0) {
//            updateDelta();
//        }
//    }
//
//    public void setDateReg(int value) {
//        int i;
//
//        i = bcd2num((value >> 8) & 0xFF);
//        // HACK: it seems D5100 Firmware 1.01 sets the day of month to 0 at 0xBFC18B42
//        // it is not allowed, so...
//        if (i == 0) {
//            i = 1;
//            value |= 0x100;
//        }
//        // HACK END
//        if (i < 1 || i > 31)
//            throw new RuntimeException("RTC day of month is invalid");
//
//        if (bcd2num(value & 0xFF) > 6)
//            throw new RuntimeException("RTC day of week is invalid");
//
//        if ((getPagerPage()) == 0) {
//            if (bcd2num((value >> 24) & 0xFF) > 99)
//                throw new RuntimeException("RTC year is invalid");
//
//            i = bcd2num((value >> 16) & 0xFF);
//            // HACK: it seems D5100 Firmware 1.01 sets the month to 0 at 0xBFC18C20
//            // it is not allowed, so...
//            if (i == 0) {
//                i = 1;
//                value |= 0x10000;
//            }
//            // HACK END
//            if (i < 1 || i > 12)
//                throw new RuntimeException("RTC month is invalid");
//        }
//        else {
//            // not used
//            value &= 0x0301FFFF;
//            // if changing hour format then convert
//            if ((value & 0x10000) != (dateRegisters[1] & 0x10000)) {
//                i = bcd2num((timeRegisters[0] >> 16) & 0xFF);
//                if ((value & 0x10000) != 0) {
//                    // new 24h, old am/pm
//                    i = (i >= 20 ? (i - 8) : i);
//                }
//                else {
//                    // new am/pm, old 24h
//                    i = (i >= 12 ? (i + 8) : i);
//                }
//                timeRegisters[0] = ((timeRegisters[0] & 0xFF00FFFF) | (num2bcd(i) << 16));
//            }
//        }
//
//        dateRegisters[getPagerPage()] = value;
//
//        if ((getPagerPage()) == 0) {
//            updateDelta();
//        }
//    }

    // Utility methods for BCD conversions

    /**
     * Convert number 0..99 to BCD byte
     */
    private static int num2bcd(int num) {
        if (num > 99 || num < 0)
            throw new RuntimeException("Number is too big for BCD");
        return ((num / 10) << 4) | (num % 10);
    }

    /**
     * Convert BCD byte to number 0..99
     */
    private static int bcd2num(int bcd) {
        int loNibble = bcd & 0xF;
        int hiNibble = (bcd >> 4);

        if (loNibble > 9 || hiNibble > 9)
            throw new RuntimeException("BCD number is invalid");
        return hiNibble * 10 + loNibble;
    }

}
