package com.nikonhacker.disassembly.tx;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.*;

import java.io.IOException;
import java.util.Set;

public class Dtx extends Disassembler
{
    private static final String DEFAULT_OPTIONS_FILENAME = "dtx.txt";

    public static void main(String[] args) throws IOException, DisassemblyException, ParsingException {
        new Dtx().execute(args);
    }


    ///* output */


    protected int disassembleOne16BitStatement(CPUState cpuState, Range memRange, int memoryFileOffset, CodeStructure codeStructure, Set<OutputOption> outputOptions) throws IOException, DisassemblyException {
        TxStatement statement = new TxStatement(memRange.getStart());
        statement.getNextStatement(memory, cpuState.pc);

//        try {
//            statement.setInstruction(TxInstructionSet.getInstructionForStatement(statement.getBinaryStatement()));
//
//            statement.decodeOperands(cpuState.pc, memory);
//
//            statement.formatOperandsAndComment((TxCPUState) cpuState, true, this.outputOptions);
//        } catch (DisassemblyException e) {
//            System.err.println("Could not decode statement 0x" + Format.asHex(statement.getBinaryStatement(), 8) + " at 0x" + Format.asHex(cpuState.pc, 8) + " : " + e.getClass().getName());
//        }

        if (codeStructure != null) {
            if ((statement.getInstruction().flowType == Instruction.FlowType.CALL || statement.getInstruction().flowType == Instruction.FlowType.INT) && outputOptions.contains(OutputOption.PARAMETERS)) {
                statement.cpuState = ((TxCPUState)cpuState).clone();
            }

            codeStructure.getStatements().put(cpuState.pc, statement);
        }
        else {
            // No structure analysis, output right now
            if (outWriter != null) {
                Disassembler.printDisassembly(outWriter, statement, cpuState.pc, memoryFileOffset, outputOptions);
            }
        }
        return 2;
    }

    @Override
    protected int disassembleOne32BitStatement(CPUState cpuState, Range memRange, int memoryFileOffset, CodeStructure codeStructure, Set<OutputOption> outputOptions) throws IOException, DisassemblyException {
        TxStatement statement = new TxStatement(memRange.getStart());
        statement.getNextStatement(memory, cpuState.pc);

        try {
            statement.setInstruction(TxInstructionSet.getInstructionForStatement(statement.getBinaryStatement()));

            statement.decodeOperands(cpuState.pc, memory);

            statement.formatOperandsAndComment((TxCPUState) cpuState, true, this.outputOptions);
        } catch (DisassemblyException e) {
            System.err.println("Could not decode statement 0x" + Format.asHex(statement.getBinaryStatement(), 8) + " at 0x" + Format.asHex(cpuState.pc, 8) + " : " + e.getClass().getName());
        }

        if (codeStructure != null) {
            if ((statement.getInstruction().flowType == Instruction.FlowType.CALL || statement.getInstruction().flowType == Instruction.FlowType.INT) && outputOptions.contains(OutputOption.PARAMETERS)) {
                statement.cpuState = ((TxCPUState)cpuState).clone();
            }

            codeStructure.getStatements().put(cpuState.pc, statement);
        }
        else {
            // No structure analysis, output right now
            if (outWriter != null) {
                Disassembler.printDisassembly(outWriter, statement, cpuState.pc, memoryFileOffset, outputOptions);
            }
        }
        return 4;
    }


    protected int disassembleOneDataRecord(CPUState dummyCpuState, Range memRange, int memoryFileOffset, Set<OutputOption> outputOptions) throws IOException {

        int sizeInBytes = 0;

//        for (int spec : memRange.getDataType().spec)
//        {
//            FrStatement statement = new FrStatement(memRange.getStart());
//            statement.getNextData(memory, dummyCpuState.pc);
//            statement.x = statement.data[0];
//            statement.xBitWidth = 16;
//            statement.setInstruction(FrInstruction.opData[spec]);
//
//            statement.decodeOperands(dummyCpuState.pc, memory);
//
//            statement.formatOperandsAndComment(dummyCpuState, true, this.outputOptions);
//
//            sizeInBytes += statement.n << 1;
//
//            if (outWriter != null) {
//                Disassembler.printDisassembly(outWriter, statement, dummyCpuState.pc, memoryFileOffset, outputOptions);
//            }
//        }

        return sizeInBytes;
    }


    protected CPUState getCPUState(Range memRange) {
        return new TxCPUState(memRange.getStart());
    }

    @Override
    protected String getDefaultOptionsFilename() {
        return DEFAULT_OPTIONS_FILENAME;
    }


    ///* initialization */

    public void initialize() throws IOException {
        super.initialize();
        // TxInstruction.initOpcodeMap(outputOptions);

        TxStatement.initFormatChars(outputOptions);

        TxCPUState.initRegisterLabels(outputOptions);
    }


    ///* options */


    protected String[] getRegisterLabels() {
        return TxCPUState.REG_LABEL;
    }

    protected CodeStructure getCodeStructure(int start) {
        return new TxCodeStructure(start);
    }
}