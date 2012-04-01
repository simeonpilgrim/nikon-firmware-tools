package com.nikonhacker.dfr;

import com.nikonhacker.Format;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class CodeStructure {

    protected int entryPoint;

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

    protected boolean isFunction(Integer address) {
        return functions.containsKey(address);
    }

    protected String getFunctionName(Integer address) {
        Symbol symbol = functions.get(address);
        return symbol==null?null:symbol.getName();
    }


    public Map<Integer, Integer> getReturns() {
        return returns;
    }

    protected boolean isReturn(Integer address) {
        return returns.containsKey(address);
    }


    public Map<Integer, Integer> getEnds() {
        return ends;
    }

    private boolean isEnd(Integer address) {
        return ends.containsKey(address);
    }





    public void writeDisassembly(Writer writer, Range memRange, Range fileRange, Set<OutputOption> outputOptions) throws IOException {

        // Start output
        Integer address = memRange.getStart();
        DisassembledInstruction instruction = instructions.get(address);

        int memoryFileOffset = outputOptions.contains(OutputOption.OFFSET)?(fileRange.start - fileRange.fileOffset):0;

        while (instruction != null && address < memRange.getEnd()) {
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

        if (instruction.opcode.type == OpCode.Type.CALL
         || instruction.opcode.type == OpCode.Type.JMP
         || instruction.opcode.type == OpCode.Type.BRA) {
            if (instruction.comment.length() > 0) {
                // replace address by label in comment
                try {
                    int targetAddress = Format.parseUnsigned(instruction.comment);
                    Symbol symbol;
                    if (instruction.opcode.type == OpCode.Type.CALL) {
                        symbol = functions.get(targetAddress);
                    }
                    else {
                        symbol = labels.get(targetAddress);
                    }
                    if (symbol != null) {
                        instruction.comment = symbol.getName();
                    }
                    if (instruction.opcode.type == OpCode.Type.JMP || instruction.opcode.type == OpCode.Type.BRA) {
                        //if (areInSameRange(functions,  address, targetAddress))
                        instruction.comment += " " + skipOrLoop(address, targetAddress);
                    }
                } catch (ParsingException e) {
                    // noop
                }
            }
            else {
                try {
                    // add label to comment if operand
                    int targetAddress = Format.parseUnsigned(instruction.operands);
                    if (instruction.opcode.type == OpCode.Type.CALL) {
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
                    if (instruction.opcode.type == OpCode.Type.JMP || instruction.opcode.type == OpCode.Type.BRA) {
                        //TODO only if(areInSameRange(address, targetAddress))
                        instruction.comment = skipOrLoop(address, targetAddress);
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

    private String skipOrLoop(Integer address, int targetAddress) {
        long target = targetAddress & 0xFFFFFFFFL;
        long addr = address & 0xFFFFFFFFL;
        return target > addr ?"(skip)":"(loop)";
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