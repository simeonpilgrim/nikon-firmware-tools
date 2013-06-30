package com.nikonhacker;


import com.nikonhacker.disassembly.CPUState;
import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.disassembly.Register32;
import com.nikonhacker.disassembly.WriteListenerRegister32;
import com.nikonhacker.disassembly.tx.NullRegister32;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.memoryHexEditor.MemoryWatch;
import com.thoughtworks.xstream.XStream;

import java.io.*;
import java.util.*;

public class Prefs {
    public enum EepromInitMode {
        BLANK, PERSISTENT, LAST_LOADED
    }

    private static final String KEY_WINDOW_MAIN = "MAIN";

    // Common
    private String buttonSize = EmulatorUI.BUTTON_SIZE_SMALL;
    private boolean closeAllWindowsOnStop = false;
    private HashMap<String, WindowPosition> windowPositionMap;
    private HashMap<String, WindowPosition> windowSizeMap;
    private int dividerLocation;
    private int lastDividerLocation;
    private boolean dividerKeepHidden;

    // Per chip
    private List<BreakTrigger>[] triggers;
    private List<MemoryWatch>[] memoryWatches;
    private EnumSet<OutputOption>[] outputOptions;
    private boolean[] autoUpdateITronObjectWindow;
    private boolean[] callStackHideJumps;
    private int[] sleepTick;
    private boolean[] writeDisassemblyToFile;
    private boolean[] sourceCodeFollowsPc;
    private String[] codeStructureGraphOrientation;
    private boolean[] firmwareWriteProtected;
    private boolean[] timersCycleSynchronous;
    private boolean[] dmaSynchronous;
    private boolean[] autoEnableTimers;
    private boolean[] logMemoryMessages;
    private boolean[] logSerialMessages;
    private boolean[] logPinMessages;
    private boolean[] logRegisterMessages;
    private boolean[] adValueFromList;
    private Map<String,List<Integer>>[] adValueListMap;
    private Map<String,Integer>[] adValueMap;
    private EepromInitMode eepromInitMode;
    private byte[] lastEepromContents;
    private String lastEepromFileName;
    private Map<String,Integer>[] ioValueOverrideMap;
    private boolean syncPlay = true;
    private int[] serialInterfaceFrameSelectedTab;
    private int[] genericSerialFrameSelectedTab;
    private int[] ioPortsFrameSelectedTab;


    private static File getPreferenceFile() {
        return new File(System.getProperty("user.home") + File.separator + "." + ApplicationInfo.getName());
    }

