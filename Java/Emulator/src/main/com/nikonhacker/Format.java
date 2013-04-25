package com.nikonhacker;

import com.nikonhacker.disassembly.ParsingException;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;

public class Format {


    public static String asHex(int value, int nbChars) {
        return StringUtils.leftPad(Integer.toHexString(value).toUpperCase(), nbChars, '0');
    }


    public static String asHexInBitsLength(String prefix, int value, int nbBits) {
        if (nbBits <= 0) {
            return "";
        }
        return prefix + asHex(value, (nbBits - 1) / 4 + 1);
    }

    public static String asBinary(int value, int nbChars) {
        return StringUtils.leftPad(Integer.toBinaryString(value).toUpperCase(), nbChars, '0');
    }

    public static char asAscii(int c)
    {
        c &= 0xFF;
        if (c > 31 && c < 127) {
            return (char)c;
        }
        else {
            return '.';
        }
    }

    public static int parseIntHexField(JTextField textField) throws NumberFormatException {
        try {
            if (textField.getText().toLowerCase().startsWith("0x")) {
                textField.setText(textField.getText().substring(2));
            }
            textField.setText(textField.getText().replace(" ",""));
            long value = Long.parseLong(textField.getText(), 16);
            if (value < 0 || value > 0xFFFFFFFFL) {
                throw new NumberFormatException("Value out of range");
            }
            textField.setBackground(Color.WHITE);
            return (int) value;
        } catch (NumberFormatException e) {
            textField.setBackground(Color.RED);
            throw(e);
        }
    }

    public static int parseIntBinaryField(JTextField textField, boolean maskMode) throws NumberFormatException {
        try {
            if (textField.getText().toLowerCase().startsWith("0b")) {
                textField.setText(textField.getText().substring(2));
            }
            textField.setText(textField.getText().replace(" ",""));
            String text = textField.getText();
            if (maskMode) {
                text = text.replace('?', '0');
            }
            long value = Long.parseLong(text, 2);
            if (value < 0 || value > 0xFFFFFFFFL){
                throw new NumberFormatException("Value out of range");
            }
            textField.setBackground(Color.WHITE);
            return (int) value;
        } catch (NumberFormatException e) {
            textField.setBackground(Color.RED);
            throw(e);
        }
    }

    public static int parseUnsignedField(JTextField textField) throws ParsingException {
        try {
            int value = parseUnsigned(textField.getText());
            textField.setBackground(Color.WHITE);
            return value;
        } catch (ParsingException e) {
            textField.setBackground(Color.RED);
            throw(e);
        }
    }


    /**
     * Parses the given string as an 32-bit integer
     * The number can be either decimal or hex (0x-prefixed) and can be followed by the K or M (case insensitive) multipliers
     *
     * @param value the String to convert
     * @return the converted int, to be considered unsigned
     */
    public static int parseUnsigned(String value) throws ParsingException {
        boolean isHex = (value.length() > 2 && value.charAt(0) == '0' && (value.charAt(1) == 'x' || value.charAt(1) == 'X'));

        long v = 0;
        int i = isHex ? 2 : 0;
        for (; i < value.length(); i++)
        {
            char ch = value.charAt(i);
            if (isHex)
            {
                if (ch >= '0' && ch <= '9')
                    v = (v * 0x10) + ch - '0';
                else if (ch >= 'a' && ch <= 'f')
                    v = (v * 0x10) + ch - 'a' + 0x0a;
                else if (ch >= 'A' && ch <= 'F')
                    v = (v * 0x10) + ch - 'A' + 0x0a;
                else
                    break;
            }
            else
            {
                if (ch >= '0' && ch <= '9')
                    v = (v * 10) + ch - '0';
                else
                    break;
            }
        }

        if (i != value.length())
        {
            switch (value.charAt(i))
            {
                case 'k':
                case 'K':
                    v *= 1024;
                    i++;
                    break;
                case 'm':
                case 'M':
                    v *= 1048576;
                    i++;
                    break;
            }
        }

        if (i != value.length())
        {
            throw new ParsingException("Unrecognized value : " + value);
        }

        return (int) v;
    }


