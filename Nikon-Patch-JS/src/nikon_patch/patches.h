enum PatchLevel{
    DevOnly,
    Alpha,
    Beta,
    Released
};

const int MAX_BLOCK = 10;

struct Patch{
    int id;
    enum PatchLevel level;
    char const * const name;
    int blocks[MAX_BLOCK];
};

struct PatchSet{
    char const * const model;
    char const * const version;
    struct Patch* patches;
    int patch_count;
};
#define PATCHSET(m,v,p) {.model=m, .version=v, .patches=p, .patch_count=sizeof(p)/sizeof(struct Patch)}

struct PatchMap{
    int id;
    uint8_t hash[16];
    struct PatchSet* patches;
};

enum Firmware {
D3100_0101,
D3100_0102,
D3200_0101,
D3200_0102,
D3200_0103,
D5100_0101,
D5100_0102,
D5200_0101,
D5200_0102,
D7000_0101,
D7000_0102,
D7000_0103,
D7000_0104,
D7100_0101,
D7100_0102,
D300s_0101,
D300s_0102,
D300_0111_B,
D600_0101,
D600_0102,
D610_0101,
D750_0101,
D800_0101,
D800_0102,
D800_0110,
D800E_0101,
D800E_0102,
D800E_0110,
D810_0102,
D4_0105,
D4_0110,
D4S_0101
};