    public static void save(Prefs prefs) {
        try {
            XStreamUtils.save(prefs, new FileOutputStream(getPreferenceFile()), getPrefsXStream());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Prefs load() {
        File preferenceFile = getPreferenceFile();
        File lastKnownGoodFile = new File(preferenceFile.getAbsolutePath() + ".lastKnownGood");
        File corruptFile = new File(preferenceFile.getAbsolutePath() + ".corrupt");
        try {
            return loadFile(preferenceFile, lastKnownGoodFile);
        }
        catch (Exception e) {
            System.out.println("Could not load preferences file. Attempting a rename to " + corruptFile.getName() + " and trying to revert to " + lastKnownGoodFile.getName() + " instead...");
            e.printStackTrace();
            preferenceFile.renameTo(corruptFile);
            try {
                return loadFile(lastKnownGoodFile, preferenceFile);
            } catch (IOException e1) {
                System.out.println("Could not load " + lastKnownGoodFile.getName() + ". Starting with a blank preference file...");
            }
        }
        return new Prefs();
    }

    private static Prefs loadFile(File file, File backupTargetFile) throws IOException {
        if (file.exists()) {
            FileInputStream inputStream = new FileInputStream(file);
            XStream prefsXStream = getPrefsXStream();
            Prefs prefs = (Prefs) XStreamUtils.load(inputStream, prefsXStream);
            inputStream.close();
            if (backupTargetFile != null) {
                // Parsing was OK. Back-up config
                FileOutputStream outputStream = new FileOutputStream(backupTargetFile);
                XStreamUtils.save(prefs, outputStream, prefsXStream);
                outputStream.close();
            }
            return prefs;
        }
        else {
            return new Prefs();
        }
    }

    private static XStream getPrefsXStream() {
        XStream xStream = XStreamUtils.getBaseXStream();
        xStream.omitField(BreakTrigger.class, "function");
        xStream.alias("wpos", WindowPosition.class);
        xStream.alias("r32", Register32.class);
        xStream.alias("nr32", NullRegister32.class);
        xStream.alias("wlr32", WriteListenerRegister32.class);
        xStream.useAttributeFor(Register32.class, "value");
        xStream.aliasField("v", Register32.class, "value");
        xStream.aliasField("r", CPUState.class, "regValue");
        return xStream;
    }

    public String getButtonSize() {
        return buttonSize;
    }

    public void setButtonSize(String buttonSize) {
        this.buttonSize = buttonSize;
    }

    public boolean isCloseAllWindowsOnStop() {
        return closeAllWindowsOnStop;
    }

    public void setCloseAllWindowsOnStop(boolean closeAllWindowsOnStop) {
        this.closeAllWindowsOnStop = closeAllWindowsOnStop;
    }

    public boolean isWriteDisassemblyToFile(int chip) {
        if (writeDisassemblyToFile == null  || writeDisassemblyToFile.length != 2) writeDisassemblyToFile = new boolean[2];
        return writeDisassemblyToFile[chip];
    }

    public void setWriteDisassemblyToFile(int chip, boolean value) {
        if (writeDisassemblyToFile == null || writeDisassemblyToFile.length != 2) writeDisassemblyToFile = new boolean[2];
        this.writeDisassemblyToFile[chip] = value;
    }

    public boolean isSourceCodeFollowsPc(int chip) {
        if (sourceCodeFollowsPc == null || sourceCodeFollowsPc.length != 2) sourceCodeFollowsPc = new boolean[]{true, true};
        return sourceCodeFollowsPc[chip];
    }

    public void setSourceCodeFollowsPc(int chip, boolean value) {
        if (sourceCodeFollowsPc == null || sourceCodeFollowsPc.length != 2) sourceCodeFollowsPc = new boolean[]{true, true};
        this.sourceCodeFollowsPc[chip] = value;
    }

    public int getSleepTick(int chip) {
        if (sleepTick == null || sleepTick.length != 2) sleepTick = new int[2];
        return sleepTick[chip];
    }

    public void setSleepTick(int chip, int value) {
        if (sleepTick == null || sleepTick.length != 2) sleepTick = new int[2];
        this.sleepTick[chip] = value;
    }

    public Set<OutputOption> getOutputOptions(int chip) {
        if (outputOptions == null) outputOptions = new EnumSet[2];
        if (outputOptions[chip]==null) {
            // Prepare a new outputOptions containing only default values
            outputOptions[chip] = EnumSet.noneOf(OutputOption.class);
            for (OutputOption option : EnumSet.allOf(OutputOption.class)) {
                if (option.isDefaultValue()) {
                    outputOptions[chip].add(option);
                }
            }
        }
        return outputOptions[chip];
    }

    public void setOutputOption(int chip, OutputOption outputOption, boolean value) {
        getOutputOptions(chip);
        if (value) {
            outputOptions[chip].add(outputOption);
        }
        else {
            outputOptions[chip].remove(outputOption);
        }
    }

    public List<BreakTrigger> getTriggers(int chip) {
        if (triggers == null) triggers = new List[2];
        if (triggers[chip] == null) triggers[chip] = new ArrayList<BreakTrigger>();
        return triggers[chip];
    }

    public List<MemoryWatch> getWatches(int chip) {
        if (memoryWatches == null) memoryWatches = new List[2];
        if (memoryWatches[chip] == null) memoryWatches[chip] = new ArrayList<MemoryWatch>();
        return memoryWatches[chip];
    }

    private String getKey(String windowName, int chip) {
        return windowName + "_" + Constants.CHIP_LABEL[chip];
    }

    public int getWindowPositionX(String windowName, int chip) {
        if (windowPositionMap==null) windowPositionMap = new HashMap<String, WindowPosition>();
        WindowPosition windowPosition = windowPositionMap.get(getKey(windowName, chip));
        return (windowPosition==null)?0:windowPosition.getX();
    }

    public int getWindowPositionY(String windowName, int chip) {
        if (windowPositionMap==null) windowPositionMap = new HashMap<String, WindowPosition>();
        WindowPosition windowPosition = windowPositionMap.get(getKey(windowName, chip));
        return (windowPosition==null)?0:windowPosition.getY();
    }

    public void setWindowPosition(String windowName, int chip, int x, int y) {
        if (windowPositionMap==null) windowPositionMap = new HashMap<String, WindowPosition>();
        windowPositionMap.put(getKey(windowName, chip), new WindowPosition(x, y));
    }


    public int getMainWindowPositionX() {
        if (windowPositionMap==null) windowPositionMap = new HashMap<String, WindowPosition>();
        WindowPosition windowPosition = windowPositionMap.get(KEY_WINDOW_MAIN);
        return (windowPosition==null)?0:windowPosition.getX();
    }

    public int getMainWindowPositionY() {
        if (windowPositionMap==null) windowPositionMap = new HashMap<String, WindowPosition>();
        WindowPosition windowPosition = windowPositionMap.get(KEY_WINDOW_MAIN);
        return (windowPosition==null)?0:windowPosition.getY();
    }

    public void setMainWindowPosition(int x, int y) {
        if (windowPositionMap==null) windowPositionMap = new HashMap<String, WindowPosition>();
        windowPositionMap.put(KEY_WINDOW_MAIN, new WindowPosition(x, y));
    }


    public int getWindowSizeX(String windowName, int chip) {
        if (windowSizeMap==null) windowSizeMap = new HashMap<String, WindowPosition>();
        WindowPosition windowSize = windowSizeMap.get(getKey(windowName, chip));
        return (windowSize==null)?0:windowSize.getX();
    }

    public int getWindowSizeY(String windowName, int chip) {
        if (windowSizeMap==null) windowSizeMap = new HashMap<String, WindowPosition>();
        WindowPosition windowSize = windowSizeMap.get(getKey(windowName, chip));
        return (windowSize==null)?0:windowSize.getY();
    }

    public void setWindowSize(String windowName, int chip, int x, int y) {
        if (windowSizeMap==null) windowSizeMap = new HashMap<String, WindowPosition>();
        windowSizeMap.put(getKey(windowName, chip), new WindowPosition(x, y));
    }


    public int getMainWindowSizeX() {
        if (windowSizeMap==null) windowSizeMap = new HashMap<String, WindowPosition>();
        WindowPosition windowSize = windowSizeMap.get(KEY_WINDOW_MAIN);
        return (windowSize==null)?0:windowSize.getX();
    }

    public int getMainWindowSizeY() {
        if (windowSizeMap==null) windowSizeMap = new HashMap<String, WindowPosition>();
        WindowPosition windowSize = windowSizeMap.get(KEY_WINDOW_MAIN);
        return (windowSize==null)?0:windowSize.getY();
    }

    public void setMainWindowSize(int x, int y) {
        if (windowSizeMap==null) windowSizeMap = new HashMap<String, WindowPosition>();
        windowSizeMap.put(KEY_WINDOW_MAIN, new WindowPosition(x, y));
    }


    public String getCodeStructureGraphOrientation(int chip) {
        if (codeStructureGraphOrientation == null || codeStructureGraphOrientation.length != 2) codeStructureGraphOrientation = new String[2];
        return codeStructureGraphOrientation[chip];
    }

    public void setCodeStructureGraphOrientation(int chip, String value) {
        if (codeStructureGraphOrientation == null || codeStructureGraphOrientation.length != 2) codeStructureGraphOrientation = new String[2];
        this.codeStructureGraphOrientation[chip] = value;
    }

    public void setFirmwareWriteProtected(int chip, boolean isFirmwareWriteProtected) {
        if (firmwareWriteProtected == null || firmwareWriteProtected.length != 2) firmwareWriteProtected = new boolean[2];
        this.firmwareWriteProtected[chip] = isFirmwareWriteProtected;
    }

    public boolean isFirmwareWriteProtected(int chip) {
        if (firmwareWriteProtected == null || firmwareWriteProtected.length != 2) firmwareWriteProtected = new boolean[2];
        return firmwareWriteProtected[chip];
    }

    public void setAutoUpdateITronObjects(int chip, boolean autoUpdateITronObjects) {
        if (autoUpdateITronObjectWindow == null) autoUpdateITronObjectWindow = new boolean[2];
        this.autoUpdateITronObjectWindow[chip] = autoUpdateITronObjects;
    }

    public boolean isAutoUpdateITronObjects(int chip) {
        if (autoUpdateITronObjectWindow == null) autoUpdateITronObjectWindow = new boolean[2];
        return autoUpdateITronObjectWindow[chip];
    }

    public boolean isCallStackHideJumps(int chip) {
        if (callStackHideJumps == null) callStackHideJumps = new boolean[2];
        return callStackHideJumps[chip];
    }

    public void setCallStackHideJumps(int chip, boolean value) {
        if (callStackHideJumps == null) callStackHideJumps = new boolean[2];
        this.callStackHideJumps[chip] = value;
    }

    public int getDividerLocation() {
        return dividerLocation;
    }

    public void setDividerLocation(int dividerLocation) {
        this.dividerLocation = dividerLocation;
    }

    public int getLastDividerLocation() {
        return lastDividerLocation;
    }

    public void setLastDividerLocation(int lastDividerLocation) {
        this.lastDividerLocation = lastDividerLocation;
    }

    public boolean isDividerKeepHidden() {
        return dividerKeepHidden;
    }

    public void setDividerKeepHidden(boolean dividerKeepHidden) {
        this.dividerKeepHidden = dividerKeepHidden;
    }

    public boolean areTimersCycleSynchronous(int chip) {
        if (timersCycleSynchronous == null || timersCycleSynchronous.length != 2) timersCycleSynchronous = new boolean[]{true, true};
        return timersCycleSynchronous[chip];
    }

    public void setTimersCycleSynchronous(int chip, boolean areTimersCycleSynchronous) {
        if (timersCycleSynchronous == null || timersCycleSynchronous.length != 2) timersCycleSynchronous = new boolean[]{true, true};
        this.timersCycleSynchronous[chip] = areTimersCycleSynchronous;
    }

    public boolean isDmaSynchronous(int chip) {
        if (dmaSynchronous == null || dmaSynchronous.length != 2) dmaSynchronous = new boolean[]{true, true};
        return dmaSynchronous[chip];
    }

    public void setDmaSynchronous(int chip, boolean isDmaSynchronous) {
        if (dmaSynchronous == null || dmaSynchronous.length != 2) dmaSynchronous = new boolean[]{true, true};
        this.dmaSynchronous[chip] = isDmaSynchronous;
    }

    public boolean isAutoEnableTimers(int chip) {
        if (autoEnableTimers == null || autoEnableTimers.length != 2) autoEnableTimers = new boolean[]{true, true};
        return autoEnableTimers[chip];
    }

    public void setAutoEnableTimers(int chip, boolean isAutoEnableTimers) {
        if (autoEnableTimers == null || autoEnableTimers.length != 2) autoEnableTimers = new boolean[]{true, true};
        this.autoEnableTimers[chip] = isAutoEnableTimers;
    }

    public boolean isLogMemoryMessages(int chip) {
        if (logMemoryMessages == null || logMemoryMessages.length != 2) logMemoryMessages = new boolean[]{false, false};
        return logMemoryMessages[chip];
    }

    public void setLogMemoryMessages(int chip, boolean isLogMemoryMessages) {
        if (logMemoryMessages == null || logMemoryMessages.length != 2) logMemoryMessages = new boolean[]{false, false};
        this.logMemoryMessages[chip] = isLogMemoryMessages;
    }

    public boolean isLogSerialMessages(int chip) {
        if (logSerialMessages == null || logSerialMessages.length != 2) logSerialMessages = new boolean[]{false, false};
        return logSerialMessages[chip];
    }

    public void setLogSerialMessages(int chip, boolean isLogSerialMessages) {
        if (logSerialMessages == null || logSerialMessages.length != 2) logSerialMessages = new boolean[]{false, false};
        this.logSerialMessages[chip] = isLogSerialMessages;
    }

    public boolean isLogPinMessages(int chip) {
        if (logPinMessages == null || logPinMessages.length != 2) logPinMessages = new boolean[]{false, false};
        return logPinMessages[chip];
    }

    public void setLogPinMessages(int chip, boolean isLogPinMessages) {
        if (logPinMessages == null || logPinMessages.length != 2) logPinMessages = new boolean[]{true, true};
        this.logPinMessages[chip] = isLogPinMessages;
    }

    public boolean isLogRegisterMessages(int chip) {
        if (logRegisterMessages == null || logRegisterMessages.length != 2) logRegisterMessages = new boolean[]{true, true};
        return logRegisterMessages[chip];
    }

    public void setLogRegisterMessages(int chip, boolean isLogRegisterMessages) {
        if (logRegisterMessages == null || logRegisterMessages.length != 2) logRegisterMessages = new boolean[]{true, true};
        this.logRegisterMessages[chip] = isLogRegisterMessages;
    }

    public boolean isAdValueFromList(int chip) {
        if (adValueFromList == null || adValueFromList.length != 2) adValueFromList = new boolean[]{true, true};
        return adValueFromList[chip];
    }

    public void setAdValueFromList(int chip, boolean isAdValueFromList) {
        if (adValueFromList == null || adValueFromList.length != 2) adValueFromList = new boolean[]{true, true};
        this.adValueFromList[chip] = isAdValueFromList;
    }

    public int getSerialInterfaceFrameSelectedTab(int chip) {
        if (this.serialInterfaceFrameSelectedTab == null || this.serialInterfaceFrameSelectedTab.length != 2) this.serialInterfaceFrameSelectedTab = new int[]{0, 0};
        return serialInterfaceFrameSelectedTab[chip];
    }

    public void setSerialInterfaceFrameSelectedTab(int chip, int serialInterfaceFrameSelectedTab) {
        if (this.serialInterfaceFrameSelectedTab == null || this.serialInterfaceFrameSelectedTab.length != 2) this.serialInterfaceFrameSelectedTab = new int[]{0, 0};
        this.serialInterfaceFrameSelectedTab[chip] = serialInterfaceFrameSelectedTab;
    }

    public int getGenericSerialFrameSelectedTab(int chip) {
        if (this.genericSerialFrameSelectedTab == null || this.genericSerialFrameSelectedTab.length != 2) this.genericSerialFrameSelectedTab = new int[]{0, 0};
        return genericSerialFrameSelectedTab[chip];
    }

    public void setGenericSerialFrameSelectedTab(int chip, int genericSerialFrameSelectedTab) {
        if (this.genericSerialFrameSelectedTab == null || this.genericSerialFrameSelectedTab.length != 2) this.genericSerialFrameSelectedTab = new int[]{0, 0};
        this.genericSerialFrameSelectedTab[chip] = genericSerialFrameSelectedTab;
    }

    public int getIoPortsFrameSelectedTab(int chip) {
        if (this.ioPortsFrameSelectedTab == null || this.ioPortsFrameSelectedTab.length != 2) this.ioPortsFrameSelectedTab = new int[]{0, 0};
        return ioPortsFrameSelectedTab[chip];
    }

    public void setIoPortsFrameSelectedTab(int chip, int ioPortsFrameSelectedTab) {
        if (this.ioPortsFrameSelectedTab == null || this.ioPortsFrameSelectedTab.length != 2) this.ioPortsFrameSelectedTab = new int[]{0, 0};
        this.ioPortsFrameSelectedTab[chip] = ioPortsFrameSelectedTab;
    }

    public List<Integer> getAdValueList(int chip, String channelKey) {
        if (adValueListMap == null || adValueListMap.length != 2) adValueListMap = new Map[]{new HashMap<String, ArrayList<Integer>>(), new HashMap<String, ArrayList<Integer>>()};
        return adValueListMap[chip].get(channelKey);
    }

    public List<Integer> setAdValueList(int chip, String channelKey, List<Integer> values) {
        if (adValueListMap == null || adValueListMap.length != 2) adValueListMap = new Map[]{new HashMap<String, ArrayList<Integer>>(), new HashMap<String, ArrayList<Integer>>()};
        return adValueListMap[chip].put(channelKey, values);
    }

    public int getAdValue(int chip, String channelKey) {
        if (adValueMap == null || adValueMap.length != 2) adValueMap = new Map[]{new HashMap<String, Integer>(), new HashMap<String, Integer>()};
        Integer value = adValueMap[chip].get(channelKey);
        return (value == null?0:value);
    }

    public void setAdValue(int chip, String channelKey, int value) {
        if (adValueMap == null || adValueMap.length != 2) adValueMap = new Map[]{new HashMap<String, Integer>(), new HashMap<String, Integer>()};
        adValueMap[chip].put(channelKey, value);
    }

    public EepromInitMode getEepromInitMode() {
        if (eepromInitMode == null) {
            return EepromInitMode.BLANK;
        }
        else {
            return eepromInitMode;
        }
    }

    public void setEepromInitMode(EepromInitMode eepromInitMode) {
        this.eepromInitMode = eepromInitMode;
    }

    public byte[] getLastEepromContents() {
        return lastEepromContents;
    }

    public void setLastEepromContents(byte[] lastEepromContents) {
        this.lastEepromContents = lastEepromContents;
    }

    public String getLastEepromFileName() {
        return lastEepromFileName;
    }

    public void setLastEepromFileName(String lastEepromFileName) {
        this.lastEepromFileName = lastEepromFileName;
    }

    public Integer getPortInputValueOverride(int chip, int portNumber, int bitNumber) {
        if (ioValueOverrideMap == null || ioValueOverrideMap.length != 2) ioValueOverrideMap = new Map[]{new HashMap<String, Integer>(), new HashMap<String, Integer>()};
        return ioValueOverrideMap[chip].get(portNumber + "-" + bitNumber);
    }

    public void setPortInputValueOverride(int chip, int portNumber, int bitNumber, int value) {
        if (ioValueOverrideMap == null || ioValueOverrideMap.length != 2) ioValueOverrideMap = new Map[]{new HashMap<String, Integer>(), new HashMap<String, Integer>()};
        ioValueOverrideMap[chip].put(portNumber + "-" + bitNumber, value);
    }

    public void removePortInputValueOverride(int chip, int portNumber, int bitNumber) {
        if (ioValueOverrideMap == null || ioValueOverrideMap.length != 2) ioValueOverrideMap = new Map[]{new HashMap<String, Integer>(), new HashMap<String, Integer>()};
        ioValueOverrideMap[chip].remove(portNumber + "-" + bitNumber);
    }

    public boolean isSyncPlay() {
        return syncPlay;
    }

    public void setSyncPlay(boolean syncPlay) {
        this.syncPlay = syncPlay;
    }

    /**
     * This is basically just a structure with an X Y value.
     * Now used for position but also for size
     * java.awt.Point looks similar but has double as getters :-(. Moreover, it would break current config files
     */
    public class WindowPosition {
        int x = 0;
        int y = 0;

        public WindowPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }
}
