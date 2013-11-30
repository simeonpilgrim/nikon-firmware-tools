package com.nikonhacker.disassembly.fr;

import com.nikonhacker.disassembly.*;
import com.nikonhacker.emu.memory.Memory;

import java.io.PrintWriter;
import java.util.*;

public class FrCodeAnalyzer extends CodeAnalyzer {

    public FrCodeAnalyzer(CodeStructure codeStructure, SortedSet<Range> ranges, Memory memory, Map<Integer, Symbol> symbols, Map<Integer, List<Integer>> jumpHints, Set<OutputOption> outputOptions, PrintWriter debugPrintWriter) {
        super(codeStructure, ranges, memory, symbols, jumpHints, outputOptions, debugPrintWriter);
    }

    protected final int[] getJmpTableAddressSize(int address) {
// try to resolve this typical FR compiler construct :
//                            000ABB66  A850                         CMP     #0x5,R<0>   ; number_of_elements
//                            000ABB68  F510                         BNC:D   part_2_of_sub_abb5e_              ; (skip)
//                            000ABB6A  8B0D                          MOV    R<0>,AC
//                            000ABB6C  9F8C 002A 5384               LDI:32  #0x<002A5384>,R12 ; base_address
//                            000ABB72  B42D                         LSL     #2,AC
//                            000ABB74  00CC                         LD      @(AC,R12),R12
//                            000ABB76  970C                         JMP     @R12
        if (
            ((memory.loadInstruction16(address - 14) & 0xFF00) == 0xF500)   // BNC:D
            &&((memory.loadInstruction16(address - 12) & 0xFF0F) == 0x8B0D) // MOV    R<0>,AC
            && (memory.loadInstruction16(address - 10) == 0x9F8C) // LDI:32  #0x<base_address>,R12
            && (memory.loadInstruction16(address - 4) == 0xB42D) // LSL     #2,AC
            && (memory.loadInstruction16(address - 2) == 0x00CC) // LD      @(AC,R12),R12
            && (memory.loadInstruction16(address    ) == 0x970C) // JMP     @R12
            ) {

            int baseAddress = memory.loadInstruction32(address - 8);
            
            // Short version (size <= 0xF) : 000ABB66  A850                         CMP     #0x5,R<0>   ; number_of_elements
            if (   ((memory.loadInstruction16(address - 16) & 0xFF00) == 0xA800) // comparing max value with register <n'>
                && ((memory.loadInstruction16(address - 16) & 0x000F) == ((memory.loadInstruction16(address - 12) & 0x00F0) >> 4)) // check n == n'
                ) {
                    return new int[] {baseAddress, (memory.loadInstruction16(address - 16) & 0x00F0) >> 4};
            }
            // Long version (size > 0xF):  0030A43A  C10C                         LDI:8   #0x10,R12
            //                             0030A43C  AAC4                         CMP     R12,R4
            else if (  ((memory.loadInstruction16(address - 18) & 0xF000) == 0xC000) // copying max value to register <m>
                    && ((memory.loadInstruction16(address - 16) & 0xFF00) == 0xAA00) // comparing register <m'> with register <n'>
                    && ((memory.loadInstruction16(address - 18) & 0x000F) == ((memory.loadInstruction16(address - 16) & 0x00F0) >> 4)) // check m == m'
                    && ((memory.loadInstruction16(address - 16) & 0x000F) == ((memory.loadInstruction16(address - 12) & 0x00F0) >> 4)) // check n == n'
                    ) {
                return new int[] {baseAddress, (memory.loadInstruction16(address - 18) & 0x0FF0) >> 4};
            }

        }
        else
            // try to resolve this alternate typical compiler construct :
//                                0012785A  C90C           LDI:8   #0x90,R12
//                                0012785C  AAC4           CMP     R12,R4
//                                0012785E  E404           BC      label_127868_              ; (skip)
//                                00127860  9F8C 0012 7C02 LDI:32  #0x00127C02,R12
//                                00127866  9F0C           JMP:D   @R12; part_2_of_sub_127848_ (skip)
//                                label_127868_:
//                                00127868  8B4D            MOV    R4,AC
//                                0012786A  9F8C 0024 A438 LDI:32  #0x0024A438,R12
//                                00127870  B42D           LSL     #2,AC
//                                00127872  00CC           LD      @(AC,R12),R12
//                                00127874  970C           JMP     @R12

        if (
               (memory.loadInstruction16(address - 22) == 0xE404) // BC +04
            && (memory.loadInstruction16(address - 20) == 0x9F8C) // LDI32:xxxx xxxx,R12
            && (memory.loadInstruction16(address - 14) == 0x9F0C) // JMP:D   @R12;
            &&((memory.loadInstruction16(address - 12) & 0xFF0F) == 0x8B0D) // MOV    R<0>,AC
            && (memory.loadInstruction16(address - 10) == 0x9F8C) // LDI:32  #0x<base_address>,R12
            && (memory.loadInstruction16(address - 4) == 0xB42D) // LSL     #2,AC
            && (memory.loadInstruction16(address - 2) == 0x00CC) // LD      @(AC,R12),R12
            && (memory.loadInstruction16(address    ) == 0x970C) // JMP     @R12
            ) {

            int baseAddress = memory.loadInstruction32(address - 8);

            // Short version (size <= 0xF) : 000B442A  A844           CMP     #0x4,R<4>   ; number_of_elements
            if (   ((memory.loadInstruction16(address - 24) & 0xFF00) == 0xA800) // comparing max value with register <n'>
                    && ((memory.loadInstruction16(address - 24) & 0x000F) == ((memory.loadInstruction16(address - 12) & 0x00F0) >> 4)) // check n == n'
                    ) {
                return new int[] {baseAddress, (memory.loadInstruction16(address - 24) & 0x00F0) >> 4};
            }
            // Long version (size > 0xF):  0012785A  C90C           LDI:8   #0x90,R12
            //                             0012785C  AAC4           CMP     R12,R4
            else if (  ((memory.loadInstruction16(address - 26) & 0xF000) == 0xC000) // copying max value to register <m>
                    && ((memory.loadInstruction16(address - 24) & 0xFF00) == 0xAA00) // comparing register <m'> with register <n'>
                    && ((memory.loadInstruction16(address - 26) & 0x000F) == ((memory.loadInstruction16(address - 24) & 0x00F0) >> 4)) // check m == m'
                    && ((memory.loadInstruction16(address - 24) & 0x000F) == ((memory.loadInstruction16(address - 12) & 0x00F0) >> 4)) // check n == n'
                    ) {
                return new int[] {baseAddress, (memory.loadInstruction16(address - 26) & 0x0FF0) >> 4};
            }
        }
        return null;
    }
}