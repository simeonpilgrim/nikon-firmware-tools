using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Diagnostics;
using System.Threading;


///*
// * Copyright (c) 2007, Kevin Schoedel. All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions
// * are met:
// *
// * - Redistributions of source code must retain the above copyright
// *   notice, this list of conditions and the following disclaimer.
// *
// * - Redistributions in binary form must reproduce the above copyright
// *   notice, this list of conditions and the following disclaimer in the
// * 	 documentation and/or other materials provided with the distribution.
// *
// * - Neither the name of Kevin Schoedel nor the names of contributors
// *   may be used to endorse or promote products derived from this software
// *   without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
// * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */

///*
// *  1.00  2007/11/05  kps   First release.
// *  1.01  2007/11/06  kps   Fix uint types, option parsing;
// *                          added split output; other minor tweaks.
// *  1.02  2007/11/07  kps   Bug fixes; minimal data flow tracking.
// *  1.03  2007/11/15  kps   Fixed a stupid bug.
// */


namespace dfr2
{
    partial class Program
    {
        delegate int DisFun(DISTATE d);

        static void Main(string[] args)
        {
            fileoptions("dfr.txt");

            if (options(args) == false) return;
            initialize();
            process();
            finalize();
        }

        static void Log(string fmt, params object[] obj)
        {
            Console.WriteLine(fmt, obj);
            Debug.WriteLine(fmt, obj);
        }



        ///* misc definitions */

        const int BITS = 32;

        static long SignExtendMask(long b, long x)
        {
            return ((-b) * ((b & x) != 0 ? 1 : 0));
        }

        static long SignExtend(int n, long x)
        {
            return (x | SignExtendMask((1 << (n - 1)), x));
        }

        static long NEG(int n, long x)
        {
            //#define NEG(n,x)    (-SEX((n), (x)))
            return (-SignExtend((n), (x)));
        }

        static bool IsNeg(int n, ulong x)
        {
            return ((x) & (1ul << ((n) - 1))) != 0;
            //#define ISNEG(n,x)  ((x) & (1ul << ((n) - 1)))
        }

        delegate string NameMapLoc(DATA addr);


        ///* registers */

        enum Reg // reg
        {
            R0, R1, R2, R3,
            R4, R5, R6, R7,
            R8, R9, R10, R11,
            R12, R13, R14, R15,
            TBR, RP, SSP, USP,
            MDH, MDL, D6, D7,
            D8, D9, D10, D11,
            D12, D13, D14, D15,
            CR0, CR1, CR2, CR3,
            CR4, CR5, CR6, CR7,
            CR8, CR9, CR10, CR11,
            CR12, CR13, CR14, CR15,
            PS, CCR, ILM,
            NOREG = -1,
            AC = R13,
            FP = R14,
            SP = R15,
        }

        const int SPECIALS =   16;
        const int COPROCESSOR = 32;


        ///* instructions */

        enum itype
        {
            TYPE_A,     /* [   op          |  Rj   |  Ri   ] */
            TYPE_B,     /* [   op  |       x       |  Ri   ] */
            TYPE_C,     /* [   op          |   x   |  Ri   ] */
            TYPE_D,     /* [   op          |       x       ] */
            TYPE_E,     /* [   op                  |  Ri   ] */
            TYPE_F,     /* [   op    |     offset / 2      ] */
            TYPE_Z,     /* [   op                          ] */
            TYPE_W,     /* [               x               ] */
        }

        class OPCODE
        {
            public UInt16 encoding;
            public UInt16 mask;
            public itype type;
            public string name;
            public string format;
            public string action;

            public OPCODE(UInt16 _encoding, UInt16 _mask, itype _type, string _name, string _format, string _action)
            {
                encoding = _encoding;
                mask = _mask;
                type = _type;
                name = _name;
                format = _format;
                action = _action;
            }
        }

        ///* main instruction map */

