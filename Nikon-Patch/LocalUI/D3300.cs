namespace Nikon_Patch
{
    class D3300_0101 : Firmware
    {
        public D3300_0101()
        {
            p = new Package();
            Model = "D3300";
            Version = "1.01";
        }
    }

    class D3300_0102 : Firmware
    {
        static byte[] mbps_24_20be = { 0x00, 0x36, 0x6e, 0x01, 0x00, 0x2D, 0x31, 0x01 };
        static byte[] mbps_64_60be = { 0x00, 0x90, 0xd0, 0x03, 0x00, 0x87, 0x93, 0x03 };

        // the above values divided by 1,000
        static byte[] mbps_24_20tbe = { 0xC0, 0x5D, 0x00, 0x00, 0x20, 0x4E };
        static byte[] mbps_64_60tbe = { 0x00, 0xFA, 0x00, 0x00, 0x60, 0xEA };

        Patch[] patch_hq_64mbps = {
            new Patch(1, 0x143C8BC, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x143C8CC, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x143C8D4, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x143C8E4, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x143C8F4, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x143C904, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x143C914, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x143C2C4, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x143C2DC, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x143C2F4, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x143C30C, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x143C324, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x143C348, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x143C360, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x143C36C, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x143C384, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x143C39C, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x143C3B4, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x143C3CC, mbps_24_20tbe, mbps_64_60tbe ),
            new Patch(1, 0x143C414, mbps_24_20tbe, mbps_64_60tbe ),
        };

        Patch[] patch_liveview_no_timeout_15m =
    {
               new Patch(1,0x0092F0, new byte[]{0xA0, 0xBB, 0x0D, 0x00}, new byte[] {0x80, 0x27, 0xCB, 0x05} ),
               new Patch(1,0x00C318, new byte[]{0xA0, 0xBB, 0x0D, 0x00}, new byte[] {0x80, 0x27, 0xCB, 0x05} ),
               new Patch(1,0x3039F0, new byte[]{0xA0, 0xBB, 0x0D, 0x00}, new byte[] {0x80, 0x27, 0xCB, 0x05} ),
           };

        public D3300_0102()
        {
            p = new Package();
            Model = "D3300";
            Version = "1.02";

            Patches.Add(new PatchSet(PatchLevel.Alpha, "Video 1080 HQ 64mbps Bit-rate", patch_hq_64mbps));
            Patches.Add(new PatchSet(PatchLevel.Alpha, "Liveview (15min) No Timeout", patch_liveview_no_timeout_15m));
        }
    }
}
