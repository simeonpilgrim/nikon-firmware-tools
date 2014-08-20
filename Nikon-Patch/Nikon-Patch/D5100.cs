﻿using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Linq.Expressions;
using System.Diagnostics;
using System.Text;
using System.Collections.ObjectModel;


namespace Nikon_Patch
{
    class D5100_0101 : Firmware
    {
        Patch[] patch_1 = {
            new Patch(1, 0x74AD4, new byte[] { 0xE4, 0x03 }, new byte[] { 0xE0, 0x03 }), // unlimited recording 1/2
            new Patch(1, 0x755A8, new byte[] { 0xE2, 0x0B }, new byte[] { 0xE0, 0x0B }), // unlimited recording 2/2
                          };


        Patch[] patch_3_nocomp = {
            //new Patch(1, 0xC083C, new byte[] { 0x91, 0x20 }, new byte[] { 0x81, 0xD0 }), // Turn NEF On Compression
         
            new Patch(1, 0xC29C0, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xC2E82, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xC5F3A, new byte[] { 0xE3, 0x02 }, new byte[] { 0xE1, 0x02 }), // NEF Compression
            new Patch(1, 0xD7AEE, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD7B74, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD7C06, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD7E72, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD7EF2, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD7F86, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD7FEC, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD8D48, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD8DCA, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x1159C6, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x115C2A, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x115C8C, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x1172B8, new byte[] { 0xE3, 0x02 }, new byte[] { 0xE1, 0x02 }), // NEF Compression
            new Patch(1, 0x144018, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x1466B8, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x154A04, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x2E3010, new byte[] { 0xE2, 0x15 }, new byte[] { 0xE0, 0x15 }), // NEF Compression
       
            // Check 0x10
            new Patch(1, 0x115408, new byte[] { 0xE2, 0x05 }, new byte[] { 0xE1, 0x05 }), // NEF Compression
            new Patch(1, 0x115560, new byte[] { 0xE2, 0x04 }, new byte[] { 0xE1, 0x04 }), // NEF Compression
            new Patch(1, 0x13DC5E, new byte[] { 0xE2, 0x04 }, new byte[] { 0xE1, 0x04 }), // NEF Compression
            new Patch(1, 0x1A617C, new byte[] { 0xF2, 0x06 }, new byte[] { 0xF1, 0x06 }), // NEF Compression
            //new Patch(1, 0x2AE1C2, new byte[] { 0xE2, 0x2B }, new byte[] { 0xE1, 0x2B }), // NEF Compression // this alters the menu
             };

        Patch[] patch_3_LosslessNEF = {
            // 0x00204660 (01C460 file )
            // ldi:32 0x0037af1e, r12
            // jmp @r12
            new Patch(1, 0x1C4660, new byte[] { 0x9F, 0x85, 0x8F, 0x9A, 0x85, 0x63 }, new byte[] { 0x9F, 0x8C, 0x00, 0x37, 0xAF, 0x1E }), 
            new Patch(1, 0x1C4666, new byte[] { 0x9F, 0x87, 0x8F, 0x9A, 0x85, 0x56 }, new byte[] { 0x97, 0x0C, 0x9F, 0xA0, 0x9F, 0xA0 }), 

            // 37AF1E (033AF1E)
            // ldi:32 0x8F8515B1, r0
            // ldi:32 0x0020466C, r12
            // borl #8, r0
            // ldi:32 0x8F9A8563, r5
            // ldi:32  #Set03_Copy, r7
            // jmp @r12

            new Patch(1, 0x33AF1E, new byte[] { 0x17, 0x81, 0x0F, 0x01, 0x9f, 0x85}, new byte[] {0x9F, 0x80, 0x8F, 0x85, 0x15, 0xB1 }),
            new Patch(1, 0x33AF24, new byte[] { 0xBF, 0xFF, 0xFF, 0xFF, 0x8B, 0x46 }, new byte[] { 0x9F, 0x8C, 0x00, 0x20, 0x46, 0x6C }), 
            new Patch(1, 0x33AF2A, new byte[] { 0xCD, 0x1C }, new byte[] { 0x90, 0x80 }),
            new Patch(1, 0x33AF2C, new byte[] { 0xC1, 0xC4, 0x97, 0x8C, 0x1F, 0x40 }, new byte[] { 0x9F, 0x85, 0x8F, 0x9A, 0x85, 0x63 }),
            new Patch(1, 0x33AF32, new byte[] { 0x9F, 0xA0, 0x9F, 0x8C, 0x00, 0x31 }, new byte[] { 0x9F, 0x87, 0x8F, 0x9A, 0x85, 0x56 }),
            new Patch(1, 0x33AF38, new byte[] { 0x61, 0xB6 }, new byte[] { 0x97, 0x0C }), 
                                    };


