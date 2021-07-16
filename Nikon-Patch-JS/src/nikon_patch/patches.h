enum PatchLevel : char{
    DevOnly,
    Alpha,
    Beta,
    Released
};

#define MAX_BLOCK 8

struct Change {
    uint8_t const * const orig;
    uint8_t const * const patch;
    int file_offset;
    uint16_t orig_len;
    uint16_t patch_len;
    uint8_t file_idx;
};

#define CHANGE(i,o,b,a) {.file_idx=i, .file_offset=o, .orig=b, .orig_len=sizeof(b), .patch = a, .patch_len = sizeof(a)}

struct Patch{
    char const * const name;
    struct Change const ** const changes;
    uint8_t blocks[MAX_BLOCK];
    int changes_len;
    enum PatchLevel level;
    uint8_t id;
};

struct PatchSet{
    char const * const model;
    char const * const version;
    struct Patch const * const patches;
    uint8_t patch_count;
    uint8_t patch_type; /* 0 modern file, */
};

#define PATCHSET(m,v,p,t) {.model=m, .version=v, .patches=p, .patch_count=sizeof(p)/sizeof(struct Patch), .patch_type = t}

struct PatchMap{
    int id;
    uint8_t hash[16];
    struct PatchSet* patches;
};

extern struct PatchMap patches[];
extern const uint32_t patches_count;