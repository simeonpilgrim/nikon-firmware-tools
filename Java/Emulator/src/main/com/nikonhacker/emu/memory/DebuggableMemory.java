/*
 * Part of this file is taken from PearColator project
 * aka binarytranslator.org. The binarytranslator.org
 * project is distributed under the Common Public License (CPL).
 * A copy of the license is included in the distribution, and is also
 * available at http://www.opensource.org/licenses/cpl1.0.php
 *
 * (C) Copyright Ian Rogers, The University of Manchester 2003-2006
 */
package com.nikonhacker.emu.memory;

import com.nikonhacker.emu.memory.listener.MemoryActivityListener;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/*
 * Part of this file is taken from PearColator project
 * aka binarytranslator.org. The binarytranslator.org
 * project is distributed under the Common Public License (CPL).
 * A copy of the license is included in the distribution, and is also
 * available at http://www.opensource.org/licenses/cpl1.0.php
 *
 * (C) Copyright Ian Rogers, The University of Manchester 2003-2006
 */
public class DebuggableMemory extends AbstractMemory implements Memory {

    private List<MemoryActivityListener> activityListeners = new ArrayList<MemoryActivityListener>();

    public enum AccessSource{
        /** Access due to code reading/writing to memory */
        CODE,
        /** Access done by DMA controller */
        DMA,
        /** Access done by IMAGE transfer of FR */
        IMGA;

        public static EnumSet<AccessSource> selectableAccessSource = EnumSet.of(CODE, DMA, IMGA);
    }


    public DebuggableMemory(boolean logMemoryMessages) {
        clear();
        setLogMemoryMessages(logMemoryMessages);
    }

    public void clear() {
        super.clear();
    }

    public void addActivityListener(MemoryActivityListener activityListener) {
        if (activityListener.isReadOnly()) {
            // add at the end so that logging occurs after modifications
            activityListeners.add(activityListener);
        }
        else {
            // add at the start so that modifications occur before logging
            activityListeners.add(0, activityListener);
        }
    }

    public boolean removeActivityListener(MemoryActivityListener activityListener) {
        return activityListeners.remove(activityListener);
    }

    /**
     * Perform a byte load where the sign extended result fills the return value
     *
     * @param addr the address of the value to load
     * @return the sign extended result
     */
    final public int loadSigned8(int addr) {
        return loadSigned8(addr, AccessSource.CODE);
    }

    final public int loadSigned8(int addr, AccessSource accessSource) {
        int page = getPTE(addr);
        int offset = getOffset(addr);
        try {
            byte[] pageData = readableMemory[page];
            if (pageData == null) {
                map(truncateToPage(addr), PAGE_SIZE, true, true, true);
                pageData = readableMemory[page];
            }

            byte value = pageData[offset];
            if (accessSource != null) {
                for (MemoryActivityListener activityListener : activityListeners) {
                    if (activityListener.matches(addr)) {
                        Byte b = activityListener.onLoadData8(pageData, addr, value, accessSource);
                        if (b != null) {
                            value = b;
                        }
                    }
                }
            }

            // value is promoted to int, and sign extended in the process.
            // e.g. if value is 0xFF, then the returned value will be 0xFFFFFFFF = -1d
            return value;
        } catch (NullPointerException e) {
             System.err.println("Null pointer exception loading from address: 0x" + Integer.toHexString(addr));
             throw e;
        }
    }

    /**
     * Perform a byte load where the zero extended result fills the return value
     *
     * @param addr the address of the value to load
     * @return the zero extended result
     */
    final public int loadUnsigned8(int addr) {
        return loadUnsigned8(addr, AccessSource.CODE);
    }
    
