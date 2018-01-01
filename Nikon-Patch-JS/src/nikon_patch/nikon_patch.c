#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <emscripten/emscripten.h>
#include "md5.h"
#include "xor.h"
#include "patches.h"

int GenerateOutput(struct PatchSet const * const ps);
int CheckPatches(int select_len);
void ApplyPatches(int select_len);
void LoadBlocksOffsets();
void CorrectCrcs();

int main(int argc, char ** argv) {
  return 0;
}

struct BlockOffset {
    int offset;
    int length;
};

const uint32_t MAX_BLOCKS = 10;
struct BlockOffset blocks_table[MAX_BLOCKS];

const uint32_t MAX_OUTPUT = 4*1024;
char output[MAX_OUTPUT];

const int32_t MAX_FILE = 50*1024*1024;
uint32_t data_length = 0;
uint8_t input_file[MAX_FILE];
uint8_t output_file[MAX_FILE];

const uint32_t MAX_SELECT = 20;
uint32_t selected[MAX_SELECT];

char const * const PatchLevelStr[] = {"DevOnly","Alpha","Beta","Released"};
struct PatchSet const * selectedPatch = NULL;

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
extern uint32_t* EMSCRIPTEN_KEEPALIVE getSelectPtr(){
    return selected;
}

extern int32_t EMSCRIPTEN_KEEPALIVE patch_firmare(int32_t select_len){
    if( selectedPatch == NULL){
        printf("selectedPatch is NULL\n");
        return 0;
    }
    if( data_length < 1 || data_length > MAX_FILE){
        printf("data_length is out of range: %d\n", data_length);
        return 0;
    }
    if(select_len < 1 || select_len > MAX_SELECT){
        printf("select_len is out of range: %d\n", select_len);
        return 0;
    }

    // for(int i = 0; i < select_len; i++){
    //     printf("s: %d\n", selected[i]);
    // }
    
    memcpy(output_file, input_file, data_length);
    if(selectedPatch->patch_type == 0){
        Xor(output_file, data_length);
    }
    LoadBlocksOffsets();

    if(CheckPatches(select_len) == 0){
        return 0;
    }
    ApplyPatches(select_len);
    CorrectCrcs();
    if(selectedPatch->patch_type == 0){
        Xor(output_file, data_length);
    }
    return data_length;
}

extern int32_t EMSCRIPTEN_KEEPALIVE detectFirmware(int32_t data_len){
    selectedPatch = NULL;
    data_length = 0;
    //printf("ptr_check %p %d\n",data_len);
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
                selectedPatch = ps;
                data_length = data_len;
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

int CheckPatches(int select_len){
    for(int i = 0; i < select_len; i++){
        int p_idx = selected[i];

        for(int p=0; p<selectedPatch->patch_count; p++){
            struct Patch* pp = &(selectedPatch->patches[p]);
        
            if(p_idx == pp->id){
                //printf("patch %s\n",pp->name);
                for(int ci=0;ci <pp->changes_len; ci++){
                    //printf(" change %d\n",ci);
                    struct Change *c = pp->changes[ci];
                    uint32_t file_offset = blocks_table[c->file_idx].offset;
                    for(int b=0; b<c->orig_len;b++){
                        int base = file_offset + c->file_offset;
                        if(output_file[base+b] != c->orig[b]){
                            //printf("no match %d %d %d %d\n", base, b, output_file[base+b], c->orig[b] );
                            return 0;
                        }
                    }
                }
            }
        }
    }

    return 1;
}

void ApplyPatches(int select_len){
    for(int i = 0; i < select_len; i++){
        int p_idx = selected[i];
        for(int p=0; p<selectedPatch->patch_count; p++){
            struct Patch* pp = &(selectedPatch->patches[p]);
        
            if(p_idx == pp->id){
                for(int ci=0;ci <pp->changes_len; ci++){
                    struct Change *c = pp->changes[ci];
                    uint32_t file_offset = blocks_table[c->file_idx].offset;
                    for(int b=0; b<c->orig_len;b++){
                        int base = file_offset + c->file_offset;
                        output_file[base+b] = c->patch[b];
                    }
                }
            }
        }
    }
}

uint32_t ReadU32(uint8_t* data, uint32_t offset){
    // big endian to little
    uint32_t v = data[offset+3] + (data[offset+2]<<8)+(data[offset+1]<<16)+(data[offset]<<24);
    return v;
}

void LoadBlocksOffsets(){
    memset(blocks_table, 0, sizeof(blocks_table));

    if(selectedPatch->patch_type == 1){
        blocks_table[0].offset = 0;
        blocks_table[0].length = data_length;
    } else {
        if( data_length > 0x30){
            int pos = 0x20;
            uint32_t count = ReadU32(output_file,pos+0);
            printf("header count %d\n", count);
            //uint32_t headerlen = ReadU32(output_file, pos + 4);
            //uint32_t dummy1 = ReadU32(output_file, pos + 8);
            //uint32_t dummy2 = ReadU32(output_file, pos + 12);
            pos += 16;

            if(data_length> ((count * 30 )+pos)){
                for(int c = 0; c < count; c++){
                    blocks_table[c].offset = ReadU32(output_file, pos + 16);
                    blocks_table[c].length = ReadU32(output_file, pos + 20);
                    //printf("block %d %4x %4x\n", c, blocks_table[c].offset, blocks_table[c].length);
                    //uint32_t hdummy1 = ReadU32(output_file, pos + 24);
                    //uint32_t hdummy2 = ReadU32(output_file, pos + 28);

                    pos += 32; 
                }
            }
        }
    }
}

uint32_t CrcBig(uint8_t* data, int len)
{
    uint32_t rem = 0x0000;

    for (uint32_t i = 0; i < len; i++)
    {
        rem = rem ^ (data[i] << 8);
        for (uint32_t j = 0; j < 8; j++)
        {
            if ((rem & 0x8000) != 0)
            {
                rem = (rem << 1) ^ 0x1021;
            }
            else
            {
                rem = rem << 1;

            }
            rem = rem & 0xFFFF; // Trim remainder to 16 bits
        }
    }

    return rem;
}

void CorrectCrcs(){
    for(int i =0; i<MAX_BLOCKS;i++){
        if( blocks_table[i].length > 2){
            uint8_t* data = &output_file[blocks_table[i].offset];
            uint32_t len = blocks_table[i].length;

            uint32_t checksum = CrcBig(data, len-2);
            
            data[len - 2] = ((checksum >> 8) & 0xff);
            data[len - 1] = ((checksum >> 0) & 0xff);
            
        }
    }
}