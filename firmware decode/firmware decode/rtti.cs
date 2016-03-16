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


                help.AddFuncName(0x502BD4A0, "pure_virt_memberPtr");

                help.SortClasses();

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
            int funcOffset = 0;
            int classOffset = 0;
            int state = 0;
            while (ReadUint32BE(data, file_loc + offset) != 0)
            {
                int val = (int)ReadUint32BE(data, file_loc + offset);
                offset += 4;
                if (state == 0)
                {
                    if (val < 0)
                    {
                        funcOffset = 0;
                        classOffset = val;

                        state = 1;
                    }
                    else
                    {
                        thisClass.AddFunction(classOffset, funcOffset, val);
                        funcOffset += 1;
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
            Dictionary<long, string> func_names;
            List<long> dup_func_names;

            public RttiHelp()
            {
                hdrs = new Dictionary<long, ClassHdr>();
                hrd_refs = new List<Tuple<long, long>>();
                que = new Queue<long>();
                func_names = new Dictionary<long, string>();
                dup_func_names = new List<long>();

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


            public void SortClasses()
            {
                var unsorted = hdrs.Keys.ToList();
                var sorted = new List<long>();

                Debug.WriteLine("SortClasses: {0}", unsorted.Count);
                int index = 0;
                // find next class
                while(unsorted.Count > 0)
                {
                    long us = unsorted[index];
                    var uc = hdrs[us];
                    bool allsorted = true;
                    foreach (long bca in uc.BaseClassAddr())
                    {
                        if (unsorted.Contains(bca))
                        {
                            allsorted = false;
                            index += 1;
                            break;
                        }
                    }

                    if (allsorted)
                    { 
                        // do work.
                        Debug.WriteLine("SortClasses: work on: 0x{0:X4} left: {1}", us, unsorted.Count);

                        uc.SortFuncs(this);

                        // remove from unsorted.
                        index = 0;
                        unsorted.Remove(us);
                        sorted.Add(us);
                    }
                }

                Debug.WriteLine(string.Format("Dup Func Name count: {0}", dup_func_names.Count));
            }

            internal string GetFuncName(long addr)
            {
                string name;
                if (func_names.TryGetValue(addr, out name))
                {
                    return name;
                }
                return "Not Solved";
            }

            internal void AddFuncName(long addr, string name)
            {
                if (func_names.ContainsKey(addr))
                {
                    if (dup_func_names.Contains(addr) == false)
                    {
                        Debug.WriteLine(string.Format("func_name dup {0:X4}", addr)); 
                        dup_func_names.Add(addr);
                    }
                }
                else
                {
                    func_names.Add(addr, name);
                }
            }

            internal bool HasFuncName(long addr)
            {
                return func_names.ContainsKey(addr);
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



        class ClassHdr
        {
            public ClassHdr(RttiHelp help, byte[] data, long startAddr, long addrOffset)
            {
                mem_loc = startAddr;
                file_loc = startAddr - addrOffset;
                baseClassesAddr = new List<Tuple<int, long>>();
                funcs = new List<Tuple<int, int, long>>();
                baseClasses = new List<Tuple<int, ClassHdr>>();

                Read(help, data, mem_loc, file_loc, addrOffset);
            }

            List<Tuple<int, long>> baseClassesAddr;
            List<Tuple<int, ClassHdr>> baseClasses;
            List<Tuple<int, int, long>> funcs;

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
                    baseClassesAddr.Add(new Tuple<int,long>(0, sub_loc));
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
                                baseClassesAddr.Add(new Tuple<int,long>(b, suba_loc));
                            else
                            {
                                baseClassesAddr.Add(new Tuple<int, long>(b, suba_loc));
                            }
                        }
                    }
                    else
                    {
                        throw new Exception(string.Format("subtype 0x{0:X} not known {1}", addr, subtype ));
                    }

                }
            }

            public void AddFunction(int offset, int funcIdx, long funcAddr)
            {
                long real_addr = funcAddr & 0xFFffFFfe;
                bool arm16 = real_addr != funcAddr;
                bool neg_off = offset < 0;
                int t_offset = neg_off ? -offset : offset;

                Debug.WriteLine("{0} {1}{2:X2} {5} 0x{3:X4}{4}", name, neg_off?"-":"", t_offset, real_addr, arm16?"+1":"", funcIdx);
                funcs.Add(new Tuple<int,int,long>(offset, funcIdx,real_addr));

            }
            
            public long mem_loc;
            long file_loc;

            UInt32 type;
            long name_loc;
            string name;
            long sub_loc;

            long subtype;
            long subcount;

            int baseclassfunc_count = 0;
            int myclassfunc_count = 0;
            int max_base_depth = 0;


            public override string ToString()
            {
                return string.Format("0x{0:X4} {1}", name_loc, name);
            }

            internal void Dump(RttiHelp help, TextWriter tw, TextWriter tw_sym, string p)
            {
                tw.WriteLine("0x{1:X4} t: {2:X4} n: {3:x4} '{4}' fc: {5} bfc: {6} mfc: {7} ",p ,mem_loc, type, name_loc, name, baseclassfunc_count+myclassfunc_count, baseclassfunc_count, myclassfunc_count);
                foreach (var b in baseClassesAddr)
                {
                    var hdr = help.GetClass(b.Item2);

                    tw.Write("{0}  c {1:X} ", p, b.Item1);
                    hdr.Dump(help, tw, tw_sym, p + "  ");
                }

                foreach (var f in funcs)
                {
                    long funcAddr = f.Item3;
                    int funcIdx = f.Item2;
                    int offset = f.Item1;
                    long real_addr = funcAddr & 0xFFffFFfe;
                    bool arm16 = real_addr != funcAddr;
                    bool neg_off = offset < 0;
                    int t_offset = neg_off ? -offset : offset;

                    tw.WriteLine("{0}  f {1}{2:X2} {3} 0x{4:X4}{5} {6}", p, neg_off ? "-" : "", t_offset, funcIdx, real_addr, arm16 ? "+1" : "", help.GetFuncName(real_addr) );

                    //tw.WriteLine("{0}  f {1:X} {2:X4}", p, f.Item1, f.Item2);
                }
            }

            internal void DumpBase(RttiHelp help, TextWriter tw, TextWriter tw_sym, string p)
            {
                tw.WriteLine("{0}0x{1:X4} t: {2:X4} n: {3:x4} '{4}'", p, mem_loc, type, name_loc, name);
                foreach (var b in baseClassesAddr)
                {
                    var hdr = help.GetClass(b.Item2);
                    tw.WriteLine("{0}  {1:X} {2:X4}", p, b.Item1, b.Item2);
                    hdr.DumpBase(help, tw, tw_sym, p + "  ");
                }
            }

            public IEnumerable BaseClassAddr()
            {
                foreach (var bca in baseClassesAddr)
                {
                    yield return bca.Item2;
                }
            }

            ClassHdr FindFuncBaseClassByOffset(RttiHelp help, int offset, int index)
            {
                foreach (var bca in baseClasses)
                {
                    if( bca.Item1 == -offset)
                    {
                        return bca.Item2;    
                    }
                }
                
                return null;
            }

            void ResolveBaseClassOffsets(RttiHelp help)
            {
                // Add the offsetted base classes for all base class to this class table.
                max_base_depth = 0;

                foreach (var bca in baseClassesAddr)
                {
                    int offset = bca.Item1; 
                    var bc = help.GetClass(bca.Item2);

                    foreach (var bbc in bc.baseClasses)
                    {
                        baseClasses.Add(new Tuple<int,ClassHdr>(offset + bbc.Item1, bbc.Item2));
                    }

                    baseClasses.Add(new Tuple<int,ClassHdr>(offset, bc));
                    max_base_depth = Math.Max(max_base_depth, bc.max_base_depth + 1);
                }
            }

            public void SortFuncs(RttiHelp help)
            {
                Debug.WriteLine("Sorting: {0} base: {1} funcs: {2}", name, baseClassesAddr.Count, funcs.Count);

                ResolveBaseClassOffsets(help);

                // the first BaseClass virt table is blended with ours, to sort that out.
                if (baseClassesAddr.Count != 0)
                {
                    var bc = help.GetClass(baseClassesAddr[0].Item2);
                    baseclassfunc_count = bc.myclassfunc_count + bc.baseclassfunc_count;
                    // if I have no functions set at my level, but the first does does, I have no, but have base items.
                    myclassfunc_count = Math.Max((funcs.Count - baseclassfunc_count), 0);
                }
                else
                {
                    baseclassfunc_count = 0;
                    myclassfunc_count = funcs.Count;
                }

                // count base class function, and find my function count.
                foreach (var bbc in baseClasses)
                {
                    Debug.WriteLine(string.Format("BC: {0} {1:X} {2} {3}", name, bbc.Item1, bbc.Item2.max_base_depth, bbc.Item2.name));

                    Debug.WriteLine(string.Format("  fc: {0} bfc: {1} mfc: {2}", bbc.Item2.funcs.Count, bbc.Item2.baseclassfunc_count, bbc.Item2.myclassfunc_count));
                    // my func count is: my number of functions minus base class, if I define some.

                }

                Debug.WriteLine(string.Format("me FC: {0} bfc: {1} mfc: {2}", funcs.Count, baseclassfunc_count, myclassfunc_count));


                foreach (var fun in funcs)
                {
                    var offset = fun.Item1;
                    var index = fun.Item2;
                    var addr = fun.Item3;

                    var bc = FindFuncBaseClassByOffset(help, offset, index);
                    string bc_name = bc == null ? "unknown" : bc.name;

                    
                    if (offset != 0 ||
                        index < baseclassfunc_count)
                    {
                        // overloaded virtual functions
                        if (help.HasFuncName(addr) == false)
                        {
                            //TODO find the correct base class name, and base class virt index
                            help.AddFuncName(addr, string.Format("{0}_{1}_virt{2}", name, bc_name, index));
                        }
                    }
                    else
                    {
                        // original virtual functions
                        help.AddFuncName(addr, string.Format("{0}_virt{1}", name, index));
                    }
                }
            }
        }


 
    }
}

