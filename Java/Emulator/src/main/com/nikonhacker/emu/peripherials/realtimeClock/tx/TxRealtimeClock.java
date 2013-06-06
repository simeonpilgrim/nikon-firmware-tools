package com.nikonhacker.emu.peripherials.realtimeClock.tx;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.peripherials.realtimeClock.RealtimeClock;

import java.util.Calendar;

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

public class TxRealtimeClock implements RealtimeClock {
    private Platform platform;
    private Prefs prefs;
    
    private int pager;
    private int restr;
    
    private int timer[] = new int[2];
    private int dater[] = new int[2];
    
    private long diffClock;		// in ms

    public TxRealtimeClock(Platform platform, Prefs prefs) {
        this.platform = platform;
        this.prefs = prefs;
        restr = 0xC7;
        updateClock();
        dater[1]=0x100;
    }

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


    public Platform getPlatform() {
        return platform;
    }

    public Prefs getPrefs() {
        return prefs;
    }

    /**
	 * Get emulator RTC value
     */
	private Calendar getClock() {
        Calendar calendar = Calendar.getInstance();
        int hour = bcd2num((timer[0] >> 16) & 0xFF);

        if ((dater[1] & 0x10000) == 0) {
            // convert am/pm back
            hour = (hour % 20) + (hour >= 20 ? 12 : 0);
        }

        calendar.set(2000 + bcd2num((dater[0] >> 24) & 0xFF),
                bcd2num((dater[0] >> 16) & 0xFF) - 1,
                bcd2num((dater[0] >> 8) & 0xFF),
                hour,
                bcd2num((timer[0] >> 8) & 0xFF),
                bcd2num(timer[0] & 0xFF));

        return calendar;
    }

	/**
	 * Set emulator RTC value
     */
	private void setClock(Calendar value) {
        int hour = value.get(Calendar.HOUR_OF_DAY);

        if ((dater[1] & 0x10000) == 0) {
            // convert am/pm
            hour = (hour % 12) + (hour >= 12 ? 20 : 0);
        }
        timer[0] = (num2bcd(hour) << 16) |
                   (num2bcd(value.get(Calendar.MINUTE)) << 8) |
                    num2bcd(value.get(Calendar.SECOND));
        dater[0] = (num2bcd(value.get(Calendar.YEAR) % 100)   << 24) |
                   (num2bcd(value.get(Calendar.MONTH) + 1)    << 16) |
                   (num2bcd(value.get(Calendar.DAY_OF_MONTH)) << 8) |
                    num2bcd(value.get(Calendar.DAY_OF_WEEK) - 1);
    }

	/**
	 * Update emulator RTC based on host PC clock
     */
	private void updateClock() {
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeInMillis(System.currentTimeMillis() + diffClock);
        setClock(rightNow);
    }

	/**
	 * New clock value set, compute difference compared to host PC clock
     */
	private void updateDiffClock() {
        long hostTime = System.currentTimeMillis();
        Calendar clock = getClock();
        diffClock = clock.getTimeInMillis() - hostTime;
    }


    // --------------------- Read ---------------------


    public int getTimeReg() {
        if ((pager & 1) == 0) {
            if ((pager & 0b1000) != 0) {
                updateClock();
            }
        }
        return timer[pager & 1];
    }

    public int getDateReg() {
        if ((pager & 1) == 0) {
            if ((pager & 0b1000) != 0) {
                updateClock();
            }
        }
        return dater[pager & 1];
    }

    public int getPager() {
        return pager;
    }

    public int getRestr() {
        return restr;
    }


    // --------------------- Write ---------------------


