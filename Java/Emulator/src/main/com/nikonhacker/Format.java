package com.nikonhacker;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;

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

    public static int parseHexField(JTextField textField) throws NumberFormatException {
        try {
            if (textField.getText().toLowerCase().startsWith("0x")) {
                textField.setText(textField.getText().substring(2));
            }
            long address = Long.parseLong(textField.getText(), 16);
            if (address < 0 || address > 0xFFFFFFFFL) {
                throw new NumberFormatException("Value out of range");
            }
            textField.setBackground(Color.WHITE);
            return (int) address;
        } catch (NumberFormatException e) {
            textField.setBackground(Color.RED);
            throw(e);
        }
    }

    public static int parseBinaryField(JTextField textField) throws NumberFormatException {
        try {
            if (textField.getText().toLowerCase().startsWith("0b")) {
                textField.setText(textField.getText().substring(2));
            }
            long address = Long.parseLong(textField.getText(), 2);
            if (address < 0 || address > 0xFFFFFFFFL){
                throw new NumberFormatException("Value out of range");
            }
            textField.setBackground(Color.WHITE);
            return (int) address;
        } catch (NumberFormatException e) {
            textField.setBackground(Color.RED);
            throw(e);
        }
    }
}
