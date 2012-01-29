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
import java.util.*;

public class Dfr
{
    private static final String DEFAULT_OPTIONS_FILE = "dfr.txt";

    public Set<OutputOption> outputOptions = EnumSet.noneOf(OutputOption.class);

    String inputFileName;
    String outputFileName;

    boolean optLittleEndian = false;
    boolean optSplitPerMemoryRange = false;

    Memory memory = new FastMemory();

    Writer outWriter;
    PrintStream debugPrintStream = System.err;

    MemoryMap fileMap = new MemoryMap('i', "File map");
    MemoryMap memMap = new MemoryMap('m', "Memory map");
    MemoryMap rangeMap = new MemoryMap('d', "Selected ranges");

    String startTime = "";
    private Map<Integer, Symbol> symbols = new HashMap<Integer, Symbol>();

    private void usage()
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
                        + "-s address=name   define symbol\n"
                        + "-t address        interrupt vector start, equivalent to -m address,0x400=DATA:V\n"
                        + "-v                verbose\n"
                        + "-w options        output options (use -w? to list options)\n"
                        + "-x file           read options from file\n"
                        + "Numbers are C-style (16 or 0x10) and can be followed by the K or M multipliers.\n"
                        + "A range is start-end or start,length.\n";

        System.err.println("Usage: " + getClass().getSimpleName() + "[options] filename");
        System.err.println("Options:");
        System.err.println(help);
    }

    public static void main(String[] args) throws IOException, DisassemblyException, ParsingException {
        new Dfr().execute(args);
    }

    public Set<OutputOption> getOutputOptions() {
        return outputOptions;
    }

    public void setOutputOptions(Set<OutputOption> outputOptions) {
        this.outputOptions = outputOptions;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public Writer getOutWriter() {
        return outWriter;
    }

    public void setOutWriter(Writer outWriter) {
        this.outWriter = outWriter;
    }

    public PrintStream getDebugPrintStream() {
        return debugPrintStream;
    }

    public void setDebugPrintStream(PrintStream debugPrintStream) {
        this.debugPrintStream = debugPrintStream;
    }

    private void execute(String[] args) throws ParsingException, IOException, DisassemblyException {
        if (!new File(DEFAULT_OPTIONS_FILE).exists()) {
            System.err.println("Default options file " + DEFAULT_OPTIONS_FILE + " not found.");
        }
        else {
            readOptions(DEFAULT_OPTIONS_FILE);
        }
        processOptions(args);

        initialize();

        disassembleMemRanges();

        cleanup();
    }



    ///* Logging */
    void log(String s)
    {
        try {
            debugPrintStream.println(s);
        } catch (Exception e) {
            System.err.println(s);
        }
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
        return (-signExtend(n, x));
    }

    static boolean IsNeg(int n, int x)
    {
        return (x & (1 << (n - 1))) != 0;
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
        
        if (outWriter != null) {
            outWriter.close();
        }

        outWriter = new FileWriter(outName + "." + ext);
    }

    
    private void writeHeader(Writer writer) throws IOException {
        writer.write("; Disassembler based on Dfr v1.03 by Kevin Schoedel\n");
        writer.write(";   Date:   " + startTime + "\n");
        writer.write(";   Input:  " + inputFileName + "\n");
        writer.write(";   Output: " + (outputFileName == null ? "(default)" : outputFileName) + "\n");
        writer.write("\n");
    }


    int disassembleOneInstruction(CPUState cpuState, Range memRange, int memoryFileOffset, CodeStructure codeStructure) throws IOException {
        DisassembledInstruction disassembledInstruction = new DisassembledInstruction(memRange.start);
        disassembledInstruction.getNextInstruction(memory, cpuState.pc);
        if ((disassembledInstruction.opcode = OpCode.opCodeMap[disassembledInstruction.data[0]]) == null)
        {
            disassembledInstruction.opcode = OpCode.opData[DATA.SpecType_MD_WORD];
        }

        disassembledInstruction.decodeInstructionOperands(cpuState.pc, memory);

        disassembledInstruction.formatOperandsAndComment(cpuState, true, outputOptions);
        
        if (codeStructure != null) {
            codeStructure.getInstructions().put(cpuState.pc, disassembledInstruction);
        }
        else {
            // No structure analysis, output right now
            printDisassembly(disassembledInstruction, cpuState.pc, memoryFileOffset);
        }

        return disassembledInstruction.n << 1;
    }


    int disassembleOneDataRecord(CPUState dummyCpuState, Range memRange, int memoryFileOffset) throws IOException {

        int sizeInBytes = 0;

        for (int spec : memRange.data.spec)
        {
            DisassembledInstruction disassembledInstruction = new DisassembledInstruction(memRange.start);
            disassembledInstruction.getNextData(memory, dummyCpuState.pc);
            disassembledInstruction.x = disassembledInstruction.data[0];
            disassembledInstruction.xBitWidth = 16;
            disassembledInstruction.opcode = OpCode.opData[spec];

            disassembledInstruction.decodeInstructionOperands(dummyCpuState.pc, memory);

            disassembledInstruction.formatOperandsAndComment(dummyCpuState, true, outputOptions);

            sizeInBytes += disassembledInstruction.n << 1;

            printDisassembly(disassembledInstruction, dummyCpuState.pc, memoryFileOffset);
        }

        return sizeInBytes;
    }

    /**
     *
     * @param disassembledInstruction
     * @param address
     * @param memoryFileOffset offset between memory and file (to print file position alongside memory address)  @throws IOException
     */
    private void printDisassembly(DisassembledInstruction disassembledInstruction, int address, int memoryFileOffset) throws IOException {
        outWriter.write(Format.asHex(address, 8) + " ");

        if (memoryFileOffset != 0) {
            outWriter.write("(" + Format.asHex(address - memoryFileOffset, 8) + ") ");
        }

        outWriter.write(disassembledInstruction.toString());
    }


    void disassembleDataMemoryRange(Range memRange, Range fileRange) throws IOException, DisassemblyException {

        fixRangeBoundaries(memRange);

        int memoryFileOffset = fileRange.start - fileRange.fileOffset;

        CPUState dummyCpuState = new CPUState(memRange.start); // TODO : get rid of it (! take care, used for interrupt vector counter)
        while (dummyCpuState.pc < memRange.end)
        {
            dummyCpuState.pc += disassembleOneDataRecord(dummyCpuState, memRange, memoryFileOffset);
        }
    }

    void disassembleCodeMemoryRange(Range memRange, Range fileRange, CodeStructure codeStructure) throws IOException, DisassemblyException {
        fixRangeBoundaries(memRange);

        int memoryFileOffset = fileRange.start - fileRange.fileOffset;

        CPUState cpuState = new CPUState(memRange.start);

        while (cpuState.pc < memRange.end)
        {
            cpuState.pc += disassembleOneInstruction(cpuState, memRange, memoryFileOffset, codeStructure);
        }
    }

    private void fixRangeBoundaries(Range memRange) {
        if ((memRange.start & 1) != 0)
        {
            log("ERROR : Odd start address 0x" + Format.asHex(memRange.start, 8));
            memRange.start--;
        }
        if ((memRange.end & 1) != 0)
        {
            memRange.end++;
        }
    }

    public void disassembleMemRanges() throws IOException, DisassemblyException {
        if (!outputOptions.contains(OutputOption.STRUCTURE)) {
            // Original one pass disassembly
            for (Range range : memMap.ranges) {
                // find file offset covering this memory location.
                Range matchingFileRange = getMatchingFileRange(range);

                printRangeHeader(range, matchingFileRange);

                if (range.data.isCode()) {
                    disassembleCodeMemoryRange(range, matchingFileRange, null);
                }
                else {
                    disassembleDataMemoryRange(range, matchingFileRange);
                }
            }
        }
        else {
            // Advanced two pass disassembly, with intermediary structural analysis
            CodeStructure codeStructure = new CodeStructure(memMap.ranges.first().start);
            // Disassemble the code ranges
            for (Range range : memMap.ranges) {
                if (range.data.isCode()) {
                    disassembleCodeMemoryRange(range, getMatchingFileRange(range), codeStructure);
                }
            }
            codeStructure.postProcess(symbols, memMap.ranges, memory, debugPrintStream, outputOptions.contains(OutputOption.ORDINAL));

            for (Range range : memMap.ranges) {
                // find file offset covering this memory location.
                Range matchingFileRange = getMatchingFileRange(range);
                printRangeHeader(range, matchingFileRange);
                if (range.data.isCode()) {
                    codeStructure.writeDisassembly(outWriter, range);
                }
                else {
                    disassembleDataMemoryRange(range, matchingFileRange);
                }
            }

            // print and output
            debugPrintStream.println(codeStructure.getInstructions().size() + " instructions");
            debugPrintStream.println(codeStructure.getLabels().size() + " labels");
            debugPrintStream.println(codeStructure.getFunctions().size() + " functions");
            debugPrintStream.println(codeStructure.getReturns().size() + " returns");
        }
    }

    private void printRangeHeader(Range range, Range matchingFileRange) throws IOException {
        String msg = "Disassembly of 0x" + Format.asHex(range.start, 8) + "-0x" + Format.asHex(range.end, 8)
                + " (file 0x" + Format.asHex(range.start - matchingFileRange.start + matchingFileRange.fileOffset, 8)
                + ") as " + range.data;
        if (outputOptions.contains(OutputOption.VERBOSE)) {
            debugPrintStream.println(msg);
        }
        outWriter.write("\n");
        outWriter.write("; ########################################################################\n");
        outWriter.write("; " + msg + "\n");
        outWriter.write("; ########################################################################\n");
    }

    /**
     * Find file offset covering this memory location.
     * @param memRange
     * @return
     */
    private Range getMatchingFileRange(Range memRange) {
        Range matchingFileRange = null;
        for (Range fileRange : fileMap.ranges) {
            if (memRange.start >= fileRange.start && memRange.start <= fileRange.end) {
                matchingFileRange = fileRange;
                break;
            }
        }
        if (matchingFileRange == null) {
            debugPrintStream.println("WARNING : No matching file range ('-i' option) found for address 0x" + Format.asHex(memRange.start, 8) + "...");
            debugPrintStream.println("ssuming no offset between file and memory for now.");
            return memRange;
        }
        return matchingFileRange;
    }


    ///* initialization */

    public void initialize() throws IOException {
        startTime = new Date().toString();

        if (inputFileName == null)
        {
            log(getClass().getSimpleName() + ": no input file");
            usage();
            System.exit(-1);
        }

        OpCode.initOpcodeMap(outputOptions);

        DisassembledInstruction.initFormatChars(outputOptions);

        CPUState.initRegisterLabels(outputOptions);

//        if (outOptions.fileMap || outOptions.memoryMap) {
        openOutput(0, false, /*outOptions.optSplitPerMemoryRange ? "map" :*/ "asm");
        writeHeader(outWriter);
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

    public void cleanup() throws IOException {
        outWriter.close();
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
                    debugPrintStream.println("-" + option + ": not implemented yet!\n");
                    System.exit(1);
                    break;

                case 'F':
                case 'f':
                    argument = optionHandler.getArgument();
                    if (StringUtils.isBlank(argument)) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }
                    debugPrintStream.println("-" + option + ": not implemented yet!\n");
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
                    debugPrintStream.println("-" + option + ": not implemented yet!\n");
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
                    OptionHandler.parseSymbol(symbols, argument);
                    break;

                case 'T':
                case 't':
                    argument = optionHandler.getArgument();
                    if (StringUtils.isBlank(argument)) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }
                    memMap.add(OptionHandler.parseTypeRange(option, argument + "," + CodeStructure.INTERRUPT_VECTOR_LENGTH + "=DATA:V"));
                    break;

                case 'V':
                case 'v':
                    outputOptions.add(OutputOption.VERBOSE);
                    break;

                case 'W':
                case 'w':
                    argument = optionHandler.getArgument();
                    if (StringUtils.isBlank(argument)) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }
                    if (!OutputOption.parseFlag(outputOptions, option, argument)) {
                        System.exit(1);
                    }
                    break;

                case 'X':
                case 'x':
                    argument = optionHandler.getArgument();
                    if (StringUtils.isBlank(argument)) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }
                    try {
                        readOptions(argument);
                    } catch (IOException e) {
                        debugPrintStream.println("Cannot open given options file '" + argument + "'");
                        System.exit(1);
                    }
                    break;

                case 'Z':
                case 'z':
                    outputOptions.add(OutputOption.DEBUG);
                    break;

                default:
                    log("unknown option \"-" + option + "\"");
                    usage();
                    return false;
            }
        }

        return true;
    }


    public void readOptions(String filename) throws IOException, ParsingException {
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