    // Following methods were copied from MARS' Binary utility class


    /**
     *  Returns int representing the bit values of the high order 32 bits of given
     *  64 bit long value.
     *   @param longValue The long value from which to extract bits.
     *   @return int containing high order 32 bits of argument
     **/

    public static int highOrderLongToInt(long longValue) {
        return (int) (longValue >> 32);  // high order 32 bits
    }


    /**
     *  Returns int representing the bit values of the low order 32 bits of given
     *  64 bit long value.
     *   @param longValue The long value from which to extract bits.
     *   @return int containing low order 32 bits of argument
     **/
    public static int lowOrderLongToInt(long longValue) {
        return (int) (longValue << 32 >> 32);  // low order 32 bits
    }

    /**
     *  Returns long (64 bit integer) combining the bit values of two given 32 bit
     *  integer values.
     *   @param highOrder Integer to form the high-order 32 bits of result.
     *   @param lowOrder Integer to form the high-order 32 bits of result.
     *   @return long containing concatenated 32 bit int values.
     **/
    public static long twoIntsToLong(int highOrder, int lowOrder) {
        return (((long)highOrder) << 32) | (((long)lowOrder) & 0xFFFFFFFFL);
    }



    /**
     *  Returns the bit value of the given bit position of the given int value.
     *   @param value The value to read the bit from.
     *   @param bit bit position in range 0 (least significant) to 31 (most)
     *   @return 0 if the bit position contains 0, and 1 otherwise.
     **/

    public static int bitValue(int value, int bit) {
        return 1 & (value >> bit);
    }


    /**
     *  Returns the bit value of the given bit position of the given long value.
     *   @param value The value to read the bit from.
     *   @param bit bit position in range 0 (least significant) to 63 (most)
     *   @return 0 if the bit position contains 0, and 1 otherwise.
     **/

    public static int bitValue(long value, int bit) {

        return (int) (1L & (value >> bit));
    }

    /**
     *  Sets the specified bit of the specified value to 1, and returns the result.
     *   @param value The value in which the bit is to be set.
     *   @param bit bit position in range 0 (least significant) to 31 (most)
     *   @return value possibly modified with given bit set to 1.
     **/

    public static int setBit(int value, int bit) {
        return value | ( 1 << bit) ;
    }


    /**
     *  Sets the specified bit of the specified value to 0, and returns the result.
     *   @param value The value in which the bit is to be set.
     *   @param bit bit position in range 0 (least significant) to 31 (most)
     *   @return value possibly modified with given bit set to 0.
     **/

    public static int clearBit(int value, int bit) {
        return value &  ~(1 << bit);
    }


    public static boolean isBitSet(int value, int bit) {
        return (value & (1 << bit)) != 0;
    }

    // setByte and getByte added by DPS on 12 July 2006

    /**
     *  Sets the specified byte of the specified value to the low order 8 bits of
     *  specified replacement value, and returns the result.
     *   @param value The value in which the byte is to be set.
     *   @param position byte position in range 0 (least significant) to 3 (most)
     *   @param replace value to place into that byte position - use low order 8 bits
     *   @return value modified value.
     **/

    public static int setByte(int value, int position, int replace) {
        return value & ~(0xFF << (position<<3)) | ((replace & 0xFF) << (position<<3)) ;
    }


    /**
     *  Gets the specified byte of the specified value.
     *   @param value The value in which the byte is to be retrieved.
     *   @param position byte position in range 0 (least significant) to 3 (most)
     *   @return zero-extended byte value in low order byte.
     **/

    public static int getByte(int value, int position) {
        return value << ((3-position)<<3) >>> 24;
    }
}
