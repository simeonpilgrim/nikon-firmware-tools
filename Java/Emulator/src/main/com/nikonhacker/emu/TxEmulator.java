package com.nikonhacker.emu;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.Instruction;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.disassembly.tx.TxInstruction;
import com.nikonhacker.disassembly.tx.TxInstructionSet;
import com.nikonhacker.disassembly.tx.TxStatement;
import com.nikonhacker.emu.interrupt.InterruptRequest;
import com.nikonhacker.emu.interrupt.tx.TxInterruptRequest;
import com.nikonhacker.emu.interrupt.tx.Type;
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
                                processInterrupt((TxInterruptRequest) interruptRequest, txCpuState.getPc());
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

    private void processInterrupt(TxInterruptRequest interruptRequest, int pcToStore) {
        TxCPUState txCPUState = (TxCPUState) cpuState;

        txCPUState.setSscrPSS(txCPUState.getSscrCSS());

        // This follows the graphs in the architecture document (Table 6.3), but then some info was added from the HW spec (ex : setting EXL or ERL)
        switch (interruptRequest.getType()) {
            case RESET_EXCEPTION:
                txCPUState.setStatusBEV();
                txCPUState.clearStatusNMI();
                txCPUState.setStatusERL();
                txCPUState.clearStatusRP();
                txCPUState.setReg(TxCPUState.ErrorEPC, pcToStore);
                // set PSS to CSS without changing CSS
                txCPUState.setSscrPSS(txCPUState.getSscrCSS());

                // Branch to reset routine
                txCPUState.setPc(TxCPUState.RESET_ADDRESS);
                break;
            case NMI:
                txCPUState.setStatusNMI();
                txCPUState.setStatusERL();
                // hardware spec section 6.2.2.1 says BD should be modified in this case,
                // but table 6.3 section 6.1.3.3 says it should not.
                // Architecture spec also says it shouldn't, so...
                // txCPUState.setCauseBD(context.inDelaySlot);
                txCPUState.setReg(TxCPUState.ErrorEPC, pcToStore);
                // set PSS to CSS without changing CSS
                txCPUState.setSscrPSS(txCPUState.getSscrCSS());

                // Branch to reset routine
                txCPUState.setPc(TxCPUState.RESET_ADDRESS);
                break;
            default:
                if (!txCPUState.isStatusEXLSet()) {
                    if (context.getStoredDelaySlotType() == Instruction.DelaySlotType.NONE) {
                        txCPUState.clearCauseBD();
                    }
                    else {
                        txCPUState.setCauseBD();
                    }
                }
                txCPUState.setStatusEXL();
                txCPUState.setCauseExcCode(interruptRequest.getCode());
                txCPUState.setReg(TxCPUState.EPC, pcToStore);

                if (interruptRequest.getType().isInterrupt()) {
                    // Interrupt
                    // set CSS to PSS and change CSS
                    txCPUState.pushSscrCssIfSwitchingEnabled(interruptRequest.getLevel());

                    // set ILEV
                    ((TxInterruptController)interruptController).pushIlevCmask(interruptRequest.getLevel());

                    // Branch to handler
                    if (txCPUState.isStatusBEVSet()) {
                        if (txCPUState.isCauseIVSet()) {
                            // BEV=1 & IV=1
                            txCPUState.setPc(TxInterruptController.ADDRESS_INTERRUPT_BEV1_IV1);
                        }
                        else {
                            // BEV=1 & IV=0
                            txCPUState.setPc(TxInterruptController.ADDRESS_INTERRUPT_BEV1_IV0);
                        }
                    }
                    else {
                        if (txCPUState.isCauseIVSet()) {
                            // BEV=0 & IV=1
                            txCPUState.setPc(TxInterruptController.ADDRESS_INTERRUPT_BEV0_IV1);
                        }
                        else {
                            // BEV=0 & IV=0
                            txCPUState.setPc(TxInterruptController.ADDRESS_INTERRUPT_BEV0_IV0);
                        }
                    }

                    // IVR = 4 x interrupt_number
                    ((TxInterruptController) interruptController).setIvr8_0(interruptRequest.getInterruptNumber() << 2);

                }
                else {
                    // Other Exceptions
                    if (interruptRequest.getType() == Type.COPROCESSOR_UNUSABLE_EXCEPTION) {
                        txCPUState.setCauseCE(interruptRequest.getCoprocessorNumber());
                    }

                    if (   (interruptRequest.getType() == Type.INSTRUCTION_ADDRESS_ERROR_EXCEPTION)
                        || (interruptRequest.getType() == Type.DATA_ADDRESS_ERROR_EXCEPTION)) {
                        txCPUState.setReg(TxCPUState.BadVAddr, interruptRequest.getBadVAddr());
                    }

                    // Branch to handler
                    if (txCPUState.isStatusBEVSet()) {
                        // BEV=1
                        txCPUState.setPc(TxInterruptController.ADDRESS_INTERRUPT_BEV1_IV0);
                    }
                    else {
                        // BEV=0
                        txCPUState.setPc(TxInterruptController.ADDRESS_INTERRUPT_BEV0_IV0);
                    }
                    // set PSS to CSS without changing CSS
                    txCPUState.setSscrPSS(txCPUState.getSscrCSS());
                }
        }
    }

}
