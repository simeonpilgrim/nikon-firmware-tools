package com.nikonhacker.emu.memory.listener;

public class TrackingMemoryActivityListener extends Abstract8BitMemoryActivityListener implements MemoryActivityListener {

    /** Arrays to track activity */
    private int[] pageActivityMap;
    private int[][] cellActivityMaps;
    
    private int pageSize;

    /** Preference to keep values flashing (rotating) as data is read or written
     */
    private boolean mustRotateValues = false;


    public TrackingMemoryActivityListener(int numPages, int pageSize) {
        this.pageSize = pageSize;
        pageActivityMap = new int[numPages];
        cellActivityMaps = new int[numPages][];
    }

    public int[] getPageActivityMap() {
        return pageActivityMap;
    }

    public int[] getCellActivityMap(int page) {
        return cellActivityMaps[page];
    }

    public boolean isMustRotateValues() {
        return mustRotateValues;
    }

    public void setMustRotateValues(boolean mustRotateValues) {
        this.mustRotateValues = mustRotateValues;
//        if (!mustRotateValues) {
//            // Clear transparency
//            for (int page = 0; page < numPages; page++) {
//                pageActivityMap[page] &= 0xFFFFFF;
//                if (cellActivityMaps[page] != null) {
//                    for (int offset = 0; offset < pageSize ; offset++) {
//                        cellActivityMaps[page][offset] &= 0xFFFFFF;
//                    }
//                }
//            }
//        }
    }


    @Override
    public boolean matches(int address) {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    public Byte onLoadData8(byte[] pageData, int address, byte value) {
        int pageNumber = address >>> 16;
        int offset = address & 0xFFFF;

//        if ((pageActivityMap[page] & 0xFF00)!=0xFF00) pageActivityMap[page]+= 0x0100;
//        if (mustRotateValues) pageActivityMap[page] ^= 0x7F000000;
//
//        if (cellActivityMaps[page] == null) {
//            cellActivityMaps[page] = new int[pageSize];
//        }
//        if ((cellActivityMaps[page][offset] & 0xFF00)!=0xFF00) cellActivityMaps[page][offset]+= 0x0100;
//        if (mustRotateValues) cellActivityMaps[page][offset] ^= 0x7F000000;

        if (mustRotateValues || ((pageActivityMap[pageNumber] & 0xFF00)!=0xFF00)) pageActivityMap[pageNumber]+= 0x0100;
        if (cellActivityMaps[pageNumber] == null) {
            cellActivityMaps[pageNumber] = new int[pageSize];
        }
        if (mustRotateValues || ((cellActivityMaps[pageNumber][offset] & 0xFF00)!=0xFF00)) cellActivityMaps[pageNumber][offset]+= 0x0100;

        return null;
    }

    public void onLoadInstruction8(byte[] pageData, int address, byte value) {
        int pageNumber = address >>> 16;
        int offset = address & 0xFFFF;

//        if ((pageActivityMap[page] & 0xFF)!=0xFF) pageActivityMap[page]+= 0x01;
//        if (mustRotateValues) pageActivityMap[page] ^= 0x7F000000;
//
//        if (cellActivityMaps[page] == null) {
//            cellActivityMaps[page] = new int[pageSize];
//        }
//        if ((cellActivityMaps[page][offset] & 0xFF)!=0xFF) cellActivityMaps[page][offset]+= 0x01;
//        if (mustRotateValues) cellActivityMaps[page][offset] ^= 0x7F000000;

        if (mustRotateValues || ((pageActivityMap[pageNumber] & 0xFF)!=0xFF)) pageActivityMap[pageNumber]+= 0x01;
        if (cellActivityMaps[pageNumber] == null) {
            cellActivityMaps[pageNumber] = new int[pageSize];
        }
        if (mustRotateValues || ((cellActivityMaps[pageNumber][offset] & 0xFF)!=0xFF)) cellActivityMaps[pageNumber][offset]+= 0x01;
    }

    public void onStore8(byte[] pageData, int address, byte value) {
        int pageNumber = address >>> 16;
        int offset = address & 0xFFFF;

//        if ((pageActivityMap[page] & 0xFF0000)!=0xFF0000) pageActivityMap[page]+= 0x010000;
//        if (mustRotateValues) pageActivityMap[page] ^= 0x7F000000;
//
//        if (cellActivityMaps[page] == null) {
//            cellActivityMaps[page] = new int[pageSize];
//        }
//        if ((cellActivityMaps[page][offset] & 0xFF0000)!=0xFF0000) cellActivityMaps[page][offset]+= 0x010000;
//        if (mustRotateValues) cellActivityMaps[page][offset] ^= 0x7F000000;

        if (mustRotateValues || ((pageActivityMap[pageNumber] & 0xFF0000)!=0xFF0000)) pageActivityMap[pageNumber]+= 0x010000;
        if (cellActivityMaps[pageNumber] == null) {
            cellActivityMaps[pageNumber] = new int[pageSize];
        }
        if (mustRotateValues || ((cellActivityMaps[pageNumber][offset] & 0xFF0000)!=0xFF0000)) cellActivityMaps[pageNumber][offset]+= 0x010000;
    }

}
