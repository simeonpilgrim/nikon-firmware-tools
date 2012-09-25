package com.nikonhacker;

public class BinaryArithmetics {
    /**
     * Extend with Negative sign
     * @param i number of used bits in original number
     * @param x original number
     * @return
     */
    public static int extn(int i, int x) {
        int mask = (1 << i) - 1;
        return ~mask | x;
    }

    public static int signExtendMask(int b, int x)
    {
        return ((-b) * ((b & x) != 0 ? 1 : 0));
    }

    /**
     * Interpret value as a signed value based on its last numBits bits, and extend the higher bits
     * so that return represents the same number, but on 32 bits
     * @param numBits the number of bits to take into account
     * @param value the original number
     */
    public static int signExtend(int numBits, int value)
    {
        return (value | signExtendMask((1 << (numBits - 1)), value));
    }

    public static int NEG(int n, int x)
    {
        return (-signExtend(n, x));
    }

    public static boolean IsNeg(int n, int x)
    {
        return (x & (1 << (n - 1))) != 0;
    }

    /**
     * See http://www.javamex.com/java_equivalents/unsigned_arithmetic.shtml for explanations
     * @param n1
     * @param n2
     * @return true if n1<n2, unsigned
     */
    public static boolean isLessThanUnsigned(int n1, int n2) {
        return (n1 < n2) ^ ((n1 < 0) != (n2 < 0));
    }

    /**
     *
     * @param n1
     * @param n2
     * @return true if n1>n2, unsigned
     */
    public static boolean isGreaterThanUnsigned(int n1, int n2) {
        return (n1 > n2) ^ ((n1 < 0) != (n2 < 0));
    }

}
