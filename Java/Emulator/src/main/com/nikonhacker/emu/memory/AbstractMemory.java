package com.nikonhacker.emu.memory;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.Range;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
public abstract class AbstractMemory implements Memory {
    /** The size of a single page in bytes. */
    static final int PAGE_SIZE = 0x10000;

    /** Bits in offset (inside a page) = numbits(PAGE_SIZE-1) */
    static final int OFFSET_BITS = 16;

    /** The number of pages */
    static final int NUM_PAGES = 0x10000;

    /** The maximum amount of RAM available */
    public static final long MAX_RAM = (long) PAGE_SIZE * (long) NUM_PAGES;

    /** The memory backing store */
    byte readableMemory[][];
    byte writableMemory[][];
    byte executableMemory[][];

    /** Do we have more optimal nio mmap operation? */
    boolean HAVE_java_nio_FileChannelImpl_nio_mmap_file = false;

    protected boolean logMemoryMessages = true;

    public AbstractMemory() {
        clear();
    }

    @Override
    public void setLogMemoryMessages(boolean logMemoryMessages) {
        this.logMemoryMessages = logMemoryMessages;
    }

    public void clear() {
        readableMemory = new byte[NUM_PAGES][];
        writableMemory = new byte[NUM_PAGES][];
        executableMemory = new byte[NUM_PAGES][];
    }

    /** Return the offset part of the address */
    static int getOffset(int address) {
        return address & (PAGE_SIZE - 1);
    }

    /** Return the page table entry part of the address */
    static int getPTE(int address) {
        return address >>> OFFSET_BITS;
    }

    /**
     * Find free consecutive pages
     *
     * @param pages the number of pages required
     * @return the address found
     */
    final int findFreePages(int pages) {
        starting_page_search:
        for (int i = 0; i < NUM_PAGES; i++) {
            if (getPage(i) == null) {
                int start = i;
                int end = i + pages;
                for (; i <= end; i++) {
                    if (getPage(i) != null) {
                        continue starting_page_search;
                    }
                }
                return start << OFFSET_BITS;
            }
        }
        throw new Error(
                "No mappable consecutive pages found for an anonymous map of size"
                        + (pages * PAGE_SIZE));
    }

    /**
     * Map an anonymous page of memory
     *
     * @param addr  the address to map or NULL if don't care
     * @param len   the amount of memory to map
     * @param read  is the page readable
     * @param write is the page writable
     * @param exec  is the page executable
     */
    public int map(int addr, int len, boolean read, boolean write, boolean exec)
            throws MemoryMapException {
        // Check address is page aligned
        if ((addr % PAGE_SIZE) != 0) {
            MemoryMapException.unalignedAddress(addr);
        }

        // Create memory
        int numPages = (len + PAGE_SIZE - 1) / PAGE_SIZE;
        byte pages[][] = new byte[numPages][PAGE_SIZE];

        // Find address if not specified
        if (addr == 0) {
            addr = findFreePages(numPages);
        }

        if (logMemoryMessages) {
            System.out.println("Anonymous mapping: addr=0x"
                    + Integer.toHexString(addr) + " len=" + len + (read ? " r" : " -")
                    + (write ? "w" : "-") + (exec ? "x" : "-"));
        }

        // Get page table entry
        int pte = getPTE(addr);
        for (int i = 0; i < numPages; i++) {

            // Check pages aren't already allocated
            if (getPage(pte + i) != null) {
                throw new Error("Memory map of already mapped location addr=0x"
                        + Integer.toHexString(addr) + " len=" + len);
            }

            // Allocate pages
            readableMemory[pte + i] = read ? pages[i] : new byte[0];
            writableMemory[pte + i] = write ? pages[i] : new byte[0];
            executableMemory[pte + i] = exec ? pages[i] : new byte[0];
        }

        return addr;
    }