    final public int loadUnsigned8(int addr, AccessSource accessSource) {
        int page = getPTE(addr);
        int offset = getOffset(addr);
        try {
            byte[] pageData = readableMemory[page];
            if (pageData == null) {
                map(truncateToPage(addr), PAGE_SIZE, true, true, true);
                pageData = readableMemory[page];
            }

            byte value = pageData[offset];
            if (accessSource != null) {
                for (MemoryActivityListener activityListener : activityListeners) {
                    if (activityListener.matches(addr)) {
                        Byte b = activityListener.onLoadData8(pageData, addr, value, accessSource);
                        if (b != null) {
                            value = b;
                        }
                    }
                }
            }
            // value is promoted to int, then only the last 8 bits are returned.
            // e.g. if value is 0xFF, then the returned value will be 0x000000FF = 255d
            return value & 0xFF;
        } catch (NullPointerException e) {
            System.err.println("Null pointer exception loading from address: 0x" + Integer.toHexString(addr));
            throw e;
        }
    }

    /**
     * Perform a 16bit load where the sign extended result fills the return value
     *
     * @param addr the address of the value to load
     * @return the sign extended result
     */
    public int loadSigned16(int addr) {
        return loadSigned16(addr, AccessSource.CODE);
    }
    
    public int loadSigned16(int addr, AccessSource accessSource) {
        int value = (loadSigned8(addr, null) << 8) | loadUnsigned8(addr + 1, null);
        if (accessSource != null) {
            for (MemoryActivityListener activityListener : activityListeners) {
                if (activityListener.matches(addr)) {
                    Integer i = activityListener.onLoadData16(readableMemory[getPTE(addr)], addr, value, accessSource);
                    if (i != null) {
                        value = i;
                    }
                }
            }
        }
        // TODO shouldn't we sign-extend this value ???
        // e.g. if value is 0xFFFF, then the returned value will be 0xFFFFFFFF = -1d
        // return BinaryArithmetics.signExtend(16, value);
        return value;
    }

    /**
     * Perform a 16bit load where the zero extended result fills the return value
     *
     * @param addr the address of the value to load
     * @return the zero extended result
     */
    public int loadUnsigned16(int addr) {
        return loadUnsigned16(addr, AccessSource.CODE);
    }
    
    public int loadUnsigned16(int addr, AccessSource accessSource) {
        int value = (loadUnsigned8(addr, null) << 8) | loadUnsigned8(addr + 1, null);
        if (accessSource != null) {
            for (MemoryActivityListener activityListener : activityListeners) {
                if (activityListener.matches(addr)) {
                    Integer i = activityListener.onLoadData16(readableMemory[getPTE(addr)], addr, value, accessSource);
                    if (i != null) {
                        value = i;
                    }
                }
            }
        }
        // value is already an int, with highest 16 bits set to 0.
        // e.g. if value is 0x(0000)FFFF, then the returned value will be 0x0000FFFF = 65535d
        return value;
    }

    /**
     * Perform a 32bit load
     *
     * @param addr the address of the value to load
     * @return the result
     */
    public int load32(int addr) {
        return load32(addr, AccessSource.CODE);
    }

    public int load32(int addr, AccessSource accessSource) {
        int value = (loadSigned8(addr, null) << 24) | (loadUnsigned8(addr + 1, null) << 16)
                | (loadUnsigned8(addr + 2, null) << 8) | loadUnsigned8(addr + 3, null);
        if (accessSource != null) {
            for (MemoryActivityListener activityListener : activityListeners) {
                if (activityListener.matches(addr)) {
                    Integer i = activityListener.onLoadData32(readableMemory[getPTE(addr)], addr, value, accessSource);
                    if (i != null) {
                        value = i;
                    }
                }
            }
        }
        return value;
    }


    /**
     * Perform a 8bit load from memory that must be executable
     *
     * @param addr the address of the value to load
     * @return the result
     */
    public int loadInstruction8(int addr) {
        return loadInstruction8(addr, AccessSource.CODE);
    }

