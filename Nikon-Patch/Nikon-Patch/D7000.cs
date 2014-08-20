using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Linq.Expressions;
using System.Diagnostics;
using System.Text;


namespace Nikon_Patch
{
    class D7000_0103 : Firmware
    {
        Patch[] patch_1 = {
            new Patch(1, 0x741FE, new byte[] { 0xE4, 0x03 }, new byte[] { 0xE0, 0x03 }), // unlimited recording 1/2
            new Patch(1, 0x74D28, new byte[] { 0xE2, 0x0B }, new byte[] { 0xE0, 0x0B }), // unlimited recording 2/2
                          };

        //Patch[] patch_2 = {
            //new Patch(1, 0xC8E9E, new byte[] { 0xE2, 0x44 }, new byte[] { 0xE1, 0x44 }), // LIVE VIEW rec 1/2
         //                  };


        Patch[] patch_stareater = {
              new Patch(1, 0xC882A, new byte[] { 0xED, 0x12 }, new byte[] { 0xE0, 0x12 }), // star eater 1/2
              new Patch(1, 0xC8A96, new byte[] { 0xED, 0x12 }, new byte[] { 0xE0, 0x12 }), // star eater 2/2
                                };

        Patch[] patch_clean_hdmi = {
              new Patch(1, 0x2dfbe2, new byte[] { 0xE2, 0x04 }, new byte[] { 0xE1, 0x04 }), // 1/2
              new Patch(1, 0xd8e90, new byte[] { 0xF2, 0x05 }, new byte[] { 0xF1, 0x05 }), // 2/2
                                };

        Patch[] patch_LV_no_timeout = {
              new Patch(1, 0x1952D6, new byte[] { 0xE2, 0x16 }, new byte[] { 0xE1, 0x16 }), // 1/3
              new Patch(1, 0x197DBC, new byte[] { 0xE2, 0x0F }, new byte[] { 0xE1, 0x0F }), // 1/3
              new Patch(1, 0x1980C4, new byte[] { 0xE3, 0x14 }, new byte[] { 0xE0, 0x14 }), // 1/3
                                      };

        Patch[] patch_Mov_1080_24_36mbps = {
              new Patch(1, 0x73b84, new byte[] {0x9F,0x81,0x01,0x4F,0xB1,0x80,0x9F,0x80,0x01,0x12,0xA8,0x80}, 
                                    new byte[] {0x9F,0x81,0x02,0x06,0xcc,0x80,0x9F,0x80,0x01,0xc9,0xc3,0x80}),
              new Patch(1, 0x73B98, new byte[] {0x9F,0x81,0x00,0xB7,0x1B,0x00,0x9F,0x80,0x00,0x98,0x96,0x80}, 
                                    new byte[] {0x9F,0x81,0x01,0x4F,0xB1,0x80,0x9F,0x80,0x01,0x12,0xA8,0x80}),
                                           };

        Patch[] patch_Mov_1080_24_49mbps = {
              new Patch(1, 0x73b84, new byte[] {0x9F,0x81,0x01,0x4F,0xB1,0x80,0x9F,0x80,0x01,0x12,0xA8,0x80}, 
                                    new byte[] {0x9F,0x81,0x02,0xeb,0xae,0x40,0x9F,0x80,0x02,0xae,0xa5,0x40}),
              new Patch(1, 0x73B98, new byte[] {0x9F,0x81,0x00,0xB7,0x1B,0x00,0x9F,0x80,0x00,0x98,0x96,0x80}, 
                                    new byte[] {0x9F,0x81,0x01,0x4F,0xB1,0x80,0x9F,0x80,0x01,0x12,0xA8,0x80}),
                                           };

