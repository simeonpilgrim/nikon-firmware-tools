D5300
=====

B Firmware
---
file: b930_nnn.bin  
processor: ARM v7 LE  
load base: 0x90020000  
mem maps: v1.03
Src|Dst|Len|Statis
---|---|---|---
0x91498e24|0x10000000|0x3b5b4|rw
0x914d43d8|0x1003b5b4|0x1834|rw
0x915b26a0|0x10119c60|0x1cd4c|rw
0x915b2688|0x10119868|0x4|rw
segments:
Addr|Len|Status|Notesg
---|---|---|---
0x10000000|?|rwx|Kernal
0x90020000|?|rx|B Firmware
0xA2000000|?|rwv|A Firmware shared memory window?


D3300
=====

B Firmware
---
file: b860_nnn.bin  
processor: ARM v7 LE  
load base: 90020000  
mem maps: v1.02  

Src|Dst|Len|Statis
---|---|---|---
0x9158a358|0x10000000|0x0003b5b4|rwx
0x915c590c|0x1003b5b4|0x0000179c|rw
0x916a73a0|0x1011d420|0x00028b3c|rw
0x916a739c|0x1011d03c|4|rw