        Patch[] patch_NEF_Overscan = {

            new Patch(1, 0x227D30, new byte[] { 0x00, 0x20, 0x46, 0x60 }, new byte[] { 0x00, 0x37, 0xAF, 0x5A }), 

            // 37AF5A (033AF5A)
            // stm0 (4,5,6,7)
            // stm1 (8,9,10,11)
            // st rp, @15

            // ldi:32 0x8F8515B1, r0
            // ldi:32 0x00108a46, r12
            // borh #2, r0
            // call @r12
            // nop

            // ld @r15, rp
            // ldm1 (8,9,10,11)
            // ldm0 (4,5,6,7)
            // ldi:32 0x00204660, r12
            // jmp @r12

            new Patch(1, 0x33AF5A, new byte[] { 0x17, 0x81 }, new byte[] {0x8E, 0x0F }),
            new Patch(1, 0x33AF5C, new byte[] { 0x0F, 0x01 }, new byte[] {0x8F, 0xF0 }),
            new Patch(1, 0x33AF5E, new byte[] { 0x9F, 0x85 }, new byte[] {0x17, 0x81 }),

            new Patch(1, 0x33AF60, new byte[] { 0xBF, 0xFF, 0xFF, 0xFF, 0x8B, 0x46 }, new byte[] { 0x9F, 0x80, 0x8F, 0x85, 0x15, 0xB1 }),
            new Patch(1, 0x33AF66, new byte[] { 0xCD, 0x1C, 0xC1, 0xC4, 0x97, 0x8C }, new byte[] { 0x9F, 0x8C, 0x00, 0x10, 0x8A, 0x46 }), 
            new Patch(1, 0x33AF6C, new byte[] { 0x1F, 0x40 }, new byte[] { 0x91, 0x20 }),
            new Patch(1, 0x33AF6E, new byte[] { 0x9F, 0xA0 }, new byte[] { 0x97, 0x1C }),
            new Patch(1, 0x33AF70, new byte[] { 0x9F, 0x8C }, new byte[] { 0x9F, 0xA0 }),

            new Patch(1, 0x33AF72, new byte[] { 0x00, 0x31 }, new byte[] { 0x07, 0x81 }),
            new Patch(1, 0x33AF74, new byte[] { 0x61, 0xDE }, new byte[] { 0x8D, 0x0F }),
            new Patch(1, 0x33AF76, new byte[] { 0x9F, 0x1C }, new byte[] { 0x8C, 0xF0 }),
            new Patch(1, 0x33AF78, new byte[] { 0x8B, 0x64, 0x9F, 0x8C, 0x00, 0x31 }, new byte[] { 0x9F, 0x8C, 0x00, 0x20, 0x46, 0x60 }),
            new Patch(1, 0x33AF7E, new byte[] { 0x95, 0x18 }, new byte[] { 0x97, 0x0C }), 
                                    };


        Patch[] patch_4 = {
            new Patch(1, 0xC0842, new byte[] { 0x81, 0xB0 }, new byte[] { 0x91, 0x40 }), // Jpeg Compression
                          };


        Patch[] patch_MEM = {
            new Patch(1, 0x1AA60E-0x40000, new byte[] { 
                    0x8F, 0xF0, 0x17, 0x81, 0x0F, 0x0B, 0x8B, 0x5B, 0x8B, 0x4A, 0xA8, 0x8A, 0xFC, 0x06, 0x8B, 0xB8, 0x9F, 0x8C, 0x00, 0x1A, 0xA7, 0x70, 0x9F, 0x0C, 0xC0, 0x04, 0x8B, 0xA4, 0xA4, 0xA4, 0x97, 0xB4, 0xB4, 0x64 }, 
                new byte[] { 0x8F, 0xF0, 0x17, 0x81, 0x0F, 0x0A, 0x8B, 0x4A, 0x9F, 0x89, 0x8F, 0x86, 0xB5, 0x28, 
                    0xA8, 0x8A,0xFC, 0x07, 0x8B, 0x58, 0xCF, 0xF4, 0x9F, 0x8C, 0x00, 0x1A, 0xA8, 0x98, 0x9F, 
                    0x0C, 0x97, 0x84, 0xC6, 0x20 }), // Write code mem 1/2

            new Patch(1, 0x1C4138-0x40000, new byte[] { 0xCF, 0x00 }, new byte[] { 0xC8, 0x00 }), // Write code mem 2/2
                          };

