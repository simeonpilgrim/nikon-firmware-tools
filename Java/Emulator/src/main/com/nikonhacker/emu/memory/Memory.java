package com.nikonhacker.emu.memory;

import com.nikonhacker.disassembly.Range;

import java.io.*;
import java.util.Collection;

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

    /**
     *
     * @return the number of pages in memory
     */
    int getNumPages();

    /**
     *
     * @return the size of a memory page
     */
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

    void loadFile(File file, int memoryOffset, boolean isWriteProtected) throws IOException;

    void loadFile(File file, Collection<Range> ranges, boolean isWriteProtected) throws IOException;

    void saveToFile(File file, int startAddress, int length) throws IOException;

    void saveAllToStream(OutputStream outputStream) throws IOException;

    void loadAllFromStream(InputStream inputStream) throws IOException;

    void clear();
}
