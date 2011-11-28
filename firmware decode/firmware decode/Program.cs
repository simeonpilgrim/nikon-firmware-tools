using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Diagnostics;

namespace Nikon_Decode
{
    partial class Program
    {

        static void Main(string[] args)
        {
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0101.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0102.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0103.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D5100_0101.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3100_0101.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D300S101.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3S_0101.bin");

            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0102.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0103.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D5100_0101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3100_0101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D300S101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3S_0101.bin");

 
            //SearchWords(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0101.bin");
            //SearchWords(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0102.bin");
            //SearchWords(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3100_0101.bin");
            //SearchWords(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D300S101.bin");
            //SearchWords(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3S_0101.bin");
            //SearchWords(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D5100_0101.bin");

            //SearchWords2(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D5100_0101.bin");

            //TryCRC_16(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin");
            //TryCRC_16(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b740101b.bin");
            //TryCRC_16(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b810101b.bin");
            //TryCRC_16(@"C:\Users\spilgrim\Downloads\Nikon\Decode\bd3s101c.bin");
            //TryCRC_16(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b750101b.bin"); 
            //TryCRC_16(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b750102a.bin");

            SearchJpegs(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin");
            SearchJpegs(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b740101b.bin");
            SearchJpegs(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b810101b.bin");
            SearchJpegs(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b750102a.bin");
            SearchJpegs(@"C:\Users\spilgrim\Downloads\Nikon\Decode\bd3s101c.bin");


            //TryCRC_16(@"C:\Temp\b640101b-HaCkEd.bin");
        }


        static byte[] Xor_Ord1 = {
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

        static byte[] Xor_Ord2 = {
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

        static int[] Xor_Ord3 = {
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

        static void DecodePackageFile(string fileName)
        {
            if (File.Exists(fileName))
            {
                BinaryReader br = null;
                BinaryWriter bw = null;

                try
                {
                    br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite));
                    bw = new BinaryWriter(File.Open(fileName + ".out.bin", FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

                    int count = (int)br.BaseStream.Length;
                    byte[] data = br.ReadBytes(count);

                    for (int i = 0; i < count; i++)
                    {
                        int ord1_idx = i & 0xFF;
                        int ord2_idx = (i >> 8) & 0xFF;
                        int ord3_idx = (i >> 16) & 0xFF;

                        int b = data[i] ^ Xor_Ord1[ord1_idx] ^ Xor_Ord2[ord2_idx] ^ Xor_Ord3[ord3_idx];
 
                        data[i] = (byte)b;
                    }

                    bw.Write(data);
                }
                finally
                {
                    if (br != null)
                        br.Close();
                    if (bw != null)
                        bw.Close();
                }
            }
        }

        static UInt32 ReadUint32(BinaryReader br)
        {
            byte[] b = br.ReadBytes(4);
            return (UInt32)(b[0] << 24 | b[1] << 16 | b[2] << 8 | b[3]);
        }

        static string ReadString(BinaryReader br, int count)
        {
            byte[] b = br.ReadBytes(count);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++)
            {
                byte c = b[i];
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


        static void ExactFirmware(string fileName)
        {
            if (File.Exists(fileName))
            {
                BinaryReader br = null;

                try
                {
                    br = new BinaryReader(File.Open(fileName + ".out2.bin", FileMode.Open, FileAccess.Read, FileShare.ReadWrite));
                    br.BaseStream.Seek(0x20, SeekOrigin.Begin);

                    uint count = ReadUint32(br);
                    uint headerlen = ReadUint32(br);
                    uint dummy1 = ReadUint32(br);
                    uint dummy2 = ReadUint32(br);

                    var header = new List<Tuple<string, uint, uint>>();

                    // Read Header
                    for (int c = 0; c < count; c++)
                    {
                        string firmwareName = Path.Combine(Path.GetDirectoryName(fileName), ReadString(br, 16));
                        uint start = ReadUint32(br);
                        uint len = ReadUint32(br);
                        uint hdummy1 = ReadUint32(br);
                        uint hdummy2 = ReadUint32(br);

                        header.Add(new Tuple<string, uint, uint>(firmwareName, start, len));
                    }

                    foreach (var t in header)
                    {
                        DumpFile(br, t.Item1, t.Item2, t.Item3);
                    }
                }
                finally
                {
                    if (br != null)
                        br.Close();
                }
            }
        }

        static void DumpFile(BinaryReader br, string fileName, uint start, uint len)
        {
            BinaryWriter bw = null;

            try
            {
                bw = new BinaryWriter(File.Open(fileName, FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

                br.BaseStream.Seek(start, SeekOrigin.Begin);

                var data = br.ReadBytes((int)len);

                bw.Write(data);
            }
            finally
            {
                if (bw != null)
                    bw.Close();
            }
        }


        static void SearchWords(string fileName)
        {

            if (File.Exists(fileName))
            {
                BinaryReader br = null;
                StreamWriter sw = null;

                try
                {
                    int base_pos = 0;

                    br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite));
                    sw = new StreamWriter(File.Open(fileName + ".words.txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

                    while (br.BaseStream.Position < br.BaseStream.Length)
                    {
                        long count = Math.Min(br.BaseStream.Length - br.BaseStream.Position, 256 * 256);

                        if (count > 0)
                        {
                            byte[] data = br.ReadBytes((int)count);
                            byte[] datab = (byte[])data.Clone();

                            for (int m = 0; m < 256; m++)
                            {
                                for (int ii = 0; ii < count; ii++)
                                {
                                    int pos = base_pos + ii;
                                    int ord1_idx = pos & 0xFF;
                                    int ord2_idx = (pos >> 8) & 0xFF;

                                    int b = data[ii] ^ Xor_Ord1[ord1_idx] ^ Xor_Ord2[ord2_idx];

                                    datab[ii] = (byte)(b ^ m);
                                }

                                // N - 4e, i - 69, k - 6b, o - 6f, n - 6E
                                for (int ii = 0; ii < count - 4; ii++)
                                {
                                    if (datab[ii + 0] == 0x4e &&
                                          datab[ii + 1] == 0x69 &&
                                          datab[ii + 2] == 0x6b &&
                                          datab[ii + 3] == 0x6f &&
                                          datab[ii + 4] == 0x6e)
                                    {
                                        sw.WriteLine("0x{0:x6} mask: 0x{1:x2} {2}{3}{4}{5}{6}", base_pos, m,
                                            (char)datab[ii + 0], (char)datab[ii + 1], (char)datab[ii + 2], (char)datab[ii + 3], (char)datab[ii + 4]);

                                        break;
                                    }
                                }

                            }
                        }
                        base_pos += (int)count;

                    }
                }
                finally
                {
                    if (br != null)
                        br.Close();
                    if (sw != null)
                        sw.Close();
                }
            }
        }

        static void SearchWords2(string fileName)
        {

            if (File.Exists(fileName))
            {
                BinaryReader br = null;
                StreamWriter sw = null;

                try
                {
                    int base_pos = 0;

                    br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite));
                    sw = new StreamWriter(File.Open(fileName + ".words2.txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

                    while (br.BaseStream.Position < br.BaseStream.Length)
                    {
                        long count = Math.Min(br.BaseStream.Length - br.BaseStream.Position, 256 * 256);

                        if (count > 0)
                        {
                            byte[] data = br.ReadBytes((int)count);
                            byte[] datab = (byte[])data.Clone();

                            for (int m = 0; m < 256; m++)
                            {
                                for (int ii = 0; ii < count; ii++)
                                {
                                    int pos = base_pos + ii;
                                    int ord1_idx = pos & 0xFF;
                                    int ord2_idx = (pos >> 8) & 0xFF;

                                    int b = data[ii] ^ Xor_Ord1[ord1_idx] ^ Xor_Ord2[ord2_idx];

                                    datab[ii] = (byte)(b ^ m);
                                }

                                // J 4A, F 46, I 49, F 46

                                for (int ii = 0; ii < count - 4; ii++)
                                {
                                    if (datab[ii + 0] == 0x4a &&
                                          datab[ii + 1] == 0x46 &&
                                          datab[ii + 2] == 0x49 &&
                                          datab[ii + 3] == 0x46)
                                    {
                                        sw.WriteLine("0x{0:x6} mask: 0x{1:x2} {2}{3}{4}{5}", base_pos, m,
                                            (char)datab[ii + 0], (char)datab[ii + 1], (char)datab[ii + 2], (char)datab[ii + 3]);

                                        break;
                                    }
                                }

                            }
                        }
                        base_pos += (int)count;

                    }
                }
                finally
                {
                    if (br != null)
                        br.Close();
                    if (sw != null)
                        sw.Close();
                }
            }
        }

        static void SearchJpegs(string fileName)
        {
            if (File.Exists(fileName))
            {
                byte[] data;
                using (var br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite)))
                {
                    data = br.ReadBytes((int)br.BaseStream.Length);
                }

                List<int> markers = new List<int>();
                for (int i = 0; i < (data.Length - 4); i++)
                {
                    // J 4A, F 46, I 49, F 46
                    if (data[i + 0] == 0x4a &&
                          data[i + 1] == 0x46 &&
                          data[i + 2] == 0x49 &&
                          data[i + 3] == 0x46)
                    {
                        markers.Add(i-6);
                    }
                }
                markers.Add(data.Length);

                for (int i = 0; i < markers.Count - 1; i++)
                {
                    var name = string.Format("{0}.{1:x6}.jpg", fileName, markers[i]);
                    using (var bw = new BinaryWriter(File.Open(name, FileMode.Create)))
                    {
                        bw.Write(data, markers[i], markers[i + 1] - markers[i]);
                    }
                }
            }
        }



    }
}
