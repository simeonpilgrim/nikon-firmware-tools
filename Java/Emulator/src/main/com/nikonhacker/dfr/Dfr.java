package com.nikonhacker.dfr;

///*
// * Copyright (c) 2007, Kevin Schoedel. All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions
// * are met:
// *
// * - Redistributions of source code must retain the above copyright
// *   notice, this list of conditions and the following disclaimer.
// *
// * - Redistributions in binary form must reproduce the above copyright
// *   notice, this list of conditions and the following disclaimer in the
// * 	 documentation and/or other materials provided with the distribution.
// *
// * - Neither the name of Kevin Schoedel nor the names of contributors
// *   may be used to endorse or promote products derived from this software
// *   without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
// * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */

///*
// *  1.00  2007/11/05  kps   First release.
// *  1.01  2007/11/06  kps   Fix unsigned int types, option parsing;
// *                          added split output; other minor tweaks.
// *  1.02  2007/11/07  kps   Bug fixes; minimal data flow tracking.
// *  1.03  2007/11/15  kps   Fixed a stupid bug.
//
// Further modifications and port to C# by Simeon Pilgrim
// Further modifications and port to Java by Vicne
// */

import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.FastMemory;
import com.nikonhacker.emu.memory.Memory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Date;

public class Dfr
{
    public static OutOptions outOptions = new OutOptions();

    final static String cmdname = "Dfr";
    final static String version = "1.03";

    String inputFileName;
    String outputFileName;

    boolean optLittleEndian = false;
    boolean optSplitPerMemoryRange = false;

    Memory memory = new FastMemory();

    FileWriter fileWriter;

    MemoryMap fileMap = new MemoryMap('i', "File map");
    MemoryMap memMap = new MemoryMap('m', "Memory map");
    MemoryMap rangeMap = new MemoryMap('d', "Selected ranges");

    String startTime = "";

    ///* output formatting */
    public static String fmt_nxt = ",";
    public static String fmt_imm = "#";
    public static String fmt_and = ",";
    public static String fmt_inc = "+";
    public static String fmt_dec = "-";
    public static String fmt_mem = "@";
    public static String fmt_par = "(";
    public static String fmt_ens = ")";

    public static String hexPrefix = "0x";

    static void usage()
    {
        String help =
                  "-d range          disassemble only specified range\n"
                + "-e address=name   (not implemented) define entry point symbol\n"
                + "-f range=address  (not implemented) map range of input file to memory address\n"
                + "-h                display this message\n"
                + "-i range=offset   map range of memory to input file offset\n"
                + "-l                (not implemented) little-endian input file\n"
                + "-m range=type     describe memory range (use -m? to list types)\n"
                + "-o filename       output file\n"
                + "-r                separate output file for each memory range\n"
                + "-s address=name   (not implemented) define symbol\n"
                + "-t address        equivalent to -m address,0x400=DATA:V\n"
                + "-v                verbose\n"
                + "-w options        output options (use -w? to list options)\n"
                + "-x file           read options from file\n"
                + "Numbers are C-style. A range is start-end or start,length.\n";

        log("Usage: " + cmdname + "[options] filename");
        log("Options:");
        log(help);
    }

    public static void main(String[] args) throws IOException, DisassemblyException, ParsingException {
        new Dfr().execute("dfr.txt", args);
    }

    private void execute(String dfrFilename, String[] args) throws ParsingException, IOException, DisassemblyException {
        if (!new File(dfrFilename).exists()) {
            error("File " + dfrFilename + " does not exist !");
            usage();
            System.exit(-1);
        }

        readOptions(dfrFilename);
        processOptions(args);

        initialize();

        disassembleMemRanges();

        cleanup();
    }

    static void log(String s)
    {
        System.out.println(s);
        System.err.println(s);
    }


    ///* misc definitions */

    /**
     * Extend with Negative sign
     * @param i number of used bits in original number
     * @param x original number
     * @return
     */
    public static int extn(int i, int x) {
        int mask = (1 << i) - 1;
        return ~mask | x;
    }

    static int signExtendMask(int b, int x)
    {
        return ((-b) * ((b & x) != 0 ? 1 : 0));
    }

