package com.nikonhacker.emu;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.disassembly.tx.TxInstruction;
import com.nikonhacker.disassembly.tx.TxInstructionSet;
import com.nikonhacker.disassembly.tx.TxStatement;
import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.interrupt.tx.TxInterruptRequest;
import com.nikonhacker.emu.peripherials.interruptController.TxInterruptController;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.BreakCondition;

import java.io.PrintWriter;
import java.util.Set;

public class TxEmulator extends Emulator {

    public TxEmulator() {
    }

    @Override
    public void setOutputOptions(Set<OutputOption> outputOptions) {
        super.setOutputOptions(outputOptions);
        TxInstructionSet.init(outputOptions);
        TxStatement.initFormatChars(outputOptions);
        TxCPUState.initRegisterLabels(outputOptions);
    }


    /**
     * Starts emulating
     *
     * @return the BreakCondition that caused the emulator to stop
     * @throws EmulationException
     */
    @Override
    public BreakCondition play() throws EmulationException {
        TxStatement statement = new TxStatement();

        TxCPUState txCpuState = (TxCPUState) cpuState;

        txCpuState.setAllRegistersDefined();

        try {
            for (;;) {

                statement.reset();
                if (((TxCPUState) cpuState).is16bitIsaMode) {
                    int binaryStatement16 = memory.loadInstruction16(cpuState.pc);

                    statement.fill16bInstruction(binaryStatement16, cpuState.pc, memory);

                    statement.decode16BitOperands(cpuState.pc);
                }
                else {
                    int binaryStatement32 = memory.loadInstruction32(cpuState.pc);

                    statement.fill32bInstruction(binaryStatement32);

                    statement.decode32BitOperands();
                }

                if (instructionPrintWriter != null) {
                    // copying to make sure we keep a reference even if instructionPrintWriter gets set to null in between but still avoid costly synchronization
                    PrintWriter printWriter = instructionPrintWriter;
                    if (printWriter != null) {
                        // OK. copy is still not null
                        statement.formatOperandsAndComment(context, false, outputOptions);
                        printWriter.print("0x" + Format.asHex(txCpuState.pc, 8) + " " + statement.toString(outputOptions));
                    }
                }

                // ACTUAL INSTRUCTION EXECUTION
                ((TxInstruction) statement.getInstruction()).getSimulationCode().simulate(statement, context);

                totalCycles ++; // approximation

                /* Delay slot processing */
                if (context.nextPc != null) {
                    if (context.delaySlotDone) {
                        txCpuState.setPc(context.nextPc);
                        context.nextPc = null;
                        if (context.nextReturnAddress != null) {
                            int targetRegister = TxCPUState.RA;
                            if (context.nextReturnAddressTargetRegister != null) {
                                targetRegister = context.nextReturnAddressTargetRegister;
                                context.nextReturnAddressTargetRegister = null;
                            }
                            txCpuState.setReg(targetRegister, context.nextReturnAddress);
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
                        InterruptRequest interruptRequest = interruptController.getNextRequest();
                        //Double test because lack of synchronization means the status could have changed in between
                        if (interruptRequest != null) {
                            if (txCpuState.accepts(interruptRequest)){
                                if (instructionPrintWriter != null) {
                                    PrintWriter printWriter = instructionPrintWriter;
                                    if (printWriter != null) {
                                        printWriter.println("------------------------- Accepting " + interruptRequest);
                                    }
                                }
                                // TODO : We probably should not remove the request from queue automatically.
                                // TODO   This has to be done explicitely by writing to INTCLR register
                                interruptController.removeRequest(interruptRequest);
                                // TODO : Currently, interrupts are not checked in delay slots (see above).
                                // TODO   Permit that and use address of branch instruction instead of PC if in delay slot !
                                // Note : must use getPc() so that current ISA mode is stored and restored when returning from interrupt
                                ((TxInterruptController)interruptController).processInterrupt((TxInterruptRequest) interruptRequest, txCpuState.getPc(), context);
                            }
                        }
                    }
                }

                /* Break if requested */
                if (!breakConditions.isEmpty()) {
                    //Double test to avoid useless synchronization if empty, at the cost of a double test when not empty (debug)
                    synchronized (breakConditions) {
                        for (BreakCondition breakCondition : breakConditions) {
                            if (breakCondition.matches(txCpuState, memory)) {
                                BreakTrigger trigger = breakCondition.getBreakTrigger();
                                if (trigger != null) {
                                    if (trigger.mustBeLogged() && breakLogPrintWriter != null) {
                                        trigger.log(breakLogPrintWriter, txCpuState, context.callStack, memory);
                                    }
                                    if (trigger.getInterruptToRequest() != null) {
                                        interruptController.request(trigger.getInterruptToRequest());
                                    }
                                    if (trigger.getPcToSet() != null) {
                                        txCpuState.pc = trigger.getPcToSet();
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
                        for (int i = 0; i < sleepIntervalMs / 100; i++) {
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
            System.err.println(txCpuState);
            try {
                statement.formatOperandsAndComment(context, false, outputOptions);
                System.err.println("Offending instruction : " + statement);
            } catch (Exception e1) {
                System.err.println("Cannot disassemble offending instruction :" + statement.getFormattedBinaryStatement());
            }
            System.err.println("(on or before PC=0x" + Format.asHex(txCpuState.pc, 8) + ")");
            throw new EmulationException(e);
        }

    }


}
