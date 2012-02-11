package com.nikonhacker.dfr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.Memory;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

public class CodeStructure {

    public static final int INTERRUPT_VECTOR_LENGTH = 0x400;

    private int entryPoint;

    // TODO : Optimize by merging instructions/labels/functions/returns/ends in a same table with various properties ?

    /** Map address -> Instruction */
    TreeMap<Integer, DisassembledInstruction> instructions = new TreeMap<Integer, DisassembledInstruction>();
    
    /** Map address -> Labels */
    Map<Integer, Symbol> labels = new TreeMap<Integer, Symbol>();
    
    /** Map address -> Functions */
    SortedMap<Integer, Function> functions = new TreeMap<Integer, Function>();

    /** Map address -> Start of corresponding function */
    Map<Integer, Integer> returns = new TreeMap<Integer, Integer>();

    /** Map address -> Start of corresponding function 
     *  (This Map may differ from returns due to delay slots)
     */
    Map<Integer, Integer> ends = new TreeMap<Integer, Integer>();

    public CodeStructure(int address) {
        this.entryPoint = address;
    }

    public int getEntryPoint() {
        return entryPoint;
    }

    public TreeMap<Integer, DisassembledInstruction> getInstructions() {
        return instructions;
    }

    
    public Map<Integer, Symbol> getLabels() {
        return labels;
    }

    private boolean isLabel(Integer address) {
        return labels.containsKey(address);
    }

    
    public SortedMap<Integer, Function> getFunctions() {
        return functions;
    }

    private boolean isFunction(Integer address) {
        return functions.containsKey(address);
    }

    private String getFunctionName(Integer address) {
        Symbol symbol = functions.get(address);
        return symbol==null?null:symbol.getName();
    }


    public Map<Integer, Integer> getReturns() {
        return returns;
    }

    private boolean isReturn(Integer address) {
        return returns.containsKey(address);
    }


    public Map<Integer, Integer> getEnds() {
        return ends;
    }

    private boolean isEnd(Integer address) {
        return ends.containsKey(address);
    }

    /**
     * Post-process instructions to retrieve code structure
     *
     *
     * @param ranges The defined ranges in the binary file
     * @param memory The memory image
     * @param symbols The symbols defined to override auto-generated function names
     * @param useOrdinalNames if true, use ordinal numbers instead of address to generate names
     * @param debugPrintWriter a PrintWriter to write debug/info messages to
     * @throws IOException
     */
    public void postProcess(SortedSet<Range> ranges, Memory memory, Map<Integer, Symbol> symbols, boolean useOrdinalNames, PrintWriter debugPrintWriter) throws IOException {


        Set<Integer> processedInstructions = new HashSet<Integer>();

        debugPrintWriter.println("Following functions, starting at entry point...");
        Function main = new Function(entryPoint, "main", "", Function.Type.MAIN);
        functions.put(entryPoint, main);
        followFunction(main, entryPoint, processedInstructions, debugPrintWriter);


        debugPrintWriter.println("Following functions, starting at each interrupt...");
        for (Range range : ranges) {
            if (range instanceof InterruptVectorRange) {
                for (int interruptNumber = 0; interruptNumber < INTERRUPT_VECTOR_LENGTH / 4; interruptNumber++) {
                    Integer address = memory.load32(range.getStart() + 4 * (0x100 - interruptNumber - 1));
                    if (!functions.containsKey(address)) {
                        Function function = new Function(address, "interrupt_0x" + Format.asHex(interruptNumber,2) + "_", "");
                        functions.put(address, function);
                        followFunction(function, address, processedInstructions, debugPrintWriter);
                    }
                }
            }
        }


        debugPrintWriter.println("Processing remaining instructions as 'unknown' functions...");
        Map.Entry<Integer, DisassembledInstruction> entry = instructions.firstEntry();
        while (entry != null) {
            Integer address = entry.getKey();
            if (!processedInstructions.contains(address)) {
                Function function = new Function(address, "", "", Function.Type.UNKNOWN);
                functions.put(address, function);
                followFunction(function, address, processedInstructions, debugPrintWriter);
            }
            entry = instructions.higherEntry(address);
        }


        debugPrintWriter.println("Generating names for functions...");
        int functionNumber = 1;
        for (Integer address : functions.keySet()) {
            Function function = functions.get(address);
            if (StringUtils.isBlank(function.getName())) {
                String functionId = useOrdinalNames?("" + functionNumber):Integer.toHexString(address);
                if (function.getType() == Function.Type.UNKNOWN) {
                    function.setName("unknown_" + functionId + "_");
                }
                else {
                    function.setName("function_" + functionId + "_");
                }
            }
            // increment even if unused, to make output stable no matter what future replacements will occur
            functionNumber++;
        }


        debugPrintWriter.println("Overwriting function names with given symbols...");
        if (symbols != null) {
            for (Integer address : symbols.keySet()) {
                if (isFunction(address)) {
                    Function function = functions.get(address);
                    Symbol symbol = symbols.get(address);
                    function.setName(symbol.getName());
                    function.setComment(symbol.getComment());
                }
            }
        }


        debugPrintWriter.println("Generating names for labels (this can take some time)...");
        int labelNumber = 1;
        // Temporary storage for names given to returns to make sure they are unique
        Set<String> usedReturnLabels = new HashSet<String>();
        for (Integer address : labels.keySet()) {
            boolean match = false;
            for (Function function : functions.values()) {
                if (function.getAddress() == address) {
                    labels.put(address, new Symbol(address, "start_of_" + getFunctionName(address), null));
                    match = true;
                }
                else {
                    for (int i = 0; i < function.getCodeSegments().size(); i++) {
                        CodeSegment codeSegment = function.getCodeSegments().get(i);
                        if (codeSegment.getStart() == address) {
                            labels.put(address, new Symbol(address, "part_" + (i+1) + "_of_" + function.getName(), null));
                            match = true;
                            break;
                        }
                    }
                }
            }
            if (!match) {
                String label;
                if (isReturn(address)) {
                    Integer matchingFunctionStart = returns.get(address);
                    if (matchingFunctionStart != null) {
                        String functionName = getFunctionName(matchingFunctionStart);
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
                        label = "end_unidentified_fn_l_" + (useOrdinalNames?("" + labelNumber):Integer.toHexString(address)) + "_";
                    }
                }
                else {
                    label = "label_" + (useOrdinalNames?("" + labelNumber):Integer.toHexString(address)) + "_";
                }
                labels.put(address, new Symbol(address, label, null));
            }
            // increment even if unused, to make output stable no matter what future replacements will occur
            labelNumber++;
        }

    }

