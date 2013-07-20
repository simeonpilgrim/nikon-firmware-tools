package com.nikonhacker.emu;

import junit.framework.TestCase;

public class MasterClockTest  extends TestCase {
    public void testClockable1() throws Exception {
        MasterClock masterClock = new MasterClock();
        TestClockable d1 = new TestClockable("1", 80000000);
        TestClockable d2 = new TestClockable("2", 132000000);

        masterClock.add(d1, d1, true);
        masterClock.add(d2, d2, true);
        new Thread(masterClock).start();

        Thread.sleep(5000);
    }

    public void testClockable2() throws Exception {
        MasterClock masterClock = new MasterClock();
        TestClockable d1 = new TestClockable("1", 80000000);
        TestClockable d2 = new TestClockable("2", 132000000);

        masterClock.add(d1, d1, true);
        masterClock.add(d2, d2, false);
        new Thread(masterClock).start();
        Thread.sleep(100);
        masterClock.setEnabled(d2, true);
        Thread.sleep(5000);
    }

    private static class TestClockable implements Clockable, ClockableCallbackHandler {
        private String name;
        private int frequencyHz;
        private int runs = 0;
        private int maxRuns = 1000;

        public TestClockable(String name, int frequencyHz) {
            this.name = name;
            this.frequencyHz = frequencyHz;
        }

        public int getFrequencyHz() {
            return frequencyHz;
        }

        public Object onClockTick() {
            System.out.print(name);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {}
            runs++;
            return (runs < maxRuns)?null:new Object();
        }

        @Override
        public void onNormalExit(Object o) {
            System.out.println("[STOP" + name + "]");
        }

        @Override
        public void onException(Exception e) {
            System.out.println("[EXC." + name + "]");
        }
    }
}
