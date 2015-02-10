using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Linq.Expressions;
using System.Diagnostics;
using System.Text;


namespace Nikon_Patch
{
    class D610_0101 : Firmware
    {
        Patch[] patch_1080_36mbps_NQ = {
            new Patch(1, 0x23DD2, Sys.mbps24 , Sys.mbps40 ),
            new Patch(1, 0x23DD8, Sys.mbps20 , Sys.mbps36 ),
            new Patch(1, 0x23DE6, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23DEC, Sys.mbps10 , Sys.mbps20 ),

            new Patch(1, 0x23E4A, Sys.mbps24 , Sys.mbps40 ),
            new Patch(1, 0x23E50, Sys.mbps20 , Sys.mbps36 ),
            new Patch(1, 0x23E5E, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23E64, Sys.mbps10 , Sys.mbps20 ),

            new Patch(1, 0x23EDC, Sys.mbps24 , Sys.mbps40 ),
            new Patch(1, 0x23EE2, Sys.mbps20 , Sys.mbps36 ),
            new Patch(1, 0x23EF0, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23EF6, Sys.mbps10 , Sys.mbps20 ),
        };

        Patch[] patch_1080_54mbps_NQ = {
            new Patch(1, 0x23DD2, Sys.mbps24 , Sys.mbps57 ),
            new Patch(1, 0x23DD8, Sys.mbps20 , Sys.mbps53 ),
            new Patch(1, 0x23DE6, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23DEC, Sys.mbps10 , Sys.mbps20 ),

            new Patch(1, 0x23E4A, Sys.mbps24 , Sys.mbps57 ),
            new Patch(1, 0x23E50, Sys.mbps20 , Sys.mbps53 ),
            new Patch(1, 0x23E5E, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23E64, Sys.mbps10 , Sys.mbps20 ),

            new Patch(1, 0x23EDC, Sys.mbps24 , Sys.mbps57 ),
            new Patch(1, 0x23EE2, Sys.mbps20 , Sys.mbps53 ),
            new Patch(1, 0x23EF0, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23EF6, Sys.mbps10 , Sys.mbps20 ),
        };


        Patch[] patch_1080_720_64mbps_NQ = {
            // 1080 0 24 fps
            new Patch(1, 0x23DD2, Sys.mbps24 , Sys.mbps64 ),
            new Patch(1, 0x23DD8, Sys.mbps20 , Sys.mbps60 ),
            new Patch(1, 0x23DE6, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23DEC, Sys.mbps10 , Sys.mbps20 ),

            // 1080 2 30 fps
            new Patch(1, 0x23DFE, Sys.mbps24 , Sys.mbps64 ),
            new Patch(1, 0x23E04, Sys.mbps20 , Sys.mbps60 ),
            new Patch(1, 0x23E12, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23E18, Sys.mbps10 , Sys.mbps20 ),

            // 1080 6 25 fps
            new Patch(1, 0x23E4A, Sys.mbps24 , Sys.mbps64 ),
            new Patch(1, 0x23E50, Sys.mbps20 , Sys.mbps60 ),
            new Patch(1, 0x23E5E, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23E64, Sys.mbps10 , Sys.mbps20 ),

            
            // 720 0 24 fps
            new Patch(1, 0x23EB0, Sys.mbps10, Sys.mbps64 ),
            new Patch(1, 0x23EB6, Sys.mbps8 , Sys.mbps60 ),
            new Patch(1, 0x23EC4, Sys.mbps6 , Sys.mbps24 ),
            new Patch(1, 0x23ECA, Sys.mbps5 , Sys.mbps20 ),

            // 720 1 60 fps
            new Patch(1, 0x23EDC, Sys.mbps24 , Sys.mbps64 ),
            new Patch(1, 0x23EE2, Sys.mbps20 , Sys.mbps60 ),
            new Patch(1, 0x23EF0, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23EF6, Sys.mbps10 , Sys.mbps20 ),

            // 720 2 30 fps
            new Patch(1, 0x23F08, Sys.mbps12, Sys.mbps64 ),
            new Patch(1, 0x23F0E, Sys.mbps10, Sys.mbps60 ),
            new Patch(1, 0x23F1C, Sys.mbps8 , Sys.mbps24 ),
            new Patch(1, 0x23F22, Sys.mbps6 , Sys.mbps20 ),

            // 720 5 50 fps
            new Patch(1, 0x23F54, Sys.mbps24 , Sys.mbps64 ),
            new Patch(1, 0x23F5A, Sys.mbps20 , Sys.mbps60 ),
            new Patch(1, 0x23F68, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23F6E, Sys.mbps10 , Sys.mbps20 ),

                                       };

        Patch[] patch_Language_Fix = {
            new Patch(1, 0x380258, new byte[] { 0xE2, 0x08 }, new byte[] { 0xE0, 0x08 }),
            new Patch(1, 0x385758, new byte[] { 0xE2, 0x11 }, new byte[] { 0xE0, 0x11 }),
            new Patch(1, 0x41E638, new byte[] { 0xE2, 0x06 }, new byte[] { 0xE0, 0x06 }),
            //new Patch(1, 0x3E320C, new byte[] { 0xB1, 0xF4 }, new byte[] { 0xC0, 0x04 }), this case (from 3200) was not found
                          };

        public D610_0101()
        {
            p = new Package();
            Model = "D610";
            Version = "1.01";

            Patches.Add(new PatchSet(PatchLevel.Alpha, "Multi-Language Support", patch_Language_Fix));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 36mbps Bit-rate NQ old HQ", patch_1080_36mbps_NQ, patch_1080_54mbps_NQ, patch_1080_720_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 54mbps Bit-rate NQ old HQ", patch_1080_54mbps_NQ, patch_1080_36mbps_NQ, patch_1080_720_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080/720 HQ 64mbps Bit-rate NQ old HQ", patch_1080_720_64mbps_NQ, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ));
        }
    }
}