    void followFunction(Function currentFunction, Integer address, Set<Integer> processedInstructions, PrintWriter debugPrintWriter) throws IOException {
        if (instructions.get(address) == null) {
            debugPrintWriter.println("ERROR : no decoded instruction at 0x" + Format.asHex(address, 8) + " (not a CODE range)");
            return;
        }
        CodeSegment currentSegment = new CodeSegment();
        currentFunction.getCodeSegments().add(currentSegment);
        List<Jump> jumps = new ArrayList<Jump>();
        currentSegment.setStart(address);
        while(address != null) {
            DisassembledInstruction instruction = instructions.get(address);
            processedInstructions.add(address);
            currentSegment.setEnd(address);
            if (instruction.opcode.isReturn) {
//                    labels.put(address, new Symbol(address, "", ""));
                returns.put(address, currentFunction.getAddress());
                ends.put(address + (instruction.opcode.hasDelaySlot ? 2 : 0), currentFunction.getAddress());
            }
            if (instruction.opcode.isCall) {
                if (instruction.opcode.hasDelaySlot) {
                    currentSegment.setEnd(address + 2);
                    processedInstructions.add(address + 2);
                }
                Integer targetAddress = instruction.decodedX;
                Jump call = new Jump(address, targetAddress, false);
                currentFunction.getCalls().add(call);
                if (targetAddress == null || targetAddress == 0) {
                    debugPrintWriter.println("WARNING : Cannot determine target of call made at 0x" + Format.asHex(address, 8) + " (dynamic address ?)");
                }
                else {
                    Function function = functions.get(targetAddress);
                    if (function == null) {
                        // new Function
                        function = new Function(targetAddress, "", "", Function.Type.STANDARD);
                        functions.put(targetAddress, function);
                        followFunction(function, targetAddress, processedInstructions, debugPrintWriter);
                    }
                    else {
                        // Already processed. If it was an unknown entry point, declare it a standard function now that some code calls it
                        if (function.getType() == Function.Type.UNKNOWN) {
                            function.setType(Function.Type.STANDARD);
                        }
                    }
                    function.getCalledBy().put(call, currentFunction);
                }
            }
            if (instruction.opcode.isJumpOrBranch) {
                if (instruction.decodedX != 0) {
                    labels.put(instruction.decodedX, new Symbol(instruction.decodedX, "", ""));
                    Jump jump = new Jump(address, instruction.decodedX, instruction.opcode.isConditional);
                    jumps.add(jump);
                    currentFunction.getJumps().add(jump);
                }
            }
            if (instruction.opcode.isReturn || (instruction.opcode.isJumpOrBranch && !instruction.opcode.isConditional)) {
                if (instruction.opcode.hasDelaySlot) {
                    currentSegment.setEnd(address + 2);
                    processedInstructions.add(address + 2);
                }
                break;
            }
            address = instructions.higherKey(address);
        }

        // Process jumps
        currentFunction.getJumps().addAll(jumps);
        boolean inProcessedSegment;
        for (Jump jump:jumps) {
            inProcessedSegment = false;
            for (CodeSegment segment : currentFunction.getCodeSegments()) {
                if (jump.target >= segment.start && jump.target <= segment.end) {
                    // At first look, this part of code has already been processed.
                    // However, it happens (eg 001B77A4) that a jump ends on the delay slot of an unconditional JMP
                    // So we should consider we're really in a processed segment if
                    // - either it's a jump/call/return
                    DisassembledInstruction instruction = instructions.get(jump.target);
                    if (instruction != null && (instruction.opcode.isCall || instruction.opcode.isJumpOrBranch || instruction.opcode.isReturn)) {
                        inProcessedSegment = true;
                        break;
                    }
                    // - or the next instruction is also in the range
                    Integer addressFollowingTarget = instructions.higherKey(jump.target);
                    if (addressFollowingTarget != null && addressFollowingTarget >= segment.start && addressFollowingTarget <= segment.end) {
                        inProcessedSegment = true;
                        break;
                    }
                    // Otherwise, it has to be followed...
                }
            }
            if (!inProcessedSegment) {
                followFunction(currentFunction, jump.target, processedInstructions, debugPrintWriter);
            }
        }

        // Merge segments to clean up
        for (int i = 0; i < currentFunction.getCodeSegments().size(); i++) {
            // take a segment
            CodeSegment segmentA = currentFunction.getCodeSegments().get(i);
            // and try to merge it with all following ones
            for (int j = i + 1; j < currentFunction.getCodeSegments().size(); j++) {
                CodeSegment segmentB = currentFunction.getCodeSegments().get(j);
                if ((segmentA.start >= segmentB.start - 2 && segmentA.start <= segmentB.end + 2)
                        || (segmentA.end >= segmentB.start - 2 && segmentA.end <= segmentB.end + 2)) {
                    // merge
                    segmentA.setStart(Math.min(segmentA.getStart(), segmentB.getStart()));
                    segmentA.setEnd(Math.max(segmentA.getEnd(), segmentB.getEnd()));
                    currentFunction.getCodeSegments().remove(j);
                }
            }
        }
    }


