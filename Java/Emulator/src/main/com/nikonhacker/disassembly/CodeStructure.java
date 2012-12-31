package com.nikonhacker.disassembly;

import com.nikonhacker.Format;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public abstract class CodeStructure {

    private int entryPoint;

    // TODO : Optimize by merging statements/labels/functions/returns/ends in a same table with various properties ?

    private TreeMap<Integer, Statement> statements = new TreeMap<Integer, Statement>();
    
    private Map<Integer, Symbol> labels = new TreeMap<Integer, Symbol>();
    
    private SortedMap<Integer, Function> functions = new TreeMap<Integer, Function>();

    private Map<Integer, Integer> returns = new TreeMap<Integer, Integer>();

    private Map<Integer, Integer> ends = new TreeMap<Integer, Integer>();

    public CodeStructure(int address) {
        this.entryPoint = address;
    }

    public int getEntryPoint() {
        return entryPoint;
    }

    /** Map address -> Statement */
    public TreeMap<Integer, Statement> getStatements() {
        return statements;
    }

    
    /** Map address -> Labels */
    public Map<Integer, Symbol> getLabels() {
        return labels;
    }

    private boolean isLabel(Integer address) {
        return labels.containsKey(address);
    }

    
    /** Map address -> Functions */
    public SortedMap<Integer, Function> getFunctions() {
        return functions;
    }

    public boolean isFunction(Integer address) {
        return functions.containsKey(address);
    }

    public String getFunctionName(Integer address) {
        Symbol symbol = functions.get(address);
        return symbol==null?null:symbol.getName();
    }

    public String getLabelName(Integer address) {
        Symbol symbol = labels.get(address);
        return symbol==null?null:symbol.getName();
    }


    /** Map address -> Start of corresponding function */
    public Map<Integer, Integer> getReturns() {
        return returns;
    }

    public boolean isReturn(Integer address) {
        return returns.containsKey(address);
    }


    /** Map address -> Start of corresponding function
     *  (This Map may differ from returns due to delay slots)
     */
    public Map<Integer, Integer> getEnds() {
        return ends;
    }

    private boolean isEnd(Integer address) {
        return ends.containsKey(address);
    }





    public void writeDisassembly(Writer writer, Range memRange, Range fileRange, Set<OutputOption> outputOptions) throws IOException {

        // Start output
        Integer address = memRange.getStart();
        Statement statement = statements.get(address);

        int memoryFileOffset = outputOptions.contains(OutputOption.OFFSET)?(fileRange.getStart() - fileRange.getFileOffset()):0;

        while (statement != null && address < memRange.getEnd()) {
            writeStatement(writer, address, statement, memoryFileOffset, outputOptions);

            address = statements.higherKey(address);
            statement = address==null?null: statements.get(address);
        }

    }

    public void writeStatement(Writer writer, Integer address, Statement statement, int memoryFileOffset, Set<OutputOption> outputOptions) throws IOException {
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

        // Replace target addresses and comments by symbol names, etc.
        improveOperandAndComment(address, statement, outputOptions);

        // print statement
        Disassembler.printDisassembly(writer, statement, address, memoryFileOffset, outputOptions);

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

    /**
     * This method replaces addresses in operands and comments by corresponding symbol names (if any),
     * indicates if branches go forward (skip) or backwards (loop) and decodes call parameters
     * @param address
     * @param statement
     * @param outputOptions
     */
    private void improveOperandAndComment(Integer address, Statement statement, Set<OutputOption> outputOptions) {
        if (EnumSet.of(Instruction.FlowType.JMP, Instruction.FlowType.BRA, Instruction.FlowType.CALL, Instruction.FlowType.INT).contains(statement.getInstruction().getFlowType())) {
            try {
                int targetAddress;
                // get address in comment (if any) or in operand
                if (statement.getCommentString().length() > 0) {
                    targetAddress = Format.parseUnsigned(statement.getCommentString());
                }
                else {
                    targetAddress = Format.parseUnsigned(statement.getOperandString());
                }

                // fetch corresponding symbol
                Symbol symbol;
                if (EnumSet.of(Instruction.FlowType.JMP, Instruction.FlowType.BRA).contains(statement.getInstruction().getFlowType())) {
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

                if (EnumSet.of(Instruction.FlowType.JMP, Instruction.FlowType.BRA).contains(statement.getInstruction().getFlowType())) {
                    // Add (skip) or (loop) according to jump direction
                    //TODO only if(areInSameRange(address, targetAddress))
                    if (statement.getCommentString().length() > 0) {
                        statement.setCommentString((text + " " + skipOrLoop(address, targetAddress)).trim());
                    }
                    else {
                        statement.setOperandString(text);
                        statement.setCommentString(skipOrLoop(address, targetAddress));
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
                                if (parameter.getInVariableName() != null) {
                                    if(!text.endsWith("(")) {
                                        text+=", ";
                                    }
                                    text+=parameter.getInVariableName() + "=";
                                    if (statement.getContext().cpuState.isRegisterDefined(parameter.getRegister())) {
                                        text+="0x" + Integer.toHexString(statement.getContext().cpuState.getReg(parameter.getRegister()));
                                    }
                                    else {
                                        text+= getRegisterLabels()[parameter.getRegister()];
                                    }
                                }
                                else if (parameter.getOutVariableName() != null) {
                                    if (prefix.length() > 0) {
                                        prefix += ",";
                                    }
                                    prefix+= getRegisterLabels()[parameter.getRegister()];
                                }
                            }
                            text += ")";
                            if (prefix.length() > 0) {
                                text = prefix + "=" + text;
                            }
                        }
                    }

                    if (statement.getCommentString().length() > 0) {
                        statement.setCommentString(text);
                    }
                    else {
                        statement.setOperandString(text);
                    }
                }
            } catch(ParsingException e){
                // noop
            }
        }
    }

    public abstract String[] getRegisterLabels();

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
            text = text.trim();
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

    public void setEntryPoint(int entryPoint) {
        this.entryPoint = entryPoint;
    }

    public void setStatements(TreeMap<Integer, Statement> statements) {
        this.statements = statements;
    }

    public void setLabels(Map<Integer, Symbol> labels) {
        this.labels = labels;
    }

    public void setFunctions(SortedMap<Integer, Function> functions) {
        this.functions = functions;
    }

    public void setReturns(Map<Integer, Integer> returns) {
        this.returns = returns;
    }

    public void setEnds(Map<Integer, Integer> ends) {
        this.ends = ends;
    }
}