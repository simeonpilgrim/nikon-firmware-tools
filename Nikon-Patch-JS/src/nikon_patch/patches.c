struct Patch D3100_0101_patches[] = {
    {.id = 1, .level = Released, .name="Change Time Based Video Restrictions to 17:28", .blocks={}}
    ,{.id = 2, .level = Released, .name="Non-brand Battery", .blocks={}}
    ,{.id = 3, .level = Beta, .name="ISO/Shutter Lv Patch", .blocks={}}
};

struct Patch D3100_0102_patches[] = {
    {.id = 1, .level = Released, .name="Change Time Based Video Restrictions to 17:28", .blocks={}}
    ,{.id = 2, .level = Released, .name="Non-brand Battery", .blocks={}}
    ,{.id = 3, .level = Released, .name="ISO/Shutter Lv Patch", .blocks={}}
    ,{.id = 4, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate", .blocks={5,7,6}}
    ,{.id = 5, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate", .blocks={4,7,6}}
    ,{.id = 6, .level = Beta, .name="Video 1080 HQ 64mbps Bit-rate", .blocks={5,4,7}}
    ,{.id = 7, .level = Released, .name="Video HQ 29Mbps with playback", .blocks={4,5,6}}
};

struct Patch D3200_0101_patches[] = {
    {.id = 1, .level = Released, .name="Non-brand Battery", .blocks={}}
    ,{.id = 2, .level = Released, .name="Multi-Language Support", .blocks={}}
    ,{.id = 3, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate", .blocks={4,5,6}}
    ,{.id = 4, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate", .blocks={3,5,6}}
    ,{.id = 5, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate NQ old HQ", .blocks={4,3,6}}
    ,{.id = 6, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate NQ old HQ", .blocks={4,3,5}}
};

struct Patch D3200_0102_patches[] = {
    {.id = 1, .level = Released, .name="Non-Brand Battery", .blocks={}}
    ,{.id = 2, .level = Released, .name="Multi-Language Support", .blocks={}}
    ,{.id = 3, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate", .blocks={4,5,6}}
    ,{.id = 4, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate", .blocks={3,5,6}}
    ,{.id = 5, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate NQ old HQ", .blocks={4,3,6}}
    ,{.id = 6, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate NQ old HQ", .blocks={4,3,5}}
};

struct Patch D3200_0103_patches[] = {
    {.id = 1, .level = Released, .name="Multi-Language Support", .blocks={}}
    ,{.id = 2, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate", .blocks={3,4,5,6}}
    ,{.id = 3, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate", .blocks={2,4,5,6}}
    ,{.id = 4, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate NQ old HQ", .blocks={3,2,5,6}}
    ,{.id = 5, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate NQ old HQ", .blocks={3,2,4,6}}
    ,{.id = 6, .level = Beta, .name="Video 1080 HQ 64mbps Bit-rate NQ old HQ", .blocks={3,2,4,5}}
};

struct Patch D5100_0101_patches[] = {
    {.id = 1, .level = Released, .name="Remove Time Based Video Restrictions", .blocks={}}
    ,{.id = 2, .level = Released, .name="Liveview Manual Control ISO/Shutter", .blocks={}}
    ,{.id = 3, .level = Released, .name="Clean HDMI & LCD Liveview", .blocks={}}
    ,{.id = 4, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate", .blocks={5,6,7}}
    ,{.id = 5, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate", .blocks={4,6,7}}
    ,{.id = 6, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate NQ old HQ", .blocks={4,5,7}}
    ,{.id = 7, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate NQ old HQ", .blocks={4,5,6}}
    ,{.id = 8, .level = Released, .name="NEF Compression Off", .blocks={9}}
    ,{.id = 9, .level = Released, .name="NEF Compression Lossless", .blocks={8}}
    ,{.id = 10, .level = Released, .name="Disable Nikon Star Eater", .blocks={}}
    ,{.id = 11, .level = Beta, .name="True Dark Current", .blocks={12}}
    ,{.id = 12, .level = Beta, .name="True Dark Current - Menu based", .blocks={11}}
    ,{.id = 13, .level = Released, .name="Jpeg Compression - Quality (vs. Space)", .blocks={}}
    ,{.id = 14, .level = Released, .name="Non-Brand Battery", .blocks={}}
};

struct Patch D5100_0102_patches[] = {
    {.id = 1, .level = Released, .name="Remove Time Based Video Restrictions", .blocks={}}
    ,{.id = 2, .level = Released, .name="Liveview Manual Control ISO/Shutter", .blocks={}}
    ,{.id = 3, .level = Released, .name="Clean HDMI & LCD Liveview", .blocks={}}
    ,{.id = 4, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate", .blocks={5,6,7,8,9,10}}
    ,{.id = 5, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate", .blocks={4,6,7,8,9,10}}
    ,{.id = 6, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate NQ old HQ", .blocks={4,5,7,8,9,10}}
    ,{.id = 7, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate NQ old HQ", .blocks={4,5,6,8,9,10}}
    ,{.id = 8, .level = Released, .name="Video HQ 29Mbps with playback", .blocks={7,4,5,6,9,10}}
    ,{.id = 9, .level = Released, .name="Video 1080 HQ 54Mbps, NQ 29Mbps", .blocks={8,7,4,5,6,10}}
    ,{.id = 10, .level = Released, .name="Video 1080 HQ 64Mbps, NQ 22Mbps", .blocks={9,8,7,4,5,6}}
    ,{.id = 11, .level = Beta, .name="HDMI Output 1080i FullScreen Fixed", .blocks={12}}
    ,{.id = 12, .level = Beta, .name="HDMI Output 720p FullScreen", .blocks={11}}
    ,{.id = 13, .level = Released, .name="NEF Compression Off", .blocks={14}}
    ,{.id = 14, .level = Released, .name="NEF Compression Lossless", .blocks={13}}
    ,{.id = 15, .level = Released, .name="Disable Nikon Star Eater", .blocks={}}
    ,{.id = 16, .level = Alpha, .name="True Dark Current - Menu based", .blocks={}}
    ,{.id = 17, .level = Released, .name="Jpeg Compression - Quality (vs. Space)", .blocks={}}
    ,{.id = 18, .level = Released, .name="Non-Brand Battery", .blocks={}}
};

struct Patch D5200_0101_patches[] = {
    {.id = 1, .level = Released, .name="Video HQ 40mbps Bit-rate", .blocks={}}
};

struct Patch D5200_0102_patches[] = {
    {.id = 1, .level = Released, .name="Video HQ 40mbps Bit-rate", .blocks={2,3,4}}
    ,{.id = 2, .level = Beta, .name="Video HQ 64mbps Bit-rate", .blocks={1,3,4}}
    ,{.id = 3, .level = DevOnly, .name="Video HQ 8mbps Bit-rate", .blocks={1,2,4}}
    ,{.id = 4, .level = DevOnly, .name="Video HQ 1mbps Bit-rate", .blocks={1,2,3}}
    ,{.id = 5, .level = Beta, .name="Liveview (15min) No Timeout", .blocks={}}
};

struct Patch D7000_0103_patches[] = {
    {.id = 1, .level = Beta, .name="HDMI Output 720p FullScreen", .blocks={}}
    ,{.id = 2, .level = Released, .name="Remove Time Based Video Restrictions", .blocks={}}
    ,{.id = 3, .level = Released, .name="Disable Nikon Star Eater", .blocks={}}
    ,{.id = 4, .level = Beta, .name="True Dark Current", .blocks={7}}
    ,{.id = 5, .level = Released, .name="Clean HDMI & LCD Liveview", .blocks={}}
    ,{.id = 6, .level = Released, .name="Liveview No Display Auto Off", .blocks={}}
    ,{.id = 7, .level = Beta, .name="True Dark Current - Menu based", .blocks={4}}
    ,{.id = 8, .level = Beta, .name="Variable Frame Rate <= 30 fps", .blocks={}}
    ,{.id = 9, .level = Released, .name="Video 1080 24fps HQ 36mbps NQ old HQ", .blocks={10,11}}
    ,{.id = 10, .level = Released, .name="Video 1080 24fps HQ 49mbps NQ old HQ", .blocks={9,11}}
    ,{.id = 11, .level = Released, .name="Video 1080 24fps HQ 64mbps NQ old HQ", .blocks={9,10}}
};

struct Patch D7000_0104_patches[] = {
    {.id = 1, .level = Released, .name="Remove Time Based Video Restrictions", .blocks={}}
    ,{.id = 2, .level = Released, .name="Disable Nikon Star Eater", .blocks={}}
    ,{.id = 3, .level = Released, .name="Video 1080 24fps HQ 36mbps NQ old HQ", .blocks={4,5}}
    ,{.id = 4, .level = Released, .name="Video 1080 24fps HQ 49mbps NQ old HQ", .blocks={3,5}}
    ,{.id = 5, .level = Released, .name="Video 1080 24fps HQ 64mbps NQ old HQ", .blocks={3,4}}
};

struct Patch D7100_0101_patches[] = {
};

struct Patch D7100_0102_patches[] = {
};

struct Patch D300s_0101_patches[] = {
};

struct Patch D300s_0102_patches[] = {
    {.id = 1, .level = Released, .name="Disable Nikon Star Eater", .blocks={}}
};

struct Patch D300_0111_B_patches[] = {
    {.id = 1, .level = Released, .name="Disable Nikon Star Eater", .blocks={}}
};

struct Patch D600_0101_patches[] = {
    {.id = 1, .level = Released, .name="Multi-Language Support", .blocks={}}
    ,{.id = 2, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate", .blocks={3,4,5,6}}
    ,{.id = 3, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate", .blocks={2,4,5,6}}
    ,{.id = 4, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate NQ old HQ", .blocks={2,3,5,6}}
    ,{.id = 5, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate NQ old HQ", .blocks={2,3,4,6}}
    ,{.id = 6, .level = Released, .name="Video 1080 HQ 64mbps Bit-rate NQ old HQ", .blocks={2,3,4,5}}
    ,{.id = 7, .level = Beta, .name="True Dark Current - Menu based", .blocks={}}
};

struct Patch D600_0102_patches[] = {
};

struct Patch D610_0101_patches[] = {
    {.id = 1, .level = Released, .name="Multi-Language Support", .blocks={}}
    ,{.id = 2, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate NQ old HQ", .blocks={3,4}}
    ,{.id = 3, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate NQ old HQ", .blocks={2,4}}
    ,{.id = 4, .level = Released, .name="Video 1080/720 HQ 64mbps Bit-rate NQ old HQ", .blocks={2,3}}
    ,{.id = 5, .level = Beta, .name="True Dark Current - Menu based", .blocks={}}
};

struct Patch D750_0101_patches[] = {
};

struct Patch D800_0101_patches[] = {
};

struct Patch D800_0102_patches[] = {
    {.id = 1, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate NQ old HQ", .blocks={2,3}}
    ,{.id = 2, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate NQ old HQ", .blocks={1,3}}
    ,{.id = 3, .level = Released, .name="Video 1080 HQ 64mbps Bit-rate NQ old HQ", .blocks={1,2}}
};

struct Patch D800_0110_patches[] = {
    {.id = 1, .level = Released, .name="Multi-Language Support", .blocks={}}
    ,{.id = 2, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate NQ old HQ", .blocks={3,4,5}}
    ,{.id = 3, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate NQ old HQ", .blocks={2,4,5}}
    ,{.id = 4, .level = Released, .name="Video 1080 HQ 64mbps Bit-rate NQ old HQ", .blocks={2,3,5}}
    ,{.id = 5, .level = Released, .name="Video 1080 HQ 64mbps, NQ 36mbps", .blocks={4,2,3}}
    ,{.id = 6, .level = Released, .name="True Dark Current - Menu based", .blocks={}}
};

struct Patch D800E_0101_patches[] = {
};

struct Patch D800E_0102_patches[] = {
    {.id = 1, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate NQ old HQ", .blocks={2,3}}
    ,{.id = 2, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate NQ old HQ", .blocks={1,3}}
    ,{.id = 3, .level = Released, .name="Video 1080 HQ 64mbps Bit-rate NQ old HQ", .blocks={1,2}}
};

struct Patch D800E_0110_patches[] = {
    {.id = 1, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate NQ old HQ", .blocks={2,3}}
    ,{.id = 2, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate NQ old HQ", .blocks={1,3}}
    ,{.id = 3, .level = Released, .name="Video 1080 HQ 64mbps Bit-rate NQ old HQ", .blocks={1,2}}
    ,{.id = 4, .level = Beta, .name="True Dark Current - Menu based", .blocks={}}
};

struct Patch D810_0102_patches[] = {
};

struct Patch D4_0105_patches[] = {
    {.id = 1, .level = Released, .name="Video 1080 HQ 36mbps Bit-rate", .blocks={2,3,4}}
    ,{.id = 2, .level = Released, .name="Video 1080 HQ 54mbps Bit-rate", .blocks={1,3,4}}
    ,{.id = 3, .level = Beta, .name="Video 1080 HQ 36mbps Bit-rate NQ old HQ", .blocks={1,2,4}}
    ,{.id = 4, .level = Beta, .name="Video 1080 HQ 54mbps Bit-rate NQ old HQ", .blocks={1,2,3}}
};

struct Patch D4_0110_patches[] = {
};

struct Patch D4S_0101_patches[] = {
};


struct PatchSet D3100_0101_ps = PATCHSET("D3100", "1.01", D3100_0101_patches);
struct PatchSet D3100_0102_ps = PATCHSET("D3100", "1.02", D3100_0102_patches);
struct PatchSet D3200_0101_ps = PATCHSET("D3200", "1.01", D3200_0101_patches);
struct PatchSet D3200_0102_ps = PATCHSET("D3200", "1.02", D3200_0102_patches);
struct PatchSet D3200_0103_ps = PATCHSET("D3200", "1.03", D3200_0103_patches);
struct PatchSet D5100_0101_ps = PATCHSET("D5100", "1.01", D5100_0101_patches);
struct PatchSet D5100_0102_ps = PATCHSET("D5100", "1.02", D5100_0102_patches);
struct PatchSet D5200_0101_ps = PATCHSET("D5200", "1.01", D5200_0101_patches);
struct PatchSet D5200_0102_ps = PATCHSET("D5200", "1.02", D5200_0102_patches);
struct PatchSet D7000_0103_ps = PATCHSET("D7000", "1.03", D7000_0103_patches);
struct PatchSet D7000_0104_ps = PATCHSET("D7000", "1.04", D7000_0104_patches);
struct PatchSet D7100_0101_ps = PATCHSET("D7100", "1.01", D7100_0101_patches);
struct PatchSet D7100_0102_ps = PATCHSET("D7100", "1.02", D7100_0102_patches);
struct PatchSet D300s_0101_ps = PATCHSET("D300s", "1.01", D300s_0101_patches);
struct PatchSet D300s_0102_ps = PATCHSET("D300s", "1.02", D300s_0102_patches);
struct PatchSet D300_0111_B_ps = PATCHSET("D300", "1.11 B File", D300_0111_B_patches);
struct PatchSet D600_0101_ps = PATCHSET("D600", "1.01", D600_0101_patches);
struct PatchSet D600_0102_ps = PATCHSET("D600", "1.02", D600_0102_patches);
struct PatchSet D610_0101_ps = PATCHSET("D610", "1.01", D610_0101_patches);
struct PatchSet D750_0101_ps = PATCHSET("D750", "1.01", D750_0101_patches);
struct PatchSet D800_0101_ps = PATCHSET("D800", "1.01", D800_0101_patches);
struct PatchSet D800_0102_ps = PATCHSET("D800", "1.02", D800_0102_patches);
struct PatchSet D800_0110_ps = PATCHSET("D800", "1.10", D800_0110_patches);
struct PatchSet D800E_0101_ps = PATCHSET("D800E", "1.01", D800E_0101_patches);
struct PatchSet D800E_0102_ps = PATCHSET("D800E", "1.02", D800E_0102_patches);
struct PatchSet D800E_0110_ps = PATCHSET("D800E", "1.10", D800E_0110_patches);
struct PatchSet D810_0102_ps = PATCHSET("D810", "1.02", D810_0102_patches);
struct PatchSet D4_0105_ps = PATCHSET("D4", "1.05", D4_0105_patches);
struct PatchSet D4_0110_ps = PATCHSET("D4", "1.10", D4_0110_patches);
struct PatchSet D4S_0101_ps = PATCHSET("D4S", "1.01", D4S_0101_patches);

struct PatchMap patches[] = {
     {.id = 1, .hash = {0x16,0xA0,0x50,0x50,0xCA,0xB2,0x60,0x2F,0x99,0x63,0x36,0xAC,0x86,0x9A,0xD8,0xE0}, .patches = &D3100_0101_ps}
     ,{.id = 2, .hash = {0x30,0xB1,0x12,0x1F,0x22,0x22,0x11,0x20,0x95,0xFF,0xD2,0x34,0x31,0xD4,0x97,0x15}, .patches = &D3100_0102_ps}
     ,{.id = 3, .hash = {0xAB,0x1C,0x12,0x9D,0x37,0xDC,0x53,0xF6,0x92,0x60,0xA4,0x53,0xD5,0x95,0x62,0x02}, .patches = &D3200_0101_ps}
     ,{.id = 4, .hash = {0xA8,0xC3,0x87,0x3A,0x41,0x24,0x25,0x64,0x27,0xED,0x5B,0x12,0x10,0x77,0x87,0x30}, .patches = &D3200_0102_ps}
     ,{.id = 5, .hash = {0x48,0xCF,0x8E,0x2F,0x20,0xFA,0xE9,0x6D,0x5A,0x35,0xB9,0xC3,0x01,0xFF,0xA3,0x12}, .patches = &D3200_0103_ps}
     ,{.id = 6, .hash = {0x21,0x84,0xF8,0x65,0x82,0xB2,0x7A,0x80,0x49,0xDC,0x8C,0x7D,0x91,0x8A,0xDA,0x50}, .patches = &D5100_0101_ps}
     ,{.id = 7, .hash = {0x22,0x14,0x21,0x0A,0xD2,0xC6,0x5B,0x5E,0x85,0x78,0x99,0xCA,0x79,0xF3,0xDA,0x19}, .patches = &D5100_0102_ps}
     ,{.id = 8, .hash = {0x84,0x53,0x41,0xEC,0x3D,0x92,0xEF,0x46,0x39,0xB0,0x29,0xD7,0xEE,0x16,0x2A,0x2C}, .patches = &D5200_0101_ps}
     ,{.id = 9, .hash = {0x81,0x4B,0x8A,0x99,0x29,0x07,0x55,0x38,0x93,0xFE,0x76,0x52,0x3A,0xA4,0x82,0x19}, .patches = &D5200_0102_ps}
     ,{.id = 10, .hash = {0x60,0x6C,0x50,0x98,0xC4,0x41,0x4A,0x91,0x98,0x47,0x83,0x3D,0xAC,0x45,0x63,0x2C}, .patches = &D7000_0103_ps}
     ,{.id = 11, .hash = {0x76,0xDE,0xFC,0x08,0xF0,0x0A,0xCE,0x3D,0x62,0xBD,0x77,0x00,0x9F,0x97,0x4E,0xC7}, .patches = &D7000_0104_ps}
     ,{.id = 12, .hash = {0xE6,0xD5,0x42,0x68,0x09,0xFE,0x3C,0x64,0xE9,0xA3,0x5B,0x9A,0x3A,0xBD,0xBA,0x7D}, .patches = &D7100_0101_ps}
     ,{.id = 13, .hash = {0xAB,0x4F,0x00,0xE4,0x4B,0x43,0x5C,0x2B,0xE4,0x2D,0xB3,0x97,0x5E,0xEC,0x7F,0x91}, .patches = &D7100_0102_ps}
     ,{.id = 14, .hash = {0xA0,0xCB,0xD0,0x36,0x5F,0x00,0x24,0xA3,0x15,0x1D,0x0F,0x5D,0x6E,0x60,0xC2,0xDB}, .patches = &D300s_0101_ps}
     ,{.id = 15, .hash = {0x7F,0x55,0x03,0xB7,0x95,0xFC,0x41,0xC8,0x3A,0x26,0xE9,0x8D,0x83,0x4D,0x48,0xFF}, .patches = &D300s_0102_ps}
     ,{.id = 16, .hash = {0x77,0xE5,0x42,0x1A,0xFF,0x01,0x1E,0xA7,0x32,0x3F,0x63,0xDB,0xD2,0xC6,0x0E,0x93}, .patches = &D300_0111_B_ps}
     ,{.id = 17, .hash = {0x74,0x0B,0x38,0x22,0x33,0x58,0x17,0xAC,0xA6,0x46,0xF8,0xB9,0x2C,0x3C,0xF6,0xC9}, .patches = &D600_0101_ps}
     ,{.id = 18, .hash = {0x0D,0xCC,0xFD,0x43,0xD7,0xFB,0x8A,0xC4,0x86,0xA4,0xF1,0x90,0x81,0x52,0x03,0x3D}, .patches = &D600_0102_ps}
     ,{.id = 19, .hash = {0xD5,0x29,0x2A,0x20,0x0A,0x98,0x4E,0x15,0xCB,0x8B,0x5B,0x9C,0xD7,0x8A,0xE3,0x25}, .patches = &D610_0101_ps}
     ,{.id = 20, .hash = {0x50,0x64,0x0A,0x6B,0xA9,0xEC,0x2F,0x70,0x46,0xA0,0x27,0x64,0xAC,0x3A,0x67,0x6B}, .patches = &D750_0101_ps}
     ,{.id = 21, .hash = {0x99,0xD7,0x8D,0xEB,0xE5,0x04,0x51,0x03,0x4A,0x32,0x83,0x6E,0x7F,0xDF,0x77,0x88}, .patches = &D800_0101_ps}
     ,{.id = 22, .hash = {0xFD,0xD8,0x91,0xE9,0xFC,0xFE,0xAF,0x30,0x44,0x74,0xE2,0xDC,0x72,0x43,0x70,0x44}, .patches = &D800_0102_ps}
     ,{.id = 23, .hash = {0x61,0xEB,0xBE,0x3C,0xD1,0x6D,0x6A,0x33,0x55,0x2A,0x05,0x79,0x2C,0xAF,0x91,0x27}, .patches = &D800_0110_ps}
     ,{.id = 24, .hash = {0x9C,0x16,0xE0,0x6D,0x85,0x69,0x78,0x71,0x22,0x44,0x35,0x40,0x89,0x33,0x22,0xC0}, .patches = &D800E_0101_ps}
     ,{.id = 25, .hash = {0xB4,0x25,0xC8,0x79,0x71,0x8B,0xC7,0xF9,0x48,0xFD,0x05,0xA6,0x1D,0x7D,0x0A,0x24}, .patches = &D800E_0102_ps}
     ,{.id = 26, .hash = {0xA6,0xA6,0xC6,0xA7,0x74,0x8D,0x5A,0xCC,0x97,0xE8,0x59,0xAD,0x50,0x31,0xAC,0xE5}, .patches = &D800E_0110_ps}
     ,{.id = 27, .hash = {0x0E,0xED,0x2B,0xB2,0x48,0x75,0x61,0x5B,0x5F,0x40,0x8D,0x6D,0x90,0x3A,0x66,0xC6}, .patches = &D810_0102_ps}
     ,{.id = 28, .hash = {0xB0,0x1F,0xD3,0xDF,0xE5,0x74,0x2F,0xB7,0x49,0xA0,0x85,0xD3,0xE4,0x14,0x52,0x7D}, .patches = &D4_0105_ps}
     ,{.id = 29, .hash = {0x80,0x9D,0xBB,0xE0,0x40,0x95,0x4E,0x02,0x24,0xF0,0x95,0xB9,0xC2,0xF6,0xA8,0xC0}, .patches = &D4_0110_ps}
     ,{.id = 30, .hash = {0xBB,0xB0,0x44,0x5F,0x58,0x18,0x57,0xCD,0x0A,0xF7,0x76,0x9D,0xEF,0xBD,0x09,0x51}, .patches = &D4S_0101_ps}
};