        static OPCODE[] opcode = {
            new OPCODE( 0x0000, 0xFF00, itype.TYPE_A, "LD",     "@(A&j),i",     "iw"        ),
            new OPCODE( 0x0100, 0xFF00, itype.TYPE_A, "LDUH",   "@(A&j),i",     "iw"        ),
            new OPCODE( 0x0200, 0xFF00, itype.TYPE_A, "LDUB",   "@(A&j),i",     "iw"        ),
            new OPCODE( 0x0300, 0xFF00, itype.TYPE_C, "LD",     "@(S&4u),i",    "iw"        ),
            new OPCODE( 0x0400, 0xFF00, itype.TYPE_A, "LD",     "@j,i;Ju",      "iw"        ),
            new OPCODE( 0x0500, 0xFF00, itype.TYPE_A, "LDUH",   "@j,i;Ju",      "iw"        ),
            new OPCODE( 0x0600, 0xFF00, itype.TYPE_A, "LDUB",   "@j,i;Ju",      "iw"        ),
            new OPCODE( 0x0700, 0xFFF0, itype.TYPE_E, "LD",     "@S+,i",        "iwSw"      ),
            new OPCODE( 0x0710, 0xFFF0, itype.TYPE_E, "MOV",    "i,P",          "Pw"        ),
            new OPCODE( 0x0780, 0xFFFF, itype.TYPE_E, "LD",     "@S+,g",        "Sw"        ),
            new OPCODE( 0x0781, 0xFFFF, itype.TYPE_E, "LD",     "@S+,g",        "Sw"        ),
            new OPCODE( 0x0782, 0xFFFF, itype.TYPE_E, "LD",     "@S+,g",        "Sw"        ),
            new OPCODE( 0x0783, 0xFFFF, itype.TYPE_E, "LD",     "@S+,g",        "Sw"        ),
            new OPCODE( 0x0784, 0xFFFF, itype.TYPE_E, "LD",     "@S+,g",        "Sw"        ),
            new OPCODE( 0x0785, 0xFFFF, itype.TYPE_E, "LD",     "@S+,g",        "Sw"        ),
            new OPCODE( 0x0790, 0xFFFF, itype.TYPE_Z, "LD",     "@S+,P",        "Sw"        ),
            new OPCODE( 0x0800, 0xFF00, itype.TYPE_D, "DMOV",   "@4u,A",        "Aw"        ),
            new OPCODE( 0x0900, 0xFF00, itype.TYPE_D, "DMOVH",  "@2u,A",        "Aw"        ),
            new OPCODE( 0x0A00, 0xFF00, itype.TYPE_D, "DMOVB",  "@u,A",         "Aw"        ),
            new OPCODE( 0x0B00, 0xFF00, itype.TYPE_D, "DMOV",   "@4u,@-S",      "Sw"        ),
            new OPCODE( 0x0C00, 0xFF00, itype.TYPE_D, "DMOV",   "@4u,@A+",      "Aw"        ),
            new OPCODE( 0x0D00, 0xFF00, itype.TYPE_D, "DMOVH",  "@2u,@A+",      "Aw"        ),
            new OPCODE( 0x0E00, 0xFF00, itype.TYPE_D, "DMOVB",  "@u,@A+",       "Aw"        ),
            new OPCODE( 0x0F00, 0xFF00, itype.TYPE_D, "ENTER",  "#4u",          "SwFw"      ),
            new OPCODE( 0x1000, 0xFF00, itype.TYPE_A, "ST",     "i,@(A&j)",     ""          ),
            new OPCODE( 0x1100, 0xFF00, itype.TYPE_A, "STH",    "i,@(A&j)",     ""          ),
            new OPCODE( 0x1200, 0xFF00, itype.TYPE_A, "STB",    "i,@(A&j)",     ""          ),
            new OPCODE( 0x1300, 0xFF00, itype.TYPE_C, "ST",     "i,@(S&4u)",    ""          ),
            new OPCODE( 0x1400, 0xFF00, itype.TYPE_A, "ST",     "i,@j;Ju",      ""          ),
            new OPCODE( 0x1500, 0xFF00, itype.TYPE_A, "STH",    "i,@j;Ju",      ""          ),
            new OPCODE( 0x1600, 0xFF00, itype.TYPE_A, "STB",    "i,@j;Ju",      ""          ),
            new OPCODE( 0x1700, 0xFFF0, itype.TYPE_E, "ST",     "i,@-S",        "Sw"        ),
            new OPCODE( 0x1710, 0xFFF0, itype.TYPE_E, "MOV",    "P,i",          "iw"        ),
            new OPCODE( 0x1780, 0xFFFF, itype.TYPE_E, "ST",     "g,@-S",        "Sw"        ),
            new OPCODE( 0x1781, 0xFFFF, itype.TYPE_E, "ST",     "g,@-S",        "Sw"        ),
            new OPCODE( 0x1782, 0xFFFF, itype.TYPE_E, "ST",     "g,@-S",        "Sw"        ),
            new OPCODE( 0x1783, 0xFFFF, itype.TYPE_E, "ST",     "g,@-S",        "Sw"        ),
            new OPCODE( 0x1784, 0xFFFF, itype.TYPE_E, "ST",     "g,@-S",        "Sw"        ),
            new OPCODE( 0x1785, 0xFFFF, itype.TYPE_E, "ST",     "g,@-S",        "Sw"        ),
            new OPCODE( 0x1790, 0xFFFF, itype.TYPE_Z, "ST",     "P,@-S",        "Sw"        ),
            new OPCODE( 0x1800, 0xFF00, itype.TYPE_D, "DMOV",   "A,@4u",        ""          ),
            new OPCODE( 0x1900, 0xFF00, itype.TYPE_D, "DMOVH",  "A,@2u",        ""          ),
            new OPCODE( 0x1A00, 0xFF00, itype.TYPE_D, "DMOVB",  "A,@u",         ""          ),
            new OPCODE( 0x1B00, 0xFF00, itype.TYPE_D, "DMOV",   "@S+,@4u",      "Sw"        ),
            new OPCODE( 0x1C00, 0xFF00, itype.TYPE_D, "DMOV",   "@A+,@4u",      "Aw"        ),
            new OPCODE( 0x1D00, 0xFF00, itype.TYPE_D, "DMOVH",  "@A+,@2u",      "Aw"        ),
            new OPCODE( 0x1E00, 0xFF00, itype.TYPE_D, "DMOVB",  "@A+,@u",       "Aw"        ),
            new OPCODE( 0x1F00, 0xFF00, itype.TYPE_D, "INT",    "#u",           "("         ),
            new OPCODE( 0x2000, 0xF000, itype.TYPE_B, "LD",     "@(F&4s),i",    "iw"        ),
            new OPCODE( 0x3000, 0xF000, itype.TYPE_B, "ST",     "i,@(F&4s)",    ""          ),
            new OPCODE( 0x4000, 0xF000, itype.TYPE_B, "LDUH",   "@(F&2s),i",    "iw"        ),
            new OPCODE( 0x5000, 0xF000, itype.TYPE_B, "STH",    "i,@(F&2s)",    ""          ),
            new OPCODE( 0x6000, 0xF000, itype.TYPE_B, "LDUB",   "@(F&s),i",     "iw"        ),
            new OPCODE( 0x7000, 0xF000, itype.TYPE_B, "STB",    "i,@(F&s)",     ""          ),
            new OPCODE( 0x8000, 0xFF00, itype.TYPE_C, "BANDL",  "#u,@i;Iu",     ""          ),
            new OPCODE( 0x8100, 0xFF00, itype.TYPE_C, "BANDH",  "#u,@i;Iu",     ""          ),
            new OPCODE( 0x8200, 0xFF00, itype.TYPE_A, "AND",    "j,i",          "iw"        ),
            new OPCODE( 0x8300, 0xFF00, itype.TYPE_D, "ANDCCR", "#u",           ""          ),
            new OPCODE( 0x8400, 0xFF00, itype.TYPE_A, "AND",    "j,@i;Iu",      ""          ),
            new OPCODE( 0x8500, 0xFF00, itype.TYPE_A, "ANDH",   "j,@i;Iu",      ""          ),
            new OPCODE( 0x8600, 0xFF00, itype.TYPE_A, "ANDB",   "j,@i;Iu",      ""          ),
            new OPCODE( 0x8700, 0xFF00, itype.TYPE_D, "STILM",  "#u",           ""          ),
            new OPCODE( 0x8800, 0xFF00, itype.TYPE_C, "BTSTL",  "#u,@i;Iu",     ""          ),
            new OPCODE( 0x8900, 0xFF00, itype.TYPE_C, "BTSTH",  "#u,@i;Iu",     ""          ),
            new OPCODE( 0x8A00, 0xFF00, itype.TYPE_A, "XCHB",   "@j,i;Ju",      "iw"        ),
            new OPCODE( 0x8B00, 0xFF00, itype.TYPE_A, "MOV",    "j,i",          "iw"        ),
            new OPCODE( 0x8C00, 0xFF00, itype.TYPE_D, "LDM0",   "z",            "Sw"        ),
            new OPCODE( 0x8D00, 0xFF00, itype.TYPE_D, "LDM1",   "y",            "Sw"        ),
            new OPCODE( 0x8E00, 0xFF00, itype.TYPE_D, "STM0",   "xz",           "Sw"        ),
            new OPCODE( 0x8F00, 0xFF00, itype.TYPE_D, "STM1",   "xy",           "Sw"        ),
            new OPCODE( 0x9000, 0xFF00, itype.TYPE_C, "BORL",   "#u,@i;Iu",     ""          ),
            new OPCODE( 0x9100, 0xFF00, itype.TYPE_C, "BORH",   "#u,@i;Iu",     ""          ),
            new OPCODE( 0x9200, 0xFF00, itype.TYPE_A, "OR",     "j,i",          "iw"        ),
            new OPCODE( 0x9300, 0xFF00, itype.TYPE_D, "ORCCR",  "#u",           ""          ),
            new OPCODE( 0x9400, 0xFF00, itype.TYPE_A, "OR",     "j,@i;Iu",      ""          ),
            new OPCODE( 0x9500, 0xFF00, itype.TYPE_A, "ORH",    "j,@i;Iu",      ""          ),
            new OPCODE( 0x9600, 0xFF00, itype.TYPE_A, "ORB",    "j,@i;Iu",      ""          ),
            new OPCODE( 0x9700, 0xFFF0, itype.TYPE_E, "JMP",    "@i;Iu",        "!"         ),
            new OPCODE( 0x9710, 0xFFF0, itype.TYPE_E, "CALL",   "@i;Iu",        "("         ),
            new OPCODE( 0x9720, 0xFFFF, itype.TYPE_Z, "RET",    "",             ")"         ),
            new OPCODE( 0x9730, 0xFFFF, itype.TYPE_Z, "RETI",   "",             ")"         ),
            new OPCODE( 0x9740, 0xFFF0, itype.TYPE_E, "DIV0S",  "i",            "iw"        ),
            new OPCODE( 0x9750, 0xFFF0, itype.TYPE_E, "DIV0U",  "i",            "iw"        ),
            new OPCODE( 0x9760, 0xFFF0, itype.TYPE_E, "DIV1",   "i",            "iw"        ),
            new OPCODE( 0x9770, 0xFFF0, itype.TYPE_E, "DIV2",   "i",            "iw"        ),
            new OPCODE( 0x9780, 0xFFF0, itype.TYPE_E, "EXTSB",  "i",            "iw"        ),
            new OPCODE( 0x9790, 0xFFF0, itype.TYPE_E, "EXTUB",  "i",            "iw"        ),
            new OPCODE( 0x97A0, 0xFFF0, itype.TYPE_E, "EXTSH",  "i",            "iw"        ),
            new OPCODE( 0x97B0, 0xFFF0, itype.TYPE_E, "EXTUH",  "i",            "iw"        ),
            new OPCODE( 0x9800, 0xFF00, itype.TYPE_C, "BEORL",  "#u,@i;Iu",     ""          ),
            new OPCODE( 0x9900, 0xFF00, itype.TYPE_C, "BEORH",  "#u,@i;Iu",     ""          ),
            new OPCODE( 0x9A00, 0xFF00, itype.TYPE_A, "EOR",    "j,i",          "iw"        ),
            new OPCODE( 0x9B00, 0xFF00, itype.TYPE_C, "LDI:20", "X#u,i",        "iv"        ),
            new OPCODE( 0x9C00, 0xFF00, itype.TYPE_A, "EOR",    "j,@i;Iu",      ""          ),
            new OPCODE( 0x9D00, 0xFF00, itype.TYPE_A, "EORH",   "j,@i;Iu",      ""          ),
            new OPCODE( 0x9E00, 0xFF00, itype.TYPE_A, "EORB",   "j,@i;Iu",      ""          ),
            new OPCODE( 0x9F00, 0xFFF0, itype.TYPE_E, "JMP:D",  "@i;Iu",        "_!"        ),
            new OPCODE( 0x9F10, 0xFFF0, itype.TYPE_E, "CALL:D", "@i;Iu",        "_("        ),
            new OPCODE( 0x9F20, 0xFFFF, itype.TYPE_Z, "RET:D",  "",             "_)"        ),
            new OPCODE( 0x9F30, 0xFFFF, itype.TYPE_Z, "INTE",   "",             ""          ),
            new OPCODE( 0x9F60, 0xFFFF, itype.TYPE_Z, "DIV3",   "",             ""          ),
            new OPCODE( 0x9F70, 0xFFFF, itype.TYPE_Z, "DIV4S",  "",             ""          ),
            new OPCODE( 0x9F80, 0xFFF0, itype.TYPE_E, "LDI:32", "XX#u,i",       "iv"        ),
            new OPCODE( 0x9F90, 0xFFFF, itype.TYPE_Z, "LEAVE",  "",             ""          ),
            new OPCODE( 0x9FA0, 0xFFFF, itype.TYPE_Z, "NOP",    "",             ""          ),
            new OPCODE( 0x9FC0, 0xFFF0, itype.TYPE_E, "COPOP",  "Y#u,#c,l,k",   ""          ),
            new OPCODE( 0x9FD0, 0xFFF0, itype.TYPE_E, "COPLD",  "Y#u,#c,j,k",   ""          ),
            new OPCODE( 0x9FE0, 0xFFF0, itype.TYPE_E, "COPST",  "Y#u,#c,l,i",   "iw"        ),
            new OPCODE( 0x9FF0, 0xFFF0, itype.TYPE_E, "COPSV",  "Y#u,#c,l,i",   "iw"        ),
            new OPCODE( 0xA000, 0xFF00, itype.TYPE_C, "ADDN",   "#u,i",         "iw"        ),
            new OPCODE( 0xA100, 0xFF00, itype.TYPE_C, "ADDN2",  "#n,i",         "iw"        ),
            new OPCODE( 0xA200, 0xFF00, itype.TYPE_A, "ADDN",   "j,i",          "iw"        ),
            new OPCODE( 0xA300, 0xFF00, itype.TYPE_D, "ADDSP",  "#4s",          "Sw"        ),
            new OPCODE( 0xA400, 0xFF00, itype.TYPE_C, "ADD",    "#u,i",         "iw"        ),
            new OPCODE( 0xA500, 0xFF00, itype.TYPE_C, "ADD2",   "#n,i",         "iw"        ),
            new OPCODE( 0xA600, 0xFF00, itype.TYPE_A, "ADD",    "j,i",          "iw"        ),
            new OPCODE( 0xA700, 0xFF00, itype.TYPE_A, "ADDC",   "j,i",          "iw"        ),
            new OPCODE( 0xA800, 0xFF00, itype.TYPE_C, "CMP",    "#u,i",         "iw"        ),
            new OPCODE( 0xA900, 0xFF00, itype.TYPE_C, "CMP2",   "#n,i",         "iw"        ),
            new OPCODE( 0xAA00, 0xFF00, itype.TYPE_A, "CMP",    "j,i",          "iw"        ),
            new OPCODE( 0xAB00, 0xFF00, itype.TYPE_A, "MULU",   "j,i",          "iw"        ),
            new OPCODE( 0xAC00, 0xFF00, itype.TYPE_A, "SUB",    "j,i",          "iw"        ),
            new OPCODE( 0xAD00, 0xFF00, itype.TYPE_A, "SUBC",   "j,i",          "iw"        ),
            new OPCODE( 0xAE00, 0xFF00, itype.TYPE_A, "SUBN",   "j,i",          "iw"        ),
            new OPCODE( 0xAF00, 0xFF00, itype.TYPE_A, "MUL",    "j,i",          "iw"        ),
            new OPCODE( 0xB000, 0xFF00, itype.TYPE_C, "LSR",    "#d,i",         "iw"        ),
            new OPCODE( 0xB100, 0xFF00, itype.TYPE_C, "LSR2",   "#d,i",         "iw"        ),
            new OPCODE( 0xB200, 0xFF00, itype.TYPE_A, "LSR",    "j,i",          "iw"        ),
            new OPCODE( 0xB300, 0xFFF0, itype.TYPE_A, "MOV",    "i,h",          ""          ),
            new OPCODE( 0xB310, 0xFFF0, itype.TYPE_A, "MOV",    "i,h",          ""          ),
            new OPCODE( 0xB320, 0xFFF0, itype.TYPE_A, "MOV",    "i,h",          ""          ),
            new OPCODE( 0xB330, 0xFFF0, itype.TYPE_A, "MOV",    "i,h",          ""          ),
            new OPCODE( 0xB340, 0xFFF0, itype.TYPE_A, "MOV",    "i,h",          ""          ),
            new OPCODE( 0xB350, 0xFFF0, itype.TYPE_A, "MOV",    "i,h",          ""          ),
            new OPCODE( 0xB400, 0xFF00, itype.TYPE_C, "LSL",    "#d,i",         "iw"        ),
            new OPCODE( 0xB500, 0xFF00, itype.TYPE_C, "LSL2",   "#d,i",         "iw"        ),
            new OPCODE( 0xB600, 0xFF00, itype.TYPE_A, "LSL",    "j,i",          "iw"        ),
            new OPCODE( 0xB700, 0xFFF0, itype.TYPE_A, "MOV",    "h,i",          "iw"        ),
            new OPCODE( 0xB710, 0xFFF0, itype.TYPE_A, "MOV",    "h,i",          "iw"        ),
            new OPCODE( 0xB720, 0xFFF0, itype.TYPE_A, "MOV",    "h,i",          "iw"        ),
            new OPCODE( 0xB730, 0xFFF0, itype.TYPE_A, "MOV",    "h,i",          "iw"        ),
            new OPCODE( 0xB740, 0xFFF0, itype.TYPE_A, "MOV",    "h,i",          "iw"        ),
            new OPCODE( 0xB750, 0xFFF0, itype.TYPE_A, "MOV",    "h,i",          "iw"        ),
            new OPCODE( 0xB800, 0xFF00, itype.TYPE_C, "ASR",    "#d,i",         "iw"        ),
            new OPCODE( 0xB900, 0xFF00, itype.TYPE_C, "ASR2",   "#d,i",         "iw"        ),
            new OPCODE( 0xBA00, 0xFF00, itype.TYPE_A, "ASR",    "j,i",          "iw"        ),
            new OPCODE( 0xBB00, 0xFF00, itype.TYPE_A, "MULUH",  "j,i",          "iw"        ),
            new OPCODE( 0xBC00, 0xFF00, itype.TYPE_C, "LDRES",  "@i+,#u;Iu",    ""          ),
            new OPCODE( 0xBD00, 0xFF00, itype.TYPE_C, "STRES",  "#u,@i+;Iu",    ""          ),
            new OPCODE( 0xBF00, 0xFF00, itype.TYPE_A, "MULH",   "j,i",          "iw"        ),
            new OPCODE( 0xC000, 0xF000, itype.TYPE_B, "LDI:8",  "#u,i",         "iw"        ),
            new OPCODE( 0xD000, 0xF800, itype.TYPE_F, "CALL",   "2ru",          "("         ),
            new OPCODE( 0xD800, 0xF800, itype.TYPE_F, "CALL:D", "2ru",          "_("        ),
            new OPCODE( 0xE000, 0xFF00, itype.TYPE_D, "BRA",    "2ru",          "!"         ),
            new OPCODE( 0xE100, 0xFF00, itype.TYPE_D, "BNO",    "2ru",          "?"         ),
            new OPCODE( 0xE200, 0xFF00, itype.TYPE_D, "BEQ",    "2ru",          "?"         ),
            new OPCODE( 0xE300, 0xFF00, itype.TYPE_D, "BNE",    "2ru",          "?"         ),
            new OPCODE( 0xE400, 0xFF00, itype.TYPE_D, "BC",     "2ru",          "?"         ),
            new OPCODE( 0xE500, 0xFF00, itype.TYPE_D, "BNC",    "2ru",          "?"         ),
            new OPCODE( 0xE600, 0xFF00, itype.TYPE_D, "BN",     "2ru",          "?"         ),
            new OPCODE( 0xE700, 0xFF00, itype.TYPE_D, "BP",     "2ru",          "?"         ),
            new OPCODE( 0xE800, 0xFF00, itype.TYPE_D, "BV",     "2ru",          "?"         ),
            new OPCODE( 0xE900, 0xFF00, itype.TYPE_D, "BNV",    "2ru",          "?"         ),
            new OPCODE( 0xEA00, 0xFF00, itype.TYPE_D, "BLT",    "2ru",          "?"         ),
            new OPCODE( 0xEB00, 0xFF00, itype.TYPE_D, "BGE",    "2ru",          "?"         ),
            new OPCODE( 0xEC00, 0xFF00, itype.TYPE_D, "BLE",    "2ru",          "?"         ),
            new OPCODE( 0xED00, 0xFF00, itype.TYPE_D, "BGT",    "2ru",          "?"         ),
            new OPCODE( 0xEE00, 0xFF00, itype.TYPE_D, "BLS",    "2ru",          "?"         ),
            new OPCODE( 0xEF00, 0xFF00, itype.TYPE_D, "BHI",    "2ru",          "?"         ),
            new OPCODE( 0xF000, 0xFF00, itype.TYPE_D, "BRA:D",  "2ru",          "_!"        ),
            new OPCODE( 0xF100, 0xFF00, itype.TYPE_D, "BNO:D",  "2ru",          "_?"        ),
            new OPCODE( 0xF200, 0xFF00, itype.TYPE_D, "BEQ:D",  "2ru",          "_?"        ),
            new OPCODE( 0xF300, 0xFF00, itype.TYPE_D, "BNE:D",  "2ru",          "_?"        ),
            new OPCODE( 0xF400, 0xFF00, itype.TYPE_D, "BC:D",   "2ru",          "_?"        ),
            new OPCODE( 0xF500, 0xFF00, itype.TYPE_D, "BNC:D",  "2ru",          "_?"        ),
            new OPCODE( 0xF600, 0xFF00, itype.TYPE_D, "BN:D",   "2ru",          "_?"        ),
            new OPCODE( 0xF700, 0xFF00, itype.TYPE_D, "BP:D",   "2ru",          "_?"        ),
            new OPCODE( 0xF800, 0xFF00, itype.TYPE_D, "BV:D",   "2ru",          "_?"        ),
            new OPCODE( 0xF900, 0xFF00, itype.TYPE_D, "BNV:D",  "2ru",          "_?"        ),
            new OPCODE( 0xFA00, 0xFF00, itype.TYPE_D, "BLT:D",  "2ru",          "_?"        ),
            new OPCODE( 0xFB00, 0xFF00, itype.TYPE_D, "BGE:D",  "2ru",          "_?"        ),
            new OPCODE( 0xFC00, 0xFF00, itype.TYPE_D, "BLE:D",  "2ru",          "_?"        ),
            new OPCODE( 0xFD00, 0xFF00, itype.TYPE_D, "BGT:D",  "2ru",          "_?"        ),
            new OPCODE( 0xFE00, 0xFF00, itype.TYPE_D, "BLS:D",  "2ru",          "_?"        ),
            new OPCODE( 0xFF00, 0xFF00, itype.TYPE_D, "BHI:D",  "2ru",          "_?"        ),
            };

