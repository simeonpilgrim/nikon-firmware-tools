enum PatchLevel{
    DevOnly,
    Alpha,
    Beta,
    Released
};

#define MAX_BLOCK 10

struct Change {
    int file_idx;
    int file_offset;
    uint8_t * orig;
    int orig_len;
    uint8_t * patch;
    int patch_len;
};
#define CHANGE(i,o,b,a) {.file_idx=i, .file_offset=o, .orig=b, .orig_len=sizeof(b), .patch = a, .patch_len = sizeof(a)}

struct Patch{
    int id;
    enum PatchLevel level;
    char const * const name;
    int blocks[MAX_BLOCK];
    struct Change ** changes;
    int changes_len;
};

struct PatchSet{
    char const * const model;
    char const * const version;
    struct Patch* patches;
    int patch_count;
    int patch_type; /* 0 modern file, */
};
#define PATCHSET(m,v,p,t) {.model=m, .version=v, .patches=p, .patch_count=sizeof(p)/sizeof(struct Patch), .patch_type = t}

struct PatchMap{
    int id;
    uint8_t hash[16];
    struct PatchSet* patches;
};

extern struct PatchMap patches[];
extern const uint32_t patches_count;