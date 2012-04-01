using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Diagnostics;
using System.Collections;
using System.Drawing;
using System.Drawing.Imaging;

namespace Nikon_Decode
{
    partial class Program
    {
        const long MaxIndex = 0x8b1;
        const long EngTableAddr = 0x5EA9A0;
        const long EngPlaybackTextAddr = 0x5EB6FC;
        const long EngLastAddr = 0x5ECC64;

        private static Dictionary<long, Struct6> menus;

        private static void DumpMenus5100(string fileName)
        {
            LoadFuncNames(@"C:\Users\spilgrim\Downloads\FrEmu\dfr_sim.txt");


            if (File.Exists(fileName))
            {
                BinaryReader br = null;

                byte[] data;

                using (br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite)))
                {
                    data = br.ReadBytes((int)br.BaseStream.Length);
                }


                Queue<long> q = new Queue<long>();
                List<long> resolved = new List<long>();
                menus = new Dictionary<long, Struct6>();

                resolved.Add(0);

                long[] startList = { 0x8F9CA140, 0x8F9CE6A0, 0x8F9C8F50, 0x8F9CFBC0, 0x8F9CD060, 0x8F9CE870, 
                                     0x8F9CC210, 0x8F9CA060, 0x8F9C9FB0, 0x8F9CCF70, 0x8F9CE700, 0x8F9CF7F0, 
                                     0x8F9CA120, 0x8F9CA020, 0x8F9C9F30, 0x8F9C9D20, 0x8F9C9C10, 0x8F9C9B90,
                                     0x8F9C9B20, 0x8F9C9AA0, 0x8F9C9860, 0x8F9C9ED0, 0x8F9C9C90, 0x8F9C9800,
                                     0x8F9C97C0, 0x8F9C9750, 0x8F9C96D0, 0x8F9C9650, 0x8F9C8DB0, 0x8F9C8CD0,
                                     0x8F9C8A30, 0x8F9C9560, 0x8F9C95E0, 0x8F9CC120, 0x8F9C8540, 0x8F9C8DF0, 
                                     0x8F9C8420, 0x8F9C7C70, 0x8F9C7A30, 0x8F9C7A60, 0x8F9C72A0, 0x8F9C71E0,
                                     0x8F9C7190, 0x8F9C6F90, 0x8F9C6F70, 0x8F9C6F20, 0x8F9C6EE0, 0x8F9C6EA0,
                                     0x8F9C6E80, 0x8F9C6E60, 0x8F9C6E40, 0x8F9C6E20, 0x8F9C6E00, 0x8F9C6DE0,
                                     0x8F9C6DC0};


                uint addrOffset = (0x8F9C4E78 - 0x37BDE4) + 0x40000;

                foreach (var l in startList)
                {
                    q.Enqueue(l);
                }
                

                while (q.Count > 0)
                {
                    var addr = q.Dequeue();

                    var s6 = new Struct6(data, addr, addrOffset);
                    s6.ReadElements(data);

                    resolved.Add(addr);
                    menus.Add(addr, s6);
                    foreach (var s14 in s6.menu_elements)
                    {
                        if (Array.IndexOf(startList, s14.menu_ptr) != -1)
                        {

                        }
                        if (resolved.Contains(s14.menu_ptr) == false &&
                            q.Contains(s14.menu_ptr) == false)
                        {
                            q.Enqueue(s14.menu_ptr);   
                        }
                    }

                }