        static OPCODE[] opaltstack = {
           new OPCODE( 0x0700, 0xFFF0, itype.TYPE_E, "POP",    "i",            ""          ),
           new OPCODE( 0x0780, 0xFFFF, itype.TYPE_E, "POP",    "g",            ""          ),
           new OPCODE( 0x0781, 0xFFFF, itype.TYPE_E, "POP",    "g",            ""          ),
           new OPCODE( 0x0782, 0xFFFF, itype.TYPE_E, "POP",    "g",            ""          ),
           new OPCODE( 0x0783, 0xFFFF, itype.TYPE_E, "POP",    "g",            ""          ),
           new OPCODE( 0x0784, 0xFFFF, itype.TYPE_E, "POP",    "g",            ""          ),
           new OPCODE( 0x0785, 0xFFFF, itype.TYPE_E, "POP",    "g",            ""          ),
           new OPCODE( 0x0790, 0xFFFF, itype.TYPE_Z, "POP",    "P",            ""          ),
           new OPCODE( 0x0B00, 0xFF00, itype.TYPE_D, "PUSH",   "@4u",          ""          ),
           new OPCODE( 0x1700, 0xFFF0, itype.TYPE_E, "PUSH",   "i",            ""          ),
           new OPCODE( 0x1780, 0xFFFF, itype.TYPE_E, "PUSH",   "g",            ""          ),
           new OPCODE( 0x1781, 0xFFFF, itype.TYPE_E, "PUSH",   "g",            ""          ),
           new OPCODE( 0x1782, 0xFFFF, itype.TYPE_E, "PUSH",   "g",            ""          ),
           new OPCODE( 0x1783, 0xFFFF, itype.TYPE_E, "PUSH",   "g",            ""          ),
           new OPCODE( 0x1784, 0xFFFF, itype.TYPE_E, "PUSH",   "g",            ""          ),
           new OPCODE( 0x1785, 0xFFFF, itype.TYPE_E, "PUSH",   "g",            ""          ),
           new OPCODE( 0x1790, 0xFFFF, itype.TYPE_Z, "PUSH",   "P",            ""          ),
           new OPCODE( 0x1B00, 0xFF00, itype.TYPE_D, "POP",    "@u",           ""          ),
           new OPCODE( 0x8C00, 0xFF00, itype.TYPE_D, "POP",    "z",            ""          ),
           new OPCODE( 0x8D00, 0xFF00, itype.TYPE_D, "POP",    "y",            ""          ),
           new OPCODE( 0x8E00, 0xFF00, itype.TYPE_D, "PUSH",   "xz",           ""          ),
           new OPCODE( 0x8F00, 0xFF00, itype.TYPE_D, "PUSH",   "xy",           ""          ),
                             };

