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
import java.util.StringTokenizer;

public class Dfr
{
    public static OutOptions outOptions = new OutOptions();

    final static String cmdname = "Dfr";
    final static String version = "1.03";

    String inputFileName = "";
    String outputFileName = "";

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
                        // + "-e address=name   define entry point symbol\n"
                        + "-f range=address  map range of input file to memory address\n"
                        + "-h                display this message\n"
                        + "-i range=offset   map range of memory to input file offset\n"
                        + "-l                little-endian input file\n"
                        + "-m range=type     describe memory range (use -m? to list types)\n"
                        + "-o filename       output file\n"
                        + "-r                separate output file for each memory range\n"
                        // + "-s address=name   define symbol\n"
                        + "-t address        equivalent to -m address,0x400=DATA:V\n"
                        + "-v                verbose\n"
                        + "-w options        output options (use -w? to list options)\n"
                        + "-x file           read options from file\n"
                        + "Numbers are C-style. A range is start-end or start,length.\n"
                ;

        Log("Usage: " + cmdname + "[options] filename");
        Log("Options:");
        Log(help);
    }

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

    public static void main(String[] args) throws IOException, DisassemblyException, OptionParsingException {
        new Dfr().execute("dfr.txt", args);
    }

    private void execute(String dfrFile, String[] args) throws OptionParsingException, IOException, DisassemblyException {
        readOptions(dfrFile);

        options(args);
        initialize();
        disassembleMemRanges();
        cleanup();
    }

    static void Log(String s)
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
        boolean altReg = false;     /* use AC, SP, FP for R13-R15 - now automatic according to instruction */
        boolean altDMov = false; /* use LD/ST for some DMOV etc */
        boolean altShift = false;   /* use large constant for LSL2 etc */
        boolean altStack = false;   /* use PUSH/POP */
        boolean altSpecials = false;/* use special register operation */
        boolean cStyle = false;     /* use C style operand syntax */
        boolean hexDollar = false;  /* use $0 syntax for hexadecimal numbers */
        boolean fileMap = false;    /* write file map */
        boolean memoryMap = false;  /* write memory map */
        boolean symTab = false;     /* write symbol table */
        boolean xRef = false;       /* write cross reference */
        boolean verbose = false; /* verbose messages */
        boolean debug = false;   /* debug disassembler miscellaneous */
        boolean invert = false;     /* parsing: invert sense of flag */
    }

    ///* output */

    void openoutput(int pc, int usepc, String ext) throws IOException {
        //    if (usepc)
        //        sprintf_s(outname, FILENAME_MAX, "%s-%08lX%s%s", outbase, pc, dot, ext);
        //    else
        //        sprintf_s(outname, FILENAME_MAX, "%s%s%s", outbase, dot, ext);
        //    if (outfp)
        //        fclose(outfp);

        String outname = FilenameUtils.getBaseName(inputFileName) + "." + ext;
        fileWriter = new FileWriter(outname);
    }

    void Info(String s) throws IOException {
        fileWriter.write(s);

        if (outOptions.verbose)
        {
            System.out.println(s);
        }
        System.err.println(s);
    }

    static void Error(String s)
    {
        Log("*****");
        Log(s);
    }


    void writeHeader() throws IOException {
        fileWriter.write("DFR " + version + "\n");
        fileWriter.write("  Date:   " + startTime + "\n");
        fileWriter.write("  Input:  " + inputFileName + "\n");
        fileWriter.write("  Output: " + outputFileName + "\n");
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
            Error("Odd start address 0x" + Format.asHex(startPc, 8));
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
                    Error("input error: " + (-sizeInBytes - 1));
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
                Info("Disassemble 0x" + Format.asHex(memRange.start, 8) + "-0x" + Format.asHex(memRange.end, 8)
                        + " (file 0x" + Format.asHex(matchingFileRange.fileOffset, 8)
                        + ") as " + memRange.data + "\n");
                Info("\n");
                disassembleMemoryRange(memRange, matchingFileRange);
                Info("\n");
            }
        }
    }


    ///* initialization */

    void initialize() throws IOException {
        startTime = new Date().toString();

        if (inputFileName.length() == 0)
        {
            Log(cmdname + ": no input file\n");
            usage();
            System.exit(-1);
        }

        File binaryFile = new File(inputFileName);

        memory.loadFile(binaryFile, 0x40000);

        if (outOptions.hexDollar) {
            hexPrefix = "$";
        }

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
        openoutput(0, 0, /*outOptions.optSplitPerMemoryRange ? "map" :*/ "asm");
        writeHeader();
//        }

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
     * Parses the given string as an 32-bit integer
     * The number can be either decimal or hex (0x-prefixed) and can be followed by the K or M (case insensitive) multipliers
     * @param s the String to convert
     * @return the converted int, to be considered unsigned
     */
    static int parseUnsigned(char opt, String s) throws OptionParsingException {
        boolean isHex = (s.length() > 2 && s.charAt(0) == '0' && (s.charAt(1) == 'x' || s.charAt(1) == 'X'));

        long v = 0;
        int i = isHex ? 2 : 0;
        for (; i < s.length(); i++)
        {
            char ch = s.charAt(i);
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

        if (i != s.length())
        {
            switch (s.charAt(i))
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

        if (i != s.length())
        {
            throw new OptionParsingException(cmdname + ": -" + opt + " unrecognized value : " + s);
        }

        return (int) v;
    }


    /**
     * Parses a String of the form start-end=offset or start,length=offset
     * start, end and offset can be either decimal or hex (0x-prefixed) and can be followed by the K or M (case insensitive) multipliers
     * @param s the String to parse
     * @return the converted Range
     */
    static Range parseOffsetRange(char opt, String s) throws OptionParsingException {
        StringTokenizer st = new StringTokenizer(s.replace(":", "="), "-,=", true);
        if (st.countTokens() != 5) {
            throw new OptionParsingException(cmdname + ": -" + opt + " has an malformed range : " + s);
        }
        int start = parseUnsigned(opt, st.nextToken());
        String sep = st.nextToken();
        int end = parseUnsigned(opt, st.nextToken()) + ("-".equals(sep)?0:start);
        String nextSep = st.nextToken();
        if (!"=".equals(nextSep)) {
            throw new OptionParsingException(cmdname + ": -" + opt + " has an malformed range : " + s + " (expected '=' or ':' before last address)");
        }
        int offset = parseUnsigned(opt, st.nextToken());
        return new Range(start, end, offset);
    }


    /**
     * Parses a String of the form start-end=datatype[:wordsize] or start,length=datatype[:wordsize]
     * start and end can be either decimal or hex (0x-prefixed) and can be followed by the K or M (case insensitive) multipliers
     * @param s the String to parse
     * @return the converted Range
     */
    private static Range parseTypeRange(char opt, String s) throws OptionParsingException {
        StringTokenizer st = new StringTokenizer(s, ",-=", true);

        if (st.countTokens() != 5) {
            throw new OptionParsingException(cmdname + ": -" + opt + " has a malformed range : " + s);
        }

        int start = parseUnsigned(opt, st.nextToken());

        String sep = st.nextToken();

        int end = parseUnsigned(opt, st.nextToken()) + ("-".equals(sep)?0:start);

        String nextSep = st.nextToken();
        if (!"=".equals(nextSep)) {
            throw new OptionParsingException(cmdname + ": -" + opt + " has a malformed range : " + s + " (expected '=' before last address)");
        }

        DATA map = parseMemtype(st.nextToken());

        return new Range(start, end, map);
    }


    //int parseflags(int opt, const char *arg, uint32_t *flagsp, struct flag *flag)
    //{
    //    char *s;
    //    int i, on;

    //    while (*arg) {
    //        if (!_strnicmp(arg, "no", 2)) {
    //            on = 0;
    //            arg += 2;
    //        }
    //        else {
    //            on = 1;
    //        }
    //        for (i = 0; flag[i].width; ++i) {
    //            if (!_strnicmp(arg, flag[i].flag, flag[i].width)) {
    //                if (flag[i].value & OF_INVERT)
    //                    on = !on;
    //                if (on)
    //                    *flagsp |= flag[i].value;
    //                else
    //                    *flagsp &= ~flag[i].value;
    //                break;
    //            }
    //        }
    //        if (!flag[i].width) {
    //            if (*arg != '?')
    //                fprintf(stderr, "%s: unknown flag at \"%s\"\n", cmdname, arg);
    //            for (i = 0; flag[i].width; ++i)
    //                if (flag[i].desc)
    //                    fprintf(stderr, "  -%c %-15s-- %s\n",
    //                            opt, flag[i].flag, flag[i].desc);
    //            return 1;
    //        }
    //        if ((s = strchr(arg, ',')) != NULL)
    //            arg = s + 1;
    //        else
    //            break;
    //    }
    //    return 0;
    //}



    static DATA parseMemtype(String arg) throws OptionParsingException {
        if (StringUtils.isBlank(arg))  {
            throw new OptionParsingException(cmdname + ": no memtype given");
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
                                Error(memtypehelp);
                                throw new OptionParsingException(cmdname + ": unrecognized " + wtf + " at \"" + arg + "\"\n");
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
                Error(memtypehelp);
                throw new OptionParsingException(cmdname + ": unrecognized " + wtf + " at \"" + arg + "\"\n");
        }

        return memp;
    }


    boolean options(String[] args) throws OptionParsingException {
        ParseOpt pos = new ParseOpt(args);
        char opt;
        String arg;

        while ((opt = pos.next()) != Character.MAX_VALUE)
        {
            switch (opt)
            {
                case 0:
                    if (inputFileName.length() > 0)
                    {
                        Log(cmdname + ": too many input files");
                        usage();
                        return false;
                    }
                    inputFileName = pos.arg();
                    break;


                case 'D':
                case 'd':
                    arg = pos.arg();
                    if (arg == null || arg.length() == 0) {
                        Log(cmdname + ": option \"-" + opt + "\" requires an argument");
                        return false;
                    }

                    Range range1 = parseOffsetRange(opt, arg);
                    range1.setFileOffset(1);
                    rangeMap.add(range1);
                    break;

                case 'E':
                case 'e':
                    //        if (!(arg = Parseopt_arg(&pos))) {
                    //            Log(MessageFormat.format("{0}: option \"-{1}\" requires an argument", cmdname, opt));
                    //            return false;
                    //        }
                    //        fprintf(stderr, "%s: symbol table not implemented yet!\n");
                    return false;

                case 'F':
                case 'f':
                    //        if (!(arg = Parseopt_arg(&pos))) {
                    //          Log(MessageFormat.format("{0}: option \"-{1}\" requires an argument", cmdname, opt));
                    //          return false;
                    //        }
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
                    arg = pos.arg();
                    if (StringUtils.isBlank(arg)) {
                        Log(cmdname + ": option \"-" + opt + "\" requires an argument");
                        return false;
                    }

                    Range range = parseOffsetRange(opt, arg);
                    if (range == null)
                        break;

                    fileMap.add(range);
                    break;

                case 'L':
                case 'l':
                    optLittleEndian = true;
                    break;

                case 'M':
                case 'm':
                    arg = pos.arg();
                    memMap.add(parseTypeRange(opt, arg));
                    break;

                case 'O':
                case 'o':
                    outputFileName = pos.arg();
                    if (outputFileName == null || outputFileName.length() == 0) {
                        Log(cmdname + ": option \"-" + opt + "\" requires an argument");
                        return false;
                    }
                    break;

                case 'R':
                case 'r':
                    optSplitPerMemoryRange = true;
                    break;

                case 'S':
                case 's':
                    //        if (!(arg = Parseopt_arg(&pos)))
                    //            goto missing;
                    //        fprintf(stderr, "%s: symbol table not implemented yet!\n");
                    //        exit(1);
                    break;

                case 'T':
                case 't':
                    //        if (!(arg = Parseopt_arg(&pos)))
                    //            goto missing;
                    //        start = parseUnsigned(arg, &r);
                    //        insmap(&memmap, start, start + 0x3FF, MKDATA(1, MD_VECTOR));
                    break;

                case 'V':
                case 'v':
                    outOptions.verbose = true;
                    break;

                case 'W':
                case 'w':
                    //        if (!(arg = Parseopt_arg(&pos)))
                    //            goto missing;
                    //        if (parseflags(opt, arg, &opt_outflag, outflags))
                    //            exit(1);
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
                    Log(cmdname + ": unknown option \"-" + opt + "\"");
                    usage();
                    return false;
            }
        }

        pos.end();
        return true;
    }


    private void readOptions(String filename) throws IOException, OptionParsingException {
        BufferedReader fp = new BufferedReader(new FileReader(filename));

        String buf;
        while ((buf = fp.readLine()) != null)
        {
            buf = buf.trim();
            if (buf.length() == 0 || buf.charAt(0) == '#')
                continue;

            if ((buf.charAt(0) == '-') && buf.length() > 2)
            {
                if (Character.isWhitespace(buf.charAt(2)))
                {
                    String ss = buf.substring(0, 2);
                    String p = buf.substring(2).trim();
                    if (StringUtils.isNotBlank(p))
                    {
                        options(new String[]{ss, p});
                        continue;
                    }
                }
            }

            options(new String[]{buf});
        }
    }
}