package com.nikonhacker.emu.peripherials.ioPort.tx;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.function.PinFunction;
import com.nikonhacker.emu.peripherials.ioPort.function.tx.*;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;

import java.util.Arrays;

public class TxIoPort extends IoPort {

    /** Port function register 1 */
    private byte functionRegister1;
    /** Port function register 2 */
    private byte functionRegister2;
    /** Port function register 3 */
    private byte functionRegister3;
    /** Port open-drain control register */
    private byte openDrainControlRegister;
    /** Port pull-up control register */
    private byte pullUpControlRegister;

    /** The 3 following arrays declare the functions that can be performed by each Port Pin
     * when the corresponding bit of the corresponding register is 1.
     * e.g. if fn1Handlers[2] is a TxIoPinSerialRxFunction, it means that
     * when bit 2 of functionRegister1 is set, then this port's pin 2 behaves as a Rx pin of a serial port.
     * Each array is 8 elements long and element [n] of port X corresponds to pin PXn.
     */
    private PinFunction[] functions1;
    private PinFunction[] functions2;
    private PinFunction[] functions3;

    public TxIoPort(int portNumber, InterruptController interruptController) {
        super(Constants.CHIP_TX, portNumber, interruptController);
        for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
            inputFunctions[bitNumber] = new TxIoPinInputFunction(getShortName() + bitNumber);
            outputFunctions[bitNumber] = new TxIoPinOutputFunction(getShortName() + bitNumber);
        }
    }

    public void setFunctions1(PinFunction[] functions1) {
        if (functions1.length != 8) {
            throw new RuntimeException("Trying to assign a Function 1 Role array of length " + functions1.length + ": " + Arrays.toString(functions1));
        }
        this.functions1 = functions1;
    }

    public void setFunctions2(PinFunction[] functions2) {
        if (functions2.length != 8) {
            throw new RuntimeException("Trying to assign a Function 2 Role array of length " + functions2.length + ": " + Arrays.toString(functions2));
        }
        this.functions2 = functions2;
    }

    public void setFunctions3(PinFunction[] functions3) {
        if (functions3.length != 8) {
            throw new RuntimeException("Trying to assign a Function 3 Role array of length " + functions3.length + ": " + Arrays.toString(functions3));
        }
        this.functions3 = functions3;
    }

    /**
     * Get configuration register mask - called by external devices
     * @return mask: 0=Input 1=Output
     */
    public byte getControlRegister() {
        return getDirection();
    }

    /**
     * Set configuration register mask - called by CPU
     * Synonym for the generic setDirection() method;
     * @param controlRegister mask: 0=Input 1=Output
     */
    public void setControlRegister(byte controlRegister) {
        setDirection(controlRegister);
    }

    public byte getFunctionRegister1() {
        return functionRegister1;
    }

    public void setFunctionRegister1(byte functionRegister1) {
        this.functionRegister1 = functionRegister1;
        updatePinFunctions();
    }

    public byte getFunctionRegister2() {
        return functionRegister2;
    }

    public void setFunctionRegister2(byte functionRegister2) {
        this.functionRegister2 = functionRegister2;
        updatePinFunctions();
    }

    public byte getFunctionRegister3() {
        return functionRegister3;
    }

    public void setFunctionRegister3(byte functionRegister3) {
        this.functionRegister3 = functionRegister3;
        updatePinFunctions();
    }

    private void updatePinFunctions() {
        for (int pinNumber = 0; pinNumber < 8; pinNumber++) {
            int pinMask = 1 << pinNumber;
            if (functions1 != null && (functionRegister1 & pinMask & 0xFF) != 0) {
                // fn1 defines the behaviour of this pin
                pins[pinNumber].setFunction(functions1[pinNumber]);
            }
            else if (functions2 != null && (functionRegister2 & pinMask & 0xFF) != 0) {
                // fn2 defines the behaviour of this pin
                pins[pinNumber].setFunction(functions2[pinNumber]);
            }
            else if (functions3 != null && (functionRegister3 & pinMask & 0xFF) != 0) {
                // fn3 defines the behaviour of this pin
                pins[pinNumber].setFunction(functions3[pinNumber]);
            }
            else if (pins[pinNumber].isInput()) {
                // pin is configured as plain input
                pins[pinNumber].setFunction(inputFunctions[pinNumber]);
            }
            else {
                // pin is configured as plain output
                pins[pinNumber].setFunction(outputFunctions[pinNumber]);
            }
        }
    }

    public byte getOpenDrainControlRegister() {
        return openDrainControlRegister;
    }

    public void setOpenDrainControlRegister(byte openDrainControlRegister) {
//        System.out.println("Port #" + portNumber + " : setOpenDrainControlRegister(0x" + Format.asHex( openDrainControlRegister& 0xFF, 2) + ")");
        this.openDrainControlRegister = openDrainControlRegister;
    }

    public byte getPullUpControlRegister() {
        return pullUpControlRegister;
    }

    public void setPullUpControlRegister(byte pullUpControlRegister) {
//        System.out.println("Port #" + portNumber + " : setPullUpControlRegister(0x" + Format.asHex(pullUpControlRegister& 0xFF, 2) + ")");
        this.pullUpControlRegister = pullUpControlRegister;
    }

    public byte getInputEnableControlRegister() {
        return getInputEnabled();
    }


    public void setInputEnableControlRegister(byte inputEnableControlRegister) {
        setInputEnabled(inputEnableControlRegister);
    }

    /**
     * This static method initializes all the ports of the Tx chip, declaring functions assigned to each bit of each function
     * register of each Tx port. This is a direct reformatting of register descriptions at chapter 7 of the spec.
     *
     * IMPORTANT! To facilitate transposition from the registers in the spec to this method, the pins are described
     * from bit 7 (on the left) to bit 0 (on the right).
     * But as these arrays have to be indexed by the bit number (0 first, 7 last), we insert a "reverse" call in between.
     *
     * That way, ioPorts[2].getFn3Handlers()[7] will correctly return the TxIoPinTimerInputHandler(0x5, 1), presented here at the first place
     */
    public static IoPort[] setupPorts(InterruptController interruptController, ProgrammableTimer[] programmableTimers) {
        TxIoPort[] ioPorts = new TxIoPort[TxIoListener.NUM_PORT];
        for (int i = 0; i < TxIoListener.NUM_PORT; i++) {
            ioPorts[i] = new TxIoPort(i, interruptController);
        }

        // Indicate port features
        // Indicate the meaning of function1 register for port 0
        ioPorts[IoPort.PORT_0].setFunctions1(reverse(new PinFunction[]{new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction()}));

        // Port 1
        ioPorts[IoPort.PORT_1].setFunctions1(reverse(new PinFunction[]{new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction()}));
        ioPorts[IoPort.PORT_1].setFunctions2(reverse(new PinFunction[]{new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction()}));

        // Port 2
        ioPorts[IoPort.PORT_2].setFunctions1(reverse(new PinFunction[]{new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction()}));
        ioPorts[IoPort.PORT_2].setFunctions2(reverse(new PinFunction[]{new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction()}));
        ioPorts[IoPort.PORT_2].setFunctions3(reverse(new PinFunction[]{new TxIoPinTimerInputFunction(programmableTimers[0x5], 1), new TxIoPinTimerInputFunction(programmableTimers[0x5], 0), new TxIoPinTimerInputFunction(programmableTimers[0x3], 1), new TxIoPinTimerInputFunction(programmableTimers[0x3], 0), new TxIoPinTimerInputFunction(programmableTimers[0x2], 1), new TxIoPinTimerInputFunction(programmableTimers[0x2], 0), new TxIoPinTimerInputFunction(programmableTimers[0x1], 1), new TxIoPinTimerInputFunction(programmableTimers[0x1], 0)}));

        // Port 3
        ioPorts[IoPort.PORT_3].setFunctions1(reverse(new PinFunction[]{new TxIoPinCpuSignalFunction(7), new TxIoPinCpuSignalFunction(6), new TxIoPinCpuSignalFunction(5), new TxIoPinCpuSignalFunction(4), new TxIoPinCpuSignalFunction(3), new TxIoPinCpuSignalFunction(2), new TxIoPinCpuSignalFunction(1), new TxIoPinCpuSignalFunction(0)}));
        ioPorts[IoPort.PORT_3].setFunctions2(reverse(new PinFunction[]{new TxIoPinCaptureInputFunction(0x3), new TxIoPinCaptureInputFunction(0x2), new TxIoPinCaptureInputFunction(0x1), new TxIoPinTimerOutputFunction(0xE), null, new TxIoPinCaptureInputFunction(0x0), null, null}));

        // Port 4
        ioPorts[IoPort.PORT_4].setFunctions1(reverse(new PinFunction[]{new TxIoPinTimerOutputFunction(0xF), null, null, new TxIoPinClockFunction(), new TxIoPinChipSelectFunction(3), new TxIoPinChipSelectFunction(2), new TxIoPinChipSelectFunction(1), new TxIoPinChipSelectFunction(0)}));
        ioPorts[IoPort.PORT_4].setFunctions2(reverse(new PinFunction[]{null, null, null, null, new TxIoPinKeyFunction(27), new TxIoPinKeyFunction(26), new TxIoPinKeyFunction(25), new TxIoPinKeyFunction(24)}));

        // Port 5
        ioPorts[IoPort.PORT_5].setFunctions1(reverse(new PinFunction[]{new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction()}));
        ioPorts[IoPort.PORT_5].setFunctions2(reverse(new PinFunction[]{new TxIoPinTimerOutputFunction(0x3), new TxIoPinTimerOutputFunction(0x2), new TxIoPinTimerOutputFunction(0x1), new TxIoPinTimerOutputFunction(0x0), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INTF), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INTE), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INTD), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INTC)}));
        ioPorts[IoPort.PORT_5].setFunctions3(reverse(new PinFunction[]{new TxIoPinKeyFunction(29), new TxIoPinKeyFunction(28), null, null, null, null, null, null}));

        // Port 6
        ioPorts[IoPort.PORT_6].setFunctions1(reverse(new PinFunction[]{new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction(), new TxIoPinBusFunction()}));
        ioPorts[IoPort.PORT_6].setFunctions2(reverse(new PinFunction[]{new TxIoPinTimerOutputFunction(0x5), new TxIoPinSerialClockFunction(1), new TxIoPinSerialRxFunction(1), new TxIoPinSerialTxFunction(1), new TxIoPinTimerOutputFunction(4), new TxIoPinSerialClockFunction(0), new TxIoPinSerialRxFunction(0), new TxIoPinSerialTxFunction(0)}));
        ioPorts[IoPort.PORT_6].setFunctions3(reverse(new PinFunction[]{null, null, new TxIoPinInterruptFunction(interruptController, TxInterruptController.INTB), null, null, new TxIoPinSerialCtsFunction(0), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INTA), null}));

        // Port 7
        ioPorts[IoPort.PORT_7].setFunctions2(reverse(new PinFunction[]{new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT13), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT12), null, null, new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT11), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT10), null, null}));

        // Port 8
        ioPorts[IoPort.PORT_8].setFunctions2(reverse(new PinFunction[]{new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT9), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT8), null, null, null, null, null, null}));

        // Port 9
        ioPorts[IoPort.PORT_9].setFunctions1(reverse(new PinFunction[]{new TxIoPinTimerOutputFunction(0xA), new TxIoPinSerialClockFunction(2), new TxIoPinSerialRxFunction(2), new TxIoPinSerialTxFunction(2), new TxIoPinTimerOutputFunction(0x9), new TxIoPinSerialClockFunction(TxIoListener.NUM_SERIAL_IF + 0), new TxIoPinSerialRxFunction(TxIoListener.NUM_SERIAL_IF + 0), new TxIoPinSerialTxFunction(TxIoListener.NUM_SERIAL_IF + 0)}));
        ioPorts[IoPort.PORT_9].setFunctions2(reverse(new PinFunction[]{null, new TxIoPinSerialCtsFunction(2), null, null, null, new TxIoPinSerialCtsFunction(TxIoListener.NUM_SERIAL_IF + 0), null, null}));

        // Port A
        ioPorts[IoPort.PORT_A].setFunctions1(reverse(new PinFunction[]{new TxIoPinPhaseCounterInputFunction(2, /*spec says 0, which is a duplicate of the next 1. Assuming 1*/ 1), new TxIoPinPhaseCounterInputFunction(2, 0), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT5), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT4), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT3), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT2), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT1), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT0)}));
        ioPorts[IoPort.PORT_A].setFunctions2(reverse(new PinFunction[]{null, null, new TxIoPinTimerInputFunction(programmableTimers[0x6], 1), new TxIoPinTimerInputFunction(programmableTimers[0x6], 0), new TxIoPinPhaseCounterInputFunction(1, 1), new TxIoPinPhaseCounterInputFunction(1, 0), new TxIoPinPhaseCounterInputFunction(0, 1), new TxIoPinPhaseCounterInputFunction(0, 0)}));

        // Port B
        ioPorts[IoPort.PORT_B].setFunctions1(reverse(new PinFunction[]{new TxIoPinTimerOutputFunction(0x8), new TxIoPinSerialClockFunction(TxIoListener.NUM_SERIAL_IF + 0), new TxIoPinSerialRxFunction(TxIoListener.NUM_SERIAL_IF + 1), new TxIoPinSerialTxFunction(TxIoListener.NUM_SERIAL_IF + 1), new TxIoPinTimerOutputFunction(0x7), new TxIoPinTimerOutputFunction(0x6), new TxIoPinPhaseCounterInputFunction(3, 1), new TxIoPinPhaseCounterInputFunction(3, 0)}));
        ioPorts[IoPort.PORT_B].setFunctions2(reverse(new PinFunction[]{null, new TxIoPinSerialCtsFunction(TxIoListener.NUM_SERIAL_IF + 0), null, null, null, null, null, null}));

        // Port C
        ioPorts[IoPort.PORT_C].setFunctions1(reverse(new PinFunction[]{new TxIoPinCaptureOutputFunction(3), new TxIoPinSbiClockFunction(), new TxIoPinSbiInFunction(), new TxIoPinSbiOutFunction(), new TxIoPinCaptureOutputFunction(2), new TxIoPinCaptureOutputFunction(1), new TxIoPinCaptureOutputFunction(0), new TxIoPinTimerInputFunction(programmableTimers[0xC])}));
        ioPorts[IoPort.PORT_C].setFunctions2(reverse(new PinFunction[]{null, null, null, null, null, null, null, new TxIoPinKeyFunction(30)}));

        // Port D
        ioPorts[IoPort.PORT_D].setFunctions1(reverse(new PinFunction[]{new TxIoPinADTriggerFunction('B'), new TxIoPinKeyFunction(31), new TxIoPinTimerOutputFunction(0xD), new TxIoPinTimerOutputFunction(0xC), new TxIoPinTimerOutputFunction(0xB), new TxIoPinSerialClockFunction(TxIoListener.NUM_SERIAL_IF + 2), new TxIoPinSerialRxFunction(TxIoListener.NUM_SERIAL_IF + 2), new TxIoPinSerialTxFunction(TxIoListener.NUM_SERIAL_IF + 2)}));
        ioPorts[IoPort.PORT_D].setFunctions2(reverse(new PinFunction[]{null, new TxIoPinADTriggerFunction('A'), null, null, null, new TxIoPinSerialCtsFunction(TxIoListener.NUM_SERIAL_IF + 2), null, null}));

        // Port E
        ioPorts[IoPort.PORT_E].setFunctions1(reverse(new PinFunction[]{new TxIoPinKeyFunction(15), new TxIoPinKeyFunction(14), new TxIoPinKeyFunction(13), new TxIoPinKeyFunction(12), new TxIoPinKeyFunction(11), new TxIoPinKeyFunction(10), new TxIoPinKeyFunction(9), new TxIoPinKeyFunction(8)}));

        // Port F
        ioPorts[IoPort.PORT_F].setFunctions1(reverse(new PinFunction[]{new TxIoPinKeyFunction(23), new TxIoPinKeyFunction(22), new TxIoPinKeyFunction(21), new TxIoPinKeyFunction(20), new TxIoPinKeyFunction(19), new TxIoPinKeyFunction(18), new TxIoPinKeyFunction(17), new TxIoPinKeyFunction(16)}));
        ioPorts[IoPort.PORT_F].setFunctions2(reverse(new PinFunction[]{new TxIoPinCaptureOutputFunction(7), new TxIoPinCaptureOutputFunction(6), new TxIoPinCaptureOutputFunction(5), new TxIoPinCaptureOutputFunction(4), new TxIoPinDmaAckFunction(4), new TxIoPinDmaReqFunction(4), new TxIoPinDmaAckFunction(0), new TxIoPinDmaReqFunction(0)}));

        // Port G
        ioPorts[IoPort.PORT_G].setFunctions1(reverse(new PinFunction[]{new TxIoPinKeyFunction(7), new TxIoPinKeyFunction(6), new TxIoPinKeyFunction(5), new TxIoPinKeyFunction(4), new TxIoPinKeyFunction(3), new TxIoPinKeyFunction(2), new TxIoPinKeyFunction(1), new TxIoPinKeyFunction(0)}));

        // Port H
        ioPorts[IoPort.PORT_H].setFunctions1(reverse(new PinFunction[]{new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT1F), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT1E), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT1D), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT1C), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT1B), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT1A), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT19), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT18)}));
        ioPorts[IoPort.PORT_H].setFunctions2(reverse(new PinFunction[]{new TxIoPinTimerInputFunction(programmableTimers[0xD], 1), new TxIoPinTimerInputFunction(programmableTimers[0xD], 0), new TxIoPinTimerInputFunction(programmableTimers[0xB], 1), new TxIoPinTimerInputFunction(programmableTimers[0xB], 0), new TxIoPinTimerInputFunction(programmableTimers[0xA], 1), new TxIoPinTimerInputFunction(programmableTimers[0xA], 0), new TxIoPinTimerInputFunction(programmableTimers[0x9], 1), new TxIoPinTimerInputFunction(programmableTimers[0x9], 0)}));

        // Port I
        ioPorts[IoPort.PORT_I].setFunctions1(reverse(new PinFunction[]{new TxIoPinADTriggerSyncFunction(), new TxIoPinTimerOutputFunction(0x11), new TxIoPinTimerOutputFunction(10), new TxIoPinADTriggerFunction('C'), new TxIoPinPhaseCounterInputFunction(5, 1), new TxIoPinPhaseCounterInputFunction(5, 0), new TxIoPinPhaseCounterInputFunction(4, 1), new TxIoPinPhaseCounterInputFunction(4, 0)}));

        // Port J
        ioPorts[IoPort.PORT_J].setFunctions1(reverse(new PinFunction[]{new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT7), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT6), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT17), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT16), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT15), new TxIoPinInterruptFunction(interruptController, TxInterruptController.INT14), new TxIoPinTimerInputFunction(programmableTimers[0x11], 1), new TxIoPinTimerInputFunction(programmableTimers[0x11], 0)}));

        return ioPorts;
    }

    private static PinFunction[] reverse(PinFunction[] bit7to0) {
        if (bit7to0.length != 8) {
            throw new RuntimeException("Configuration error: there should be 8 values in " + Arrays.toString(bit7to0));
        }
        PinFunction[] bit0to7 = new PinFunction[8];
        for (int i = 0; i < 8; i++) {
            bit0to7[i] = bit7to0[7-i];
        }
        return bit0to7;
    }

}
