package com.nikonhacker.disassembly;

import com.nikonhacker.ApplicationInfo;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.fr.CodeAnalyzer;
import com.nikonhacker.emu.memory.FastMemory;
import com.nikonhacker.emu.memory.Memory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

public abstract class Disassembler {

    private int chip;

    public Set<OutputOption> outputOptions = EnumSet.noneOf(OutputOption.class);
    protected String inputFileName;
    /**
     * If null, no output is created.
     * If empty, default name is used.
     * If specified, this name is used.
     */
    String outputFileName = "";
    boolean optLittleEndian = false;
    boolean optSplitPerMemoryRange = false;
    protected Memory memory = null;
    protected Writer outWriter;
    protected PrintWriter debugPrintWriter = new PrintWriter(new OutputStreamWriter(System.err));
    protected MemoryMap fileMap = new MemoryMap('i', "File map");
    protected MemoryMap memMap = new MemoryMap('m', "Memory map");
    MemoryMap rangeMap = new MemoryMap('d', "Selected ranges");
    protected String startTime = "";
    protected Map<Integer, Symbol> symbols = new HashMap<Integer, Symbol>();
    protected Map<Integer, List<Integer>> jumpHints = new HashMap<Integer, List<Integer>>();

    /**
     *
     * @param writer
     * @param statement
     * @param address
     * @param memoryFileOffset offset between memory and file (to print file position alongside memory address)
     * @throws java.io.IOException
     */
    public static void printDisassembly(Writer writer, Statement statement, int address, int memoryFileOffset, Set<OutputOption> options) throws IOException {
        if (options.contains(OutputOption.ADDRESS)) {
            writer.write(Format.asHex(address, 8) + " ");
        }

        if (memoryFileOffset != 0) {
            writer.write("(" + Format.asHex(address - memoryFileOffset, 8) + ") ");
        }

        writer.write(statement.toString(options));
    }

    protected void usage()
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

        System.err.println("Usage: " + getClass().getSimpleName() + " [options] filename");
        System.err.println("Options:");
        System.err.println(help);
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

    ///* Logging */
    protected void log(String s)
    {
        try {
            debugPrintWriter.write(s);
        } catch (Exception e) {
            System.err.println(s);
        }
    }

