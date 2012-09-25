package com.nikonhacker.emu;

import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.disassembly.tx.TxInstructionSet;
import com.nikonhacker.disassembly.tx.TxStatement;
import com.nikonhacker.emu.trigger.condition.BreakCondition;

import java.util.Set;

public class TxEmulator extends Emulator {
    @Override
    public void setOutputOptions(Set<OutputOption> outputOptions) {
        TxInstructionSet.init(outputOptions);
        TxStatement.initFormatChars(outputOptions);
        TxCPUState.initRegisterLabels(outputOptions);
        this.outputOptions = outputOptions;
    }


    /**
     * Starts emulating
     * @return the BreakCondition that caused the emulator to stop
     * @throws EmulationException
     */
    @Override
    public BreakCondition play() throws EmulationException {
        throw new RuntimeException("TxEmulator.play unimplemented");
    }
}
