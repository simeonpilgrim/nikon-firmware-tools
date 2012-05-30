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
            DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D4__0101.bin");
            DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D800_0101.bin");
            DecodePackageFile(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D800E_0101.bin");

            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\V1_0111.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\J1_0111.bin");

            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0102.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0103.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D5100_0101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3100_0101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D300S101.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3S_0101.bin");
            ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D4__0101.bin");
            ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D800_0101.bin");
            ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D800E_0101.bin");


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

            //InteractiveTextD5100(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin");
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
                    int width = data[pos + 6];
                    int loc = (int)ReadUint32(data, pos + 8) - 0x040000; // Fix RAM space -> File space.
                    int indextab = (int)ReadUint32(data, pos + 12);

                    int ll = last_loc - loc;
                    int v1 = dummy1 & 0x01ff;
                    int v2 = (dummy1 >> 9) & 0x7f;
                   // var ol = new Overlay(dummy1, loc, width, 
                    Debug.WriteLine("D: {0:X8} W: {1:X2}  ({2:X8}) L: {3:X8} I: {4:X8} Len: {5:X8} {6:X8} {7:X8} {8} {9}",
                        dummy1, width, ReadUint32(data, pos + 4), loc, indextab, ll, v1, v2, ((double)ll) / ((double)v1), ((double)ll) / ((double)width));

                    //if (loc != 0 && width > 0 && last_loc != 0)
                    //{
                    //    int lenght = last_loc - loc;
                    //    int height = (lenght * 8) / (width);

                    //    var name = string.Format("{0}_{1:X8}_BW.png", Path.Combine(Path.Combine(Path.GetDirectoryName(fileName), "Overlays"), Path.GetFileNameWithoutExtension(fileName)), loc);

                    //    DumpRGBLineStrippedBig1BitBW(data, name, data.Length, loc, width, height);
                    //}


                    last_loc = loc;
                }
            }
        }

        static void DumpRGBLineStrippedBig1Bit(byte[] dataIn, string name, int fileSize, int offset, int Pix_X, int Pix_Y)
        {
            var bitmap = new Bitmap(Pix_X, Pix_Y, PixelFormat.Format24bppRgb);

            int inWidth = (Pix_X + 7) / 8;

            // 1 pixel
            for (int j = 0; j < Pix_Y; j++)
            {
                //int j_off = j * RowsStep;

                if (offset + (inWidth * 3) < fileSize)
                {
                    // RGB stripped by row.
                    for (int i = 0; i < Pix_X; i++)
                    {
                        int shift = 7 - (i % 8);
                        int in_off = i / 8;
                        int c_scale = 255 / 1; // scale below values to 0 - 255 range;
                        int r = ((dataIn[offset + in_off] >> shift) & 0x01) * c_scale;
                        int g = ((dataIn[offset + in_off + inWidth] >> shift) & 0x01) * c_scale;
                        int b = ((dataIn[offset + in_off + inWidth + inWidth] >> shift) & 0x01) * c_scale;

                        bitmap.SetPixel(i, j, Color.FromArgb(r, g, b));
                    }
                    offset += inWidth * 3;
                }
            }

            if (File.Exists(name)) File.Delete(name);

            bitmap.Save(name, ImageFormat.Png);
        }

        static void DumpRGBLineStrippedBig1BitBW(byte[] dataIn, string name, int fileSize, int offset, int Pix_X, int Pix_Y)
        {
            var bitmap = new Bitmap(Pix_X, Pix_Y, PixelFormat.Format24bppRgb);

            int inWidth = (Pix_X + 7) / 8;

            // 1 pixel
            for (int j = 0; j < Pix_Y; j++)
            {
                if (offset + (inWidth * 3) < fileSize)
                {
                    // BW stripped by row.
                    for (int i = 0; i < Pix_X; i++)
                    {
                        int shift = 7 - (i % 8);
                        int in_off = i / 8;
                        int c_scale = 255 / 1; // scale below values to 0 - 255 range;
                        int r = ((dataIn[offset + in_off] >> shift) & 0x01) * c_scale;
                        //int g = ((dataIn[offset + in_off + inWidth] >> shift) & 0x01) * c_scale;
                        //int b = ((dataIn[offset + in_off + inWidth + inWidth] >> shift) & 0x01) * c_scale;

                        bitmap.SetPixel(i, j, Color.FromArgb(r, r, r));
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