        Patch[] patch_Mov_1080_24_64mbps = {
              new Patch(1, 0x73b84, new byte[] {0x9F,0x81,0x01,0x4F,0xB1,0x80,0x9F,0x80,0x01,0x12,0xA8,0x80}, 
                                    new byte[] {0x9F,0x81,0x03,0xd0,0x90,0x00,0x9F,0x80,0x03,0x93,0x87,0x00}),
              new Patch(1, 0x73B98, new byte[] {0x9F,0x81,0x00,0xB7,0x1B,0x00,0x9F,0x80,0x00,0x98,0x96,0x80}, 
                                    new byte[] {0x9F,0x81,0x01,0x4F,0xB1,0x80,0x9F,0x80,0x01,0x12,0xA8,0x80}),
                                           };

        Patch[] patch_Mov_1080_24_XXmbps = {
              new Patch(1, 0x73B86, new byte[] {0x01}, new byte[] {0x07}),
              new Patch(1, 0x73B8C, new byte[] {0x01}, new byte[] {0x07}),
              new Patch(1, 0x73B9A, new byte[] {0x00}, new byte[] {0x05}),
              new Patch(1, 0x73BA0, new byte[] {0x00}, new byte[] {0x05}),
                                           };

        Patch[] patch_Mov_1080_60fps = {
                // patch movBitrateCalculate 1080 jump table.
              new Patch(1, 0x9A348, new byte[] {0x00,0x0b,0x3E,0x36,0x00,0x0b,0x3E,0x36}, 
                  new byte[] {0x00,0x0b,0x3B,0x80,0x00,0x0b,0x3B,0x80}),
              new Patch(1, 0x9A358, new byte[] {0x00,0x0b,0x3E,0x36,0x00,0x0b,0x3E,0x36}, 
                  new byte[] {0x00,0x0b,0x3B,0x80,0x00,0x0b,0x3B,0x80}),

              //// patch 0BEA06 50/60 fps check (464 only originally)
              new Patch(1, 0x7EA06, new byte[] {0xF2,0x2C}, new byte[] {0xF0,0x2C}),

              //// patch 0BEA06 30/25 fps check (720, 464 only originally)
              new Patch(1, 0x7EA1C, new byte[] {0xF2,0x21}, new byte[] {0xF0,0x21}),

              // patch movSettings_Check_ResVsFps 1080 res/fps check
              new Patch(1, 0x83E4C, new byte[] {0xE2,0x31}, new byte[] {0xE0,0x31}),

              // patch setup 0 -> 1 23.9 -> 59.9
              //new Patch(1, 0x83588, new byte[] {0xC0,0x00}, new byte[] {0xC0,0x10}),
              

              // patch setup 0 -> 6 23.9 -> 25
              new Patch(1, 0x83588, new byte[] {0xC0,0x00}, new byte[] {0xC0,0x60}),
              new Patch(1, 0x11a7fa, new byte[] {0xC0,0x00}, new byte[] {0xC0,0x60}),
                                           };

        Patch[] patch_Mov_720_60fps = {
                // patch movBitrateCalculate 720 jump table.
              new Patch(1, 0x9A364, new byte[] {0x00,0x0b,0x3E,0x36}, new byte[] {0x00, 0x0B, 0x3C, 0x06}),
              new Patch(1, 0x9A374, new byte[] {0x00,0x0b,0x3E,0x36}, new byte[] {0x00, 0x0B, 0x3C, 0x06}),

              //// patch 0BEA06 50/60 fps check (464 only originally)
              new Patch(1, 0x7EA06, new byte[] {0xF2,0x2C}, new byte[] {0xF0,0x2C}),

              //// patch 0BEA06 30/25 fps check (720, 464 only originally)
              new Patch(1, 0x7EA1C, new byte[] {0xF2,0x21}, new byte[] {0xF0,0x21}),

              // patch movSettings_Check_ResVsFps all res/fps ok
              new Patch(1, 0x83e14, new byte[] {0x04, 0x40, 0xA8, 0x00}, new byte[] {0xC0, 0x04, 0x97, 0x20}),
              //new Patch(1, 0x83e8e, new byte[] {0xCF,0xF4}, new byte[] {0xC0, 0x04}),

              // patch setup 0 -> 1 23.9 -> 59.9
              //new Patch(1, 0x83588, new byte[] {0xC0,0x00}, new byte[] {0xC0,0x10}),
              
              // patch setup 0 -> 6 23.9 -> 25
              new Patch(1, 0x83588, new byte[] {0xC0,0x00}, new byte[] {0xC0,0x60}),
              new Patch(1, 0x11a7f4, new byte[] {0xC0,0x00}, new byte[] {0xC0,0x60}),
               
              // patch setup 2 -> 1 30 -> 60
              new Patch(1, 0x8358C, new byte[] {0xC0,0x20}, new byte[] {0xC0,0x10}),
              new Patch(1, 0x11a7fC, new byte[] {0xC0,0x20}, new byte[] {0xC0,0x10}),
        };

