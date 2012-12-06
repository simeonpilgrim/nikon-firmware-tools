package com.nikonhacker.disassembly;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.fr.InterruptVectorRange;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class OptionHandler
{
    static String memtypehelp =
            "Memtypes are:\n"
                    + "NONE              do not disassemble\n"
                    + "UNKNOWN           unknown contents\n"
                    + "CODE[:width]      disassemble as code where possible. Where applicable, width is one of:\n"
                    + "                    L or 32 -- 32-bit instructions\n"
                    + "                    W or 16 -- 16-bit instructions\n"
                    + "DATA[:width]      disassemble as data; width is up to 8 of:\n"
                    + "                    L or 32 -- long (32-bit) data\n"
                    + "                    N       -- long (32-bit) data, no labels\n"
                    + "                    R       -- rational\n"
                    + "                    V       -- vector\n"
                    + "                    W or 16 -- word (16-bit) data\n"
            ;

    int index;
    String[] arguments;
    String currentToken;

    public OptionHandler(String[] arguments)
    {
        index = 0;
        this.arguments = arguments;
        currentToken = null;
    }


    public Character getNextOption() {
        if (StringUtils.isBlank(currentToken)) {
            if (index >= arguments.length) return null; // end of list

            currentToken = arguments[index++]; // read next

            if (currentToken.charAt(0) != '-') return 0; // not an option (input filename)

            currentToken = currentToken.substring(1); // remove the dash 
        }
        char c = currentToken.charAt(0); // get the option
        currentToken = currentToken.substring(1); // to allow options such as -d2 . Now currentArg = 2
        return c;
    }

    public String getArgument() {
        if (StringUtils.isBlank(currentToken)) {
            if (index >= arguments.length) return null; // end of list

            currentToken = arguments[index++]; // read next
        }
        String argument = currentToken; // get the argument
        currentToken = null; // clear current to force a read at next call
        return argument;
    }


    /**
     * Parses a String of the form start-end=offset or start,length=offset
     * start, end and offset can be either decimal or hex (0x-prefixed) and can be followed by the K or M (case insensitive) multipliers
     * @param option the option that called this method
     * @param rangeString the String to parse
     * @return the converted Range
     */
    public static Range parseOffsetRange(char option, String rangeString) throws ParsingException {
        StringTokenizer rangeTokenizer = new StringTokenizer(rangeString.replace(":", "="), "-,=", true);
        if (rangeTokenizer.countTokens() != 5) {
            throw new ParsingException("-" + option + " has an malformed range : " + rangeString);
        }
        int start = Format.parseUnsigned(rangeTokenizer.nextToken());

        String sep = rangeTokenizer.nextToken();

        int end = Format.parseUnsigned(rangeTokenizer.nextToken()) + ("-".equals(sep)?0:start);

        String nextSep = rangeTokenizer.nextToken();

        if (!"=".equals(nextSep)) {
            throw new ParsingException("-" + option + " has an malformed range : " + rangeString + " (expected '=' or ':' before last address)");
        }

        int offset = Format.parseUnsigned(rangeTokenizer.nextToken());
        return new Range(start, end, offset);
    }

    /**
     * Parses a String of the form start-end=type[:width] or start,length=type[:width]
     * start and end can be either decimal or hex (0x-prefixed) and can be followed by the K or M (case insensitive) multipliers
     * @param option the option that called this method
     * @param rangeString the String to parse
     * @return the converted Range
     * @see RangeType for values of type and width
     */
    public static Range parseTypeRange(char option, String rangeString) throws ParsingException {
        StringTokenizer rangeTokenizer = new StringTokenizer(rangeString, ",-=", true);

        if (rangeTokenizer.countTokens() != 5) {
            throw new ParsingException("-" + option + " has a malformed range : " + rangeString);
        }

        int start = Format.parseUnsigned(rangeTokenizer.nextToken());

        String sep = rangeTokenizer.nextToken();

        int end = Format.parseUnsigned(rangeTokenizer.nextToken()) + ("-".equals(sep)?0:start);

        String nextSep = rangeTokenizer.nextToken();
        if (!"=".equals(nextSep)) {
            throw new ParsingException("-" + option + " has a malformed range : " + rangeString + " (expected '=' before last address)");
        }

        RangeType map = parseRangeType(rangeTokenizer.nextToken());

        if (option == 't') {
            return new InterruptVectorRange(start, end, map);
        }
        else {
            return new Range(start, end, map);
        }
    }

    static RangeType parseRangeType(String arg) throws ParsingException {
        if (StringUtils.isBlank(arg))  {
            throw new ParsingException("no memtype given");
        }
        RangeType rangeType = new RangeType();

        String[] type_width = StringUtils.split(arg, ':');

        String type = type_width[0].toUpperCase();
        if (type.startsWith("C")) {
            rangeType.memoryType = RangeType.MemoryType.CODE;
            if (type_width.length == 1) {
                rangeType.widths.add(RangeType.Width.MD_WORD);
            }
            else {
                String width = type_width[1].toUpperCase();
                if (width.startsWith("L") || "32".equalsIgnoreCase(width)) {
                    rangeType.widths.add(RangeType.Width.MD_LONG);
                }
                else if (width.startsWith("W") || "16".equalsIgnoreCase(width)) {
                    rangeType.widths.add(RangeType.Width.MD_WORD);
                }
                else {
                    throw new ParsingException("unrecognized data type at '" + arg + "'\n" + memtypehelp + "'\n");
                }
            }
        }
        else if (type.startsWith("D")) {
            rangeType.memoryType = RangeType.MemoryType.DATA;

            if (type_width.length == 1) {
                rangeType.widths.add(RangeType.Width.MD_WORD);
            }
            else {
                int i = 1;
                while (type_width.length > i) {
                    String width = type_width[i].toUpperCase();
                    if (width.startsWith("L") || "32".equalsIgnoreCase(width)) {
                        rangeType.widths.add(RangeType.Width.MD_LONG);
                    }
                    else if (width.startsWith("N")) {
                        rangeType.widths.add(RangeType.Width.MD_LONGNUM);
                    }
                    else if (width.startsWith("R")) {
                        rangeType.widths.add(RangeType.Width.MD_RATIONAL);
                    }
                    else if (width.startsWith("V")) {
                        rangeType.widths.add(RangeType.Width.MD_VECTOR);
                    }
                    else if (width.startsWith("W") || "16".equalsIgnoreCase(width)) {
                        rangeType.widths.add(RangeType.Width.MD_WORD);
                    }
                    else {
                        throw new ParsingException("unrecognized data type at '" + arg + "'\n" + memtypehelp + "'\n");
                    }
                    i++;
                }
            }
        }
        else if (type.startsWith("N")) {
            rangeType.memoryType = RangeType.MemoryType.NONE;
        }
        else if (type.startsWith("U")) {
            rangeType.memoryType = RangeType.MemoryType.UNKNOWN;
        }
        else if (type.startsWith("U")) {
            rangeType.memoryType = RangeType.MemoryType.DATA;
            rangeType.widths.add(RangeType.Width.MD_VECTOR);
        }
        else {
            throw new ParsingException("unrecognized memory type at '" + arg + "'\n" + memtypehelp + "\n");
        }

        return rangeType;
    }

    public static void parseSymbol(Map<Integer, Symbol> symbols, String argument, String[][] registerLabels) throws ParsingException {
        Integer address = Format.parseUnsigned(StringUtils.substringBefore(argument, "="));
        String text = StringUtils.substringAfter(argument, "=").trim();
        symbols.put(address, new Symbol(address, text, registerLabels));
    }

    public static void parseJumpHint(Map<Integer, List<Integer>> jumpHints, String argument) throws ParsingException {
        Integer source = Format.parseUnsigned(StringUtils.substringBefore(argument, "="));
        String targets = StringUtils.substringAfter(argument, "=").trim();
        List<Integer> targetList = new ArrayList<Integer>();
        StringTokenizer t = new StringTokenizer(targets, ",");
        while (t.hasMoreTokens()) {
            targetList.add(Format.parseUnsigned(t.nextToken().trim()));
        }
        jumpHints.put(source, targetList);
    }
}



