package com.nikonhacker;

import com.nikonhacker.dfr.DATA;
import com.nikonhacker.dfr.OptionParsingException;
import com.nikonhacker.dfr.Range;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.StringTokenizer;

public class Format {

    static String memtypehelp =
            "Memtypes are:\n"
                    + "NONE              do not disassemble\n"
                    + "UNKNOWN           unknown contents\n"
                    + "CODE              disassemble as code where possible\n"
                    + "DATA[:spec]       disassemble as data; spec is up to 8 of:\n"
                    + "                    L -- long (32-bit) data\n"
                    + "                    N -- long (32-bit) data, no labels\n"
                    + "                    R -- rational\n"
                    + "                    V -- vector\n"
                    + "                    W -- word (16-bit) data\n"
            ;

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

    public static int parseIntHexField(JTextField textField) throws NumberFormatException {
        try {
            if (textField.getText().toLowerCase().startsWith("0x")) {
                textField.setText(textField.getText().substring(2));
            }
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

    /**
     * Parses the given string as an 32-bit integer
     * The number can be either decimal or hex (0x-prefixed) and can be followed by the K or M (case insensitive) multipliers
     * @param option the option that called this method
     * @param value the String to convert
     * @return the converted int, to be considered unsigned
     */
    public static int parseUnsigned(Character option, String value) throws OptionParsingException {
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
            throw new OptionParsingException(((option != null)?("-" + option + " "):"") + "Unrecognized value : " + value);
        }

        return (int) v;
    }

    /**
     * Parses a String of the form start-end=offset or start,length=offset
     * start, end and offset can be either decimal or hex (0x-prefixed) and can be followed by the K or M (case insensitive) multipliers
     * @param option the option that called this method
     * @param rangeString the String to parse
     * @return the converted Range
     */
    public static Range parseOffsetRange(char option, String rangeString) throws OptionParsingException {
        StringTokenizer rangeTokenizer = new StringTokenizer(rangeString.replace(":", "="), "-,=", true);
        if (rangeTokenizer.countTokens() != 5) {
            throw new OptionParsingException("-" + option + " has an malformed range : " + rangeString);
        }
        int start = parseUnsigned(option, rangeTokenizer.nextToken());

        String sep = rangeTokenizer.nextToken();

        int end = parseUnsigned(option, rangeTokenizer.nextToken()) + ("-".equals(sep)?0:start);

        String nextSep = rangeTokenizer.nextToken();

        if (!"=".equals(nextSep)) {
            throw new OptionParsingException("-" + option + " has an malformed range : " + rangeString + " (expected '=' or ':' before last address)");
        }

        int offset = parseUnsigned(option, rangeTokenizer.nextToken());
        return new Range(start, end, offset);
    }

    /**
     * Parses a String of the form start-end=datatype[:wordsize] or start,length=datatype[:wordsize]
     * start and end can be either decimal or hex (0x-prefixed) and can be followed by the K or M (case insensitive) multipliers
     * @param option the option that called this method
     * @param rangeString the String to parse
     * @return the converted Range
     */
    public static Range parseTypeRange(char option, String rangeString) throws OptionParsingException {
        StringTokenizer rangeTokenizer = new StringTokenizer(rangeString, ",-=", true);

        if (rangeTokenizer.countTokens() != 5) {
            throw new OptionParsingException("-" + option + " has a malformed range : " + rangeString);
        }

        int start = parseUnsigned(option, rangeTokenizer.nextToken());

        String sep = rangeTokenizer.nextToken();

        int end = parseUnsigned(option, rangeTokenizer.nextToken()) + ("-".equals(sep)?0:start);

        String nextSep = rangeTokenizer.nextToken();
        if (!"=".equals(nextSep)) {
            throw new OptionParsingException("-" + option + " has a malformed range : " + rangeString + " (expected '=' before last address)");
        }

        DATA map = parseMemtype(rangeTokenizer.nextToken());

        return new Range(start, end, map);
    }

    static DATA parseMemtype(String arg) throws OptionParsingException {
        if (StringUtils.isBlank(arg))  {
            throw new OptionParsingException("no memtype given");
        }

        DATA memp = new DATA();
        String wtf = "memory type";
        switch (arg.charAt(0))
        {
            case 'C':
            case 'c':
                memp.memType = DATA.MEMTYPE_CODE;
                break;

            case 'D':
            case 'd':
                wtf = "data type";
                memp.memType = DATA.MEMTYPE_DATA;
                memp.spec.add(DATA.SpecType_MD_WORD);

                int separator = arg.lastIndexOf(':');
                if (separator != -1)
                {
                    memp.spec.clear(); // remove above default of MD_WORD
                    separator++;
                    while (separator < arg.length())
                    {
                        char c = arg.charAt(separator++);

                        int md;
                        switch ((c + "").toLowerCase().charAt(0))
                        {
                            case 'l': md = DATA.SpecType_MD_LONG; break;
                            case 'n': md = DATA.SpecType_MD_LONGNUM; break;
                            case 'r': md = DATA.SpecType_MD_RATIONAL; break;
                            case 'v': md = DATA.SpecType_MD_VECTOR; break;
                            case 'w': md = DATA.SpecType_MD_WORD; break;
                            default:
                                throw new OptionParsingException("unrecognized " + wtf + " at '" + arg + "'\n" + memtypehelp + "'\n");
                        }
                        memp.spec.add(md);
                    }
                }
                break;

            case 'n':
            case 'N':
                memp.memType = DATA.MEMTYPE_NONE;
                break;

            case 'u':
            case 'U':
                memp.memType = DATA.MEMTYPE_UNKNOWN;
                break;

            case 'v':
            case 'V':
                memp.memType = DATA.MEMTYPE_DATA;
                memp.spec.add(DATA.SpecType_MD_VECTOR);
                break;

            default:
                throw new OptionParsingException("unrecognized " + wtf + " at '" + arg + "'\n" + memtypehelp + "\n");
        }

        return memp;
    }
}
