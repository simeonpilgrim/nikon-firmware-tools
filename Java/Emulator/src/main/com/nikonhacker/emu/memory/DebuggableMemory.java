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

import com.nikonhacker.Format;
import com.nikonhacker.emu.EmulatorOptions;
import com.nikonhacker.emu.memory.listener.MemoryActivityListener;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
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

    /** Constructor - used when this is the instantiated class */
    public DebuggableMemory() {
        clear();
    }

    public List<MemoryActivityListener> getActivityListeners() {
        return activityListeners;
    }

    public void addActivityListener(MemoryActivityListener activityListener) {
        activityListeners.add(activityListener);
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
        try {
            int page = getPTE(addr);
            int offset = getOffset(addr);
            if (EmulatorOptions.debugMemory)
                System.err.println("LoadS8 address: 0x" + Integer.toHexString(addr)
                        + " val: " + readableMemory[page][offset]);

            byte[] pageData = readableMemory[page];
            if (pageData == null) {
                map(truncateToPage(addr), PAGE_SIZE, true, true, true);
                pageData = readableMemory[page];
            }

            for (MemoryActivityListener activityListener : activityListeners) {
                activityListener.onLoadData(page, offset, pageData[offset]);
            }

            return pageData[offset];
        } catch (NullPointerException e) {
            System.err.println("Null pointer exception at address: 0x" + Integer.toHexString(addr));
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
        try {
            int page = getPTE(addr);
            int offset = getOffset(addr);
            if (EmulatorOptions.debugMemory)
                System.err.println("LoadU8 address: 0x" + Integer.toHexString(addr)
                        + " val: " + readableMemory[page][offset]);

            byte[] pageData = readableMemory[page];
            if (pageData == null) {
                map(truncateToPage(addr), PAGE_SIZE, true, true, true);
                pageData = readableMemory[page];
            }

            for (MemoryActivityListener activityListener : activityListeners) {
                activityListener.onLoadData(page, offset, pageData[offset]);
            }

            return pageData[offset] & 0xFF;
        } catch (NullPointerException e) {
            throw new MemoryException("Memory not initialized trying to read data from address: 0x" + Integer.toHexString(addr));
        }
    }

    /**
     * Perform a 16bit load where the sign extended result fills the return value
     *
     * @param addr the address of the value to load
     * @return the sign extended result
     */
    public int loadSigned16(int addr) {
        return (loadSigned8(addr) << 8) | loadUnsigned8(addr + 1);
    }

    /**
     * Perform a 16bit load where the zero extended result fills the return value
     *
     * @param addr the address of the value to load
     * @return the zero extended result
     */
    public int loadUnsigned16(int addr) {
        return (loadUnsigned8(addr) << 8) | loadUnsigned8(addr + 1);
    }

    /**
     * Perform a 32bit load
     *
     * @param addr the address of the value to load
     * @return the result
     */
    public int load32(int addr) {
        try {
            return (loadSigned8(addr) << 24) | (loadUnsigned8(addr + 1) << 16)
                    | (loadUnsigned8(addr + 2) << 8) | loadUnsigned8(addr + 3);
        } catch (Exception e) {
            throw new SegmentationFault(addr, e);
        }
    }


    /**
     * Perform a 8bit load from memory that must be executable
     *
     * @param addr the address of the value to load
     * @return the result
     */
    public int loadInstruction8(int addr) {
        int page = getPTE(addr);
        int offset = getOffset(addr);
        if (EmulatorOptions.debugMemory)
            System.err.println("LoadI8 address: 0x" + Integer.toHexString(addr)
                    + " val: " + executableMemory[page][offset]);

        for (MemoryActivityListener activityListener : activityListeners) {
            activityListener.onLoadInstruction(page, offset, executableMemory[page][offset]);
        }
        return executableMemory[page][offset] & 0xFF;
    }

    public int loadInstruction16(int addr) {
        return (loadInstruction8(addr) << 8) | loadInstruction8(addr + 1);
    }

    /**
     * Perform a 32bit load from memory that must be executable
     *
     * @param addr the address of the value to load
     * @return the result
     */
    public int loadInstruction32(int addr) {
        return (loadInstruction8(addr) << 24)
                | (loadInstruction8(addr + 1) << 16)
                | (loadInstruction8(addr + 2) << 8) | loadInstruction8(addr + 3);
    }

    /**
     * Perform a byte store
     *
     * @param value the value to store
     * @param addr  the address of where to store
     */
    public final void store8(int addr, int value) {
        int page = getPTE(addr);
        int offset = getOffset(addr);
        if (EmulatorOptions.debugMemory) {
            System.err.println("Store8 address: 0x" + Integer.toHexString(addr)
                    + " val: 0x" + Format.asHex((value & 0xFF), 2) + " - 0b" + StringUtils.leftPad(Integer.toBinaryString(value & 0xFF), 8).replace('0', ' '));
        }

        byte[] pageData = writableMemory[page];
        if (pageData == null) {
            map(truncateToPage(addr), PAGE_SIZE, true, true, true);
            pageData = writableMemory[page];
        }

        for (MemoryActivityListener activityListener : activityListeners) {
            activityListener.onStore(page, offset, (byte) value);
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
        store8(addr, value >> 8);
        store8(addr + 1, value);
    }

    /**
     * Perform a 32bit store
     *
     * @param value the value to store
     * @param addr  the address of where to store
     */
    public void store32(int addr, int value) {
        try {
            store8(addr, value >> 24);
            store8(addr + 1, value >> 16);
            store8(addr + 2, value >> 8);
            store8(addr + 3, value);
        } catch (Exception e) {
            throw new SegmentationFault(addr, e);
        }
    }

    public byte[] getPageForAddress(int addr) {
        return getPage(getPTE(addr));
    }
}