        static OPCODE[] opaltshift = {
           new OPCODE( 0xB100, 0xFF00, itype.TYPE_C, "LSR",    "#bd,i",        "iw"        ),
           new OPCODE( 0xB500, 0xFF00, itype.TYPE_C, "LSL",    "#bd,i",        "iw"        ),
           new OPCODE( 0xB900, 0xFF00, itype.TYPE_C, "ASR",    "#bd,i",        "iw"        ),
        };

        static OPCODE[] opaltdmov = {
           new OPCODE( 0x0800, 0xFF00, itype.TYPE_D, "LD",     "@4u,A",        ""          ),
           new OPCODE( 0x0900, 0xFF00, itype.TYPE_D, "LDUH",   "@2u,A",        ""          ),
           new OPCODE( 0x0A00, 0xFF00, itype.TYPE_D, "LDUB",   "@u,A",         ""          ),
           new OPCODE( 0x1800, 0xFF00, itype.TYPE_D, "ST",     "A,@4u",        ""          ),
           new OPCODE( 0x1900, 0xFF00, itype.TYPE_D, "STUH",   "A,@2u",        ""          ),
           new OPCODE( 0x1A00, 0xFF00, itype.TYPE_D, "STUB",   "A,@u",         ""          ),
};

        static OPCODE[] opaltspecial = {
           new OPCODE( 0x8300, 0xFF00, itype.TYPE_D, "AND",    "#u,C",         "Cw"        ),
           new OPCODE( 0x8700, 0xFF00, itype.TYPE_D, "MOV",    "#u,M",         ""          ),
           new OPCODE( 0x9300, 0xFF00, itype.TYPE_D, "OR",     "#u,C",         "Cw"        ),
           new OPCODE( 0xA300, 0xFF00, itype.TYPE_D, "ADD",    "#4s,S",        ""          ),
};

