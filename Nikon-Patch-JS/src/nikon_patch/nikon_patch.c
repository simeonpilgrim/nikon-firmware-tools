#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <emscripten/emscripten.h>
#include "md5.h"
#include "patches.h"
#include "patches.c"

int GenerateOutput(struct PatchSet const * const ps);

int main(int argc, char ** argv) {
  return 0;
}

const uint32_t patches_count = sizeof(patches)/sizeof(struct PatchMap);

const uint32_t MAX_OUTPUT = 4*1024;
char output[MAX_OUTPUT];

const int32_t MAX_FILE = 30*1024*1024;
uint8_t input_file[MAX_FILE];
uint8_t output_file[MAX_FILE];

char const * const PatchLevelStr[] = {"DevOnly","Alpha","Beta","Released"};

extern int32_t EMSCRIPTEN_KEEPALIVE getMaxFileSize(){
    return MAX_FILE;
}
extern char* EMSCRIPTEN_KEEPALIVE getJsonPtr(){
    return output;
}
extern uint8_t* EMSCRIPTEN_KEEPALIVE getInFilePtr(){
    return input_file;
}
extern uint8_t* EMSCRIPTEN_KEEPALIVE getOutFilePtr(){
    return output_file;
}

extern int32_t EMSCRIPTEN_KEEPALIVE detectFirmware(int32_t data_len){
    //printf("ptr_check %p %d\n",ptr,data_len);
    if(data_len > 0x30){
        MD5_CTX mdContext;

        MD5Init (&mdContext);
        MD5Update (&mdContext, input_file, data_len);
        MD5Final (&mdContext);
              
        MDPrint (&mdContext); 
        printf("\n");
           
        for(int i = 0; i < patches_count; i++){
            int y;
            for(y = 0; y < 16; y++){
                if(patches[i].hash[y] != mdContext.digest[y])
                    break;
            }
            if (y == 16){
                struct PatchSet const * const ps = patches[i].patches;
                return GenerateOutput(ps);
            }
        }
    }
    return 0;
}

int GenerateOutput(struct PatchSet const * const ps){
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

