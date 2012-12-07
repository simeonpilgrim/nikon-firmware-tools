/*
 * This file is part of binarytranslator.org. The binarytranslator.org
 * project is distributed under the Common Public License (CPL).
 * A copy of the license is included in the distribution, and is also
 * available at http://www.opensource.org/licenses/cpl1.0.php
 *
 * (C) Copyright Ian Rogers, The University of Manchester 2003-2007
 */
package com.nikonhacker.emu.memory;

/** @author Ian Rogers */
public class MemoryWriteError extends RuntimeException {
    protected final int address;

    /** Constructor */
    public MemoryWriteError(int address) {
        super("SegFault at 0x" + Integer.toHexString(address));
        this.address = address;
    }

    public MemoryWriteError(int address, Exception cause) {
        super("SegFault at 0x" + Integer.toHexString(address), cause);
        this.address = address;
    }
}
