package com.nikonhacker.emu;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.disassembly.tx.TxInstructionSet;
import com.nikonhacker.disassembly.tx.TxStatement;
import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.interrupt.tx.TxInterruptRequest;
import com.nikonhacker.emu.peripherials.clock.tx.TxClockGenerator;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.trigger.condition.BreakCondition;
import com.nikonhacker.gui.component.disassembly.DisassemblyLogger;

import java.util.Set;

public class TxEmulator extends Emulator {

    public TxEmulator(Platform platform) {
        super(platform);
        statement = new TxStatement();
    }

    @Override
    public void setOutputOptions(Set<OutputOption> outputOptions) {
        super.setOutputOptions(outputOptions);
        TxInstructionSet.init(outputOptions);
        TxStatement.initFormatChars(outputOptions);
        TxCPUState.initRegisterLabels(outputOptions);
    }


    @Override
    public int getChip() {
        return Constants.CHIP_TX;
    }

    @Override
    public int getFrequencyHz() {
        return ((TxClockGenerator)platform.getClockGenerator()).getfCpuHz();
    }

    /**
     * Perform one emulation step
     * @return the condition that requires emulation to stop, or null if it should continue
     * @throws EmulationException
     */
    @Override
    public BreakCondition onClockTick() throws EmulationException {
        // TODO skip clock ticks if previous instruction required several cycles

        try {
            statement.reset();

            if (((TxCPUState) platform.cpuState).is16bitIsaMode) {
                // FETCH
                ((TxStatement)statement).fill16bInstruction(platform.memory.loadInstruction16(platform.cpuState.pc), platform.cpuState.pc, platform.memory);
                // DECODE
                ((TxStatement)statement).decode16BitOperands(platform.cpuState.pc);
            }
            else {
                // FETCH
                ((TxStatement)statement).fill32bInstruction(platform.memory.loadInstruction32(platform.cpuState.pc));
                // DECODE
                ((TxStatement)statement).decode32BitOperands();
            }

            // LOG
            logIfRequested(logger);

            // ACTUAL INSTRUCTION EXECUTION
            statement.getInstruction().getSimulationCode().simulate(statement, context);

            // Notify CPU cycle listeners
            int cycleListenerNumber = 0;
            while (cycleListenerNumber < cycleCounterListeners.size()) {
                CycleCounterListener cycleCounterListener = cycleCounterListeners.get(cycleListenerNumber);
                if (cycleCounterListener.onCycleCountChange(totalCycles, 1)) {
                    cycleListenerNumber++;
                }
                else {
                    cycleCounterListeners.remove(cycleCounterListener);
                }
            }

            totalCycles ++; // approximation

            /* Delay slot processing */
            if (context.nextPc != null) {
                if (context.delaySlotDone) {
                    platform.cpuState.setPc(context.nextPc);
                    context.nextPc = null;
                    if (context.nextReturnAddress != null) {
                        int targetRegister = TxCPUState.RA;
                        if (context.nextReturnAddressTargetRegister != null) {
                            targetRegister = context.nextReturnAddressTargetRegister;
                            context.nextReturnAddressTargetRegister = null;
                        }
                        platform.cpuState.setReg(targetRegister, context.nextReturnAddress);
                        context.nextReturnAddress = null;
                    }
                }
                else {
                    context.delaySlotDone = true;
                }
            }
            else {
                // If not in a delay slot, check interrupts
                if(platform.interruptController.hasPendingRequests()) { // This call is not synchronized, so it skips fast
                    final InterruptRequest interruptRequest = platform.interruptController.getNextRequest();
                    //Double test because lack of synchronization means the status could have changed in between
                    if (interruptRequest != null) {
                        if (platform.cpuState.accepts(interruptRequest)){
                            if (logger != null) {
                                DisassemblyLogger printer2 = logger;
                                if (printer2 != null) {
                                    if(printer2.isIncludeInterruptMarks()) {
                                        printer2.println(platform.getMasterClock().getFormatedTotalElapsedTimeMs() + " ------------------------- Accepting " + interruptRequest);
                                    }
                                    printer2.indent();
                                }
                            }
                            // TODO : We probably should not remove the request from queue automatically.
                            // TODO   This has to be done explicitely by writing to INTCLR register
                            platform.interruptController.removeEdgeTriggeredRequest(interruptRequest);
                            // TODO : Currently, interrupts are not checked in delay slots (see above).
                            // TODO   Permit that and use address of branch instruction instead of PC if in delay slot !
                            // Note : must use getPc() so that current ISA mode is stored and restored when returning from interrupt
                            context.pushInterrupt(interruptRequest);
                            ((TxInterruptController)platform.interruptController).processInterrupt((TxInterruptRequest) interruptRequest, platform.cpuState.getPc(), context);
                        }
                    }
                }
            }

            // Process breakConditions
            if (!breakConditions.isEmpty()) {
                synchronized (breakConditions) {
                    BreakCondition breakCondition = processConditions(breakConditions);
                    if (breakCondition != null) {
                        return breakCondition;
                    }
                }
            }

            /* Pause if requested */
            if (sleepIntervalMs != 0) {
                sleep();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.err.println(platform.cpuState);
            try {
                statement.formatOperandsAndComment(context, false, outputOptions);
                System.err.println("Offending instruction : " + statement);
            } catch (Exception e1) {
                System.err.println("Cannot disassemble offending instruction :" + statement.getFormattedBinaryStatement());
            }
            System.err.println("(on or before PC=0x" + Format.asHex(platform.cpuState.pc, 8) + ")");
            throw new EmulationException(e);
        }
        return null;
    }

}
