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
        static byte[] mbps_24_20be = { 0xC0, 0x5D, 0x00, 0x00, 0x20, 0x4E }; 
        static byte[] mbps_64_60be = { 0x00, 0xFA, 0x00, 0x00, 0x60, 0xEA }; 
        
        //the above values divided by 1,000?
        static byte[] mbps_24_20bf = { 0x10, 0xA4, 0x00, 0x00, 0xB8, 0x88 }; 
        static byte[] mbps_64_60bf = { 0x00, 0xFA, 0x00, 0x00, 0x60, 0xEA }; //repe
        
        static byte[] mbps_24_20c0 = { 0x80, 0xDE, 0x80, 0x02, 0xC0, 0x0E, 0x16, 0x02 };
        static byte[] mbps_64_60c0 = { 0x00, 0x90, 0xD0, 0x03, 0x00, 0x87, 0x93, 0x03 };
        
        static byte[] mbps_24_20c1 = { 0x00, 0x36, 0x6E, 0x01, 0x00, 0x2D, 0x31, 0x01 };
        static byte[] mbps_64_60c1 = { 0x00, 0x90, 0xD0, 0x03, 0x00, 0x87, 0x93, 0x03 }; //repe
        
        Patch[] patch_hq_64mbps = {
            new Patch(1, 0x12B5760, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x12B5778, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x12B5790, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x12B57A8, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x12B5808, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x12B5820, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x12B5838, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x12B5850, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x12B5868, mbps_24_20be, mbps_64_60be ),
            new Patch(1, 0x12B57C0, mbps_24_20be, mbps_64_60be ),
            
            new Patch(1, 0x12B57D8, mbps_24_20bf, mbps_64_60bf ),
            new Patch(1, 0x12B57F0, mbps_24_20bf, mbps_64_60bf ),
            new Patch(1, 0x12B58B0, mbps_24_20bf, mbps_64_60bf ),

            new Patch(1, 0x12B5D50, mbps_24_20c0, mbps_64_60c0 ),
            new Patch(1, 0x12B5D60, mbps_24_20c0, mbps_64_60c0 ),
            
            new Patch(1, 0x12B5D70, mbps_24_20c1, mbps_64_60c1 ),
            new Patch(1, 0x12B5D80, mbps_24_20c1, mbps_64_60c1 ),
            new Patch(1, 0x12B5D90, mbps_24_20c1, mbps_64_60c1 ),
            new Patch(1, 0x12B5DA0, mbps_24_20c1, mbps_64_60c1 ),
            new Patch(1, 0x12B5DB0, mbps_24_20c1, mbps_64_60c1 ),
            };
        
        public D5300_0102()
        {
            p = new Package();
            Model = "D5300";
            Version = "1.02";
            
            Patches.Add(new PatchSet(PatchLevel.Alpha, "Video 1080 HQ 64mbps Bit-rate", patch_hq_64mbps));
        }
    }
}        