                using (var sw = new StreamWriter(File.Open(fileName + ".menu.txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite)))
                {
                    using (var sw2 = new StreamWriter(File.Open(fileName + ".menu_sym.txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite)))
                    {
                        foreach (var l in startList)
                        {
                            MenuDump(sw, sw2, "", l);
                        }
                    }
                }
                
            }      
        }

        private static void MenuDump(TextWriter tw_txt, TextWriter tw_sym, string p, long addr)
        {
            Struct6 menu;

            if (menus.TryGetValue(addr, out menu))
            {
                menu.Dump(tw_txt, tw_sym, p);
            }
        }


        class Struct6
        {
            public Struct6(byte[] data, long startAddr, long addrOffset)
            {
                mem_loc = startAddr;
                addr_off = addrOffset;
                file_loc = startAddr - addrOffset;

                Read(data, mem_loc, addrOffset);

                headingTxt = ResolveString(data, field_0, EngPlaybackTextAddr);

            }

            public void Read(byte[] data, long startAddr, long addrOffset)
            {
                field_0 = ReadUint16(data, file_loc);
                field_2 = ReadUint16(data, file_loc + 0x02);
                field_4 = ReadUint16(data, file_loc + 0x04);
                field_6 = ReadUint16(data, file_loc + 0x06);
                field_8 = ReadUint16(data, file_loc + 0x08);
                field_A = ReadUint16(data, file_loc + 0x0A);
                field_C = ReadUint16(data, file_loc + 0x0C);
                field_E = ReadUint16(data, file_loc + 0x0E);
                field_10 = ReadUint16(data, file_loc + 0x10);
                field_12 = ReadUint16(data, file_loc + 0x12);
                field_14 = ReadUint32(data, file_loc + 0x14);
                field_18 = ReadUint32(data, file_loc + 0x18);
                field_1C = ReadUint32(data, file_loc + 0x1C);

                if (field_12 < Unknown_xx.table_count)
                {
                    field_12_item = new Unknown_xx(data, field_12, addrOffset);
                }        
            }

            public void ReadElements(byte[] data)
            {
                for (int i = 0; i < field_A; i++)
                {
                    var s14 = new Struct14(data, field_1C + (i * Struct14.size_of), addr_off);

                    menu_elements.Add(s14);
                }
            }

            public const int size_of = 0x20;

            // (sizeof=0x20)
            public long mem_loc;
            public long file_loc;
            public long addr_off;
            string headingTxt;
            public List<Struct14> menu_elements = new List<Struct14>();
            Unknown_xx field_12_item = null;

            public UInt16 field_0; //  Language Table Index (0x6xxx)
            public UInt16 field_2; //
            public UInt16 field_4; //
            public UInt16 field_6; //
            public UInt16 field_8; //
            public UInt16 field_A; //       
            public UInt16 field_C; //
            public UInt16 field_E; //
            public UInt16 field_10; //
            public UInt16 field_12; // BigFuncTable Index
            public UInt32 field_14; // Parents menu
            public UInt32 field_18; // WORD* - list count
            public UInt32 field_1C; // elements_addr

            internal void Dump(TextWriter tw, TextWriter tw_sym, string p)
            {
                tw.WriteLine("{0}Menu: 0x{1:X8} {2}", p, mem_loc, headingTxt);

                tw.WriteLine("{0}  00 field_00: 0x{1:X4}", p, field_0);
                tw.WriteLine("{0}  02 field_02: 0x{1:X4}", p, field_2);
                tw.WriteLine("{0}  04 field_04: 0x{1:X4}", p, field_4);
                tw.WriteLine("{0}  06 field_06: 0x{1:X4}", p, field_6);
                tw.WriteLine("{0}  08 field_08: 0x{1:X4} - 'Num Fixed Elements'", p, field_8);
                //tw.WriteLine("{0}  0A Total Elements: 0x{1:X4}", p, field_A);
                tw.WriteLine("{0}  0C field_0C: 0x{1:X4}", p, field_C);
                tw.WriteLine("{0}  0E field_0E: 0x{1:X4}", p, field_E);
                tw.WriteLine("{0}  10 field_10: 0x{1:X4}", p, field_10);
                tw.WriteLine("{0}  12 field_12: 0x{1:X4}", p, field_12);

                //tw.WriteLine("{0}  14 parentAddr  : 0x{1:X8}", p, field_14);
                tw.WriteLine("{0}  18 field_18: 0x{1:X8}", p, field_18);
                //tw.WriteLine("{0}  1C elementsAddr: 0x{1:X8}", p, field_1C);

                var sym = NameToSymbol(headingTxt);
                if (sym != "")
                {
                    tw_sym.WriteLine("-s 0x{0:X8}=MN_{1}", mem_loc, sym);
                }

                if (field_12_item != null)
                {
                    field_12_item.Dump(tw, p);
                }

                foreach (var el in menu_elements)
                {
                    el.Dump(tw, tw_sym, p);
                }

                //tw.WriteLine();
            }
        }

        internal class Unknown_xx
        {
            const long table_start = 0x8F9D1934;
            const long table_end = 0x8F9D555C;
            public const long table_count = (table_end - table_start) / size_of;
            const long size_of = 0x2C;
            long mem_loc;
            long file_loc;

            public Unknown_xx(byte[] data, int index, long addrOffset)
            {
                long startAddr = ((index * size_of) + table_start);

                mem_loc = startAddr;
                file_loc = startAddr - addrOffset;

                Read(data, mem_loc, file_loc);
            }

            UInt16 field_0;
            UInt16 field_2;
            UInt32 field_4;
            UInt32 field_8;
            UInt32 field_C;
            UInt32 field_10;
            UInt32 field_14;
            UInt32 field_18;
            UInt32 field_1C;
            UInt32 field_20;
            UInt32 field_24;
            UInt32 field_28;

            public void Read(byte[] data, long startAddr, long addrOffset)
            {
                field_0 = ReadUint16(data, file_loc);
                field_2 = ReadUint16(data, file_loc + 0x02);
                field_4 = ReadUint32(data, file_loc + 0x04);
                field_8 = ReadUint32(data, file_loc + 0x08);
                field_C = ReadUint32(data, file_loc + 0x0C);
                field_10 = ReadUint32(data, file_loc + 0x10);
                field_14 = ReadUint32(data, file_loc + 0x14);
                field_18 = ReadUint32(data, file_loc + 0x18);
                field_1C = ReadUint32(data, file_loc + 0x1C);
                field_20 = ReadUint32(data, file_loc + 0x20);
                field_24 = ReadUint32(data, file_loc + 0x24);
                field_28 = ReadUint32(data, file_loc + 0x28);
            }

            internal void Dump(TextWriter tw, string p)
            {
                tw.WriteLine("{0}BigFuncTable: 0x{1:X8} {2}", p, mem_loc, (mem_loc - table_start) / size_of);
                tw.WriteLine("{0}  field_00: 0x{1:X4}", p, field_0);
                tw.WriteLine("{0}  field_02: 0x{1:X4}", p, field_2);
                tw.WriteLine("{0}  field_04: 0x{1:X8} - {2}", p, field_4, ResolveFuncName(field_4));
                tw.WriteLine("{0}  field_08: 0x{1:X8} - {2}", p, field_8, ResolveFuncName(field_8));
                tw.WriteLine("{0}  field_0C: 0x{1:X8} - {2}", p, field_C, ResolveFuncName(field_C));
                tw.WriteLine("{0}  field_10: 0x{1:X8} - {2}", p, field_10, ResolveFuncName(field_10));
                tw.WriteLine("{0}  field_14: 0x{1:X8} - {2}", p, field_14, ResolveFuncName(field_14));
                tw.WriteLine("{0}  field_18: 0x{1:X8} - {2}", p, field_18, ResolveFuncName(field_18));
                tw.WriteLine("{0}  field_1C: 0x{1:X8} - {2}", p, field_1C, ResolveFuncName(field_1C));
                tw.WriteLine("{0}  field_20: 0x{1:X8} - {2}", p, field_20, ResolveFuncName(field_20));
                tw.WriteLine("{0}  field_24: 0x{1:X8} - {2}", p, field_24, ResolveFuncName(field_24));
                tw.WriteLine("{0}  field_28: 0x{1:X8} - {2}", p, field_28, ResolveFuncName(field_28));
            }
        }

        class Struct14
        {
            public const int size_of = 0x10;

            public Struct14(byte[] data, long startAddr, long addrOffset)
            {
                mem_loc = startAddr;
                file_loc = startAddr - addrOffset;

                Read(data, mem_loc, file_loc);

                //txt_0 = ResolveString(data, field_0, EngPlaybackTextAddr);
                txt_2 = ResolveString(data, field_2, EngPlaybackTextAddr);
            }

            public void Read(byte[] data, long startAddr, long addrOffset)
            {
                field_0 = ReadUint16(data, file_loc);
                field_2 = ReadUint16(data, file_loc + 0x02);
                field_4 = ReadUint16(data, file_loc + 0x04);
                field_6 = ReadUint16(data, file_loc + 0x06);
                field_8 = ReadUint16(data, file_loc + 0x08);
                field_A = ReadUint16(data, file_loc + 0x0A);
                menu_ptr = ReadUint32(data, file_loc + 0x0C);
            }

            long mem_loc;
            long file_loc;
            string txt_0;
            string txt_2;

            public UInt16 field_0;
            UInt16 field_2;
            UInt16 field_4;
            UInt16 field_6;
            UInt16 field_8;
            UInt16 field_A;
            public UInt32 menu_ptr; // 0x0c - Sub-Menu

            public override string ToString()
            {
                return string.Format("{0} - {1}", txt_0, txt_2);
            }

            internal void Dump(TextWriter tw, TextWriter tw_sym, string p)
            {
                tw.WriteLine("{0}Element: 0x{1:X8} {2}", p, mem_loc, txt_2);
                //tw.WriteLine("{0}  field_0: 0x{1:X4}", p, field_0);
                //tw.WriteLine("{0}  field_2: 0x{1:X4}", p, field_2);
                //tw.WriteLine("{0}  field_4: 0x{1:X4}", p, field_4);
                //tw.WriteLine("{0}  field_6: 0x{1:X4}", p, field_6);
                //tw.WriteLine("{0}  field_8: 0x{1:X4}", p, field_8);
                //tw.WriteLine("{0}  field_A: 0x{1:X4}", p, field_A);
                //tw.WriteLine("{0}  menu_ptr: 0x{1:X8}", p, menu_ptr);

                var sym = NameToSymbol(txt_2);
                if (sym != "")
                {
                    tw_sym.WriteLine("-s 0x{0:X8}=ME_{1}", mem_loc, sym);
                }

                if (menu_ptr != 0)
                {
                    MenuDump(tw, tw_sym, p + "  ", menu_ptr);
                }
            }
        }

        static Dictionary<long, string> FuncNames = new Dictionary<long,string>();

        static void LoadFuncNames(string dfr_file)
        {
            using (var sr = new StreamReader(File.Open(dfr_file, FileMode.Open, FileAccess.Read, FileShare.ReadWrite)))
            {
                while (sr.EndOfStream == false)
                {
                    var line = sr.ReadLine();

                    if (line.Length > 2 && line[0] == '-' && line[1] == 's')
                    {
                        int x_idx = line.IndexOf('x');
                        int eq_idx = line.IndexOf('=');
                        var hex = line.Substring(x_idx + 1, eq_idx - x_idx - 1);
                        var name = line.Substring(eq_idx + 1);
                        var addr = long.Parse(hex, System.Globalization.NumberStyles.HexNumber);

                        FuncNames.Add(addr, name);
                        //Console.WriteLine("{0:X} {1:X}", hex, addr);

                        
                    }
                }
            }

        }

        static List<string> Symbols = new List<string>();
        static string NameToSymbol(string text)
        {
            const int maxIn = 14;
            StringBuilder sb = new StringBuilder();

            char[] skips = { ' ', '/', '-', '(', ')' };

            for (int i = 0; i < text.Length && sb.Length < maxIn; i++)
            {
                //if (Array.IndexOf(skips, text[i]) == -1)
                char c = text[i];
                if ((c >= '0' && c <= '9') ||
                    (c >= 'a' && c <= 'z') ||
                    (c >= 'A' && c <= 'Z'))
                {
                    sb.Append(text[i]);
                }
            }

            if (sb.Length == 0) sb.Append("empty_");

            string basetxt = sb.ToString();
            string trytxt = basetxt;
            int next = 0;
            while (Symbols.Contains(trytxt))
            {
                trytxt = string.Format("{0}_{1}", basetxt, next++);
            }

            Symbols.Add(trytxt);
            return trytxt;
        }

        static string ResolveFuncName(uint addr)
        {
            if (addr == 0)
                return "null";

            if (FuncNames.ContainsKey(addr))
            {
                return FuncNames[addr];
            }

            return string.Format("sub_{0:X}", addr);
        }

        static string ResolveString(byte[] data, long offset, long baseAddr)
        {
            if (offset == 0xffff) return "";

            long masked = offset & 0xE000;
            offset = offset & 0x1FFF;

            switch (masked)
            {
                case 0x8000:
                    baseAddr = EngTableAddr;
                    break;

                case 0x0000:
                default:
                    baseAddr = EngPlaybackTextAddr;
                    break;
            }

            if ((baseAddr + (offset * 4)) > EngLastAddr)
            {

            }
            long addr = baseAddr + (offset * 4) - 0x40000;

            long saddr = ReadUint32(data, addr) - 0x40000;
            long eaddr = ReadUint32(data, addr + 4) - 0x40000;

            StringBuilder sb = new StringBuilder();
            int state = 0;
            for (long i = saddr; i < eaddr; i++)
            {
                if (state == 0 && (data[i] < 0x20 || data[i] > 0x7F))
                {
                    state = 1;
                    sb.AppendFormat("/0x{0:X2}", data[i]);
                }
                else if (state == 1)
                {
                    sb.AppendFormat("/0x{0:X2}", data[i]);
                }
                else
                {
                    sb.Append((char)data[i]);
                }
            }

            return sb.ToString();
        }
    }
}
