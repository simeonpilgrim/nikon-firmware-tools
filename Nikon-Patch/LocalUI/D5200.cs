namespace Nikon_Patch
{
    class D5200_0101 : Firmware
    {
        static byte[] mbps_24_20be = { 0x00, 0x36, 0x6e, 0x01, 0x00, 0x2D, 0x31, 0x01 };
        static byte[] mbps_40_36be = { 0x00, 0x36, 0x6e, 0x02, 0x00, 0x2D, 0x31, 0x02 };

        // the above values divided by 1,000
        static byte[] mbps_24_20tbe = { 0xC0, 0x5D, 0x00, 0x00, 0x20, 0x4E };
        static byte[] mbps_40_36tbe = { 0x49, 0x9f, 0x00, 0x00, 0xa9, 0x8f };

        Patch[] patch_40mbps = {
            new Patch(1, 0x11F91F4, mbps_24_20be, mbps_40_36be ),
            new Patch(1, 0x11F9204, mbps_24_20be, mbps_40_36be ),
            new Patch(1, 0x11F9214, mbps_24_20be, mbps_40_36be ),
            new Patch(1, 0x11F9224, mbps_24_20be, mbps_40_36be ),
            new Patch(1, 0x11F9234, mbps_24_20be, mbps_40_36be ),
            new Patch(1, 0x11F9244, mbps_24_20be, mbps_40_36be ),
            new Patch(1, 0x11F9254, mbps_24_20be, mbps_40_36be ),

            new Patch(1, 0x11F8C24, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8C3C, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8C54, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8C6C, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8C84, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8C9C, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8CB4, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8CCC, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8CE4, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8CFC, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8D14, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8D2C, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8D74, mbps_24_20tbe, mbps_40_36tbe ),
                           };



        public D5200_0101()
        {
            p = new Package();
            Model = "D5200";
            Version = "1.01";

            //Patches.Add(new PatchSet(true, "Non-Brand Battery", patch_battery));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video HQ 40mbps Bit-rate", patch_40mbps));

        }
    }


    class D5200_0102 : Firmware
    {
        static byte[] mbps_24_20be = { 0x00, 0x36, 0x6e, 0x01, 0x00, 0x2D, 0x31, 0x01 };
        static byte[] mbps_40_36be = { 0x00, 0x36, 0x6e, 0x02, 0x00, 0x2D, 0x31, 0x02 };
        static byte[] mbps_64_60be = { 0x00, 0x90, 0xd0, 0x03, 0x00, 0x87, 0x93, 0x03 };
        static byte[] mbps_8_8be = { 0xa0, 0x98, 0x7b, 0x00, 0x00, 0x12, 0x7A, 0x00 }; // 8.1 & 8.0
        static byte[] mbps_1_1be = Sys.BigDwords(1100000,1000000); // 1.1 & 1.0

        // the above values divided by 1,000
        static byte[] mbps_24_20tbe = { 0xC0, 0x5D, 0x00, 0x00, 0x20, 0x4E };
        static byte[] mbps_40_36tbe = { 0x49, 0x9F, 0x00, 0x00, 0xA9, 0x8F };
        static byte[] mbps_64_60tbe = { 0x00, 0xFA, 0x00, 0x00, 0x60, 0xEA };
        static byte[] mbps_8_8tbe = { 0xa4, 0x1F, 0x00, 0x00, 0x40, 0x1F };
        static byte[] mbps_1_1tbe = Sys.BigWords(1100, 0, 1000);

        Patch[] patch_40mbps = {
            new Patch(1, 0x11F91F4+0x2250, mbps_24_20be, mbps_40_36be ),
            new Patch(1, 0x11F9204+0x2250, mbps_24_20be, mbps_40_36be ),
            new Patch(1, 0x11F9214+0x2250, mbps_24_20be, mbps_40_36be ),
            new Patch(1, 0x11F9224+0x2250, mbps_24_20be, mbps_40_36be ),
            new Patch(1, 0x11F9234+0x2250, mbps_24_20be, mbps_40_36be ),
            new Patch(1, 0x11F9244+0x2250, mbps_24_20be, mbps_40_36be ),
            new Patch(1, 0x11F9254+0x2250, mbps_24_20be, mbps_40_36be ),

            new Patch(1, 0x11F8C24+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8C3C+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8C54+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8C6C+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8C84+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8C9C+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8CB4+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8CCC+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8CE4+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8CFC+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8D14+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8D2C+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
            new Patch(1, 0x11F8D74+0x2250, mbps_24_20tbe, mbps_40_36tbe ),
                           };

        Patch[] patch_64mbps = {
            new Patch(1, 0x11F91F4+0x2250, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x11F9204+0x2250, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x11F9214+0x2250, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x11F9224+0x2250, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x11F9234+0x2250, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x11F9244+0x2250, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x11F9254+0x2250, mbps_24_20be, mbps_64_60be ),

            new Patch(1, 0x11F8C24+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x11F8C3C+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x11F8C54+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x11F8C6C+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x11F8C84+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x11F8C9C+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x11F8CB4+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x11F8CCC+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x11F8CE4+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x11F8CFC+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x11F8D14+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x11F8D2C+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x11F8D74+0x2250, mbps_24_20tbe, mbps_64_60tbe ),
                           };

        Patch[] patch_8mbps = {
            new Patch(1, 0x11F91F4+0x2250, mbps_24_20be, mbps_8_8be ),
            new Patch(1, 0x11F9204+0x2250, mbps_24_20be, mbps_8_8be ),
            new Patch(1, 0x11F9214+0x2250, mbps_24_20be, mbps_8_8be ),
            new Patch(1, 0x11F9224+0x2250, mbps_24_20be, mbps_8_8be ),
            new Patch(1, 0x11F9234+0x2250, mbps_24_20be, mbps_8_8be ),
            new Patch(1, 0x11F9244+0x2250, mbps_24_20be, mbps_8_8be ),
            new Patch(1, 0x11F9254+0x2250, mbps_24_20be, mbps_8_8be ),

            new Patch(1, 0x11F8C24+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
            new Patch(1, 0x11F8C3C+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
            new Patch(1, 0x11F8C54+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
            new Patch(1, 0x11F8C6C+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
            new Patch(1, 0x11F8C84+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
            new Patch(1, 0x11F8C9C+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
            new Patch(1, 0x11F8CB4+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
            new Patch(1, 0x11F8CCC+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
            new Patch(1, 0x11F8CE4+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
            new Patch(1, 0x11F8CFC+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
            new Patch(1, 0x11F8D14+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
            new Patch(1, 0x11F8D2C+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
            new Patch(1, 0x11F8D74+0x2250, mbps_24_20tbe, mbps_8_8tbe ),
                           };

        Patch[] patch_1mbps = {
            new Patch(1, 0x11F91F4+0x2250, mbps_24_20be, mbps_1_1be ),
            new Patch(1, 0x11F9204+0x2250, mbps_24_20be, mbps_1_1be ),
            new Patch(1, 0x11F9214+0x2250, mbps_24_20be, mbps_1_1be ),
            new Patch(1, 0x11F9224+0x2250, mbps_24_20be, mbps_1_1be ),
            new Patch(1, 0x11F9234+0x2250, mbps_24_20be, mbps_1_1be ),
            new Patch(1, 0x11F9244+0x2250, mbps_24_20be, mbps_1_1be ),
            new Patch(1, 0x11F9254+0x2250, mbps_24_20be, mbps_1_1be ),

            new Patch(1, 0x11F8C24+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
            new Patch(1, 0x11F8C3C+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
            new Patch(1, 0x11F8C54+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
            new Patch(1, 0x11F8C6C+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
            new Patch(1, 0x11F8C84+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
            new Patch(1, 0x11F8C9C+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
            new Patch(1, 0x11F8CB4+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
            new Patch(1, 0x11F8CCC+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
            new Patch(1, 0x11F8CE4+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
            new Patch(1, 0x11F8CFC+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
            new Patch(1, 0x11F8D14+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
            new Patch(1, 0x11F8D2C+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
            new Patch(1, 0x11F8D74+0x2250, mbps_24_20tbe, mbps_1_1tbe ),
                           };

        Patch[] patch_liveview_no_timeout_15m = {
                                                    new Patch(1,0x95B4, new byte[]{0xA0, 0xBB, 0x0D, 0x00}, new byte[] {0x80, 0x27, 0xCB, 0x05} ),
                                                    new Patch(1,0xCEB8, new byte[]{0xA0, 0xBB, 0x0D, 0x00}, new byte[] {0x80, 0x27, 0xCB, 0x05} ),
                                                    new Patch(1,0x27877C, new byte[]{0xA0, 0xBB, 0x0D, 0x00}, new byte[] {0x80, 0x27, 0xCB, 0x05} ),
                                                };

        //Patch[] patch_liveview_no_timeout_15m_b = {
        //                                            new Patch(1,0x95B4, Sys.BigDwords(900000), Sys.BigDwords(10800000) ),
        //                                            new Patch(1,0xCEB8, Sys.BigDwords(900000), Sys.BigDwords(10800000) ),
        //                                            new Patch(1,0x27877C, Sys.BigDwords(900000), Sys.BigDwords(10800000) ),
        //                                        };


        public D5200_0102()
        {
            p = new Package();
            Model = "D5200";
            Version = "1.02";

            Patches.Add(new PatchSet(PatchLevel.Released, "Video HQ 40mbps Bit-rate", patch_40mbps, patch_64mbps, patch_8mbps, patch_1mbps));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video HQ 64mbps Bit-rate", patch_64mbps, patch_40mbps, patch_8mbps, patch_1mbps));
            Patches.Add(new PatchSet(PatchLevel.DevOnly, "Video HQ 8mbps Bit-rate", patch_8mbps, patch_40mbps, patch_64mbps, patch_1mbps));
            Patches.Add(new PatchSet(PatchLevel.DevOnly, "Video HQ 1mbps Bit-rate", patch_1mbps, patch_40mbps, patch_64mbps, patch_8mbps));

            Patches.Add(new PatchSet(PatchLevel.Released, "Liveview (15min) No Timeout", patch_liveview_no_timeout_15m));
        }
    }

    class D5200_0103 : Firmware
    {
        public D5200_0103()
        {
            p = new Package();
            Model = "D5200";
            Version = "1.03";
        }
    }
}