    public int loadInstruction8(int addr, AccessSource accessSource) {
        int page = getPTE(addr);
        int offset = getOffset(addr);
        if (accessSource != null) {
            for (MemoryActivityListener activityListener : activityListeners) {
                if (activityListener.matches(addr)) {
                    activityListener.onLoadInstruction8(executableMemory[page], addr, executableMemory[page][offset], accessSource);
                }
            }
        }
        return executableMemory[page][offset] & 0xFF;
    }

    public int loadInstruction16(int addr) {
        return loadInstruction16(addr, AccessSource.CODE);
    }

    public int loadInstruction16(int addr, AccessSource accessSource) {
        int value = (loadInstruction8(addr, null) << 8) | loadInstruction8(addr + 1, null);
        if (accessSource != null) {
            for (MemoryActivityListener activityListener : activityListeners) {
                if (activityListener.matches(addr)) {
                    activityListener.onLoadInstruction16(executableMemory[getPTE(addr)], addr, value, accessSource);
                }
            }
        }
        return value;
    }

    /**
     * Perform a 32bit load from memory that must be executable
     *
     * @param addr the address of the value to load
     * @return the result
     */
    public int loadInstruction32(int addr) {
        return loadInstruction32(addr, AccessSource.CODE);
    }

    public int loadInstruction32(int addr, AccessSource accessSource) {
        int value = (loadInstruction8(addr, null) << 24)
                | (loadInstruction8(addr + 1, null) << 16)
                | (loadInstruction8(addr + 2, null) << 8) | loadInstruction8(addr + 3, null);
        if (accessSource != null) {
            for (MemoryActivityListener activityListener : activityListeners) {
                if (activityListener.matches(addr)) {
                    activityListener.onLoadInstruction32(executableMemory[getPTE(addr)], addr, value, accessSource);
                }
            }
        }
        return value;
    }

    /**
     * Perform a byte store
     *
     * @param value the value to store
     * @param addr  the address of where to store
     */
    public final void store8(int addr, int value) {
        store8(addr, value, AccessSource.CODE);
    }

    public final void store8(int addr, int value, AccessSource accessSource) {
        int page = getPTE(addr);
        int offset = getOffset(addr);

        byte[] pageData = writableMemory[page];
        if (pageData == null) {
            map(truncateToPage(addr), PAGE_SIZE, true, true, true);
            pageData = writableMemory[page];
        }
        if (accessSource != null) {
            for (MemoryActivityListener activityListener : activityListeners) {
                if (activityListener.matches(addr)) {
                    activityListener.onStore8(pageData, addr, (byte) value, accessSource);
                }
            }
        }
        pageData[offset] = (byte) value;
    }

    /**
     * Perform a 16bit store
     *
     * @param value the value to store
     * @param addr  the address of where to store
     */
    public void store16(int addr, int value) {
        store16(addr, value, AccessSource.CODE);
    }

    public void store16(int addr, int value, AccessSource accessSource) {
        if (accessSource != null) {
            for (MemoryActivityListener activityListener : activityListeners) {
                if (activityListener.matches(addr)) {
                    activityListener.onStore16(writableMemory[getPTE(addr)], addr, value, accessSource);
                }
            }
        }
        store8(addr, value >> 8, null);
        store8(addr + 1, value, null);
    }

    /**
     * Perform a 32bit store
     *
     * @param value the value to store
     * @param addr  the address of where to store
     */
    public void store32(int addr, int value) {
        store32(addr, value, AccessSource.CODE);
    }

    public void store32(int addr, int value, AccessSource accessSource) {
        if (accessSource != null) {
            for (MemoryActivityListener activityListener : activityListeners) {
                if (activityListener.matches(addr)) {
                    activityListener.onStore32(writableMemory[getPTE(addr)], addr, value, accessSource);
                }
            }
        }
        store8(addr, value >> 24, null);
        store8(addr + 1, value >> 16, null);
        store8(addr + 2, value >> 8, null);
        store8(addr + 3, value, null);
    }

}