    public void writeDisassembly(Writer writer, Range range, Set<OutputOption> outputOptions) throws IOException {

        // Start output
        Integer address = range.getStart();
        DisassembledInstruction instruction = instructions.get(address);

        int memoryFileOffset = outputOptions.contains(OutputOption.OFFSET)?(range.start - range.fileOffset):0;

        while (instruction != null && address < range.getEnd()) {
            writeInstruction(writer, address, instruction, memoryFileOffset);

            address = instructions.higherKey(address);
            instruction = address==null?null:instructions.get(address);
        }

    }

    public void writeInstruction(Writer writer, Integer address, DisassembledInstruction instruction, int memoryFileOffset) throws IOException {
        // function
        if (isFunction(address)) {
            Function function = functions.get(address);
            writer.write("\n; ************************************************************************\n");
            writer.write("; " + function.getTitleLine() + "\n");
            writer.write("; ************************************************************************\n");
        }

        // label
        if (isLabel(address)) {
            writer.write(labels.get(address).getName() + ":\n");
        }

        if (instruction.opcode.isCall || instruction.opcode.isJumpOrBranch) {
            // replace address by label
            if (instruction.comment.length() > 0) {
                try {
                    int targetAddress = Format.parseUnsigned(instruction.comment);
                    Symbol symbol;
                    if (instruction.opcode.isCall) {
                        symbol = functions.get(targetAddress);
                    }
                    else {
                        symbol = labels.get(targetAddress);
                    }
                    if (symbol != null) {
                        instruction.comment = symbol.getName();
                    }
                    if (instruction.opcode.isJumpOrBranch) {
                        //if (areInSameRange(functions,  address, targetAddress))
                        instruction.comment += (targetAddress>address?" (skip)":" (loop)");
                    }
                } catch (ParsingException e) {
                    // noop
                }
            }
            else {
                try {
                    int targetAddress = Format.parseUnsigned(instruction.operands);
                    if (instruction.opcode.isCall) {
                        Function function = functions.get(targetAddress);
                        if (function != null) {
                            instruction.operands = function.getName();
                        }
                    }
                    else {
                        Symbol label = labels.get(targetAddress);
                        if (label != null) {
                            instruction.operands = label.getName();
                        }
                    }
                    if (instruction.opcode.isJumpOrBranch) {
                        //TODO only if(areInSameRange(address, targetAddress))
                        instruction.comment =  targetAddress>address?"(skip)":"(loop)";
                    }
                } catch (ParsingException e) {
                    // noop
                }
            }
        }

        // print instruction
        Dfr.printDisassembly(writer, instruction, address, memoryFileOffset);

        // after return from function
        if (isEnd(address)) {
            Integer matchingStart = ends.get(address);
            if (matchingStart == null) {
                writer.write("; end of an unidentified function (never called)\n");
            }
            else {
                writer.write("; end of " + getFunctionName(matchingStart) + "\n");
            }
            writer.write("; ------------------------------------------------------------------------\n\n");
        }
    }

    public Function findFunctionIncluding(int address) {
        for (Function function : functions.values()) {
            for (CodeSegment codeSegment : function.getCodeSegments()) {
                if (address >= codeSegment.getStart() && address <= codeSegment.getEnd()) {
                    return function;
                }
            }
        }
        return null;
    }
}
