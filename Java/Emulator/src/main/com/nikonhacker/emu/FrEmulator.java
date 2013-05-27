package com.nikonhacker.emu;

import com.nikonhacker.Format;
import com.nikonhacker.IndentPrinter;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.fr.FrInstructionSet;
import com.nikonhacker.disassembly.fr.FrStatement;
import com.nikonhacker.emu.interrupt.fr.FrInterruptRequest;
import com.nikonhacker.emu.memory.AutoAllocatingMemory;
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
 * All implemented operations can be tested with the EmulatorTest class
 */
public class FrEmulator extends Emulator {

    public static void main(String[] args) throws IOException, EmulationException, ParsingException {
        if (args.length < 2) {
            System.err.println("Usage Emulator <file> <initialPc>");
            System.err.println(" e.g. Emulator fw.bin 0x40000");
            System.exit(-1);
        }
        int initialPc = Format.parseUnsigned(args[1]);

        AutoAllocatingMemory memory = new AutoAllocatingMemory();
        memory.loadFile(new File(args[0]), initialPc, false); // TODO use ranges
        EmulatorOptions.debugMemory = false;

        FrCPUState cpuState = new FrCPUState(initialPc);

        Platform platform = new Platform();
        platform.setCpuState(cpuState);
        platform.setMemory(memory);

        FrEmulator emulator = new FrEmulator();

        emulator.setMemory(memory);
        emulator.setCpuState(cpuState);
        emulator.setInterruptController(new FrInterruptController(platform));
        emulator.setPrinter(new PrintWriter(System.out));

        emulator.play();
    }

    public FrEmulator() {
    }


    @Override
    public void setOutputOptions(Set<OutputOption> outputOptions) {
        super.setOutputOptions(outputOptions);
        FrInstructionSet.init(outputOptions);
        FrStatement.initFormatChars(outputOptions);
        FrCPUState.initRegisterLabels(outputOptions);
    }

    /**
     * Starts emulating
     * @return the BreakCondition that caused the emulator to stop
     * @throws EmulationException
     */
    @Override
    public BreakCondition play() throws EmulationException {

        FrStatement statement = new FrStatement();

        FrCPUState frCpuState = (FrCPUState)cpuState;

        frCpuState.setAllRegistersDefined();

        try {
            for (;;) {
                
                statement.reset();

                statement.getNextStatement(memory, frCpuState.pc);
    
                statement.setInstruction(FrInstructionSet.instructionMap[statement.data[0]]);
    
                statement.decodeOperands(frCpuState.pc, memory);

                if (printer != null) {
                    // copying to make sure we keep a reference even if instructionPrintWriter gets set to null in between but still avoid costly synchronization
                    IndentPrinter printer2 = printer;
                    if (printer2 != null) {
                        // OK. copy is still not null
                        statement.formatOperandsAndComment(context, false, outputOptions);
                        printer2.print("0x" + Format.asHex(frCpuState.pc, 8) + " " + statement.toString(outputOptions));

                        switch(statement.getInstruction().getFlowType()) {
                            case CALL:
                            case INT:
                                printer2.indent(); break;
                            case RET:
                                printer2.outdent(); break;
                        }
                    }
                }

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
                        frCpuState.pc = context.nextPc;
                        context.nextPc = null;
                        if (context.nextReturnAddress != null) {
                            frCpuState.setReg(FrCPUState.RP, context.nextReturnAddress);
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
                            if (frCpuState.accepts(interruptRequest)){
                                if (printer != null) {
                                    IndentPrinter printer2 = printer;
                                    if (printer2 != null) {
                                        printer2.printlnNonIndented("------------------------- Accepting " + interruptRequest);
                                        printer2.indent();
                                    }
                                }
                                interruptController.removeRequest(interruptRequest);
                                ((FrInterruptController)interruptController).processInterrupt(interruptRequest.getInterruptNumber(), frCpuState.pc, context);

                                frCpuState.setILM(interruptRequest.getICR(), false);
                            }
                        }
                    }
                }

                /* Break if requested */
                if (!breakConditions.isEmpty()) {
                    //Double test to avoid useless synchronization if empty, at the cost of a double test when not empty (debug)
                    synchronized (breakConditions) {
                        for (BreakCondition breakCondition : breakConditions) {
                            if(breakCondition.matches(frCpuState, memory)) {
                                BreakTrigger trigger = breakCondition.getBreakTrigger();
                                if (trigger != null) {
                                    if (trigger.mustBeLogged() && breakLogPrintWriter != null) {
                                        trigger.log(breakLogPrintWriter, frCpuState, context.callStack, memory);
                                    }
                                    if (trigger.getInterruptToRequest() != null) {
                                        interruptController.request(trigger.getInterruptToRequest());
                                    }
                                    if (trigger.getPcToSet() != null) {
                                        frCpuState.pc = trigger.getPcToSet();
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
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.err.println(frCpuState);
            try {
                statement.formatOperandsAndComment(context, false, outputOptions);
                System.err.println("Offending instruction : " + statement);
            }
            catch(Exception e1) {
                System.err.println("Cannot disassemble offending instruction :" + statement.getFormattedBinaryStatement());
            }
            System.err.println("(on or before PC=0x" + Format.asHex(frCpuState.pc, 8) + ")");
            throw new EmulationException(e);
        }
    }

}
