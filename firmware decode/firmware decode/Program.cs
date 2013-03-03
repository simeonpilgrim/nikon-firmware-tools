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

        static void Main(string[] args)
        {
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0101.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0102.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0103.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D5100_0101.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3100_0101.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D300S101.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3S_0101.bin");
            //DecodePackageFile1(@"C:\Users\spilgrim\Downloads\Nikon\Decode\V1_0111.bin");
            //DecodePackageFile1(@"C:\Users\spilgrim\Downloads\Nikon\Decode\J1_0111.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D4__0101.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D4__0102.bin"); 
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D4__0103.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D800_0101.bin");
            //DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D800E_0101.bin");

            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\V1_0111.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\J1_0111.bin");

            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0102.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0103.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D5100_0101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3100_0101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D300S101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3S_0101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D4__0101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D4__0102.bin");    
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D4__0103.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D800_0101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D800E_0101.bin");


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

            //SearchJpegs(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin");
            //SearchJpegs(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b740101b.bin");
            //SearchJpegs(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b810101b.bin");
            //SearchJpegs(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b750102a.bin");
            //SearchJpegs(@"C:\Users\spilgrim\Downloads\Nikon\Decode\bd3s101c.bin");


            //TryCRC_16(@"C:\Temp\b640101b-HaCkEd.bin");
            //CalcCRC(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D5100_0101.bin.out.bin");
            //CalcCRC(@"C:\Temp\D5100_0101.bin.out2.bin");

            //SearchTextPointers(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin");

            //SaveOverlays(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin", 0x35f218, 0x35fad8);
            //SaveOverlays(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin", 0x360890, 0x360a40);
            //SaveOverlays(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin", 0x35fadc, 0x360848); // Font table

            //DumpMenusD5100(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin");
            //DumpMenusD3s(@"C:\Users\spilgrim\Downloads\Nikon\Decode\bd3s101c.bin");
            //DumpMenusD7000(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b750103a.bin");
            //DumpMenusD300S(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b810101b.bin");
            //DumpMenusD700(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D700_0103.bin_B.bin");

            //InteractiveTextD5100(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin");

            //SearchDumps(@"C:\Dev\libgphoto2-2.5.0\examples\D3000_");
            MergeDumps(@"C:\Dev\libgphoto2-2.5.0\examples\testa");

        }

        static void MergeDumps(string dir)
        {         
            Directory.CreateDirectory(Path.Combine(dir, "merged"));

            var files = Directory.GetFiles(dir, "*.bin").ToArray();
            //Nikon_func_0xfe31_0x2a00d000_0x00000800_0x00000000
            //01234567890123456789012345678
            var list = files.OrderBy(a => Int64.Parse(Path.GetFileName(a).Substring(20, 8), NumberStyles.HexNumber));

            int step = 0x800;
            Int64 last = -1;
            BinaryWriter bw = null;
            byte[] data;

            foreach (var file in list)
            {
                Int64 num = Int64.Parse(Path.GetFileName(file).Substring(20, 8), NumberStyles.HexNumber);

                if (num != (last + step))
                {
                    if (bw != null)
                    {
                        bw.Close();
                        bw.Dispose();
                    }
                    var newfile = Path.Combine(dir, string.Format("Nikon_{0:X8}.bin", num));
                    bw = new BinaryWriter(File.Open(newfile, FileMode.Append, FileAccess.Write));
                }

                using (var br = new BinaryReader(File.Open(file, FileMode.Open, FileAccess.Read, FileShare.ReadWrite)))
                {
                    data = br.ReadBytes((int)br.BaseStream.Length);
                    bw.Write(data);
                }

                var toPath = Path.Combine(Path.Combine(dir, "merged"), Path.GetFileName(file));
                File.Move(file, toPath);

                last = num;
            }

            if (bw != null)
            {
                bw.Close();
                bw.Dispose();
            }

        }

        static void SearchDumps(string dir)
        {
            //var bcode = new byte[] { 0x3C, 0x1A, 0xBF, 0xC0, 0x27, 0x5A, 0x05, 0x00, 0x03, 0x40, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00 };
            //var bcode = new byte[] { 0x17, 0x7A, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x40, 0x1A };
            //var bcode = new byte[] { 0x67, 0x45, 0x23, 0x01 };
            //var bcode = new byte[] { 0x00, 0x00, 0x00, 0xC0, 0x11 };
            var bcode = new byte[] { 0x53, 0x6F, 0x66, 0x74, 0x75, 0x6E, 0x65, 0x20 }; // "Softune "
            //var bcode = new byte[] { 0x8C, 0xFF, 0x8D, 0x7F, 0x83, 0xDF, 0x97, 0x30 };  

            var sw = new StreamWriter(File.Open(Path.Combine(dir, "status.txt"), FileMode.Create, FileAccess.Write, FileShare.ReadWrite));
            var sw2 = new StreamWriter(File.Open(Path.Combine(dir, "rm.cmd"), FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

            foreach (var file in Directory.GetFiles(dir, "*.bin"))
            {
                BinaryReader br = null;

                byte[] data;

                using (br = new BinaryReader(File.Open(file, FileMode.Open, FileAccess.Read, FileShare.ReadWrite)))
                {
                    data = br.ReadBytes((int)br.BaseStream.Length);
                }

                if (data.Length == 0) continue;

                bool findB = false;
                bool allSame = true;
                byte first = data[0];
                for (int i = 0; i < data.Length; i++)
                {
                    if (allSame && first != data[i]) { allSame = false; }
                    //if (allSame && first != data[i]) { allSame = false; break; }

                    if (findB == false && (i + bcode.Length) < data.Length)
                    {
                        bool bsame = true;
                        for (int j = 0; j < bcode.Length && bsame; j++)
                        {
                            bsame = data[i + j] == bcode[j];
                        }

                        if (bsame)
                        {
                            sw.WriteLine("{0} Bcode {1:X8}", Path.GetFileName(file), i);
                            Console.WriteLine("{0} Bcode {1:X8}", Path.GetFileName(file), i);
                           
                        }
                    }

                }

                if (allSame)
                {
                    sw2.WriteLine("del {0}", file);
                }
                //sw.WriteLine("{0} all same {1}", Path.GetFileName(file), allSame);
            }

            sw2.Close();
            sw2.Dispose();
            sw.Close();
            sw.Dispose();
        }


        static UInt32 ReadUint32(BinaryReader br)
        {
            byte[] b = br.ReadBytes(4);
            return (UInt32)(b[0] << 24 | b[1] << 16 | b[2] << 8 | b[3]);
        }

        static UInt32 ReadUint32(byte[] data, long pos)
        {
            return (UInt32)(data[pos + 0] << 24 | data[pos + 1] << 16 | data[pos + 2] << 8 | data[pos + 3]);
        }

        static UInt16 ReadUint16(byte[] data, long pos)
        {
            return (UInt16)(data[pos + 0] << 8 | data[pos + 1]);
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

      

        class Overlay
        {
            public int dummy1;
            public int loc;
            public int width;
            public int height;
            public Overlay(int du, int lo, int wi, int he)
            {
                dummy1 = du;
                loc = lo;
                width = wi;
                height = he;
            }
        }

        static void SaveOverlays(string fileName, int start, int stop)
        {
            if (File.Exists(fileName))
            {
                BinaryReader br = null;

                byte[] data;

                using (br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite)))
                {
                    data = br.ReadBytes((int)br.BaseStream.Length);
                }

                int last_loc = 0;
                for (uint pos = (uint)start; pos < stop; pos += 16)
                {
                    int dummy1 = (int)ReadUint32(data, pos);
                    int dis_width = data[pos + 0];
                    int dis_height = data[pos + 1];
                    int dis_step_x = data[pos + 2];
                    int dis_step_y = data[pos + 3];


                    int height = data[pos + 1];
                    int decode_width = data[pos + 6];
                    int flags = data[pos + 7];
                    int ram = (int)ReadUint32(data, pos + 8);
                    int loc = ram - 0x040000; // Fix RAM space -> File space.
                    int indextab = (int)ReadUint32(data, pos + 12);



                    int lenght = last_loc - loc;

                    var var_wid = DecodeVarWidths(data, flags, indextab, dis_height, lenght);
                    int total_height = (lenght * 8) / (decode_width);

                    Debug.WriteLine("H: {8} ({0}) W: {1}  ({2:X8}) L: {3:X8} I: {4:X8} Len: {5:X8} Vari: {6} TH: {7} Count: {9}",
                        string.Format("{0},{1} {2},{3}",
                        dis_width, dis_height, dis_step_x, dis_step_y), decode_width, ReadUint32(data, pos + 4), loc, indextab, lenght, flags, total_height, height, total_height/height);

                    if (loc != 0 && decode_width > 0 && last_loc != 0)
                    {
                        var name = string.Format("{0}_{1:X8}_BW.png", Path.Combine(Path.Combine(Path.GetDirectoryName(fileName), "Overlays"), Path.GetFileNameWithoutExtension(fileName)), loc);

                        if (var_wid.Count > 0)
                        {
                            DumpLineStrippedVariWdith(data, var_wid, dis_height, name, data.Length, loc, decode_width, dis_height*var_wid.Count);

                        }
                        else
                        {
                            DumpRGBLineStrippedBig1BitBW(data, name, data.Length, loc, decode_width, total_height);
                        }
                    }


                    last_loc = loc;
                }
            }
        }

        static List<int> DecodeVarWidths(byte[] data, int flags, int indextab, int dis_height, int length)
        {
            List<int> words = new List<int>();
            List<int> var_wid = new List<int>();

            if ((flags & 0x80) != 0)
            {
                int index_loc = indextab - 0x40000;
                int last_word = -1;
                int off = 0;

                int word = ReadUint16(data, index_loc);
                while (word > last_word &&
                    word < length)
                {
                    words.Add(word);
                    last_word = word;
                    off += 2;

                    word = ReadUint16(data, index_loc + off);
                }
                words.Add(length);

                for (int i = 0; i < (words.Count - 1); i++)
                {
                    int diff = words[i + 1] - words[i];
                    int width = (diff / dis_height) * 8;

                    var_wid.Add(width);
                }
            }
            return var_wid;
        }


        static void DumpLineStrippedVariWdith(byte[] dataIn,List<int> widths, int height,  string name, int fileSize, int offset, int Pix_X, int Pix_Y)
        {
            var bitmap = new Bitmap(Pix_X*3, Pix_Y, PixelFormat.Format24bppRgb);


            for (int v = 0; v < widths.Count; v++)
            {
 
                Pix_X = widths[v];
                int inWidth = (Pix_X + 7) / 8;

                for (int j = 0; j < height; j++)
                {
                    if (offset + inWidth < fileSize)
                    {
                        for (int i = 0; i < Pix_X; i++)
                        {
                            int shift = 7 - (i % 8);
                            int in_off = i / 8;
                            int c = ((dataIn[offset + in_off] >> shift) & 0x01) * 255;

                            bitmap.SetPixel(i, j + (v * height), Color.FromArgb(c, c, c));
                        }
                        offset += inWidth;
                    }
                }
            }
            if (File.Exists(name)) File.Delete(name);

            bitmap.Save(name, ImageFormat.Png);
        }


        static void DumpRGBLineStrippedBig1BitBW(byte[] dataIn, string name, int fileSize, int offset, int Pix_X, int Pix_Y)
        {
            var bitmap = new Bitmap(Pix_X, Pix_Y, PixelFormat.Format24bppRgb);

            int inWidth = (Pix_X + 7) / 8;

            for (int j = 0; j < Pix_Y; j++)
            {
                if (offset + (inWidth * 3) < fileSize)
                {
                    for (int i = 0; i < Pix_X; i++)
                    {
                        int shift = 7 - (i % 8);
                        int in_off = i / 8;
                        int c = ((dataIn[offset + in_off] >> shift) & 0x01) * 255;

                        bitmap.SetPixel(i, j, Color.FromArgb(c, c, c));
                    }
                    offset += inWidth;
                }
            }

            if (File.Exists(name)) File.Delete(name);

            bitmap.Save(name, ImageFormat.Png);
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
                            byte[] check = { 0xff, 0xd8, 0xff, 0xE0, 0x00, 0x10, 0x4a, 0x46, 0x49, 0x46 };

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

                                for (int ii = 0; ii < count - 4; ii++)
                                {
                                    bool match = true;
                                    for (int jj = 0; (jj < check.Length) && match; jj++)
                                        match &= datab[ii + jj] == check[jj];

                                    if( match )
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
                        markers.Add(i - 6);
                    }
                }
                markers.Add(data.Length);

                for (int i = 0; i < markers.Count - 1; i++)
                {
                    //windows can't sort hex correctly, so add i first.
                    //var name = string.Format("{0}_{1:X8}.jpg", Path.Combine(Path.Combine(Path.GetDirectoryName(fileName), "Jpgs"), Path.GetFileNameWithoutExtension(fileName)), markers[i] + 0x40000);
                    var name = string.Format("{0}_{2} {1:X8}.jpg", Path.Combine(Path.Combine(Path.GetDirectoryName(fileName), "Jpgs"), Path.GetFileNameWithoutExtension(fileName)), markers[i] + 0x40000, i);
                    using (var bw = new BinaryWriter(File.Open(name, FileMode.Create)))
                    {
                        bw.Write(data, markers[i], markers[i + 1] - markers[i]);
                    }
                }
            }
        }

        static void SearchTextPointers(string fileName)
        {
            if (File.Exists(fileName))
            {
                byte[] data;
                using (var br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite)))
                {
                    data = br.ReadBytes((int)br.BaseStream.Length);
                }


                int max = data.Length + 0x40000;
                RefSet tab = new RefSet();

                Debug.WriteLine("start build");
                for (int i = 0; i < (data.Length - 4); i += 4) //dword
                {
                    //if (data[i] == 0 && data[i + 1] <= ((max >> 16) & 0xff))
                    //{
                        int v = (data[i + 1] << 16) + (data[i + 2] << 8) + (data[i + 3]);
                        tab.Add(v, i + 0x40000);
                    //}
                }
                Debug.WriteLine("end build");

                long count = 0;
                for (int i = 0; i < (data.Length - 4); i += 2) // word aligned
                {
                    //if ((data[i + 0] & 0x1f) <= 0x08)
                    if ((data[i + 0] & 0x1f) == 0x01 && data[i + 1] == 0xf2)
                    {
                        int ii = i + 0x40000;
                        int t1 = ii - 4;

                        //foreach(var ff in tab.
                        foreach (int j in tab.AllRefs(t1))
                        {
                            Debug.WriteLine("0x{0:X6} {1:x2}{2:x2} t1: 0x{3:X6} j: 0x{4:X6}  ", ii, data[i], data[i + 1], t1, j);

                            int t2 = j - 0x10;
                            foreach (int k in tab.AllRefs(t2))
                            {
                                //int t3 = k - 8;
                                int t3 = k - 4;
                                foreach (int l in tab.AllRefs(t3))
                                {
                                    count++;
                                    Debug.WriteLine("{7,4} 0x{0:X6} {1:x2}{2:x2} t1: 0x{3:X6} t2: 0x{4:X6} t3: 0x{5:X6} l: 0x{6:X6} ", ii, data[i], data[i + 1], t1, t2, t3, l, count);
                                }
                            }
                        }
                    }
                }
            }
        }

    }



    public class RefSet
    {
        SortedList<int, List<int>> tab = new SortedList<int, List<int>>();

        public RefSet()
        {
        }

        public void Add(int a, int b)
        {
            List<int> list;
            if (tab.TryGetValue(a, out list))
            {
                list.Add(b);
            }
            else
            {
                list = new List<int>();
                list.Add(b);
                tab.Add(a, list);
            }
        }

        public IEnumerable AllRefs(int loc)
        {
            List<int> list;
            if (tab.TryGetValue(loc, out list))
            {
                foreach (int t in list)
                {
                    yield return t;
                }
            }
        }

    }
}
