using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Threading;
using System.Diagnostics;

namespace Firmware_Patch
{
    static class CRC
    {
        public static void UpdateCRC(byte[] data)
        {
            int checksum = crcBig(data, data.Length - 2);

            data[data.Length - 2] = (byte)((checksum >> 8) & 0xff);
            data[data.Length - 1] = (byte)((checksum >> 0) & 0xff);
        }


        static int crcBig(byte[] data, int len)
        {
            int rem = 0x0000;

            for (int i = 0; i < len; i++)
            {
                rem = rem ^ (data[i] << 8);
                for (int j = 0; j < 8; j++)
                {
                    if ((rem & 0x8000) != 0)
                    {
                        rem = (rem << 1) ^ 0x1021;
                    }
                    else
                    {
                        rem = rem << 1;

                    }
                    rem = rem & 0xFFFF; // Trim remainder to 16 bits
                }
            }

            return rem;
        }

    }
}
