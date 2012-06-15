package com.nikonhacker.dfr;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
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
    private final Set<Integer> processedInstructions;
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

        processedInstructions = new HashSet<Integer>();
        interruptTable = new HashMap<Integer, Integer>();

        int40mapping = null;
    }

    /**
     * Post-process instructions to retrieve code structure
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

        if (outputOptions.contains(OutputOption.INT40)) {
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
                        DisassembledInstruction instruction = codeStructure.instructions.get(int40address + 0x3E);
                        int baseAddress = instruction.decodedX;
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
        Function main = new Function(codeStructure.entryPoint, "main", "", Function.Type.MAIN);
        codeStructure.functions.put(codeStructure.entryPoint, main);
        try {
            followFunction(main, codeStructure.entryPoint, false);
        }
        catch (DisassemblyException e) {
            debugPrintWriter.println("Error disassembling 'main' code at 0x" + Format.asHex(codeStructure.entryPoint, 2) + ": " + e.getMessage());
        }


        debugPrintWriter.println("Following flow starting at each interrupt...");
        for (Integer interruptNumber : interruptTable.keySet()) {
            Integer address = interruptTable.get(interruptNumber);
            String name = "interrupt_0x" + Format.asHex(interruptNumber, 2) + "_";
            Function function = codeStructure.functions.get(address);
            if (function == null) {
                function = new Function(address, name, "", Function.Type.INTERRUPT);
                codeStructure.functions.put(address, function);
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


        debugPrintWriter.println("Processing remaining instructions as 'unknown' functions...");
        Map.Entry<Integer, DisassembledInstruction> entry = codeStructure.instructions.firstEntry();
        while (entry != null) {
            Integer address = entry.getKey();
            if (!processedInstructions.contains(address)
                && codeStructure.instructions.get(address).opcode.encoding != 0x9FA0 // NOP stuffing
                && memory.loadInstruction16(address) != 0x0000 // 0x0000 stuffing
                ) {
                // Not a 0000 nor a NOP
                Function function = new Function(address, "", "", Function.Type.UNKNOWN);
                codeStructure.functions.put(address, function);
                try {
                    followFunction(function, address, false);
                }
                catch (DisassemblyException e) {
                    debugPrintWriter.println("SHOULD NOT HAPPEN. Please report this case on the forums ! : Error disassembling unknown function at 0x" + Format.asHex(address , 2) + ": " + e.getMessage());
                }
            }
            entry = codeStructure.instructions.higherEntry(address);
        }


        debugPrintWriter.println("Generating names for functions...");
        int functionNumber = 1;
        for (Integer address : codeStructure.functions.keySet()) {
            Function function = codeStructure.functions.get(address);
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
                    Function function = codeStructure.functions.get(address);
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
        // Temporary storage for names given to returns to make sure they are unique
        Set<String> usedReturnLabels = new HashSet<String>();

        // Give names to labels linked to function start/parts (if they were spotted as targets already)
        for (Integer address : codeStructure.functions.keySet()) {
            Function function = codeStructure.functions.get(address);
            if (codeStructure.labels.containsKey(address)) {
                codeStructure.labels.put(address, new Symbol(address, "start_of_" + codeStructure.getFunctionName(address), null));
            }
            for (int i = 0; i < function.getCodeSegments().size(); i++) {
                CodeSegment codeSegment = function.getCodeSegments().get(i);
                int startAddress = codeSegment.getStart();
                if (codeStructure.labels.containsKey(startAddress)) {
                    codeStructure.labels.put(startAddress, new Symbol(startAddress, "part_" + (i+1) + "_of_" + function.getName(), null));
                }
            }
        }

        for (Integer address : codeStructure.labels.keySet()) {
            if (codeStructure.labels.get(address).getName().length() == 0) {
                String label;
                if (codeStructure.isReturn(address)) {
                    Integer matchingFunctionStart = codeStructure.returns.get(address);
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
                    label = codeStructure.labels.get(address).getName();
                    if (StringUtils.isBlank(label)) {
                        label = "label_" + (outputOptions.contains(OutputOption.ORDINAL)?("" + labelNumber):Integer.toHexString(address)) + "_";
                    }
                }
                codeStructure.labels.put(address, new Symbol(address, label, null));
            }
            // increment even if unused, to make output stable no matter what future replacements will occur
            labelNumber++;
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
        Function function = codeStructure.functions.get(address);
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

    void followFunction(Function currentFunction, Integer address, boolean stopAtFirstProcessedInstruction) throws IOException, DisassemblyException {
        if (codeStructure.instructions.get(address) == null) {
            throw new DisassemblyException("No decoded instruction at 0x" + Format.asHex(address, 8) + " (not a CODE range)");
        }
        CodeSegment currentSegment = new CodeSegment();
        currentFunction.getCodeSegments().add(currentSegment);
        List<Jump> jumps = new ArrayList<Jump>();
        currentSegment.setStart(address);
        while(address != null) {
            if (stopAtFirstProcessedInstruction && processedInstructions.contains(address)) {
                Integer previousAddress = codeStructure.instructions.lowerKey(address);
                // Check we're not in delay slot. We shouldn't stop on delay slot
                if (previousAddress == null || !codeStructure.instructions.get(previousAddress).opcode.hasDelaySlot) {
                    break;
                }
            }
            DisassembledInstruction instruction = codeStructure.instructions.get(address);
            processedInstructions.add(address);
            currentSegment.setEnd(address);
            switch (instruction.opcode.type) {
                case RET:
                    codeStructure.returns.put(address, currentFunction.getAddress());
                    codeStructure.ends.put(address + (instruction.opcode.hasDelaySlot ? 2 : 0), currentFunction.getAddress());
                    break;
                case JMP:
                case BRA:
                    if (instruction.decodedX != 0) {
                        codeStructure.labels.put(instruction.decodedX, new Symbol(instruction.decodedX, "", ""));
                        Jump jump = new Jump(address, instruction.decodedX, instruction.opcode);
                        jumps.add(jump);
                        currentFunction.getJumps().add(jump);
                    }
                    else {
                        // target is dynamic. See if we have a hint
                        List<Integer> potentialTargets = jumpHints.get(address);
                        if (potentialTargets != null) {
                            int i = 0;
                            for (Integer potentialTarget : potentialTargets) {
                                Jump jump = new Jump(address, potentialTarget, instruction.opcode);
                                jumps.add(jump);
                                currentFunction.getJumps().add(jump);
                                codeStructure.labels.put(potentialTarget, new Symbol(potentialTarget, "jmp_target_" + Integer.toHexString(address) + "_" + i, null));
                                i++;
                            }
                        }
                        else {
                            // try to resolve this typical compiler construct :
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
                                            Jump jump = new Jump(address, potentialTarget, instruction.opcode);
                                            jumps.add(jump);
                                            currentFunction.getJumps().add(jump);
                                            codeStructure.labels.put(potentialTarget, new Symbol(potentialTarget, "jmp_target_" + Integer.toHexString(address) + "_" + i, null));
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
                                                Jump jump = new Jump(address, potentialTarget, instruction.opcode);
                                                jumps.add(jump);
                                                currentFunction.getJumps().add(jump);
                                                codeStructure.labels.put(potentialTarget, new Symbol(potentialTarget, "jmp_target_" + Integer.toHexString(address) + "_" + i, null));
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
                    break;
                case CALL:
                    if (instruction.opcode.hasDelaySlot) {
                        currentSegment.setEnd(address + 2);
                        processedInstructions.add(address + 2);
                    }
                    int targetAddress = instruction.decodedX;
                    if (targetAddress == 0) {
                        List<Integer> potentialTargets = jumpHints.get(address);
                        if (potentialTargets != null) {
                            int i = 0;
                            for (Integer potentialTarget : potentialTargets) {
                                addCall(currentFunction, instruction, address, potentialTarget, "call_target_" + Integer.toHexString(address) + "_" + i);
                                i++;
                            }
                        }
                        else {
                            Jump call = new Jump(address, targetAddress, instruction.opcode);
                            currentFunction.getCalls().add(call);
                            debugPrintWriter.println("WARNING : Cannot determine dynamic target of CALL. Add -j 0x" + Format.asHex(address, 8) + "=addr1[, addr2[, ...]] to specify targets");
                        }
                    }
                    else {
                        addCall(currentFunction, instruction, address, targetAddress, "");
                    }
                    break;
                case INT:
                case INTE:
                    Integer interruptAddress = interruptTable.get(instruction.decodedX);
                    if (instruction.decodedX == 0x40 && int40mapping != null) {
                        processInt40Call(currentFunction, address, instruction);
                    }
                    else {
                        Jump interruptCall = new Jump(address, interruptAddress, instruction.opcode);
                        currentFunction.getCalls().add(interruptCall);
                        Function interrupt = codeStructure.functions.get(interruptAddress);
                        if (interrupt != null) {
                            interrupt.getCalledBy().put(interruptCall, currentFunction);
                        }
                        else {
                            debugPrintWriter.println("Error : following INT at 0x" + Format.asHex(address, 8) + ": no code found at 0x" + Format.asHex(interruptAddress, 8));
                        }
                    }
                    break;
            }

            if (instruction.opcode.type == OpCode.Type.RET || instruction.opcode.type == OpCode.Type.JMP) {
                if (instruction.opcode.hasDelaySlot) {
                    currentSegment.setEnd(address + 2);
                    processedInstructions.add(address + 2);
                }
                // End of segment
                break;
            }
            address = codeStructure.instructions.higherKey(address);
        }

        // Process jumps
        currentFunction.getJumps().addAll(jumps);
        boolean inProcessedSegment;
        for (Jump jump : jumps) {
            inProcessedSegment = false;
            for (CodeSegment segment : currentFunction.getCodeSegments()) {
                if (jump.target >= segment.start && jump.target <= segment.end) {
                    // At first look, this part of code has already been processed.
                    // However, it happens (eg 001B77A4) that a jump ends on the delay slot of an unconditional JMP
                    // So we should consider we're really in a processed segment if
                    // - either it's a jump/call/return
                    DisassembledInstruction instruction = codeStructure.instructions.get(jump.target);
                    if (instruction != null && (instruction.opcode.type == OpCode.Type.CALL
                            || instruction.opcode.type == OpCode.Type.JMP
                            || instruction.opcode.type == OpCode.Type.BRA
                            || instruction.opcode.type == OpCode.Type.RET)) {
                        inProcessedSegment = true;
                        break;
                    }
                    // - or the next instruction is also in the range
                    Integer addressFollowingTarget = codeStructure.instructions.higherKey(jump.target);
                    if (addressFollowingTarget != null && addressFollowingTarget >= segment.start && addressFollowingTarget <= segment.end) {
                        inProcessedSegment = true;
                        break;
                    }
                    // Otherwise, it has to be followed...
                }
            }
            if (!inProcessedSegment) {
                try {
                    followFunction(currentFunction, jump.target, false);
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

    private void addCall(Function currentFunction, DisassembledInstruction instruction, Integer sourceAddress, int targetAddress, String defaultName) throws IOException {
        Jump call = new Jump(sourceAddress, targetAddress, instruction.opcode);
        currentFunction.getCalls().add(call);
        Function function = codeStructure.functions.get(targetAddress);
        if (function == null) {
            // new Function
            function = new Function(targetAddress, defaultName, "", Function.Type.STANDARD);
            codeStructure.functions.put(targetAddress, function);
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

    private void processInt40Call(Function currentFunction, Integer address, DisassembledInstruction instruction) throws IOException {
        // REALOS System calls
        // Determine R12 before the call by reading the instructions up to 200 bytes backwards (168 needed for call at 0x001824D0)
        // TODO : ideally, should follow program flow by climbing back function coderanges and not addresses in a straight line.
        // TODO : Here, we run the risk of not catching the good R12 value (not the case in practice)...
        Integer r12 = null;
        boolean r12SignExtend = false;
        for (int offset = 1; offset < 100; offset++) {
            DisassembledInstruction candidateInstruction = codeStructure.instructions.get(address - 2 * offset);
            if (candidateInstruction != null) {
                if (candidateInstruction.opcode.encoding == 0x9F80 && candidateInstruction.decodedI == 12) {
                    /* LDI:32 #i32, R12 */
                    r12 = candidateInstruction.decodedX;
                    break;
                }
                if (candidateInstruction.opcode.encoding == 0xC000 && candidateInstruction.decodedI == 12) {
                    /* LDI:8 #i8, R12 */
                    if (r12SignExtend) {
                        r12 = BinaryArithmetics.signExtend(8, candidateInstruction.decodedX);
                    }
                    else {
                        r12 = candidateInstruction.decodedX;
                    }
                    break;
                }
                if (candidateInstruction.opcode.encoding == 0x9780 && candidateInstruction.decodedI == 12) {
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
                Jump interrupt40Call = new Jump(address, int40targetAddress, instruction.opcode /* TODO should characterize that it is a INT40 call */);
                currentFunction.getCalls().add(interrupt40Call);
                Function target = codeStructure.functions.get(int40targetAddress);
                if (target == null) {
                    // new Function
                    target = new Function(int40targetAddress, "", "", Function.Type.STANDARD);
                    codeStructure.functions.put(int40targetAddress, target);
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
                if (StringUtils.isBlank(instruction.comment)) {
                    Symbol symbol = symbols.get(int40targetAddress);
                    if (symbol != null) {
                        instruction.comment = ""+int40targetAddress;
//                        instruction.comment = symbol.getName();
                    }
                }
            }
        }
    }


}