        Patch[] patch_Battery = {
            new Patch(0, 0x8C664, new byte[] { 0xF0, 0x00, 0x2A, 0x1C }, new byte[] { 0x65, 0x00, 0x65, 0x00 }), // non-brand  battery 6/7
            new Patch(0, 0x8C6B8, new byte[] { 0xF0, 0x00, 0x2A, 0x03 }, new byte[] { 0x65, 0x00, 0x65, 0x00 }), // non-brand  battery 2/7
            new Patch(0, 0x8C6FA, new byte[] { 0xF0, 0x00, 0x61, 0x05 }, new byte[] { 0x65, 0x00, 0x65, 0x00 }), // non-brand  battery 3/7
            new Patch(0, 0x8C72A, new byte[] { 0xF2, 0x00, 0x73, 0x00 }, new byte[] { 0xF3, 0x00, 0x73, 0x00 }), // non-brand  battery 4/7
            new Patch(0, 0x8C72E, new byte[] { 0xF0, 0x00, 0x61, 0x0B }, new byte[] { 0xF0, 0x00, 0x60, 0x0F }), // non-brand  battery 5/7
            new Patch(0, 0x8D13A, new byte[] { 0x6a, 0x00 }, new byte[] { 0x6a, 0x01 }), // Leegong battery 7/7
            //new Patch(0, 0x8D178, new byte[] { 0x6a, 0x00 }, new byte[] { 0x6a, 0x00 }), // non-brand battery 1/7
                         };


        Patch[] patch_LCD = {
            new Patch(0, 0x2F1E8, new byte[] { 0x1A, 0x1F, 0x02, 0x4E }, new byte[] { 0x65, 0x00, 0x65, 0x00 }), // allow LCD testing 1/2
            new Patch(0, 0x2F48E, new byte[] { 0x1A, 0x1F, 0x02, 0x4E }, new byte[] { 0x65, 0x00, 0x65, 0x00 }), // allow LCD testing 2/2

                         };

        Patch[] patch_clean_hdmi = {
              new Patch(1, 0x2FC134, new byte[] { 0xE2, 0x04 }, new byte[] { 0xE1, 0x04 }), // 1/2
              new Patch(1, 0x0DC53E, new byte[] { 0xE2, 0x04 }, new byte[] { 0xE1, 0x04 }), // 2/2
                                };


        Patch[] patch_stareater = {
              new Patch(1, 0xC9346, new byte[] { 0xED, 0x12 }, new byte[] { 0xE0, 0x12 }), // star eater 1/2
              new Patch(1, 0xC96BA, new byte[] { 0xED, 0x12 }, new byte[] { 0xE0, 0x12 }), // star eater 2/2
                                };