        Patch[] patch_Mov_720_30fps_A = {
            // swap 720 24 NQ for 720 30 NQ
            new Patch(1, 0x2A89D2, new byte[] {0xC1,0x84}, new byte[] {0xC1,0xE4}),
        };

        Patch[] patch_Mov_720_30fps_B = {
            // swap setMovieGlobalSettings 24 for 30
            new Patch(1, 0xd2198, new byte[] {0xC0,0x00}, new byte[] {0xC0,0x20}),
        };


        Patch[] patch_Mov_720_30fps_C = {
            // swap setMovieGlobalSettings 24 for 30
            new Patch(1, 0xd2198, new byte[] {0xC0,0x00}, new byte[] {0xC0,0x20}),
            // swap 10F5CE GetVideoFpsIdx 0 for 2 (24 -> 30)
            new Patch(1, 0xCF5E8, new byte[] {0xC0,0x04}, new byte[] {0xC0,0x24}),
       };


       Patch[] patch_Mov_720_30fps_D = {
            // swap setMovieGlobalSettings 24 for 30
            new Patch(1, 0xd2198, new byte[] {0xC0,0x00}, new byte[] {0xC0,0x20}),
            // swap 10F5CE GetVideoFpsIdx 0 for 2 (24 -> 30)
            new Patch(1, 0xCF5E8, new byte[] {0xC0,0x04}, new byte[] {0xC0,0x34}),
       };

       Patch[] patch_Mov_720_30fps_E = {
            // patch movBitrateCalculate 720 jump table.
            new Patch(1, 0x9A364, new byte[] {0x00,0x0b,0x3E,0x36}, new byte[] {0x00, 0x0B, 0x3C, 0x06}),
            new Patch(1, 0x9A374, new byte[] {0x00,0x0b,0x3E,0x36}, new byte[] {0x00, 0x0B, 0x3C, 0x06}),

            //// patch 0BEA06 50/60 fps check (464 only originally)
            new Patch(1, 0x7EA06, new byte[] {0xF2,0x2C}, new byte[] {0xF0,0x2C}),

            //// patch 0BEA06 30/25 fps check (720, 464 only originally)
            new Patch(1, 0x7EA1C, new byte[] {0xF2,0x21}, new byte[] {0xF0,0x21}),

            // patch movSettings_Check_ResVsFps all res/fps ok
            new Patch(1, 0x83e14, new byte[] {0x04, 0x40, 0xA8, 0x00}, new byte[] {0xC0, 0x04, 0x97, 0x20}),

            // patch setup 2 -> 1 30 -> 60
            new Patch(1, 0x8358C, new byte[] {0xC0,0x20}, new byte[] {0xC0,0x10}),
            new Patch(1, 0x11a7fC, new byte[] {0xC0,0x20}, new byte[] {0xC0,0x10}),

            // swap 10F5CE GetVideoFpsIdx 2 for 3 (30 -> ??)
            new Patch(1, 0xCF5EC, new byte[] {0xC0,0x24}, new byte[] {0xC0,0x34}),
        // GetVideoFpsIdx to 3 made 60fps time out...
        };

       Patch[] patch_Mov_424_60fps = {
            // patch setup 2 -> 1 30 -> 60
            new Patch(1, 0x8358C, new byte[] {0xC0,0x20}, new byte[] {0xC0,0x10}),
            new Patch(1, 0x11a7fC, new byte[] {0xC0,0x20}, new byte[] {0xC0,0x10}),

            // try alter 28BC5C fps_idx_1_3_4_5:.byte 1,3,4,5
            //new Patch(1, 0x24BC5C, new byte[] {1,3,4,5}, new byte[] {1,3,2,5}),
        };

