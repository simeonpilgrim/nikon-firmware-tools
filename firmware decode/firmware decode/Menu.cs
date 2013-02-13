using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Diagnostics;
using System.Collections;
using System.Drawing;
using System.Drawing.Imaging;
using System.Globalization;

namespace Nikon_Decode
{
    partial class Program
    {
        public const int OUTPUT = 1; // 0 DFR, 1 IDA make, 2 IDA clean

        private static Dictionary<long, Struct6> menus;
        static Dictionary<long, Struct14> elements;

        internal class FirmConsts
        {
            public uint BFT_start;
            public uint BFT_end;
            public long BFT_Count { get { return (BFT_end - BFT_start) / Unknown_xx.size_of; } }
            public uint File_offset;

            public long EngTableAddr;
            public long EngLastAddr;
            public long EngMenuTextAddr;
            public long EngDailTextAddr;
            public long EngHelpTextAddr;

            public uint Copy_From;
            public uint Copy_To;
            public uint Copy_Offset { get { return (Copy_To - Copy_From) + File_offset; } }

            public string DFR_file;

            public long[] MenuRootList = {};
        }

        internal static FirmConsts firmConsts = null;

        internal class D5100_0101_Const : FirmConsts
        {
            public D5100_0101_Const()
            {
                BFT_start = 0x8F9D1934;
                BFT_end = 0x8F9D555C;
                File_offset = 0x40000;

                EngTableAddr = 0x5EA9A0;
                EngLastAddr = 0x5ECC64;
                EngMenuTextAddr = 0x5EB6FC;
                EngHelpTextAddr = 0x5EB324;
                EngDailTextAddr = 0x5EB324;

                Copy_From = 0x37BDE4;
                Copy_To = 0x8F9C4E78;

                DFR_file = @"C:\Users\spilgrim\Downloads\FrEmu\b640101b.dfr.txt";

                MenuRootList = new long[] { 0x8F9CA140, 0x8F9CE6A0, 0x8F9C8F50, 0x8F9CFBC0, 0x8F9CD060, 0x8F9CE870, 
                                     0x8F9CC210, 0x8F9CA060, 0x8F9C9FB0, 0x8F9CCF70, 0x8F9CE700, 0x8F9CF7F0, 
                                     0x8F9CA120, 0x8F9CA020, 0x8F9C9F30, 0x8F9C9D20, 0x8F9C9C10, 0x8F9C9B90,
                                     0x8F9C9B20, 0x8F9C9AA0, 0x8F9C9860, 0x8F9C9ED0, 0x8F9C9C90, 0x8F9C9800,
                                     0x8F9C97C0, 0x8F9C9750, 0x8F9C96D0, 0x8F9C9650, 0x8F9C8DB0, 0x8F9C8CD0,
                                     0x8F9C8A30, 0x8F9C9560, 0x8F9C95E0, 0x8F9CC120, 0x8F9C8540, 0x8F9C8DF0, 
                                     0x8F9C8420, 0x8F9C7C70, 0x8F9C7A30, 0x8F9C7A60, 0x8F9C72A0, 0x8F9C71E0,
                                     0x8F9C7190, 0x8F9C6F90, 0x8F9C6F70, 0x8F9C6F20, 0x8F9C6EE0, 0x8F9C6EA0,
                                     0x8F9C6E80, 0x8F9C6E60, 0x8F9C6E40, 0x8F9C6E20, 0x8F9C6E00, 0x8F9C6DE0,
                                     0x8F9C6DC0};
            }
        }

