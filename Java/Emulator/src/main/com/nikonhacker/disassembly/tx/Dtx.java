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

        if (isExtend(binaryStatement)) {
            // This is the EXTEND prefix. Get real instruction
            int realBinaryStatement = memory.loadInstruction16(cpuState.pc + 2);
            try {
                statement.setInstruction(TxInstructionSet.getExtendedInstructionFor16BitStatement(realBinaryStatement));
            } catch (DisassemblyException e) {
                System.err.println("Could not decode statement 0x" + Format.asHex(statement.getBinaryStatement(), 4) + " at 0x" + Format.asHex(cpuState.pc, 8) + ": " + e.getClass().getName());
            }
            statement.setBinaryStatement((binaryStatement << 16) | realBinaryStatement);
            bytesInInstruction = 4;
        }
        else {
            try {
                statement.setInstruction(TxInstructionSet.getInstructionFor16BitStatement(binaryStatement));
            } catch (DisassemblyException e) {
                System.err.println("Could not decode statement 0x" + Format.asHex(statement.getBinaryStatement(), 4) + " at 0x" + Format.asHex(cpuState.pc, 8) + ": " + e.getClass().getName());
            }
            statement.setBinaryStatement(binaryStatement);
            bytesInInstruction = 2;
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

    /**
     * Determines if this statement is the "EXTEND" prefix
     * @param binaryStatement
     * @return
     */
    private static boolean isExtend(int binaryStatement) {
        return (binaryStatement & 0b1111100000000000) == 0b1111000000000000;
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