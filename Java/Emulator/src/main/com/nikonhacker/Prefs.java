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
    int sleepTick = 2;
    List<BreakTrigger>[] triggers = new ArrayList[2];
    Set<OutputOption> outputOptions;

    /**
    @deprecated Use buttonSize field
     */
    boolean largeToolbarButtons = false;

    String buttonSize = EmulatorUI.BUTTON_SIZE_MEDIUM;

    boolean closeAllWindowsOnStop = false;

    boolean writeDisassemblyToFile = true;
    boolean followPc = true;

    private HashMap<String, WindowPosition> windowPositionMap;
    private HashMap<String, WindowPosition> windowSizeMap;

    private String codeStructureGraphOrientation;

    private boolean autoUpdateRealOsObjects = true;

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

    public boolean isLargeToolbarButtons() {
        return largeToolbarButtons;
    }

    public void setLargeToolbarButtons(boolean largeToolbarButtons) {
        this.buttonSize = largeToolbarButtons?EmulatorUI.BUTTON_SIZE_LARGE:EmulatorUI.BUTTON_SIZE_SMALL;
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

    public Set<OutputOption> getOutputOptions() {
        if (outputOptions==null) {
            // Prepare a new outputOptions containing only default values
            outputOptions = EnumSet.noneOf(OutputOption.class);
            for (OutputOption option : EnumSet.allOf(OutputOption.class)) {
                if (option.isDefaultValue()) {
                    outputOptions.add(option);
                }
            }
        }
        return outputOptions;
    }

    public void setOutputOption(OutputOption outputOption, boolean value) {
        getOutputOptions();
        if (value) {
            outputOptions.add(outputOption);
        }
        else {
            outputOptions.remove(outputOption);
        }
    }

    public List<BreakTrigger> getTriggers(int chip) {
        if (triggers[chip] == null) triggers[chip] = new ArrayList<BreakTrigger>();
        return triggers[chip];
    }

    public void setTriggers(int chip, List<BreakTrigger> triggers) {
        this.triggers[chip] = triggers;
    }

    public int getWindowPositionX(String windowName) {
        if (windowPositionMap==null) windowPositionMap = new HashMap<String, WindowPosition>();
        WindowPosition windowPosition = windowPositionMap.get(windowName);
        return (windowPosition==null)?0:windowPosition.getX();
    }

    public int getWindowPositionY(String windowName) {
        if (windowPositionMap==null) windowPositionMap = new HashMap<String, WindowPosition>();
        WindowPosition windowPosition = windowPositionMap.get(windowName);
        return (windowPosition==null)?0:windowPosition.getY();
    }

    public void setWindowPosition(String windowName, int x, int y) {
        if (windowPositionMap==null) windowPositionMap = new HashMap<String, WindowPosition>();
        windowPositionMap.put(windowName, new WindowPosition(x, y));
    }

    public int getWindowSizeX(String windowName) {
        if (windowSizeMap==null) windowSizeMap = new HashMap<String, WindowPosition>();
        WindowPosition windowSize = windowSizeMap.get(windowName);
        return (windowSize==null)?0:windowSize.getX();
    }

    public int getWindowSizeY(String windowName) {
        if (windowSizeMap==null) windowSizeMap = new HashMap<String, WindowPosition>();
        WindowPosition windowSize = windowSizeMap.get(windowName);
        return (windowSize==null)?0:windowSize.getY();
    }

    public void setWindowSize(String windowName, int x, int y) {
        if (windowSizeMap==null) windowSizeMap = new HashMap<String, WindowPosition>();
        windowSizeMap.put(windowName, new WindowPosition(x, y));
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

    /**
     * This is basically just a structure with an X Y value.
     * Now used for position but also for size
     * Should probably have used java.awt.Point but didn't want to break saved prefs ...
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