        static OPCODE[] opdata = {
           new OPCODE( 0x0000, 0x0000, itype.TYPE_W, "DW",     "u;a",          ""          ),
           new OPCODE( 0x0000, 0x0000, itype.TYPE_W, "DL",     "Xu;a",         ""          ),
           new OPCODE( 0x0000, 0x0000, itype.TYPE_W, "DL",     "Xu;a",         ""          ),
           new OPCODE( 0x0000, 0x0000, itype.TYPE_W, "DL",     "Xu;INT #v",    ""          ),
           new OPCODE( 0x0000, 0x0000, itype.TYPE_W, "DR",     "Xq;f",         ""          ),
};

        enum SpecType
        {
            MD_WORD = 00,
            MD_LONG = 01,
            MD_LONGNUM = 02,
            MD_VECTOR = 03,
            MD_RATIONAL = 04,
            UNKNOWN
        }

        static OPCODE[] decode = new OPCODE[0x10000];


        ///* output formatting */

        class FMT
        {
            public string nxt = ",";
            public string imm = "#";
            public string and = ",";
            public string inc = "+";
            public string dec = "-";
            public string mem = "@";
            public string par = "(";
            public string ens = ")";
            public string asc = "{0}";
            public string ud = "{0}";
            public string[] u = new string[BITS + 1];
            public string[] n = new string[BITS + 1];
        }

        static FMT fmt = new FMT();


        ///* address mapping */

        class RANGE : IComparable
        {
            public UInt64 start;
            public UInt64 end;
            public DATA data;
            public ulong fileoffset;

            public RANGE(UInt64 _start, UInt64 _end, DATA _data)
            {
                start = _start;
                end = _end;
                data = _data;
                fileoffset = 0;
            }

            public RANGE(UInt64 _start, UInt64 _end, ulong _offset)
            {
                start = _start;
                end = _end;
                data = null;
                fileoffset = _offset;
            }

            public int CompareTo(object obj)
            {
                var o = obj as RANGE;
                if (o != null)
                {
                    if (start == o.start)
                        return 0;
                    else if (start > o.start)
                        return 1;
                    else
                        return -1;
                }

                throw new NotImplementedException();
            }
        }

        
        class MAP
        {
            int opt;
            string name;

            public SortedSet<RANGE> list;

            public MAP(int _opt, string _name)
            {
                opt = _opt;
                name = _name;
                list = new SortedSet<RANGE>();
            }

            public void Add(RANGE r)
            {
                list.Add(r);
            }
        }






        enum MemType
        {
            MEMTYPE_NONE = 0,
            MEMTYPE_UNKNOWN = 1,
            MEMTYPE_CODE = 2,
            MEMTYPE_DATA = 3,
        }

        class DATA
        {
            public List<SpecType> spec = new List<SpecType>();
            public MemType mem = MemType.MEMTYPE_UNKNOWN;
            public int len = 0;

            public string MemName()
            {
                StringBuilder sb = new StringBuilder();

                switch (mem)
                {
                    case MemType.MEMTYPE_NONE: return "NONE";
                    case MemType.MEMTYPE_UNKNOWN: return "UNKNOWN";
                    case MemType.MEMTYPE_CODE: return "CODE";
                    case MemType.MEMTYPE_DATA:
                        sb.Append("DATA:");

                        foreach (var s in spec)
                        {
                            switch (s)
                            {
                                case SpecType.MD_LONG: sb.Append("L"); break;
                                case SpecType.MD_LONGNUM: sb.Append("N"); break;
                                case SpecType.MD_RATIONAL: sb.Append("R"); break;
                                case SpecType.MD_VECTOR: sb.Append("V"); break;
                                case SpecType.MD_WORD: sb.Append("W"); break;
                                default: sb.Append("?"); break;
                            }
                        }
                        return sb.ToString();
                    //default:
                    //    return string.Format("?0x{0:x}?", n);
                }

                throw new Exception("shouldn't be here");
                // return "Error";
            }
        }
        
        


        ///* disassembly */
        [Flags]
        enum DF
        {
            FLOW = 0x01,
            BREAK = 0x02,
            DELAY = 0x04,
            BRANCH = 0x10,
            JUMP = 0x20,
            CALL = 0x40,
            RETURN = 0x80,

            TO_KEEP = (FLOW | BRANCH | JUMP | CALL | RETURN),
            TO_COPY = (DELAY),
            TO_DELAY = (BREAK),
        }

        class DIS
        {
            public DF flags;
            public ulong pc;
            public int[] data = new int[3];
            public int n;        /* number of data[] words */
            public int w;        /* width of x in bits */
            public int c;        /* coprocessor operation */
            public Reg i;        /* Ri/Rs operand */
            public Reg j;        /* Rj operand */
            public ulong x;        /* constant operand */
            public OPCODE opcode = null;   /* decoded opcode */
            public string opnds;    /* formatted operand list */
            public string comment;  /* optional comment */
            public RANGE memrange; /* memory range */

            public DIS(ulong _pc, RANGE _mr)
            {
                flags = 0;
                pc = _pc;
                data[0] = data[1] = data[2] = 0xDEAD;
                n = 0;
                w = 0;
                c = 0;
                i = Reg.NOREG;
                j = Reg.NOREG;
                x = 0;
                opnds = null;
                comment = null;
                memrange = _mr;
            }

            public void writedis(TextWriter tw)
            {
                tw.Write("{0:X8} ", pc);

                for (int ii = 0; ii < 3; ++ii)
                {
                    if (ii < n)
                        tw.Write(" {0:X4}", data[ii]);
                    else
                        tw.Write("     ");
                }
                if ((flags & DF.DELAY) != 0)
                    tw.Write("  {0,-12}  {1,-6} {2}", "", opcode.name, opnds);
                else
                    tw.Write("  {0,-12} {1,-7} {2}", "", opcode.name, opnds);

                if (comment != "")
                {
                    tw.Write("{0,22}", "; " + comment);
                }
                tw.WriteLine();
                if ((flags & DF.BREAK) != 0)
                    tw.WriteLine();
            }
        }

