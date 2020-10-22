namespace Nikon_Patch
{
    class D4_0105 : Firmware
    {

        Patch[] patch_1080_36mbps = {
            new Patch(2, 0x20BAA, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(2, 0x20BB0, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(2, 0x20BD6, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(2, 0x20BDC, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(2, 0x20C22, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(2, 0x20C28, new byte[] { 0x01 } , new byte[] { 0x02 } ),
                                 };

        Patch[] patch_1080_54mbps = {
            new Patch(2, 0x20BAA, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(2, 0x20BB0, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(2, 0x20BD6, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(2, 0x20BDC, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(2, 0x20C22, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(2, 0x20C28, new byte[] { 0x01 } , new byte[] { 0x03 } ),
                                  };

        Patch[] patch_1080_36mbps_NQ = {
            new Patch(2, 0x20BAA, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(2, 0x20BB0, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(2, 0x20BBE, new byte[] { 0x00, 0xB7, 0x1B } , new byte[] { 0x01, 0x6E, 0x36 } ),
            new Patch(2, 0x20BC4, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x31, 0x2D, 0x00  } ),

            new Patch(2, 0x20BD6, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(2, 0x20BDC, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(2, 0x20BEA, new byte[] { 0x00, 0xB7, 0x1B } , new byte[] { 0x01, 0x6E, 0x36 } ),
            new Patch(2, 0x20BF0, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x31, 0x2D, 0x00 } ),

            new Patch(2, 0x20C22, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(2, 0x20C28, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(2, 0x20C36, new byte[] { 0x00, 0xB7, 0x1B } , new byte[] { 0x01, 0x6E, 0x36 } ),
            new Patch(2, 0x20C3C, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x31, 0x2D, 0x00 } ),
                                 };

        Patch[] patch_1080_54mbps_NQ = {
            new Patch(2, 0x20BAA, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(2, 0x20BB0, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(2, 0x20BBE, new byte[] { 0x00, 0xB7, 0x1B } , new byte[] { 0x01, 0x6E, 0x36 } ),
            new Patch(2, 0x20BC4, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x31, 0x2D, 0x00  } ),

            new Patch(2, 0x20BD6, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(2, 0x20BDC, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(2, 0x20BEA, new byte[] { 0x00, 0xB7, 0x1B } , new byte[] { 0x01, 0x6E, 0x36 } ),
            new Patch(2, 0x20BF0, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x31, 0x2D, 0x00 } ),

            new Patch(2, 0x20C22, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(2, 0x20C28, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(2, 0x20C36, new byte[] { 0x00, 0xB7, 0x1B } , new byte[] { 0x01, 0x6E, 0x36 } ),
            new Patch(2, 0x20C3C, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x31, 0x2D, 0x00 } ),
                                 };


        public D4_0105()
        {
            p = new Package();
            Model = "D4";
            Version = "1.05";

            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 36mbps Bit-rate", patch_1080_36mbps, patch_1080_54mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 54mbps Bit-rate", patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 36mbps Bit-rate NQ old HQ", patch_1080_36mbps_NQ, patch_1080_36mbps, patch_1080_54mbps, patch_1080_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 54mbps Bit-rate NQ old HQ", patch_1080_54mbps_NQ, patch_1080_36mbps, patch_1080_54mbps, patch_1080_36mbps_NQ));
        }
    }

    class D4_0110 : Firmware
    {
        public D4_0110()
        {
            p = new Package();
            Model = "D4";
            Version = "1.10";
        }
    }

    class D4S_0101 : Firmware
    {
        public D4S_0101()
        {
            p = new Package();
            Model = "D4S";
            Version = "1.01";
        }
    }

    class D4S_0132 : Firmware
    {
        public D4S_0132()
        {
            p = new Package();
            Model = "D4S";
            Version = "1.32";
        }
    }
}
