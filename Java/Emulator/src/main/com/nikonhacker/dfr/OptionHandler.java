package com.nikonhacker.dfr;

import com.nikonhacker.Format;
import org.apache.commons.lang3.StringUtils;

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

        DATA map = parseMemtype(rangeTokenizer.nextToken());

        return new Range(start, end, map);
    }

    static DATA parseMemtype(String arg) throws ParsingException {
        if (StringUtils.isBlank(arg))  {
            throw new ParsingException("no memtype given");
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
                                throw new ParsingException("unrecognized " + wtf + " at '" + arg + "'\n" + memtypehelp + "'\n");
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
                throw new ParsingException("unrecognized " + wtf + " at '" + arg + "'\n" + memtypehelp + "\n");
        }

        return memp;
    }

    void end() { }

    Character getNextOption() {
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

    String getArgument() {
        if (StringUtils.isBlank(currentToken)) {
            if (index >= arguments.length) return null; // end of list

            currentToken = arguments[index++]; // read next
        }
        String argument = currentToken; // get the argument
        currentToken = null; // clear current to force a read at next call
        return argument;
    }
}



