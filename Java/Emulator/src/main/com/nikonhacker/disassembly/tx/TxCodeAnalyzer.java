package com.nikonhacker.disassembly.tx;

import com.nikonhacker.BinaryArithmetics;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.*;
import com.nikonhacker.emu.memory.Memory;

import java.io.PrintWriter;
import java.util.*;
import java.lang.Math;

public class TxCodeAnalyzer extends CodeAnalyzer {

    public TxCodeAnalyzer(CodeStructure codeStructure, SortedSet<Range> ranges, Memory memory, Map<Integer, Symbol> symbols, Map<Integer, List<Integer>> jumpHints, Set<OutputOption> outputOptions, PrintWriter debugPrintWriter) {
        super(codeStructure, ranges, memory, symbols, jumpHints, outputOptions, debugPrintWriter);
    }

    private final static int extendOffset(final int opcode) {
        return (opcode&0x1F) | ((opcode&0x7E00000)>>16) | ((opcode&0x1F0000)>>5);
    }

    protected final int[] getJmpTableAddressSize(final int address) {
        /*
        resolve typical compiler constructs

        1)
        BFC8C8E4 5C08     sltiu   r4, 0x08
        BFC8C8E6 F7FF6013 bteqz   ...
        
        BFC8C8EA 3288     sll     r2, r4, 2       OR BFC3404E E288     sll     r2, 2

        BFC8C8EC F7D74BE9 lui     r3, 0xBFC9
        BFC8C8F0 E269     addu    r2, r3
        BFC8C8F2 F0E09A50 lw      r2, 0x00F0(r2)
        BFC8C8F6 EA80     jrc     r2

        2)
        BFC4CC9A 5F32     sltiu   r<x>, 0x32
        BFC4CC9C F000600C bteqz   ...
        
        BFC4CCA0 F7D74BE9 lui     r3, 0xBFC9
        BFC4CCA4 E769     addu    r2, r<y>, r3
        BFC4CCA6 F608A240 lbu     r2, 0x4600(r2)
        BFC4CCAA E288     sll     r2, 2

        BFC4CCAC F7D74BE9 lui     r3, 0xBFC9
        BFC4CCB0 E269     addu    r2, r3
        BFC4CCB2 F6289A54 lw      r2, 0x4634(r2)
        BFC4CCB6 EA80     jrc     r2

        */
        if (
              ((memory.loadInstruction32(address - 10)& 0xF800FFE0)== 0xF0004BE0) // lui     r3, <baseHI>  
            && (memory.loadInstruction16(address - 6) == 0xE269)                  // addu    r2, r3        
            &&((memory.loadInstruction32(address - 4) & 0xF800FFE0)== 0xF0009A40) // lw      r2, <baseLOsigned>(r2)
            && (memory.loadInstruction16(address    ) == 0xEA80)) {               // jrc     r2            

                // tabled access
                final int baseAddress = (extendOffset(memory.loadInstruction32(address - 10))<<16) +
                                        BinaryArithmetics.signExtend(16, extendOffset(memory.loadInstruction32(address - 4)));
                    
                if (
                    ((memory.loadInstruction16(address - 18) & 0xF800) == 0x5800) // sltiu   r<x>, 0x08 ; number_of_elements
                    && ((memory.loadInstruction32(address - 16) & 0xF800FFE0) == 0xF0006000)) { // bteqz   part_2_of_init

                    // simple case (1)
                    final int shiftOpcode = memory.loadInstruction16(address - 12);

                    if ( (((shiftOpcode & 0xFF1F) == 0x3208)       // sll     r2, r<y>, 2
                         && ((memory.loadInstruction16(address - 18) & 0x0700) == ((shiftOpcode & 0x00E0) << 3))) // check x == y)
                         ||
                         (((shiftOpcode & 0xF8FF) == 0xE088)       // sll     r2,2
                         && ((memory.loadInstruction16(address - 18) & 0x0700) == (shiftOpcode & 0x0700))) // check x == y)
                         ) {
                        return new int[] {baseAddress, (memory.loadInstruction16(address - 18) & 0x00FF) };
                    }
                } else if ( 
                    ((memory.loadInstruction16(address - 28) & 0xF800) == 0x5800) // sltiu   r<x>, 0x08 ; number_of_elements
                    && ((memory.loadInstruction32(address - 26) & 0xF800FFE0) == 0xF0006000)) { // bteqz   

                    // indirect table case (2)
                    final int addOpcode = memory.loadInstruction16(address - 18);

                    if ( (((addOpcode & 0xF8FF) == 0xE069)       // addu    r2, r<y>, r3
                         && ((memory.loadInstruction16(address - 28) & 0x0700) == (addOpcode & 0x0700))) // check x == y)
                         ) {
                         int indexTableSize = memory.loadInstruction16(address - 28) & 0x00FF;
                         
                         // get index table address
                        if ( indexTableSize>0
                            &&((memory.loadInstruction32(address - 22)& 0xF800FFE0)== 0xF0004BE0) // lui     r3, <baseHI>  
                                                                                                  // ...
                            &&((memory.loadInstruction32(address -16) & 0xF800FFE0)== 0xF000A240) // lbu     r2, <baseLOsigned>(r2)
                            && (memory.loadInstruction16(address -12) == 0xE288)) {               // sll     r2, 2            
                         
                            // parse table of indexes and search for highest value - this will be jump table size
                            final int baseAddressIndexes = (extendOffset(memory.loadInstruction32(address - 22))<<16) +
                                                    BinaryArithmetics.signExtend(16, extendOffset(memory.loadInstruction32(address - 16)));
                            int maxIndex = 0;
                            while (indexTableSize-- >0) {
                                maxIndex = Math.max(maxIndex, memory.loadInstruction8(baseAddressIndexes+indexTableSize));
                            }
                            return new int[] {baseAddress,maxIndex+1};
                        }
                    } 
                }
        }
        return null;
    }
}
