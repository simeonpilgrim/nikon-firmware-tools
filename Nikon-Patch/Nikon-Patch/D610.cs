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


        Patch[] patch_1080_64mbps_NQ = {
            new Patch(1, 0x23DD2, Sys.mbps24 , Sys.mbps64 ),
            new Patch(1, 0x23DD8, Sys.mbps20 , Sys.mbps60 ),
            new Patch(1, 0x23DE6, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23DEC, Sys.mbps10 , Sys.mbps20 ),

            new Patch(1, 0x23E4A, Sys.mbps24 , Sys.mbps64 ),
            new Patch(1, 0x23E50, Sys.mbps20 , Sys.mbps60 ),
            new Patch(1, 0x23E5E, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23E64, Sys.mbps10 , Sys.mbps20 ),

            new Patch(1, 0x23EDC, Sys.mbps24 , Sys.mbps64 ),
            new Patch(1, 0x23EE2, Sys.mbps20 , Sys.mbps60 ),
            new Patch(1, 0x23EF0, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23EF6, Sys.mbps10 , Sys.mbps20 ),
        };


        public D610_0101()
        {
            p = new Package();
            Model = "D610";
            Version = "1.01";

            Patches.Add(new PatchSet(PatchLevel.Alpha, "Video 1080 HQ 36mbps Bit-rate NQ old HQ", patch_1080_36mbps_NQ, patch_1080_54mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Alpha, "Video 1080 HQ 54mbps Bit-rate NQ old HQ", patch_1080_54mbps_NQ, patch_1080_36mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Alpha, "Video 1080 HQ 64mbps Bit-rate NQ old HQ", patch_1080_64mbps_NQ, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ));
        }
    }
}
