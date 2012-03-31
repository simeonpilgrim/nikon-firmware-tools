using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Linq.Expressions;
using System.Diagnostics;
using System.Text;


namespace Firmware_Patch
{
    static class Program
    {


        static void Main(string[] args)
        {
            if (args.Length == 0)
            {
                Usage();
                return;
            }

            if (File.Exists(args[0]) == false)
            {
                Usage();
                return;
            }

            var m = FirmwareMatch(args[0]);
            //var m = FirmwareMatch(@"C:\Users\spilgrim\Downloads\Nikon\D5100Update\D5100_0101.bin");

            if( m == null )
            {
                Usage();
                Console.WriteLine("Firmware ether not valid, or not supported");
                Console.WriteLine();
                return;
            }

            if (m.ParseOptions(args) == false)
            {
                Usage();
                Console.WriteLine("Firmware ether not valid, or not supported");
                Console.WriteLine();
                return;
            }

            m.Patch(args[0] + ".patched");
        }

        static void Usage()
        {
            Console.WriteLine("FirmwarePatch {0}.{1}", 1, 0);
            Console.WriteLine("  Usage:");
            Console.WriteLine("  FirmwarePatch [firmware] [options]");
            Console.WriteLine("     [firmware] - the name of the firmware you would like to patch");
            Console.WriteLine("     [options]  - there arn't any, yet.");
            Console.WriteLine();
        }

        static bool HashSame(byte[] h1, byte[] h2)
        {
            for (int i = 0; i < 16; i++)
                if (h1[i] != h2[i])
                    return false;

            return true;
        }

        static void PrintHash(byte[] h)
        {
            Debug.WriteLine("");
            Debug.Write("byte[] HASH = {");
            foreach (var b in h)
            {
                Debug.Write(String.Format("{0:X2}, ", b));
            }
            Debug.WriteLine("} ;");
        }


        static Firmware FirmwareMatch(string file_name)
        {
            byte[] d5100_0101Hash = { 0x21, 0x84, 0xF8, 0x65, 0x82, 0xB2, 0x7A, 0x80, 0x49, 0xDC, 0x8C, 0x7D, 0x91, 0x8A, 0xDA, 0x50 };

            var hash = GetMD5HashFromFile(file_name);

            PrintHash(hash);

            if(HashSame( hash, d5100_0101Hash) )
            {
                return new D5100_0101(file_name);
            }
            return null;
        }


        static byte[] GetMD5HashFromFile(string file_name)
        {
            byte[] retVal = null;

            using (FileStream file = new FileStream(file_name, FileMode.Open))
            {
                MD5 md5 = new MD5CryptoServiceProvider();
                retVal = md5.ComputeHash(file);
                file.Close();
            }
            return retVal;
        }


    }

    static class Sys
    {
        public static UInt32 ReadUint32(byte[] data, long pos)
        {
            return (UInt32)(data[pos + 0] << 24 | data[pos + 1] << 16 | data[pos + 2] << 8 | data[pos + 3]);
        }

        public static UInt16 ReadUint16(byte[] data, long pos)
        {
            return (UInt16)(data[pos + 0] << 8 | data[pos + 1]);
        }

        public static string ReadString(byte[] data, long pos, int count)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++)
            {
                byte c = data[i + pos];
                if (c != 0)
                {
                    sb.Append((char)c);
                }
                else
                {
                    break;
                }
            }

