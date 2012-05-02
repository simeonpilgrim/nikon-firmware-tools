using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Linq.Expressions;
using System.Diagnostics;
using System.Text;


namespace Firmware_Patch
{
    class D3100_0101 : Firmware
    {
        string in_filename;
        Package p = null;


        Patch[] patches = {
            new Patch(1, 0x84C200, new byte[] { 0xE4, 0x02 }, new byte[] { 0xE0, 0x02 }), // unlimited recording 1/2
            new Patch(1, 0x84CD1A, new byte[] { 0xE2, 0x0B }, new byte[] { 0xE0, 0x0B }), // unlimited recording 2/2
                          };

        public D3100_0101(string filename)
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
            Console.WriteLine("Patching D3100 0101 - Remove Time Restrictions on video recording");
            Console.WriteLine();

            foreach (Patch pp in patches)
            {
                p.Patch(pp.block, pp.start, pp.patch);
            }

            p.Repackage(outfilename);
        }
    }
}