        Patch[] patch_5 = {
            new Patch(1, 0x265514, new byte[] { 0x53, 0x57, 0x33, 0x3A, 0x4F, 0x46, 0x46, 0x00, 0x53, 0x57, 0x33, 0x3A  }, new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }), // Live view manual
            new Patch(1, 0x265520, new byte[] { 0x4F, 0x4E, 0x20, 0x00, 0x53, 0x57, 0x32, 0x3A, 0x4F, 0x46, 0x46, 0x00, 0x53, 0x57, 0x32, 0x3A  }, new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }), // Live view manual
            new Patch(1, 0x265530, new byte[] { 0x4F, 0x4E, 0x20, 0x00, 0x53, 0x57, 0x31, 0x3A, 0x4F, 0x46, 0x46, 0x00, 0x53, 0x57, 0x31, 0x3A  }, new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }), // Live view manual
            new Patch(1, 0x265540, new byte[] { 0x4F, 0x4E }, new byte[] {0x00, 0x00 }), // Live view manual
            new Patch(0, 0x03739A, new byte[] { 0xA0, 0x62 }, new byte[] {0x6B, 0xFF }), // Live view manual - TX19A
                              };


        Patch[] patch_bitrate_36mbps = {
            new Patch(1, 0x074464, new byte[] { 0x01 } , new byte[] { 0x02 } ), // 1080 - 23.9fps
            new Patch(1, 0x07446A, new byte[] { 0x01 } , new byte[] { 0x02 } ),

            new Patch(1, 0x074490, new byte[] { 0x01 } , new byte[] { 0x02 } ), // 1080 - 29.9fps NTSC
            new Patch(1, 0x074496, new byte[] { 0x01 } , new byte[] { 0x02 } ),

            new Patch(1, 0x0744FC, new byte[] { 0x01 } , new byte[] { 0x02 } ), // 1080 - 25fps PAL
            new Patch(1, 0x074502, new byte[] { 0x01 } , new byte[] { 0x02 } ),
                             };

        Patch[] patch_bitrate_54mbps = {
            new Patch(1, 0x074464, new byte[] { 0x01 } , new byte[] { 0x03 } ), // 1080 - 23.9fps
            new Patch(1, 0x07446A, new byte[] { 0x01 } , new byte[] { 0x03 } ),

            new Patch(1, 0x074490, new byte[] { 0x01 } , new byte[] { 0x03 } ), // 1080 - 29.9fps NTSC
            new Patch(1, 0x074496, new byte[] { 0x01 } , new byte[] { 0x03 } ),

            new Patch(1, 0x0744FC, new byte[] { 0x01 } , new byte[] { 0x03 } ), // 1080 - 25fps PAL
            new Patch(1, 0x074502, new byte[] { 0x01 } , new byte[] { 0x03 } ),
                             };

        Patch[] patch_bitrate_36mbps_NQ = {
            new Patch(1, 0x74464, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x7446A, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x74478, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4F, 0xB1, 0x80 } ),
            new Patch(1, 0x7447E, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x12, 0xA8, 0x80 } ),

            new Patch(1, 0x74490, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x74496, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x744A4, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4F, 0xB1, 0x80 } ),
            new Patch(1, 0x744AA, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x12, 0xA8, 0x80 } ),

            new Patch(1, 0x744FC, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x74502, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x74510, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4F, 0xB1, 0x80 } ),
            new Patch(1, 0x74516, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x12, 0xA8, 0x80 } ),
                             };

        Patch[] patch_bitrate_54mbps_NQ = {
            new Patch(1, 0x74464, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x7446A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x74478, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4F, 0xB1, 0x80 } ),
            new Patch(1, 0x7447E, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x12, 0xA8, 0x80 } ),

            new Patch(1, 0x74490, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x74496, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x744A4, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4F, 0xB1, 0x80 } ),
            new Patch(1, 0x744AA, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x12, 0xA8, 0x80 } ),

            new Patch(1, 0x744FC, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x74502, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x74510, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4F, 0xB1, 0x80 } ),
            new Patch(1, 0x74516, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x12, 0xA8, 0x80 } ),
                             };

        public D5100_0101()
        {
            p = new Package();
            Model = "D5100";
            Version = "1.01";

            // Video
            Patches.Add(new PatchSet(PatchLevel.Released, "Remove Time Based Video Restrictions", patch_1));
            Patches.Add(new PatchSet(PatchLevel.Released, "Liveview Manual Control ISO/Shutter", patch_5));
            Patches.Add(new PatchSet(PatchLevel.Released, "Clean HDMI & LCD Liveview", patch_clean_hdmi));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 36mbps Bit-rate", patch_bitrate_36mbps, patch_bitrate_54mbps, patch_bitrate_36mbps_NQ, patch_bitrate_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 54mbps Bit-rate", patch_bitrate_54mbps, patch_bitrate_36mbps, patch_bitrate_36mbps_NQ, patch_bitrate_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 36mbps Bit-rate NQ old HQ", patch_bitrate_36mbps_NQ, patch_bitrate_36mbps, patch_bitrate_54mbps, patch_bitrate_54mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video 1080 HQ 54mbps Bit-rate NQ old HQ", patch_bitrate_54mbps_NQ, patch_bitrate_36mbps, patch_bitrate_54mbps, patch_bitrate_36mbps_NQ));

            // Still pictures
            Patches.Add(new PatchSet(PatchLevel.Released, "NEF Compression Off", patch_3_nocomp, patch_3_LosslessNEF));
            Patches.Add(new PatchSet(PatchLevel.Released, "NEF Compression Lossless", patch_3_LosslessNEF, patch_3_nocomp));
            Patches.Add(new PatchSet(PatchLevel.Released, "Disable Nikon Star Eater", patch_stareater)); 
            //Patches.Add(new PatchSet(true, "*FOR TESTERS* NEF Overscan", patch_NEF_Overscan));

            Patches.Add(new PatchSet(PatchLevel.Released, "Jpeg Compression - Quality (vs. Space)", patch_4));

            // Misc
            Patches.Add(new PatchSet(PatchLevel.Released, "Non-Brand Battery", patch_Battery));

            //Patches.Add(new PatchSet("Mem Write Test", patch_MEM));
            //Patches.Add(new PatchSet("LCD TEST", patch_LCD));
        }
    }



    class D5100_0102 : Firmware
    {
        Patch[] patch_1 = {
            new Patch(1, 0x74AD4, new byte[] { 0xE4, 0x03 }, new byte[] { 0xE0, 0x03 }), // unlimited recording 1/2
            new Patch(1, 0x755A8, new byte[] { 0xE2, 0x0B }, new byte[] { 0xE0, 0x0B }), // unlimited recording 2/2
                          };


        Patch[] patch_battery = {
            new Patch(0, 0x08B52A, new byte[] { 0xF0, 0x28, 0xA2, 0x52, 0xF0, 0x60 } , new byte[] { 0x6A, 0x20, 0x6A, 0x20, 0x6A, 0x20 } ),
            new Patch(0, 0x08C980, new byte[] { 0xF0, 0x28, 0xA4, 0x92 } , new byte[] { 0x6C, 0x20, 0x6C, 0x20 } ),
        };

        Patch[] patch_3_nocomp = {
            new Patch(1, 0xC29C0, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xC2E82, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xC5F3A, new byte[] { 0xE3, 0x02 }, new byte[] { 0xE1, 0x02 }), // NEF Compression
            new Patch(1, 0xD7AEE, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD7B74, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD7C06, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD7E72, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD7EF2, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD7F86, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD7FEC, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD8D48, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0xD8DCA, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x1159C6, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x115C2A, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x115C8C, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x1172B8, new byte[] { 0xE3, 0x02 }, new byte[] { 0xE1, 0x02 }), // NEF Compression
            new Patch(1, 0x144018, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x1466B8, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x154A04, new byte[] { 0xF2, 0x02 }, new byte[] { 0xF0, 0x02 }), // NEF Compression
            new Patch(1, 0x2E3010, new byte[] { 0xE2, 0x15 }, new byte[] { 0xE0, 0x15 }), // NEF Compression
       
            // Check 0x10
            new Patch(1, 0x115408, new byte[] { 0xE2, 0x05 }, new byte[] { 0xE1, 0x05 }), // NEF Compression
            new Patch(1, 0x115560, new byte[] { 0xE2, 0x04 }, new byte[] { 0xE1, 0x04 }), // NEF Compression
            new Patch(1, 0x13DC5E, new byte[] { 0xE2, 0x04 }, new byte[] { 0xE1, 0x04 }), // NEF Compression
            new Patch(1, 0x1A617C, new byte[] { 0xF2, 0x06 }, new byte[] { 0xF1, 0x06 }), // NEF Compression
             };

        Patch[] patch_3_LosslessNEF = {
            new Patch(1, 0x1C4660, new byte[] { 0x9F, 0x85, 0x8F, 0x9A, 0x85, 0x63 }, new byte[] { 0x9F, 0x8C, 0x00, 0x37, 0xAF, 0x1E }), 
            new Patch(1, 0x1C4666, new byte[] { 0x9F, 0x87, 0x8F, 0x9A, 0x85, 0x56 }, new byte[] { 0x97, 0x0C, 0x9F, 0xA0, 0x9F, 0xA0 }), 

            new Patch(1, 0x33AF1E, new byte[] { 0x17, 0x81, 0x0F, 0x01, 0x9f, 0x85}, new byte[] {0x9F, 0x80, 0x8F, 0x85, 0x15, 0xB1 }),
            new Patch(1, 0x33AF24, new byte[] { 0xBF, 0xFF, 0xFF, 0xFF, 0x8B, 0x46 }, new byte[] { 0x9F, 0x8C, 0x00, 0x20, 0x46, 0x6C }), 
            new Patch(1, 0x33AF2A, new byte[] { 0xCD, 0x1C }, new byte[] { 0x90, 0x80 }),
            new Patch(1, 0x33AF2C, new byte[] { 0xC1, 0xC4, 0x97, 0x8C, 0x1F, 0x40 }, new byte[] { 0x9F, 0x85, 0x8F, 0x9A, 0x85, 0x63 }),
            new Patch(1, 0x33AF32, new byte[] { 0x9F, 0xA0, 0x9F, 0x8C, 0x00, 0x31 }, new byte[] { 0x9F, 0x87, 0x8F, 0x9A, 0x85, 0x56 }),
            new Patch(1, 0x33AF38, new byte[] { 0x61, 0xB6 }, new byte[] { 0x97, 0x0C }), 
                                    };

        Patch[] patch_4 = {
            new Patch(1, 0xC0842, new byte[] { 0x81, 0xB0 }, new byte[] { 0x91, 0x40 }), // Jpeg Compression
                          };

        Patch[] patch_clean_hdmi = {
            new Patch(1, 0x2FC134, new byte[] { 0xE2, 0x04 }, new byte[] { 0xE1, 0x04 }), // 1/2
            new Patch(1, 0x0DC53E, new byte[] { 0xE2, 0x04 }, new byte[] { 0xE1, 0x04 }), // 2/2
                                };

        Patch[] patch_stareater = {
            new Patch(1, 0xC9346, new byte[] { 0xED, 0x12 }, new byte[] { 0xE0, 0x12 }), // star eater 1/2
            new Patch(1, 0xC96BA, new byte[] { 0xED, 0x12 }, new byte[] { 0xE0, 0x12 }), // star eater 2/2
                                };

        Patch[] patch_LiveViewManual = {
            new Patch(0, 0x03739A, new byte[] { 0xA0, 0x62 }, new byte[] { 0x6B, 0xFF }), 
            new Patch(1, 0x265514, new byte[] { 0x53, 0x57, 0x33, 0x3A, 0x4F, 0x46, 0x46, 0x00, 0x53, 0x57, 0x33, 0x3A }, new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }), 
            new Patch(1, 0X265520, new byte[] { 0x4F, 0x4E, 0x20, 0x00, 0x53, 0x57, 0x32, 0x3A, 0x4F, 0x46, 0x46, 0x00, 0x53, 0x57, 0x32, 0x3A }, new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }), 
            new Patch(1, 0x265530, new byte[] { 0x4F, 0x4E, 0x20, 0x00, 0x53, 0x57, 0x31, 0x3A, 0x4F, 0x46, 0x46, 0x00, 0x53, 0x57, 0x31, 0x3A }, new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }), 
            new Patch(1, 0x265540, new byte[] { 0x4F, 0x4E }, new byte[] { 0x00, 0x00 }), 
                               };

        Patch[] patch_bitrate_36mbps = {
            new Patch(1, 0x074464, new byte[] { 0x01 } , new byte[] { 0x02 } ), // 1080 - 23.9fps
            new Patch(1, 0x07446A, new byte[] { 0x01 } , new byte[] { 0x02 } ),

            new Patch(1, 0x074490, new byte[] { 0x01 } , new byte[] { 0x02 } ), // 1080 - 29.9fps NTSC
            new Patch(1, 0x074496, new byte[] { 0x01 } , new byte[] { 0x02 } ),

            new Patch(1, 0x0744FC, new byte[] { 0x01 } , new byte[] { 0x02 } ), // 1080 - 25fps PAL
            new Patch(1, 0x074502, new byte[] { 0x01 } , new byte[] { 0x02 } ),
                             };

        Patch[] patch_bitrate_54mbps = {
            new Patch(1, 0x074464, new byte[] { 0x01 } , new byte[] { 0x03 } ), // 1080 - 23.9fps
            new Patch(1, 0x07446A, new byte[] { 0x01 } , new byte[] { 0x03 } ),

            new Patch(1, 0x074490, new byte[] { 0x01 } , new byte[] { 0x03 } ), // 1080 - 29.9fps NTSC
            new Patch(1, 0x074496, new byte[] { 0x01 } , new byte[] { 0x03 } ),

            new Patch(1, 0x0744FC, new byte[] { 0x01 } , new byte[] { 0x03 } ), // 1080 - 25fps PAL
            new Patch(1, 0x074502, new byte[] { 0x01 } , new byte[] { 0x03 } ),
                             };

        Patch[] patch_bitrate_36mbps_NQ = {
            new Patch(1, 0x74464, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x7446A, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x74478, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4F, 0xB1, 0x80 } ),
            new Patch(1, 0x7447E, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x12, 0xA8, 0x80 } ),

            new Patch(1, 0x74490, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x74496, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x744A4, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4F, 0xB1, 0x80 } ),
            new Patch(1, 0x744AA, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x12, 0xA8, 0x80 } ),

            new Patch(1, 0x744FC, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x74502, new byte[] { 0x01 } , new byte[] { 0x02 } ),
            new Patch(1, 0x74510, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4F, 0xB1, 0x80 } ),
            new Patch(1, 0x74516, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x12, 0xA8, 0x80 } ),
                             };

        Patch[] patch_bitrate_54mbps_NQ = {
            new Patch(1, 0x74464, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x7446A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x74478, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4F, 0xB1, 0x80 } ),
            new Patch(1, 0x7447E, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x12, 0xA8, 0x80 } ),

            new Patch(1, 0x74490, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x74496, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x744A4, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4F, 0xB1, 0x80 } ),
            new Patch(1, 0x744AA, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x12, 0xA8, 0x80 } ),

            new Patch(1, 0x744FC, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x74502, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x74510, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4F, 0xB1, 0x80 } ),
            new Patch(1, 0x74516, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x12, 0xA8, 0x80 } ),
                             };

        Patch[] patch_bitrate_54mbps_29mbps_NQ = {
            new Patch(1, 0x74464, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x7446A, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x74478, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0xBA, 0x81, 0x40 } ),
            new Patch(1, 0x7447E, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x7D, 0x78, 0x40 } ),

            new Patch(1, 0x74490, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x74496, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x744A4, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0xBA, 0x81, 0x40 } ),
            new Patch(1, 0x744AA, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x7D, 0x78, 0x40 } ),

            new Patch(1, 0x744FC, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x74502, new byte[] { 0x01 } , new byte[] { 0x03 } ),
            new Patch(1, 0x74510, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0xBA, 0x81, 0x40 } ),
            new Patch(1, 0x74516, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x7D, 0x78, 0x40 } ),
                             };

        Patch[] patch_bitrate_29mbps_playback = {
new Patch(1, 0x074478, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x4C, 0x4B, 0x40 } ),
new Patch(1, 0x07447E, new byte[] { 0x00, 0x98, 0x96, 0x80 } , new byte[] { 0x01, 0x4C, 0x4B, 0x40 } ),
new Patch(1, 0x0744A4, new byte[] { 0x00, 0xB7, 0x1B, 0x00 } , new byte[] { 0x01, 0x98, 0x96, 0x80 } ),
new Patch(1, 0x0744AA, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x0744B8, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x0744BE, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x0744D8, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x0744DE, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x074510, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x074516, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x074556, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x07455C, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x074582, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x074588, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x074596, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x07459C, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x0745EE, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x0745F4, new byte[] { 0x00 } , new byte[] { 0x01 } ),
new Patch(1, 0x07467A, new byte[] { 0x00 } , new byte[] { 0x03 } ),
new Patch(1, 0x074680, new byte[] { 0x00 } , new byte[] { 0x03 } ),
new Patch(1, 0x0746D0, new byte[] { 0x00 } , new byte[] { 0x03 } ),
new Patch(1, 0x0746D6, new byte[] { 0x00 } , new byte[] { 0x03 } ),
new Patch(1, 0x586CC7, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x587BF8, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x587F2A, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x587F64, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x587FA7, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x587FDF, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x588017, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x588051, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5885B2, new byte[] { 0x20, 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x00, 0x00, 0x58, 0x2D, 0x48, 0x51, 0x00 } ),
new Patch(1, 0x5885E9, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x59C21D, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x59D19C, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x59D4E5, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x59D522, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x59D568, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x59D5A3, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x59D5DE, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },  new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x59D61B, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C }, new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5AE71F, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5AF4EF, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5AF7A8, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5AF7E4, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5AF829, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5AF863, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5AF89D, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5AF8D9, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5AFD81, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5AFDB9, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5BFF10, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5C10E7, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5C142E, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5C146D, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5C14BE, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5C14FB, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5C1538, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5C1577, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5C1B60, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5C1B9B, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5FC355, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C },new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5FD32F, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C,0x65 } , new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00, 0x00 } ),
new Patch(1, 0x5FD672, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C,0x65 } , new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00, 0x00 } ),
new Patch(1, 0x5FD6B0, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C,0x65 } , new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00, 0x00 } ),
new Patch(1, 0x5FD6FA, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C,0x65 } , new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00, 0x00 } ),
new Patch(1, 0x5FD736, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C,0x65 } , new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00, 0x00 } ),
new Patch(1, 0x5FD772, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C,0x65 } , new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00, 0x00 } ),
new Patch(1, 0x5FD7B0, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C,0x65 } , new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00, 0x00 } ),
new Patch(1, 0x5FDD2A, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C,0x65 } , new byte[] { 0x45, 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x5FDD64, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C,0x65 } , new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00, 0x00 } ),
new Patch(1, 0x66C210, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C } ,new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x66C647, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C } ,new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x66CD46, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C } ,new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x6CC21C, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C } ,new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x6CC5AD, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C } ,new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x6CCC30, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C } ,new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x7116DE, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C } ,new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x711A65, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C } ,new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
new Patch(1, 0x712069, new byte[] { 0x6E, 0x6F, 0x72, 0x6D, 0x61, 0x6C } ,new byte[] { 0x58, 0x2D, 0x48, 0x51, 0x00, 0x00 } ),
                             };
        public D5100_0102()
        {
            p = new Package();
            Model = "D5100";
            Version = "1.02";

            // Video

            Patches.Add(new PatchSet(PatchLevel.Released, "Remove Time Based Video Restrictions", patch_1));
            Patches.Add(new PatchSet(PatchLevel.Released, "Liveview Manual Control ISO/Shutter", patch_LiveViewManual));
            Patches.Add(new PatchSet(PatchLevel.Released, "Clean HDMI & LCD Liveview", patch_clean_hdmi));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 36mbps Bit-rate", patch_bitrate_36mbps, patch_bitrate_54mbps, patch_bitrate_36mbps_NQ, patch_bitrate_54mbps_NQ, patch_bitrate_29mbps_playback, patch_bitrate_54mbps_29mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 54mbps Bit-rate", patch_bitrate_54mbps, patch_bitrate_36mbps, patch_bitrate_36mbps_NQ, patch_bitrate_54mbps_NQ, patch_bitrate_29mbps_playback, patch_bitrate_54mbps_29mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 36mbps Bit-rate NQ old HQ", patch_bitrate_36mbps_NQ, patch_bitrate_36mbps, patch_bitrate_54mbps, patch_bitrate_54mbps_NQ, patch_bitrate_29mbps_playback, patch_bitrate_54mbps_29mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 HQ 54mbps Bit-rate NQ old HQ", patch_bitrate_54mbps_NQ, patch_bitrate_36mbps, patch_bitrate_54mbps, patch_bitrate_36mbps_NQ, patch_bitrate_29mbps_playback, patch_bitrate_54mbps_29mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Beta, "Video HQ 29Mbps with playback", patch_bitrate_29mbps_playback, patch_bitrate_54mbps_NQ, patch_bitrate_36mbps, patch_bitrate_54mbps, patch_bitrate_36mbps_NQ, patch_bitrate_54mbps_29mbps_NQ));
            Patches.Add(new PatchSet(PatchLevel.Alpha, "Video 1080 HQ 54Mbps, NQ 29Mbps", patch_bitrate_54mbps_29mbps_NQ, patch_bitrate_29mbps_playback, patch_bitrate_54mbps_NQ, patch_bitrate_36mbps, patch_bitrate_54mbps, patch_bitrate_36mbps_NQ));

            // Still pictures
            Patches.Add(new PatchSet(PatchLevel.Released, "NEF Compression Off", patch_3_nocomp, patch_3_LosslessNEF));
            Patches.Add(new PatchSet(PatchLevel.Released, "NEF Compression Lossless", patch_3_LosslessNEF, patch_3_nocomp));
            Patches.Add(new PatchSet(PatchLevel.Released, "Disable Nikon Star Eater", patch_stareater));

            Patches.Add(new PatchSet(PatchLevel.Released, "Jpeg Compression - Quality (vs. Space)", patch_4));

            Patches.Add(new PatchSet(PatchLevel.Released, "Non-Brand Battery", patch_battery));

        }
    }

}
