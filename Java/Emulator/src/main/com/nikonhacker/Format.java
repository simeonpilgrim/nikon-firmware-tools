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

}