    /**
     * Interpret x as a signed value based on its last n bits, and extend that MSB
     * so that return represents the same number, but on 32 bits
     * @param n the number of bits to take into account
     * @param x the original number
     */
    public static int signExtend(int n, int x)
    {
        return (x | signExtendMask((1 << (n - 1)), x));
    }

    public static int NEG(int n, int x)
    {
        return (-signExtend((n), (x)));
    }

    static boolean IsNeg(int n, int x)
    {
        return ((x) & (1 << ((n) - 1))) != 0;
    }


    ///* output options */
    public static class OutOptions {
        private static final String REGISTER = "register";

        private static final String DMOV = "dmov";
        private static final String SHIFT = "shift";
        private static final String STACK = "stack";
        private static final String SPECIALS = "specials";

        private static final String CSTYLE = "cstyle";
        private static final String DOLLAR = "dollar";

        private static final String FILEMAP = "filemap";
        private static final String MEMORYMAP = "memorymap";

        private static final String SYMBOLS = "symbols";
        private static final String XREF1 = "crossreference";
        private static final String XREF2 = "xreference";

        private static final String VERBOSE = "verbose";
        private static final String DEBUG = "debug";

        private static final String HELP = "?";

        /** use AC, SP, FP for R13-R15 - now automatic according to instruction */
        boolean altReg = false;

        /** use LD/ST for some DMOV etc */
        boolean altDMov = false;

        /** use large constant for LSL2 etc */
        boolean altShift = false;

        /** use PUSH/POP */
        boolean altStack = false;

        /** use special register operation */
        boolean altSpecials = false;

        /** use C style operand syntax */
        boolean cStyle = false;

        /** use $0 syntax for hexadecimal numbers */
        boolean hexDollar = false;

        /** write file map */
        boolean fileMap = false;

        /** write memory map */
        boolean memoryMap = false;

        /** write symbol table */
        boolean symTab = false;

        /** write cross reference */
        boolean xRef = false;

        /** verbose messages */
        boolean verbose = false;

        /** debug disassembler miscellaneous */
        boolean debug = false;

        public boolean parseFlag(Character option, String optionValue) throws ParsingException {
            boolean value = true;
            if (optionValue.toLowerCase().startsWith("no")) {
                value = false;
                optionValue = optionValue.substring(2);
            }

            if (REGISTER.equals(optionValue)) altReg = value;
            else if (DMOV.equals(optionValue)) altDMov = value;
            else if (SHIFT.equals(optionValue)) altShift = value;
            else if (STACK.equals(optionValue)) altStack = value;
            else if (SPECIALS.equals(optionValue)) altSpecials = value;
            else if (CSTYLE.equals(optionValue)) cStyle = value;
            else if (DOLLAR.equals(optionValue)) hexDollar = value;
            else if (FILEMAP.equals(optionValue)) fileMap = value;
            else if (MEMORYMAP.equals(optionValue)) memoryMap = value;
            else if (SYMBOLS.equals(optionValue)) symTab = value;
            else if (XREF1.equals(optionValue) || XREF2.equals(optionValue)) xRef = value;
            else if (VERBOSE.equals(optionValue)) verbose = value;
            else if (DEBUG.equals(optionValue)) debug = value;
            else {
                if (!HELP.equals(optionValue)) {
                    System.err.println("Unsupported output format option : " + optionValue);
                }
                System.out.println("Here are the allowed output format options (-" + option + ")");
                System.out.println(" -" + option + REGISTER + ": use AC, FP, SP instead of R13, R14, R15");
                System.out.println(" -" + option + DMOV + ": use LD/ST for some DMOV operations");
                System.out.println(" -" + option + SHIFT + ": use LSR, LSL, ASR instead of SR2, LSL2, ASR2 (adding 16 to shift)");
                System.out.println(" -" + option + STACK + ": use PUSH/POP for stack operations");
                System.out.println(" -" + option + SPECIALS + ": use AND, OR, ST, ADD instead of ANDCCR, ORCCR, STILM, ADDSP");
                System.out.println(" -" + option + CSTYLE + ": use C style operand syntax");
                System.out.println(" -" + option + DOLLAR + ": use $0 syntax for hexadecimal numbers");
                System.out.println(" -" + option + FILEMAP + ": write file map");
                System.out.println(" -" + option + MEMORYMAP + ": write memory map");
                System.out.println(" -" + option + SYMBOLS + ": write symbol table");
                System.out.println(" -" + option + XREF1 + ": write cross reference");
                System.out.println(" -" + option + VERBOSE + ": verbose messages");
                System.out.println(" -" + option + DEBUG + ": debug disassembler");
                return false;
            }
            return true;
        }
    }