    /**
     * Map a page of memory from file
     *
     * @param file  the file map in from
     * @param addr  the address to map or NULL if don't care
     * @param len   the amount of memory to map
     * @param read  is the page readable
     * @param write is the page writable
     * @param exec  is the page executable
     */
    public int map(RandomAccessFile file, long offset, int addr, int len,
                   boolean read, boolean write, boolean exec) throws MemoryMapException {
        // Check address is page aligned
        if ((addr % PAGE_SIZE) != 0) {
            MemoryMapException.unalignedAddress(addr);
        }

        // Check file offset is page aligned
        /*if ((offset % PAGE_SIZE) != 0) {
          MemoryMapException.unalignedFileOffset(offset);
        }*/

        // Calculate number of pages
        int num_pages = (len + PAGE_SIZE - 1) / PAGE_SIZE;
        // Find address if not specified
        if (addr == 0) {
            addr = findFreePages(num_pages);
        }
        if (logMemoryMessages) {
            System.out.println("Mapping file " + file + " offset=" + offset
                    + " addr=0x" + Integer.toHexString(addr) + " len=" + len
                    + (read ? " r" : " -") + (write ? "w" : "-") + (exec ? "x" : "-"));
        }
        try {
            // Get page table entry
            int pte = getPTE(addr);
            // Can we optimise the reads to use mmap?
            if (!HAVE_java_nio_FileChannelImpl_nio_mmap_file) {
                // Sub-optimal
                file.seek(offset);
                for (int i = 0; i < num_pages; i++) {
                    // Check pages aren't already allocated
                    if (getPage(pte + i) != null) {
                        throw new Error("Memory map of already mapped location addr=0x" + Integer.toHexString(addr) + " len=" + len);
                    }
                    // Allocate page
                    byte page[] = new byte[PAGE_SIZE];
                    if (i == 0) { // first read, start from offset upto a page length
                        file.read(page, getOffset(addr), PAGE_SIZE - getOffset(addr));
                    }
                    else if (i == (num_pages - 1)) { // last read
                        file.read(page, 0, ((len - getOffset(addr)) % PAGE_SIZE));
                    }
                    else {
                        file.read(page);
                    }

                    readableMemory[pte + i] = read ? page : new byte[0];
                    writableMemory[pte + i] = write ? page : new byte[0];
                    executableMemory[pte + i] = exec ? page : new byte[0];
                }
            }
            else {
                for (int i = 0; i < num_pages; i++) {
                    // Check pages aren't already allocated
                    if (getPage(pte + i) != null) {
                        throw new Error("Memory map of already mapped location addr=0x" + Integer.toHexString(addr) + " len=" + len);
                    }

                    // Allocate page
                    if (read && write) {
                        readableMemory[pte + i] = file.getChannel().map(
                                FileChannel.MapMode.READ_WRITE, offset + (i * PAGE_SIZE),
                                PAGE_SIZE).array();
                        writableMemory[pte + i] = readableMemory[pte + i];
                        if (exec) {
                            executableMemory[pte + i] = readableMemory[pte + i];
                        }
                    }
                    else if (read) {
                        readableMemory[pte + i] = file.getChannel().map(
                                FileChannel.MapMode.READ_ONLY, offset + (i * PAGE_SIZE),
                                PAGE_SIZE).array();
                        if (exec) {
                            executableMemory[pte + i] = readableMemory[pte + i];
                        }
                    }
                    else if (exec) {
                        executableMemory[pte + i] = file.getChannel().map(
                                FileChannel.MapMode.READ_ONLY, offset + (i * PAGE_SIZE),
                                PAGE_SIZE).array();
                    }
                    else {
                        throw new Error("Unable to map address 0x"
                                + Integer.toHexString(addr) + " with permissions "
                                + (read ? "r" : "-") + (write ? "w" : "-") + (exec ? "x" : "-"));
                    }
                }
            }
            return addr;
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    /**
     * Returns the page currently mapped at the given page table entry.
     *
     * @param pte The page table entry, for which a page is to be retrieved.
     * @return The page (R,W,X) mapped at the given page table entry or null, if no page is currently mapped
     *         to that entry.
     */
    byte[] getPage(int pte) {

        if (readableMemory[pte] != null)
            return readableMemory[pte];

        if (writableMemory[pte] != null)
            return writableMemory[pte];

        if (executableMemory[pte] != null)
            return executableMemory[pte];

        return null;
    }

    /**
     * Returns the page containing the given address.
     *
     * @param addr The address for which a page is to be retrieved.
     * @return The page (R,W,X) mapped at the given page table entry or null, if no page is currently mapped
     *         to that entry.
     */
    public byte[] getPageForAddress(int addr) {
        int pte = getPTE(addr);
        return getPage(pte);
    }

    /**
     * Unmap a page of memory
     *
     * @param addr the address to unmap
     * @param len  the amount of memory to unmap
     */
    public void unmap(int addr, int len) {
        for (int i = 0; i < len; i += PAGE_SIZE) {

            int pte = getPTE(addr + i);
            if (getPage(pte) != null) {
                readableMemory[pte] = null;
                writableMemory[pte] = null;
                executableMemory[pte] = null;
            }
            else {
                throw new Error("Unmapping memory that's not mapped addr=0x" + Integer.toHexString(addr) + " len=" + len);
            }
        }
    }

    /**
     * Is the given address mapped into memory?
     *
     * @param addr to check
     * @return true => memory is mapped
     */
    public boolean isMapped(int addr) {
        return getPage(getPTE(addr)) != null;
    }

    /**
     *
     * @return the number of pages in memory
     */
    public int getNumPages() {
        return NUM_PAGES;
    }

    /**
     *
     * @return the size of a memory page
     */
    public int getPageSize() {
        return PAGE_SIZE;
    }

    /**
     * Is the given address aligned on a page boundary?
     *
     * @param addr the address to check
     * @return whether the address is aligned
     */
    public boolean isPageAligned(int addr) {
        return (addr % PAGE_SIZE) == 0;
    }

    /**
     * Make the given address page aligned to the page beneath it
     *
     * @param addr the address to truncate
     * @return the truncated address
     */
    public int truncateToPage(int addr) {
        return (addr >> OFFSET_BITS) << OFFSET_BITS;
    }

    /**
     * Make the given address page aligned to the page above it
     *
     * @param addr the address to truncate
     * @return the truncated address
     */
    public int truncateToNextPage(int addr) {
        return ((addr + PAGE_SIZE - 1) >> OFFSET_BITS) << OFFSET_BITS;
    }

    public void changeProtection(int address, int len, boolean newRead, boolean newWrite, boolean newExec) {

        while (len > 0) {
            int pte = getPTE(address);
            byte[] page = getPage(pte);

            if (page == null)
                throw new RuntimeException("Segmentation fault at 0x" + Format.asHex(address, 8));

            readableMemory[pte] = newRead ? page : new byte[0];
            writableMemory[pte] = newWrite ? page : new byte[0];
            executableMemory[pte] = newExec ? page : new byte[0];

            address += PAGE_SIZE;
            len -= PAGE_SIZE;
        }
    }

    public void loadFile(File file, int startAddress, boolean isWriteProtected) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        int pte = getPTE(startAddress);
        int offset = getOffset(startAddress);
        long bytesRemainingToRead = file.length();
        while (bytesRemainingToRead > 0) {
            int bytesRemainingInPage = getPageSize() - offset;
            int bytesToRead = (int) Math.min(bytesRemainingInPage, bytesRemainingToRead);
            byte[] page = getPage(pte);
            if (page == null) {
                // Unallocated page, allocate it
                map(pte << OFFSET_BITS, getPageSize(), true, !isWriteProtected, true);
                page = getPage(pte);
            }
            int bytesRead = fis.read(page, offset, bytesToRead);
            if (bytesRead != bytesToRead) {
                throw new IOException("Error : expected " + bytesToRead + " bytes but could only read " + bytesRead);
            }
            bytesRemainingToRead -= bytesToRead;
            pte++;
            offset = 0;
        }
        fis.close();
    }

    public void loadFile(File sourceFile, Collection<Range> ranges, boolean isWriteProtected) throws IOException {
        FileChannel fc = new RandomAccessFile(sourceFile, "r").getChannel();
        for (Range range : ranges) {

            int rangeSize = range.getEnd() - range.getStart() + 1;

            ByteBuffer buffer = ByteBuffer.allocate(rangeSize);

            // Read bytes of the file.
            if (fc.position()!=range.getFileOffset()) {
                if (range.getFileOffset()>=fc.size())
                    throw new IOException("Error : expected file offset " + range.getFileOffset() + " do not exist");
                fc.position(range.getFileOffset());
            }
            int bytesRead;
            do {
                bytesRead = fc.read(buffer);
            } while (bytesRead != -1 && buffer.hasRemaining());

            // Push bytes to memory
            int address = range.getStart();
            // map address must be page size aligned
            map(truncateToPage(address), getOffset(address) + rangeSize, true, !isWriteProtected, true);
            int bytesPushed = 0;
            buffer.position(0);
            while (bytesPushed < rangeSize) {
                int page = getPTE(address);
                int offset = getOffset(address);
                byte[] pageBuffer = getPage(page);
                int byteCount = Math.min(rangeSize - bytesPushed, PAGE_SIZE - offset);
                buffer.get(pageBuffer, offset, byteCount);
                address += byteCount;
                bytesPushed += byteCount;
            }
        }
        fc.close();
    }

    public void saveToFile(File file, int startAddress, int length) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        int pte = getPTE(startAddress);
        int offset = getOffset(startAddress);
        int bytesRemainingToWrite = length;
        while (bytesRemainingToWrite > 0) {
            int bytesRemainingInPage = getPageSize() - offset;
            int bytesToWrite = Math.min(bytesRemainingInPage, bytesRemainingToWrite);
            byte[] page = getPage(pte);
            if (page == null) {
                // Unallocated page, use 0-filled page
                page = new byte[PAGE_SIZE];
            }
            fos.write(page, offset, bytesToWrite);
            bytesRemainingToWrite -= bytesToWrite;
            pte++;
            offset = 0;
        }
        fos.close();
    }

