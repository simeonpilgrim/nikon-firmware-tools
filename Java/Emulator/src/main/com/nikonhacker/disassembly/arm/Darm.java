package com.nikonhacker.disassembly.arm;

import com.nikonhacker.Constants;
import com.nikonhacker.disassembly.Disassembler;
import com.nikonhacker.disassembly.StatementContext;
import com.nikonhacker.disassembly.Range;
import com.nikonhacker.disassembly.CodeStructure;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.Instruction;
import com.nikonhacker.disassembly.DisassemblyException;
import com.nikonhacker.disassembly.RangeType;
import com.nikonhacker.disassembly.CPUState;

import java.io.IOException;
import java.util.Set;

public class Darm extends Disassembler
{
    public Darm() {
        super(Constants.CHIP_ARM);
    }

    public static void main(String[] args) {
        Darm darm = new Darm();
        try {
            darm.execute(args);
        } catch (Exception | Error e) {
            e.printStackTrace();
            darm.log("ERROR : " + e.getClass().getName() + ": " + e.getMessage()+"\n");
        } finally {
            darm.closeDebugPrintWriter();
        }
    }


    /* output */
    protected int disassembleOne16BitStatement(StatementContext context, Range memRange, int memoryFileOffset, CodeStructure codeStructure, Set<OutputOption> outputOptions) throws IOException {
        ArmStatement statement = new ArmStatement(memRange.getStart());

        statement.getNextStatement(memory, context.cpuState.pc);

        statement.fillInstruction();

        statement.decodeOperands(context.cpuState.pc, memory);

        statement.formatOperandsAndComment(context, true, this.outputOptions);

        if (codeStructure != null) {
            if ((statement.getInstruction().flowType == Instruction.FlowType.CALL || statement.getInstruction().flowType == Instruction.FlowType.INT) && outputOptions.contains(OutputOption.PARAMETERS)) {
                statement.context = new StatementContext();
                statement.context.cpuState = ((ArmCPUState) context.cpuState).createCopy();
            }

            codeStructure.putStatement(context.cpuState.pc, statement);
        }
        else {
            // No structure analysis, output right now
            if (outWriter != null) {
                Disassembler.printDisassembly(outWriter, statement, context.cpuState.pc, memoryFileOffset, outputOptions);
            }
        }

        return statement.numData << 1;
    }

    @Override
    protected int disassembleOne32BitStatement(StatementContext context, Range memRange, int memoryFileOffset, CodeStructure codeStructure, Set<OutputOption> outputOptions) throws DisassemblyException {
        throw new DisassemblyException("Darm only has 16-bit instructions. Please check your dfr.txt config file for wrong CODE ranges");
    }


    protected int disassembleOneDataRecord(StatementContext context, Range memRange, int memoryFileOffset, Set<OutputOption> outputOptions) throws IOException {

        int sizeInBytes = 0;

        for (RangeType.Width spec : memRange.getRangeType().widths)
        {
            ArmStatement statement = new ArmStatement(memRange.getStart());
            statement.getNextData(memory, context.cpuState.pc);
            if (spec.getWidth()>2)
                statement.getNextData(memory, context.cpuState.pc);

            statement.setInstruction(ArmInstructionSet.opData[spec.getIndex()]);

            statement.decodeOperands(context.cpuState.pc, memory);

            statement.formatOperandsAndComment(context, true, this.outputOptions);

            sizeInBytes += statement.numData << 1;

            if (outWriter != null) {
                Disassembler.printDisassembly(outWriter, statement, context.cpuState.pc, memoryFileOffset, outputOptions);
            }
        }

        return sizeInBytes;
    }


    protected CPUState getCPUState(Range memRange) {
        return new ArmCPUState(memRange.getStart());
    }

    /* initialization */
    public void initialize() throws IOException {
        super.initialize();
        ArmInstructionSet.init(outputOptions);

        ArmStatement.initFormatChars(outputOptions);

        ArmCPUState.initRegisterLabels(outputOptions);
    }


    protected String[][] getRegisterLabels() {
        return ArmCPUState.REG_LABEL;
    }

    protected CodeStructure getCodeStructure(int start) {
        return new ArmCodeStructure(start);
    }
}