        internal class D3S_0101_Const : FirmConsts
        {
            public D3S_0101_Const()
            {
                BFT_start = 0; // not known yet.
                BFT_end = 0; // not known yet.
                File_offset = 0x40000;

                EngTableAddr = 0x46ABC8;
                EngMenuTextAddr = 0x46B564;
                EngLastAddr = 0x46D3D0;

                Copy_From = 0x2FF8A4;
                Copy_To = 0x877DD99C;

                DFR_file = @"";

                MenuRootList = new long[] { 0x877E8348, 0x877E8CB8, 0x877EA0A8, 0x877EA088, 0x877ECAD8,
                                0x877ECA28, 0x877EF9C8, 0x877F1868, 0x877F2E68, 0x877EF908, 0x877EF818, 
                                0x877EF578, 0x877EF348, 0x877EF1E8, 0x877EF158, 0x877EF0B8, 0x877EEE48, 
                                0x877EEF18, 0x877EEEB8, 0x877EEED8, 0x877E9C98, 0x877E9BE8, 0x877E9B08, 
                                0x877E9A58, 0x877E9948, 0x877E9878, 0x877E9728, 0x877E96E8, 0x877E9678, 
                                0x877E95F8, 0x877E9598, 0x877E93E8, 0x877E8FB8, 0x877E9168, 0x877E91E8, 
                                0x877E9258, 0x877E92D8, 0x877E8F58, 0x877E8F18, 0x877E8E28, 0x877E8EA8, 
                                0x877E8DA8, 0x877E8D38, 0x877E9358, 0x877E8C78, 0x877E81C8, 0x877E8168,
                                0x877E8128, 0x877E8018, 0x877E7E78, 0x877E7EC8, 0x877E7F78, 0x877E7CB8, 
                                0x877E7D18, 0x877E7D38, 0x877E7BB8, 0x877E7BD8, 0x877E7BF8, 0x877E7C18, 
                                0x877E7C38, 0x877E7C58, 0x877E7C78, 0x877E7C98, 0x877E7CD8, 0x877E7CF8 };
            }
        }

        internal class D7000_0103_Const : FirmConsts
        {
            public D7000_0103_Const()
            {
                BFT_start = 0x8F9BF0E8;
                BFT_end = 0x8F9C4470;
                File_offset = 0x40000;

                EngTableAddr = 0x52A790;
                EngMenuTextAddr = 0x52B484;
                EngLastAddr = 0x52D49C;

                Copy_From = 0x367E74;
                Copy_To = 0x8F9ACE44;

                DFR_file = @"";

                MenuRootList = new long[] { 0x8F9B5518, 0x8F9BBB48, 0x8F9BB608, 0x8F9BA338, 0x8F9BA0D8, 
                                0x8F9B82A8, 0x8F9BA158, 0x8F9B7448, 0x8F9B74E8, 0x8F9B7488, 0x8F9B74A8, 
                                0x8F9B7688, 0x8F9B7728, 0x8F9B77B8, 0x8F9B80E8, 0x8F9B81D8, 0x8F9B7E48, 
                                0x8F9B7948, 0x8F9B52F8, 0x8F9B2CD8, 0x8F9B2CB8, 0x8F9B2C18, 0x8F9B2AE8, 
                                0x8F9B2AA8, 0x8F9B2A38, 0x8F9B29B8, 0x8F9B2958, 0x8F9B27A8, 0x8F9B2718, 
                                0x8F9B2698, 0x8F9B2618, 0x8F9B25A8, 0x8F9B2528, 0x8F9B22E8, 0x8F9B2288,
                                0x8F9B2248, 0x8F9B21D8, 0x8F9B2158, 0x8F9B20D8, 0x8F9B2068, 0x8F9B1FE8, 
                                0x8F9B1368, 0x8F9B11E8, 0x8F9B1188, 0x8F9B1168, 0x8F9B1148, 0x8F9B1108,
                                0x8F9B0FC8, 0x8F9B0F28, 0x8F9B0758, 0x8F9B0588, 0x8F9B0558, 0x8F9AFDC8, 
                                0x8F9AFD08, 0x8F9AFCB8, 0x8F9AFAB8, 0x8F9AFA98, 0x8F9AFA48, 0x8F9AFA08,
                                0x8F9AF9E8, 0x8F9AF9C8 ,0x8F9AF9A8, 0x8F9AF988, 0x8F9AF968, 0x8F9AF948,
                                0x8F9AF928, 0x8F9AF908, 0x8F9AF8E8, 0x8F9AF8C8, 0x8F9AF8A8, 0x8F9AF888};
            }
        }

