package com.nikonhacker.emu;

import com.nikonhacker.Format;
import com.nikonhacker.IndentPrinter;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.fr.FrInstructionSet;
import com.nikonhacker.disassembly.fr.FrStatement;
import com.nikonhacker.emu.interrupt.fr.FrInterruptRequest;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.peripherials.clock.fr.FrClockGenerator;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.BreakCondition;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * This Emulator is based on :
 * - FR Family instruction manual for the basics - http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-00101-5E.pdf
 * - FR80 Family programming manual for specifics - http://edevice.fujitsu.com/fj/MANUAL/MANUALp/en-pdf/CM71-00104-3E.pdf
 * All implemented instructions can be tested with the FrEmulatorTest class
 */
public class FrEmulator extends Emulator {

    FrStatement statement = new FrStatement();

    public FrEmulator() {
    }

    @Override
    public void setOutputOptions(Set<OutputOption> outputOptions) {
        super.setOutputOptions(outputOptions);
        FrInstructionSet.init(outputOptions);
        FrStatement.initFormatChars(outputOptions);
        FrCPUState.initRegisterLabels(outputOptions);
    }

    @Override
    public int getFrequencyHz() {
        // TODO use ((FrClockGenerator)platform.getClockGenerator).getXxx();
        return FrClockGenerator.FREQUENCY;
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

            statement.getNextStatement(memory, cpuState.pc);

            statement.setInstruction(FrInstructionSet.instructionMap[statement.data[0]]);

            statement.decodeOperands(cpuState.pc, memory);

            if (printer != null) {
                // copying to make sure we keep a reference even if instructionPrintWriter gets set to null in between but still avoid costly synchronization
                IndentPrinter printer2 = printer;
                if (printer2 != null) {
                    // OK. copy is still not null
                    statement.formatOperandsAndComment(context, false, outputOptions);
                    printer2.print("0x" + Format.asHex(cpuState.pc, 8) + " " + statement.toString(outputOptions));

                    switch(statement.getInstruction().getFlowType()) {
                        case CALL:
                        case INT:
                            printer2.indent(); break;
                        case RET:
                            printer2.outdent(); break;
                    }
                }
            }

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
                    cpuState.pc = context.nextPc;
                    context.nextPc = null;
                    if (context.nextReturnAddress != null) {
                        cpuState.setReg(FrCPUState.RP, context.nextReturnAddress);
                        context.nextReturnAddress = null;
                    }
                }
                else {
                    context.delaySlotDone = true;
                }
            }
            else {
                // If not in a delay slot, check interrupts
                if(interruptController.hasPendingRequests()) { // This call is not synchronized, so it skips fast
                    FrInterruptRequest interruptRequest = (FrInterruptRequest) interruptController.getNextRequest();
                    //Double test because lack of synchronization means the status could have changed in between
                    if (interruptRequest != null) {
                        if (cpuState.accepts(interruptRequest)){
                            if (printer != null) {
                                IndentPrinter printer2 = printer;
                                if (printer2 != null) {
                                    printer2.printlnNonIndented("------------------------- Accepting " + interruptRequest);
                                    printer2.indent();
                                }
                            }
                            interruptController.removeEdgeTriggeredRequest(interruptRequest);
                            ((FrInterruptController)interruptController).processInterrupt(interruptRequest.getInterruptNumber(), cpuState.pc, context);

                            ((FrCPUState)cpuState).setILM(interruptRequest.getICR(), false);
                        }
                    }
                }
            }

            /* Break if requested */
            if (!breakConditions.isEmpty()) {
                //Double test to avoid useless synchronization if empty, at the cost of a double test when not empty (debug)
                synchronized (breakConditions) {
                    for (BreakCondition breakCondition : breakConditions) {
                        if (breakCondition.matches(cpuState, memory)) {
                            BreakTrigger trigger = breakCondition.getBreakTrigger();
                            if (trigger != null) {
                                if (trigger.mustBeLogged() && breakLogPrintWriter != null) {
                                    trigger.log(breakLogPrintWriter, cpuState, context.callStack, memory);
                                }
                                if (trigger.getInterruptToRequest() != null) {
                                    interruptController.request(trigger.getInterruptToRequest());
                                }
                                if (trigger.getPcToSet() != null) {
                                    cpuState.pc = trigger.getPcToSet();
                                }
                            }
                            if (trigger == null || trigger.mustBreak()) {
                                return breakCondition;
                            }
                        }
                    }
                }
            }

            /* Pause if requested */
            if (sleepIntervalMs != 0) {
                exitSleepLoop = false;
                if (sleepIntervalMs < 100) {
                    try {
                        Thread.sleep(sleepIntervalMs);
                    } catch (InterruptedException e) {
                        // noop
                    }
                }
                else {
                    for (int i = 0; i < sleepIntervalMs /100; i++) {
                        try {
                            Thread.sleep(100);
                            if (exitSleepLoop) {
                                break;
                            }
                        } catch (InterruptedException e) {
                            // noop
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.err.println(cpuState);
            try {
                statement.formatOperandsAndComment(context, false, outputOptions);
                System.err.println("Offending instruction : " + statement);
            }
            catch(Exception e1) {
                System.err.println("Cannot disassemble offending instruction :" + statement.getFormattedBinaryStatement());
            }
            System.err.println("(on or before PC=0x" + Format.asHex(cpuState.pc, 8) + ")");
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

        Platform platform = new Platform();
        platform.setCpuState(cpuState);
        platform.setMemory(memory);

        FrEmulator emulator = new FrEmulator();

        emulator.setMemory(memory);
        emulator.setCpuState(cpuState);
        emulator.setInterruptController(new FrInterruptController(platform));
        emulator.setPrinter(new PrintWriter(System.out));

        new MasterClock().add(new FrEmulator(), null, true);
    }
}
