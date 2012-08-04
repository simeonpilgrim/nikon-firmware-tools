package com.nikonhacker.disassembly.fr;

///*
// * Copyright (c) 2007, Kevin Schoedel. All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions
// * are met:
// *
// * - Redistributions of source code must retain the above copyright
// *   notice, this list of conditions and the following disclaimer.
// *
// * - Redistributions in binary form must reproduce the above copyright
// *   notice, this list of conditions and the following disclaimer in the
// * 	 documentation and/or other materials provided with the distribution.
// *
// * - Neither the name of Kevin Schoedel nor the names of contributors
// *   may be used to endorse or promote products derived from this software
// *   without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
// * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */

///*
// *  1.00  2007/11/05  kps   First release.
// *  1.01  2007/11/06  kps   Fix unsigned int types, option parsing;
// *                          added split output; other minor tweaks.
// *  1.02  2007/11/07  kps   Bug fixes; minimal data flow tracking.
// *  1.03  2007/11/15  kps   Fixed a stupid bug.
//
// Further modifications and port to C# by Simeon Pilgrim
// Further modifications and port to Java by Vicne
// */

import com.nikonhacker.disassembly.*;

import java.io.*;
import java.util.*;

public class Dfr extends Disassembler
{
    private static final String DEFAULT_OPTIONS_FILENAME = "dfr.txt";

    public static void main(String[] args) throws IOException, DisassemblyException, ParsingException {
        new Dfr().execute(args);
    }


    ///* output */


    protected int disassembleOneStatement(CPUState cpuState, Range memRange, int memoryFileOffset, CodeStructure codeStructure, Set<OutputOption> outputOptions) throws IOException {
        FrStatement statement = new FrStatement(memRange.getStart());
        statement.getNextStatement(memory, cpuState.pc);
        FrInstruction instruction = FrInstruction.instructionMap[statement.data[0]];

        if (instruction == null) {
            statement.setInstruction(FrInstruction.opData[DataType.SpecType_MD_WORD]);
        }
        else {
            statement.setInstruction(instruction);
        }

        statement.decodeOperands(cpuState.pc, memory);

        statement.formatOperandsAndComment(cpuState, true, this.outputOptions);

        if (codeStructure != null) {
            if ((statement.getInstruction().flowType == Instruction.FlowType.CALL || statement.getInstruction().flowType == Instruction.FlowType.INT) && outputOptions.contains(OutputOption.PARAMETERS)) {
                statement.cpuState = ((FrCPUState)(cpuState)).clone();
            }

            codeStructure.getStatements().put(cpuState.pc, statement);
        }
        else {
            // No structure analysis, output right now
            if (outWriter != null) {
                Disassembler.printDisassembly(outWriter, statement, cpuState.pc, memoryFileOffset, outputOptions);
            }
        }

        return statement.n << 1;
    }


    protected int disassembleOneDataRecord(CPUState dummyCpuState, Range memRange, int memoryFileOffset, Set<OutputOption> outputOptions) throws IOException {

        int sizeInBytes = 0;

        for (int spec : memRange.getDataType().spec)
        {
            FrStatement statement = new FrStatement(memRange.getStart());
            statement.getNextData(memory, dummyCpuState.pc);
            statement.x = statement.data[0];
            statement.xBitWidth = 16;
            statement.setInstruction(FrInstruction.opData[spec]);

            statement.decodeOperands(dummyCpuState.pc, memory);

            statement.formatOperandsAndComment(dummyCpuState, true, this.outputOptions);

            sizeInBytes += statement.n << 1;

            if (outWriter != null) {
                Disassembler.printDisassembly(outWriter, statement, dummyCpuState.pc, memoryFileOffset, outputOptions);
            }
        }

        return sizeInBytes;
    }


    protected CPUState getCPUState(Range memRange) {
        return new FrCPUState(memRange.getStart());
    }

    protected String getDefaultOptionsFilename() {
        return DEFAULT_OPTIONS_FILENAME;
    }
    ///* initialization */

    public void initialize() throws IOException {
        super.initialize();
        FrInstruction.initOpcodeMap(outputOptions);

        FrStatement.initFormatChars(outputOptions);

        FrCPUState.initRegisterLabels(outputOptions);
    }


    ///* options */


    protected String[] getRegisterLabels() {
        return FrCPUState.REG_LABEL;
    }

    protected CodeStructure getCodeStructure(int start) {
        return new FrCodeStructure(start);
    }
}