        internal class D300S_0101_Const : FirmConsts
        {
            public D300S_0101_Const()
            {
                BFT_start = 0x0;
                BFT_end = 0x0;
                File_offset = 0x40000;

                EngTableAddr = 0x465640;
                EngMenuTextAddr = 0x466130;
                EngLastAddr = 0x467E28;

                Copy_From = 0x2DB304;
                Copy_To = 0x9F92DCF8;

                DFR_file = @"";

                MenuRootList = new long[] { 0x9F9380B4, 0x9F938004, 0x9F937F14, 0x9F937ED4, 0x9F93A6B4, 
                                0x9F93A8D4, 0x9F937DE4, 0x9F937D84, 0x9F937BD4, 0x9F937B44, 0x9F940B94, 
                                0x9F9406E4, 0x9F9402A4, 0x9F93FE64, 0x9F93EE04, 0x9F93D674, 0x9F93D5D4,
                                0x9F93D244, 0x9F93D4E4, 0x9F93D014, 0x9F93EC04, 0x9F93EC64, 0x9F935E84,
                                0x9F935EA4, 0x9F935EC4, 0x9F935EE4, 0x9F935F04, 0x9F935F24, 0x9F935F44, 
                                0x9F935F64, 0x9F935F84, 0x9F935FA4, 0x9F935FC4, 0x9F935FE4, 0x9F936004, 
                                0x9F936024, 0x9F936044, 0x9F936184, 0x9F936234, 0x9F936284, 0x9F9362C4, 
                                0x9F936364, 0x9F9367A4, 0x9F936AF4, 0x9F937714, 0x9F937E64, 0x9F937AC4, 
                                0x9F937A44, 0x9F9379D4, 0x9F937954, 0x9F9373D4, 0x9F936974, 0x9F938094,
                                0x9F93CB14, 0x9F93CBE4, 0x9F93CD84, 0x9F93CE24, 0x9F93CEB4, 0x9F936914, 
                                0x9F9368D4, 0x9F936754};
            }
        }

        internal class D700_0103_Const : FirmConsts
        {
            public D700_0103_Const()
            {
                BFT_start = 0x0;
                BFT_end = 0x0;
                File_offset = 0x40000;

                EngTableAddr = 0x3FF7E0;
                EngMenuTextAddr = 0x400220;
                EngLastAddr = 0x401C20;

                Copy_From = 0x2AC724;
                Copy_To = 0x9F049094;;

                DFR_file = @"";

                MenuRootList = new long[] { 
                    0x9F057D00, 0x9F0594F0, 0x9F054E20, 0x9F059B10,
               

                    0x9F0573F0, 0x9F057180, 0x9F059A90, 0x9F057B50, 0x9F057C40, 0x9F0578B0, 0x9F057680, 
                    0x9F057490, 0x9F057520, 0x9F052710, 0x9F0526F0, 0x9F054C00, 0x9F0521E0, 0x9F052050,
                    0x9F051FA0, 0x9F051E80, 0x9F051DF0, 0x9F052130, 0x9F051D00, 0x9F051CC0, 0x9F0519C0,
                    0x9F051B70, 0x9F051BD0, 0x9F051C50, 0x9F051930, 0x9F0518B0, 0x9F0517C0, 0x9F051500,
                    0x9F0514A0, 0x9F051460, 0x9F0513F0, 0x9F051370, 0x9F0512F0, 0x9F051740, 0x9F051830,
                    0x9F051280, 0x9F051200, 0x9F0511C0,  

                    0x9F050C40, 0x9F050AC0, 0x9F050A00, 0x9F0509B0, 0x9F0506E0, 0x9F0506A0,
                    0x9F050660, 0x9F050680, 0x9F050640, 0x9F050620, 
                };
            }
        }

        private static void InteractiveTextD5100(string fileName)
        {
            if (File.Exists(fileName))
            {
                byte[] data;

                using (BinaryReader br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite)))
                {
                    data = br.ReadBytes((int)br.BaseStream.Length);
                }