       Patch[] patch_Mov_1080_30fps = {
            // patch setup 720 30 NQ -> 1080 30 HQ
            new Patch(1, 0x2a8928, new byte[] {0xC0,0x14}, new byte[] {0xC0,0x04}),
            new Patch(1, 0x2a893C, new byte[] {0xC0,0x14}, new byte[] {0xC0,0x04}),

            //// patch 0BEA06 30/25 fps check (720, 464 only originally)
            new Patch(1, 0x7EA1C, new byte[] {0xF2,0x21}, new byte[] {0xF0,0x21}),

            // patch movSettings_Check_ResVsFps all res/fps ok
            new Patch(1, 0x83e14, new byte[] {0x04, 0x40, 0xA8, 0x00}, new byte[] {0xC0, 0x04, 0x97, 0x20}),

        };

       Patch[] patch_dark_current = {
            // Disable dark current
            new Patch(1, 0x21E44A, new byte[] {0x00, 0x9C, 0x00, 0x01, 0x37, 0x00}, new byte[] {0x00, 0x9C, 0x00, 0x00, 0x00, 0x00}),

            // Lower Sensor Bias Level to allow better JPEG preview
            new Patch(1, 0x21C821, new byte[] {0x58, 0x02}, new byte[] {0x80, 0x00}),

            // lastly, make change for start up code so that it loads these fixed table into ram instead of calibrated data in flash, essentially the key to success
            new Patch(1, 0x199E17, new byte[] {0x90, 0x00, 0x00 }, new byte[] {0x25, 0xC2, 0xD4}),
        };

