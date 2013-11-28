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
                Usage();
                Console.WriteLine("Firmware files ether not valid, or not the same versions");
                Console.WriteLine();
                return;
            }
        }

        private static bool PackageDiff(string file_a, string file_b)
        {
            var a = new Package();
            var b = new Package();
            if (a.TryOpen(file_a) && b.TryOpen(file_b))
            {
                if ( a.header.Count != b.header.Count ) 
                    return false;

                int count = a.header.Count;
                for (int i = 0; i < count; i++)
                {
                    if (a.header[i].Item1 != b.header[i].Item1 || // file name
                        a.header[i].Item2 != b.header[i].Item2 || // file offset
                        a.header[i].Item3 != b.header[i].Item3 )  // file length
                        return false;
                }

                using (var sw = new StreamWriter(file_b + "_patch.txt"))
                {
                    for (int i = 0; i < count; i++)
                    {
                        var ba = a.blocks[i];
                        var bb = b.blocks[i];

                        var dataend = ba.Length - 2; // ignore CRC at end of block

                        for (int j = 0; j < dataend; j++)
                        {
                            if (ba[j] != bb[j])
                            {
                                var patch_s = string.Format("new Patch({0}, 0x{1:X6}, new byte[] {{ 0x{2:X2} }} , new byte[] {{ 0x{3:X2} }} ),", i, j, ba[j], bb[j]);
                                //var s = string.Format("{0}, 0x{1:X6}, 0x{2:X2}, 0x{3:X2}", i, j, ba[j], bb[j]);
                                sw.WriteLine(patch_s);
                                Debug.WriteLine(patch_s);
                            }
                        }
                    }

                }

            }

            return false;
        }

        static void Usage()
        {
            Console.WriteLine("FirmwarePatch {0}.{1}", 1, 2);
            Console.WriteLine("  Usage:");
            Console.WriteLine("  FirmwarePatch [firmware orig] [firmware new]");
            Console.WriteLine();
        }
    }
}
