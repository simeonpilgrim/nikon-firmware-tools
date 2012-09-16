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
        int bytesInInstruction;
        TxStatement statement = new TxStatement(memRange.getStart());

        int binaryStatement = memory.loadInstruction16(cpuState.pc);

        switch (binaryStatement & 0b1111100000000000) {
            case 0b1111000000000000:
                // This is the EXTEND prefix. Get real instruction
                int realBinaryStatement = memory.loadInstruction16(cpuState.pc + 2);
                statement.setBinaryStatement((binaryStatement << 16) | realBinaryStatement);

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
                        try {
                            statement.setInstruction(TxInstructionSet.getExtendedInstructionFor16BitStatement(realBinaryStatement));
                        } catch (DisassemblyException e) {
                            System.err.println("Could not decode statement 0x" + Format.asHex(statement.getBinaryStatement(), 4) + " at 0x" + Format.asHex(cpuState.pc, 8) + ": " + e.getClass().getName());
                        }
                }
                bytesInInstruction = 4;
                break;
            case 0b0001100000000000:
                // This is the JAL/JALX prefix.
                int fullStatement = (binaryStatement << 16) | memory.loadInstruction16(cpuState.pc + 2);
                statement.setBinaryStatement(fullStatement);
                try {
                    statement.setInstruction(TxInstructionSet.getJalInstructionForStatement(fullStatement));
                } catch (DisassemblyException e) {
                    System.err.println("Could not decode statement 0x" + Format.asHex(statement.getBinaryStatement(), 4) + " at 0x" + Format.asHex(cpuState.pc, 8) + ": " + e.getClass().getName());
                }
                bytesInInstruction = 4;
                break;
            default:
                try {
                    statement.setInstruction(TxInstructionSet.getInstructionFor16BitStatement(binaryStatement));
                } catch (DisassemblyException e) {
                    System.err.println("Could not decode statement 0x" + Format.asHex(statement.getBinaryStatement(), 4) + " at 0x" + Format.asHex(cpuState.pc, 8) + ": " + e.getClass().getName());
                }
                statement.setBinaryStatement(binaryStatement);
                bytesInInstruction = 2;
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
        return bytesInInstruction;
    }

    @Override
    protected int disassembleOne32BitStatement(CPUState cpuState, Range memRange, int memoryFileOffset, CodeStructure codeStructure, Set<OutputOption> outputOptions) throws IOException, DisassemblyException {
        TxStatement statement = new TxStatement(memRange.getStart());
        statement.setBinaryStatement(memory.loadInstruction32(cpuState.pc));

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

        // TODO

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