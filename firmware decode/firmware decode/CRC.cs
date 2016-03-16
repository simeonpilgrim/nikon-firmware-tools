using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Threading;
using System.Diagnostics;

namespace Nikon_Decode
{
    partial class Program
    {
        static void CalcCRC(string filename)
        {
            Debug.WriteLine(Path.GetFileName(filename));

            var br = new BinaryReader(File.Open(filename, FileMode.Open, FileAccess.Read, FileShare.ReadWrite));
            int len = (int)br.BaseStream.Length;
            if (len <= 2) return;

            var data = br.ReadBytes(len );

            var b1 = data[len-2];
            var b2 = data[len-1];
            //data[len - 2] = 0;
            //data[len - 1] = 0;

            br.Close();
            br.Dispose();

            int file_CRC_1 = (b1 << 8) + b2; //msb

            Debug.WriteLine("F: 0x{0:x4}  ", file_CRC_1);

            int checksum_A = crcBig(data, data.Length - 2, 0x0000, 0x1021);
            Debug.WriteLine("crc 0x0000 0x{0:x4} ", checksum_A);
        }


        static int crcBig(byte[] data, int len, int start, int mask)
        {
            int rem = start;

            for (int i = 0; i < len; i++)
            {
                rem = rem ^ (data[i] << 8);
                for (int j = 0; j < 8; j++)
                {
                    if ((rem & 0x8000) != 0)
                    {
                        rem = (rem << 1) ^ mask;
                    }
                    else
                    {
                        rem = rem << 1;

                    }
                    rem = rem & 0xFFFF; // Trim remainder to 16 bits
                }
            }
            // A popular variant complements rem here
            return rem;
        }

    }
}
