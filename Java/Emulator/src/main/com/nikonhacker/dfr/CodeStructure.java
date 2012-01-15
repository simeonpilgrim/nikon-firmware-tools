package com.nikonhacker.dfr;

import com.nikonhacker.Format;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

public class CodeStructure {
    /** Map address -> Instruction */
    Map<Integer, DisassembledInstruction> instructions = new TreeMap<Integer, DisassembledInstruction>();
    
    /** Map address -> Labels */
    Map<Integer, Symbol> labels = new TreeMap<Integer, Symbol>();
    
    /** Map address -> Functions */
    Map<Integer, Function> functions = new TreeMap<Integer, Function>();

    /** Map address -> Start of corresponding function */
    Map<Integer, Integer> returns = new TreeMap<Integer, Integer>();

    /** Map address -> Start of corresponding function 
     *  (This Map may differ from returns due to delay slots)
     */
    Map<Integer, Integer> ends = new TreeMap<Integer, Integer>();

    
    public Map<Integer, DisassembledInstruction> getInstructions() {
        return instructions;
    }

    
    public Map<Integer, Symbol> getLabels() {
        return labels;
    }

    private boolean isLabel(Integer address) {
        return labels.containsKey(address);
    }

    
    public Map<Integer, Function> getFunctions() {
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


    // Post-process jump addresses
    public void postProcess(Map<Integer, Symbol> symbols) {
        for (Integer address : getInstructions().keySet()) {
            DisassembledInstruction disassembledInstruction = getInstructions().get(address);
            if (disassembledInstruction.opcode.isJumpOrBranch) getLabels().put(disassembledInstruction.decodedX, null);
            if (disassembledInstruction.opcode.isCall) getFunctions().put(disassembledInstruction.decodedX, null);
            if (disassembledInstruction.opcode.isReturn) {
                getReturns().put(address, null);
                getEnds().put(address + (disassembledInstruction.opcode.hasDelaySlot ? 2 : 0), null);
            }
        }


        // TODO : parse interrupt table and add interrupts to function table so that function table is complete

        // Try to match returns with starts
        Integer lastFunctionStartAddress = null;
        for (Integer address : instructions.keySet()) {
            if (isFunction(address)) {
                lastFunctionStartAddress = address;
            }
            if (isReturn(address)) {
                returns.put(address, lastFunctionStartAddress);
            }
            if (isEnd(address)) {
                ends.put(address, lastFunctionStartAddress);
                lastFunctionStartAddress = null;
            }
        }

        // Give default names to functions
        int functionNumber = 1;
        for (Integer address : functions.keySet()) {
            if (StringUtils.isBlank(getFunctionName(address))) {
                functions.put(address, new Function(address, "function_" + functionNumber + "_", null));
            }
            // increment even if unused, to make output stable no matter what future replacements will occur
            functionNumber++;
        }

        // Override names using symbols table (if defined)
        if (symbols != null) {
            for (Integer address : symbols.keySet()) {
                if (isFunction(address)) {
                    functions.put(address, new Function(symbols.get(address)));
                }
            }
        }

        // Give default names to labels
        int labelNumber = 1;
        for (Integer address : labels.keySet()) {
            if (isFunction(address)) {
                labels.put(address, new Symbol(address, "start_of_" + getFunctionName(address), null));
            }
            else if (isReturn(address)) {
                Integer matchingStart = returns.get(address);
                if (matchingStart != null) {
                    labels.put(address, new Symbol(address, "end_" + getFunctionName(matchingStart), null));
                }
                else {
                    labels.put(address, new Symbol(address, "end_unidentified_fn_l_" + labelNumber + "_", null));
                }
            }
            else {
                labels.put(address, new Symbol(address, "label_" + labelNumber + "_", null));
            }

            // increment even if unused, to make output stable no matter what future replacements will occur
            labelNumber++;
        }

        // TODO : trace a graph of calls

    }

    public void writeDisassembly(Writer writer) throws IOException {

        // Start output
        for (Integer address : instructions.keySet()) {
            DisassembledInstruction instruction = instructions.get(address);

            // function
            if (isFunction(address)) {
                writer.write("\n; ************************************\n");
                writer.write("; " + functions.get(address).toString() + "\n");
                writer.write("; ************************************\n");
            }

            // label
            if (isLabel(address)) {
                writer.write("; " + labels.get(address).getName() + " :\n");
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

                        instruction.comment = symbol.toString();
                        if (instruction.opcode.isJumpOrBranch) {
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
                            instruction.operands = functions.get(targetAddress).getName();
                        }
                        else {
                            instruction.operands = labels.get(targetAddress).getName();
                        }
                        if (instruction.opcode.isJumpOrBranch) {
                            instruction.comment =  targetAddress>address?"(skip)":"(loop)";
                        }
                    } catch (ParsingException e) {
                        // noop
                    }
                }
            }

            // print instruction
            writer.write(Format.asHex(address, 8) + " " + instruction.toString());

            // return from function
            if (isEnd(address)) {
                Integer matchingStart = ends.get(address);
                if (matchingStart == null) {
                    writer.write("; end of an unidentified function (never called)\n");
                }
                else {
                    writer.write("; end of " + getFunctionName(matchingStart) + "\n");
                }
                writer.write("; ------------------------------------\n\n");
            }
        }

    }

}
