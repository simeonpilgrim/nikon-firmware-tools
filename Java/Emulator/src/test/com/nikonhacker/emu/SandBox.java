package com.nikonhacker.emu;

public class SandBox {

    public final static int SSP = 18;
    public final static int USP = 19;

    public static void main(String[] args) {
        //registerSharingTest();
        produceReversedHexTable();
    }

    private static void produceReversedHexTable() {
        for (int i = 255; i >=0; i--) {
            System.out.println((i<16?"0":"") + Integer.toHexString(i) + " (" + i + ")");
        }
    }

    private static void registerSharingTest() {
        Register32 regValue[] = new Register32[48];
        for (int i = 0; i < regValue.length; i++) {
            regValue[i] = new Register32(0);
        }
        regValue[15] = regValue[SSP];

        regValue[SSP].setValue(1);
        regValue[USP].setValue(2);

        System.out.println("R15=" + regValue[15] + ", SSP=" + regValue[SSP] + ", USP=" + regValue[USP]);

        regValue[15].setValue(3);
        System.out.println("R15=" + regValue[15] + ", SSP=" + regValue[SSP] + ", USP=" + regValue[USP]);

        regValue[15] = regValue[USP];
        System.out.println("R15=" + regValue[15] + ", SSP=" + regValue[SSP] + ", USP=" + regValue[USP]);
    }

    private static class Register32 {
        int value;

        private Register32(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "" + value;
        }
    }
    
    
}
