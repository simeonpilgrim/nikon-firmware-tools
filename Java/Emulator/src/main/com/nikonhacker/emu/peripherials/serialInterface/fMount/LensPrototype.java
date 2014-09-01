package com.nikonhacker.emu.peripherials.serialInterface.fMount;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.emu.MasterClock;
import com.nikonhacker.emu.Clockable;
import com.nikonhacker.emu.peripherials.serialInterface.fMount.LensDevice;
import com.nikonhacker.emu.peripherials.serialInterface.fMount.FMountCircuit;

/*
    This is just some commands emulation. There is no particular lens type
    emulated, but most info comes from 35mm 1.8 G

 */
public class LensPrototype extends LensDevice implements Clockable {

    private static final int SLOW_BAUD_RATE = 192307/2;    // Bps
    private static final int FAST_BAUD_RATE = 312500/2;    // Bps

    private MasterClock masterClock;
    private boolean isTimerActive;

    // set if lens was disconnected from mount
    private boolean unpluged;

    private enum State {
        IDLE,
        MOUNT_ASSERT_PIN2,
        RECEIVE_COMMAND,
        SEND_BYTES,
        RECEIVE_BYTES
    }

    private State state = State.IDLE;

    // emulated time of last action from mount
    private long lastAccessTime;

    private int baudRate = SLOW_BAUD_RATE; // Bps

    // TODO workaround for MasterClock.prepareSchedule() broken calculations
    // (setting too low frequency results very low speed (5cps), so use higher frequency with counter)
    private int timerCount;

    // only set if command needs several actions
    private int currentCommand = -1;
    private int transferCount;
    private int[] transferData;

    private int[] command40Data = {0x00, 0x00, 0x01, 0x04, 0x00, 0x00, 0x01};
    private int[] command41Data = {0, 0};
    private int[] command28Data = {0x10, 0x14, 0xff, 0xff, 0xcc, 0xfa, 0x07, 0x03,
                                   0x03, 0x8d, 0x04, 0x00, 0x40, 0x6c, 0xf9, 0xfb,
                                   0x44, 0x14, 0x9f, 0x58, 0x44, 0x44, 0x14, 0x14,
                                   0xa1, 0x14, 0x00, 0x13, 0x5e, 0x08, 0x07, 0xf8,
                                   0x06, 0x43, 0x2b, 0x44, 0x00, 0x01, 0x00, 0xfa,
                                   0x00, 0x00, 0x45, 0x04};
    private int[] commandC2Data = {0x08, 0x00, 0x00, 0x00};
    private int[] commandE7Data = {0};
    private int[] commandEAData = {0};

    public LensPrototype (FMountCircuit fMountCircuit, MasterClock masterClock) {
        super(fMountCircuit);
        this.masterClock = masterClock;
    }

    public String toString() {
        return "Lens Prototype";
    }

    // ------------------------ Methods called from FMountCircuit (safe by design)
    /**
        Mount write byte serially (called from FMountCircuit)
     */
    @Override
    public void write(Integer value) {
        if (testCommandAborted())
            return;
        // if command byte
        switch (state) {
            case RECEIVE_COMMAND:
                // analyse command code
                currentCommand = value;
                switch (value) {
                    case 0x28:  // get status
                        state = State.SEND_BYTES;
                        transferCount = command28Data.length;
                        transferData = command28Data;
                        break;
                    case 0x40:  // get capabilities, set extra params
                        state = State.SEND_BYTES;
                        transferCount = command40Data.length;
                        transferData = command40Data;
                        break;
                    case 0x41:  // get extra params
                        state = State.SEND_BYTES;
                        transferCount = command41Data.length;
                        transferData = command41Data;
                        break;
                    case 0xC2:
                        state = State.SEND_BYTES;
                        transferCount = commandC2Data.length;
                        transferData = commandC2Data;
                        break;
                    case 0xEA:
                        state = State.RECEIVE_BYTES;
                        transferCount = commandEAData.length;
                        transferData = commandEAData;
                        break;
                    case 0xE7:
                        state = State.RECEIVE_BYTES;
                        transferCount = commandE7Data.length;
                        transferData = commandE7Data;
                        break;
                    default:
                        currentCommand = 0;
                        System.out.println("Lens do not support command:"+Format.asHex(value&0xFF, 2));
                        // just do nothing and camera consider this command as aborted after 5ms
                }
                if (transferCount!=0) {
                    // aprox 177us
                    timerCount = (baudRate==FAST_BAUD_RATE ? 28 : 17);
                    masterClock.add(this, -1, true, false);
                }
                break;
            case RECEIVE_BYTES:
                transferData[transferData.length - (transferCount--)] = value;
                if (transferCount!=0) {
                    // aprox 30us @156 KBps, 72us @ 96 KBps
                    timerCount = (baudRate==FAST_BAUD_RATE ? 5 : 7);
                    masterClock.add(this, -1, true, false);
                } else {
                    // if command 0x40 was successfull, set new baud rate
                    if (currentCommand==0x40)
                        baudRate = FAST_BAUD_RATE;    // Bps
                    state = State.IDLE;
                }
        }
        fMountCircuit.setPin2Value(1);
    }

