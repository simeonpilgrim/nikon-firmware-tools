package com.nikonhacker.emu;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.fr.FrInstructionSet;
import com.nikonhacker.disassembly.fr.FrStatement;
import com.nikonhacker.emu.interrupt.fr.FrInterruptRequest;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.clock.fr.FrClockGenerator;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.trigger.condition.BreakCondition;
import com.nikonhacker.gui.component.disassembly.DisassemblyLogger;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

/**
 * This Emulator is based on :
 * - FR Family instruction manual for the basics - http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-00101-5E.pdf
 * - FR80 Family programming manual for specifics - http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-00104-3E.pdf
 * All implemented instructions can be tested with the FrEmulatorTest class
 */
public class FrEmulator extends Emulator {

    private Set<OutputOption> lastOutputOptions;
    private boolean lastOutputOptionsValid = false;

    public FrEmulator(Platform platform) {
        super(platform);
        statement = new FrStatement();
    }

    @Override
    public void setOutputOptions(Set<OutputOption> outputOptions) {
        // coderat: optimisation, because it is called on each "step" of emulator and init of all instruction maps takes too long
        if (!lastOutputOptionsValid || !lastOutputOptions.equals(outputOptions)) {
            lastOutputOptions = EnumSet.copyOf(outputOptions);
            lastOutputOptionsValid = true;

            super.setOutputOptions(outputOptions);
            FrInstructionSet.init(outputOptions);
            FrStatement.initFormatChars(outputOptions);
            FrCPUState.initRegisterLabels(outputOptions);
        }
    }

    @Override
    public int getChip() {
        return Constants.CHIP_FR;
    }

    @Override
    public int getFrequencyHz() {
        return ((FrClockGenerator)platform.getClockGenerator()).getCClkFrequency();
    }

    /**
     * Perform one emulation step
     * @return the condition that requires emulation to stop, or null if it should continue
     * @throws EmulationException
     */
    @Override
    public BreakCondition onClockTick() throws EmulationException {

        // Skip clock ticks if previous instruction required several cycles
        if (context.cycleIncrement > 1) {
            context.cycleIncrement--;
            return null;
        }

        try {
            statement.reset();

            // FETCH
            ((FrStatement)statement).getNextStatement(platform.memory, platform.cpuState.pc);

            // DECODE
            statement.setInstruction(FrInstructionSet.instructionMap[((FrStatement)statement).data[0]]);
            ((FrStatement)statement).decodeOperands(platform.cpuState.pc, platform.memory);

            // LOG
            logIfRequested(logger);

            // ACTUAL INSTRUCTION EXECUTION
            statement.getInstruction().getSimulationCode().simulate(statement, context);

            // Notify CPU cycle listeners
            int cycleListenerNumber = 0;
            while (cycleListenerNumber < cycleCounterListeners.size()) {
                CycleCounterListener cycleCounterListener = cycleCounterListeners.get(cycleListenerNumber);
                if (cycleCounterListener.onCycleCountChange(totalCycles, context.cycleIncrement)) {
                    cycleListenerNumber++;
                }
                else {
                    cycleCounterListeners.remove(cycleCounterListener);
                }
            }

            totalCycles += context.cycleIncrement;

            /* Delay slot processing */
            if (context.nextPc != null) {
                if (context.delaySlotDone) {
                    platform.cpuState.pc = context.nextPc;
                    context.nextPc = null;
                    if (context.nextReturnAddress != null) {
                        platform.cpuState.setReg(FrCPUState.RP, context.nextReturnAddress);
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
                    FrInterruptRequest interruptRequest = (FrInterruptRequest) platform.interruptController.getNextRequest();
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
                            platform.interruptController.removeEdgeTriggeredRequest(interruptRequest);
                            ((FrInterruptController)platform.interruptController).processInterrupt(interruptRequest.getInterruptNumber(), platform.cpuState.pc, context);

                            ((FrCPUState)platform.cpuState).setILM(interruptRequest.getICR(), false);
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


    public static void main(String[] args) throws IOException, EmulationException, ParsingException {
        if (args.length < 2) {
            System.err.println("Usage Emulator <file> <initialPc>");
            System.err.println(" e.g. Emulator fw.bin 0x40000");
            System.exit(-1);
        }
        int initialPc = Format.parseUnsigned(args[1]);

        DebuggableMemory memory = new DebuggableMemory(false);
        memory.loadFile(new File(args[0]), initialPc, false); // TODO use ranges
        memory.setLogMemoryMessages(false);

        FrCPUState cpuState = new FrCPUState(initialPc);

        MasterClock masterClock = new MasterClock();
        Platform platform = new Platform(masterClock);
        platform.setCpuState(cpuState);
        platform.setMemory(memory);

        FrEmulator emulator = new FrEmulator(platform);

        emulator.setContext(memory, cpuState, new FrInterruptController(platform));
        emulator.setDisassemblyLogger(new DisassemblyLogger(System.out));

        masterClock.add(new FrEmulator(platform), null, true);
    }
}
