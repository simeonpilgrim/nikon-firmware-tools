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
        private static Dictionary<long, vft> vfts;

        internal class RttiConsts
        {
            public FirmOffsets offsets;
            public uint File_offset(uint addr) { return offsets.Resolve(addr); }

            public uint Rtti_From;
            public uint Rtti_To;

        }

        internal static RttiConsts rttiConsts = null;

        internal class D5200_0101_RttiConst : RttiConsts
        {
            public D5200_0101_RttiConst()
            {
                // this is need for loading firmware and tracing addresses back
                offsets = new FirmOffsets(0x40000, 0x880000, 0x040000);
                Rtti_From = 0x5142FCA4;
                Rtti_To = 0x514320B8;
            }
        }





        private static void DumpRttiD5200(string fileName)
        {
            rttiConsts = new D5200_0101_RttiConst();
            DumpRtti(fileName);
        }





        private static void DumpRtti(string fileName)
        {
            if (File.Exists(fileName))
            {
                BinaryReader br = null;
                RttiHelp help = new RttiHelp();

                byte[] data;

                using (br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite)))
                {
                    data = br.ReadBytes((int)br.BaseStream.Length);
                }

                RangeVft(data, 0x5142FCA8, 0x514320B8, 0x51432044 - 0x01412044, help);
                RangeVft(data, 0x51432158, 0x51435024, 0x51432044 - 0x01412044, help);
                //RangeVft(data, 0x51432158, 0x51458F6C, 0x51432044 - 0x01412044, help);

                //var off = ScanVft(data, 0x51431EFC, 0x51432044 - 0x01412044, help);

                //Debug.WriteLine("off {0:X4}", off);
                //help.AddClassRef(0, 0x5141DF00);
                //while (help.HasWork())
                //{
                //    var addr = help.GetWork();

                //    var h = new ClassHdr(help, data, addr, 0x51432044 - 0x01412044);
                //    help.AddClass(addr, h);
                //}

                using (var sw = new StreamWriter(File.Open(fileName + ".class.txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite)))
                {
                    using (var sw2 = new StreamWriter(File.Open(fileName + ".class_sym.idc", FileMode.Create, FileAccess.Write, FileShare.ReadWrite)))
                    {
                        help.DumpHdrs(sw, sw2);
                    }
                }

            }
        }

        private static void RangeVft(byte[] data, long s_addr, long e_addr, long fileFix, RttiHelp help)
        {
            long off = s_addr;
            while (off < e_addr)
            {
                off = ScanVft(data, off, fileFix, help);
            }
        }

        private static long ScanVft(byte[] data, long addr, long fileFix, RttiHelp help)
        {
            long file_loc = addr - fileFix;
            long offset = 0;
            long class_addr = ReadUint32BE(data, file_loc);
            offset += 4;

            if (class_addr == 0)
                return addr + offset;

            help.AddClassRef(addr, class_addr);
            while (help.HasWork())
            {
                var w_addr = help.GetWork();
                var w_class = new ClassHdr(help, data, w_addr, fileFix);
                help.AddClass(w_addr, w_class);
            }

            var thisClass = help.GetClass(class_addr);
            var activeClass = thisClass;
            int activeOffset = 0;
            int state = 0;
            while (ReadUint32BE(data, file_loc + offset) != 0)
            {
                int val = (int)ReadUint32BE(data, file_loc + offset);
                offset += 4;
                if (state == 0)
                {
                    if (val < 0)
                    {
                        activeOffset = val;
                        state = 1;
                    }
                    else
                    {
                        thisClass.AddFunction(activeOffset, val);
                        activeOffset += 4;
                    }
                }
                else if (state == 1)
                {
                    // check class is same as above
                    if (val != class_addr)
                    {
                        int z = 0; 
                    }
                    state = 0;
                }
            }

            return addr + offset + 4;
        }

        class RttiHelp
        {
            Dictionary<long, ClassHdr> hdrs;
            List<Tuple<long, long>> hrd_refs;
            Queue<long> que;

            public RttiHelp()
            {
                hdrs = new Dictionary<long, ClassHdr>();
                hrd_refs = new List<Tuple<long, long>>();
                que = new Queue<long>();
            }

            
            public void AddClass(long addr, ClassHdr hdr)
            {
                hdrs.Add(addr, hdr);
            }

            public void AddClassRef(long from_addr, long to_addr)
            {
                if (hdrs.ContainsKey(to_addr) == false && que.Contains(to_addr) == false)
                {
                    if (to_addr == 0)
                    {
                        int z = 0;
                    }
                    que.Enqueue(to_addr);
                }
                hrd_refs.Add(new Tuple<long,long>(from_addr, to_addr));
            }

            public bool HasWork()
            {
                return que.Count() > 0;
            }

            public long GetWork()
            {
                return que.Dequeue();
            }

            public void DumpHdrs(TextWriter tw, TextWriter tw_sym)
            {
                foreach (var h in hdrs)
                {
                    h.Value.Dump(this, tw, tw_sym, "");
                }
            }

            public ClassHdr GetClass(long addr)
            {
                return hdrs[addr];
            }
        }

        internal class funcptr
        {
            long mem_loc;
            string name;
            public funcptr()
            {
                mem_loc = 0;
                name = "";
            }
        }

        internal class vft
        {
            public long size_of = 0;
            long mem_loc;
            long file_loc;

            public vft(byte[] data, long addr, long addrOffset)
            {
                if (firmConsts.BFT_start != 0)
                {
                    mem_loc = addr;
                    file_loc = addr - addrOffset;

                    Read(data, mem_loc, file_loc);
                }
            }

            List<funcptr> funcptrs;


            public void Read(byte[] data, long startAddr, long addrOffset)
            {
            }

            internal void Dump(TextWriter tw, string p)
            {
                if (firmConsts.BFT_start != 0)
                {
                    tw.WriteLine("{0}VFT: 0x{1:X8}", p, mem_loc);
                }
            }
        }

        class ClassHdr
        {
            public ClassHdr(RttiHelp help, byte[] data, long startAddr, long addrOffset)
            {
                mem_loc = startAddr;
                file_loc = startAddr - addrOffset;
                baseClasses = new List<Tuple<int, long>>();
                funcs = new List<Tuple<int, long>>();

                Read(help, data, mem_loc, file_loc, addrOffset);
            }

            List<Tuple<int, long>> baseClasses;
            List<Tuple<int, long>> funcs;

            public void Read(RttiHelp help, byte[] data, long addr, long fileOffset, long fileFix)
            {
                type = ReadUint32BE(data, file_loc);
                name_loc = ReadUint32BE(data, file_loc + 0x04);
                name = ReadString(data, name_loc - fileFix, 100);

                Debug.WriteLine("0x{0:X4} t: {1:X4} n: {2:x4} '{3}'", addr, type, name_loc, name);
                if (type == 0x51458F20)
                {
                }
                else if (type == 0x51458F30)
                {
                    sub_loc = ReadUint32BE(data, file_loc + 0x08);
                    help.AddClassRef(addr, sub_loc);
                    baseClasses.Add(new Tuple<int,long>(0, sub_loc));
                }
                else if (type == 0x51458F40)
                {
                    subtype = ReadUint32BE(data, file_loc + 0x08);

                    if (subtype == 0 || subtype == 2)
                    {
                        subcount = ReadUint32BE(data, file_loc + 0x0C);

                        for (int i = 0; i < subcount; i++)
                        {
                            UInt32 suba_loc = ReadUint32BE(data, file_loc + 0x10 + (8 * i));
                            byte a = data[file_loc + 0x10 + (8 * i) + 0x04];
                            byte b = data[file_loc + 0x10 + (8 * i) + 0x05];
                            byte c = data[file_loc + 0x10 + (8 * i) + 0x06];
                            byte d = data[file_loc + 0x10 + (8 * i) + 0x07];

                            Debug.WriteLine(" 0x{0:X4} {1:x2} {2:x2} {3:x2} {4:x2}", suba_loc, a, b, c, d);
                            help.AddClassRef(addr, suba_loc);
                            if (a == 2)
                                baseClasses.Add(new Tuple<int,long>(b, suba_loc));
                            else
                            {
                                baseClasses.Add(new Tuple<int, long>(b, suba_loc));
                            }
                        }
                    }
                    else
                    {
                        throw new Exception(string.Format("subtype 0x{0:X} not known {1}", addr, subtype ));
                    }

                }
            }

            public void AddFunction(int offset, long funcAddr)
            {
                long real_addr = funcAddr & 0xFFffFFfe;
                bool arm16 = real_addr != funcAddr;
                bool neg_off = offset < 0;
                int t_offset = neg_off ? -offset : offset;

                Debug.WriteLine("{0} {1}{2:X2} 0x{3:X4}{4}", name, neg_off?"-":"", t_offset, real_addr, arm16?"+1":"");
                funcs.Add(new Tuple<int,long>(offset, real_addr));

            }

            public long mem_loc;
            long file_loc;

            UInt32 type;
            long name_loc;
            string name;
            long sub_loc;

            long subtype;
            long subcount;


            public override string ToString()
            {
                return string.Format("{0}", name_loc);
            }

            internal void Dump(RttiHelp help, TextWriter tw, TextWriter tw_sym, string p)
            {
                tw.WriteLine("0x{1:X4} t: {2:X4} n: {3:x4} '{4}'",p ,mem_loc, type, name_loc, name);
                foreach (var b in baseClasses)
                {
                    var hdr = help.GetClass(b.Item2);

                    tw.Write("{0}  c {1:X} ", p, b.Item1);
                    hdr.Dump(help, tw, tw_sym, p + "  ");
                }

                foreach (var f in funcs)
                {
                    long funcAddr = f.Item2;
                    int offset = f.Item1;
                    long real_addr = funcAddr & 0xFFffFFfe;
                    bool arm16 = real_addr != funcAddr;
                    bool neg_off = offset < 0;
                    int t_offset = neg_off ? -offset : offset;

                    tw.WriteLine("{0}  f {1}{2:X2} 0x{3:X4}{4}", p, neg_off ? "-" : "", t_offset, real_addr, arm16 ? "+1" : "");

                    //tw.WriteLine("{0}  f {1:X} {2:X4}", p, f.Item1, f.Item2);
                }
            }

            internal void DumpBase(RttiHelp help, TextWriter tw, TextWriter tw_sym, string p)
            {
                tw.WriteLine("{0}0x{1:X4} t: {2:X4} n: {3:x4} '{4}'", p, mem_loc, type, name_loc, name);
                foreach (var b in baseClasses)
                {
                    var hdr = help.GetClass(b.Item2);
                    tw.WriteLine("{0}  {1:X} {2:X4}", p, b.Item1, b.Item2);
                    hdr.DumpBase(help, tw, tw_sym, p + "  ");
                }
            }
        }


 
    }
}

