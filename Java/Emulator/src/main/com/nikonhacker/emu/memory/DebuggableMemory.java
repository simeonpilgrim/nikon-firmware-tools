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

import com.nikonhacker.emu.memory.listener.IoActivityListener;
import com.nikonhacker.emu.memory.listener.MemoryActivityListener;

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

    public static final int NO_IO_LISTENER = -1;

    private List<MemoryActivityListener> activityListeners = new ArrayList<MemoryActivityListener>();
    private IoActivityListener ioActivityListener;
    private int ioPage = NO_IO_LISTENER;


    public DebuggableMemory() {
        clear();
    }

    public void clear() {
        super.clear();
    }

    public void addActivityListener(MemoryActivityListener activityListener) {
        activityListeners.add(activityListener);
    }

    public boolean removeActivityListener(MemoryActivityListener activityListener) {
        return activityListeners.remove(activityListener);
    }

    /**
     * Sets a listener for IO page activity
     * @param ioActivityListener new listener, or null to remove
     */
    public void setIoActivityListener(IoActivityListener ioActivityListener) {
        if (ioActivityListener == null) {
            this.ioPage = NO_IO_LISTENER;
        }
        else {
            this.ioPage = ioActivityListener.getIoPage();
        }
        this.ioActivityListener = ioActivityListener;
    }

    /**
     * Perform a byte load where the sign extended result fills the return value
     *
     * @param addr the address of the value to load
     * @return the sign extended result
     */
    final public int loadSigned8(int addr) {
        return loadSigned8(addr, true);
    }
    
    final public int loadSigned8(int addr, boolean enableListeners) {
        int page = getPTE(addr);
        int offset = getOffset(addr);
        try {
            byte[] pageData = readableMemory[page];
            if (pageData == null) {
                map(truncateToPage(addr), PAGE_SIZE, true, true, true);
                pageData = readableMemory[page];
            }

            byte value = pageData[offset];
            if (enableListeners) {
                for (MemoryActivityListener activityListener : activityListeners) {
                    activityListener.onLoadData8(addr, value);
                }
                if (getPTE(addr) == ioPage) {
                    // if it matches, we have a listener AND we're in its page
                    Byte b = ioActivityListener.onIoLoad8(pageData, addr, value);
                    if (b != null) {
                        value = b;
                    }
                }
            }

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
        return loadUnsigned8(addr, true);
    }
    
    final public int loadUnsigned8(int addr, boolean enableListeners) {
        int page = getPTE(addr);
        int offset = getOffset(addr);
        try {
            byte[] pageData = readableMemory[page];
            if (pageData == null) {
                map(truncateToPage(addr), PAGE_SIZE, true, true, true);
                pageData = readableMemory[page];
            }

            int value = pageData[offset] & 0xFF;
            if (enableListeners) {
                for (MemoryActivityListener activityListener : activityListeners) {
                    activityListener.onLoadData8(addr, pageData[offset]);
                }
                if (getPTE(addr) == ioPage) {
                    // if it matches, we have a listener AND we're in its page
                    Byte b = ioActivityListener.onIoLoad8(pageData, addr, pageData[offset]);
                    if (b != null) {
                        value = b;
                    }
                }
            }
            return value;
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
        return loadSigned16(addr, true);
    }
    
    public int loadSigned16(int addr, boolean enableListeners) {
        int value = (loadSigned8(addr, false) << 8) | loadUnsigned8(addr + 1, false);
        if (enableListeners) {
            for (MemoryActivityListener activityListener : activityListeners) {
                activityListener.onLoadData16(addr, value);
            }
            if (getPTE(addr) == ioPage) {
                // if it matches, we have a listener AND we're in its page
                Integer i = ioActivityListener.onIoLoad16(readableMemory[ioPage], addr, value);
                if (i != null) {
                    value = i;
                }
            }
        }

        return value;
    }

    /**
     * Perform a 16bit load where the zero extended result fills the return value
     *
     * @param addr the address of the value to load
     * @return the zero extended result
     */
    public int loadUnsigned16(int addr) {
        return loadUnsigned16(addr, true);   
    }
    
    public int loadUnsigned16(int addr, boolean enableListeners) {
        int value = (loadUnsigned8(addr, false) << 8) | loadUnsigned8(addr + 1, false);
        if (enableListeners) {
            for (MemoryActivityListener activityListener : activityListeners) {
                activityListener.onLoadData16(addr, value);
            }
            if (getPTE(addr) == ioPage) {
                // if it matches, we have a listener AND we're in its page
                Integer i = ioActivityListener.onIoLoad16(readableMemory[ioPage], addr, value);
                if (i != null) {
                    value = i;
                }
            }
        }
        return value;
    }

    /**
     * Perform a 32bit load
     *
     * @param addr the address of the value to load
     * @return the result
     */
    public int load32(int addr) {
        return load32(addr, true);
    }

    public int load32(int addr, boolean enableListeners) {
        int value = (loadSigned8(addr, false) << 24) | (loadUnsigned8(addr + 1, false) << 16)
                | (loadUnsigned8(addr + 2, false) << 8) | loadUnsigned8(addr + 3, false);
        if (enableListeners) {
            for (MemoryActivityListener activityListener : activityListeners) {
                activityListener.onLoadData32(addr, value);
            }
            if (getPTE(addr) == ioPage) {
                // if it matches, we have a listener AND we're in its page
                Integer i = ioActivityListener.onIoLoad32(readableMemory[ioPage], addr, value);
                if (i != null) {
                    value = i;
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
        return loadInstruction8(addr, true);
    }

    public int loadInstruction8(int addr, boolean enableListeners) {
        int page = getPTE(addr);
        int offset = getOffset(addr);
        if (enableListeners) {
            for (MemoryActivityListener activityListener : activityListeners) {
                activityListener.onLoadInstruction8(addr, executableMemory[page][offset]);
            }
        }
        return executableMemory[page][offset] & 0xFF;
    }

    public int loadInstruction16(int addr) {
        return loadInstruction16(addr, true);
    }

    public int loadInstruction16(int addr, boolean enableListeners) {
        int value = (loadInstruction8(addr, false) << 8) | loadInstruction8(addr + 1, false);
        if (enableListeners) {
            for (MemoryActivityListener activityListener : activityListeners) {
                activityListener.onLoadInstruction16(addr, value);
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
        return loadInstruction32(addr, true);
    }

    public int loadInstruction32(int addr, boolean enableListeners) {
        int value = (loadInstruction8(addr, false) << 24)
                | (loadInstruction8(addr + 1, false) << 16)
                | (loadInstruction8(addr + 2, false) << 8) | loadInstruction8(addr + 3, false);
        if (enableListeners) {
            for (MemoryActivityListener activityListener : activityListeners) {
                activityListener.onLoadInstruction32(addr, value);
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
        store8(addr, value, true);
    }

    public final void store8(int addr, int value, boolean enableListeners) {
        int page = getPTE(addr);
        int offset = getOffset(addr);

        byte[] pageData = writableMemory[page];
        if (pageData == null) {
            map(truncateToPage(addr), PAGE_SIZE, true, true, true);
            pageData = writableMemory[page];
        }
        if (enableListeners) {
            if (page == ioPage) {
                // if it matches, we have a listener AND we're in its page
                ioActivityListener.onIoStore8(pageData, addr, (byte) value);
            }
            for (MemoryActivityListener activityListener : activityListeners) {
                activityListener.onStore8(addr, (byte) value);
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
        store16(addr, value, true);
    }

    public void store16(int addr, int value, boolean enableListeners) {
        if (enableListeners) {
            if (getPTE(addr) == ioPage) {
                // if it matches, we have a listener AND we're in its page
                ioActivityListener.onIoStore16(writableMemory[ioPage], addr, value);
            }
            for (MemoryActivityListener activityListener : activityListeners) {
                activityListener.onStore16(addr, value);
            }
        }
        store8(addr, value >> 8, false);
        store8(addr + 1, value, false);
    }

    /**
     * Perform a 32bit store
     *
     * @param value the value to store
     * @param addr  the address of where to store
     */
    public void store32(int addr, int value) {
        store32(addr, value, true);
    }

    public void store32(int addr, int value, boolean enableListeners) {
        if (enableListeners) {
            if (getPTE(addr) == ioPage) {
                // if it matches, we have a listener AND we're in its page
                ioActivityListener.onIoStore32(writableMemory[ioPage], addr, value);
            }
            for (MemoryActivityListener activityListener : activityListeners) {
                activityListener.onStore32(addr, value);
            }
        }
        store8(addr, value >> 24, false);
        store8(addr + 1, value >> 16, false);
        store8(addr + 2, value >> 8, false);
        store8(addr + 3, value, false);
    }

    public byte[] getPageForAddress(int addr) {
        int pte = getPTE(addr);
        return getPage(pte);
    }
}
