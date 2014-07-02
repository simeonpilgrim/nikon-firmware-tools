package com.nikonhacker.emu;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.Prefs;
import com.nikonhacker.XStreamUtils;
import com.nikonhacker.disassembly.*;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.tx.NullRegister32;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.memory.DebuggableMemory;
import com.nikonhacker.emu.memory.listener.fr.*;
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
import com.nikonhacker.emu.peripherials.frontPanel.CameraLed;
import com.nikonhacker.emu.peripherials.frontPanel.FrontPanel;
import com.nikonhacker.emu.peripherials.frontPanel.tx.D5100FrontPanel;
import com.nikonhacker.emu.peripherials.imageTransferCircuit.ImageTransferCircuit;
import com.nikonhacker.emu.peripherials.imageTransferCircuit.fr.FrImageTransferCircuit;
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
import com.nikonhacker.emu.peripherials.jpegCodec.JpegCodec;
import com.nikonhacker.emu.peripherials.jpegCodec.fr.FrJpegCodec;
import com.nikonhacker.emu.peripherials.keyCircuit.KeyCircuit;
import com.nikonhacker.emu.peripherials.keyCircuit.tx.TxKeyCircuit;
import com.nikonhacker.emu.peripherials.lcd.Lcd;
import com.nikonhacker.emu.peripherials.lcd.fr.FrLcd;
import com.nikonhacker.emu.peripherials.mirrorBox.MirrorBox;
import com.nikonhacker.emu.peripherials.programmableTimer.ProgrammableTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.fr.FrReloadTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.fr.FrReloadTimer32;
import com.nikonhacker.emu.peripherials.programmableTimer.tx.TxInputCaptureTimer;
import com.nikonhacker.emu.peripherials.programmableTimer.tx.TxTimer;
import com.nikonhacker.emu.peripherials.realtimeClock.RealtimeClock;
import com.nikonhacker.emu.peripherials.realtimeClock.tx.TxRealtimeClock;
import com.nikonhacker.emu.peripherials.resolutionConverter.ResolutionConverter;
import com.nikonhacker.emu.peripherials.resolutionConverter.fr.FrResolutionConverter;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.eeprom.St95040;
import com.nikonhacker.emu.peripherials.serialInterface.eeprom.St950x0;
import com.nikonhacker.emu.peripherials.serialInterface.flashCharger.Nhhs2;
import com.nikonhacker.emu.peripherials.serialInterface.fMount.FMountCircuit;
import com.nikonhacker.emu.peripherials.serialInterface.fr.FrSerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.lcd.LcdDriver;
import com.nikonhacker.emu.peripherials.serialInterface.tx.TxHSerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.tx.TxSerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.util.SpiBus;
import com.nikonhacker.emu.peripherials.sdController.SdController;
import com.nikonhacker.emu.peripherials.sdController.fr.FrSdController;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.AlwaysBreakCondition;
import com.nikonhacker.emu.trigger.condition.AndCondition;
import com.nikonhacker.emu.trigger.condition.BreakPointCondition;
import com.nikonhacker.emu.trigger.condition.MemoryValueBreakCondition;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class EmulationFramework {

    private static final int BASE_ADDRESS_FUNCTION_CALL[] = {0xFFFFFFF0, 0x10001000};

    private static final String FRAMEWORK_ZIPENTRY_NAME = "Framework";
    private static final String MEMORY_ZIPENTRY_NAME    = "Memory";

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

    private CodeStructure[] codeStructure;

    public EmulationFramework(Prefs prefs) {
        this.prefs = prefs;
        initCodeStructure();
    }

    private void initCodeStructure() {
        codeStructure = new CodeStructure[2];
    }

    public void setPrefs(Prefs prefs) {
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


    public void initialize(final int chip, File imageFile) {

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
            JpegCodec[] jpegCodec = null;
            ResolutionConverter[] resolutionConverter = null;
            FrontPanel frontPanel = null;
            ImageTransferCircuit imageTransferCircuit = null;
            Lcd lcd = null;
            SdController[] sdController = null;
            MirrorBox mirrorBox = null;

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
                jpegCodec = new FrJpegCodec[Expeed40X3IoListener.NUM_JPEG_CODEC];
                resolutionConverter = new FrResolutionConverter[Expeed4002IoListener.NUM_RESOLUTION_CONVERTER];
                imageTransferCircuit = new FrImageTransferCircuit(platform[chip]);
                lcd = new FrLcd(platform[chip]);
                sdController = new FrSdController[Expeed6300IoListener.NUM_SD_CONTROLLER];

                setupMemoryListeners(chip, memory);

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

                for (int i = 0; i<jpegCodec.length; i++) {
                    jpegCodec[i] = new FrJpegCodec(i,platform[chip]);
                }

                for (int i = 0; i<resolutionConverter.length; i++) {
                    resolutionConverter[i] = new FrResolutionConverter(i,platform[chip]);
                }

                for (int i = 0; i<sdController.length; i++) {
                    sdController[i] = new FrSdController(i,platform[chip]);
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

                setupMemoryListeners(chip, memory);

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
                    serialInterfaces[i] = new TxSerialInterface(i, platform[chip], prefs.isLogSerialMessages(chip));
                }
                // Hi-speed
                for (int i = 0; i < TxIoListener.NUM_HSERIAL_IF; i++) {
                    serialInterfaces[TxIoListener.NUM_SERIAL_IF + i] = new TxHSerialInterface(i, platform[chip], prefs.isLogSerialMessages(chip));
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

                FMountCircuit fMountCircuit = new FMountCircuit("F-Mount");
                // Store devices
                serialDevices.add(eeprom);
                serialDevices.add(lcdDriver);
                serialDevices.add(nhhs2);
                serialDevices.add(fMountCircuit);

                mirrorBox = new MirrorBox(platform[chip]);

                // Perform connection
                connectTxHsc2SerialDevices(serialInterfaces[TxIoListener.NUM_SERIAL_IF + 2], eeprom, lcdDriver, ioPorts);
                connectTxHsc1SerialDevice(serialInterfaces[TxIoListener.NUM_SERIAL_IF + 1], nhhs2);

                connectTxSc1SerialDevice(serialInterfaces[1], fMountCircuit, ioPorts);

                AdValueProvider provider = new TxAdPrefsValueProvider(prefs, Constants.CHIP_TX);
                adConverter = new TxAdConverter(emulator[Constants.CHIP_TX], (TxInterruptController) interruptController, provider);

                frontPanel = new D5100FrontPanel(prefs);

                connectFrontPanel(frontPanel, ioPorts);

                connectMirrorBox(mirrorBox, ioPorts);
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
            platform[chip].setJpegCodec(jpegCodec);
            platform[chip].setResolutionConverter(resolutionConverter);
            platform[chip].setFrontPanel(frontPanel);
            platform[chip].setImageTransferCircuit(imageTransferCircuit);
            platform[chip].setLcd(lcd);
            platform[chip].setSdController(sdController);
            platform[chip].setMirrorBox(mirrorBox);

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
            // Store path in prefs
            // Make it relative if possible
            String workDir = new File(".").getCanonicalPath() + File.separator;
            String fileName = imageFile.getCanonicalPath();
            if (fileName.startsWith(workDir)) {
                fileName = fileName.substring(workDir.length());
            }

            prefs.setFirmwareFilename(chip, fileName);
            isImageLoaded[chip] = true;

            cpuState.reset();

            if (isImageLoaded[Constants.CHIP_FR] && isImageLoaded[Constants.CHIP_TX]) {
                // Two CPUs are ready.
                // Perform serial interconnection
                interconnectChipSerialPorts(platform[Constants.CHIP_FR].getSerialInterfaces(), platform[Constants.CHIP_TX].getSerialInterfaces());
                // Perform serial interconnection
                interconnectChipIoPorts(platform[Constants.CHIP_FR].getIoPorts(), platform[Constants.CHIP_TX].getIoPorts());
                // Perform LCD screen connection
                connectLcdScreenRelatedPins(platform[Constants.CHIP_FR].getIoPorts(), platform[Constants.CHIP_TX].getIoPorts(), (FrLcd) platform[Constants.CHIP_FR].getLcd());
                connectFrontPanelPins(platform[Constants.CHIP_FR].getIoPorts(), platform[Constants.CHIP_TX].getFrontPanel());
            }

            // 3. RESTORE

            // Finally, add the emulator to the list of clockable devices, in disabled state
            // We pass here an anonymous Callback Handler that does what is required here, then called the
            // external callback handler
            masterClock.add(emulator[chip], chip, false, true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setupCallbacks(final ClockableCallbackHandler callbackHandler0, final ClockableCallbackHandler callbackHandler1) {
        final ClockableCallbackHandler[] clockableCallbackHandlers = new ClockableCallbackHandler[2];

        clockableCallbackHandlers[0] = getCallbackHandler(0, callbackHandler0);
        clockableCallbackHandlers[1] = getCallbackHandler(1, callbackHandler1);
        masterClock.setupClockableCallbackHandlers(clockableCallbackHandlers);
    }

    private ClockableCallbackHandler getCallbackHandler(final int chip, final ClockableCallbackHandler callbackHandler) {
        return new ClockableCallbackHandler() {
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
            };
    }

    private final void setupMemoryListeners(int chip, DebuggableMemory memory) {
        if (chip==Constants.CHIP_FR) {
            // Standard FR registers
            memory.addActivityListener(new ExpeedIoListener(platform[chip], prefs.isLogRegisterMessages(chip)));
            // Unknown component 0x4006
            memory.addActivityListener(new Expeed4006IoListener(platform[chip], prefs.isLogRegisterMessages(chip)));
            // Specific Pin I/O register
            memory.addActivityListener(new ExpeedPinIoListener(platform[chip], prefs.isLogRegisterMessages(chip)));
            // 63000XXX and 64000XXX
            memory.addActivityListener(new Expeed6300IoListener(platform[chip], prefs.isLogRegisterMessages(chip)));
            // 6B0000XX interrupt sharing macro in ASIC
            memory.addActivityListener(new Expeed6B00IoListener(platform[chip], prefs.isLogRegisterMessages(chip)));
            // JPEG codec 0x40X3
            memory.addActivityListener(new Expeed40X3IoListener(platform[chip], prefs.isLogRegisterMessages(chip)));
            // Resolution converter 0x40XF and 0x4002
            memory.addActivityListener(new Expeed4002IoListener(platform[chip], prefs.isLogRegisterMessages(chip)));
            // Image Transfer 0x4018
            memory.addActivityListener(new Expeed4018IoListener(platform[chip], prefs.isLogRegisterMessages(chip)));

        } else if (chip==Constants.CHIP_TX) {
            memory.addActivityListener(new TxIoListener(platform[chip], prefs.isLogRegisterMessages(chip)));
        }
    }

    private void connectLcdScreenRelatedPins(IoPort[] frIoPorts, IoPort[] txIoPorts, FrLcd lcd) {
        Pin.interconnect(frIoPorts[IoPort.PORT_C].getPin(3), lcd.getPowerPin());
    }

    private void connectFrontPanelPins(IoPort[] frIoPorts, FrontPanel frontPanel) {
        Pin.interconnect(frIoPorts[IoPort.PORT_2].getPin(0), frontPanel.getLed(FrontPanel.KEY_CARDLED).getPin());
    }

    private void connectFrontPanel(FrontPanel frontPanel, IoPort[] txIoPorts) {
        // TODO: this is D5100 specific. Make it generic
        // Connect CPU pins with front panel

        //R9	P23/A19/A3/TB2IN1	"right"
        Pin.interconnect(txIoPorts[IoPort.PORT_2].getPin(3), frontPanel.getButton(FrontPanel.KEY_RIGHT).getPin(0));
        //U11	P40/CS0/KEY24	 "AE/AF" Lock
        Pin.interconnect(txIoPorts[IoPort.PORT_4].getPin(0), frontPanel.getButton(FrontPanel.KEY_AEL_AFL).getPin(0));
        //B13	PA0/INT0/PHC0IN0	Power "On"
        Pin.interconnect(txIoPorts[IoPort.PORT_A].getPin(0), frontPanel.getButton(FrontPanel.KEY_POWER).getPin(0));
        //B12	PA1/INT1/PHC0IN1	 "up"
        Pin.interconnect(txIoPorts[IoPort.PORT_A].getPin(1), frontPanel.getButton(FrontPanel.KEY_UP).getPin(0));
        //C12	PA2/INT2/PHC1IN0	 "down"
        Pin.interconnect(txIoPorts[IoPort.PORT_A].getPin(2), frontPanel.getButton(FrontPanel.KEY_DOWN).getPin(0));
        //D12	PA3/INT3/PHC1IN1	"left"
        Pin.interconnect(txIoPorts[IoPort.PORT_A].getPin(3), frontPanel.getButton(FrontPanel.KEY_LEFT).getPin(0));
        //M1	PC0/TBTIN/KEY30	 "i"
        Pin.interconnect(txIoPorts[IoPort.PORT_C].getPin(0), frontPanel.getButton(FrontPanel.KEY_I).getPin(0));
        //A8	PE1/KEY09	"+/-"
        Pin.interconnect(txIoPorts[IoPort.PORT_E].getPin(1), frontPanel.getButton(FrontPanel.KEY_PLUS_MINUS).getPin(0));
        //B8	PE2/KEY10	"Timer / Fn"
        Pin.interconnect(txIoPorts[IoPort.PORT_E].getPin(2), frontPanel.getButton(FrontPanel.KEY_TIMER).getPin(0));
        //C8	PE3/KEY11	"flash"
        Pin.interconnect(txIoPorts[IoPort.PORT_E].getPin(3), frontPanel.getButton(FrontPanel.KEY_FLASH).getPin(0));
        //E7	PF2/KEY18/DREQ4	"record"
        Pin.interconnect(txIoPorts[IoPort.PORT_F].getPin(2), frontPanel.getButton(FrontPanel.KEY_REC).getPin(0));
        //L14	PG0/KEY00	 "ok"
        Pin.interconnect(txIoPorts[IoPort.PORT_G].getPin(0), frontPanel.getButton(FrontPanel.KEY_OK).getPin(0));
        //K17	PG1/KEY01	"+"
        Pin.interconnect(txIoPorts[IoPort.PORT_G].getPin(1), frontPanel.getButton(FrontPanel.KEY_ZOOM_IN).getPin(0));
        //K16	PG2/KEY02	"-"
        Pin.interconnect(txIoPorts[IoPort.PORT_G].getPin(2), frontPanel.getButton(FrontPanel.KEY_ZOOM_OUT).getPin(0));
        //K15	PG3/KEY03	 "menu"
        Pin.interconnect(txIoPorts[IoPort.PORT_G].getPin(3), frontPanel.getButton(FrontPanel.KEY_MENU).getPin(0));
        //K14	PG4/KEY04	 "play"
        Pin.interconnect(txIoPorts[IoPort.PORT_G].getPin(4), frontPanel.getButton(FrontPanel.KEY_PLAY).getPin(0));
        //J16	PG5/KEY05	 LiveView
        Pin.interconnect(txIoPorts[IoPort.PORT_G].getPin(5), frontPanel.getButton(FrontPanel.KEY_LIVEVIEW).getPin(0));
        //J15	PG6/KEY06	 "info"
        Pin.interconnect(txIoPorts[IoPort.PORT_G].getPin(6), frontPanel.getButton(FrontPanel.KEY_INFO).getPin(0));
        //J14	PG7/KEY07	 "del"
        Pin.interconnect(txIoPorts[IoPort.PORT_G].getPin(7), frontPanel.getButton(FrontPanel.KEY_DELETE).getPin(0));

        // - P84 and PJ5 set to low = shutter full press
        // - PJ5 set to low and P84 set to high = shutter half press.
        Pin.interconnect(txIoPorts[IoPort.PORT_J].getPin(5), frontPanel.getButton(FrontPanel.KEY_SHUTTER).getPin(0)); // 1 - 0 - 0 - 0
        Pin.interconnect(txIoPorts[IoPort.PORT_8].getPin(4), frontPanel.getButton(FrontPanel.KEY_SHUTTER).getPin(1)); // 1 - 1 - 0 - 1

        // P57 and P56 might be scroll wheel
        // TODO: one "click" should step through 4 steps
        Pin.interconnect(txIoPorts[IoPort.PORT_5].getPin(6), frontPanel.getButton(FrontPanel.KEY_DIAL).getPin(0));
        Pin.interconnect(txIoPorts[IoPort.PORT_5].getPin(7), frontPanel.getButton(FrontPanel.KEY_DIAL).getPin(1));

        // ?? MODE DIAL
        // PKEY20,21,22,23 (PF4, PF5, PF6, PF7)
        Pin.interconnect(txIoPorts[IoPort.PORT_F].getPin(4), frontPanel.getButton(FrontPanel.KEY_MODEDIAL).getPin(0));
        Pin.interconnect(txIoPorts[IoPort.PORT_F].getPin(5), frontPanel.getButton(FrontPanel.KEY_MODEDIAL).getPin(1));
        Pin.interconnect(txIoPorts[IoPort.PORT_F].getPin(6), frontPanel.getButton(FrontPanel.KEY_MODEDIAL).getPin(2));
        Pin.interconnect(txIoPorts[IoPort.PORT_F].getPin(7), frontPanel.getButton(FrontPanel.KEY_MODEDIAL).getPin(3));

        // Flash Open sensor = PH3 : is the flash unit open ?
        // TODO

    }

    /**
     * Connect Tx serial interface HSC1 with the flash charge driver
     *
     * @param serialInterface
     * @param nhhs2
     */
    private void connectTxHsc1SerialDevice(SerialInterface serialInterface, Nhhs2 nhhs2) {
        SerialDevice.interConnectSerialDevices(serialInterface, nhhs2);
    }

    /**
     * Connect Tx serial interface SC1 with lens mount
     *
     * @param serialInterface
     * @param fMountCircuit
     */
    private void connectTxSc1SerialDevice(SerialInterface serialInterface, FMountCircuit fMountCircuit, IoPort[] txIoPorts) {
        SerialDevice.interConnectSerialDevices(serialInterface, fMountCircuit);

        // Connect CPU pins
        Pin.interconnect(txIoPorts[IoPort.PORT_I].getPin(3), fMountCircuit.getFromTXPin());
        Pin.interconnect(txIoPorts[IoPort.PORT_J].getPin(7), fMountCircuit.getToTXPin());
    }

    private void connectMirrorBox(MirrorBox mirrorBox, IoPort[] txIoPorts) {
        Pin.interconnect(txIoPorts[IoPort.PORT_A].getPin(5), mirrorBox.getWiperPin(0));
        Pin.interconnect(txIoPorts[IoPort.PORT_H].getPin(0), mirrorBox.getWiperPin(1));
        Pin.interconnect(txIoPorts[IoPort.PORT_H].getPin(1), mirrorBox.getWiperPin(2));

        Pin.interconnect(txIoPorts[IoPort.PORT_6].getPin(3), mirrorBox.getMovePin());
        Pin.interconnect(txIoPorts[IoPort.PORT_1].getPin(1), mirrorBox.getDirPin());
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

        // TODO TX ??? => FR 0x5000010B.bit3 (INT23) autofocus reached (for beep)
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
                BreakTrigger breakTrigger = new BreakTrigger(chip, "Run to cursor at 0x" + Format.asHex(endAddress, 8));
                breakTrigger.getCpuStateValues().setPc(endAddress);
                breakTrigger.getCpuStateFlags().pc = 1;
                emulator[chip].addBreakCondition(new BreakPointCondition(endAddress, breakTrigger));
            }
        }
    }

    public void prepareEmulation(final int chip) {
        //System.err.println("Preparing emulation of " + Constants.CHIP_LABEL[chip]);
        isEmulatorPlaying[chip] = true;
        emulator[chip].setOutputOptions(prefs.getOutputOptions(chip));
        masterClock.enableClockable(emulator[chip]);
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
        masterClock.setupClockableCallbackHandlers(null);
    }


    public static XStream getFrameworkXStream() {
        XStream xStream = XStreamUtils.getBaseXStream();

        // Don't store memory via XStream
        xStream.omitField(Platform.class, "memory");
        xStream.omitField(StatementContext.class, "memory");

        // Don't store disassembled code via XStream (Java heap overflow)
        xStream.omitField(EmulationFramework.class, "codeStructure");

        // Don't store prefs
        xStream.omitField(EmulationFramework.class, "prefs");
        xStream.omitField(TxDmaController.class, "prefs");
        xStream.omitField(TxAdPrefsValueProvider.class, "prefs");
        xStream.omitField(FrontPanel.class, "prefs");
        xStream.omitField(D5100FrontPanel.class, "prefs");

        // Don't store callback handler
        xStream.omitField(MasterClock.class, "clockableCallbackHandlers");
        xStream.omitField(CameraLed.class, "listener");
// instead of omit we close window before save
//        xStream.omitField(IoPort.class, "IoPortConfigListener");

        // Use some aliases
        xStream.alias("r32", Register32.class);
        xStream.alias("nr32", NullRegister32.class);
        xStream.alias("wlr32", WriteListenerRegister32.class);
        xStream.useAttributeFor(Register32.class, "value");
        xStream.aliasField("v", Register32.class, "value");
        xStream.aliasField("r", CPUState.class, "regValue");
        return xStream;
    }


    public static void saveStateToFile(EmulationFramework framework, String destinationFilename) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(new File(destinationFilename));
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));

        StringWriter writer = new StringWriter();
        getFrameworkXStream().toXML(framework, writer);

        byte[] bytes = writer.toString().getBytes("UTF-8");

        ZipEntry zipEntry = new ZipEntry(FRAMEWORK_ZIPENTRY_NAME);
        zipEntry.setSize(bytes.length);
        zipOutputStream.putNextEntry(zipEntry);
        IOUtils.write(bytes, zipOutputStream);

        for (int chip = 0; chip < 2; chip++) {
            DebuggableMemory memory = framework.getPlatform(chip).getMemory();
            zipEntry = new ZipEntry(MEMORY_ZIPENTRY_NAME + chip);
            zipEntry.setSize(memory.getNumPages() + memory.getNumUsedPages() * memory.getPageSize());
            zipOutputStream.putNextEntry(zipEntry);
            memory.saveAllToStream(zipOutputStream);
        }

        zipOutputStream.close();
        fileOutputStream.close();
    }


    public static EmulationFramework load(String sourceFilename, Prefs prefs) throws IOException {
        EmulationFramework framework = null;
        FileInputStream fileInputStream = null;
        ZipInputStream zipInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(sourceFilename));
            zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));

            // Read CPU State
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry == null || !FRAMEWORK_ZIPENTRY_NAME.equals(entry.getName())) {
                throw new IOException("Error loading state file\nFirst file not called " + FRAMEWORK_ZIPENTRY_NAME);
            }
            else {
                framework = (EmulationFramework) XStreamUtils.load(zipInputStream, getFrameworkXStream());

                // clean code structure
                framework.initCodeStructure();
                /* Relink prefs */
                framework.setPrefs(prefs);
                /* Restore and relink memory */
                for (int chip = 0; chip < 2; chip++) {
                    // Read memory
                    entry = zipInputStream.getNextEntry();
                    String expectedEntry = MEMORY_ZIPENTRY_NAME + chip;
                    if (entry == null || !expectedEntry.equals(entry.getName())) {
                        throw new IOException("Error loading state file\nExpected a file called " + expectedEntry + " but got " + entry.getName());
                    }
                    else {
                        // Restore memory to platform
                        final DebuggableMemory memory = new DebuggableMemory(prefs.isLogMemoryMessages(chip));
                        framework.getPlatform(chip).setMemory(memory);
                        memory.loadAllFromStream(zipInputStream);
                        // Also update its reference in framework
                        framework.getEmulator(chip).context.memory = memory;
                        framework.setupMemoryListeners(chip, memory);
                    }
                }

                ((TxDmaController)framework.getPlatform(Constants.CHIP_TX).getDmaController()).setPrefs(prefs);
                ((TxAdPrefsValueProvider)((TxAdConverter)framework.getPlatform(Constants.CHIP_TX).getAdConverter()).getProvider()).setPrefs(prefs);

                // We do not want that loaded front panel writes to Prefs, so do not set
            }
        } finally {
            if (zipInputStream != null) zipInputStream.close();
            if (fileInputStream != null) fileInputStream.close();
        }

        return framework;
    }

}
