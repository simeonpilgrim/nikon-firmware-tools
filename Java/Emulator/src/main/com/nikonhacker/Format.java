package com.nikonhacker;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

public class Format {

    public static String asHex(int value, int nbChars) {
        return StringUtils.leftPad(Integer.toHexString(value).toUpperCase(), nbChars, '0');
    }

    public static String asHexInBitsLength(int value, int nbBits) {
        return asHex(value, (nbBits - 1) / 4 + 1);
    }

    public static String asBinary(int value, int nbChars) {
        return StringUtils.leftPad(Integer.toBinaryString(value).toUpperCase(), nbChars, '0');
    }

    static char asAscii(int c)
    {
        c &= 0xFF;
        if (c > 31 && c < 127) {
            return (char)c;
        }
        else {
            return '.';
        }
    }

    public static int parseHexField(JTextField addressField) throws NumberFormatException {
        if (addressField.getText().toLowerCase().startsWith("0x")) {
            addressField.setText(addressField.getText().substring(2));
        }
        long address = Long.parseLong(addressField.getText(), 16);
        if (address >=0 && address <= 0xFFFFFFFFL){
            return (int) address;
        }
        throw new NumberFormatException("Address out of range");
    }
}
