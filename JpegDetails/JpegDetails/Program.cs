using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Diagnostics;

namespace JpegDetails
{
    class Program
    {
        static TextWriter log;
        static void SetLogFile(string filename)
        {
            if( log != null )
            {
                log.Close();
                log.Dispose();
            }

            if( filename != "")
                log = new StreamWriter(File.Open(filename, FileMode.OpenOrCreate));
        }

        static void Log(string fmt, params object[] obj)
        {
            if (log != null)
                log.WriteLine(fmt, obj);
            Console.WriteLine(fmt, obj);
            Debug.WriteLine(fmt, obj);
        }

        static void Main(string[] args)
        {
            SetLogFile(@"C:\Temp\Jpeg - Nikon.txt");
            Details(@"C:\Users\spilgrim\Downloads\Nikon\Decode\Jpgs\b640101b_126 007E17B4.jpg");
            SetLogFile(@"C:\Temp\Jpeg - Replacement.txt"); 
            Details(@"C:\Users\spilgrim\Downloads\vaderhead_300dpi.jpg");
            SetLogFile("");
        }

        static int xWord(BinaryReader br)
        {
            return (br.ReadByte() << 8) + br.ReadByte();
        }
        static char xChar(BinaryReader br)
        {
            return (char)br.ReadByte();
        }
        static byte xByte(BinaryReader br)
        {
            return br.ReadByte();
        }

        const int SOI  = 0xFFD8;
        const int APP0 = 0xFFD8;
        const int DQT  = 0xFFDB; // Define quantization table
        const int DHT  = 0xFFC4; // Define Huffman table
        const int SOF0 = 0xFFC0; // Baseline DCT
        const int SOF1 = 0xFFC1; // Extended sequential DCT
        const int SOF2 = 0xFFC2; // Progressive DCT
        const int SOF3 = 0xFFC3; // Lossless (sequential)
        const int SOS = 0xFFDA; // Start of scan

        static void Details(string filename)
        {
            using (var br = new BinaryReader(File.Open(filename, FileMode.Open)))
            {
                Log("");
                Log(filename);

                Log("SOI marker       {0:x4}", xWord(br));
                Log("APP0 marker      {0:x4}", xWord(br));
                Log("Length           {0:x4}", xWord(br));
                Log("Identifier       {0:x2}{1:x2}{2:x2}{3:x2}{4:x2}", xByte(br), xByte(br), xByte(br), xByte(br), xByte(br));
                Log("Version          {0:x4}", xWord(br));
                Log("Density units    {0}", xByte(br));
                Log("X Density        {0:x4}", xWord(br));
                Log("Y Density        {0:x4}", xWord(br));
                int tw = xByte(br);
                int th = xByte(br);
                Log("Thumbnail width  {0}", tw);
                Log("Thumbnail height {0}", th);
                Log("  skipping {0} bytes", 3 * tw * th);
                for (int i = 0; i < (3 * tw * th); i++)
                    xByte(br);

                bool good = true;
                while (good)
                {
                    int m1 = xWord(br);

                    switch (m1)
                    {
                        //case APP0: break;
                        case DQT:
                            DqtDetails(br);
                            break;

                        case DHT:
                            DhtDetails(br);
                            break;

                        case SOF0:
                        case SOF1:
                        case SOF2:
                        case SOF3:
                            SOFxDetails(br, m1);
                            break;

                        case SOS:
                            SosDetails(br);
                            break;

                        default:
                            Log("UNKNOWN: {0:x4}", m1);
                            good = false;
                            break;

                    }
                }
            }
        }

        private static void SosDetails(BinaryReader br)
        {
            Log("SOS - Start of Scan segment");
            int len = xWord(br);
            Log("  Length {0}", len);
            int Ns = xByte(br);
            Log("  #Segs  {0}", Ns);

            for (int i = 0; i < Ns; i++)
            {
                Log("  Cs{0}    {1} - Scan component selector", i, xByte(br));
                int tmp = xByte(br);
                int Td = (tmp & 0xf0) >> 4;
                int Ta = tmp & 0x0f;
                Log("    Td{0}  {1}", i, Td);
                Log("    Ta{0}  {1}", i, Ta);
            }
            Log("  Ss  {0}", xByte(br));
            Log("  Se  {0}", xByte(br));
            int tmp2 = xByte(br);
            int Ah = (tmp2 & 0xf0) >> 4;
            int Al = tmp2 & 0x0f;
            Log("  Ah  {0}", Ah);
            Log("  Al  {0}", Al);
        }

        private static void DhtDetails(BinaryReader br)
        {
            Log("DHT - Define Huffman table segment");
            int len = xWord(br);
            Log("  Length {0}", len);
            int tmp = xByte(br);
            int Tc = (tmp & 0xf0) >> 4;
            int Th = tmp & 0x0f;
            Log("  Tc     {0:x2} {1}", Tc, (Tc == 0) ? "DC table" : ((Tc == 1) ? "AC table" : "???"));
            Log("  Th     {0:x2}", Th);
            int skip = len - 3;
            Log("  skipping {0} bytes", skip);
            for (int i = 0; i < skip; i++)
                xByte(br);
        }

        private static void SOFxDetails(BinaryReader br, int marker)
        {
            Log("SOFx - {0:x4}", marker);
            int len = xWord(br);
            Log("  Length {0}", len);
            int skip = len - 2;
            Log("  skipping {0} bytes", skip);
            for (int i = 0; i < skip; i++)
                xByte(br);
     
        }

        private static void DqtDetails(BinaryReader br)
        {
            Log("DQT - Define quantization table segment");
            int len = xWord(br);
            Log("  Length {0}", len);
            int tmp = xByte(br);
            int Pq = (tmp & 0xf0) >> 4;
            int Tq = tmp & 0x0f;
            Log("  Pq     {0:x2} {1}", Pq, (Pq == 0) ? "8-bit" : ((Pq == 1) ? "16-bit" : "???"));
            Log("  Tq     {0:x2}", Tq);
            int skip = len - 3;
            Log("  skipping {0} bytes", skip);
            for (int i = 0; i < skip; i++)
                xByte(br);
        }
    }
}
