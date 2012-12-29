package com.nikonhacker.disassembly;

import com.nikonhacker.Constants;

import java.util.EnumSet;
import java.util.Set;

/** output options */
public enum OutputOption {
    REGISTER    ("register",        new String[]{"use 'AC', 'FP', 'SP' instead of 'R13', 'R14', 'R15'", "use '$zero', '$at', '$v0' instead of 'r0', 'r1', 'r2', etc."}, false),
    DMOV        ("dmov",            new String[]{"use 'LD'/'ST' for some 'DMOV' operations", "use 'move' instead of 'addu $xx, $yy, $zero' or 'or $xx, $yy, $zero' and 'nop' instead of 'move $zero, $xx'"}, false),
    SHIFT       ("shift",           new String[]{"use 'LSR', 'LSL', 'ASR' instead of 'SR2', 'LSL2', 'ASR2' (adding 16 to shift)", "use nop instead of 'sll $zero, $zero, 0'"}, false),
    STACK       ("stack",           new String[]{"use 'PUSH'/'POP' for stack operations", null}, false),
    SPECIALS    ("specials",        new String[]{"use 'AND', 'OR', 'ST', 'ADD' instead of 'ANDCCR', 'ORCCR', 'STILM', 'ADDSP'", null}, false),
    BZ          ("bz",              new String[]{null, "use 'beqz', 'bnez', 'beqzl', 'bnezl' instead of 'beq', 'bne', 'beql', 'bnel' when $rt=$zero"}, false),
    LI          ("li",              new String[]{null, "use 'li' instead of 'addiu' and 'ori' when $rs=$zero"}, false),
    RET         ("ret",             new String[]{null, "use 'ret' instead of 'jr $ra'"}, false),

    CSTYLE      ("cstyle",          new String[]{"use C style operand syntax", null}, false),
    DOLLAR      ("dollar",          new String[]{"use $0 syntax for hexadecimal numbers", null}, false),

    ADDRESS     ("address",         "include memory address", true),
    OFFSET      ("offset",          "include file position (add offset)", false),
    HEXCODE     ("hexcode",         "include hex version of instruction and operands", true),
    BLANKS      ("blanks",          "include a large blank area before disassembled statement", true),

    STRUCTURE   ("structure",       "structural code analysis (code flow, symbols, etc). Needs more resources.", true),
    ORDINAL     ("ordinalnames",    "(if structure is enabled) generate names based on ordinal numbers instead of address", false),
    PARAMETERS  ("parameters",      "(if structure is enabled) try to resolve not only functions but also parameters", false),
    INT40       ("int40",           new String[]{"(if structure is enabled) resolve calls through INT40 wrapper", null}, true),

    //FILEMAP     ("filemap",         "write file map"),
    //MEMORYMAP   ("memorymap",       "write memory map"),

    //SYMBOLS     ("symbols",         "write symbol table"),
    //XREF1       ("crossreference",  "write cross reference"),
    //XREF2       ("xreference",      "write cross reference"),

    VERBOSE     ("verbose",         "verbose messages", false),
    DEBUG       ("debug",           "debug disassembler", false)
    ;

    private String key;
    private String[] help;
    private boolean defaultValue;

    public static EnumSet<OutputOption> allFormatOptions =     EnumSet.of(REGISTER, DMOV, SHIFT, STACK, SPECIALS, BZ, LI, RET, CSTYLE, DOLLAR, ADDRESS, OFFSET, HEXCODE, BLANKS);
    public static EnumSet<OutputOption> defaultFormatOptions = EnumSet.of(REGISTER, DMOV, SHIFT, STACK, SPECIALS, BZ, LI, RET, CSTYLE, DOLLAR, ADDRESS, HEXCODE);

    /**
     * @param key the option's key
     * @param help Help string, option available for both FR and TX CPUs
     * @param defaultValue
     */
    OutputOption(String key, String help, boolean defaultValue) {
        this.key = key;
        this.help = new String[]{help, help};
        this.defaultValue = defaultValue;
    }
    /**
     * @param key the option's key
     * @param help Help strings for FR & TX CPUs. If null, this option does not apply to the corresponding CPU
     * @param defaultValue
     */
    OutputOption(String key, String[]help, boolean defaultValue) {
        this.key = key;
        this.help = help;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getFrHelp() {
        return help[Constants.CHIP_FR];
    }

    public String getTxHelp() {
        return help[Constants.CHIP_TX];
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    private static String getFullHelp(int chip, Character option) {
        String s = "Here are the allowed output options" + (option==null?"":" (-" + option + ") :\n");
        for (OutputOption outputOption : EnumSet.allOf(OutputOption.class)) {
            if (outputOption.help[chip] != null) {
                s += (option==null?"  ":("  -" + option)) + outputOption.key + " : " + outputOption.help[chip] + "\n";
            }
        }
        return s;
    }

    private static OutputOption getByKey(String key) {
        for (OutputOption outputOption : EnumSet.allOf(OutputOption.class)) {
            if (outputOption.key.equals(key)) return outputOption;
        }
        return null;
    }

    public static void setOption(Set<OutputOption> outputOptions, String key, boolean value) throws ParsingException {
        OutputOption option = getByKey(key);
        if (option == null) {
            throw new ParsingException("Unrecognized output option '" + key + "'");
        }
        else {
            if (value) {
                outputOptions.add(option);
            }
            else {
                outputOptions.remove(option);
            }
        }
    }

    public static boolean parseFlag(int chip, Set<OutputOption> outputOptions, Character option, String optionValue) throws ParsingException {
        if ("?".equals(optionValue)) {
            System.err.println(getFullHelp(chip, option));
            return false;
        }

        boolean value = true;
        String key = optionValue;
        if (key.toLowerCase().startsWith("no")) {
            value = false;
            key = key.substring(2);
        }

        setOption(outputOptions, key, value);

        return true;
    }

}
