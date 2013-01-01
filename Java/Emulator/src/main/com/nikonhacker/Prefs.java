package com.nikonhacker;


import com.nikonhacker.disassembly.OutputOption;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.gui.EmulatorUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

public class Prefs {
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
    private EnumSet<OutputOption>[] outputOptions;
    private Map<Integer,Byte>[] ioPortMap;
    private boolean[] autoUpdateRealOsObjectWindow;
    private boolean[] callStackHideJumps;
    private int sleepTick[];
    private boolean writeDisassemblyToFile[];
    private boolean sourceCodeFollowsPc[];
    private String codeStructureGraphOrientation[];

    private static File getPreferenceFile() {
        return new File(System.getProperty("user.home") + File.separator + "." + ApplicationInfo.getName());
    }

    public static void save(Prefs prefs) {
        try {
            XStreamUtils.save(prefs, new FileOutputStream(getPreferenceFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Prefs load() {
        File preferenceFile = getPreferenceFile();
        try {
            if (preferenceFile.exists()) {
                FileInputStream inputStream = new FileInputStream(preferenceFile);
                Prefs prefs = (Prefs) XStreamUtils.load(inputStream);
                inputStream.close();
                return prefs;
            }
        } catch (Exception e) {
            System.out.println("Could not load preferences file. Attempting a rename to " + preferenceFile.getAbsolutePath() + ".corrupt and creating new file.");
            preferenceFile.renameTo(new File(preferenceFile.getAbsolutePath() + ".corrupt"));
            e.printStackTrace();
        }
        return new Prefs();
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
        if (sourceCodeFollowsPc == null || sourceCodeFollowsPc.length != 2) sourceCodeFollowsPc = new boolean[2];
        return sourceCodeFollowsPc[chip];
    }

    public void setSourceCodeFollowsPc(int chip, boolean value) {
        if (sourceCodeFollowsPc == null || sourceCodeFollowsPc.length != 2) sourceCodeFollowsPc = new boolean[2];
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

    public void setTriggers(int chip, List<BreakTrigger> triggers) {
        this.triggers[chip] = triggers;
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

    public void setAutoUpdateRealOsObjects(int chip, boolean autoUpdateRealOsObjects) {
        if (autoUpdateRealOsObjectWindow == null) autoUpdateRealOsObjectWindow = new boolean[2];
        this.autoUpdateRealOsObjectWindow[chip] = autoUpdateRealOsObjects;
    }

    public boolean isAutoUpdateRealOsObjects(int chip) {
        if (autoUpdateRealOsObjectWindow == null) autoUpdateRealOsObjectWindow = new boolean[2];
        return autoUpdateRealOsObjectWindow[chip];
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

    public byte getPortValue(int chip, int portNumber) {
        if (ioPortMap == null) ioPortMap = new Map[2];
        if (ioPortMap[chip] == null) ioPortMap[chip] = new HashMap<Integer, Byte>();
        Byte value = ioPortMap[chip].get(portNumber);
        if (value == null) return 0;
        else return value;
    }

    public void setPortValue(int chip, int portNumber, byte value) {
        if (ioPortMap == null) ioPortMap = new Map[2];
        if (ioPortMap[chip] == null) ioPortMap[chip] = new HashMap<Integer, Byte>();
        ioPortMap[chip].put(portNumber, value);
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