    ///* output */

    void openOutput(int pc, boolean usePC, String ext) throws IOException {
        String outName = "";
        if (outputFileName == null) {
            outName = FilenameUtils.getBaseName(inputFileName);
        }
        if (usePC) {
            outName += "_" + Format.asHex(pc, 8);
        }
        
        outName += "." + ext;

        if (fileWriter != null)
            fileWriter.close();

        fileWriter = new FileWriter(outName);
    }

    
    ///* Logging */

    void info(String s) throws IOException {
        fileWriter.write(s);

        if (outOptions.verbose)
        {
            System.out.println(s);
        }
        System.err.println(s);
    }

    static void error(String s)
    {
        log("*****");
        log(s);
    }


    void writeHeader() throws IOException {
        fileWriter.write("DFR " + version + "\n");
        fileWriter.write("  Date:   " + startTime + "\n");
        fileWriter.write("  Input:  " + inputFileName + "\n");
        fileWriter.write("  Output: " + (outputFileName==null?"(default)":outputFileName) + "\n");
        fileWriter.write("\n");
    }


    int disassembleOneCodeRecord(CPUState cpuState, Range memRange, int memoryFileOffset) throws IOException {
        DisassemblyState disassemblyState = new DisassemblyState(memRange.start);
        disassemblyState.getNextInstruction(memory, cpuState.pc);
        if ((disassemblyState.opcode = OpCode.opCodeMap[disassemblyState.data[0]]) == null)
        {
            disassemblyState.opcode = OpCode.opData[DATA.SpecType_MD_WORD];
        }

        disassemblyState.decodeInstructionOperands(cpuState, memory);

        disassemblyState.formatOperandsAndComment(cpuState, true);

        printDisassembly(disassemblyState, cpuState, memoryFileOffset);

        return disassemblyState.n << 1;
    }


    int disassembleOneDataRecord(CPUState cpuState, Range memRange, int memoryFileOffset) throws IOException {

        int sizeInBytes = 0;

        for (int spec : memRange.data.spec)
        {
            DisassemblyState disassemblyState = new DisassemblyState(memRange.start);
            disassemblyState.getNextData(memory, cpuState.pc);
            disassemblyState.x = disassemblyState.data[0];
            disassemblyState.w = 16;
            disassemblyState.opcode = OpCode.opData[spec];

            disassemblyState.decodeInstructionOperands(cpuState, memory);

            disassemblyState.formatOperandsAndComment(cpuState, true);

            sizeInBytes += disassemblyState.n << 1;

            printDisassembly(disassemblyState, cpuState, memoryFileOffset);
        }

        return sizeInBytes;
    }

    /**
     *
     * @param disassemblyState
     * @param cpuState
     * @param memoryFileOffset offset between memory and file (to print file position alongside memory address)
     * @throws IOException
     */
    private void printDisassembly(DisassemblyState disassemblyState, CPUState cpuState, int memoryFileOffset) throws IOException {
        fileWriter.write(Format.asHex(cpuState.pc, 8) + " ");

        if (memoryFileOffset != 0) {
            fileWriter.write("(" + Format.asHex(cpuState.pc - memoryFileOffset, 8) + ") ");
        }

        fileWriter.write(disassemblyState.toString());
    }


