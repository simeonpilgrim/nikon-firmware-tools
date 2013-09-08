package com.nikonhacker.emu;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.Prefs;
import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.CodeStructure;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.fr.Expeed4006IoListener;
import com.nikonhacker.emu.memory.listener.fr.Expeed6B00IoListener;
import com.nikonhacker.emu.memory.listener.fr.ExpeedIoListener;
import com.nikonhacker.emu.memory.listener.fr.ExpeedPinIoListener;
import com.nikonhacker.emu.memory.listener.tx.TxIoListener;
import com.nikonhacker.emu.peripherials.adConverter.AdConverter;
import com.nikonhacker.emu.peripherials.adConverter.AdValueProvider;
import com.nikonhacker.emu.peripherials.adConverter.tx.TxAdConverter;
import com.nikonhacker.emu.peripherials.adConverter.tx.TxAdPrefsValueProvider;
import com.nikonhacker.emu.peripherials.clock.ClockGenerator;
import com.nikonhacker.emu.peripherials.clock.fr.FrClockGenerator;
import com.nikonhacker.emu.peripherials.clock.tx.TxClockGenerator;
import com.nikonhacker.emu.peripherials.dmaController.DmaController;
import com.nikonhacker.emu.peripherials.dmaController.tx.TxDmaController;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;
import com.nikonhacker.emu.peripherials.interruptController.SharedInterruptCircuit;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrInterruptController;
import com.nikonhacker.emu.peripherials.interruptController.fr.FrSharedInterruptCircuit;
import com.nikonhacker.emu.peripherials.interruptController.tx.TxInterruptController;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.Pin;
import com.nikonhacker.emu.peripherials.ioPort.fr.FrIoPort;
import com.nikonhacker.emu.peripherials.ioPort.tx.TxIoPort;
import com.nikonhacker.emu.peripherials.ioPort.util.FixedSourceComponent;
import com.nikonhacker.emu.peripherials.keyCircuit.KeyCircuit;
import com.nikonhacker.emu.peripherials.keyCircuit.tx.TxKeyCircuit;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.fr.FrReloadTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.fr.FrReloadTimer32;
import com.nikonhacker.emu.peripherials.programmableTimer.tx.TxInputCaptureTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.tx.TxTimer;
import com.nikonhacker.emu.peripherials.realtimeClock.RealtimeClock;
import com.nikonhacker.emu.peripherials.realtimeClock.tx.TxRealtimeClock;
import com.nikonhacker.emu.peripherials.serialInterface.AbstractSerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.eeprom.St95040;
import com.nikonhacker.emu.peripherials.serialInterface.eeprom.St950x0;
import com.nikonhacker.emu.peripherials.serialInterface.flashCharger.Nhhs2;
import com.nikonhacker.emu.peripherials.serialInterface.fr.FrSerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.lcd.LcdDriver;
import com.nikonhacker.emu.peripherials.serialInterface.tx.TxHSerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.tx.TxSerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.util.SpiBus;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.AlwaysBreakCondition;
import com.nikonhacker.emu.trigger.condition.AndCondition;
import com.nikonhacker.emu.trigger.condition.BreakPointCondition;
import com.nikonhacker.emu.trigger.condition.MemoryValueBreakCondition;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmulationFramework {

    private static final int BASE_ADDRESS_FUNCTION_CALL[] = {0xFFFFFFF0, 0x10001000};


    /** Type of run */
    public static enum ExecutionMode {
        /** Run without any break */
        RUN("Run"),

        /** Run without all break triggers enabled */
        DEBUG("Debug"),

        /** Just execute one instruction, then break, */
        STEP("Step");
        private String label;

        ExecutionMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }


    private Prefs prefs;

    private final MasterClock masterClock = new MasterClock();
    private       Emulator[]  emulator    = new Emulator[2];
    private       Platform[]  platform    = new Platform[2];
    private St950x0 eeprom;

    private boolean[] isImageLoaded     = {false, false};
    private boolean[] isEmulatorPlaying = {false, false};

    private CodeStructure[] codeStructure = new CodeStructure[2];

    public EmulationFramework(Prefs prefs) {
        this.prefs = prefs;
    }

    public MasterClock getMasterClock() {
        return masterClock;
    }

    public Emulator getEmulator(int chip) {
        return emulator[chip];
    }

    public Platform getPlatform(int chip) {
        return platform[chip];
    }

    public boolean isImageLoaded(int chip) {
        return isImageLoaded[chip];
    }

    public boolean isEmulatorPlaying(int chip) {
        return isEmulatorPlaying[chip];
    }

    public CodeStructure getCodeStructure(int chip) {
        return codeStructure[chip];
    }

    public void setCodeStructure(int chip, CodeStructure codeStructure) {
        this.codeStructure[chip] = codeStructure;
    }


    public boolean isEmulatorReady(int chip) {
        return isImageLoaded[chip] && !isEmulatorPlaying[chip];
    }


    public void initialize(final int chip, File imageFile, final ClockableCallbackHandler callbackHandler) {

        //System.err.println("Loading image for " + Constants.CHIP_LABEL[chip]);
        try {

            // 1. CLEANUP

            // Remove old emulator from the list of clockable devices
            masterClock.remove(emulator[chip]);

            // Scratch any analysis that was previously done
            codeStructure[chip] = null;

            // 2. CREATE NEW

            // TODO We should not create a new platform, just reset it
            // TODO Otherwise, the cross-linkings risks memory leaks
            platform[chip] = new Platform(masterClock);

            // Create a brand new emulator
            emulator[chip] = (chip == Constants.CHIP_FR)?(new FrEmulator(platform[chip])):(new TxEmulator(platform[chip]));

            // Prepare all devices
            CPUState cpuState;
            DebuggableMemory memory = new DebuggableMemory(prefs.isLogMemoryMessages(chip));
            ProgrammableTimer[] programmableTimers;
            IoPort[] ioPorts;
            SerialInterface[] serialInterfaces;
            List<SerialDevice> serialDevices = new ArrayList<SerialDevice>();
            ClockGenerator clockGenerator;
            InterruptController interruptController;
            DmaController dmaController = null;
            RealtimeClock realtimeClock = null;
            KeyCircuit keyCircuit = null;
            SharedInterruptCircuit sharedInterruptCircuit = null;
            AdConverter adConverter = null;

            if (chip == Constants.CHIP_FR) {
                cpuState = new FrCPUState();

                // Initializing only the platform's cpuState here is ugly, but is required
                // so that timers can hook to the cpu passed via the platform (at least on TX)...
                platform[chip].setCpuState(cpuState);

                programmableTimers = new ProgrammableTimer[ExpeedIoListener.NUM_TIMER + ExpeedIoListener.NUM_TIMER32];
                serialInterfaces = new SerialInterface[ExpeedIoListener.NUM_SERIAL_IF];
                clockGenerator = new FrClockGenerator();
                interruptController = new FrInterruptController(platform[chip]);
                sharedInterruptCircuit = new FrSharedInterruptCircuit(interruptController);

                // Standard FR registers
                memory.addActivityListener(new ExpeedIoListener(platform[chip], prefs.isLogRegisterMessages(chip)));
                // Unknown component 4006
                memory.addActivityListener(new Expeed4006IoListener(platform[chip], prefs.isLogRegisterMessages(chip)));
                // Specific Pin I/O register
                memory.addActivityListener(new ExpeedPinIoListener(platform[chip], prefs.isLogRegisterMessages(chip)));
                // 6B0000XX interrupt sharing macro in ASIC
                memory.addActivityListener(new Expeed6B00IoListener(platform[chip], prefs.isLogRegisterMessages(chip)));

                // Programmable timers
                for (int i = 0; i < ExpeedIoListener.NUM_TIMER; i++) {
                    programmableTimers[i] = new FrReloadTimer(i, platform[chip]);
                }
                for (int i = 0; i < ExpeedIoListener.NUM_TIMER32; i++) {
                    programmableTimers[ExpeedIoListener.NUM_TIMER + i] = new FrReloadTimer32(i, platform[chip]);
                }

                // I/O ports
                ioPorts = FrIoPort.setupPorts(interruptController, prefs.isLogPinMessages(chip));

                // Serial interfaces
                for (int i = 0; i < serialInterfaces.length; i++) {
                    serialInterfaces[i] = new FrSerialInterface(i, platform[chip], prefs.isLogSerialMessages(chip));
                }
            }
            else {
                cpuState = new TxCPUState();

                // Initializing only the platform's cpuState here is ugly, but is required
                // so that timers can hook to the cpu passed via the platform...
                platform[chip].setCpuState(cpuState);

                programmableTimers = new ProgrammableTimer[TxIoListener.NUM_16B_TIMER + TxIoListener.NUM_32B_TIMER];
                serialInterfaces = new SerialInterface[TxIoListener.NUM_SERIAL_IF + TxIoListener.NUM_HSERIAL_IF];
                clockGenerator = new TxClockGenerator();
                interruptController = new TxInterruptController(platform[chip]);

                memory.addActivityListener(new TxIoListener(platform[chip], prefs.isLogRegisterMessages(chip)));

                // Programmable timers
                // First put all 16-bit timers
                for (int i = 0; i < TxIoListener.NUM_16B_TIMER; i++) {
                    programmableTimers[i] = new TxTimer(i, platform[chip]);
                }
                // Then add the 32-bit input capture timer
                programmableTimers[TxIoListener.NUM_16B_TIMER] = new TxInputCaptureTimer(platform[chip]);

                // I/O ports
                ioPorts = TxIoPort.setupPorts(platform[chip], interruptController, programmableTimers, prefs.isLogPinMessages(chip));

                // Serial interfaces
                // Standard
                for (int i = 0; i < TxIoListener.NUM_SERIAL_IF; i++) {
                    serialInterfaces[i] = new TxSerialInterface(i, platform[chip], prefs.isLogSerialMessages(chip), prefs);
                }
                // Hi-speed
                for (int i = 0; i < TxIoListener.NUM_HSERIAL_IF; i++) {
                    serialInterfaces[TxIoListener.NUM_SERIAL_IF + i] = new TxHSerialInterface(i, platform[chip], prefs.isLogSerialMessages(chip), prefs);
                }

                ((TxCPUState) cpuState).setInterruptController((TxInterruptController) interruptController);

                dmaController = new TxDmaController(platform[chip], prefs);
                realtimeClock = new TxRealtimeClock(platform[chip]);
                keyCircuit = new TxKeyCircuit(interruptController);

                // Devices to be linked to the Tx chip

                // Eeprom
                eeprom = new St95040("Eeprom");
                switch(prefs.getEepromInitMode()) {
                    case BLANK:
                        eeprom.clear();
                        break;
                    case PERSISTENT:
                        byte[] lastEepromContents = prefs.getLastEepromContents();
                        if (lastEepromContents != null) {
                            eeprom.loadArray(lastEepromContents);
                        }
                        else {
                            System.err.println("Attempt at loading previous eeprom values failed. No stored values...");
                            eeprom.clear();
                        }
                        break;
                    case LAST_LOADED:
                        String lastEepromFileName = prefs.getLastEepromFileName();
                        if (StringUtils.isNotBlank(lastEepromFileName)) {
                            try {
                                eeprom.loadBinary(new File(lastEepromFileName));
                            } catch (IOException e) {
                                System.err.println("Error reloading last eeprom contents from file '" + lastEepromFileName + "': " + e.getMessage());
                                eeprom.clear();
                            }
                        }
                        else {
                            System.err.println("Attempt at reloading last eeprom contents from file failed. Seems no eeprom was ever loaded...");
                            eeprom.clear();
                        }
                        break;
                }

                // Viewfinder LCD driver
                LcdDriver lcdDriver = new LcdDriver("ViewFinder LCD");

                // Flash charger
                Nhhs2 nhhs2 = new Nhhs2("Flash charger");

                // Store devices
                serialDevices.add(eeprom);
                serialDevices.add(lcdDriver);
                serialDevices.add(nhhs2);

                // Perform connection
                connectTxHsc2SerialDevices(serialInterfaces[TxIoListener.NUM_SERIAL_IF + 2], eeprom, lcdDriver, ioPorts);
                connectTxHsc1SerialDevice(serialInterfaces[TxIoListener.NUM_SERIAL_IF + 1], nhhs2);

                AdValueProvider provider = new TxAdPrefsValueProvider(prefs, Constants.CHIP_TX);
                adConverter = new TxAdConverter(emulator[Constants.CHIP_TX], (TxInterruptController) interruptController, provider);
            }

            // Set up input port overrides according to prefs
            for (int portNumber = 0; portNumber < ioPorts.length; portNumber++) {
                for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
                    Integer override = prefs.getPortInputValueOverride(chip, portNumber, bitNumber);
                    if (override != null) {
                        Pin pin = ioPorts[portNumber].getPin(bitNumber);
                        // Insert fixed source, if requested
                        switch (override) {
                            case 0:
                                // GND
                                new FixedSourceComponent(0, "Fixed " + Constants.LABEL_LO + " for " + pin.getName(), prefs.isLogPinMessages(chip)).insertAtPin(pin);
                                break;
                            case 1:
                                // VCC
                                new FixedSourceComponent(1, "Fixed " + Constants.LABEL_HI + " for " + pin.getName(), prefs.isLogPinMessages(chip)).insertAtPin(pin);
                                break;
                            default:
                                new FixedSourceComponent(override, "Fixed " + override + " for " + pin.getName(), prefs.isLogPinMessages(chip)).insertAtPin(pin);
                                break;
                        }
                    }
                }
            }


            platform[chip].setMemory(memory);
            platform[chip].setClockGenerator(clockGenerator);
            platform[chip].setInterruptController(interruptController);
            platform[chip].setProgrammableTimers(programmableTimers);
            platform[chip].setIoPorts(ioPorts);
            platform[chip].setSerialInterfaces(serialInterfaces);
            platform[chip].setDmaController(dmaController);
            platform[chip].setRealtimeClock(realtimeClock);
            platform[chip].setKeyCircuit(keyCircuit);
            platform[chip].setAdConverter(adConverter);
            platform[chip].setSerialDevices(serialDevices);
            platform[chip].setSharedInterruptCircuit(sharedInterruptCircuit);

            clockGenerator.setPlatform(platform[chip]);

            // TODO is it the right way to create a context here ?
            // TODO passing cpu, memory and interrupt controller a second time although they're in the platform
            // TODO sounds weird...
            emulator[chip].setContext(memory, cpuState, interruptController);
            emulator[chip].clearCycleCounterListeners();

            // TODO: let user choose whether he wants to load at reset address or refer to a dfr/dtx file's "-i" option
            // That would allow to load "relocatable" areas at their right place.
            // e.g. TX code @0xBFC0A000-0xBFC0ED69 is copied to RAM 0xFFFF4000-0xFFFF8D69 by code 0xBFC1C742-0xBFC1C76A
            memory.loadFile(imageFile, cpuState.getResetAddress(), prefs.isFirmwareWriteProtected(chip));
            isImageLoaded[chip] = true;

            cpuState.reset();

            if (isImageLoaded[Constants.CHIP_FR] && isImageLoaded[Constants.CHIP_TX]) {
                // Two CPUs are ready.
                // Perform serial interconnection
                interconnectChipSerialPorts(platform[Constants.CHIP_FR].getSerialInterfaces(), platform[Constants.CHIP_TX].getSerialInterfaces());
                // Perform serial interconnection
                interconnectChipIoPorts(platform[Constants.CHIP_FR].getIoPorts(), platform[Constants.CHIP_TX].getIoPorts());
            }

            // 3. RESTORE

            // Finally, add the emulator to the list of clockable devices, in disabled state
            // We pass here an anonymous Callback Handler that does what is required here, then called the
            // external callback handler
            masterClock.add(emulator[chip], new ClockableCallbackHandler() {
                @Override
                public void onNormalExit(Object o) {
                    try {
                        isEmulatorPlaying[chip] = false;
                        emulator[chip].clearBreakConditions();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (callbackHandler != null) callbackHandler.onNormalExit(o);
                }

                @Override
                public void onException(Exception e) {
                    isEmulatorPlaying[chip] = false;
                    emulator[chip].clearBreakConditions();
                    e.printStackTrace();
                    if (callbackHandler != null) callbackHandler.onException(e);
                }
            }, false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connect Tx serial interface HSC1 with the flash charge driver
     *
     * @param serialInterface
     * @param nhhs2
     */
    private void connectTxHsc1SerialDevice(SerialInterface serialInterface, Nhhs2 nhhs2) {
        AbstractSerialDevice.interConnectSerialDevices(serialInterface, nhhs2);
    }

    /**
     * Connect Tx serial interface HSC2 with the eeprom and the lcd driver via a SPI bus
     *
     * @param serialInterface
     * @param eeprom
     * @param lcdDriver
     * @param txIoPorts
     */
    private void connectTxHsc2SerialDevices(SerialInterface serialInterface, St950x0 eeprom, LcdDriver lcdDriver, IoPort[] txIoPorts) {
        // Create a bus with the CPU as master
        SpiBus bus = new SpiBus("bus", serialInterface) ; // Master

        // Connect slaves
        bus.addSlaveDevice(eeprom); // Slave 1
        bus.addSlaveDevice(lcdDriver); // Slave 2
        bus.connect();

        // Connect CPU pins with eeprom and lcd driver ~SELECT pins
        Pin.interconnect(txIoPorts[IoPort.PORT_4].getPin(6), eeprom.getSelectPin());
        Pin.interconnect(txIoPorts[IoPort.PORT_E].getPin(6), lcdDriver.getSelectPin());
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    private void interconnectChipSerialPorts(SerialInterface[] frSerialInterfaces, SerialInterface[] txSerialInterfaces) {
        // Reconnect Fr Serial channel 5 with Tx serial interface HSC0
        SerialInterface frSerialInterface5 = frSerialInterfaces[5];
        SerialInterface txSerialInterfaceH0 = txSerialInterfaces[TxIoListener.NUM_SERIAL_IF + 0];
        frSerialInterface5.connectTargetDevice(txSerialInterfaceH0);
        txSerialInterfaceH0.connectTargetDevice(frSerialInterface5);
    }

    private void interconnectChipIoPorts(IoPort[] frIoPorts, IoPort[] txIoPorts) {
        // FR 0x50000100.bit5 => TX P53 (INTF), triggered (low) by FR at 001A8CBE and 001A8E24 and set back hi at 001A8E58
        Pin.interconnect(frIoPorts[IoPort.PORT_0].getPin(5), txIoPorts[IoPort.PORT_5].getPin(3));

        // TX PC3 => FR 0x50000107.bit6 (INT16) , tested by FR at 001A885C, 001A8896 (init) and 001A8976 (send header)
        Pin.interconnect(frIoPorts[IoPort.PORT_7].getPin(6), txIoPorts[IoPort.PORT_C].getPin(3));

        // Pin.interconnect(main power button, txIoPorts[IoPort.PORT_A].getPin(0));
    }



    /**
     * Prepares breakpoints for the given run mode on the given chip
     * @param chip
     * @param executionMode mode to run in
     * @param endAddress if not null, stop when reaching this address
     */
    public void prepareBreakTriggers(int chip, ExecutionMode executionMode, Integer endAddress) {
        //System.err.println("Play request for " + Constants.CHIP_LABEL[chip]);
        if (!isImageLoaded[chip]) {
            throw new RuntimeException("No Image loaded !");
        }

        if (executionMode == ExecutionMode.STEP) {
            emulator[chip].addBreakCondition(new AlwaysBreakCondition());
        }
        else {
            if (executionMode == ExecutionMode.DEBUG) {
                for (BreakTrigger breakTrigger : prefs.getTriggers(chip)) {
                    if (breakTrigger.isActive()) {
                        // Arm memory change detection triggers
                        for (MemoryValueBreakCondition memoryValueBreakCondition : breakTrigger.getMemoryValueBreakConditions()) {
                            if (memoryValueBreakCondition.isChangeDetection()) {
                                memoryValueBreakCondition.setValue(platform[chip].getMemory().load32(memoryValueBreakCondition.getAddress()));
                                memoryValueBreakCondition.setNegate(true);
                            }
                        }
                        emulator[chip].addBreakCondition(new AndCondition(breakTrigger.getBreakConditions(codeStructure[chip], platform[chip].getMemory()), breakTrigger));
                    }
                }
            }
            if (endAddress != null) {
                // Set a temporary break condition at given endAddress
                CPUState values = (chip==Constants.CHIP_FR)?new FrCPUState(endAddress):new TxCPUState(endAddress);
                CPUState flags = (chip==Constants.CHIP_FR)?new FrCPUState():new TxCPUState();
                flags.pc = 1;
                // TODO adapt this for Tx
                if (chip==Constants.CHIP_FR) {
                    ((FrCPUState)flags).setILM(0, false);
                    flags.setReg(FrCPUState.TBR, 0);
                }
                BreakTrigger breakTrigger = new BreakTrigger("Run to cursor at 0x" + Format.asHex(endAddress, 8), values, flags, new ArrayList<MemoryValueBreakCondition>());
                emulator[chip].addBreakCondition(new BreakPointCondition(endAddress, breakTrigger));
            }
        }
    }

    public void prepareEmulation(final int chip) {
        //System.err.println("Preparing emulation of " + Constants.CHIP_LABEL[chip]);
        isEmulatorPlaying[chip] = true;
        emulator[chip].setOutputOptions(prefs.getOutputOptions(chip));
        masterClock.setEnabled(emulator[chip], true);
        // TODO what's the use of this here ?
        platform[chip].getCpuState().setAllRegistersDefined();
    }

    public void playOneFunction(int chip, int address, boolean debugMode) {
        if (chip == Constants.CHIP_TX) {
            System.err.println("Not implemented for TX");
            // TODO : implement the equivalent for TX
        }
        else {
            // TODO : make the call transparent by cloning CPUState
            // To execute one function only, we put a fake CALL at a conventional place, followed by an infinite loop
            platform[chip].getMemory().store16(BASE_ADDRESS_FUNCTION_CALL[chip], 0x9f8c);      // LD          ,R12
            platform[chip].getMemory().store32(BASE_ADDRESS_FUNCTION_CALL[chip] + 2, address); //     address
            platform[chip].getMemory().store16(BASE_ADDRESS_FUNCTION_CALL[chip] + 6, 0x971c);  // CALL @R12
            platform[chip].getMemory().store16(BASE_ADDRESS_FUNCTION_CALL[chip] + 8, 0xe0ff);  // HALT, infinite loop

            // And we put a breakpoint on the instruction after the call
            emulator[chip].clearBreakConditions();
            emulator[chip].addBreakCondition(new BreakPointCondition(BASE_ADDRESS_FUNCTION_CALL[chip] + 8, null));

            platform[chip].getCpuState().pc = BASE_ADDRESS_FUNCTION_CALL[chip];

            if (debugMode) {
                for (BreakTrigger breakTrigger : prefs.getTriggers(chip)) {
                    if (breakTrigger.mustBreak() || breakTrigger.mustBeLogged()) {
                        emulator[chip].addBreakCondition(new AndCondition(breakTrigger.getBreakConditions(codeStructure[chip], platform[chip].getMemory()), breakTrigger));
                    }
                }
            }

            prepareEmulation(chip);
            masterClock.start();
        }
    }

    public void pauseEmulator(int chip) {
        emulator[chip].addBreakCondition(new AlwaysBreakCondition());
        emulator[chip].exitSleepLoop();
    }

    public void stopEmulator(int chip) {
        prefs.setLastEepromContents(eeprom.getMemory());
        emulator[chip].addBreakCondition(new AlwaysBreakCondition());
        emulator[chip].exitSleepLoop();
        try {
            // Wait for emulator to stop
            Thread.sleep(120);
        } catch (InterruptedException e) {
            // nop
        }
        if (prefs.isSyncPlay()) {
            masterClock.resetTotalElapsedTimePs();
        }
    }



    public void dispose() {
        if (eeprom != null) {
            prefs.setLastEepromContents(eeprom.getMemory());
        }
    }
}