    public void setTimeReg(int value) {
        if ((pager & 1) != 0) {
            if (bcd2num(value & 0xFF) > 59)
                throw new RuntimeException("RTC second is invalid");
        }
        else {
            // not used
            value &= 0xFFFFFF00;
        }

        if (bcd2num((value >> 8) & 0xFF) > 59)
            throw new RuntimeException("RTC minute is invalid");

        if ((dater[1] & 0x10000) == 0) {
            // convert am/pm
            int hour = bcd2num((value >> 16) & 0xFF);
            if ((hour > 11 && hour < 20) || (hour > 31))
                throw new RuntimeException("RTC hour is invalid");
        }
        else {
            if (bcd2num((value >> 16) & 0xFF) > 23)
                throw new RuntimeException("RTC hour is invalid");
        }

        timer[pager & 1] = value;

        if ((pager & 1) == 0) {
            updateDiffClock();
        }
    }

    public void setDateReg(int value) {
        int i;

        i = bcd2num((value >> 8) & 0xFF);
        // HACK: it seems D5100 Firmware 1.01 sets the day of month to 0 at 0xBFC18B42
        // it is not allowed, so...
        if (i == 0) {
            i = 1;
            value |= 0x100;
        }
        // HACK END
        if (i < 1 || i > 31)
            throw new RuntimeException("RTC day of month is invalid");

        if (bcd2num(value & 0xFF) > 6)
            throw new RuntimeException("RTC day of week is invalid");

        if ((pager & 1) == 0) {
            if (bcd2num((value >> 24) & 0xFF) > 99)
                throw new RuntimeException("RTC year is invalid");

            i = bcd2num((value >> 16) & 0xFF);
            // HACK: it seems D5100 Firmware 1.01 sets the month to 0 at 0xBFC18C20
            // it is not allowed, so...
            if (i == 0) {
                i = 1;
                value |= 0x10000;
            }
            // HACK END
            if (i < 1 || i > 12)
                throw new RuntimeException("RTC month is invalid");
        }
        else {
            // not used
            value &= 0x0301FFFF;
            // if changing hour format then convert
            if ((value & 0x10000) != (dater[1] & 0x10000)) {
                i = bcd2num((timer[0] >> 16) & 0xFF);
                if ((value & 0x10000) != 0) {
                    // new 24h, old am/pm
                    i = (i >= 20 ? (i - 8) : i);
                }
                else {
                    // new am/pm, old 24h
                    i = (i >= 12 ? (i + 8) : i);
                }
                timer[0] = ((timer[0] & 0xFF00FFFF) | (num2bcd(i) << 16));
            }
        }

        dater[pager & 1] = value;

        if ((pager & 1) == 0) {
            updateDiffClock();
        }
    }

    public void setHourr(byte value) {
        setTimeReg((timer[pager & 1] & 0xFF00FFFF) | (value << 16));
    }

    public void setMinr(byte value) {
        setTimeReg((timer[pager & 1] & 0xFFFF00FF) | (value << 8));
    }

    public void setSecr(byte value) {
        setTimeReg((timer[pager & 1] & 0xFFFFFF00) | value);
    }

    public void setYearr(byte value) {
        setDateReg((dater[pager & 1] & 0xFFFFFF) | (value << 24));
    }

    public void setMonthr(byte value) {
        setDateReg((dater[pager & 1] & 0xFF00FFFF) | (value << 16));
    }

    public void setDater(byte value) {
        setDateReg((dater[pager & 1] & 0xFFFF00FF) | (value << 8));
    }

    public void setDayr(byte value) {
        setDateReg((dater[pager & 1] & 0xFFFFFF00) | value);
    }

    public void setPager(int value) {
        // ignored fields
        value &= 0b10011101;
        if ((value & 0b10000100) != 0) {
            // TODO
            throw new RuntimeException("RTC interrupt/alarm are not implemented");
        }
        if ((value & 0b10000) != 0) {
            // do seconds rounding
            Calendar adjTime = getClock();
            // if >29
            if ((timer[0] & 0xFF) > 0x29) {
                adjTime.add(Calendar.MINUTE, 1);
            }
            adjTime.set(Calendar.SECOND, 0);
            setClock(adjTime);
            value &= 0b11101111;
        }
        pager = value;
    }

    public void setRestr(int value) {
        // ignored fields
        value &= 0b11110111;
        restr = value;
        // TODO reset clock, alarm register
    }
}
