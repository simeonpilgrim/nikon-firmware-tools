namespace Nikon_Patch
{
    class D3200_0101 : Firmware
    {
        Patch[] patch_vid_time = {
            new Patch(1, 0x240E4, new byte[] { 0xE4, 0x03 }, new byte[] { 0xE0, 0x03 }), // unlimited recording 1/3
            new Patch(1, 0x24288, new byte[] { 0xF4, 0x05 }, new byte[] { 0xF0, 0x05 }), // unlimited recording 2/3
            new Patch(1, 0x2500A, new byte[] { 0xE2, 0x0B }, new byte[] { 0xE0, 0x0B }), // unlimited recording 3/3
                          };

        Patch[] patch_battery = {
            new Patch(0, 0x8E884, new byte[] { 0xF0, 0x00, 0x2A, 0x1C }, new byte[] { 0x65, 0x00, 0x65, 0x00 }), // battery 1
            new Patch(0, 0x8E8D8, new byte[] { 0xF0, 0x00, 0x2A, 0x03 }, new byte[] { 0x65, 0x00, 0x65, 0x00 }), // battery 2
            new Patch(0, 0x8E91A, new byte[] { 0xF0, 0x00, 0x61, 0x05 }, new byte[] { 0x65, 0x00, 0x65, 0x00 }), // battery 3
            new Patch(0, 0x8E94A, new byte[] { 0xF2 }, new byte[] { 0xF3 }), // BATTERY 4
            new Patch(0, 0x8E950, new byte[] { 0x61, 0x0B }, new byte[] { 0x60, 0x0F }), // BATTERY 5
            new Patch(0, 0x8F467, new byte[] { 0x00 }, new byte[] { 0x01 }), // BATTERY 6
                          };
        Patch[] patch_battery_2 = {
            new Patch(0, 0x08D66A, new byte[] { 0xF0, 0x28, 0xA2, 0x52, 0xF0, 0x60 } , new byte[] { 0x6A, 0x20, 0x6A, 0x20, 0x6A, 0x20 } ),
            new Patch(0, 0x08EAB0, new byte[] { 0xF0, 0x28, 0xA4, 0x92 } , new byte[] { 0x6C, 0x20, 0x6C, 0x20 } ),
                                };

        Patch[] patch_LV_Manual = {
            new Patch(0, 0x36E16, new byte[] { 0xA0, 0x62 }, new byte[] { 0x6B, 0xFF }),
                          };

        Patch[] patch_Language_Fix = {
            new Patch(1, 0x342C2C, new byte[] { 0xE2, 0x08 }, new byte[] { 0xE0, 0x08 }),
            new Patch(1, 0x346a9a, new byte[] { 0xE2, 0x11 }, new byte[] { 0xE0, 0x11 }),
            new Patch(1, 0x3D72A8, new byte[] { 0xE2, 0x06 }, new byte[] { 0xE0, 0x06 }),
            new Patch(1, 0x3E320C, new byte[] { 0xB1, 0xF4 }, new byte[] { 0xC0, 0x04 }),
                          };

        Patch[] patch_1080_36mbps = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x02 } ),
                                  };

        Patch[] patch_1080_36mbps_NQ = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239A0, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239A6, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239CC, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239D2, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A18, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23A1E, Sys.mbps10, Sys.mbps20 ),
                                  };

        Patch[] patch_1080_54mbps = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
                                  };

        Patch[] patch_1080_54mbps_NQ = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239A0, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239A6, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239CC, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239D2, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A18, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23A1E, Sys.mbps10, Sys.mbps20 ),
                                  };

        Patch[] patch_Extend_LiveView = {
            new Patch(1, 0x238F3E, new byte[] { 0x03, 0x84 }, new byte[] { 0x2A, 0x30 }),
        };

        public D3200_0101()
        {
            p = new Package();
            Model = "D3200";
            Version = "1.01";

            //Patches.Add(new PatchSet(true, "Remove Time Based Video Restrictions", patch_vid_time));
            Patches.Add(new PatchSet(PatchLevel.Released, "Non-brand Battery", patch_battery));
            Patches.Add(new PatchSet(PatchLevel.Released, "Multi-Language Support", patch_Language_Fix));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 36mbps Bit-rate", patch_1080_36mbps, patch_1080_54mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 54mbps Bit-rate", patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 36mbps Bit-rate NQ old HQ", patch_1080_36mbps_NQ, patch_1080_54mbps, patch_1080_36mbps, patch_1080_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 54mbps Bit-rate NQ old HQ", patch_1080_54mbps_NQ, patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Change 15 - minute LiveView to 3 - hour", patch_Extend_LiveView));
        }
    }

    class D3200_0102 : Firmware
    {
        Patch[] patch_LV_Manual = {
            new Patch(0, 0x3739A, new byte[] { 0xA0, 0x62 }, new byte[] { 0x6B, 0xFF }),
                          };

        Patch[] patch_battery = {
            new Patch(0, 0x08D66A, new byte[] { 0xF0, 0x28, 0xA2, 0x52, 0xF0, 0x60 } , new byte[] { 0x6A, 0x20, 0x6A, 0x20, 0x6A, 0x20 } ),
            new Patch(0, 0x08EBA8, new byte[] { 0xF0, 0x28, 0xA4, 0x92 } , new byte[] { 0x6C, 0x20, 0x6C, 0x20 } ),
        };

        Patch[] patch_Language_Fix = {
            new Patch(1, 0x3423D8, new byte[] { 0xE2, 0x08 }, new byte[] { 0xE0, 0x08 }),
            new Patch(1, 0x346246, new byte[] { 0xE2, 0x11 }, new byte[] { 0xE0, 0x11 }),
            new Patch(1, 0x3D6A54, new byte[] { 0xE2, 0x06 }, new byte[] { 0xE0, 0x06 }),
            new Patch(1, 0x3E29B8, new byte[] { 0xB1, 0xF4 }, new byte[] { 0xC0, 0x04 }),
                          };

        Patch[] patch_1080_36mbps = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x02 } ),
                                  };

        Patch[] patch_1080_54mbps = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
                                  };

        Patch[] patch_1080_36mbps_NQ = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239A0, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239A6, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239CC, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239D2, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A18, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23A1E, Sys.mbps10, Sys.mbps20 ),
                                  };

        Patch[] patch_1080_54mbps_NQ = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239A0, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239A6, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239CC, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239D2, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A18, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23A1E, Sys.mbps10, Sys.mbps20 ),
                                  };

        Patch[] patch_Extend_LiveView = {
            new Patch(1, 0x2388C2, new byte[] { 0x03, 0x84 }, new byte[] { 0x2A, 0x30 }),
        };

        public D3200_0102()
        {
            p = new Package();
            Model = "D3200";
            Version = "1.02";

            Patches.Add(new PatchSet(PatchLevel.Released, "Non-Brand Battery", patch_battery));
            Patches.Add(new PatchSet(PatchLevel.Released, "Multi-Language Support", patch_Language_Fix));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 36mbps Bit-rate", patch_1080_36mbps, patch_1080_54mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 54mbps Bit-rate", patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 36mbps Bit-rate NQ old HQ", patch_1080_36mbps_NQ, patch_1080_54mbps, patch_1080_36mbps, patch_1080_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 54mbps Bit-rate NQ old HQ", patch_1080_54mbps_NQ, patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Change 15 - minute LiveView to 3 - hour", patch_Extend_LiveView));
        }
    }

    class D3200_0103 : Firmware
    {
        Patch[] patch_1080_36mbps = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x02 } ),
                                  };

        Patch[] patch_1080_36mbps_NQ = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239A0, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239A6, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239CC, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239D2, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A18, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23A1E, Sys.mbps10, Sys.mbps20 ),
                                  };

        Patch[] patch_1080_54mbps = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
                                  };

        Patch[] patch_Language_Fix = {
            new Patch(1, 0x3432b8, new byte[] { 0xE2, 0x08 }, new byte[] { 0xE0, 0x08 }),
            new Patch(1, 0x347126, new byte[] { 0xE2, 0x11 }, new byte[] { 0xE0, 0x11 }),
            new Patch(1, 0x3D7A1C, new byte[] { 0xE2, 0x06 }, new byte[] { 0xE0, 0x06 }),    
            new Patch(1, 0x3E398A, new byte[] { 0xB1, 0xF4 }, new byte[] { 0xC0, 0x04 }),
                          };

        Patch[] patch_1080_54mbps_NQ = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239A0, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239A6, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239CC, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239D2, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A18, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23A1E, Sys.mbps10, Sys.mbps20 ),
                                  };

        Patch[] patch_1080_64mbps_NQ = {
            new Patch(1, 0x2398C, Sys.mbps24, Sys.mbps64 ),
            new Patch(1, 0x23992, Sys.mbps20, Sys.mbps60 ),
            new Patch(1, 0x239A0, Sys.mbps12, Sys.mbps24 ),
            new Patch(1, 0x239A6, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x239B8, Sys.mbps24, Sys.mbps64 ),
            new Patch(1, 0x239BE, Sys.mbps20, Sys.mbps60 ),
            new Patch(1, 0x239CC, Sys.mbps12, Sys.mbps24 ),
            new Patch(1, 0x239D2, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x23A04, Sys.mbps24, Sys.mbps64 ),
            new Patch(1, 0x23A0A, Sys.mbps20, Sys.mbps60 ),
            new Patch(1, 0x23A18, Sys.mbps12, Sys.mbps24 ),
            new Patch(1, 0x23A1E, Sys.mbps10, Sys.mbps20 ),
                                  };

        Patch[] patch_Extend_LiveView = {
            new Patch(1, 0x2397EE, new byte[] { 0x03, 0x84 }, new byte[] { 0x2A, 0x30 }),
        };

        public D3200_0103()
        {
            p = new Package();
            Model = "D3200";
            Version = "1.03";

            Patches.Add(new PatchSet(PatchLevel.Released, "Multi-Language Support", patch_Language_Fix));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 36mbps Bit-rate", patch_1080_36mbps, patch_1080_54mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 54mbps Bit-rate", patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 36mbps Bit-rate NQ old HQ", patch_1080_36mbps_NQ, patch_1080_54mbps, patch_1080_36mbps, patch_1080_54mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 54mbps Bit-rate NQ old HQ", patch_1080_54mbps_NQ, patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 64mbps Bit-rate NQ old HQ", patch_1080_64mbps_NQ, patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Change 15 - minute LiveView to 3 - hour", patch_Extend_LiveView));
        }
    }

    class D3200_0104 : Firmware
    {
        Patch[] patch_1080_36mbps = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x02 } ),
                                  };

        Patch[] patch_1080_36mbps_NQ = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239A0, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239A6, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x239CC, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239D2, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x23A18, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23A1E, Sys.mbps10, Sys.mbps20 ),
                                  };

        Patch[] patch_1080_54mbps = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
                                  };

        Patch[] patch_Language_Fix = {
            new Patch(1, 0x343EE4, new byte[] { 0xE2, 0x08 }, new byte[] { 0xE0, 0x08 }),
            new Patch(1, 0x347D52, new byte[] { 0xE2, 0x11 }, new byte[] { 0xE0, 0x11 }),
            new Patch(1, 0x3D8648, new byte[] { 0xE2, 0x06 }, new byte[] { 0xE0, 0x06 }),    
            new Patch(1, 0x3E45B6, new byte[] { 0xB1, 0xF4 }, new byte[] { 0xC0, 0x04 }),
                          };

        Patch[] patch_1080_54mbps_NQ = {
            new Patch(1, 0x2398C, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23992, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239A0, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239A6, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x239B8, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239BE, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x239CC, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x239D2, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x23A04, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A0A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x23A18, Sys.mbps12 , Sys.mbps24 ),
            new Patch(1, 0x23A1E, Sys.mbps10, Sys.mbps20 ),
                                  };

        Patch[] patch_1080_64mbps_NQ = {
            new Patch(1, 0x2398C, Sys.mbps24, Sys.mbps64 ),
            new Patch(1, 0x23992, Sys.mbps20, Sys.mbps60 ),
            new Patch(1, 0x239A0, Sys.mbps12, Sys.mbps24 ),
            new Patch(1, 0x239A6, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x239B8, Sys.mbps24, Sys.mbps64 ),
            new Patch(1, 0x239BE, Sys.mbps20, Sys.mbps60 ),
            new Patch(1, 0x239CC, Sys.mbps12, Sys.mbps24 ),
            new Patch(1, 0x239D2, Sys.mbps10, Sys.mbps20 ),

            new Patch(1, 0x23A04, Sys.mbps24, Sys.mbps64 ),
            new Patch(1, 0x23A0A, Sys.mbps20, Sys.mbps60 ),
            new Patch(1, 0x23A18, Sys.mbps12, Sys.mbps24 ),
            new Patch(1, 0x23A1E, Sys.mbps10, Sys.mbps20 ),
                                  };

        Patch[] patch_Extend_LiveView = {
            new Patch(1, 0x23A352, new byte[] { 0x03, 0x84 }, new byte[] { 0x2A, 0x30 }),
        };

        // LV audio capture (which feeds HDMI audio) only runs while the sound level meter is shown.
        // Replace "LDI:32 #0x3D09D6,R12; CALL @R12" (meter visible?) with "LDI:8 #1,R4; NOP x3" at each capture start/stop decision.
        static readonly byte[] meter_check = { 0x9F, 0x8C, 0x00, 0x3D, 0x09, 0xD6, 0x97, 0x1C };
        static readonly byte[] meter_forced = { 0xC0, 0x14, 0x9F, 0xA0, 0x9F, 0xA0, 0x9F, 0xA0 };
        Patch[] patch_hdmi_audio = {
            new Patch(1, 0x1965AC, meter_check, meter_forced),
            new Patch(1, 0x197668, meter_check, meter_forced),
            new Patch(1, 0x197746, meter_check, meter_forced),
            new Patch(1, 0x19781A, meter_check, meter_forced),
            new Patch(1, 0x19B33C, meter_check, meter_forced),
            new Patch(1, 0x200216, meter_check, meter_forced),
        };

        public D3200_0104()
        {
            p = new Package();
            Model = "D3200";
            Version = "1.04";

            Patches.Add(new PatchSet(PatchLevel.Beta, "HDMI audio without sound level meter", patch_hdmi_audio));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Multi-Language Support", patch_Language_Fix));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 36mbps Bit-rate", patch_1080_36mbps, patch_1080_54mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 54mbps Bit-rate", patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 36mbps Bit-rate NQ old HQ", patch_1080_36mbps_NQ, patch_1080_54mbps, patch_1080_36mbps, patch_1080_54mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 54mbps Bit-rate NQ old HQ", patch_1080_54mbps_NQ, patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_NQ, patch_1080_64mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 64mbps Bit-rate NQ old HQ", patch_1080_64mbps_NQ, patch_1080_54mbps, patch_1080_36mbps, patch_1080_36mbps_NQ, patch_1080_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Change 15 - minute LiveView to 3 - hour", patch_Extend_LiveView));
        }
    }
}
