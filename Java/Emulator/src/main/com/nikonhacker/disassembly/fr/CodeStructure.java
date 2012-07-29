package com.nikonhacker.disassembly.fr;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class CodeStructure {

    protected int entryPoint;

    // TODO : Optimize by merging statements/labels/functions/returns/ends in a same table with various properties ?

    /** Map address -> Statement */
    TreeMap<Integer, FrStatement> statements = new TreeMap<Integer, FrStatement>();
    
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

    public TreeMap<Integer, FrStatement> getStatements() {
        return statements;
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
        FrStatement statement = statements.get(address);

        int memoryFileOffset = outputOptions.contains(OutputOption.OFFSET)?(fileRange.getStart() - fileRange.getFileOffset()):0;

        while (statement != null && address < memRange.getEnd()) {
            writeStatement(writer, address, statement, memoryFileOffset, outputOptions);

            address = statements.higherKey(address);
            statement = address==null?null: statements.get(address);
        }

    }

    public void writeStatement(Writer writer, Integer address, FrStatement statement, int memoryFileOffset, Set<OutputOption> outputOptions) throws IOException {
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

        if (EnumSet.of(FrInstruction.Type.JMP, FrInstruction.Type.BRA, FrInstruction.Type.CALL, FrInstruction.Type.INT).contains(statement.instruction.type)) {
            try {
                int targetAddress;
                // get address in comment (if any) or in operand
                if (statement.comment.length() > 0) {
                    targetAddress = Format.parseUnsigned(statement.comment);
                }
                else {
                    targetAddress = Format.parseUnsigned(statement.operands);
                }

                // fetch corresponding symbol
                Symbol symbol;
                if (EnumSet.of(FrInstruction.Type.JMP, FrInstruction.Type.BRA).contains(statement.instruction.type)) {
                    symbol = labels.get(targetAddress);
                }
                else { // CALLs
                    symbol = functions.get(targetAddress);
                }

                // If found, replace target address by label
                String text = "";
                if (symbol != null) {
                    text = symbol.getName();
                }

                if (EnumSet.of(FrInstruction.Type.JMP, FrInstruction.Type.BRA).contains(statement.instruction.type)) {
                    // Add (skip) or (loop) according to jump direction
                    //TODO only if(areInSameRange(address, targetAddress))
                    if (statement.comment.length() > 0) {
                        statement.comment = (text + " " + skipOrLoop(address, targetAddress)).trim();
                    }
                    else {
                        statement.operands = text;
                        statement.comment = skipOrLoop(address, targetAddress);
                    }
                }
                else { // CALL or INT
                    if (outputOptions.contains(OutputOption.PARAMETERS)) {
                        // Add function parameters
                        Function function = (Function)symbol;
                        if (function != null && function.getParameterList() != null) {
                            text +="(";
                            String prefix = "";
                            for (Symbol.Parameter parameter : function.getParameterList()) {
                                if (parameter.getInVariable() != null) {
                                    if(!text.endsWith("(")) {
                                        text+=", ";
                                    }
                                    text+=parameter.getInVariable() + "=";
                                    if (statement.cpuState.isRegisterDefined(parameter.getRegister())) {
                                        text+="0x" + Integer.toHexString(statement.cpuState.getReg(parameter.getRegister()));
                                    }
                                    else {
                                        text+= FrCPUState.REG_LABELS[parameter.getRegister()];
                                    }
                                }
                                else if (parameter.getOutVariable() != null) {
                                    if (prefix.length() > 0) {
                                        prefix += ",";
                                    }
                                    prefix+= FrCPUState.REG_LABELS[parameter.getRegister()];
                                }
                            }
                            text += ")";
                            if (prefix.length() > 0) {
                                text = prefix + "=" + text;
                            }
                        }
                    }

                    if (statement.comment.length() > 0) {
                        statement.comment = text;
                    }
                    else {
                        statement.operands = text;
                    }
                }
            } catch(ParsingException e){
                // noop
            }
        }

        // print statement
        Dfr.printDisassembly(writer, statement, address, memoryFileOffset, outputOptions);

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

    /**
     * Try to convert given text to address
     * @param text can be a (dfr.txt defined) function name, an address with or without 0x, of a fictious
     *             function name of the form xxx_address[_]
     * @return the converted address, or null if none matches
     */
    public Integer getAddressFromText(String text) {
        Integer address = null;
        if (StringUtils.isNotBlank(text)) {
            // Try to find by name
            for (Integer candidate : getFunctions().keySet()) {
                Function f = getFunctions().get(candidate);
                if (text.equalsIgnoreCase(f.getName())) {
                    return candidate;
                }
            }
            // No match by name
            // Try to interpret as address, adding 0x if omitted
            try {
                address = Format.parseUnsigned((text.startsWith("0x")?"":"0x") + text);
            } catch (ParsingException e) {
                // Not parseable as address
                // Try to interpret as name xxx_address[_]
                while(text.endsWith("_")) {
                    text = StringUtils.chop(text);
                }
                text = StringUtils.substringAfterLast(text, "_");
                try {
                    address = Format.parseUnsigned("0x" + text);
                } catch (ParsingException e1) {
                    // do nothing. address remains null
                }
            }
        }
        return address;
    }
}