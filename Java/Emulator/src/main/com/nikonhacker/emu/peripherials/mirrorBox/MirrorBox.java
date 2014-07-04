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
    private Pin mirrorLock;
    private Pin mirrorX1;

    // TODO it is WORKAROUND because MasterClock doesn't work with low clock frequencies!!!
    private static final int CLOCK_FREQ = 100000;
    private int timerCount;

    // states for shooting photo movement only!
    private static final int[] wipersStates = {0b111, 0b110, 0b100, 0b000};
    // in ms
    private static final int[] wipersTimes = {0, 18, 30, 33, 0};
    private int state;
    private int mirrorMovement;
    private boolean isMoving;
    private int     timeFromPuls;

    public MirrorBox(Platform platform) {
        this.platform = platform;
        state = 0;

        for (int i=0; i<3; i++) {
            wipers[i] = new PulledOutputPin(this.getClass().getSimpleName() + " WIPER" + i + " pin", (wipersStates[0]>>(2-i))&1);
        }
        mirrorX1 = new Pin(this.getClass().getSimpleName() + " X1 pin");
        mirrorMove = new Pin(this.getClass().getSimpleName() + " MOVE pin") {
            @Override
            public final void setInputValue(int value) {
                setMovePin(value);
            }
        };
        mirrorDir = new Pin(this.getClass().getSimpleName() + " DIR pin") {
            @Override
            public final void setInputValue(int value) {
                setDirPin(value);
            }
        };
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

    public final Pin getX1Pin() {
        return mirrorX1;
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

        if (timeFromPuls!=0) {
            setClockInterval(timeFromPuls);
            timeFromPuls = 0;
            // in emulator puls width can be
            mirrorX1.setOutputValue(1);
            mirrorX1.setOutputValue(0);
            return null;
        }
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

    // ms
    private final void setClockInterval(int t) {
        if (t!=0) {
            timerCount = t*CLOCK_FREQ/1000;
        } else {
            timerCount = 0;
        }
    }

    private final void setMirrorMovement(int value, boolean inClock) {
        if (value != mirrorMovement || inClock) {
            mirrorMovement = value;

            if (mirrorMovement>0) {

                if (wipersTimes[state+1]!=0) {

                    if (1==state) {
                        setClockInterval(1);
                        timeFromPuls = wipersTimes[state+1] - 1;
                    } else
                        setClockInterval(wipersTimes[state+1]);
                    // if first time
                    if (!inClock)
                        platform.getMasterClock().add(this, -1, true, false);
                } else {

                    // move not possible, so stop (in reality still spins due to inertion/torque)
                    // Mirror brake phase is short(~5-10ms), so the emulation will stay in last state
                    setClockInterval(0);
                    if (inClock) {
                        platform.getMasterClock().remove(this);
                    }
                }

            } else if (mirrorMovement<0) {

                if (wipersTimes[state]!=0) {

                    // if highest state
                    if (wipersStates.length-1 == state) {
                        setClockInterval(6);
                        timeFromPuls = wipersTimes[state] - 6;

                    // seems same pulse as on going up
                    } else if (state==2) {
                        setClockInterval(wipersTimes[state+1] - 1);
                        timeFromPuls = 1;
                    } else
                        setClockInterval(wipersTimes[state]);
                    // if first time
                    if (!inClock)
                        platform.getMasterClock().add(this, -1, true, false);
                } else {

                    // move not possible, so stop
                    setClockInterval(0);
                    if (inClock) {
                        platform.getMasterClock().remove(this);
                    }
                }
            } else {

                setClockInterval(0);
                timeFromPuls = 0;
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

    private final void setMovePin(int value) {
        if (value == 0) {   // stop
            isMoving = false;
            setMirrorMovement(0, false);
        } else {
            isMoving = true;
        }
    }

    private final void setDirPin(int value) {
        if (isMoving) {
            setMirrorMovement( (value == 1 ? -1 : 1), false);
            isMoving = false;
        }
    }
}