            return sb.ToString();
        }

    }

    interface Firmware
    {
        bool ParseOptions(string[] options);
        void Patch(string outfilename);
    }

    class Patch
    {
        public Patch(int _block, int _start, byte[] _orig, byte[] _patch)
        {
            block = _block;
            start = _start;
            orig = _orig;
            patch = _patch;
        }
        public int block;
        public int start;
        public byte[] orig;
        public byte[] patch;
    }

    class D5100_0101 : Firmware
    {
        string in_filename;
        Package p = null;

        Patch[] patches = {
            new Patch(1, 0x74AD4, new byte[] { 0xE4, 0x03 }, new byte[] { 0xE0, 0x03 }), // unlimited recording 1/2
            new Patch(1, 0x755A8, new byte[] { 0xE2, 0x0B }, new byte[] { 0xE0, 0x0B }), // unlimited recording 2/2
                          };

        public D5100_0101(string filename)
        {
            in_filename = filename;
            p = new Package();
        }

        public bool ParseOptions(string[] options) 
        {
            // open file           
            // de-XOR
            // pull apart
            if (p.TryOpen(in_filename) == false)
            {
                return false;
            }

            foreach (Patch pp in patches)
            {
                if (p.PatchCheck(pp.block, pp.start, pp.orig) == false)
                {
                    return false;
                }
            }

            return true;      
        }

        public void Patch(string outfilename)
        {
            foreach (Patch pp in patches)
            {
                p.Patch(pp.block, pp.start, pp.patch);
            }

            p.Repackage(outfilename);
        }
    }

    class Package
    {
        byte[] xored;
        byte[] raw;
        List<Tuple<string, uint, uint>> header = new List<Tuple<string, uint, uint>>();
        List<byte[]> blocks = new List<byte[]>();

        const long max_size = 32 * 1024 * 1024;
        public bool TryOpen(string filename)
        {
            var br = new BinaryReader(File.OpenRead(filename));
            if (br.BaseStream.Length > max_size)
                return false;

            int len = (int)br.BaseStream.Length;
            xored = br.ReadBytes(len);

            raw = new byte[len];
            for (int i = 0; i < len; i++)
            {
                int ord1_idx = i & 0xFF;
                int ord2_idx = (i >> 8) & 0xFF;
                int ord3_idx = (i >> 16) & 0xFF;

                int b = xored[i] ^ Xor_Ord1[ord1_idx] ^ Xor_Ord2[ord2_idx] ^ Xor_Ord3[ord3_idx];
                raw[i] = (byte)b;
            }

            if (ExactFirmware() == false)
            {
                return false;
            }

            return true;
        }

        public bool PatchCheck(int block, int start, byte[] orig)
        {
            if (block >= blocks.Count) return false;

            var b = blocks[block];
            if (start >= b.Length ||
                (start + orig.Length) > b.Length)
            {
                return false;
            }

            for (int i = 0; i < orig.Length; i++)
            {
                if (orig[i] != b[start + i])
                {
                    var b1 = orig[i];
                    var b2 = b[start + i];
                    return false;
                }
            }

            return true;
        }


        public void Patch(int block, int start, byte[] data)
        {
            var b = blocks[block];
            for (int i = 0; i < data.Length; i++)
            {
                b[start + i] = data[i];
            }
        }

        public void UpdateCRCs()
        {
            foreach (var b in blocks)
            {
                CRC.UpdateCRC(b);
            }
        }

        public void Repackage(string outfilename)
        {
            UpdateCRCs();

            // put back together
            for(int i = 0; i < header.Count; i++)
            {
                var t = header[i];
                Array.Copy(blocks[i], 0, raw, t.Item2, t.Item3);
            }

            // XOR
            for (int i = 0; i < raw.Length; i++)
            {
                int ord1_idx = i & 0xFF;
                int ord2_idx = (i >> 8) & 0xFF;
                int ord3_idx = (i >> 16) & 0xFF;

                int b = raw[i] ^ Xor_Ord1[ord1_idx] ^ Xor_Ord2[ord2_idx] ^ Xor_Ord3[ord3_idx];
                xored[i] = (byte)b;
            }

            // write output
            using (var bw = new BinaryWriter(File.Open(outfilename, FileMode.Create, FileAccess.Write, FileShare.ReadWrite)))       
            {
                bw.Write(xored);
            }
        }

        byte[] Xor_Ord1 = {
                        0xE8, 0xFE, 0x46, 0xE3, 0x7D, 0xAB, 0x5C, 0xB9, 0xA5, 0x53, 0xC4, 0xC7, 0x32, 0xCD, 0x9C, 0xED,
                        0x94, 0x67, 0x4E, 0x5B, 0x48, 0x3A, 0x52, 0xB6, 0x34, 0xF7, 0x8E, 0x12, 0x90, 0x98, 0x82, 0x3C,
                        0x09, 0x2B, 0x68, 0xA8, 0x24, 0xD5, 0x10, 0x9D, 0x9A, 0xD9, 0xA9, 0x7F, 0x50, 0x4B, 0xDD, 0x9E,
                        0x62, 0x1C, 0x6A, 0x64, 0x02, 0x11, 0xDA, 0x4A, 0x6E, 0x35, 0xBD, 0xA0, 0xB1, 0xD7, 0xDE, 0x83,
                        0x5D, 0xE5, 0x5E, 0xCC, 0xDB, 0xAC, 0x18, 0xE2, 0x25, 0x69, 0x07, 0xBC, 0x39, 0x97, 0x14, 0xEB,
                        0xB2, 0x73, 0x36, 0x8A, 0x99, 0xB8, 0x1E, 0x2E, 0xEF, 0x93, 0xBA, 0xEE, 0xF5, 0xC5, 0x7B, 0x74,
                        0x8D, 0xE1, 0xC3, 0x4F, 0x41, 0x42, 0x6C, 0xE6, 0xA2, 0x06, 0x6F, 0x85, 0x2A, 0x2F, 0x1B, 0x38,
                        0x08, 0xAF, 0x44, 0x00, 0x33, 0x63, 0x91, 0x22, 0x87, 0x70, 0xB0, 0x43, 0xB5, 0x66, 0xE0, 0xFF,
                        0x30, 0xAD, 0x8F, 0xC1, 0xF4, 0xEA, 0xF9, 0xF6, 0x51, 0xD6, 0xD4, 0x3E, 0x04, 0x72, 0x3D, 0x54,
                        0x78, 0xC0, 0x7E, 0x26, 0xFA, 0x56, 0x58, 0xC9, 0x55, 0xC8, 0xA6, 0x16, 0x23, 0x84, 0xB7, 0xCB,
                        0x45, 0x7C, 0xD8, 0x7A, 0x27, 0x2D, 0xCA, 0x03, 0x3F, 0x17, 0x0B, 0x57, 0xBB, 0x3B, 0xF0, 0x49,
                        0x1F, 0xD1, 0x86, 0x80, 0x95, 0x20, 0x6B, 0xC6, 0xBF, 0xAA, 0x79, 0xD2, 0x75, 0xCF, 0xAE, 0xD0,
                        0x5F, 0xF1, 0x61, 0xF3, 0xFB, 0xCE, 0x29, 0x65, 0x0F, 0x31, 0x2C, 0xFD, 0x76, 0x0C, 0x4C, 0x4D,
                        0x60, 0xF8, 0x88, 0x6D, 0xA4, 0xEC, 0x9B, 0x92, 0x47, 0xA1, 0xE4, 0x21, 0xFC, 0x81, 0xA7, 0xC2,
                        0x0E, 0xA3, 0x40, 0x0D, 0x8B, 0x59, 0x96, 0x37, 0xE7, 0xF2, 0x77, 0x1D, 0x28, 0x0A, 0x8C, 0x5A,
                        0x15, 0x9F, 0x01, 0xB3, 0xD3, 0xBE, 0xB4, 0x1A, 0x89, 0xE9, 0x13, 0x05, 0xDF, 0xDC, 0x71, 0x19,   
                    };

        byte[] Xor_Ord2 = {
                        0x76, 0x0F, 0x43, 0xD9, 0xDB, 0xDC, 0x9B, 0x49, 0x4E, 0x42, 0xB7, 0x9F, 0xEC, 0x55, 0x19, 0x11, 
                        0x58, 0x23, 0x69, 0xA2, 0xB8, 0x68, 0xE8, 0x2B, 0x91, 0xF3, 0x1A, 0x34, 0xED, 0x0A, 0x06, 0x89, 
                        0xB2, 0x79, 0x2A, 0xC8, 0xEE, 0xA3, 0xB5, 0xD0, 0xFD, 0x17, 0xF9, 0xCE, 0x74, 0x39, 0x47, 0xC5, 
                        0xC1, 0x5D, 0x86, 0x7F, 0x6A, 0xAB, 0xE5, 0xF5, 0xC9, 0x96, 0x71, 0x1C, 0x09, 0x25, 0xD3, 0x8C, 
                        0x0C, 0x02, 0xB1, 0x48, 0x7C, 0x46, 0x3E, 0x08, 0x7B, 0x01, 0x54, 0x6B, 0xB9, 0x4F, 0xCD, 0xF1, 
                        0x51, 0x50, 0x59, 0xA4, 0xA7, 0x6C, 0x3F, 0xB6, 0x9D, 0xBC, 0x4C, 0x9E, 0x16, 0x37, 0xA1, 0xC0, 
                        0x6E, 0xE2, 0xDA, 0x3D, 0x22, 0xCB, 0xE7, 0x5B, 0x98, 0x53, 0x92, 0x36, 0x90, 0xAC, 0x31, 0x24, 
                        0x21, 0xC6, 0x63, 0x35, 0xB4, 0x5C, 0x1F, 0x77, 0x4A, 0xE4, 0x0D, 0x13, 0x8D, 0xC7, 0x99, 0x7E, 
                        0x81, 0x3C, 0x60, 0x28, 0xF0, 0xBF, 0x82, 0x2C, 0x78, 0x7A, 0x5F, 0x93, 0x84, 0x70, 0xEA, 0x9A, 
                        0x8E, 0xD2, 0x27, 0xE0, 0xCF, 0x6D, 0x10, 0x9C, 0x56, 0x07, 0x12, 0xFA, 0x26, 0x97, 0x80, 0xE3, 
                        0xE1, 0x61, 0x8A, 0x75, 0xA9, 0x5A, 0xDE, 0x1E, 0x5E, 0x4D, 0x66, 0x0E, 0xBA, 0x4B, 0x20, 0x40, 
                        0xA8, 0x8F, 0x52, 0x7D, 0xDF, 0xE6, 0xAF, 0x6F, 0xBE, 0xFC, 0x94, 0xA0, 0x3A, 0x33, 0x45, 0x14, 
                        0x62, 0x00, 0x87, 0xAE, 0xB3, 0x8B, 0xD4, 0xCA, 0xFF, 0xE9, 0x04, 0x88, 0xCC, 0x41, 0xD7, 0xD6, 
                        0xBB, 0x95, 0x32, 0x18, 0xF8, 0x72, 0x65, 0x3B, 0x29, 0xAD, 0x44, 0x1B, 0xC2, 0xD8, 0x1D, 0xA5, 
                        0xDD, 0x67, 0xD5, 0x30, 0xA6, 0xEF, 0x2F, 0xF2, 0x83, 0x2D, 0x03, 0xBD, 0x15, 0x2E, 0xD1, 0x73, 
                        0x57, 0xAA, 0xFB, 0x85, 0xF4, 0x64, 0x0B, 0xC4, 0xF7, 0xEB, 0x38, 0xC3, 0xF6, 0x05, 0xFE, 0xB0,
                      };

        int[] Xor_Ord3 = {
                        0x2f, 0x25, 0x06, 0xbb, 0xe3, 0x82, 0x70, 0xce, 0x86, 0xfb, 0x100, 0x3a, 0xf8, 0xbf, 0x76, 0xf5, 
                        0xc3, 0xf6, 0x9d, 0x9e, 0x10, 0xea, 0xfc, 0x9c, 0xdd, 0x12, 0x46, 0x93, 0x4f, 0xa6, 0xad, 0x68,
                        0x94, 0xb8, 0x22, 0xe1, 0x62, 0x5b, 0xcf, 0x67, 0x08, 0xe7, 0x48, 0x0c, 0x7e, 0x7d, 0xb1, 0xa9,
                        0x3e, 0x1b, 0xc5, 0x0a, 0x11, 0xaa, 0xba, 0xb2, 0xd0, 0x79, 0xa5, 0xdb, 0xc6, 0x6a, 0xa3, 0xd4,
                        0x83, 0x55, 0x87, 0x9b, 0x5c, 0x27, 0xab, 0x77, 0x36, 0x42, 0x61, 0x2d, 0xb4, 0x4d, 0x1a, 0x9f,
                        0x64, 0x8f, 0xd5, 0x43, 0x05, 0x24, 0x72, 0x02, 0x41, 0x15, 0x6d, 0x6e, 0x47, 0x26, 0x65, 0x13,
                        0x49, 0x95, 0xcb, 0x71, 0xef, 0x6f, 0x2c, 0xbd, 0xfd, 0xac, 0x31, 0xde, 0x2a, 0x5e, 0x29, 0xa2,
                        0xbc, 0x28, 0xc7, 0x56, 0xc8, 0x16, 0x75, 0xe4, 0x19, 0xf2, 0xbe, 0x37, 0x90, 0xe8, 0x3b, 0x92,
                        0x35, 0x4b, 0xd2, 0xd7, 0xd8, 0x0e, 0x51, 0x8b, 0xb0, 0xec, 0x4c, 0xb9, 0x59, 0x2e, 0x66, 0x54,
                        0xb5, 0xd1, 0xe5, 0x1e, 0x0f, 0x5a, 0xc4, 0xcd, 0xeb, 0x4e, 0x7c, 0x74, 0x8e, 0xfa, 0x81, 0xe2,
                        0x7f, 0x9a, 0xa8, 0x5f, 0x32, 0x89, 0x98, 0x07, 0x39, 0xc0, 0x38, 0x1f, 0x01, 0xe0, 0x8d, 0x1d,
                        0xf3, 0x96, 0x57, 0xa0, 0x0b, 0xed, 0x09, 0x18, 0x8c, 0xf4, 0x0d, 0x21, 0xd9, 0x23, 0x78, 0xa1,
                        0xc1, 0xfe, 0x3c, 0x30, 0x73, 0x5d, 0x40, 0x99, 0xb6, 0x6c, 0x7b, 0x14, 0xca, 0xc2, 0xe6, 0x8a,
                        0xe9, 0xae, 0x7a, 0xd6, 0xf1, 0x3d, 0xa4, 0x34, 0x2b, 0xda, 0x63, 0x84, 0xb3, 0xaf, 0x88, 0xb7,
                        0x45, 0x97, 0xdc, 0x20, 0x17, 0x3f, 0x4a, 0x52, 0x03, 0x33, 0xdf, 0xd3, 0x04, 0x69, 0x91, 0xf0,
                        0xf9, 0xcc, 0x50, 0x44, 0xff, 0x80, 0x58, 0xf7, 0xc9, 0x60, 0x6b, 0x53, 0xa7, 0x85, 0xee, 0x1c, 
                        };


        bool ExactFirmware()
        {
            if( raw.Length < 4 )
            {
                return false;
            }

            int pos = 0x20;
            uint count = Sys.ReadUint32(raw, pos + 0);
            uint headerlen = Sys.ReadUint32(raw, pos + 4);
            uint dummy1 = Sys.ReadUint32(raw, pos + 8);
            uint dummy2 = Sys.ReadUint32(raw, pos + 12);
            pos += 16;

            if( headerlen > raw.Length ||
                (count * 30 ) > raw.Length )
            {
                return false;
            }

            header.Clear();

            // Read Header
            for (int c = 0; c < count; c++)
            {
                string firmwareName = Sys.ReadString(raw, pos + 0, 16);
                uint start = Sys.ReadUint32(raw, pos + 16);
                uint len = Sys.ReadUint32(raw, pos + 20);
                uint hdummy1 = Sys.ReadUint32(raw, pos + 24);
                uint hdummy2 = Sys.ReadUint32(raw, pos + 28);

                pos += 32;
                if (start >= raw.Length ||
                    (start + len) > raw.Length)
                {
                    return false;
                }

                header.Add(new Tuple<string, uint, uint>(firmwareName, start, len));
            }

            foreach (var t in header)
            {
                var block = new byte[t.Item3];
                Array.Copy(raw, t.Item2, block, 0, t.Item3);
                blocks.Add(block);
            }

            return true;
        }

            
    }
}
