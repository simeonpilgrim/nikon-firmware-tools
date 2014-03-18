using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Linq.Expressions;
using System.Diagnostics;
using System.Text;


namespace Firmware_Patch
{
    class D600_0101 : Firmware
    {
        string in_filename;
        Package p = null;

 				Patch[] patches = {
        };

        public D600_0101(string filename)
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

            if (patches.Length==0)
            	return false;

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
            Console.WriteLine("Patching D600 0101 - ");
            Console.WriteLine();

            foreach (Patch pp in patches)
            {
                p.Patch(pp.block, pp.start, pp.patch);
            }

            p.Repackage(outfilename);
        }
    }
}