    void disassembleMemoryRange(Range memRange, Range fileRange) throws IOException, DisassemblyException {
        int startPc = memRange.start;
        int end = memRange.end;
        boolean isCode;

        if ((startPc & 1) != 0)
        {
            error("Odd start address 0x" + Format.asHex(startPc, 8));
            // start &= 0xffFFffFFffFFffFe;
            startPc --;
        }
        if ((end & 1) != 0)
        {
            end++;
        }

        switch (memRange.data.memType)
        {
            case DATA.MEMTYPE_CODE:
            case DATA.MEMTYPE_UNKNOWN:
                isCode = true;
                break;
            case DATA.MEMTYPE_DATA:
                isCode = false;
                break;
            default:
                throw new DisassemblyException("Unknown memory type : " + memRange.data.memType);
        }

        CPUState cpuState = new CPUState(startPc);

        int memoryFileOffset = fileRange.start - fileRange.fileOffset;

        while (cpuState.pc < end)
        {
            int sizeInBytes;
            if (isCode) {
                sizeInBytes = disassembleOneCodeRecord(cpuState, memRange, memoryFileOffset);
            }
            else {
                sizeInBytes = disassembleOneDataRecord(cpuState, memRange, memoryFileOffset);
            }

            if (sizeInBytes < 0)
            {
                if (sizeInBytes != -1)
                    error("input error: " + (-sizeInBytes - 1));
                fileRange.end = cpuState.pc;
                System.out.println("WARNING : setting pc to max...");
                cpuState.pc = -1;
                break;
            }
            cpuState.pc += sizeInBytes;
        }
    }

    void disassembleMemRanges() throws IOException, DisassemblyException {
        for (Range memRange : memMap.ranges) {
            // find file offset covering this memory location.
            Range matchingFileRange = null;
            for (Range fileRange : fileMap.ranges) {
                if (memRange.start >= fileRange.start && memRange.start <= fileRange.end) {
                    matchingFileRange = fileRange;
                    break;
                }
            }

            if (matchingFileRange != null) {
                info("Disassemble 0x" + Format.asHex(memRange.start, 8) + "-0x" + Format.asHex(memRange.end, 8)
                        + " (file 0x" + Format.asHex(matchingFileRange.fileOffset, 8)
                        + ") as " + memRange.data + "\n");
                info("\n");
                disassembleMemoryRange(memRange, matchingFileRange);
                info("\n");
            }
        }
    }


    ///* initialization */

    void initialize() throws IOException {
        startTime = new Date().toString();

        if (inputFileName == null)
        {
            log(cmdname + ": no input file\n");
            usage();
            System.exit(-1);
        }

        if (outOptions.hexDollar) {
            hexPrefix = "$";
        }

        OpCode.initOpcodeMap();

        if (outOptions.cStyle) {
            fmt_imm = "";
            fmt_and = "+";
            fmt_inc = "++";
            fmt_dec = "--";
            fmt_mem = "*";
        }

        if (outOptions.altReg) {
            CPUState.REG_LABEL[CPUState.AC] = "AC";
            CPUState.REG_LABEL[CPUState.FP] = "FP";
            CPUState.REG_LABEL[CPUState.SP] = "SP";
        }
        
//        if (outOptions.fileMap || outOptions.memoryMap) {
        openOutput(0, false, /*outOptions.optSplitPerMemoryRange ? "map" :*/ "asm");
        writeHeader();
//        }

        File binaryFile = new File(inputFileName);

        memory.loadFile(binaryFile, fileMap.ranges);


        //    fixmap(&filemap, 0);
//            if (outOptions.fileMap)
//                dumpmap(&filemap);

        //    fixmap(&memmap, MEMTYPE_UNKNOWN);
        //    fillmap(&memmap, MEMTYPE_UNKNOWN);
        //    delmap(&memmap, MEMTYPE_NONE);
//        if (outOptions.memoryMap)
//            dumpmap(&memmap);

        //    fixmap(&rangemap, 1);
//            if (outOptions.fileMap || outOptions.memoryMap)
//                dumpmap(&rangemap);
    }

    void cleanup() throws IOException {
        fileWriter.close();
    }


    ///* options */