    public int getNumUsedPages() {
        int numPages = 0;
        for (int i = 0; i < NUM_PAGES; i++) {
            if ((readableMemory[i] != null) || (writableMemory[i] != null) || (executableMemory[i] != null)) {
                numPages++;
            }
        }
        return numPages;
    }

    public void saveAllToStream(OutputStream outputStream) throws IOException {
        // Header contains one byte per page, each with the 3 LSB representing R/W/X
        for (int i = 0; i < NUM_PAGES; i++) {
            outputStream.write((byte) ((readableMemory[i] == null ? 0 : 0x4) | (writableMemory[i] == null ? 0 : 0x2) | (executableMemory[i] == null ? 0 : 0x1)));
        }
        // Then write the contents of used pages
        for (int i = 0; i < NUM_PAGES; i++) {
            byte[] values = readableMemory[i];
            if (values == null) values = writableMemory[i];
            if (values == null) values = executableMemory[i];
            if (values != null) {
                outputStream.write(values);
            }
        }
    }

    public void loadAllFromStream(InputStream inputStream) throws IOException {
        clear();
        // Header contains one byte per page, each with the 3 LSB representing R/W/X
        for (int i = 0; i < NUM_PAGES; i++) {
            byte b = (byte) inputStream.read();
            if (b != 0) {
                byte page[] = new byte[PAGE_SIZE];
                readableMemory[i] = ((b & 0x4) != 0) ? page : null;
                writableMemory[i] = ((b & 0x2) != 0) ? page : null;
                executableMemory[i] = ((b & 0x1) != 0) ? page : null;
            }
        }
        // Then write the contents of used pages
        for (int i = 0; i < NUM_PAGES; i++) {
            byte[] values = readableMemory[i];
            if (values == null) values = writableMemory[i];
            if (values == null) values = executableMemory[i];
            if (values != null) {
                int bytesRead = 0;
                while (bytesRead != PAGE_SIZE) {
                    bytesRead += inputStream.read(values, bytesRead, PAGE_SIZE - bytesRead);
                }
            }
        }
    }
}