    protected void openOutput(int pc, boolean usePC, String ext) throws IOException {
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

            outputFileName = outName + "." + ext;
            outWriter = new FileWriter(outputFileName);
        }
    }

    protected void writeHeader(Writer writer) throws IOException {
        writer.write("; " + ApplicationInfo.getName() + " v" + ApplicationInfo.getVersion() + "\n");
        writer.write(";   Date:   " + startTime + "\n");
        if (inputFileName != null) {
            writer.write(";   Input:  " + inputFileName + "\n");
        }
        writer.write(";   Output: " + (outputFileName == null ? "(default)" : outputFileName) + "\n");
        writer.write("\n");
    }

    protected void fixRangeBoundaries(Range memRange) {
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

    protected void printRangeHeader(Range range, Range matchingFileRange) throws IOException {
        String msg = "Disassembly of 0x" + Format.asHex(range.getStart(), 8) + "-0x" + Format.asHex(range.getEnd(), 8)
                + " (file 0x" + Format.asHex(range.getStart() - matchingFileRange.getStart() + matchingFileRange.getFileOffset(), 8)
                + ") as " + range.getRangeType();
        if (outputOptions.contains(OutputOption.VERBOSE)) {
            debugPrintWriter.println(msg);
        }
        if (outWriter != null) {
            outWriter.write("; ########################################################################\n");
            outWriter.write("; " + msg + "\n");
            outWriter.write("; ########################################################################\n");
        }
    }

    protected void printRangeFooter(Range range) throws IOException {
        String msg = "End disassembly of 0x" + Format.asHex(range.getStart(), 8) + "-0x" + Format.asHex(range.getEnd(), 8);
        if (outputOptions.contains(OutputOption.VERBOSE)) {
            debugPrintWriter.println(msg);
        }
        if (outWriter != null) {
            outWriter.write("; ############  " + msg + "  ############\n\n");
        }
    }

    /**
     * Find file offset covering this memory location.
     * @param memRange
     * @return
     */
    protected Range getMatchingFileRange(Range memRange) {
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

    /**
     * Processes options passed as a String array. E.g. {"infile.bin", "-t1", "-m", "0x00040000-0x00040947=CODE"}
     *
     * @param chip
     * @param args
     * @return
     * @throws com.nikonhacker.disassembly.ParsingException
     */
    public boolean processOptions(int chip, String[] args) throws ParsingException {
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
                    OptionHandler.parseSymbol(symbols, argument, getRegisterLabels());
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
                    if (!OutputOption.parseFlag(chip, outputOptions, option, argument)) {
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
                        readOptions(this.chip, argument);
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

    protected abstract String[][] getRegisterLabels();

    public void readOptions(int chip, String filename) throws IOException, ParsingException {
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
                            processOptions(chip, new String[]{option, params});
                            continue;
                        }
                    }
                }

                processOptions(chip, new String[]{buf});
            }
        }
    }


    public CodeStructure disassembleMemRanges() throws IOException, DisassemblyException {
        if (memMap.ranges.size() == 0) {
            throw new DisassemblyException("No memory range defined in options");
        }
        if (!outputOptions.contains(OutputOption.STRUCTURE)) {
            // Original one pass disassembly
            for (Range range : memMap.ranges) {
                // find file offset covering this memory location.
                Range matchingFileRange = getMatchingFileRange(range);

                printRangeHeader(range, matchingFileRange);

                if (range.getRangeType().isCode()) {
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
            CodeStructure codeStructure = getCodeStructure(memMap.ranges.first().getStart());
            debugPrintWriter.println("Disassembling the code ranges...");
            for (Range range : memMap.ranges) {
                if (range.getRangeType().isCode()) {
                    disassembleCodeMemoryRange(range, getMatchingFileRange(range), codeStructure);
                }
            }

            debugPrintWriter.println("Post processing...");
            new CodeAnalyzer(codeStructure, memMap.ranges, memory, symbols, jumpHints, outputOptions, debugPrintWriter).postProcess();
            // print and output
            debugPrintWriter.println("Structure analysis results :");
            debugPrintWriter.println("  " + codeStructure.getStatements().size() + " statements");
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
                    if (range.getRangeType().isCode()) {
                        codeStructure.writeDisassembly(outWriter, range, matchingFileRange, outputOptions);
                    }
                    else {
                        disassembleDataMemoryRange(range, matchingFileRange);
                    }
                    printRangeFooter(range);
                }
            }
            return codeStructure;
        }
    }

    protected abstract CodeStructure getCodeStructure(int start);


    protected void disassembleDataMemoryRange(Range memRange, Range fileRange) throws IOException, DisassemblyException {

        fixRangeBoundaries(memRange);

        int memoryFileOffset = outputOptions.contains(OutputOption.OFFSET)?(fileRange.getStart() - fileRange.getFileOffset()):0;

        // TODO : get rid of this (! take care, used for interrupt vector counter)
        StatementContext dummyContext = new StatementContext();
        dummyContext.cpuState = getCPUState(memRange);
        while (dummyContext.cpuState.pc < memRange.getEnd())
        {
            dummyContext.cpuState.pc += disassembleOneDataRecord(dummyContext, memRange, memoryFileOffset, outputOptions);
        }
    }

    protected void disassembleCodeMemoryRange(Range memRange, Range fileRange, CodeStructure codeStructure) throws IOException, DisassemblyException {
        fixRangeBoundaries(memRange);

        int memoryFileOffset = outputOptions.contains(OutputOption.OFFSET)?(fileRange.getStart() - fileRange.getFileOffset()):0;

        StatementContext context = new StatementContext();
        context.cpuState = getCPUState(memRange);

        if (memRange.getRangeType().widths.contains(RangeType.Width.MD_LONG)) {
            while (context.cpuState.pc < memRange.getEnd())
            {
                context.cpuState.pc += disassembleOne32BitStatement(context, memRange, memoryFileOffset, codeStructure, outputOptions);
            }
        }
        else {
            while (context.cpuState.pc < memRange.getEnd())
            {
                context.cpuState.pc += disassembleOne16BitStatement(context, memRange, memoryFileOffset, codeStructure, outputOptions);
            }
        }
    }

    protected abstract CPUState getCPUState(Range memRange);

    protected abstract int disassembleOneDataRecord(StatementContext context, Range memRange, int memoryFileOffset, Set<OutputOption> outputOptions) throws IOException, DisassemblyException;

    protected abstract int disassembleOne16BitStatement(StatementContext context, Range memRange, int memoryFileOffset, CodeStructure codeStructure, Set<OutputOption> outputOptions) throws IOException, DisassemblyException;

    protected abstract int disassembleOne32BitStatement(StatementContext context, Range memRange, int memoryFileOffset, CodeStructure codeStructure, Set<OutputOption> outputOptions) throws IOException, DisassemblyException;

    public void initialize() throws IOException {
        startTime = new Date().toString();

        if (inputFileName == null && memory == null) {
            log(getClass().getSimpleName() + ": no input file");
            usage();
            System.exit(-1);
        }


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

    protected void execute(int chip, String[] args) throws ParsingException, IOException, DisassemblyException {
        this.chip = chip;
        if (args.length == 0) {
            usage();
            System.exit(-1);
        }
        String optionsFilename;
        String specificFilename = FilenameUtils.removeExtension(args[args.length - 1]) + "." + getClass().getSimpleName().toLowerCase() + ".txt";
        if (new File(specificFilename).exists()) {
            optionsFilename = specificFilename;
        }
        else {
            optionsFilename = getDefaultOptionsFilename();
        }

        if (!new File(optionsFilename).exists()) {
            System.out.println("No specific options file " + specificFilename + " or default options file " + optionsFilename + " could be found.");
        }
        else {
            readOptions(chip, optionsFilename);
        }
        processOptions(chip, args);

        initialize();

        disassembleMemRanges();

        cleanup();

        System.out.println("Disassembly done.");
    }

    protected abstract String getDefaultOptionsFilename();
}
