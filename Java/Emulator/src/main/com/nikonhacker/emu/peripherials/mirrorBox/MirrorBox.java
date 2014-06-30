package com.nikonhacker.emu.peripherials.mirrorBox;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.Clockable;
import com.nikonhacker.emu.peripherials.ioPort.Pin;
import com.nikonhacker.emu.peripherials.ioPort.util.PulledOutputPin;
import com.nikonhacker.emu.Platform;

public class MirrorBox implements Clockable {

    Platform platform;

    private Pin[] wipers = new PulledOutputPin[3];
    private Pin mirrorMove;
    private Pin mirrorDir;

    // TODO it is WORKAROUND because MasterClock doesn't work with low clock frequencies!!!
    private static final int CLOCK_FREQ = 100000;
    private int timerCount;

    // states for shooting photo movement only!
    private static final int[] wipersStates = {0b111, 0b110, 0b100, 0b000};
    // in tenth ms
    private static final int[] wipersTimes = {0, 70, 40, 70, 0};
    private int state;
    private int mirrorMovement;
    private boolean isMoving;

    public MirrorBox(Platform platform) {
        this.platform = platform;
        state = 0;

        for (int i=0; i<3; i++) {
            wipers[i] = new PulledOutputPin(this.getClass().getSimpleName() + " WIPER" + i + " pin", (wipersStates[0]>>(2-i))&1);
        }
        mirrorMove = new FromTXPin(this.getClass().getSimpleName() + " MOVE pin",0);
        mirrorDir = new FromTXPin(this.getClass().getSimpleName() + " DIR pin",1);
    }

    public final Pin getWiperPin(int num) {
        return wipers[num];
    }

    public final Pin getMovePin() {
        return mirrorMove;
    }

    public final Pin getDirPin() {
        return mirrorDir;
    }
    @Override
    public int getFrequencyHz() {
        return CLOCK_FREQ;
    }

    @Override
    public int getChip() {
        return Constants.CHIP_TX;
    }

    @Override
    public Object onClockTick() throws Exception {
        if ( (--timerCount)>0)
            return null;

        if (mirrorMovement==0)
            return null;

        if (mirrorMovement>0) {
            setWipers (state+1);
            state++;
        } else if (mirrorMovement<0) {
            setWipers (state-1);
            state--;
        }
        setMirrorMovement(mirrorMovement, true);
        return null;
    }

    // tenth ms
    private final void setClockInterval(int t) {
        if (t!=0) {
            timerCount = t*CLOCK_FREQ/10000;
        } else {
            timerCount = 0;
        }
    }

    private final void setMirrorMovement(int value, boolean inClock) {
        if (value != mirrorMovement || inClock) {
            mirrorMovement = value;

            if (mirrorMovement>0) {
                if (wipersTimes[state+1]!=0) {
                    setClockInterval(wipersTimes[state+1]);
                    // if first time
                    if (!inClock)
                        platform.getMasterClock().add(this, null, true, false);
                } else {
                    // move not possible, so stop (in reality still spins due to inertion/torque)
                    setClockInterval(0);
                    if (inClock) {
                        platform.getMasterClock().remove(this);
                    }
                }
            } else if (mirrorMovement<0) {
                if (wipersTimes[state]!=0) {
                    setClockInterval(wipersTimes[state]);
                    // if first time
                    if (!inClock)
                        platform.getMasterClock().add(this, null, true, false);
                } else {
                    // move not possible, so stop
                    setClockInterval(0);
                    if (inClock) {
                        platform.getMasterClock().remove(this);
                    }
                }
            } else {
                setClockInterval(0);
                platform.getMasterClock().remove(this);
            }
        }
    }

    private final void setWipers (int n) {
        n = wipersStates[n];
        int changes = n ^ wipersStates[state];

        for (int i = 2; changes!=0 ; changes>>=1, n>>=1, i--) {
            if ((changes & 1)!=0)
                wipers[i].setOutputValue(n&1);
        }
    }

    private final void setPin(int id, int value) {

        if (id == 0) { // movement
            if (value == 0) {   // stop
                isMoving = false;
                setMirrorMovement(0, false);
            } else {
                isMoving = true;
            }
        } else if (isMoving) {
            setMirrorMovement( (value == 1 ? -1 : 1), false);
            isMoving = false;
        }
    }
    // ------------------------ Classes for TX connected pins
    private class FromTXPin extends Pin {
        final int id;

        public FromTXPin(String name, int id) {
            super(name);
            this.id = id;
        }

        // only pass value
        @Override
        public final void setInputValue(int value) {
            setPin(id, value);
        }
    }

}
