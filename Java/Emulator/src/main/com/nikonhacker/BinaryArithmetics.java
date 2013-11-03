package com.nikonhacker;

public class BinaryArithmetics {
    /**
     * Extend with negative sign
     * @param numBits the number of significant bits in value
     * @param value the original value
     */
    public static int negativeExtend(int numBits, int value) {
        int mask = (1 << numBits) - 1;
        return ~mask | value;
    }

    /**
     * Sign-extend
     * Interpret value as a signed number based on its last numBits bits, and extend the higher bits
     * so that the returned value represents the same number, but on 32 bits
     * @param numBits the number of significant bits in value
     * @param value the original value
     */
    public static int signExtend(int numBits, int value)
    {
        int shift = 32 - numBits;
        return value << shift >> shift;
    }

    /**
     * Returns the opposite of value, interpreting it on numBits bits
     * @param numBits the number of significant bits in value
     * @param value the original value
     */
    public static int neg(int numBits, int value)
    {
        return (-signExtend(numBits, value));
    }

    /**
     * Tests if a number is negative, interpreting it on numBits bits
     * @param numBits the number of significant bits in value
     * @param value the original value
     * @return
     */
    public static boolean isNegative(int numBits, int value)
    {
        return (value & (1 << (numBits - 1))) != 0;
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
