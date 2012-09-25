package com.nikonhacker.disassembly.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.disassembly.*;

import java.io.IOException;
import java.util.Set;

public class Dtx extends Disassembler
{
    private static final String DEFAULT_OPTIONS_FILENAME = "dtx.txt";

    public static void main(String[] args) throws IOException, DisassemblyException, ParsingException {
        new Dtx().execute(Constants.CHIP_FR, args);
    }


    /* output */
    protected int disassembleOne16BitStatement(CPUState cpuState, Range memRange, int memoryFileOffset, CodeStructure codeStructure, Set<OutputOption> outputOptions) throws IOException, DisassemblyException {
        TxStatement statement = new TxStatement(memRange.getStart());

        int binaryStatement16 = memory.loadInstruction16(cpuState.pc);

        statement.fill16bInstruction(binaryStatement16, cpuState.pc, memory);

        statement.decode16BitOperands(cpuState.pc);

        statement.formatOperandsAndComment((TxCPUState) cpuState, true, this.outputOptions);

        if (codeStructure != null) {
            if ((statement.getInstruction().flowType == Instruction.FlowType.CALL || statement.getInstruction().flowType == Instruction.FlowType.INT) && outputOptions.contains(OutputOption.PARAMETERS)) {
                statement.cpuState = ((TxCPUState) cpuState).clone();
            }

            codeStructure.getStatements().put(cpuState.pc, statement);
        }
        else {
            // No structure analysis, output right now
            if (outWriter != null) {
                Disassembler.printDisassembly(outWriter, statement, cpuState.pc, memoryFileOffset, outputOptions);
            }
        }
        return statement.getNumBytes();
    }

    @Override
    protected int disassembleOne32BitStatement(CPUState cpuState, Range memRange, int memoryFileOffset, CodeStructure codeStructure, Set<OutputOption> outputOptions) throws IOException, DisassemblyException {
        TxStatement statement = new TxStatement(memRange.getStart());

        int binaryStatement32 = memory.loadInstruction32(cpuState.pc);

        statement.fill32bInstruction(binaryStatement32);

        statement.decode32BitOperands();

        statement.formatOperandsAndComment((TxCPUState) cpuState, true, this.outputOptions);

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

        for (RangeType.Width spec : memRange.getRangeType().widths)
        {
            TxStatement statement = new TxStatement(memRange.getStart());
            switch (spec.getWidth()) {
                case 1:
                    statement.imm = memory.loadUnsigned8(dummyCpuState.pc);
                    statement.immBitWidth = 8;
                    break;
                case 2:
                    statement.imm = memory.loadUnsigned16(dummyCpuState.pc);
                    statement.immBitWidth = 16;
                    break;
                default:
                    statement.imm = memory.load32(dummyCpuState.pc);
                    statement.immBitWidth = 32;
                    break;
            }
            statement.setBinaryStatement(statement.immBitWidth / 4, statement.imm);

            statement.setInstruction(TxInstructionSet.opData[spec.getIndex()]);

            statement.formatOperandsAndComment((TxCPUState) dummyCpuState, true, this.outputOptions);

            if (outWriter != null) {
                Disassembler.printDisassembly(outWriter, statement, dummyCpuState.pc, memoryFileOffset, outputOptions);
            }

            sizeInBytes += statement.getNumBytes();
        }

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
        TxInstructionSet.init(outputOptions);

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