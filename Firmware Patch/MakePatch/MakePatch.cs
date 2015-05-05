using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Diagnostics;

namespace MakePatch
{
    class MakePatch
    {
        static void Main(string[] args)
        {
            if (args.Length != 2)
            {
                Usage();
                return;
            }

            if (File.Exists(args[0]) == false || File.Exists(args[1]) == false)
            {
                Usage();
                return;
            }

            bool res = PackageDiff(args[0], args[1]);

            if (res == false)
            {
                res = BlobDiff(args[0], args[1]);
            }

            if (res == false)
            {
                Usage();
                Console.WriteLine("Firmware files ether not valid, or not the same versions");
                Console.WriteLine();
                return;
            }
        }

        private static bool BlobDiff(string file_a, string file_b)
        {
            var br_a = new BinaryReader(File.OpenRead(file_a));
            var br_b = new BinaryReader(File.OpenRead(file_b));

            if( br_a.BaseStream.Length != br_b.BaseStream.Length + 2 )
                return false;
  
            int len = (int)br_b.BaseStream.Length;
            using (var sw = new StreamWriter(file_b + "_patch.txt"))
            {
                CompareBlock(br_a.ReadBytes(len), br_b.ReadBytes(len), 0, len, sw);
            }

            return true;
        }

        private static bool PackageDiff(string file_a, string file_b)
        {
            var a = new Package();
            var b = new Package();
            if (a.TryOpen(file_a) && b.TryOpen(file_b))
            {
                if (a.header.Count != b.header.Count)
                    return false;

                int count = a.header.Count;
                for (int i = 0; i < count; i++)
                {
                    if (a.header[i].Item1 != b.header[i].Item1 || // file name
                        a.header[i].Item2 != b.header[i].Item2 || // file offset
                        a.header[i].Item3 != b.header[i].Item3)  // file length
                        return false;
                }

                using (var sw = new StreamWriter(file_b + "_patch.txt"))
                {
                    for (int i = 0; i < count; i++)
                    {
                        var ba = a.blocks[i];
                        var bb = b.blocks[i];
                        var dataend = ba.Length - 2; // ignore CRC at end of block

                        CompareBlock(ba, bb, i, dataend, sw);
                    }
                }

                return true;
            }

            return false;
        }

        private static void CompareBlock(byte[] ba, byte[] bb, int blockId, int dataend, StreamWriter sw)
        {
            List<Tuple<byte, byte>> chunk = new List<Tuple<byte, byte>>();

            int last = -1;
            for (int j = 0; j < dataend; j++)
            {
                if (ba[j] != bb[j])
                {
                    if (j != last + 1)
                    {
                        OutputChunk(sw, blockId, chunk, last);
                        chunk.Clear();
                    }

                    chunk.Add(new Tuple<byte, byte>(ba[j], bb[j]));
                    last = j;
                }
            }
            OutputChunk(sw, blockId, chunk, last);
        }

        private static void OutputChunk(StreamWriter sw, int fileId, List<Tuple<byte, byte>> chunk, int last)
        {
            if (chunk.Count == 0) return;

            int count = chunk.Count -1;
            var addr = last - count;
            var sba = new StringBuilder();
            var sbb = new StringBuilder();

            for (int i = 0; i <= count; i++)
            {
                sba.AppendFormat("0x{0:X2}", chunk[i].Item1);
                sbb.AppendFormat("0x{0:X2}", chunk[i].Item2);
                if (i != count)
                {
                    sba.Append(", ");
                    sbb.Append(", ");
                }

            }

            var patch_s = string.Format("new Patch({0}, 0x{1:X6}, new byte[] {{ {2} }} , new byte[] {{ {3} }} ),", fileId, addr, sba.ToString(), sbb.ToString());
            
            sw.WriteLine(patch_s);
            Debug.WriteLine(patch_s);
        }



        static void Usage()
        {
            Console.WriteLine("FirmwarePatch {0}.{1}", 1, 2);
            Console.WriteLine("  Usage:");
            Console.WriteLine("  FirmwarePatch [firmware orig] [firmware new]");
            Console.WriteLine("    Output is [firmware new]patch.txt");
            Console.WriteLine("");
        }
    }
}