                if (data != null)
                {

                    string line;
                    while ((line = Console.ReadLine()) != "")
                    {
                        long addr;
                        if (long.TryParse(line, NumberStyles.HexNumber, CultureInfo.InvariantCulture, out addr) )
                        {
                            Console.WriteLine("0x{0:X4} {1}", addr, ResolveString(data, addr, firmConsts.EngMenuTextAddr));
                        }
                    }
                }
            }
        }

        private static void DumpMenusD5100(string fileName)
        {
            firmConsts = new D5100_0101_Const();
            DumpMenus(fileName);
        }

        private static void DumpMenusD3s(string fileName)
        {
            firmConsts = new D3S_0101_Const();
            DumpMenus(fileName);
        }

        private static void DumpMenusD7000(string fileName)
        {
            firmConsts = new D7000_0103_Const();
            DumpMenus(fileName);
        }

        private static void DumpMenusD300S(string fileName)
        {
            firmConsts = new D300S_0101_Const();
            DumpMenus(fileName);
        }

        private static void DumpMenusD700(string fileName)
        {
            firmConsts = new D700_0103_Const();
            DumpMenus(fileName);
        }

        private static void DumpMenus(string fileName)
        {
            if (File.Exists(fileName))
            {
                LoadFuncNames(firmConsts.DFR_file);

                BinaryReader br = null;

                byte[] data;

                using (br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite)))
                {
                    data = br.ReadBytes((int)br.BaseStream.Length);
                }


                Queue<long> q = new Queue<long>();
                List<long> resolved = new List<long>();
                menus = new Dictionary<long, Struct6>();
                elements = new Dictionary<long, Struct14>();

                resolved.Add(0);


                uint addrOffset = firmConsts.Copy_Offset;

                foreach (var l in firmConsts.MenuRootList)
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
                        if (Array.IndexOf(firmConsts.MenuRootList, s14.menu_ptr) != -1)
                        {

                        }
                        if (resolved.Contains(s14.menu_ptr) == false &&
                            q.Contains(s14.menu_ptr) == false)
                        {
                            if (s14.menu_ptr < firmConsts.Copy_To)
                            {
                                int az = 0;
                            }
                            q.Enqueue(s14.menu_ptr);   
                        }
                    }

                }

                using (var sw = new StreamWriter(File.Open(fileName + ".menu.txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite)))
                {
                    using (var sw2 = new StreamWriter(File.Open(fileName + ".menu_sym.txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite)))
                    {
                        foreach (var l in firmConsts.MenuRootList)
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

        static void AddElement(long addr, Struct14 element)
        {
            if (elements.ContainsKey(addr) == false)
            {
                elements.Add(addr, element);
            }
        }

        public static string FindElementName(long memuAddr)
        {
            foreach (var el in elements)
            {
                if (el.Value.menu_ptr == memuAddr)
                    return el.Value.ToString();
            }

            return "";
        }


        class Struct6
        {
            public Struct6(byte[] data, long startAddr, long addrOffset)
            {
                mem_loc = startAddr;
                addr_off = addrOffset;
                file_loc = startAddr - addrOffset;

                Read(data, mem_loc, addrOffset);

                headingTxt = ResolveString(data, field_0, firmConsts.EngMenuTextAddr);

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

                if (field_12 < firmConsts.BFT_Count)
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
                string elemnt_name = Program.FindElementName(mem_loc);
                if (elemnt_name == "")
                    elemnt_name = headingTxt;

                //tw.WriteLine("{0}Menu: 0x{1:X8} {2}", p, mem_loc, headingTxt);
                tw.WriteLine("{0}Menu: 0x{1:X8} {2}", p, mem_loc, elemnt_name);

                //tw.WriteLine("{0}  00 field_00: 0x{1:X4}", p, field_0);
                //tw.WriteLine("{0}  02 field_02: 0x{1:X4}", p, field_2);
                //tw.WriteLine("{0}  04 field_04: 0x{1:X4}", p, field_4);
                //tw.WriteLine("{0}  06 field_06: 0x{1:X4}", p, field_6);
                //tw.WriteLine("{0}  08 field_08: 0x{1:X4} - 'Num Fixed Elements'", p, field_8);
                //tw.WriteLine("{0}  0A Total Elements: 0x{1:X4}", p, field_A);
                //tw.WriteLine("{0}  0C field_0C: 0x{1:X4}", p, field_C);
                //tw.WriteLine("{0}  0E field_0E: 0x{1:X4}", p, field_E);
                //tw.WriteLine("{0}  10 field_10: 0x{1:X4}", p, field_10);
                //tw.WriteLine("{0}  12 field_12: 0x{1:X4}", p, field_12);

                //tw.WriteLine("{0}  14 parentAddr  : 0x{1:X8}", p, field_14);
                //tw.WriteLine("{0}  18 field_18: 0x{1:X8}", p, field_18);
                //tw.WriteLine("{0}  1C elementsAddr: 0x{1:X8}", p, field_1C);

                var sym = NameToSymbol(mem_loc, elemnt_name);
                if (sym != "")
                {
                    switch (Program.OUTPUT)
                    {
                        case 0: tw_sym.WriteLine("-s 0x{0:X8}=MN_{1}", mem_loc, sym); break;
                        case 1: tw_sym.WriteLine("MakeMenu(0x{0:X8}, \"MN_{1}\", 0);", mem_loc, sym); break;
                        case 2: tw_sym.WriteLine("MakeNameEx(0x{0:X8}, \"\", 0 );", mem_loc); break;
                    }
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
            public const long size_of = 0x2C;
            long mem_loc;
            long file_loc;

            public Unknown_xx(byte[] data, int index, long addrOffset)
            {
                if (firmConsts.BFT_start != 0)
                {
                    long startAddr = ((index * size_of) + firmConsts.BFT_start);

                    mem_loc = startAddr;
                    file_loc = startAddr - addrOffset;

                    Read(data, mem_loc, file_loc);
                }
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
                //if (firmConsts.BFT_start != 0)
                //{
                //    tw.WriteLine("{0}BigFuncTable: 0x{1:X8} {2}", p, mem_loc, (mem_loc - firmConsts.BFT_start) / size_of);
                //    tw.WriteLine("{0}  field_00: 0x{1:X4}", p, field_0);
                //    tw.WriteLine("{0}  field_02: 0x{1:X4}", p, field_2);
                //    tw.WriteLine("{0}  field_04: 0x{1:X8} - {2}", p, field_4, ResolveFuncName(field_4));
                //    tw.WriteLine("{0}  field_08: 0x{1:X8} - {2}", p, field_8, ResolveFuncName(field_8));
                //    tw.WriteLine("{0}  field_0C: 0x{1:X8} - {2}", p, field_C, ResolveFuncName(field_C));
                //    tw.WriteLine("{0}  field_10: 0x{1:X8} - {2}", p, field_10, ResolveFuncName(field_10));
                //    tw.WriteLine("{0}  field_14: 0x{1:X8} - {2}", p, field_14, ResolveFuncName(field_14));
                //    tw.WriteLine("{0}  field_18: 0x{1:X8} - {2}", p, field_18, ResolveFuncName(field_18));
                //    tw.WriteLine("{0}  field_1C: 0x{1:X8} - {2}", p, field_1C, ResolveFuncName(field_1C));
                //    tw.WriteLine("{0}  field_20: 0x{1:X8} - {2}", p, field_20, ResolveFuncName(field_20));
                //    tw.WriteLine("{0}  field_24: 0x{1:X8} - {2}", p, field_24, ResolveFuncName(field_24));
                //    tw.WriteLine("{0}  field_28: 0x{1:X8} - {2}", p, field_28, ResolveFuncName(field_28));
                //}
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

                //txt_0 = ResolveString(data, field_0, firmConsts.EngPlaybackTextAddr);
                txt_2 = ResolveString(data, field_2, firmConsts.EngMenuTextAddr);

                Program.AddElement(mem_loc, this);
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
            //string txt_0;
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
                    return string.Format("{0}", txt_2);
            }

            internal void Dump(TextWriter tw, TextWriter tw_sym, string p)
            {
                //tw.WriteLine("{0}Element: 0x{1:X8} {2} {3}", p, mem_loc, txt_2, txt_3);
                tw.WriteLine("{0}Element: 0x{1:X8} {2}", p, mem_loc, txt_2);
                //tw.WriteLine("{0}  field_0: 0x{1:X4}", p, field_0);
                //tw.WriteLine("{0}  field_2: 0x{1:X4}", p, field_2);
                //tw.WriteLine("{0}  field_4: 0x{1:X4}", p, field_4);
                //tw.WriteLine("{0}  field_6: 0x{1:X4}", p, field_6);
                //tw.WriteLine("{0}  field_8: 0x{1:X4}", p, field_8);
                //tw.WriteLine("{0}  field_A: 0x{1:X4}", p, field_A);
                //tw.WriteLine("{0}  menu_ptr: 0x{1:X8}", p, menu_ptr);

                var sym = NameToSymbol(mem_loc, txt_2);
                if (sym != "")
                {
                    switch (Program.OUTPUT)
                    {
                        case 0: tw_sym.WriteLine("-s 0x{0:X8}=ME_{1}", mem_loc, sym); break;
                        case 1: tw_sym.WriteLine("MakeMenu(0x{0:X8}, \"ME_{1}\", 1);", mem_loc, sym); break;
                        case 2: tw_sym.WriteLine("MakeNameEx(0x{0:X8}, \"\", 0 );", mem_loc); break;
                    }
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
            FuncNames.Clear();

            if (dfr_file != "")
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
                            var param = line.IndexOf('(');
                            string name;
                            if( param != -1 )
                                name = line.Substring(eq_idx + 1, param - (eq_idx + 1) );
                            else
                                name = line.Substring(eq_idx + 1);

                            var addr = long.Parse(hex, System.Globalization.NumberStyles.HexNumber);

                            if (FuncNames.ContainsKey(addr))
                                Console.WriteLine("{0:X} already present: '{1}' '{2}'", addr, FuncNames[addr], name);
                            else
                            {
                                FuncNames.Add(addr, name);
                                //Symbols.Add(addr, name);
                            }

                            //Console.WriteLine("{0:X} {1:X}", hex, addr);
                        }
                    }
                }
            }

        }

        static Dictionary<long, string> Symbols = new Dictionary<long, string>();
        static string NameToSymbol(long addr, string text)
        {
            if (Symbols.ContainsKey(addr))
                return Symbols[addr];

            const int maxIn = 20;
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
            while (Symbols.ContainsValue(trytxt))
            {
                trytxt = string.Format("{0}_{1}", basetxt, next++);
            }

            Symbols.Add(addr, trytxt);
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
                case 0x2000:
                    baseAddr = firmConsts.EngDailTextAddr;
                    break;

                case 0x4000:
                    baseAddr = firmConsts.EngHelpTextAddr;
                    break;

                case 0x6000:
                    baseAddr = firmConsts.EngTableAddr;
                    break;

                case 0x8000:
                    baseAddr = firmConsts.EngTableAddr;
                    break;

                case 0x0000:
                default:
                    baseAddr = firmConsts.EngMenuTextAddr;
                    break;
            }

            if ((baseAddr + (offset * 4)) > firmConsts.EngLastAddr)
            {

            }
            long addr = baseAddr + (offset * 4) - firmConsts.File_offset;

            long saddr = ReadUint32(data, addr) - firmConsts.File_offset;
            long eaddr = ReadUint32(data, addr + 4) - firmConsts.File_offset;

            StringBuilder sb = new StringBuilder();
            int state = 0;
            for (long i = saddr; i < eaddr; i++)
            {
                //if (state == 0 && (data[i] < 0x20 || data[i] > 0x7F))
                if (data[i] < 0x20 || data[i] > 0x7F)
                {
                    //state = 1;
                    sb.AppendFormat("<{0:X2}>", data[i]);
                }
                //else if (state == 1)
                //{
                //    sb.AppendFormat("/0x{0:X2}", data[i]);
                //    state = 0;
                //}
                else
                {
                    sb.Append((char)data[i]);
                }
            }

            return sb.ToString();
        }
    }
}
