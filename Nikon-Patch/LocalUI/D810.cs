using System;

namespace Nikon_Patch
{
    class D810_0102 : Firmware
    {
        public D810_0102()
        {
            p = new Package();
            Model = "D810";
            Version = "1.02";
        }
    }

    class D810_0112 : Firmware
    {
        public D810_0112()
        {
            p = new Package();
            Model = "D810";
            Version = "1.12";
        }
    }

    class D810_0114 : Firmware
    {
        // Offsets for D810 v1.14 (b920_114.bin - Block 1)
        // 1080p Table Start: 0x28B994
        // Entry size: 0x1C (28 bytes)
        // Order: 60p HQ, 60p NQ, 50p HQ, 50p NQ, 30p HQ, 30p NQ, 25p HQ, 25p NQ, 24p HQ, 24p NQ

        // Original Bitrates (Max/Avg) - Little Endian
        static byte[] v_42_35 = { 0x80, 0xDE, 0x80, 0x02, 0xC0, 0x0E, 0x16, 0x02 }; // 42M / 35M
        static byte[] v_24_20 = { 0x00, 0x36, 0x6E, 0x01, 0x00, 0x2D, 0x31, 0x01 }; // 24M / 20M
        static byte[] v_12_10 = { 0x00, 0x1B, 0xB7, 0x00, 0x80, 0x96, 0x98, 0x00 }; // 12M / 10M

        // New Bitrates
        static byte[] v_36 = { 0x00, 0x51, 0x25, 0x02, 0x00, 0x51, 0x25, 0x02 }; // 36M / 36M
        static byte[] v_54 = { 0x80, 0xF9, 0x37, 0x03, 0x80, 0xF9, 0x37, 0x03 }; // 54M / 54M
        static byte[] v_64 = { 0x00, 0x90, 0xD0, 0x03, 0x00, 0x87, 0x93, 0x03 }; // 64M / 60M (Same as D5300 patch)
        
        static byte[] v_24 = { 0x00, 0x36, 0x6E, 0x01, 0x00, 0x36, 0x6E, 0x01 }; // 24M / 24M (Target for NQ)
        static byte[] v_42 = { 0x80, 0xDE, 0x80, 0x02, 0x80, 0xDE, 0x80, 0x02 }; // 42M / 42M (Target for NQ 60p)

        // Offsets
        const int o_60_hq = 0x28B994;
        const int o_60_nq = 0x28B9B0;
        const int o_50_hq = 0x28B9CC;
        const int o_50_nq = 0x28B9E8;
        const int o_30_hq = 0x28BA04;
        const int o_30_nq = 0x28BA20;
        const int o_25_hq = 0x28BA3C;
        const int o_25_nq = 0x28BA58;
        const int o_24_hq = 0x28BA74;
        const int o_24_nq = 0x28BA90;

        Patch[] patch_1080_36mbps = {
            // HQ -> 36M (For 30/25/24), 60/50 -> 54M (Boost)
            new Patch(1, o_60_hq, v_42_35, v_54),
            new Patch(1, o_50_hq, v_42_35, v_54),
            new Patch(1, o_30_hq, v_24_20, v_36),
            new Patch(1, o_25_hq, v_24_20, v_36),
            new Patch(1, o_24_hq, v_24_20, v_36),
        };

        Patch[] patch_1080_54mbps = {
            // HQ -> 54M
            new Patch(1, o_60_hq, v_42_35, v_54),
            new Patch(1, o_50_hq, v_42_35, v_54),
            new Patch(1, o_30_hq, v_24_20, v_54),
            new Patch(1, o_25_hq, v_24_20, v_54),
            new Patch(1, o_24_hq, v_24_20, v_54),
        };
        
        Patch[] patch_1080_64mbps_nq_old_hq = {
             // HQ -> 64M
            new Patch(1, o_60_hq, v_42_35, v_64),
            new Patch(1, o_50_hq, v_42_35, v_64),
            new Patch(1, o_30_hq, v_24_20, v_64),
            new Patch(1, o_25_hq, v_24_20, v_64),
            new Patch(1, o_24_hq, v_24_20, v_64),
            
            // NQ -> Old HQ (12->24, 24->42)
            new Patch(1, o_60_nq, v_24_20, v_42_35), // 24->42
            new Patch(1, o_50_nq, v_24_20, v_42_35),
            new Patch(1, o_30_nq, v_12_10, v_24_20), // 12->24
            new Patch(1, o_25_nq, v_12_10, v_24_20),
            new Patch(1, o_24_nq, v_12_10, v_24_20),
        };

