using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Linq.Expressions;
using System.Diagnostics;
using System.Text;


namespace Nikon_Patch
{
    class D300s_0102 : Firmware
    {
        Patch[] patch_star_eater = {
            new Patch(1, 0xE7F34, new byte[] { 0xED, 0x12 }, new byte[] { 0xE0, 0x12 }), 
            new Patch(1, 0xE8186, new byte[] { 0xED, 0x03 }, new byte[] { 0xE0, 0x03 }),
                                  };


        public D300s_0102()
        {
            p = new Package();
            Model = "D300s";
            Version = "1.02";

            Patches.Add(new PatchSet(PatchLevel.Released, "Disable Nikon Star Eater", patch_star_eater));
        }
    }

    class D300s_0101 : Firmware
    {
        public D300s_0101()
        {
            p = new Package();
            Model = "D300s";
            Version = "1.01";
        }
    }
}
