package com.nikonhacker.emu.memory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/*
 * Part of this file is taken from PearColator project
 * aka binarytranslator.org. The binarytranslator.org
 * project is distributed under the Common Public License (CPL).
 * A copy of the license is included in the distribution, and is also
 * available at http://www.opensource.org/licenses/cpl1.0.php
 *
 * (C) Copyright Ian Rogers, The University of Manchester 2003-2006
 */
public interface Memory {
    int map(int addr, int len, boolean read, boolean write, boolean exec)
            throws MemoryMapException;

    int map(RandomAccessFile file, long offset, int addr, int len,
            boolean read, boolean write, boolean exec) throws MemoryMapException;

    void unmap(int addr, int len);

    boolean isMapped(int addr);

    int getNumPages();

    int getPageSize();

    boolean isPageAligned(int addr);

    int truncateToPage(int addr);

    int truncateToNextPage(int addr);

    int loadSigned8(int addr);

    int loadUnsigned8(int addr);

    int loadSigned16(int addr);

    int loadUnsigned16(int addr);

    int load32(int addr);

    int loadInstruction8(int addr);

    int loadInstruction16(int addr);

    int loadInstruction32(int addr);

    void store8(int addr, int value);

    void store16(int addr, int value);

    void store32(int addr, int value);

    void changeProtection(int address, int len, boolean newRead, boolean newWrite, boolean newExec);

    void loadFile(File file, int memoryOffset) throws IOException;

    void clear();
}