    /**
     * Processes options passed as a String array. E.g. {"infile.bin", "-t1", "-m", "0x00040000-0x00040947=CODE"}
     * @param args
     * @return
     * @throws ParsingException
     */
    boolean processOptions(String[] args) throws ParsingException {
        Character option;
        String argument;
        OptionHandler optionHandler = new OptionHandler(args);

        while ((option = optionHandler.getNextOption()) != null)
        {
            switch (option)
            {
                case 0:
                    // Not an option => Input file. Check we don't have one already
                    if (inputFileName != null)
                    {
                        log("too many input files");
                        usage();
                        return false;
                    }
                    inputFileName = optionHandler.getArgument();
                    break;


                case 'D':
                case 'd':
                    argument = optionHandler.getArgument();
                    if (argument == null || argument.length() == 0) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }

                    Range range1 = OptionHandler.parseOffsetRange(option, argument);
                    range1.setFileOffset(1);
                    rangeMap.add(range1);
                    break;

                case 'E':
                case 'e':
                    argument = optionHandler.getArgument();
                    if (StringUtils.isBlank(argument)) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }
                    System.err.println("-" + option + ": not implemented yet!\n");
                    System.exit(1);
                    break;

                case 'F':
                case 'f':
                    argument = optionHandler.getArgument();
                    if (StringUtils.isBlank(argument)) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }
                    System.err.println("-" + option + ": not implemented yet!\n");
                    System.exit(1);
                    //        if (parseOffsetRange(opt, arg, &r, &start, &end, &map))
                    //            break;
                    //        insmap(&filemap, map, map + end - start, start);
                    break;

                case 'H':
                case 'h':
                case '?':
                    usage();
                    return false;

                case 'I':
                case 'i':
                    argument = optionHandler.getArgument();
                    if (StringUtils.isBlank(argument)) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }

                    Range range = OptionHandler.parseOffsetRange(option, argument);
                    if (range == null)
                        break;

                    fileMap.add(range);
                    break;

                case 'L':
                case 'l':
                    System.err.println("-" + option + ": not implemented yet!\n");
                    System.exit(1);
                    optLittleEndian = true;
                    break;

                case 'M':
                case 'm':
                    argument = optionHandler.getArgument();
                    memMap.add(OptionHandler.parseTypeRange(option, argument));
                    break;

                case 'O':
                case 'o':
                    outputFileName = optionHandler.getArgument();
                    if (StringUtils.isBlank(outputFileName)) {
                        log("option '-" + option + "' requires an argument");
                        return false;
                    }
                    break;

                case 'R':
                case 'r':
                    optSplitPerMemoryRange = true;
                    break;

                case 'S':
                case 's':
                    argument = optionHandler.getArgument();
                    if (StringUtils.isBlank(argument)) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }
                    System.err.println("-" + option + ": not implemented yet!\n");
                    System.exit(1);
                    break;

                case 'T':
                case 't':
                    argument = optionHandler.getArgument();
                    if (StringUtils.isBlank(argument)) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }
                    //        start = parseUnsigned(arg, &r);
                    //        insmap(&memmap, start, start + 0x3FF, MKDATA(1, MD_VECTOR));
                    break;

                case 'V':
                case 'v':
                    outOptions.verbose = true;
                    break;

                case 'W':
                case 'w':
                    argument = optionHandler.getArgument();
                    if (StringUtils.isBlank(argument)) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }
                    if (!outOptions.parseFlag(option, argument)) {
                        System.exit(1);
                    }
                    break;

                case 'X':
                case 'x':
                    //        if (!(arg = Parseopt_arg(&pos)))
                    //            goto missing;
                    //        if (readOptions(arg)) {
                    //            fprintf(stderr, "%s: cannot open options file \"%s\": %s\n",
                    //                cmdname, arg, strerror(errno));
                    //            exit(1);
                    //        }
                    break;

                case 'Z':
                case 'z':
                    outOptions.debug = true;
                    break;

                default:
                    log("unknown option \"-" + option + "\"");
                    usage();
                    return false;
            }
        }

        optionHandler.end();
        return true;
    }


    private void readOptions(String filename) throws IOException, ParsingException {
        BufferedReader fp = new BufferedReader(new FileReader(filename));

        String buf;
        while ((buf = fp.readLine()) != null)
        {
            buf = buf.trim();
            if (buf.length() > 0 && buf.charAt(0) != '#')
            {
                if ((buf.charAt(0) == '-') && buf.length() > 2)
                {
                    // This is an option line
                    if (Character.isWhitespace(buf.charAt(2)))
                    {
                        String option = buf.substring(0, 2);
                        String params = buf.substring(2).trim();
                        if (StringUtils.isNotBlank(params))
                        {
                            processOptions(new String[]{option, params});
                            continue;
                        }
                    }
                }

                processOptions(new String[]{buf});
            }
        }
    }
}