package com.nikonhacker.disassembly;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.fr.FrInstruction;
import com.nikonhacker.disassembly.fr.FrInstructionSet;
import com.nikonhacker.disassembly.fr.FrStatement;
import com.nikonhacker.disassembly.fr.InterruptVectorRange;
import com.nikonhacker.emu.memory.Memory;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public abstract class CodeAnalyzer {

    private CodeStructure codeStructure;
    private SortedSet<Range> ranges;
    protected Memory memory;
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
                        FrStatement statement = (FrStatement) codeStructure.getStatement(int40address + 0x3E);
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
        codeStructure.putFunction(codeStructure.getEntryPoint(), main);
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
            Function function = codeStructure.getFunction(address);
            if (function == null) {
                function = new Function(address, name, "", Function.Type.INTERRUPT);
                codeStructure.putFunction(address, function);
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
        Map.Entry<Integer, Statement> entry = codeStructure.getFirstStatementEntry();
        while (entry != null) {
            Integer address = entry.getKey();
            if (       !processedStatements.contains(address) // Not processed yet
                    && !codeStructure.getStatement(address).isPotentialStuffing() // Not stuffing
                    ) {
                // OK, let's process it
                Function function = new Function(address, "", "", Function.Type.UNKNOWN);
                codeStructure.putFunction(address, function);
                try {
                    followFunction(function, address, false);
                }
                catch (DisassemblyException e) {
                    debugPrintWriter.println("SHOULD NOT HAPPEN. Please report this case on the forums ! : Error disassembling unknown function at 0x" + Format.asHex(address , 2) + ": " + e.getMessage());
                }
            }
            entry = codeStructure.getStatementEntryAfter(address);
        }


        debugPrintWriter.println("Generating names for functions...");
        int functionNumber = 1;
        for (Integer address : codeStructure.getAllFunctionAddresses()) {
            Function function = codeStructure.getFunction(address);
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
                    Function function = codeStructure.getFunction(address);
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

        for (Integer address : codeStructure.getAllFunctionAddresses()) {
            Function function = codeStructure.getFunction(address);
            if (codeStructure.isLabel(address)) {
                codeStructure.putLabel(address, new Symbol(address, "start_of_" + codeStructure.getFunctionName(address)));
            }
            for (int i = 0; i < function.getCodeSegments().size(); i++) {
                CodeSegment codeSegment = function.getCodeSegments().get(i);
                int startAddress = codeSegment.getStart();
                if (codeStructure.isLabel(startAddress)) {
                    codeStructure.putLabel(startAddress, new Symbol(startAddress, "part_" + (i + 1) + "_of_" + function.getName()));
                }
            }
        }

        // Temporary storage for names given to returns to make sure they are unique
        Set<String> usedReturnLabels = new HashSet<String>();
        for (Integer address : codeStructure.getAllLabelAddresses()) {
            if (codeStructure.getLabelName(address).length() == 0) {
                String label;
                if (codeStructure.isReturn(address)) {
                    Integer matchingFunctionStart = codeStructure.getReturn(address);
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
                    label = codeStructure.getLabelName(address);
                    if (StringUtils.isBlank(label)) {
                        label = "loc_" + (outputOptions.contains(OutputOption.ORDINAL)?("" + labelNumber):Integer.toHexString(address)) + "_";
                    }
                }
                codeStructure.putLabel(address, new Symbol(address, label));
            }
            // increment even if unused, to make output stable no matter what future replacements will occur
            labelNumber++;
        }

        // Override label names by symbols in dfr.txt or dtx.txt file
        for (Integer labelAddress : symbols.keySet()) {
            if (!codeStructure.isFunction(labelAddress)) {
                // This is not a function symbol, it's a code label or a variable
                Symbol label = codeStructure.getLabel(labelAddress);
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
        Function function = codeStructure.getFunction(address);
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
        if (!codeStructure.isStatement(address)) {
            throw new DisassemblyException("No decoded statement at 0x" + Format.asHex(address, 8) + " (not a CODE range)");
        }
        CodeSegment currentSegment = new CodeSegment();
        currentFunction.getCodeSegments().add(currentSegment);
        List<Jump> jumps = new ArrayList<Jump>();
        currentSegment.setStart(address);
        while(address != null) {
            if (stopAtFirstProcessedStatement && processedStatements.contains(address)) {
                Integer previousAddress = codeStructure.getAddressOfStatementBefore(address);
                // Check we're not in delay slot. We shouldn't stop on delay slot
                if (previousAddress == null || !codeStructure.getStatement(previousAddress).getInstruction().hasDelaySlot()) {
                    break;
                }
            }
            Statement statement = codeStructure.getStatement(address);
            processedStatements.add(address);
            currentSegment.setEnd(address);
            switch (statement.getInstruction().getFlowType()) {
                case RET:
                    codeStructure.putReturn(address, currentFunction.getAddress());
                    codeStructure.putEnd(address + (statement.getInstruction().hasDelaySlot() ? statement.getNumBytes() : 0), currentFunction.getAddress());
                    break;
                case JMP:
                case BRA:
                    if (statement.decodedImm != 0) {
                        codeStructure.putLabel(statement.decodedImm, new Symbol(statement.decodedImm, "", ""));
                        Jump jump = new Jump(address, statement.decodedImm & 0xFFFFFFFE, statement.getInstruction(), false);
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
                        addCall(currentFunction, statement, address, targetAddress & 0xFFFFFFFE, "", false);
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
                            Function interrupt = codeStructure.getFunction(interruptAddress);
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
            address = codeStructure.getAddressOfStatementAfter(address);
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
                    Statement statement = codeStructure.getStatement(jump.getTarget());
                    if (statement != null && (statement.getInstruction().flowType == Instruction.FlowType.CALL
                            || statement.getInstruction().flowType == Instruction.FlowType.JMP
                            || statement.getInstruction().flowType == Instruction.FlowType.BRA
                            || statement.getInstruction().flowType == Instruction.FlowType.RET)) {
                        inProcessedSegment = true;
                        break;
                    }
                    // - or the next statement is also in the range
                    Integer addressFollowingTarget = codeStructure.getAddressOfStatementAfter(jump.getTarget());
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
            Statement lastStatementSegmentA = codeStructure.getStatement(segmentA.getEnd());
            if (lastStatementSegmentA == null) {
                debugPrintWriter.println("Error : no disassembled statement found at 0x" + Format.asHex(segmentA.getEnd(), 8));
            }
            else {
                int numBytesEndSegmentA = lastStatementSegmentA.getNumBytes();
                // and try to merge it with all following ones
                for (int j = i + 1; j < currentFunction.getCodeSegments().size(); j++) {
                    CodeSegment segmentB = currentFunction.getCodeSegments().get(j);
                    if (codeStructure.getStatement(segmentB.getEnd()) == null) {
                        debugPrintWriter.println("Error : no disassembled statement found at 0x" + Format.asHex(segmentB.getEnd(), 8));
                    }
                    else {
                        int numBytesEndSegmentB = codeStructure.getStatement(segmentB.getEnd()).getNumBytes();
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
        }
    }

    protected abstract int[] getJmpTableAddressSize(int address);
    
    private void resolveDynamicTarget(Function currentFunction, Integer address, List<Jump> jumps, Statement statement) {
        // First see if we have a hint
        List<Integer> potentialTargets = jumpHints.get(address);
        if (potentialTargets != null) {
            int i = 0;
            for (Integer potentialTarget : potentialTargets) {
                Jump jump = new Jump(address, potentialTarget & CodeStructure.IGNORE_ISA_BIT, statement.getInstruction(), true);
                jumps.add(jump);
                currentFunction.getJumps().add(jump);
                codeStructure.putLabel(potentialTarget, new Symbol(potentialTarget & CodeStructure.IGNORE_ISA_BIT, "jmp_target_" + Integer.toHexString(address) + "_" + i));
                i++;
            }
        }
        else {
            // Try to determine table size
            
            int addressSize[] = getJmpTableAddressSize(address);
            if (addressSize!=null) {
                try {
                    for (int i = 0; i < addressSize[1]; i++) {
                        int potentialTarget = memory.loadInstruction32(addressSize[0] + (i << 2));
                        Jump jump = new Jump(address, potentialTarget & CodeStructure.IGNORE_ISA_BIT, statement.getInstruction(), true);
                        jumps.add(jump);
                        currentFunction.getJumps().add(jump);
                        codeStructure.putLabel(potentialTarget, new Symbol(potentialTarget, "jmp_target_" + Integer.toHexString(address) + "_" + i, null));
                    }
                }
                catch (NullPointerException e) {
                    debugPrintWriter.println("Cannot follow dynamic jump at 0x" + Format.asHex(address, 8) + " (no table at 0x" + Format.asHex(addressSize[0] - 8, 8) +")");
                }
            } else {
                debugPrintWriter.println("Cannot follow dynamic jump at 0x" + Format.asHex(address, 8));
            }
        }
    }

    private void addCall(Function currentFunction, Statement statement, Integer sourceAddress, int targetAddress, String defaultName, boolean isDynamic) throws IOException {
        Jump call = new Jump(sourceAddress, targetAddress, statement.getInstruction(), isDynamic);
        currentFunction.getCalls().add(call);
        Function function = codeStructure.getFunction(targetAddress);
        if (function == null) {
            // new Function
            function = new Function(targetAddress, defaultName, "", Function.Type.STANDARD);
            codeStructure.putFunction(targetAddress, function);
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
        // µITRON REALOS System calls
        // Determine R12 before the call by reading the statements up to 200 bytes backwards (168 needed for call at 0x001824D0)
        // TODO : ideally, should follow program flow by climbing back function coderanges and not addresses in a straight line.
        // TODO : Here, we run the risk of not catching the good R12 value (not the case in practice)...
        Integer r12 = null;
        boolean r12SignExtend = false;
        for (int offset = 1; offset < 100; offset++) {
            FrStatement candidateStatement = (FrStatement) codeStructure.getStatement(address - 2 * offset);
            if (candidateStatement != null) {
                if (candidateStatement.getInstruction() instanceof FrInstructionSet.Ldi32FrInstruction && candidateStatement.decodedRiRsFs == 12) {
                    /* LDI:32 #i32, R12 */
                    r12 = candidateStatement.decodedImm;
                    break;
                }
                if (candidateStatement.getInstruction() instanceof FrInstructionSet.Ldi8FrInstruction && candidateStatement.decodedRiRsFs == 12) {
                    /* LDI:8 #i8, R12 */
                    if (r12SignExtend) {
                        r12 = BinaryArithmetics.signExtend(8, candidateStatement.decodedImm);
                    }
                    else {
                        r12 = candidateStatement.decodedImm;
                    }
                    break;
                }
                if (candidateStatement.getInstruction() instanceof FrInstructionSet.ExtsbFrInstruction && candidateStatement.decodedRiRsFs == 12) {
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
                Function target = codeStructure.getFunction(int40targetAddress);
                if (target == null) {
                    // new Function
                    target = new Function(int40targetAddress, "", "", Function.Type.STANDARD);
                    codeStructure.putFunction(int40targetAddress, target);
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
                        statement.setCommentString("0x" + Format.asHex(int40targetAddress, 8));
                    }
                }
            }
        }
    }


}
