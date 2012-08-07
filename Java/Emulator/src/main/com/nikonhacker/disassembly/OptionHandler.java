package com.nikonhacker.disassembly;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.fr.DataType;
import com.nikonhacker.disassembly.fr.InterruptVectorRange;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

///* reinventing the wheel because resetting getopt() isn't portable */
public class OptionHandler
{
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
     * Parses a String of the form start-end=datatype[:wordsize] or start,length=datatype[:wordsize]
     * start and end can be either decimal or hex (0x-prefixed) and can be followed by the K or M (case insensitive) multipliers
     * @param option the option that called this method
     * @param rangeString the String to parse
     * @return the converted Range
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

        DataType map = parseMemtype(rangeTokenizer.nextToken());

        if (option == 't') {
            return new InterruptVectorRange(start, end, map);
        }
        else {
            return new Range(start, end, map);
        }
    }

    static DataType parseMemtype(String arg) throws ParsingException {
        if (StringUtils.isBlank(arg))  {
            throw new ParsingException("no memtype given");
        }

        DataType memp = new DataType();
        String wtf = "memory type";
        switch (arg.charAt(0))
        {
            case 'C':
            case 'c':
                memp.memType = DataType.MemType.CODE;
                break;

            case 'D':
            case 'd':
                wtf = "data type";
                memp.memType = DataType.MemType.DATA;
                memp.specTypes.add(DataType.SpecType.MD_WORD);

                int separator = arg.lastIndexOf(':');
                if (separator != -1)
                {
                    memp.specTypes.clear(); // remove above default of MD_WORD
                    separator++;
                    while (separator < arg.length())
                    {
                        char c = arg.charAt(separator++);

                        DataType.SpecType md;
                        switch ((c + "").toLowerCase().charAt(0))
                        {
                            case 'l': md = DataType.SpecType.MD_LONG; break;
                            case 'n': md = DataType.SpecType.MD_LONGNUM; break;
                            case 'r': md = DataType.SpecType.MD_RATIONAL; break;
                            case 'v': md = DataType.SpecType.MD_VECTOR; break;
                            case 'w': md = DataType.SpecType.MD_WORD; break;
                            default:
                                throw new ParsingException("unrecognized " + wtf + " at '" + arg + "'\n" + memtypehelp + "'\n");
                        }
                        memp.specTypes.add(md);
                    }
                }
                break;

            case 'n':
            case 'N':
                memp.memType = DataType.MemType.NONE;
                break;

            case 'u':
            case 'U':
                memp.memType = DataType.MemType.UNKNOWN;
                break;

            case 'v':
            case 'V':
                memp.memType = DataType.MemType.DATA;
                memp.specTypes.add(DataType.SpecType.MD_VECTOR);
                break;

            default:
                throw new ParsingException("unrecognized " + wtf + " at '" + arg + "'\n" + memtypehelp + "\n");
        }

        return memp;
    }

    public static void parseSymbol(Map<Integer, Symbol> symbols, String argument, String[] registerLabels) throws ParsingException {
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