        class DISTATE
        {
            public DF flags = 0;
            public ulong pc = 0;
            public RANGE memrange = null;
            public uint rvalid = 0;
            public ulong[] reg = new ulong[32];

            public DISTATE(ulong start, RANGE mr)
            {
                pc = start;
                memrange = mr;
            }

            public bool OkReg(Reg n) { return ((((int)n) >= 0) && (((int)n) < 32)); }
            public bool IsValid(Reg n) { return OkReg(n) ? (rvalid & (1 << ((int)n))) != 0 : false; }
            public void SetValid(Reg n)
            {
                if (OkReg(n))
                    rvalid |= (uint)(1 << ((int)n));
            }
            public void SetInvalid(Reg n)
            {
                if (OkReg(n))
                    rvalid &= (uint)(~(1 << ((int)n)));
            }
        };

        ///* output options */
        [Flags]
        enum OutOpt : uint
        {
            OF_ALTREG = 0x00000001,     /* use AC, SP, FP for R13-R15 */
            OF_ALTDMOV = 0x00000002,    /* use LD/ST for some DMOV etc */
            OF_ALTSHIFT = 0x00000004,   /* use large constant for LSL2 etc */
            OF_ALTSTACK = 0x00000008,   /* use PUSH/POP */
            OF_ALTSPECIALS = 0x00000010,/* use special register operation */
            OF_CSTYLE = 0x00000100,     /* use C style operand syntax */
            OF_HEXDOLLAR = 0x00000200,  /* use $0 syntax for hexadecimal numbers */
            OF_FILEMAP = 0x00010000,    /* write file map */
            OF_MEMORYMAP = 0x00020000,  /* write memory map */
            OF_SYMTAB = 0x00040000,     /* write symbol table */
            OF_XREF = 0x00080000,       /* write cross reference */
            OF_VERBOSE = 0x01000000,    /* verbose messages */
            OF_DEBUG = 0x02000000,      /* debug disassembler miscellaneous */
            OF_INVERT = 0x80000000,     /* parsing: invert sense of flag */
        }



        const string cmdname = "dfr";
        const string version = "1.03";

        static string opt_infile = "";
        static string opt_outfile = "";
        static OutOpt opt_outflag = 0;
        static bool   opt_little  = false;
        static bool   opt_split   = false;

        static BinaryReader infp;
        static TextWriter outfp;

        static MAP filemap = new MAP('i', "File map");
        static MAP memmap = new MAP('m', "Memory map");
        static MAP rangemap = new MAP('d', "Selected ranges");

        static string starttime = "";


        ///* output */

        static bool openoutput(int pc, int usepc, string ext)
        {
            //    if (usepc)
            //        sprintf_s(outname, FILENAME_MAX, "%s-%08lX%s%s", outbase, pc, dot, ext);
            //    else
            //        sprintf_s(outname, FILENAME_MAX, "%s%s%s", outbase, dot, ext);
            //    if (outfp)
            //        fclose(outfp);

            string outname = Path.ChangeExtension(opt_infile, ext);
            outfp = new StreamWriter(outname);
            return true;
        }

        static void Info(string fmt, params object[] obj)
        {
            outfp.WriteLine(fmt, obj);

            if ((opt_outflag & OutOpt.OF_VERBOSE) != 0)
            {
                Console.WriteLine(fmt, obj);
            }
            Debug.WriteLine(fmt, obj);
        }

        static void Error(string fmt, params object[] obj)
        {
            Log("*****");
            Log(fmt, obj);
        }


        static void header()
        {
            outfp.WriteLine("DFR {0}", version);
            outfp.WriteLine("  Date:   {0}", starttime);
            outfp.WriteLine("  Input:  {0}", opt_infile);
            outfp.WriteLine("  Output: {0}", opt_outfile);
            outfp.WriteLine();
        }

        static Char Asc(int c)
        {
            c &= 0xFF;
            if (Char.IsPunctuation((Char)c) || Char.IsLetterOrDigit((Char)c))
                return (Char)c;
            else
                return '.';
        }



        static int getword(DIS dp) // this should be in the filemap class.
        {
            int a = infp.ReadByte();
            int b = infp.ReadByte();
            if (a < 0 || b < 0)
            {
                //TOOO
                //if (feof(infp))
                //    return -1;
                //return -(errno + 1);
            }

            dp.data[dp.n++] = opt_little ? (b << 8) | a : (a << 8) | b;
            return 0;
        }

        ///* address maps */

        static string memmapname(DATA n)
        {
            StringBuilder sb = new StringBuilder();

            switch (n.mem)
            {
                case MemType.MEMTYPE_NONE: return "NONE";
                case MemType.MEMTYPE_UNKNOWN: return "UNKNOWN";
                case MemType.MEMTYPE_CODE: return "CODE";
                case MemType.MEMTYPE_DATA:
                    sb.Append("DATA:");

                    foreach (var spec in n.spec)
                    {
                        switch (spec)
                        {
                            case SpecType.MD_LONG: sb.Append("L"); break;
                            case SpecType.MD_LONGNUM: sb.Append("N"); break;
                            case SpecType.MD_RATIONAL: sb.Append("R"); break;
                            case SpecType.MD_VECTOR: sb.Append("V"); break;
                            case SpecType.MD_WORD: sb.Append("W"); break;
                            default: sb.Append("?"); break;

                        }
                    }
                    return sb.ToString();

                default:
                    return string.Format("?0x{0:X}?", n);
            }
        }
    



        ///* instruction decoding */

        static void mkdecode( OPCODE[] big, OPCODE[] lit)
        {
            foreach(var op in lit)
            {
                int n = (~ op.mask) & 0xFFFF;
                for( int i = 0 ; i <= n ; i++)
                {
                    big[op.encoding | i] = op;
                }
            }
        }
             


        ///* input */

        static bool seekto(ulong start)
        {
            foreach (var frp in filemap.list)
            {
                if (start >= frp.start && start <= frp.end)
                {
                    long pos = (long)(frp.fileoffset + start - frp.start);
                    if (infp.BaseStream.Length < pos)
                    {
                        Error("0x{0:X8} (PC 0x{1:X8}) is beyond end of file", pos, start);
                        return true;
                    }
                    if (infp.BaseStream.Seek(pos, SeekOrigin.Begin) != pos)
                    {
                        Error("cannot seek to 0x{0:x8} (PC 0x{1:x8}): %s", pos, start);
                        return true;
                    }
                    return false; //success
                }
            }
            Error("0x{0:x8} is not mapped in the input file", start);
            return true;
        }

 


        ///* disassembly */

