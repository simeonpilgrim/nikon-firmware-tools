#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <emscripten/emscripten.h>
#include "md5.h"
#include "patches.h"
#include "patches.c"

int main(int argc, char ** argv) {
  return 0;
}

//struct Patch D5100_0102_patches[] = {
    // {.id=1, .level=Released, .name="Remove Time Based Video Restrictions", .blocks={2}} /*patch_1*/
// };

// struct PatchSet D5100_0102_ps = PATCHSET("D5100", "1.02", D5100_0102_patches);
// struct PatchSet D5200_0101_ps = PATCHSET("D5200", "1.01", 0);

// struct PatchMap patches[] = {
    // {.id = 1, .hash = {0x22, 0x14, 0x21, 0x0A, 0xD2, 0xC6, 0x5B, 0x5E, 0x85, 0x78, 0x99, 0xCA, 0x79, 0xF3, 0xDA, 0x19}, .patches=&D5100_0102_ps},
    // {.id = 2, .hash = {0x84, 0x53, 0x41, 0xEC, 0x3D, 0x92, 0xEF, 0x46, 0x39, 0xB0, 0x29, 0xD7, 0xEE, 0x16, 0x2A, 0x2C}, .patches=&D5200_0101_ps}
// };

const int32_t patches_count = sizeof(patches)/sizeof(struct PatchMap);

//const int MAX_OUTPUT = 4*1024;
//char output[MAX_OUTPUT];

char const * const PatchLevelStr[] = {"DevOnly","Alpha","Beta","Released"};

extern int EMSCRIPTEN_KEEPALIVE detectFirmware(int32_t* ptr,int32_t ptrlen, int32_t* out_ptr,int32_t outlen){
    //printf("ptr_check %p %d\n",ptr,ptrlen);
    if(ptrlen > 0x30){
        unsigned char* p = (unsigned char*)ptr;

        MD5_CTX mdContext;

        MD5Init (&mdContext);
        MD5Update (&mdContext, p, ptrlen);
        MD5Final (&mdContext);
              
        MDPrint (&mdContext); 
        printf("\n");
        
        char* output = (char*)out_ptr;        
        for(int i = 0; i < patches_count; i++){
            int y;
            for(y = 0; y < 16; y++){
                if(patches[i].hash[y] != mdContext.digest[y])
                    break;
            }
            if (y == 16){
                struct PatchSet const * const ps = patches[i].patches;
                //printf("match \n");  
                int outlen = 0;
                outlen += sprintf(&output[outlen],"{\"model\":\"%s\", \"version\":\"%s\", \"patches\":[", 
                    ps->model, ps->version);
                /* do all the patches */
                for(int p=0; p<ps->patch_count; p++){
                    if( p > 0){
                        outlen += sprintf(&output[outlen],",");
                    }
                    struct Patch* pp = &(ps->patches[p]);
                    outlen += sprintf(&output[outlen],"{\"id\":\"%d\", \"name\":\"%s\", \"level\":\"%s\", \"blocks\":[",
                        pp->id, pp->name, PatchLevelStr[pp->level]);
                    for(int b=0; b<MAX_BLOCK; b++){
                        int v = pp->blocks[b];
                        if( v > 0 ){
                            if(b > 0){
                                outlen += sprintf(&output[outlen],",");
                            }
                            outlen += sprintf(&output[outlen],"\"%d\"", v);
                        }              
                    }
                    outlen += sprintf(&output[outlen],"]}");         
                }
                outlen += sprintf(&output[outlen],"]}");
                output[outlen] = '\0';
                //printf("output len: %d\n",outlen);
                //printf("%s\n", output);
                return outlen;        
            }
        }
    }
    return 0;
}