        Patch[] patch_1080_36mbps_nq_old_hq = {
            // HQ -> 36M / 54M
            new Patch(1, o_60_hq, v_42_35, v_54),
            new Patch(1, o_50_hq, v_42_35, v_54),
            new Patch(1, o_30_hq, v_24_20, v_36),
            new Patch(1, o_25_hq, v_24_20, v_36),
            new Patch(1, o_24_hq, v_24_20, v_36),
            
             // NQ -> Old HQ
            new Patch(1, o_60_nq, v_24_20, v_42_35),
            new Patch(1, o_50_nq, v_24_20, v_42_35),
            new Patch(1, o_30_nq, v_12_10, v_24_20),
            new Patch(1, o_25_nq, v_12_10, v_24_20),
            new Patch(1, o_24_nq, v_12_10, v_24_20),
        };

        Patch[] patch_1080_54mbps_nq_old_hq = {
            // HQ -> 54M
            new Patch(1, o_60_hq, v_42_35, v_54),
            new Patch(1, o_50_hq, v_42_35, v_54),
            new Patch(1, o_30_hq, v_24_20, v_54),
            new Patch(1, o_25_hq, v_24_20, v_54),
            new Patch(1, o_24_hq, v_24_20, v_54),
            
             // NQ -> Old HQ
            new Patch(1, o_60_nq, v_24_20, v_42_35),
            new Patch(1, o_50_nq, v_24_20, v_42_35),
            new Patch(1, o_30_nq, v_12_10, v_24_20),
            new Patch(1, o_25_nq, v_12_10, v_24_20),
            new Patch(1, o_24_nq, v_12_10, v_24_20),
        };

        Patch[] patch_time_limit = {
            new Patch(1, 0x2BA67C, new byte[] { 0x80, 0x4F, 0x12, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 20min
            new Patch(1, 0x2BA684, new byte[] { 0x80, 0x4F, 0x12, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 20min
            new Patch(1, 0x2BA688, new byte[] { 0x80, 0x4F, 0x12, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 20min
            new Patch(1, 0x2BA690, new byte[] { 0x80, 0x4F, 0x12, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 20min
            new Patch(1, 0x2BA698, new byte[] { 0x80, 0x4F, 0x12, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 20min
            new Patch(1, 0x2BA6A0, new byte[] { 0x80, 0x4F, 0x12, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 20min
            new Patch(1, 0x2BA6A8, new byte[] { 0x80, 0x4F, 0x12, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 20min
            new Patch(1, 0x2EBBB0, new byte[] { 0x80, 0x4F, 0x12, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 20min
            new Patch(1, 0x2BA68C, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA694, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA69C, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6A4, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6AC, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6B0, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6B4, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6B8, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6BC, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6C0, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6C4, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6C8, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6CC, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6D0, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6D4, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6D8, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6DC, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6E0, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6E4, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6E8, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2BA6EC, new byte[] { 0x58, 0x73, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 29m59s
            new Patch(1, 0x2EBBB8, new byte[] { 0x40, 0x77, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 30min
            new Patch(1, 0x2EBC10, new byte[] { 0x40, 0x77, 0x1B, 0x00 }, new byte[] { 0x80, 0x27, 0xCB, 0x05 }), // 30min
        };


        public D810_0114()
        {
            p = new Package();
            Model = "D810";
            Version = "1.14";

            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 36mbps Bit-rate", patch_1080_36mbps, patch_1080_54mbps, patch_1080_36mbps_nq_old_hq, patch_1080_54mbps_nq_old_hq, patch_1080_64mbps_nq_old_hq));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 54mbps Bit-rate", patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_nq_old_hq, patch_1080_54mbps_nq_old_hq, patch_1080_64mbps_nq_old_hq));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 36mbps Bit-rate NQ old HQ", patch_1080_36mbps_nq_old_hq, patch_1080_36mbps, patch_1080_54mbps, patch_1080_54mbps_nq_old_hq, patch_1080_64mbps_nq_old_hq));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 54mbps Bit-rate NQ old HQ", patch_1080_54mbps_nq_old_hq, patch_1080_36mbps, patch_1080_54mbps, patch_1080_36mbps_nq_old_hq, patch_1080_64mbps_nq_old_hq));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 64mbps Bit-rate NQ old HQ", patch_1080_64mbps_nq_old_hq, patch_1080_36mbps, patch_1080_54mbps, patch_1080_36mbps_nq_old_hq, patch_1080_54mbps_nq_old_hq));
            
            Patches.Add(new PatchSet(PatchLevel.Beta, "Remove Time Based Video Restrictions", patch_time_limit));
        }
    }

    class D810A_0102 : Firmware
    {
        public D810A_0102()
        {
            p = new Package();
            Model = "D810A";
            Version = "1.02";
        }
    }
}