        static int disop(DISTATE sp, DIS dp)
        {
            int tmp;
            int w;
            int err;

            StringBuilder opndbuf = new StringBuilder();
            StringBuilder commbuf = new StringBuilder();

            var opnd = opndbuf;

            int inst = dp.data[0];

            dp.flags = sp.flags;
            sp.flags = 0;

            switch (dp.opcode.type)
            {
                case itype.TYPE_A:
                    dp.i = (Reg)(0xF & inst);
                    dp.j = (Reg)(0xF & (inst >> 4));
                    break;
                case itype.TYPE_B:
                    dp.i = (Reg)(0xF & inst);
                    dp.x = (ulong)(0xFF & (inst >> 4));
                    dp.w = 8;
                    break;
                case itype.TYPE_C:
                    dp.i = (Reg)(0xF & inst);
                    dp.x = (ulong)(0xF & (inst >> 4));
                    dp.w = 4;
                    break;
                case itype.TYPE_D:
                    dp.x = (ulong)(0xFF & inst);
                    dp.w = 8;
                    break;
                case itype.TYPE_E:
                    dp.i = (Reg)(0xF & inst);
                    break;
                case itype.TYPE_F:
                    dp.x = (ulong)(0x7FF & inst);
                    dp.w = 11;
                    break;
                case itype.TYPE_Z:
                    dp.j = (Reg)(0xF & (inst >> 4));
                    break;
                case itype.TYPE_W:
                    dp.x = (ulong)(inst);
                    dp.w = 16;
                    break;
            }

            foreach (char s in dp.opcode.format)
            {
                switch (s)
                {
                    case '#':
                        opnd.Append(fmt.imm);
                        break;
                    case '&':
                        opnd.Append(fmt.and);
                        break;
                    case '(':
                        opnd.Append(fmt.par);
                        break;
                    case ')':
                        opnd.Append(fmt.ens);
                        break;
                    case '+':
                        opnd.Append(fmt.inc);
                        break;
                    case ',':
                        opnd.Append(fmt.nxt);
                        break;
                    case '-':
                        opnd.Append(fmt.dec);
                        break;
                    case ';':
                        opnd = commbuf;
                        break;
                    case '@':
                        opnd.Append(fmt.mem);
                        break;
                    case '2':
                        dp.x <<= 1;
                        dp.w += 1;
                        break;
                    case '4':
                        dp.x <<= 2;
                        dp.w += 2;
                        break;
                    case 'A':
                        opnd.Append(Reg.AC);
                        break;
                    case 'C':
                        opnd.Append(Reg.CCR);
                        break;
                    case 'F':
                        opnd.Append(Reg.FP);
                        break;
                    case 'J':
                        if (sp.IsValid(dp.j))
                        {
                            dp.x = sp.reg[(int)dp.j];
                            dp.w = 32;
                        }
                        else
                        {
                            dp.x = 0;
                            dp.w = 0;
                        }
                        break;
                    case 'I':
                        if (sp.IsValid(dp.i))
                        {
                            dp.x = sp.reg[(int)dp.i];
                            dp.w = 32;
                        }
                        else
                        {
                            dp.x = 0;
                            dp.w = 0;
                        }
                        break;
                    case 'M':
                        opnd.Append(Reg.ILM);
                        break;
                    case 'P':
                        opnd.Append(Reg.PS);
                        break;
                    case 'S':
                        opnd.Append(Reg.SP);
                        break;
                    case 'X':
                        /* constant extension word */
                        if ((err = getword(dp)) != 0)
                            return err;
                        dp.x = ((ulong)(dp.x << 16)) + ((ulong)dp.data[dp.n - 1]);
                        dp.w += 16;
                        break;
                    case 'Y':
                        /* coprocessor extension word */
                        if ((err = getword(dp)) != 0)
                            return err;
                        tmp = dp.data[dp.n - 1];
                        dp.x = (ulong)dp.i;
                        dp.w = 4;
                        dp.c = 0xFF & (tmp >> 8);
                        dp.j = (Reg)(0x0F & (tmp >> 4));
                        dp.i = (Reg)(0x0F & (tmp));
                        break;
                    case 'a':
                        w = dp.w;
                        while (w >= 8)
                        {
                            w -= 8;

                            opnd.Append(Asc((int)(dp.x >> w)));
                        }
                        break;
                    case 'b':
                        /* shift2 */
                        dp.x += 16;
                        dp.w += 1;
                        break;
                    case 'c':
                        /* coprocessor operation */
                        opnd.AppendFormat(fmt.u[8], dp.c);
                        break;
                    case 'd':
                        /* unsigned decimal */
                        opnd.AppendFormat(fmt.ud, dp.x);
                        break;
                    case 'f':
                        w = dp.w >> 1;
                        
                        tmp = (int)(((1ul << w) - 1) & (dp.x >> w));
                        int tmq = (int)(((1ul << w) - 1) & dp.x);
                        if (tmq != 0)
                            opnd.AppendFormat("{0:g}", ((double)tmp) / ((double)tmq));
                        else
                            opnd.Append("NaN");

                            break;
                    case 'g':
                        dp.i += SPECIALS;
                        goto case 'i';
                    case 'h':
                        dp.j += SPECIALS;
                        goto case 'j';
                    case 'i':
                        opnd.Append((Reg)dp.i);
                        break;
                    case 'j':
                        opnd.Append((Reg)dp.j);
                        break;
                    case 'k':
                        dp.i += COPROCESSOR;
                        goto case 'i';
                    case 'l':
                        dp.j += COPROCESSOR;
                        goto case 'j';
                    case 'n':
                        /* negative constant */
                        opnd.AppendFormat(fmt.u[dp.w + 1], dp.x);
                        //TODO opnd.AppendFormat(fmt.n[dp.w + 1], ((1ul << (dp.w + 1)) - 1) & NEG(dp.w, (1ul << dp.w) | dp.x));
                        break;
                    case 'p':
                        /* pair */
                        w = dp.w >> 1;
                        opnd.AppendFormat(fmt.u[w], ((1ul << w) - 1) & (dp.x >> w));
                        opnd.Append(fmt.nxt);
                        opnd.AppendFormat(fmt.u[w], ((1ul << w) - 1) & dp.x);
                        break;
                    case 'q':
                        /* rational */
                        w = dp.w >> 1;
                        opnd.AppendFormat(fmt.ud, ((1ul << w) - 1) & (dp.x >> w));
                        opnd.Append("/");
                        opnd.AppendFormat(fmt.ud, ((1ul << w) - 1) & dp.x);
                        break;
                    case 'r':
                        /* relative */
                        dp.x = (ulong)((long)dp.pc + 2 + SignExtend(dp.w, (int)dp.x));
                        dp.w = 32;
                        break;
                    case 's':
                        /* signed constant */
                        if (IsNeg(dp.w, dp.x))
                        {
                            if ((opt_outflag & OutOpt.OF_CSTYLE) != 0 && (opnd[-1] == '+'))
                                opnd.Remove(opnd.Length - 1, 1);
                            opnd.AppendFormat(fmt.n[dp.w], NEG(dp.w, (long)dp.x));
                        }
                        else
                        {
                            opnd.AppendFormat(fmt.u[dp.w - 1], dp.x);
                        }
                        break;
                    case 'u':
                        /* unsigned constant */
                        opnd.AppendFormat(fmt.u[dp.w], dp.x);
                        break;
                    case 'v':
                        /* vector */
                        opnd.AppendFormat(fmt.u[8], 0xFF - (0xFF & ((dp.pc - dp.memrange.start) / 4)));
                        break;
                    case 'x':
                        dp.x |= 0x100;
                        break;
                    case 'y':
                        dp.c += 8;
                        goto case 'z'; /*FALLTHROUGH*/
                    case 'z':
                        /* register list */
                        opnd.Append(fmt.par);
                        bool first = true;
                        for (int i = 0; i < 8; ++i)
                        {
                            if ((dp.x & (uint)(1 << i)) != 0)
                            {
                                if (first)
                                    first = false;
                                else
                                    opnd.Append(",");
                                if ((dp.x & 0x100) != 0)
                                    opnd.Append((Reg)(dp.c + 7 - i));
                                else
                                    opnd.Append((Reg)(dp.c + i));

                            }
                        }
                        opnd.Append(fmt.ens);
                        break;
                    default:
                        opnd.Append(s);
                        break;
                }
            }



            Reg r = Reg.NOREG;
            DF dflags = 0;
            foreach (var s in dp.opcode.action)
            {
                switch (s)
                {
                    case '!':
                        /* jump */
                        dflags |= DF.FLOW | DF.BREAK | DF.BRANCH;
                        break;
                    case '?':
                        /* branch */
                        dflags |= DF.FLOW | DF.BRANCH;
                        break;
                    case '(':
                        /* call */
                        dflags |= DF.FLOW | DF.CALL;
                        //Debug.WriteLine("CALL {0:X8} {1:x8}", dp.x, dp.pc);
                        break;
                    case ')':
                        /* return */
                        dflags |= DF.FLOW | DF.BREAK | DF.CALL;
                        break;
                    case '_':
                        /* delay */
                        dflags |= DF.DELAY;
                        break;
                    case 'A':
                        r = Reg.AC;
                        break;
                    case 'C':
                        r = Reg.CCR;
                        break;
                    case 'F':
                        r = Reg.FP;
                        break;
                    case 'P':
                        r = Reg.PS;
                        break;
                    case 'S':
                        r = Reg.SP;
                        break;
                    case 'i':
                        r = dp.i;
                        break;
                    case 'j':
                        r = dp.j;
                        break;
                    case 'w':
                        sp.SetInvalid(r);
                        break;
                    case 'v':
                        if (sp.OkReg(r))
                        {
                            sp.SetValid(r);
                            sp.reg[(int)r] = dp.x;
                        }
                        break;
                    case 'x':
                        r = Reg.NOREG;
                        break;
                    default:
                        Error("bad action '{0}'", s);
                        break;
                }
            }

            dp.flags |= dflags & DF.TO_KEEP;
            sp.flags |= dflags & DF.TO_COPY;
            if ((dflags & DF.DELAY) != 0)
                sp.flags |= dflags & DF.TO_DELAY;
            else
                dp.flags |= dflags & DF.TO_DELAY;

            /*XXX*/
            dp.opnds = opndbuf.ToString();
            if (commbuf.Length >0 && dp.memrange.data.mem == MemType.MEMTYPE_UNKNOWN)
            {
                //opnd = commbuf;
                for (int i = 0; i < dp.n; ++i)
                {
                    commbuf.Append(Asc(dp.data[i] >> 8));
                    commbuf.Append(Asc(dp.data[i]));
                }
            }
            dp.comment = commbuf.ToString();
            dp.writedis(outfp);


            return dp.n << 1;
        }

