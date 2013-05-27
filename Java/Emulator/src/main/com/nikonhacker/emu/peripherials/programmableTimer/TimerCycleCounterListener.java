package com.nikonhacker.emu.peripherials.programmableTimer;

import com.nikonhacker.emu.CycleCounterListener;

import java.util.HashMap;
import java.util.Map;


public class TimerCycleCounterListener implements CycleCounterListener {

    public static final int NS_PER_CPU_CYCLE = 100; // Assume 10 MHz CPU clock => 100 ns/cycle

    Map<ProgrammableTimer, Long> timerMap = new HashMap<ProgrammableTimer, Long>();
    public boolean onCycleCountChange(long oldCount, int increment) {
        for (ProgrammableTimer programmableTimer : timerMap.keySet()) {
            Long cycleInterval = timerMap.get(programmableTimer);
            for (int i = 1; i <= increment; i++) {
                if ((oldCount + i) % cycleInterval == 0) {
                    programmableTimer.getTimerTask().run();
                }
            }
        }
        return true;
    }

    public void registerTimer(ProgrammableTimer timer, long intervalNs) {
        timerMap.put(timer, getIntervalInCycles(intervalNs));
    }

    private Long getIntervalInCycles(long intervalNs) {
        return intervalNs/NS_PER_CPU_CYCLE; // ns/(ns/cycle) = cycle
    }

    public void unregisterTimer(ProgrammableTimer timer) {
        timerMap.remove(timer);
    }

}
