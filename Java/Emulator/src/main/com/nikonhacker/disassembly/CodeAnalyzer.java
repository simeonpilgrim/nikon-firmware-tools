package com.nikonhacker.disassembly;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.fr.FrInstruction;
import com.nikonhacker.disassembly.fr.FrStatement;
import com.nikonhacker.disassembly.fr.InterruptVectorRange;
import com.nikonhacker.emu.memory.Memory;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CodeAnalyzer {

    private CodeStructure codeStructure;
    private SortedSet<Range> ranges;
    private Memory memory;
    private Map<Integer, Symbol> symbols;
    private Map<Integer, List<Integer>> jumpHints;
    private Set<OutputOption> outputOptions;
    private PrintWriter debugPrintWriter;

    public static final int INTERRUPT_VECTOR_LENGTH = 0x400;
    private static final String FUNCTION_PREFIX = "sub";
    private static final String UNKNOWN_PREFIX = "unknown";
    private final Set<Integer> processedStatements;
    private final Map<Integer,Integer> interruptTable;
    private Map<Integer,Integer> int40mapping;


    /**
     * Code Analyzer
     *
     * @param codeStructure
     * @param ranges The defined ranges in the binary file
     * @param memory The memory image
     * @param symbols The symbols defined to override auto-generated function names
     * @param jumpHints
     * @param outputOptions
     * @param debugPrintWriter a PrintWriter to write debug/info messages to
     * @throws IOException
     */
    public CodeAnalyzer(CodeStructure codeStructure, SortedSet<Range> ranges, Memory memory, Map<Integer, Symbol> symbols, Map<Integer, List<Integer>> jumpHints, Set<OutputOption> outputOptions, PrintWriter debugPrintWriter) {
        this.codeStructure = codeStructure;
        this.ranges = ranges;
        this.memory = memory;
        this.symbols = symbols;
        this.jumpHints = jumpHints;
        this.outputOptions = outputOptions;
        this.debugPrintWriter = debugPrintWriter;

        processedStatements = new HashSet<Integer>();
        interruptTable = new HashMap<Integer, Integer>();

        int40mapping = null;
    }

    /**
     * Post-process statements to retrieve code structure
     */
    public void postProcess() throws IOException {

        debugPrintWriter.println("Preprocessing interrupt table...");
        for (Range range : ranges) {
            if (range instanceof InterruptVectorRange) {
                for (int interruptNumber = 0; interruptNumber < INTERRUPT_VECTOR_LENGTH / 4; interruptNumber++) {
                    interruptTable.put(interruptNumber, memory.load32(range.getStart() + 4 * (0x100 - interruptNumber - 1)));
                }
                break;
            }
        }

        if (outputOptions.contains(OutputOption.INT40)) { // Only meaningful for FR CPU
            try {
                // Determine base address to which offsets will be added
                Integer int40address = interruptTable.get(0x40);
                if (int40address == null) {
                    debugPrintWriter.println("INT 0x40 cannot be resolved because interrupt vector declaration in dfr.txt is missing or invalid (e.g. '-t 0x000dfc00'). INT40 following will be disabled");
                }
                else {
                    if (memory.loadInstruction16(int40address + 0x3E) != 0x9F8D /* LDI:32 #i32, R13 */) {
                        debugPrintWriter.println("INT 0x40 does not have the expected structure. INT40 following will be disabled");
                    }
                    else {
                        FrStatement statement = (FrStatement) codeStructure.getStatements().get(int40address + 0x3E);
                        int baseAddress = statement.decodedImm;
                        int40mapping = new TreeMap<Integer, Integer>();
                        /* The range is 0x0004070A-0x00040869, or 0x160 bytes long, or 0x160/2 = 0xB0 (negative) offsets */
                        for (int r12 = 0; r12 > -0xB0; r12--) {
                            int40mapping.put(r12, baseAddress + BinaryArithmetics.signExtend(16, memory.loadUnsigned16(baseAddress + (r12 << 1))));
                        }
                    }
                }
            }
            catch (Exception e) {
                debugPrintWriter.println("Error processing INT40. Please check the interrupt vector address in dfr.txt.");
                debugPrintWriter.println("Continuing without INT40 processing...");
                int40mapping = null;
            }
        }


        debugPrintWriter.println("Following flow starting at entry point...");
        Function main = new Function(codeStructure.getEntryPoint(), "main", "", Function.Type.MAIN);
        codeStructure.getFunctions().put(codeStructure.getEntryPoint(), main);
        try {
            followFunction(main, codeStructure.getEntryPoint(), false);
        }
        catch (DisassemblyException e) {
            debugPrintWriter.println("Error disassembling 'main' code at 0x" + Format.asHex(codeStructure.getEntryPoint(), 2) + ": " + e.getMessage());
        }


        debugPrintWriter.println("Following flow starting at each interrupt...");
        for (Integer interruptNumber : interruptTable.keySet()) {
            Integer address = interruptTable.get(interruptNumber);
            String name = "interrupt_0x" + Format.asHex(interruptNumber, 2) + "_";
            Function function = codeStructure.getFunctions().get(address);
            if (function == null) {
                function = new Function(address, name, "", Function.Type.INTERRUPT);
                codeStructure.getFunctions().put(address, function);
                try {
                    followFunction(function, address, false);
                }
                catch (DisassemblyException e) {
                    debugPrintWriter.println("Error disassembling interrupt 0x" + Format.asHex(interruptNumber, 2) + ": " + e.getMessage());
                }
            }
            else {
                function.addAlias(name);
            }
        }


        debugPrintWriter.println("Processing remaining statements as 'unknown' functions...");
        Map.Entry<Integer, Statement> entry = codeStructure.getStatements().firstEntry();
        while (entry != null) {
            Integer address = entry.getKey();
            if (       !processedStatements.contains(address) // Not processed yet
                    && !codeStructure.getStatements().get(address).isPotentialStuffing() // Not stuffing
                    ) {
                // OK, let's process it
                Function function = new Function(address, "", "", Function.Type.UNKNOWN);
                codeStructure.getFunctions().put(address, function);
                try {
                    followFunction(function, address, false);
                }
                catch (DisassemblyException e) {
                    debugPrintWriter.println("SHOULD NOT HAPPEN. Please report this case on the forums ! : Error disassembling unknown function at 0x" + Format.asHex(address , 2) + ": " + e.getMessage());
                }
            }
            entry = codeStructure.getStatements().higherEntry(address);
        }


        debugPrintWriter.println("Generating names for functions...");
        int functionNumber = 1;
        for (Integer address : codeStructure.getFunctions().keySet()) {
            Function function = codeStructure.getFunctions().get(address);
            if (StringUtils.isBlank(function.getName())) {
                String functionId = outputOptions.contains(OutputOption.ORDINAL)?("" + functionNumber):Integer.toHexString(address);
                if (function.getType() == Function.Type.UNKNOWN) {
                    function.setName(UNKNOWN_PREFIX + "_" + functionId + "_");
                }
                else {
                    function.setName(FUNCTION_PREFIX + "_" + functionId + "_");
                }
            }
            // increment even if unused, to make output stable no matter what future replacements will occur
            functionNumber++;
        }


        debugPrintWriter.println("Overwriting function names with given symbols...");
        if (symbols != null) {
            for (Integer address : symbols.keySet()) {
                if (codeStructure.isFunction(address)) {
                    // TODO This is ugly. A function is a symbol...
                    Function function = codeStructure.getFunctions().get(address);
                    Symbol symbol = symbols.get(address);
                    function.setName(symbol.getName());
                    function.setComment(symbol.getComment());
                    function.setParameterList(symbol.getParameterList());
                }
            }
        }


        debugPrintWriter.println("Generating names for labels...");
        long start = System.currentTimeMillis();
        int labelNumber = 1;
        // First give names to labels linked to function start/segments (if they were spotted as targets already)

        for (Integer address : codeStructure.getFunctions().keySet()) {
            Function function = codeStructure.getFunctions().get(address);
            if (codeStructure.getLabels().containsKey(address)) {
                codeStructure.getLabels().put(address, new Symbol(address, "start_of_" + codeStructure.getFunctionName(address)));
            }
            for (int i = 0; i < function.getCodeSegments().size(); i++) {
                CodeSegment codeSegment = function.getCodeSegments().get(i);
                int startAddress = codeSegment.getStart();
                if (codeStructure.getLabels().containsKey(startAddress)) {
                    codeStructure.getLabels().put(startAddress, new Symbol(startAddress, "part_" + (i + 1) + "_of_" + function.getName()));
                }
            }
        }

        // Temporary storage for names given to returns to make sure they are unique
        Set<String> usedReturnLabels = new HashSet<String>();
        for (Integer address : codeStructure.getLabels().keySet()) {
            if (codeStructure.getLabels().get(address).getName().length() == 0) {
                String label;
                if (codeStructure.isReturn(address)) {
                    Integer matchingFunctionStart = codeStructure.getReturns().get(address);
                    if (matchingFunctionStart != null) {
                        String functionName = codeStructure.getFunctionName(matchingFunctionStart);
                        if (functionName==null) {
                            functionName = "unnamed_function_starting_at_0x" + Format.asHex(matchingFunctionStart, 8);
                        }
                        label = "end_" + functionName;
                        // Prevent multiple exit points of the same function from being given the same name
                        if (usedReturnLabels.contains(label)){
                            // Make it unique
                            int i = 2;
                            while (usedReturnLabels.contains(label + i + "_")) {
                                i++;
                            }
                            label = label + "_" + i;
                        }
                        usedReturnLabels.add(label);
                    }
                    else {
                        label = "end_unidentified_fn_l_" + (outputOptions.contains(OutputOption.ORDINAL)?("" + labelNumber):Integer.toHexString(address)) + "_";
                    }
                }
                else {
                    // Only overwrite if none was present
                    label = codeStructure.getLabels().get(address).getName();
                    if (StringUtils.isBlank(label)) {
                        label = "loc_" + (outputOptions.contains(OutputOption.ORDINAL)?("" + labelNumber):Integer.toHexString(address)) + "_";
                    }
                }
                codeStructure.getLabels().put(address, new Symbol(address, label));
            }
            // increment even if unused, to make output stable no matter what future replacements will occur
            labelNumber++;
        }

        // Override label names by symbols in dfr.txt or dtx.txt file
        for (Integer labelAddress : symbols.keySet()) {
            if (!codeStructure.getFunctions().containsKey(labelAddress)) {
                // This is not a function symbol, it's a code label or a variable
                Symbol label = codeStructure.getLabels().get(labelAddress);
                if (label != null) {
                    // It matches an existing code label. Rename it if it is a generic "label", or add the name otherwise
                    String newLabelName = (label.getName().startsWith("loc_") ? "" : (label.getName() + "_/_")) + symbols.get(labelAddress).getName();
                    debugPrintWriter.println("Renaming label @" + Format.asHex(labelAddress, 8) + " from '" + label.getName() + "' to '" + newLabelName + "'.");
                    label.setName(newLabelName);
                }
            }
        }

        debugPrintWriter.println("Label generation took " + (System.currentTimeMillis() - start) + "ms");


        //int target = 0x00041C52;
//        int target = 0x00041CEC;
//        for (Integer interruptNumber : interruptTable.keySet()) {
//            Integer address = interruptTable.get(interruptNumber);
//            testIfFunctionCallsTarget(address, target, "interrupt_0x" + Format.asHex(interruptNumber, 2) + "_");
//        }

    }

    private void testIfFunctionCallsTarget(Integer address, int target, String path) {
        Function function = codeStructure.getFunctions().get(address);
        if (function == null) {
            debugPrintWriter.println("Error following " + path + " : no function at 0x" + Format.asHex(address, 8));
        }
        else {
            for (Jump call : function.getCalls()) {
                if (call.getTarget() == target) {
                    debugPrintWriter.println("! Match at 0x" + Format.asHex(address, 8) + " in " + path);
                }
                else {
                    testIfFunctionCallsTarget(call.getTarget(), target, path + " > 0x" + Format.asHex(address, 8) + " "+ function.getName());
                }
            }
        }
    }

    void followFunction(Function currentFunction, Integer address, boolean stopAtFirstProcessedStatement) throws IOException, DisassemblyException {
        if (codeStructure.getStatements().get(address) == null) {
            throw new DisassemblyException("No decoded statement at 0x" + Format.asHex(address, 8) + " (not a CODE range)");
        }
        CodeSegment currentSegment = new CodeSegment();
        currentFunction.getCodeSegments().add(currentSegment);
        List<Jump> jumps = new ArrayList<Jump>();
        currentSegment.setStart(address);
        while(address != null) {
            if (stopAtFirstProcessedStatement && processedStatements.contains(address)) {
                Integer previousAddress = codeStructure.getStatements().lowerKey(address);
                // Check we're not in delay slot. We shouldn't stop on delay slot
                if (previousAddress == null || !codeStructure.getStatements().get(previousAddress).getInstruction().hasDelaySlot()) {
                    break;
                }
            }
            Statement statement = codeStructure.getStatements().get(address);
            processedStatements.add(address);
            currentSegment.setEnd(address);
            switch (statement.getInstruction().getFlowType()) {
                case RET:
                    codeStructure.getReturns().put(address, currentFunction.getAddress());
                    codeStructure.getEnds().put(address + (statement.getInstruction().hasDelaySlot() ? statement.getNumBytes() : 0), currentFunction.getAddress());
                    break;
                case JMP:
                case BRA:
                    if (statement.decodedImm != 0) {
                        codeStructure.getLabels().put(statement.decodedImm, new Symbol(statement.decodedImm, "", ""));
                        Jump jump = new Jump(address, statement.decodedImm, statement.getInstruction(), false);
                        jumps.add(jump);
                        currentFunction.getJumps().add(jump);
                    }
                    else {
                        // target is dynamic
                        resolveDynamicTarget(currentFunction, address, jumps, statement);
                    }
                    break;
                case CALL:
                    if (statement.getInstruction().hasDelaySlot()) {
                        currentSegment.setEnd(address + statement.getNumBytes());
                        processedStatements.add(address + statement.getNumBytes());
                    }
                    int targetAddress = statement.decodedImm;
                    if (targetAddress == 0) {
                        List<Integer> potentialTargets = jumpHints.get(address);
                        if (potentialTargets != null) {
                            int i = 0;
                            for (Integer potentialTarget : potentialTargets) {
                                addCall(currentFunction, statement, address, potentialTarget & 0xFFFFFFFE, "call_target_" + Integer.toHexString(address) + "_" + i, true);
                                i++;
                            }
                        }
                        else {
                            currentFunction.getCalls().add(new Jump(address, 0, statement.getInstruction(), true));
                            debugPrintWriter.println("WARNING : Cannot determine dynamic target of CALL. Add -j 0x" + Format.asHex(address, 8) + "=addr1[, addr2[, ...]] to specify targets");
                        }
                    }
                    else {
                        addCall(currentFunction, statement, address, targetAddress, "", false);
                    }
                    break;
                case INT:
                    // This is FR-specific
                    if (statement.getInstruction() instanceof FrInstruction) {
                        Integer interruptAddress = interruptTable.get(statement.decodedImm);
                        if (statement.decodedImm == 0x40 && int40mapping != null) {
                            processInt40Call(currentFunction, address, (FrStatement)statement);
                        }
                        else {
                            Jump interruptCall = new Jump(address, interruptAddress, statement.getInstruction(), false);
                            currentFunction.getCalls().add(interruptCall);
                            Function interrupt = codeStructure.getFunctions().get(interruptAddress);
                            if (interrupt != null) {
                                interrupt.getCalledBy().put(interruptCall, currentFunction);
                            }
                            else {
                                debugPrintWriter.println("Error : following INT at 0x" + Format.asHex(address, 8) + ": no code found at 0x" + Format.asHex(interruptAddress, 8));
                            }
                        }
                    }
                    break;
            }

            if (statement.getInstruction().flowType == Instruction.FlowType.RET || statement.getInstruction().flowType == Instruction.FlowType.JMP) {
                if (statement.getInstruction().hasDelaySlot()) {
                    currentSegment.setEnd(address + statement.getNumBytes());
                    processedStatements.add(address + statement.getNumBytes());
                }
                // End of segment
                break;
            }
            address = codeStructure.getStatements().higherKey(address);
        }

        // Process jumps
        currentFunction.getJumps().addAll(jumps);
        boolean inProcessedSegment;
        for (Jump jump : jumps) {
            inProcessedSegment = false;
            for (CodeSegment segment : currentFunction.getCodeSegments()) {
                if (jump.getTarget() >= segment.getStart() && jump.getTarget() <= segment.getEnd()) {
                    // At first look, this part of code has already been processed.
                    // However, it happens (eg 001B77A4) that a jump ends on the delay slot of an unconditional JMP
                    // So we should consider we're really in a processed segment if
                    // - either it's a jump/call/return
                    Statement statement = codeStructure.getStatements().get(jump.getTarget());
                    if (statement != null && (statement.getInstruction().flowType == Instruction.FlowType.CALL
                            || statement.getInstruction().flowType == Instruction.FlowType.JMP
                            || statement.getInstruction().flowType == Instruction.FlowType.BRA
                            || statement.getInstruction().flowType == Instruction.FlowType.RET)) {
                        inProcessedSegment = true;
                        break;
                    }
                    // - or the next statement is also in the range
                    Integer addressFollowingTarget = codeStructure.getStatements().higherKey(jump.getTarget());
                    if (addressFollowingTarget != null && addressFollowingTarget >= segment.getStart() && addressFollowingTarget <= segment.getEnd()) {
                        inProcessedSegment = true;
                        break;
                    }
                    // Otherwise, it has to be followed...
                }
            }
            if (!inProcessedSegment) {
                try {
                    followFunction(currentFunction, jump.getTarget(), false);
                }
                catch (DisassemblyException e) {
                    debugPrintWriter.println("Error following jump at 0x" + Format.asHex(jump.getSource(), 8) + ": " + e.getMessage());
                }
            }
        }

        // Merge segments to clean up
        for (int i = 0; i < currentFunction.getCodeSegments().size(); i++) {
            // take a segment
            CodeSegment segmentA = currentFunction.getCodeSegments().get(i);
            int numBytesEndSegmentA = codeStructure.getStatements().get(segmentA.getEnd()).getNumBytes();
            // and try to merge it with all following ones
            for (int j = i + 1; j < currentFunction.getCodeSegments().size(); j++) {
                CodeSegment segmentB = currentFunction.getCodeSegments().get(j);
                int numBytesEndSegmentB = codeStructure.getStatements().get(segmentB.getEnd()).getNumBytes();
                // Why isn't "BFC00640 03E00008 ret" (and others) considered a RET ? => because they are in unprocessed statements (?)
                // Why isn't "BFC00898 E8A0 jrc $ra" considered a RET ?
                if ((segmentA.getStart() >= segmentB.getStart() - numBytesEndSegmentB && segmentA.getStart() <= segmentB.getEnd() + numBytesEndSegmentB)
                        || (segmentA.getEnd() + numBytesEndSegmentA >= segmentB.getStart() && segmentA.getEnd() <= segmentB.getEnd() + numBytesEndSegmentB)) {
                    // merge
                    segmentA.setStart(Math.min(segmentA.getStart(), segmentB.getStart()));
                    segmentA.setEnd(Math.max(segmentA.getEnd(), segmentB.getEnd()));
                    currentFunction.getCodeSegments().remove(j);
                }
            }
        }
    }

    private void resolveDynamicTarget(Function currentFunction, Integer address, List<Jump> jumps, Statement statement) {
        // First see if we have a hint
        List<Integer> potentialTargets = jumpHints.get(address);
        if (potentialTargets != null) {
            int i = 0;
            for (Integer potentialTarget : potentialTargets) {
                Jump jump = new Jump(address, potentialTarget, statement.getInstruction(), true);
                jumps.add(jump);
                currentFunction.getJumps().add(jump);
                codeStructure.getLabels().put(potentialTarget, new Symbol(potentialTarget, "jmp_target_" + Integer.toHexString(address) + "_" + i));
                i++;
            }
        }
        else {
            // try to resolve this typical FR compiler construct :
//                            000ABB66  A850                         CMP     #0x5,R<0>   ; number_of_elements
//                            000ABB68  F510                         BNC:D   part_2_of_sub_abb5e_              ; (skip)
//                            000ABB6A  8B0D                          MOV    R<0>,AC
//                            000ABB6C  9F8C 002A 5384               LDI:32  #0x<002A5384>,R12 ; base_address
//                            000ABB72  B42D                         LSL     #2,AC
//                            000ABB74  00CC                         LD      @(AC,R12),R12
//                            000ABB76  970C                         JMP     @R12
            if (
                    ((memory.loadInstruction16(address - 14) & 0xFF00) == 0xF500)   // BNC:D
                    &&((memory.loadInstruction16(address - 12) & 0xFF0F) == 0x8B0D) // MOV    R<0>,AC
                    && (memory.loadInstruction16(address - 10) == 0x9F8C) // LDI:32  #0x<base_address>,R12
                    && (memory.loadInstruction16(address - 4) == 0xB42D) // LSL     #2,AC
                    && (memory.loadInstruction16(address - 2) == 0x00CC) // LD      @(AC,R12),R12
                    && (memory.loadInstruction16(address    ) == 0x970C) // JMP     @R12
                    ) {

                int baseAddress = memory.loadInstruction32(address - 8);

                // Try to determine table size
                Integer size = null;
                // Short version (size <= 0xF) : 000ABB66  A850                         CMP     #0x5,R<0>   ; number_of_elements
                if (   ((memory.loadInstruction16(address - 16) & 0xFF00) == 0xA800) // comparing max value with register <n'>
                    && ((memory.loadInstruction16(address - 16) & 0x000F) == ((memory.loadInstruction16(address - 12) & 0x00F0) >> 4)) // check n == n'
                    ) {
                        size = (memory.loadInstruction16(address - 16) & 0x00F0) >> 4;
                }
                // Long version (size > 0xF):  0030A43A  C10C                         LDI:8   #0x10,R12
                //                             0030A43C  AAC4                         CMP     R12,R4
                else if (  ((memory.loadInstruction16(address - 18) & 0xF000) == 0xC000) // copying max value to register <m>
                        && ((memory.loadInstruction16(address - 16) & 0xFF00) == 0xAA00) // comparing register <m'> with register <n'>
                        && ((memory.loadInstruction16(address - 18) & 0x000F) == ((memory.loadInstruction16(address - 16) & 0x00F0) >> 4)) // check m == m'
                        && ((memory.loadInstruction16(address - 16) & 0x000F) == ((memory.loadInstruction16(address - 12) & 0x00F0) >> 4)) // check n == n'
                        ) {
                    size = (memory.loadInstruction16(address - 18) & 0x0FF0) >> 4;
                }

                if (size != null) {
                    // Match complete. Add jumps
                    try {
                        for (int i = 0; i < size; i++) {
                            int potentialTarget = memory.loadInstruction32(baseAddress + (i << 2));
                            Jump jump = new Jump(address, potentialTarget, statement.getInstruction(), true);
                            jumps.add(jump);
                            currentFunction.getJumps().add(jump);
                            codeStructure.getLabels().put(potentialTarget, new Symbol(potentialTarget, "jmp_target_" + Integer.toHexString(address) + "_" + i, null));
                        }
                        //debugPrintWriter.println(Format.asHex(baseAddress, 8) + " -- # Table for jump at " + Format.asHex(address, 8) + "#-m 0x" + Format.asHex(baseAddress, 8) + "-0x" + Format.asHex(baseAddress + size * 4 - 1, 8) + "=DATA:L");
                    }
                    catch (NullPointerException e) {
                        debugPrintWriter.println("Cannot follow dynamic jump at 0x" + Format.asHex(address, 8) + " (no table at 0x" + Format.asHex(baseAddress - 8, 8) +")");
                    }
                }
                else {
                    debugPrintWriter.println("Cannot follow dynamic jump at 0x" + Format.asHex(address, 8));
                }
            }
            else {
                // try to resolve this alternate typical compiler construct :
//                                0012785A  C90C           LDI:8   #0x90,R12
//                                0012785C  AAC4           CMP     R12,R4
//                                0012785E  E404           BC      label_127868_              ; (skip)
//                                00127860  9F8C 0012 7C02 LDI:32  #0x00127C02,R12
//                                00127866  9F0C           JMP:D   @R12; part_2_of_sub_127848_ (skip)
//                                label_127868_:
//                                00127868  8B4D            MOV    R4,AC
//                                0012786A  9F8C 0024 A438 LDI:32  #0x0024A438,R12
//                                00127870  B42D           LSL     #2,AC
//                                00127872  00CC           LD      @(AC,R12),R12
//                                00127874  970C           JMP     @R12

                if (
                           (memory.loadInstruction16(address - 22) == 0xE404) // BC +04
                        && (memory.loadInstruction16(address - 20) == 0x9F8C) // LDI32:xxxx xxxx,R12
                        && (memory.loadInstruction16(address - 14) == 0x9F0C) // JMP:D   @R12;
                        &&((memory.loadInstruction16(address - 12) & 0xFF0F) == 0x8B0D) // MOV    R<0>,AC
                        && (memory.loadInstruction16(address - 10) == 0x9F8C) // LDI:32  #0x<base_address>,R12
                        && (memory.loadInstruction16(address - 4) == 0xB42D) // LSL     #2,AC
                        && (memory.loadInstruction16(address - 2) == 0x00CC) // LD      @(AC,R12),R12
                        && (memory.loadInstruction16(address    ) == 0x970C) // JMP     @R12
                        ) {

                    int baseAddress = memory.loadInstruction32(address - 8);

                    // Try to determine table size
                    Integer size = null;
                    // Short version (size <= 0xF) : 000B442A  A844           CMP     #0x4,R<4>   ; number_of_elements
                    if (   ((memory.loadInstruction16(address - 24) & 0xFF00) == 0xA800) // comparing max value with register <n'>
                            && ((memory.loadInstruction16(address - 24) & 0x000F) == ((memory.loadInstruction16(address - 12) & 0x00F0) >> 4)) // check n == n'
                            ) {
                        size = (memory.loadInstruction16(address - 24) & 0x00F0) >> 4;
                    }
                    // Long version (size > 0xF):  0012785A  C90C           LDI:8   #0x90,R12
                    //                             0012785C  AAC4           CMP     R12,R4
                    else if (  ((memory.loadInstruction16(address - 26) & 0xF000) == 0xC000) // copying max value to register <m>
                            && ((memory.loadInstruction16(address - 24) & 0xFF00) == 0xAA00) // comparing register <m'> with register <n'>
                            && ((memory.loadInstruction16(address - 26) & 0x000F) == ((memory.loadInstruction16(address - 24) & 0x00F0) >> 4)) // check m == m'
                            && ((memory.loadInstruction16(address - 24) & 0x000F) == ((memory.loadInstruction16(address - 12) & 0x00F0) >> 4)) // check n == n'
                            ) {
                        size = (memory.loadInstruction16(address - 26) & 0x0FF0) >> 4;
                    }

                    if (size != null) {
                        // Match complete. Add jumps
                        try {
                            for (int i = 0; i < size; i++) {
                                int potentialTarget = memory.loadInstruction32(baseAddress + (i << 2));
                                Jump jump = new Jump(address, potentialTarget, statement.getInstruction(), true);
                                jumps.add(jump);
                                currentFunction.getJumps().add(jump);
                                codeStructure.getLabels().put(potentialTarget, new Symbol(potentialTarget, "jmp_target_" + Integer.toHexString(address) + "_" + i, null));
                            }
//                                          debugPrintWriter.println(Format.asHex(baseAddress, 8) + " -- # Table for jump at " + Format.asHex(address, 8) + "#-m 0x" + Format.asHex(baseAddress, 8) + "-0x" + Format.asHex(baseAddress + size * 4 - 1, 8) + "=DATA:L");
                        }
                        catch (NullPointerException e) {
                            debugPrintWriter.println("Cannot follow dynamic jump at 0x" + Format.asHex(address, 8) + " (no table at 0x" + Format.asHex(baseAddress - 8, 8) +")");
                        }
                    }
                    else {
                        debugPrintWriter.println("Cannot follow dynamic jump at 0x" + Format.asHex(address, 8));
                    }
                }
                else {
                    debugPrintWriter.println("Cannot follow dynamic jump at 0x" + Format.asHex(address, 8));
                }
            }
        }
    }

    private void addCall(Function currentFunction, Statement statement, Integer sourceAddress, int targetAddress, String defaultName, boolean isDynamic) throws IOException {
        Jump call = new Jump(sourceAddress, targetAddress, statement.getInstruction(), isDynamic);
        currentFunction.getCalls().add(call);
        Function function = codeStructure.getFunctions().get(targetAddress);
        if (function == null) {
            // new Function
            function = new Function(targetAddress, defaultName, "", Function.Type.STANDARD);
            codeStructure.getFunctions().put(targetAddress, function);
            try {
                followFunction(function, targetAddress, false);
            }
            catch (DisassemblyException e) {
                debugPrintWriter.println("Error following call at 0x" + Format.asHex(sourceAddress, 8) + ": " + e.getMessage());
            }
        }
        else {
            // Already processed. If it was an unknown entry point, declare it a standard function now that some code calls it
            if (function.getType() == Function.Type.UNKNOWN) {
                function.setType(Function.Type.STANDARD);
            }
        }
        function.getCalledBy().put(call, currentFunction);
    }

    private void processInt40Call(Function currentFunction, Integer address, FrStatement statement) throws IOException {
        // REALOS System calls
        // Determine R12 before the call by reading the statements up to 200 bytes backwards (168 needed for call at 0x001824D0)
        // TODO : ideally, should follow program flow by climbing back function coderanges and not addresses in a straight line.
        // TODO : Here, we run the risk of not catching the good R12 value (not the case in practice)...
        Integer r12 = null;
        boolean r12SignExtend = false;
        for (int offset = 1; offset < 100; offset++) {
            FrStatement candidateStatement = (FrStatement) codeStructure.getStatements().get(address - 2 * offset);
            if (candidateStatement != null) {
                if (((FrInstruction)(candidateStatement.getInstruction())).encoding == 0x9F80 && candidateStatement.decodedI == 12) {
                    /* LDI:32 #i32, R12 */
                    r12 = candidateStatement.decodedImm;
                    break;
                }
                if (((FrInstruction)(candidateStatement.getInstruction())).encoding == 0xC000 && candidateStatement.decodedI == 12) {
                    /* LDI:8 #i8, R12 */
                    if (r12SignExtend) {
                        r12 = BinaryArithmetics.signExtend(8, candidateStatement.decodedImm);
                    }
                    else {
                        r12 = candidateStatement.decodedImm;
                    }
                    break;
                }
                if (((FrInstruction)(candidateStatement.getInstruction())).encoding == 0x9780 && candidateStatement.decodedI == 12) {
                    /* EXTSB R12 */
                    r12SignExtend = true;
                }
            }
        }
        if (r12 == null) {
            debugPrintWriter.println("Error : cannot determine R12 value for INT40 at 0x" + Format.asHex(address, 8));
        }
        else {
            Integer int40targetAddress = int40mapping.get(r12);
            if (int40targetAddress == null) {
                debugPrintWriter.println("Error : INT40 at 0x" + Format.asHex(address, 8) + " with value R12=0x" + Format.asHex(r12, 8) + " does not match a computed address...");
            }
            else {
                Jump interrupt40Call = new Jump(address, int40targetAddress, statement.getInstruction() /* TODO should characterize that it is a INT40 call */, false);
                currentFunction.getCalls().add(interrupt40Call);
                Function target = codeStructure.getFunctions().get(int40targetAddress);
                if (target == null) {
                    // new Function
                    target = new Function(int40targetAddress, "", "", Function.Type.STANDARD);
                    codeStructure.getFunctions().put(int40targetAddress, target);
                    try {
                        followFunction(target, int40targetAddress, false);
                    }
                    catch (DisassemblyException e) {
                        debugPrintWriter.println("Error : INT40 at 0x" + Format.asHex(address, 8) + " with value R12=0x" + Format.asHex(r12, 8) + " targets address 0x" + Format.asHex(int40targetAddress, 8) + " where no code can be found.");
                    }
                }
                else {
                    // Already processed. If it was an unknown entry point, declare it a standard function now that some code calls it
                    if (target.getType() == Function.Type.UNKNOWN) {
                        target.setType(Function.Type.STANDARD);
                    }
                }
                target.getCalledBy().put(interrupt40Call, currentFunction);
                if (StringUtils.isBlank(statement.getCommentString())) {
                    Symbol symbol = symbols.get(int40targetAddress);
                    if (symbol != null) {
                        statement.setCommentString("" + int40targetAddress);
//                        statement.comment = symbol.getName();
                    }
                }
            }
        }
    }


}
