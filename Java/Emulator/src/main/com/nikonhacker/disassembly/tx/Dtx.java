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

        int binaryStatement = memory.loadInstruction16(cpuState.pc);

        // In 16-bit ISA, all instructions are on 16-bits, except EXTENDed instructions and JAL/JALX.
        // Handle these 3 cases, based on the 5 MSBs of the 16 bits read:
        switch (binaryStatement & 0b1111100000000000) {
            case 0b1111000000000000:
                // This is the EXTEND prefix. Get real instruction
                int realBinaryStatement = memory.loadInstruction16(cpuState.pc + 2);
                statement.setBinaryStatement(4, (binaryStatement << 16) | realBinaryStatement);

                // Now most of the instructions can be determined based only on the lower 16bits, except two cases: Min/Max and Bs1f/Bfins:
                switch (realBinaryStatement & 0b1111100000011111) {
                    case 0b1110100000000101:
                        // Weird min/max encoding : they are both extended instructions with the same lower 16b pattern
                        statement.setInstruction(TxInstructionSet.getMinMaxInstructionForStatement(statement.getBinaryStatement()));
                        break;
                    case 0b1110100000000111:
                        // Weird Bs1f/Bfins encoding : they are both extended instructions with the same lower 16b pattern
                        statement.setInstruction(TxInstructionSet.getBs1fBfinsInstructionForStatement(statement.getBinaryStatement()));
                        break;
                    default:
                        // Normal case for EXTENDed instructions. Decode based on lower 16 bits
                        statement.setInstruction(TxInstructionSet.getExtendedInstructionFor16BitStatement(realBinaryStatement));
                }
                break;
            case 0b0001100000000000:
                // This is the JAL/JALX prefix.
                int fullStatement = (binaryStatement << 16) | memory.loadInstruction16(cpuState.pc + 2);
                statement.setBinaryStatement(4, fullStatement);

                try {
                    statement.setInstruction(TxInstructionSet.getJalInstructionForStatement(fullStatement));
                } catch (DisassemblyException e) {
                    System.err.println("Could not decode statement 0x" + Format.asHex(statement.getBinaryStatement(), 4) + " at 0x" + Format.asHex(cpuState.pc, 8) + ": " + e.getClass().getName());
                }
                break;
            default:
                // Normal non-EXTENDed 16-bit instructions
                statement.setBinaryStatement(2, binaryStatement);
                statement.setInstruction(TxInstructionSet.getInstructionFor16BitStatement(binaryStatement));
                break;
        }

        statement.decode16BitOperands(cpuState.pc, memory);

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
        statement.setBinaryStatement(4, memory.loadInstruction32(cpuState.pc));

        try {
            statement.setInstruction(TxInstructionSet.getInstructionFor32BitStatement(statement.getBinaryStatement()));

            statement.decode32BitOperands(cpuState.pc, memory);

            statement.formatOperandsAndComment((TxCPUState) cpuState, true, this.outputOptions);
        } catch (DisassemblyException e) {
            System.err.println("Could not decode statement 0x" + Format.asHex(statement.getBinaryStatement(), 8) + " at 0x" + Format.asHex(cpuState.pc, 8) + ": " + e.getClass().getName());
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