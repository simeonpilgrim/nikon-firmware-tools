using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Linq.Expressions;
using System.Diagnostics;
using System.Text;


namespace Nikon_Patch
{
    class D600_0101 : Firmware
    {

        Patch[] patch_1080_36mbps = {
            new Patch(1, 0x23B4E, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23B54, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23B7A, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23B80, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23BC6, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23BCC, new byte[] { 0x01 } , new byte[] { 0x02 } ),
                                  };

        Patch[] patch_1080_54mbps = {
            new Patch(1, 0x23B4E, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23B54, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23B7A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23B80, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23BC6, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23BCC, new byte[] { 0x01 } , new byte[] { 0x03 } ),
                                  };

        Patch[] patch_1080_36mbps_NQ = {
            new Patch(1, 0x23B4E, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23B54, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23B62, new byte[] { 0x00, 0xB7, 0x1B } , new byte[] { 0x01, 0x6E, 0x36 } ),
            new Patch(1, 0x23B68, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x31, 0x2D, 0x00 } ),

            new Patch(1, 0x23B7A, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23B80, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23B8E, new byte[] { 0x00, 0xB7, 0x1B } , new byte[] { 0x01, 0x6E, 0x36 } ),
            new Patch(1, 0x23B94, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x31, 0x2D, 0x00 } ),

            new Patch(1, 0x23BC6, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23BCC, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23BDA, new byte[] { 0x00, 0xB7, 0x1B } , new byte[] { 0x01, 0x6E, 0x36 } ),
            new Patch(1, 0x23BE0, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x31, 0x2D, 0x00 } ),
                                  };

        Patch[] patch_1080_54mbps_NQ = {
            new Patch(1, 0x23B4E, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23B54, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23B62, new byte[] { 0x00, 0xB7, 0x1B } , new byte[] { 0x01, 0x6E, 0x36 } ),
            new Patch(1, 0x23B68, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x31, 0x2D, 0x00 } ),

            new Patch(1, 0x23B7A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23B80, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23B8E, new byte[] { 0x00, 0xB7, 0x1B } , new byte[] { 0x01, 0x6E, 0x36 } ),
            new Patch(1, 0x23B94, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x31, 0x2D, 0x00 } ),

            new Patch(1, 0x23BC6, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23BCC, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23BDA, new byte[] { 0x00, 0xB7, 0x1B } , new byte[] { 0x01, 0x6E, 0x36 } ),
            new Patch(1, 0x23BE0, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x31, 0x2D, 0x00 } ),
                                  };

        Patch[] patch_1080_64mbps_NQ = {
            new Patch(1, 0x23B4E, Sys.mbps24 , Sys.mbps64 ),
            new Patch(1, 0x23B54, Sys.mbps20 , Sys.mbps60 ),
            new Patch(1, 0x23B62, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23B68, Sys.mbps10 , Sys.mbps20 ),

            new Patch(1, 0x23B7A, Sys.mbps24 , Sys.mbps64 ),
            new Patch(1, 0x23B80, Sys.mbps20 , Sys.mbps60 ),
            new Patch(1, 0x23B8E, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23B94, Sys.mbps10 , Sys.mbps20 ),

            new Patch(1, 0x23BC6, Sys.mbps24 , Sys.mbps64 ),
            new Patch(1, 0x23BCC, Sys.mbps20 , Sys.mbps60 ),
            new Patch(1, 0x23BDA, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23BE0, Sys.mbps10 , Sys.mbps20 ),
        };
        
        
        public D600_0101()
        {
            p = new Package();
            Model = "D600";
            Version = "1.01";

            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 36mbps Bit-rate", patch_1080_36mbps, patch_1080_54mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 54mbps Bit-rate", patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 36mbps Bit-rate NQ old HQ", patch_1080_36mbps_NQ, patch_1080_36mbps, patch_1080_54mbps, patch_1080_54mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 54mbps Bit-rate NQ old HQ", patch_1080_54mbps_NQ, patch_1080_36mbps, patch_1080_54mbps, patch_1080_36mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 64mbps Bit-rate NQ old HQ", patch_1080_64mbps_NQ, patch_1080_36mbps, patch_1080_54mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ));
        }
    }
}
