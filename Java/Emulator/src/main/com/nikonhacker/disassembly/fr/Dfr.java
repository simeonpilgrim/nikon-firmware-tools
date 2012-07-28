package com.nikonhacker.disassembly.fr;

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

import com.nikonhacker.ApplicationInfo;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.MemoryMap;
import com.nikonhacker.disassembly.*;
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
    /**
     * If null, no output is created. 
     * If empty, default name is used. 
     * If specified, this name is used.
     */
    String outputFileName = "";

    boolean optLittleEndian = false;
    boolean optSplitPerMemoryRange = false;

    Memory memory = null;

    Writer outWriter;
    PrintWriter debugPrintWriter = new PrintWriter(new OutputStreamWriter(System.err));

    MemoryMap fileMap = new MemoryMap('i', "File map");
    MemoryMap memMap = new MemoryMap('m', "Memory map");
    MemoryMap rangeMap = new MemoryMap('d', "Selected ranges");

    String startTime = "";
    private Map<Integer, Symbol> symbols = new HashMap<Integer, Symbol>();
    private Map<Integer, List<Integer>> jumpHints = new HashMap<Integer, List<Integer>>();

    private void usage()
    {
        String help =
                "-d range          disassemble only specified range\n"
                        + "-e address=name   (not implemented) define entry point symbol\n"
                        + "-f range=address  (not implemented) map range of input file to memory address\n"
                        + "-h                display this message\n"
                        + "-i range=offset   map range of memory to input file offset\n"
                        + "-j source=target[,target[,...]] define values for a dynamic jump (used in code structure analysis)\n"
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

    public void setOutputOptions(Set<OutputOption> outputOptions) {
        this.outputOptions = outputOptions;
    }

    public void setInputFileName(String inputFileName) throws IOException {
        this.inputFileName = inputFileName;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void setOutWriter(Writer outWriter) {
        this.outWriter = outWriter;
    }

    public void setDebugPrintWriter(PrintWriter debugPrintWriter) {
        this.debugPrintWriter = debugPrintWriter;
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
            debugPrintWriter.write(s);
        } catch (Exception e) {
            System.err.println(s);
        }
    }



    ///* output */

    void openOutput(int pc, boolean usePC, String ext) throws IOException {
        String outName;
        if (outputFileName == null) {
            outWriter = null;
        }
        else {
            if (outputFileName.length() == 0) {
                outName = FilenameUtils.removeExtension(inputFileName);
            }
            else {
                outName = FilenameUtils.removeExtension(outputFileName);
            }
            if (usePC) {
                outName += "_" + Format.asHex(pc, 8);
            }
            
            if (outWriter != null) {
                outWriter.close();
            }
    
            outWriter = new FileWriter(outName + "." + ext);
        }
    }

    
    private void writeHeader(Writer writer) throws IOException {
        writer.write("; " + ApplicationInfo.getName() + " v" + ApplicationInfo.getVersion() + ", disassembler based on Dfr v1.03 by Kevin Schoedel\n");
        writer.write(";   Date:   " + startTime + "\n");
        if (inputFileName != null) {
            writer.write(";   Input:  " + inputFileName + "\n");
        }
        writer.write(";   Output: " + (outputFileName == null ? "(default)" : outputFileName) + "\n");
        writer.write("\n");
    }


    int disassembleOneInstruction(CPUState cpuState, Range memRange, int memoryFileOffset, CodeStructure codeStructure, Set<OutputOption> outputOptions) throws IOException {
        DisassembledInstruction disassembledInstruction = new DisassembledInstruction(memRange.getStart());
        disassembledInstruction.getNextInstruction(memory, cpuState.pc);
        if ((disassembledInstruction.opcode = OpCode.opCodeMap[disassembledInstruction.data[0]]) == null)
        {
            disassembledInstruction.opcode = OpCode.opData[DataType.SpecType_MD_WORD];
        }

        disassembledInstruction.decodeInstructionOperands(cpuState.pc, memory);

        disassembledInstruction.formatOperandsAndComment(cpuState, true, this.outputOptions);

        if (codeStructure != null) {
            if ((disassembledInstruction.opcode.type == OpCode.Type.CALL || disassembledInstruction.opcode.type == OpCode.Type.INT) && outputOptions.contains(OutputOption.PARAMETERS)) {
                disassembledInstruction.cpuState = cpuState.clone();
            }

            codeStructure.getInstructions().put(cpuState.pc, disassembledInstruction);
        }
        else {
            // No structure analysis, output right now
            if (outWriter != null) {
                printDisassembly(outWriter, disassembledInstruction, cpuState.pc, memoryFileOffset, outputOptions);
            }
        }

        return disassembledInstruction.n << 1;
    }


    int disassembleOneDataRecord(CPUState dummyCpuState, Range memRange, int memoryFileOffset, Set<OutputOption> outputOptions) throws IOException {

        int sizeInBytes = 0;

        for (int spec : memRange.getDataType().spec)
        {
            DisassembledInstruction disassembledInstruction = new DisassembledInstruction(memRange.getStart());
            disassembledInstruction.getNextData(memory, dummyCpuState.pc);
            disassembledInstruction.x = disassembledInstruction.data[0];
            disassembledInstruction.xBitWidth = 16;
            disassembledInstruction.opcode = OpCode.opData[spec];

            disassembledInstruction.decodeInstructionOperands(dummyCpuState.pc, memory);

            disassembledInstruction.formatOperandsAndComment(dummyCpuState, true, this.outputOptions);

            sizeInBytes += disassembledInstruction.n << 1;

            if (outWriter != null) {
                printDisassembly(outWriter, disassembledInstruction, dummyCpuState.pc, memoryFileOffset, outputOptions);
            }
        }

        return sizeInBytes;
    }

    /**
     *
     * @param writer
     * @param disassembledInstruction
     * @param address
     * @param memoryFileOffset offset between memory and file (to print file position alongside memory address)
     * @throws IOException
     */
    public static void printDisassembly(Writer writer, DisassembledInstruction disassembledInstruction, int address, int memoryFileOffset, Set<OutputOption> options) throws IOException {
        if (options.contains(OutputOption.ADDRESS)) {
            writer.write(Format.asHex(address, 8) + " ");
        }

        if (memoryFileOffset != 0) {
            writer.write("(" + Format.asHex(address - memoryFileOffset, 8) + ") ");
        }

        writer.write(disassembledInstruction.toString(options));
    }


    void disassembleDataMemoryRange(Range memRange, Range fileRange) throws IOException, DisassemblyException {

        fixRangeBoundaries(memRange);

        int memoryFileOffset = outputOptions.contains(OutputOption.OFFSET)?(fileRange.getStart() - fileRange.getFileOffset()):0;

        CPUState dummyCpuState = new CPUState(memRange.getStart()); // TODO : get rid of it (! take care, used for interrupt vector counter)
        while (dummyCpuState.pc < memRange.getEnd())
        {
            dummyCpuState.pc += disassembleOneDataRecord(dummyCpuState, memRange, memoryFileOffset, outputOptions);
        }
    }

    void disassembleCodeMemoryRange(Range memRange, Range fileRange, CodeStructure codeStructure) throws IOException, DisassemblyException {
        fixRangeBoundaries(memRange);

        int memoryFileOffset = outputOptions.contains(OutputOption.OFFSET)?(fileRange.getStart() - fileRange.getFileOffset()):0;

        CPUState cpuState = new CPUState(memRange.getStart());

        while (cpuState.pc < memRange.getEnd())
        {
            cpuState.pc += disassembleOneInstruction(cpuState, memRange, memoryFileOffset, codeStructure, outputOptions);
        }
    }

    private void fixRangeBoundaries(Range memRange) {
        if ((memRange.getStart() & 1) != 0)
        {
            log("ERROR : Odd start address 0x" + Format.asHex(memRange.getStart(), 8));
            memRange.setStart(memRange.getStart() - 1);
        }
        if ((memRange.getEnd() & 1) != 0)
        {
            memRange.setEnd(memRange.getEnd() + 1);
        }
    }

    public CodeStructure disassembleMemRanges() throws IOException, DisassemblyException {
        if (!outputOptions.contains(OutputOption.STRUCTURE)) {
            // Original one pass disassembly
            for (Range range : memMap.ranges) {
                // find file offset covering this memory location.
                Range matchingFileRange = getMatchingFileRange(range);

                printRangeHeader(range, matchingFileRange);

                if (range.getDataType().isCode()) {
                    disassembleCodeMemoryRange(range, matchingFileRange, null);
                }
                else {
                    disassembleDataMemoryRange(range, matchingFileRange);
                }

                printRangeFooter(range);
            }
            return null;
        }
        else {
            // Advanced two pass disassembly, with intermediary structural analysis
            CodeStructure codeStructure = new CodeStructure(memMap.ranges.first().getStart());
            debugPrintWriter.println("Disassembling the code ranges...");
            for (Range range : memMap.ranges) {
                if (range.getDataType().isCode()) {
                    disassembleCodeMemoryRange(range, getMatchingFileRange(range), codeStructure);
                }
            }

            debugPrintWriter.println("Post processing...");
            new CodeAnalyzer(codeStructure, memMap.ranges, memory, symbols, jumpHints, outputOptions, debugPrintWriter).postProcess();
            // print and output
            debugPrintWriter.println("Structure analysis results :");
            debugPrintWriter.println("  " + codeStructure.getInstructions().size() + " instructions");
            debugPrintWriter.println("  " + codeStructure.getLabels().size() + " labels");
            debugPrintWriter.println("  " + codeStructure.getFunctions().size() + " functions");
            debugPrintWriter.println("  " + codeStructure.getReturns().size() + " returns");
            debugPrintWriter.println();

            if (outWriter != null) {
                debugPrintWriter.println("Writing output to disk...");
                for (Range range : memMap.ranges) {
                    // find file offset covering this memory location.
                    Range matchingFileRange = getMatchingFileRange(range);
                    printRangeHeader(range, matchingFileRange);
                    if (range.getDataType().isCode()) {
                        codeStructure.writeDisassembly(outWriter, range, matchingFileRange, outputOptions);
                    }
                    else {
                        disassembleDataMemoryRange(range, matchingFileRange);
                    }
                }
            }
            return codeStructure;
        }
    }

    private void printRangeHeader(Range range, Range matchingFileRange) throws IOException {
        String msg = "Disassembly of 0x" + Format.asHex(range.getStart(), 8) + "-0x" + Format.asHex(range.getEnd(), 8)
                + " (file 0x" + Format.asHex(range.getStart() - matchingFileRange.getStart() + matchingFileRange.getFileOffset(), 8)
                + ") as " + range.getDataType();
        if (outputOptions.contains(OutputOption.VERBOSE)) {
            debugPrintWriter.println(msg);
        }
        if (outWriter != null) {
            outWriter.write("; ########################################################################\n");
            outWriter.write("; " + msg + "\n");
            outWriter.write("; ########################################################################\n");
        }
    }

    private void printRangeFooter(Range range) throws IOException {
        String msg = "End disassembly of 0x" + Format.asHex(range.getStart(), 8) + "-0x" + Format.asHex(range.getEnd(), 8);
        if (outputOptions.contains(OutputOption.VERBOSE)) {
            debugPrintWriter.println(msg);
        }
        if (outWriter != null) {
            outWriter.write("; ############  " + msg + "  ############\n");
        }
    }

    /**
     * Find file offset covering this memory location.
     * @param memRange
     * @return
     */
    private Range getMatchingFileRange(Range memRange) {
        Range matchingFileRange = null;
        for (Range fileRange : fileMap.ranges) {
            if (memRange.getStart() >= fileRange.getStart() && memRange.getStart() <= fileRange.getEnd()) {
                matchingFileRange = fileRange;
                break;
            }
        }
        if (matchingFileRange == null) {
            debugPrintWriter.println("WARNING : No matching file range ('-i' option) found for address 0x" + Format.asHex(memRange.getStart(), 8) + "...");
            debugPrintWriter.println("Assuming no offset between file and memory for now.");
            return memRange;
        }
        return matchingFileRange;
    }


    ///* initialization */

    public void initialize() throws IOException {
        startTime = new Date().toString();

        if (inputFileName == null && memory == null) {
            log(getClass().getSimpleName() + ": no input file");
            usage();
            System.exit(-1);
        }

        OpCode.initOpcodeMap(outputOptions);

        DisassembledInstruction.initFormatChars(outputOptions);

        CPUState.initRegisterLabels(outputOptions);

//        if (outOptions.fileMap || outOptions.memoryMap) {
            openOutput(0, false, /*outOptions.optSplitPerMemoryRange ? "map" :*/ "asm");
            if (outWriter != null) {
                writeHeader(outWriter);
            }
//        }

        if (memory == null) {
            memory = new FastMemory();
            memory.loadFile(new File(inputFileName), fileMap.ranges);
        }

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
        if (outWriter != null) outWriter.close();
    }


    ///* options */


    /**
     * Processes options passed as a String array. E.g. {"infile.bin", "-t1", "-m", "0x00040000-0x00040947=CODE"}
     * @param args
     * @return
     * @throws ParsingException
     */
    public boolean processOptions(String[] args) throws ParsingException {
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
                    debugPrintWriter.println("-" + option + ": not implemented yet!\n");
                    System.exit(1);
                    break;

                case 'F':
                case 'f':
                    argument = optionHandler.getArgument();
                    if (StringUtils.isBlank(argument)) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }
                    debugPrintWriter.println("-" + option + ": not implemented yet!\n");
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

                case 'J':
                case 'j':
                    argument = optionHandler.getArgument();
                    if (StringUtils.isBlank(argument)) {
                        log("option \"-" + option + "\" requires an argument");
                        return false;
                    }
                    OptionHandler.parseJumpHint(jumpHints, argument);
                    break;

                case 'L':
                case 'l':
                    debugPrintWriter.println("-" + option + ": not implemented yet!\n");
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
                    memMap.add(OptionHandler.parseTypeRange(option, argument + "," + CodeAnalyzer.INTERRUPT_VECTOR_LENGTH + "=DATA:V"));
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
                        debugPrintWriter.println("Cannot open given options file '" + argument + "'");
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