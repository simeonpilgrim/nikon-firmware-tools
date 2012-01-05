/*
 * This file is part of binarytranslator.org. The binarytranslator.org
 * project is distributed under the Common Public License (CPL).
 * A copy of the license is included in the distribution, and is also
 * available at http://www.opensource.org/licenses/cpl1.0.php
 *
 * (C) Copyright Ian Rogers, The University of Manchester 2003-2006
 */
package com.nikonhacker.emu.memory;

/**
 * Captures exceptions that can occur during memory management
 */
final public class MemoryMapException 
  extends RuntimeException {
  
  public enum Reason {
    /** Attempt to allocate on a non-page boundary */
    UNALIGNED_ADDRESS,
    /** Attempt to allocate from a file on an unaligned file offset */
    UNALIGNED_FILE_OFFSET
  }

  /**
   * The type of this memory map exception
   */
  private Reason reason;

  /**
   * The file offset or address that was unaligned
   */
  private long offsetOrAddress;

  /**
   * Throw an unaligned address memory map exception
   */
  static void unalignedAddress(int addr) throws MemoryMapException {
    throw new MemoryMapException((long) addr, Reason.UNALIGNED_ADDRESS);
  }

  /**
   * Throw an unaligned file offset memory map exception
   */
  static void unalignedFileOffset(long offset) throws MemoryMapException {
    throw new MemoryMapException(offset, Reason.UNALIGNED_FILE_OFFSET);
  }

  /**
   * Constructor
   */
  private MemoryMapException(long addr, Reason reason) {
    offsetOrAddress = addr;
    this.reason = reason;
  }

  /**
   * String representation of exception
   */
  public String toString() {
    switch (reason) {
    case UNALIGNED_ADDRESS:
      return String.format("Unaligned memory map address: 0x%x", offsetOrAddress);
      
    case UNALIGNED_FILE_OFFSET:
      return String.format("Unaligned file offset: 0x%x", offsetOrAddress);
      
    default:
      throw new RuntimeException("Unexpected MemoryMapException Reason: " + reason);
    }
  }
}