    /**
        Mount drives Pin2 line state (called from FMountCircuit)
        WARNING: called also if lens keeps this line asserted for easy emulation. But in real life not !
     */
    public void setPin2Value(int value) {
        testCommandAborted();
        switch(state) {
            case IDLE: // command start: Pin2 assertion
                if (value==0) {
                    state = State.MOUNT_ASSERT_PIN2;
                }
                break;
            case MOUNT_ASSERT_PIN2: // command start: end of Pin2 assertion
                if (value == 1) {
                    state = State.RECEIVE_COMMAND;
                    fMountCircuit.setPin2Value(0);
                }
                break;
            default:
                // ignore pin pulling high, because real lens can't measure this
                if (value==0) {
                    // mount asserts pin low during command execution - this could only happens on new command byte
                    // coderat: error! I can only guess what lens would do
                    fMountCircuit.setPin2Value(1);
                    state = State.IDLE;
                }
        }
    }

    /**
        Mount reads byte serially (called from FMountCircuit)
     */
    public Integer read() {
        if (testCommandAborted())
            return null;

        if (state==State.SEND_BYTES) {
            final int ch = transferData[transferData.length - (transferCount--)];
            if (transferCount==0) {
                if (currentCommand==0x40) {
                    // special case: we get 2 additionaly bytes from D5100: 02 1A, from D800: 02 1B
                    state = State.RECEIVE_BYTES;
                    transferCount = command41Data.length;
                    transferData = command41Data;
                } else
                    state = State.IDLE;
            }
            if (transferCount!=0) {
                    // aprox 30us @156 KBps, 72us @ 96 KBps
                    timerCount = (baudRate==FAST_BAUD_RATE ? 5 : 7);
                masterClock.add(this, -1, true, false);
            }
            fMountCircuit.setPin2Value(1);
            return ch;
        }
        return null;
    }

    /**
        Lens was unplugged (called from FMountCircuit; can be called from other UI thread)
     */
    @Override
    public synchronized void unplug() {
        // if there is timer, it will be disposed later
        unpluged = true;
    }

    // ------------------------ Timer callback is synchron (called from same thread by design) with other methods except dispose()
    @Override
    public synchronized Object onClockTick() throws Exception {
        if (unpluged || state==State.IDLE) {
            masterClock.remove(this);
            return null;
        }
        if ( (--timerCount)>0)
            return null;
        fMountCircuit.setPin2Value(0);
        masterClock.remove(this);
        return null;
    }

    @Override
    public int getFrequencyHz() {
        // interval for 4 bits
        return baudRate;
    }

    @Override
    public int getChip() {
        return Constants.CHIP_TX;
    }

    private boolean testCommandAborted() {
        boolean isAbort = false;
        final long newTime = masterClock.getTotalElapsedTimePs();

        // if command started
        if (state != State.IDLE) {
            // check if mount didn't act for long time
            if ((newTime-lastAccessTime)>=5*MasterClock.PS_PER_MS) {
                System.out.println("Lens abort condition detected");
                // timeout, restart everything
                state = State.IDLE;
                transferCount = 0;
                transferData = null;
                currentCommand = -1;
                fMountCircuit.setPin2Value(1);
                isAbort = true;
            }
        }
        lastAccessTime = newTime;
        return isAbort;
    }
}
