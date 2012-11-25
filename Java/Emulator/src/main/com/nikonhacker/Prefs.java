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
    public static final String KEY_WINDOW_MAIN = "MAIN";

    int sleepTick = 2;
    List<BreakTrigger>[] triggers = new ArrayList[2];
    EnumSet<OutputOption>[] outputOptions = new EnumSet[2];

    String buttonSize = EmulatorUI.BUTTON_SIZE_SMALL;

    boolean closeAllWindowsOnStop = false;

    boolean writeDisassemblyToFile = true;
    boolean followPc = true;

    private HashMap<String, WindowPosition> windowPositionMap;
    private HashMap<String, WindowPosition> windowSizeMap;

    private String codeStructureGraphOrientation;

    private boolean autoUpdateRealOsObjects = true;

    private int dividerLocation;
    private int lastDividerLocation;
    private boolean dividerKeepHidden;

    public static File getPreferenceFile() {
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
        try {
            File preferenceFile = getPreferenceFile();
            if (preferenceFile.exists()) {
                FileInputStream inputStream = new FileInputStream(preferenceFile);
                Prefs prefs = (Prefs) XStreamUtils.load(inputStream);
                inputStream.close();
                return prefs;
            }
        } catch (Exception e) {
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

    public boolean isWriteDisassemblyToFile() {
        return writeDisassemblyToFile;
    }

    public void setWriteDisassemblyToFile(boolean writeDisassemblyToFile) {
        this.writeDisassemblyToFile = writeDisassemblyToFile;
    }

    public boolean isFollowPc() {
        return followPc;
    }

    public void setFollowPc(boolean followPc) {
        this.followPc = followPc;
    }

    public int getSleepTick() {
        return sleepTick;
    }

    public void setSleepTick(int sleepTick) {
        this.sleepTick = sleepTick;
    }

    public Set<OutputOption> getOutputOptions(int chip) {
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


    public String getCodeStructureGraphOrientation() {
        return codeStructureGraphOrientation;
    }

    public void setCodeStructureGraphOrientation(String codeStructureGraphOrientation) {
        this.codeStructureGraphOrientation = codeStructureGraphOrientation;
    }

    public void setAutoUpdateRealOsObjects(boolean autoUpdateRealOsObjects) {
        this.autoUpdateRealOsObjects = autoUpdateRealOsObjects;
    }

    public boolean isAutoUpdateRealOsObjects() {
        return autoUpdateRealOsObjects;
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