        public D7000_0103()
        {
            p = new Package();
            Model = "D7000";
            Version = "1.03";

            Patches.Add(new PatchSet(PatchLevel.Released, "Remove Time Based Video Restrictions", patch_1));
            //Patches.Add(new PatchSet(false, "Test Live View Record", patch_2));
            Patches.Add(new PatchSet(PatchLevel.Released, "Disable Nikon Star Eater", patch_stareater));
            Patches.Add(new PatchSet(PatchLevel.Beta, "True Dark Current", patch_dark_current));
            Patches.Add(new PatchSet(PatchLevel.Released, "Clean HDMI & LCD Liveview", patch_clean_hdmi));
            Patches.Add(new PatchSet(PatchLevel.Released, "Liveview No Display Auto Off", patch_LV_no_timeout));
            //Patches.Add(new PatchSet(false, "Video 1080 59.9fps HQ", patch_Mov_1080_60fps));
            //Patches.Add(new PatchSet(PatchLevel.DevOnly, "Video 720 59.9fps HQ", patch_Mov_720_60fps));

            //Patches.Add(new PatchSet(true, "Video 424 60fps", patch_Mov_424_60fps));
            //Patches.Add(new PatchSet(true, "Video 1080 30fps HQ (select 720 30 NQ)", patch_Mov_1080_30fps));

            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 24fps HQ 36mbps NQ old HQ", patch_Mov_1080_24_36mbps, patch_Mov_1080_24_49mbps, patch_Mov_1080_24_64mbps));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 24fps HQ 49mbps NQ old HQ", patch_Mov_1080_24_49mbps, patch_Mov_1080_24_36mbps, patch_Mov_1080_24_64mbps));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 24fps HQ 64mbps NQ old HQ", patch_Mov_1080_24_64mbps, patch_Mov_1080_24_36mbps, patch_Mov_1080_24_49mbps));
          //  Patches.Add(new PatchSet(true, "Video 1080 24fps 122/89 mbps", patch_Mov_1080_24_XXmbps, patch_Mov_1080_24_36mbps, patch_Mov_1080_24_49mbps, patch_Mov_1080_24_64mbps));

        }
    }


    class D7000_0104 : Firmware
    {
        Patch[] patch_1 = {
            new Patch(1, 0x741FE, new byte[] { 0xE4, 0x03 }, new byte[] { 0xE0, 0x03 }), // unlimited recording 1/2
            new Patch(1, 0x74D28, new byte[] { 0xE2, 0x0B }, new byte[] { 0xE0, 0x0B }), // unlimited recording 2/2
                          };

        Patch[] patch_stareater = {
              new Patch(1, 0xC882A, new byte[] { 0xED, 0x12 }, new byte[] { 0xE0, 0x12 }), // star eater 1/2
              new Patch(1, 0xC8A96, new byte[] { 0xED, 0x12 }, new byte[] { 0xE0, 0x12 }), // star eater 2/2
                                };

        Patch[] patch_Mov_1080_24_36mbps = {
              new Patch(1, 0x73b84, new byte[] {0x9F,0x81,0x01,0x4F,0xB1,0x80,0x9F,0x80,0x01,0x12,0xA8,0x80}, 
                                    new byte[] {0x9F,0x81,0x02,0x06,0xcc,0x80,0x9F,0x80,0x01,0xc9,0xc3,0x80}),
              new Patch(1, 0x73B98, new byte[] {0x9F,0x81,0x00,0xB7,0x1B,0x00,0x9F,0x80,0x00,0x98,0x96,0x80}, 
                                    new byte[] {0x9F,0x81,0x01,0x4F,0xB1,0x80,0x9F,0x80,0x01,0x12,0xA8,0x80}),
                                           };

        Patch[] patch_Mov_1080_24_49mbps = {
              new Patch(1, 0x73b84, new byte[] {0x9F,0x81,0x01,0x4F,0xB1,0x80,0x9F,0x80,0x01,0x12,0xA8,0x80}, 
                                    new byte[] {0x9F,0x81,0x02,0xeb,0xae,0x40,0x9F,0x80,0x02,0xae,0xa5,0x40}),
              new Patch(1, 0x73B98, new byte[] {0x9F,0x81,0x00,0xB7,0x1B,0x00,0x9F,0x80,0x00,0x98,0x96,0x80}, 
                                    new byte[] {0x9F,0x81,0x01,0x4F,0xB1,0x80,0x9F,0x80,0x01,0x12,0xA8,0x80}),
                                           };

        Patch[] patch_Mov_1080_24_64mbps = {
              new Patch(1, 0x73b84, new byte[] {0x9F,0x81,0x01,0x4F,0xB1,0x80,0x9F,0x80,0x01,0x12,0xA8,0x80}, 
                                    new byte[] {0x9F,0x81,0x03,0xd0,0x90,0x00,0x9F,0x80,0x03,0x93,0x87,0x00}),
              new Patch(1, 0x73B98, new byte[] {0x9F,0x81,0x00,0xB7,0x1B,0x00,0x9F,0x80,0x00,0x98,0x96,0x80}, 
                                    new byte[] {0x9F,0x81,0x01,0x4F,0xB1,0x80,0x9F,0x80,0x01,0x12,0xA8,0x80}),
                                           };

        public D7000_0104()
        {
            p = new Package();
            Model = "D7000";
            Version = "1.04";

            Patches.Add(new PatchSet(PatchLevel.Released, "Remove Time Based Video Restrictions", patch_1));
            Patches.Add(new PatchSet(PatchLevel.Released, "Disable Nikon Star Eater", patch_stareater));

            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 24fps HQ 36mbps NQ old HQ", patch_Mov_1080_24_36mbps, patch_Mov_1080_24_49mbps, patch_Mov_1080_24_64mbps));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 24fps HQ 49mbps NQ old HQ", patch_Mov_1080_24_49mbps, patch_Mov_1080_24_36mbps, patch_Mov_1080_24_64mbps));
            Patches.Add(new PatchSet(PatchLevel.Released, "Video 1080 24fps HQ 64mbps NQ old HQ", patch_Mov_1080_24_64mbps, patch_Mov_1080_24_36mbps, patch_Mov_1080_24_49mbps));

        }
    }

}