        static int discode(DISTATE sp)
        {
            DIS dp = new DIS(sp.pc, sp.memrange);
            int err = getword(dp);

            if (err != 0)
                return err;

            if ((dp.opcode = decode[dp.data[0]]) == null)
            {
                dp.opcode = opdata[(int)SpecType.MD_WORD];
            }

            int n = disop(sp, dp);
             return n;
        }

        static int disdata(DISTATE sp)
        {

            int n = 0;

            foreach (var spec in sp.memrange.data.spec)
            {
                int err;
                DIS dp = new DIS(sp.pc, sp.memrange);
                if ((err = getword(dp)) != 0)
                    return err;

                dp.x = (ulong)dp.data[0];
                dp.w = 16;
                dp.opcode = opdata[(int)spec];

                n += disop(sp, dp);
            }

            return n;
        }


        static ulong dis(ulong start, ulong end, RANGE drp, RANGE mrp)
        {
            DisFun disfn;

            if ((start & 1) != 0)
            {
                Error("Odd start address 0x{0:x8}", start);
                start &= 0xffFFffFFffFFffFe;
            }
            if ((end & 1) != 0)
            {
                end++;
            }

            switch (mrp.data.mem)
            {
                default:
                case MemType.MEMTYPE_NONE:
                    return end + 1;
                case MemType.MEMTYPE_UNKNOWN:
                case MemType.MEMTYPE_CODE:
                    disfn = discode;
                    break;

                case MemType.MEMTYPE_DATA:
                    disfn = disdata;
                    break;
            }

            if (seekto(start))
            {
                Debug.WriteLine("Seek failed");
                return end + 1;
            }

            DISTATE state = new DISTATE(start, mrp);

            while (state.pc < end)
            {
                int n;
                if ((n = disfn(state)) < 0)
                {
                    if (n != -1)
                        Error("input error: {0}", (-n - 1));
                    drp.end = state.pc;
                    state.pc = 0xFFffFFffFFffFFff;
                    break;
                }
                state.pc += (uint)n;
            }
            return state.pc;
        }

        static int process()
        {
            foreach (var b in memmap.list)
            {
                // find file offset covering this memory location.
                RANGE file = null;
                foreach (var f in filemap.list)
                {
                    if (b.start >= f.start && b.start <= f.end)
                    {
                        file = f;
                        break;
                    }
                }

                if (file != null)
                {
                    Info("Disassemble 0x{0:X8}-0x{1:X8} (file 0x{2:X8}) as {3}",
                        b.start, b.end, file.fileoffset + b.start - file.start, b.data.MemName());
                    Info("");
                    dis(b.start, b.end, file, b);
                    Info("");
                }
            }

            infp.Close();
            infp.Dispose();
            return 0;
        }


        ///* initialization */

        static void initialize()
        {
            starttime = DateTime.Now.ToString();

            if (opt_infile.Length == 0)
            {
                Log("{0}: no input file\n", cmdname);
                usage();
                Thread.CurrentThread.Abort();
            }

            infp = new BinaryReader(File.Open(opt_infile, FileMode.Open, FileAccess.Read, FileShare.Read));


            /* opcode decoding */
            mkdecode(decode, opcode);
            if ((opt_outflag & OutOpt.OF_ALTSTACK) != 0)
                mkdecode(decode, opaltstack);
            if ((opt_outflag & OutOpt.OF_ALTSHIFT) != 0)
                mkdecode(decode, opaltshift);
            if ((opt_outflag & OutOpt.OF_ALTDMOV) != 0)
                mkdecode(decode, opaltdmov);
            if ((opt_outflag & OutOpt.OF_ALTSPECIALS) != 0)
                mkdecode(decode, opaltspecial);

            /* format strings */
            fmt.u[0] = "";
            fmt.n[0] = "";
            for (int n = 1; n <= BITS; n++)
            {
                int w = 1 + (n - 1) / 4;
                fmt.u[n] = string.Format("{0}{{0:X{1}}}", (opt_outflag & OutOpt.OF_HEXDOLLAR) != 0 ? "$" : "0x", w);
                fmt.n[n] = string.Format("-{0}{{0:X{1}}}", (opt_outflag & OutOpt.OF_HEXDOLLAR) != 0 ? "$" : "0x", w);
            }
            //    if (opt_outflag & OF_CSTYLE) {
            //        fmt.imm = "";
            //        fmt.and = "+";
            //        fmt.inc = "++";
            //        fmt.dec = "--";
            //        fmt.mem = "*";
            //    }
            //    if (opt_outflag & OF_ALTREG) {
            //        reg[AC] = "AC";
            //        reg[FP] = "FP";
            //        reg[SP] = "SP";
            //    }

            //if ((opt_outflag & (OutOpt.OF_FILEMAP | OutOpt.OF_MEMORYMAP) )!= 0)
            //{
            if (openoutput(0, 0, /*opt_split ? "map" :*/ "asm"))
            {
                header();
            }
            //}

            //    fixmap(&filemap, 0);
            //    if (opt_outflag & OF_FILEMAP)
            //        dumpmap(&filemap);

            //    fixmap(&memmap, MEMTYPE_UNKNOWN);
            //    fillmap(&memmap, MEMTYPE_UNKNOWN);
            //    delmap(&memmap, MEMTYPE_NONE);
            //    if (opt_outflag & OF_MEMORYMAP)
            //        dumpmap(&memmap);

            //    fixmap(&rangemap, 1);
            //    if (opt_outflag & (OF_FILEMAP|OF_MEMORYMAP))
            //        dumpmap(&rangemap);
        }

        static void finalize()
        {
            infp.Close();
            infp.Dispose();
            outfp.Close();
            outfp.Dispose();
        }


        ///* options */
  
    }
}
