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
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D3100Update\D3100_0101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D3100Update\D3100_0102.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D3200Update\D3200_0101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D3200Update\D3200_0102.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D3200Update\D3200_0103.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D5100Update\D5100_0101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D5100Update\D5100_0102.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D5200Update\D5200_0101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D5200Update\D5200_0102.bin");

            //DOWAHT??(@"C:\Users\spilgrim\Downloads\Nikon\D90Update\D90_0101.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D7000Update\D7000_0101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D7000Update\D7000_0102.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D7000Update\D7000_0103.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D7000Update\D7000_0104.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D7000Update\D7000_0105.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D7000Update\D7100_0101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D7100Update\D7100_0102.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D300SUpdate\D300S101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D300SUpdate\D300S102.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D3SUpdate\D3S_0101.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D600Update\D600_0101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D600Update\D600_0102.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D610Update\D610_0101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\D610_0101a.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D750Update\D750_0101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D750Update\D750_0102.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D800Update\D800_0101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D800Update\D800_0102.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D800Update\D800_0110.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D800EUpdate\D800E_0101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D800EUpdate\D800E_0102.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D800EUpdate\D800E_0110.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D810Update\D810_0102.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D4Update\D4__0101.bin");
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D4Update\D4__0102.bin"); 
            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D4Update\D4__0103.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\D4SUpdate\D4S_0101.bin");

            //DecodeAndExtractFirm(@"C:\Users\spilgrim\Downloads\Nikon\DfUpdate\DF__0101.bin");


            //DecryptNKLDFile(@"C:\Users\spilgrim\Downloads\Nikon\NKLD\NKLD01002.BIN");
            //DecodeNKLDFile(@"C:\Users\spilgrim\Downloads\Nikon\NKLD\NKLD01002.BIN.bin");
            //DecryptNKLDFile(@"C:\Users\spilgrim\Downloads\Nikon\NKLD\NKLD01006.BIN");
            //DecodeNKLDFile(@"C:\Users\spilgrim\Downloads\Nikon\NKLD\NKLD01006.BIN.bin");
            //DecodeNKLDFile(@"C:\Dev\libgphoto2-2.5.1.1\examples\Nikon_func_0xfe63_0x00000000_0x00020000_0x00000000.bin");


            //FR_V.DecodeFile(@"C:\Users\spilgrim\Downloads\Nikon\MemDump\d5100\0x0008A2BC.BIN");



            //DecodePackageFile1(@"C:\Users\spilgrim\Downloads\Nikon\Decode\V1_0111.bin");
            //DecodePackageFile1(@"C:\Users\spilgrim\Downloads\Nikon\Decode\J1_0111.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\V1_0111.bin");
            //ExactFirmware(@"C:\Users\spilgrim\Downloads\Nikon\Decode\J1_0111.bin");

            //DecodePackageFile1(@"C:\Temp\1N 10mm f2.8\1n_10_01000600.bin");
            //ExactFirmware(@"C:\Temp\1N 10mm f2.8\1n_10_01000600.bin");



            //SearchWords(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0101.bin");
            //SearchWords(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D7000_0102.bin");
            //SearchWords(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3100_0101.bin");
            //SearchWords(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D300S101.bin");
            //SearchWords(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D3S_0101.bin");
            //SearchWords(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D5100_0101.bin");

            //SearchWords2(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D5100_0101.bin");

            //TryCRC_16(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin");

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
            //DumpMenusD7000(@"C:\Users\spilgrim\Downloads\Nikon\D7000Update\b750103a.bin");
            //DumpMenusD300S(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b810101b.bin");
            //DumpMenusD700(@"C:\Users\spilgrim\Downloads\Nikon\Decode\D700_0103.bin_B.bin");
            //DumpMenusD800(@"C:\Users\spilgrim\Downloads\Nikon\D800Update\b630101a.bin");

            //DumpRttiD5200(@"C:\Users\spilgrim\Downloads\Nikon\D5200Update\b970101.bin");

            //InteractiveTextD5100(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin");
            //InteractiveTextD7000(@"C:\Users\spilgrim\Downloads\Nikon\D7000Update\b750103a.bin");

            //SearchDumpsForIntDiff(@"C:\Users\spilgrim\Downloads\Nikon\D7000Update\b750103a.bin", 0x79DF9A, 0x79DFB2, 0x79DFBA, 0x79DFC2);
            //SearchDumpsForIntDiff(@"C:\Users\spilgrim\Downloads\Nikon\D7000Update\b750103a.bin", 0x79DFB2, 0x79DFBA, 0x79DFC2);

            //SearchDumpsForIntDiff(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin", 0x74e884, 0x74e891, 0x74e896, 0x74e8a5, 0x74e8b2, 0x74e8c0, 0x74e8d1);
            //SearchDumpsForIntDiff(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b720101_.bin", 0xB426d0, 0xB426dd, 0xB426e3, 0xB426f2, 0xB426fd, 0xB42707, 0xB42717);
            //SearchDumpsForDiff_B(@"C:\Users\spilgrim\Downloads\Nikon\D3000\B5500101.bin", 0x3b3548-0x19618c, 0x196180, 0x19618c, 0x19619C, 0x1961b4, 0x1961c4, 0x1961d4, 0x1961e4, 0x1961f4, 0x196204, 0x196218, 0x196228);

            //SearchDumpsFor(@"C:\Dev\examples\D3000", 0x4A, 0x46, 0x49, 0x46); // JPEG tiff header
            //SearchDumpsFor(@"C:\Dev\examples\D5100", 0x4A, 0x46, 0x49, 0x46); // JPEG tiff header
            //MergeDumps(@"C:\Dev\libgphoto2-2.5.0\examples\testa");
            //FindEmptyBlocks(@"C:\Users\spilgrim\Downloads\Nikon\Decode\b640101b.bin", 0x40000);

           // Dump_D7000_S179(@"C:\Users\spilgrim\Downloads\Nikon\MemDump\D7000\s179.bin");

            //Dump_All_NEF_MakerNotes_002C(@"C:\Users\spilgrim\Pictures\");
            Dump_All_NEF_MakerNotes_002C(System.IO.Directory.GetCurrentDirectory());

        }


        private static void DecodeAndExtractFirm(string filename)
        {
            DecodePackageFile(filename);
            ExactFirmware(filename);
        }

        private static void Dump_D7000_S179_s177_B_A(string filename, string header, byte[] data, int offset, int number)
        {
            var tw = new StreamWriter(File.Open(filename, FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

            tw.WriteLine(header);

            for (int i = 0; i < number; i++)
            {
                int idx = offset + (i * 6);
                int addr = (data[idx + 0] << 8) + data[idx + 1];
                int high = (data[idx + 2] << 8) + data[idx + 4];
                int low = (data[idx + 3] << 8) + data[idx + 5];

                var s = string.Format("0x{3:X4} 0x{0:X4} : 0x{1:X4} {2:X4} ( {4} )", addr, high, low, idx - offset, (high <<16)+(low) );
                            Debug.WriteLine(s);
                            tw.WriteLine(s);


            }

            tw.Close();
            tw.Dispose();
        }

        private static void Dump_D7000_S179_s177_D(string filename, string header, byte[] data, int offset)
        {
            var tw = new StreamWriter(File.Open(filename, FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

            tw.WriteLine(header);


            tw.WriteLine(string.Format("Msg 00: {0}", DataToStr(data, offset + 0, 12)));
            tw.WriteLine(string.Format("Msg 0E a/b: {0}", DataToStr(data, offset + 0xc, 2)));
            tw.WriteLine(string.Format("Msg 0F a/b: {0}", DataToStr(data, offset + 0xe, 2)));
            tw.WriteLine(string.Format("Msg 10: {0}", DataToStr(data, offset + 0x10, 2)));
            tw.WriteLine(string.Format("Msg 12 a/b: {0}", DataToStr(data, offset + 0x12, 2)));
            tw.WriteLine(string.Format("Msg 13: {0}", DataToStr(data, offset + 0x14, 9)));
            tw.WriteLine(string.Format("Msg 1F: {0}", DataToStr(data, offset + 0x1D, 2)));
            tw.WriteLine(string.Format("Msg 23: {0}", DataToStr(data, offset + 0x1F, 7)));

            tw.Close();
            tw.Dispose();
        }

        private static void Dump_D7000_S179_s177_D_a(string filename, string header, byte[] data, int offset)
        {
            var tw = new StreamWriter(File.Open(filename, FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

            tw.WriteLine(header);


            tw.WriteLine(string.Format("Msg 00: {0}", DataToStr(data, offset + 0, 8)));
            tw.WriteLine(string.Format("Msg 0E: {0}", DataToStr(data, offset + 0xC, 6)));
            tw.WriteLine(string.Format("Msg 1F: {0}", DataToStr(data, offset + 0x1A, 2)));
            tw.WriteLine(string.Format("Msg 23: {0}", DataToStr(data, offset + 0x1C, 7)));

            tw.Close();
            tw.Dispose();
        }

        private static void Dump_D7000_S179_X(TextWriter tw, byte[] data, int offset)
        {
            var v0 = (data[offset+0] << 8) + data[offset+1];
            var v1 = (data[offset+2] << 8) + data[offset+3];

            tw.WriteLine(string.Format("{0:X4} {1:X4} ({2})", v0, v1, v1));
        }

        private static void Dump_D7000_S179_s177_E(string filename, byte[] data, int offset)
        {
            var tw = new StreamWriter(File.Open(filename, FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

            for (int v5 = 0; v5 < 7; v5++)
            {
                for (int r10 = 0; r10 < 3; r10++) // 0,1,2
                {
                    for (int r11 = 0; r11 < 1; r11++) // < 9
                    {
                        tw.WriteLine("");
                        tw.WriteLine("v5: {0} r10: {1} r11: {2}", v5, r10, r11);
                    
                        for (int i = 0; i < 9; i++)
                        {
                            int idx = offset + 0x1E6 + (r10 * 0xF30) + (0x17A * r11) + (v5 * 0x36) + (6 * i);
            
                            int addr = (data[idx + 0] << 8) + data[idx + 1];
                            int high = (data[idx + 2] << 8) + data[idx + 4];
                            int low = (data[idx + 3] << 8) + data[idx + 5];

                            var s = string.Format("0x{3:X4} 0x{0:X4} : 0x{1:X4} {2:X4} ( {4} )", addr, high, low, idx - offset, (high <<16)+(low) );
                            tw.WriteLine(s);
                        }
                    }
                }
            }

            tw.Close();
            tw.Dispose();
        }

        private static void Dump_Bit8(TextWriter tw, int b)
        {
            for (int i = 0; i < 8; i++)
            {
                tw.Write((b & (1<<i)) != 0? '1':'0');
            }
            tw.Write(' ');
        }

        //private static int last_12bitsof16(int b0, int b1)
        //{
        //    int r = 0;
        //    //for (int i = 4; i < 8; i++)
        //    //{
        //    //    ((b & (1 << i)) != 0 ? (1) : 0);
        //    //}         
        //}
        private static void Dump_D7000_S179_ISO_tab(string filename, byte[] data, int offset, int num)
        {
            var tw = new StreamWriter(File.Open(filename, FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

            for (int i = 0; i < num; i=i+1)
            {
                var b0 = data[offset + 0 + (i * 8)];
                var b1 = data[offset + 1 + (i * 8)];
                var b2 = data[offset + 2 + (i * 8)];
                var b3 = data[offset + 3 + (i * 8)];
                var b4 = data[offset + 4 + (i * 8)];
                var b5 = data[offset + 5 + (i * 8)];
                var b6 = data[offset + 6 + (i * 8)];
                var b7 = data[offset + 7 + (i * 8)];

                var v0 = (b0 >> 4) + (b1 << 4);
                var v1 = (b3 << 8) + b2;
                var v2 = (b5 << 8) + b4;
                var v3 = (b7 << 8) + b6;
                Dump_Bit8(tw, b0);
                Dump_Bit8(tw, b1);
                Dump_Bit8(tw, b2);
                Dump_Bit8(tw, b3);
                Dump_Bit8(tw, b4);
                Dump_Bit8(tw, b5);
                Dump_Bit8(tw, b6);
                Dump_Bit8(tw, b7);

                tw.WriteLine(string.Format("{0,4} {1:X4} {2:X4} {3:X4} {4:X4}   {1} {2} {3} {4}", i, v0, v1, v2, v3));
            }

            tw.Close();
            tw.Dispose();
        }

        private static void Dump_D7000_S179(string fileName)
        {
            if (File.Exists(fileName))
            {
                BinaryReader br = null;
                StreamWriter tw = null;

                try
                {
                    br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite));
                    tw = new StreamWriter(File.Open(fileName + ".txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

                    var data = br.ReadBytes((int)br.BaseStream.Length);

                    Dump_D7000_S179_s177_B_A(fileName + " - 25 fps.txt", "0x02560  fpsIdx_1345_3 25fps",  data, 0x02560, (0x02662 - 0x02560) / 6);
                    Dump_D7000_S179_s177_B_A(fileName + " - 30 fps.txt", "0x02662 fpsIdx_1345_4 30fps", data, 0x02662, (0x02662 - 0x02560) / 6);
                    Dump_D7000_S179_s177_B_A(fileName + " - 10 fps.txt", "0x0245E fpsIdx_1345_2 10fps", data, 0x0245E, (0x02662 - 0x02560) / 6);
                    Dump_D7000_S179_s177_B_A(fileName + " - 24 fps.txt", "0x0235C fpsIdx_1345_1 24fps", data, 0x0235C, (0x02662 - 0x02560) / 6);

                    Dump_D7000_S179_s177_D(fileName + "D - 24 fps.txt", "0595 fpsIdx_1345_1 24fps", data, 0x0595);
                    Dump_D7000_S179_s177_D(fileName + "D - 25 fps.txt", "0606 fpsIdx_1345_1 25fps", data, 0x0606);
                    Dump_D7000_S179_s177_D(fileName + "D - 30 fps.txt", "0640 fpsIdx_1345_1 30fps", data, 0x0640);
                    Dump_D7000_S179_s177_D(fileName + "D - 30 fps.txt", "0640 fpsIdx_1345_1 30fps", data, 0x0640);

                    Dump_D7000_S179_s177_D_a(fileName + "D - 2.txt", "05CF", data, 0x05CF);
                    Dump_D7000_S179_s177_D_a(fileName + "D - 5 AF.txt", "067A", data, 0x067A);

                    tw.WriteLine("1E994A fpsIdx_b_4_30 A");
                    Dump_D7000_S179_X(tw, data, 0x19b4);
                    Dump_D7000_S179_X(tw, data, 0x19b8);
                    Dump_D7000_S179_X(tw, data, 0x1968);
                    Dump_D7000_S179_X(tw, data, 0x19bc);

                    tw.WriteLine("1E994A fpsIdx_b_4_30 B");
                    Dump_D7000_S179_X(tw, data, 0x1960);
                    Dump_D7000_S179_X(tw, data, 0x1968);
                    Dump_D7000_S179_X(tw, data, 0x1970);

                    tw.WriteLine("1E994A fpsIdx_b_4_30 END");
                    Dump_D7000_S179_X(tw, data, 0x1978);
                    Dump_D7000_S179_X(tw, data, 0x1980);


                    tw.WriteLine();
                    tw.WriteLine("1E9A08 fpsIdx_b_3_25 A");
                    Dump_D7000_S179_X(tw, data, 0x1938);
                    Dump_D7000_S179_X(tw, data, 0x193C);
                    Dump_D7000_S179_X(tw, data, 0x18EC);
                    Dump_D7000_S179_X(tw, data, 0x1940);

                    tw.WriteLine("1E9A08 fpsIdx_b_3_25 B");
                    Dump_D7000_S179_X(tw, data, 0x18E4);
                    Dump_D7000_S179_X(tw, data, 0x18Ec);
                    Dump_D7000_S179_X(tw, data, 0x18F4);

                    tw.WriteLine("1E9A08 fpsIdx_b_3_25 END");
                    Dump_D7000_S179_X(tw, data, 0x18FC);
                    Dump_D7000_S179_X(tw, data, 0x1904);


                    tw.WriteLine();
                    tw.WriteLine("1E9AC0 fps_idx_1_24 A");
                    Dump_D7000_S179_X(tw, data, 0x184C);
                    Dump_D7000_S179_X(tw, data, 0x1850);
                    Dump_D7000_S179_X(tw, data, 0x1800);
                    Dump_D7000_S179_X(tw, data, 0x1854);

                    tw.WriteLine("1E9AC0 fps_idx_1_24 B");
                    Dump_D7000_S179_X(tw, data, 0x17f8);
                    Dump_D7000_S179_X(tw, data, 0x1800);
                    Dump_D7000_S179_X(tw, data, 0x1808);

                    tw.WriteLine("1E9AC0 fps_idx_1_24 END");
                    Dump_D7000_S179_X(tw, data, 0x1810);
                    Dump_D7000_S179_X(tw, data, 0x1918);


                    tw.WriteLine();
                    tw.WriteLine("1E98A8 fpsIdx_b_2");
                    Dump_D7000_S179_X(tw, data, 0x1874);
                    Dump_D7000_S179_X(tw, data, 0x1878);
                    Dump_D7000_S179_X(tw, data, 0x187C);
                    Dump_D7000_S179_X(tw, data, 0x1880);
                    Dump_D7000_S179_X(tw, data, 0x1884);


                    tw.WriteLine();
                    tw.WriteLine("1E98C2 fpsIdx_b_5_AF? A");
                    Dump_D7000_S179_X(tw, data, 0x1A24);
                    Dump_D7000_S179_X(tw, data, 0x1A28);
                    Dump_D7000_S179_X(tw, data, 0x19DC);
                    Dump_D7000_S179_X(tw, data, 0x1A2C);
                    Dump_D7000_S179_X(tw, data, 0x19DC);
                    tw.WriteLine("1E98C2 fpsIdx_b_5_AF? B");
                    Dump_D7000_S179_X(tw, data, 0x19DC);
                    Dump_D7000_S179_X(tw, data, 0x19E0);
                    Dump_D7000_S179_X(tw, data, 0x19E4);
                    Dump_D7000_S179_X(tw, data, 0x1AE8);
                    Dump_D7000_S179_X(tw, data, 0x19EC);

                    tw.WriteLine();
                    tw.WriteLine("1E97AA x0E_eq_1");
                    Dump_D7000_S179_X(tw, data, 0x1760);
                    Dump_D7000_S179_X(tw, data, 0x1768);
                    Dump_D7000_S179_X(tw, data, 0x1718 + 8);
                    Dump_D7000_S179_X(tw, data, 0x1770);
                    Dump_D7000_S179_X(tw, data, 0x1718 + 10);

                    tw.WriteLine();
                    tw.WriteLine("1E97AA x0E_eq_2");
                    Dump_D7000_S179_X(tw, data, 0x1764);
                    Dump_D7000_S179_X(tw, data, 0x176C);
                    Dump_D7000_S179_X(tw, data, 0x1718 + 8);
                    Dump_D7000_S179_X(tw, data, 0x1774);
                    Dump_D7000_S179_X(tw, data, 0x1718 + 0x10);


                    Dump_D7000_S179_s177_E(fileName + "_E.txt", data, 0x5EB0);

                    Dump_D7000_S179_ISO_tab(fileName + "_iso.txt", data, 0xC48A, 120);
                }
                finally
                {
                    if (br != null)
                    {           
                        br.Close();
                        br.Dispose();
                    }
                    if (tw != null)
                    {
                        tw.Close();
                        tw.Dispose();
                    }
                }
            }
        }

        private static void Dump_All_NEF_MakerNotes_002C(string path)
        {
            if (Directory.Exists(path))
            {
                StreamWriter tw = null;
                try
                {
                    tw = new StreamWriter(File.Open(Path.Combine(path, "search_002C.txt"), FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

                    foreach( var file in Directory.EnumerateFiles(path, "*.NEF", SearchOption.AllDirectories))
                    {
                        Dump_NEF_MakerNotes_002C(file, tw);
                    }
                }
                finally
                {
                    if (tw != null)
                    {
                        tw.Close();
                        tw.Dispose();
                    }
                }
            }
        }

        private static void Dump_NEF_MakerNotes_002C(string fileName, StreamWriter tw)
        {
            if (File.Exists(fileName))
            {
                BinaryReader br = null;
 
                try
                {
                    br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite));

                    var data = br.ReadBytes((int)br.BaseStream.Length);
                    var datalen = data.Length;
                    if( datalen < 8)
                        return;

                    // Check TIFF header is big endian
                    if (data[0] != 0x4D || data[1] != 0x4D)
                        return;

                    // check TIFF header is version x2A
                    if (data[2] != 0x00 || data[3] != 0x2A)
                        return;

                    var hoffset = (data[4] << 24) + (data[5] << 16) + (data[6] << 8) + (data[7]);

                    int exifoffset = 0;
                    // scan TIFF header IFD0 entries for ExifOffset
                    int hcount = (data[hoffset] << 8) + data[hoffset+1];
                    for(int i = 0; i <hcount; i++)
                    {
                        var idx = i * 12;
                        if(idx + 12 >= datalen )
                            return;

                        int token = (data[hoffset + 2 + idx + 0] << 8) + (data[hoffset + 2 + idx + 1]);
                        int token_type = (data[hoffset + 2 + idx+ 2] << 8) + (data[hoffset + 2 + idx + 3]);
                        if( token == 0x8769 && token_type == 4)
                        {
                            exifoffset = (data[hoffset + 2 + idx + 8] << 24) + (data[hoffset + 2 + idx + 9] << 16) + (data[hoffset + 2 + idx + 10] << 8) + (data[hoffset + 2 + idx + 11]);
                            break;
                        }
                    }

                    if (exifoffset == 0) 
                        return;

                    // find MakerNotes 
                    int mnoffset = 0;
                    int exifcount = (data[exifoffset] << 8) + data[exifoffset + 1];
                    for (int i = 0; i < exifcount; i++)
                    {
                        var idx = i * 12;
                        if (idx + 12 >= datalen)
                            return;

                        int token = (data[exifoffset + 2 + idx + 0] << 8) + (data[exifoffset + 2 + idx + 1]);
                        int token_type = (data[exifoffset + 2 + idx + 2] << 8) + (data[exifoffset + 2 + idx + 3]);
                        if (token == 0x927C && token_type == 7)
                        {
                            mnoffset = (data[exifoffset + 2 + idx + 8] << 24) + (data[exifoffset + 2 + idx + 9] << 16) + (data[exifoffset + 2 + idx + 10] << 8) + (data[exifoffset + 2 + idx + 11]);
                            break;
                        }
                    }

                    // Check it's the Nikon MakerNotes
                    if (data[mnoffset] != 'N' || data[mnoffset + 1] != 'i' || data[mnoffset + 2] != 'k' || data[mnoffset + 3] != 'o' || data[mnoffset + 4] != 'n')
                        return;


                    // find 002C UnknownInfo 
                    int uioffset = 0;
                    int uilength = 0;
                    int mncount = (data[mnoffset + 18 + 0] << 8) + data[mnoffset + 18 + 1];
                    for (int i = 0; i < mncount; i++)
                    {
                        var idx = i * 12;
                        if (idx + 12 >= datalen)
                            return;

                        int token = (data[mnoffset + 18 + 2 + idx + 0] << 8) + (data[mnoffset + 18 + 2 + idx + 1]);
                        int token_type = (data[mnoffset + 18 + 2 + idx + 2] << 8) + (data[mnoffset + 18 + 2 + idx + 3]);
                        if (token == 0x002C && token_type == 7)
                        {
                            uilength = (data[mnoffset + 18 + 2 + idx + 4] << 24) + (data[mnoffset + 18 + 2 + idx + 5] << 16) + (data[mnoffset + 18 + 2 + idx + 6] << 8) + (data[mnoffset + 18 + 2 + idx + 7]);
                            uioffset = (data[mnoffset + 18 + 2 + idx + 8] << 24) + (data[mnoffset + 18 + 2 + idx + 9] << 16) + (data[mnoffset + 18 + 2 + idx + 10] << 8) + (data[mnoffset + 18 + 2 + idx + 11]);
                            uioffset += mnoffset + 0xA; // not sure where the 0x0A comes from.
                            break;
                        }
                    }

                    // Check for expected tokens.
                    if (data[uioffset] != 0x30 || data[uioffset + 1] != 0x31 || data[uioffset + 2] != 0x30 || data[uioffset + 3] != 0x31)
                        return;

                    int v0 = data[uioffset + 4]; // always 0x23
                    int v1 = data[uioffset + 5]; // r11, entry count;
                    int v2 = (data[uioffset + 6] << 8) + data[uioffset + 7];
                    int v3 = (data[uioffset + 8] << 8) + data[uioffset + 9];

                    int v4 = (data[uioffset + 10] << 24) + (data[uioffset + 11] << 16) + (data[uioffset + 12] << 8) + (data[uioffset + 13]); // zero

                    tw.Write("{0:X2} {1:X2} {2:X4} {3:X4} ", v0, v1, v2, v3);

                    for (int i = 0; i < v1; i++)
                    {
                        int idx = uioffset + 14;
                        int v = (data[idx + 0] << 24) + (data[idx + 1] << 16) + (data[idx + 2] << 8) + (data[idx + 3]);
                        tw.Write("{0:X8} ", v);
                    }
                    tw.WriteLine("     {0}", fileName);
                }
                finally
                {
                    if (br != null)
                    {
                        br.Close();
                        br.Dispose();
                    }
                }
            }
        }

        private static void DecodeNKLDFile(string fileName)
        {
            if (File.Exists(fileName))
            {
                BinaryReader br = null;
                StreamWriter tw = null;

                try
                {
                    br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite));
                    tw = new StreamWriter(File.Open(fileName + ".txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

                    if (br.BaseStream.Length > 0x20000) return;

                    var data = br.ReadBytes((int)br.BaseStream.Length);
                    int off = 0;
                    var dataOffset = (data[off++] << 8) + data[off++];
                    var fileLen = (data[off++] << 8) + data[off++];
                    var majorVer = data[off++];
                    var minorVer = data[off++];
                    var entryCount = (data[off++] << 8) + data[off++];
                    var magicId = (data[off++] << 24) + (data[off++] << 16) + (data[off++] << 8) + (data[off++] << 0);
                    var dataLen = (data[off++] << 8) + data[off++];
                    var unknown = (data[off++] << 8) + data[off++];

                    if ((UInt32)magicId == 0x87C7CAAC &&
                        majorVer == 1)
                    {
                        off = dataOffset;
                        for (int i = 0; i < entryCount; i++)
                        {
                            var block_len = (data[off + 0] << 8) + data[off + 1];

                            var b02 = data[off + 2];
                            var b03 = data[off + 3];

                            var b04 = data[off + 4];
                            var b05 = data[off + 5];
                            var b06 = data[off + 6];
                            var b07 = data[off + 7];

                            var w1E = (data[off + 0x1E] << 8) + data[off + 0x1F];


                            //var s = string.Format("{0}/{1} {2:X5} {3:X4}: {4:X2} {5:X2} {6:X4}", i, entryCount, off, block_len, b02, b03, w1E);
                            var s = string.Format("{0:X2} {1:X2} {2:X2} {3:X2} {4:X2} {5:X2} [{6}] [{7}] [{8}]",
                                b02, b03, b04, b05, b06, b07, DataToStr(data, off + 0xe, 8), DataToStr(data, off + 0x16, 8), DataToStr(data, off + 0x16+w1E, 0xDB));

                            Debug.WriteLine(s);
                            tw.WriteLine(s);
                            off += block_len;
                        }
                    }
                }
                finally
                {
                    if (br != null)
                        br.Close();
                    if (tw != null)
                        tw.Close();
                }
            }

        }

        static string DataToStr(byte[] data, int offset, int len)
        {
            var sb = new StringBuilder(len);
            for (int i = 0; i < len; i++)
            {
                sb.AppendFormat("{0:X2} ", data[offset + i]);
            }
            return sb.ToString();
        }

        private static void DecryptNKLDFile(string fileName)
        {
            if (File.Exists(fileName))
            {
                BinaryReader br = null;
                BinaryWriter bw = null;

                try
                {
                    br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite));
                    bw = new BinaryWriter(File.Open(fileName + ".bin", FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

                    if (br.BaseStream.Length > 0x20000) return;

                    var data = br.ReadBytes((int)br.BaseStream.Length);
                    int off = 0;
                    var dataOffset = (data[off++] << 8) + data[off++];
                    var fileLen = (data[off++] << 8) + data[off++];
                    var majorVer = data[off++];
                    var minorVer = data[off++];
                    var entryCount = (data[off++] << 8) + data[off++];
                    var magicId = (data[off++] << 24) + (data[off++] << 16) + (data[off++] << 8) + (data[off++] << 0);
                    var dataLen = (data[off++] << 8) + data[off++];
                    var unknown = (data[off++] << 8) + data[off++];

                    if ((UInt32)magicId == 0x87C7CAAC &&
                        majorVer == 1)
                    {
                        Decrypt.Encrypt(0xB401C81B, "NCDSLR", data, dataOffset, dataLen);

                        bw.Write(data);
                    }
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

        static void SearchDumpsForIntDiff(string filename, params int[] dcode)
        {
            var sw = new StreamWriter(File.Open(filename + "status.txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

            byte[] data = File.ReadAllBytes(filename);

            if (data.Length == 0) return;

            int dl = dcode.Length;
            for(int loc = 0; loc < (data.Length - (dcode.Length*4)); loc++)     
            {
                for(int off = 0; off < (dcode.Length-1); off++)
                {
                    int t1 = dcode[off];
                    int t2 = dcode[off + 1];
                    int d1 = (t2 - t1)/8;
                    int v1 = (int)ReadUint32(data, loc + ((off + 0) * 4));
                    int v2 = (int)ReadUint32(data, loc + ((off + 1) * 4));

                    int d2 = v2 - v1; 

                    if( d1 != d2 )
                        break;

                    if( off == (dcode.Length-2))
                    {                            
                        sw.WriteLine("Match at {0:X8}",  loc);
                        Console.WriteLine("Match at {0:X8}",  loc);
                    }
                }

            }

            sw.Close();
            sw.Dispose();
        }

        static void SearchDumpsForDiff_B(string filename, int offset, params int[] dcode)
        {
            var sw = new StreamWriter(File.Open(filename + "status_b.txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

            byte[] data = File.ReadAllBytes(filename);

            if (data.Length == 0) return;

            int dl = dcode.Length;
            for (int loc = 0; loc < (data.Length - (dcode.Length * 4)); loc++)
            {
                for (int off = 0; off < (dcode.Length - 1); off++)
                {
                    int t1 = dcode[off];
                    int t2 = dcode[off + 1];
                    int d1 = (t2 - t1) / 8;
                    int v1 = (int)ReadUint16(data, loc + ((off + 0) * 2));
                    int v2 = (int)ReadUint16(data, loc + ((off + 1) * 2));

                    int d2 = v2 - v1;

                    if (d1 != d2)
                        break;

                    if (off == (dcode.Length - 2))
                    {
                        sw.WriteLine("Match at {0:X8}", loc-offset);
                        Console.WriteLine("Match at {0:X8}", loc-offset);
                    }
                }

            }

            sw.Close();
            sw.Dispose();
        }


        static void SearchDumpsFor(string dir, params byte[] bcode)
        {
            var sw = new StreamWriter(File.Open(Path.Combine(dir, "status.txt"), FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

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
                byte first = data[0];
                for (int i = 0; i < data.Length; i++)
                {
                    if (findB == false && (i + bcode.Length) < data.Length)
                    {
                        bool bsame = true;
                        for (int j = 0; j < bcode.Length && bsame; j++)
                        {
                            bsame = data[i + j] == bcode[j];
                        }

                        if (bsame)
                        {
                            var savFile = Path.Combine(dir, String.Format("pic_0x{0:X8}.jpg", i));
                            using (var bw = new BinaryWriter(File.Open(savFile, FileMode.Create, FileAccess.Write)))
                            {
                                bw.Write(data, i-6, data.Length - (i-6));
                                bw.Close();
                            }

                            sw.WriteLine("{0} Bcode {1:X8}", Path.GetFileName(file), i);
                            Console.WriteLine("{0} Bcode {1:X8}", Path.GetFileName(file), i);
                           
                        }
                    }

                }
            }
            sw.Close();
            sw.Dispose();
        }

        static void FindEmptyBlocks(string file, int offset)
        {
            BinaryReader br = null;
            var sw = new StreamWriter(File.Open( file+ "_blocks.txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

            byte[] data;

            using (br = new BinaryReader(File.Open(file, FileMode.Open, FileAccess.Read, FileShare.ReadWrite)))
            {
                data = br.ReadBytes((int)br.BaseStream.Length);
            }

            if (data.Length == 0) return;

            int count = 0;

            for (int i = 0; i < data.Length; i++)
            {
                if (data[i] == 0xFF)
                {
                    count++;
                }
                else if (count > 0x100)
                {
                    sw.WriteLine("Block {0:X8} Length {1:X8}", (i - count) + offset, count);
                    Console.WriteLine("{0} Bcode {1:X8}", (i - count) + offset, count);
                    count = 0;
                }
                else
                {
                    count = 0;
                }
            }

            sw.Close();
            sw.Dispose();
        }

        static void SearchDumps(string dir)
        {
            //var bcode = new byte[] { 0x3C, 0x1A, 0xBF, 0xC0, 0x27, 0x5A, 0x05, 0x00, 0x03, 0x40, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00 };
            //var bcode = new byte[] { 0x17, 0x7A, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x40, 0x1A };
            //var bcode = new byte[] { 0x67, 0x45, 0x23, 0x01 };
            //var bcode = new byte[] { 0x00, 0x00, 0x00, 0xC0, 0x11 };
            //var bcode = new byte[] { 0x53, 0x6F, 0x66, 0x74, 0x75, 0x6E, 0x65, 0x20 }; // "Softune "
            //var bcode = new byte[] { 0x8C, 0xFF, 0x8D, 0x7F, 0x83, 0xDF, 0x97, 0x30 };  

            var bcode = new byte[] { 0x4A, 0x46, 0x49, 0x46 }; // JFIF - Jpeg/Tiff header

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

        static UInt32 ReadUint32BE(byte[] data, long pos)
        {
            return (UInt32)(data[pos + 0] << 0 | data[pos + 1] << 8 | data[pos + 2] << 16 | data[pos + 3] << 24);
        }

        static UInt16 ReadUint16(byte[] data, long pos)
        {
            return (UInt16)(data[pos + 0] << 8 | data[pos + 1]);
        }

        static string ReadString(byte[] data, long pos, int max)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < max; i++)
            {
                byte c = data[pos+i];
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
