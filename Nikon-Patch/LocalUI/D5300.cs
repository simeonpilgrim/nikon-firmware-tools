namespace Nikon_Patch
{
    class D5300_0101 : Firmware
    {
        public D5300_0101()
        {
            p = new Package();
            Model = "D5300";
            Version = "1.01";
        }
    }

    class D5300_0102 : Firmware
    {
        static byte[] mbps_24_20be = { 0x00, 0x36, 0x6e, 0x01, 0x00, 0x2D, 0x31, 0x01 };
        static byte[] mbpa_42_35be = { 0x80, 0xDE, 0x80, 0x02, 0xC0, 0x0E, 0x16, 0x02 };
        static byte[] mbps_64_60be = { 0x00, 0x90, 0xd0, 0x03, 0x00, 0x87, 0x93, 0x03 };

        // the above values divided by 1,000
        static byte[] mbps_24_20tbe = { 0xC0, 0x5D, 0x00, 0x00, 0x20, 0x4E };
        static byte[] mbps_42_35tbe = { 0x10, 0xA4, 0x00, 0x00, 0xB8, 0x88 };
        static byte[] mbps_64_60tbe = { 0x00, 0xFA, 0x00, 0x00, 0x60, 0xEA };

        Patch[] patch_hq_64mbps = {
            new Patch(1, 0x12B5D50, mbpa_42_35be, mbps_64_60be),
            new Patch(1, 0x12B5D60, mbpa_42_35be, mbps_64_60be),
            new Patch(1, 0x12B5D70, mbps_24_20be, mbps_64_60be),
            new Patch(1, 0x12B5D80, mbps_24_20be, mbps_64_60be),
            new Patch(1, 0x12B5D90, mbps_24_20be, mbps_64_60be),
            new Patch(1, 0x12B5DA0, mbps_24_20be, mbps_64_60be),
            new Patch(1, 0x12B5DB0, mbps_24_20be, mbps_64_60be),
            new Patch(1, 0x12B5760, mbps_24_20tbe, mbps_64_60tbe),
            new Patch(1, 0x12B5778, mbps_24_20tbe, mbps_64_60tbe),
            new Patch(1, 0x12B5790, mbps_24_20tbe, mbps_64_60tbe),
            new Patch(1, 0x12B57A8, mbps_24_20tbe, mbps_64_60tbe),
            new Patch(1, 0x12B57C0, mbps_24_20tbe, mbps_64_60tbe),
            new Patch(1, 0x12B57D8, mbps_42_35tbe, mbps_64_60tbe),
            new Patch(1, 0x12B57F0, mbps_42_35tbe, mbps_64_60tbe),
            new Patch(1, 0x12B5808, mbps_24_20tbe, mbps_64_60tbe),
            new Patch(1, 0x12B5820, mbps_24_20tbe, mbps_64_60tbe),
            new Patch(1, 0x12B5838, mbps_24_20tbe, mbps_64_60tbe),
            new Patch(1, 0x12B5850, mbps_24_20tbe, mbps_64_60tbe),
            new Patch(1, 0x12B5868, mbps_24_20tbe, mbps_64_60tbe),
            new Patch(1, 0x12B58B0, mbps_24_20tbe, mbps_64_60tbe),
        };

        public D5300_0102()
        {
            p = new Package();
            Model = "D5300";
            Version = "1.02";

            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 64mbps Bit-rate", patch_hq_64mbps));
        }
    }

    class D5300_0103 : Firmware
    {
       Patch[] patch_liveview_no_timeout_15m = 
           {
               new Patch(1,0x0092E0, new byte[]{0xA0, 0xBB, 0x0D, 0x00}, new byte[] {0x80, 0x27, 0xCB, 0x05} ),
               new Patch(1,0x00C0E8, new byte[]{0xA0, 0xBB, 0x0D, 0x00}, new byte[] {0x80, 0x27, 0xCB, 0x05} ),
               new Patch(1,0x2DAF2C, new byte[]{0xA0, 0xBB, 0x0D, 0x00}, new byte[] {0x80, 0x27, 0xCB, 0x05} ),
           };

        public D5300_0103()
        {
            p = new Package();
            Model = "D5300";
            Version = "1.03";

            Patches.Add(new PatchSet(PatchLevel.DevOnly, "Liveview (15min) No Timeout", patch_liveview_no_timeout_15m));
        }
    }
}
