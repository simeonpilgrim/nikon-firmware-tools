package com.nikonhacker.disassembly;

import com.nikonhacker.Format;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public abstract class CodeStructure {

    public static final int IGNORE_ISA_BIT = 0xFFFFFFFE;

    private int entryPoint;


    /** Map address -> Statement */
    private TreeMap<Integer, Statement> statements = new TreeMap<Integer, Statement>();

    // TODO:
    // Should each statement include one "label", one "function", one "returnOf" and one "endOf" field ?
    // Should we group them in one "statementMetadata" class and use a single map address -> StatementMetadata ?
    // Probably NOT: This would be more space efficient, but the cost of getAddressOfStatementBefore() or
    // getAllLabelAddresses() would increase dramatically

    /** Map address -> Labels */
    private Map<Integer, Symbol> labels = new TreeMap<Integer, Symbol>();

    /** Map address -> Functions */
    private SortedMap<Integer, Function> functions = new TreeMap<Integer, Function>();

    /** Map address of return -> Start of corresponding function */
    private Map<Integer, Integer> returns = new TreeMap<Integer, Integer>();

    /** Map address of end -> Start of corresponding function
     *  (This Map may differ from returns due to delay slots)
     */
    private Map<Integer, Integer> ends = new TreeMap<Integer, Integer>();

    // Cache for task-related addresses
    public Integer tblTaskData;
    public Integer pCurrentTCB;
    public Integer tblTCB;


    public CodeStructure(int address) {
        this.entryPoint = address;
    }

    public int getEntryPoint() {
        return entryPoint;
    }


    // STATEMENTS

    public boolean isStatement(Integer address) {
        return statements.containsKey(address & IGNORE_ISA_BIT);
    }

    public Statement getStatement(Integer address) {
        return statements.get(address & IGNORE_ISA_BIT);
    }

    public void putStatement(int address, Statement statement) {
        statements.put(address & IGNORE_ISA_BIT, statement);
    }

    public int getNumStatements() {
        return statements.size();
    }

    public Integer getAddressOfStatementBefore(Integer address) {
        return statements.lowerKey(address & IGNORE_ISA_BIT);
    }

    public Integer getAddressOfStatementAfter(Integer address) {
        return statements.higherKey(address & IGNORE_ISA_BIT);
    }

    public Map.Entry<Integer, Statement> getFirstStatementEntry() {
        return statements.firstEntry();
    }

    public Map.Entry<Integer, Statement> getStatementEntryAfter(Integer address) {
        return statements.higherEntry(address & IGNORE_ISA_BIT);
    }


    // LABELS

    public boolean isLabel(Integer address) {
        return labels.containsKey(address & IGNORE_ISA_BIT);
    }

    public Symbol getLabel(int address) {
        return labels.get(address & IGNORE_ISA_BIT);
    }

    public void putLabel(int address, Symbol symbol) {
        labels.put(address & IGNORE_ISA_BIT, symbol);
    }

    public int getNumLabels() {
        return labels.size();
    }

    /** TX Note : returned addresses never contain ISA-bit. In other words, all addresses are even */
    public Set<Integer> getAllLabelAddresses() {
        return labels.keySet();
    }

    public String getLabelName(Integer address) {
        Symbol symbol = labels.get(address);
        return symbol==null?null:symbol.getName();
    }


    // FUNCTIONS

    public boolean isFunction(int address) {
        return functions.containsKey(address & IGNORE_ISA_BIT);
    }

    public Function getFunction(int address) {
        return functions.get(address & IGNORE_ISA_BIT);
    }

    public void putFunction(int address, Function function) {
        functions.put(address & IGNORE_ISA_BIT, function);
    }

    public int getNumFunctions() {
        return functions.size();
    }

    /** TX Note : returned addresses never contain ISA-bit. In other words, all addresses are even */
    public Set<Integer> getAllFunctionAddresses() {
        return functions.keySet();
    }

    public String getFunctionName(int address) {
        Symbol symbol = getFunction(address);
        return symbol==null?null:symbol.getName();
    }

    public Function findFunctionIncluding(int address) {
        address = address & IGNORE_ISA_BIT;
        for (Function function : functions.values()) {
            for (CodeSegment codeSegment : function.getCodeSegments()) {
                if (address >= codeSegment.getStart() && address <= codeSegment.getEnd()) {
                    return function;
                }
            }
        }
        return null;
    }


    // RETURNS

    public boolean isReturn(Integer address) {
        return returns.containsKey(address & IGNORE_ISA_BIT);
    }

    public int getReturn(int addressOfReturn) {
        return returns.get(addressOfReturn & IGNORE_ISA_BIT);
    }

    public void putReturn(int addressOfReturn, int startAddressOfCorrespondingFunction) {
        returns.put(addressOfReturn & IGNORE_ISA_BIT, startAddressOfCorrespondingFunction);
    }

    public int getNumReturns() {
        return returns.size();
    }


    // ENDS

    private boolean isEnd(Integer address) {
        return ends.containsKey(address & IGNORE_ISA_BIT);
    }

    public void putEnd(int addressOfEnd, int startAddressOfCorrespondingFunction) {
        ends.put(addressOfEnd & IGNORE_ISA_BIT, startAddressOfCorrespondingFunction);
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
            Function function = getFunction(address);
            writer.write("\n; ************************************************************************\n");
            writer.write("; " + function.getTitleLine() + "\n");
            writer.write("; ************************************************************************\n");
            writer.write(function.getName() + ":\n");
        }

        // label
        if (isLabel(address)) {
            writer.write(getLabelName(address) + ":\n");
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
                String firstOperands = "";
                if (statement.getCommentString().length() > 0) {
                    targetAddress = Format.parseUnsigned(statement.getCommentString());
                }
                else {
                    firstOperands = statement.getOperandString().substring(0, statement.getOperandString().lastIndexOf(' ') + 1);
                    targetAddress = Format.parseUnsigned(statement.getOperandString().substring(statement.getOperandString().lastIndexOf(' ') + 1)); // also works if no blanks in String
                }

                // fetch corresponding symbol
                Symbol symbol;
                if (EnumSet.of(Instruction.FlowType.JMP, Instruction.FlowType.BRA).contains(statement.getInstruction().getFlowType())) {
                    symbol = getLabel(targetAddress);
                }
                else { // CALLs
                    symbol = getFunction(targetAddress);
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
                        statement.setOperandString(firstOperands + text);
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
                                // register could be both: input AND output
                                if (parameter.getOutVariableName() != null) {
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

    /**
     * Try to convert given text to address
     * @param text can be a (dfr.txt defined) function name, an address with or without 0x, of a fictious
     *             function name of the form xxx_address[_]
     * @return the converted address, or null if none matches
     */
    public Integer getAddressFromString(String text) {
        Integer address = null;
        if (StringUtils.isNotBlank(text)) {
            text = text.trim();
            // Try to find by name
            for (Integer candidateAddress : getAllFunctionAddresses()) {
                Function f = getFunction(candidateAddress);
                if (text.equalsIgnoreCase(f.getName())) {
                    return candidateAddress;
                }
            }
            // No match by name
            // Try to interpret as address, adding 0x if omitted
            try {
                address = Format.parseUnsigned((text.startsWith("0x")?"":"0x") + text) & IGNORE_ISA_BIT;
            } catch (ParsingException e) {
                // Not parseable as address
                // Try to interpret as name xxx_address[_]
                while(text.endsWith("_")) {
                    text = StringUtils.chop(text);
                }
                text = StringUtils.substringAfterLast(text, "_");
                try {
                    address = Format.parseUnsigned("0x" + text) & IGNORE_ISA_BIT;
                } catch (ParsingException e1) {
                    // do nothing. address remains null
                }
            }
        }
        return address;
    }
}