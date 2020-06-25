D5300
=====

B Firmware
---
file: b930_nnn.bin  
processor: ARM v7 LE  
load base: 0x90020000  
mem maps:
Src|Dst|Len|Statis
---|---|---|---
0x91498e24| 0x10000000|0x3b5b4|rw
0x914d43d8|0x1003b5b4|0x1834|rw
0x915b26a0|0x10119c60|0x1cd4c|rw
0x915b2688|0x10119868|0x4|rw
segments:
Addr|Len|Status|Notesg
---|---|---|---
0x10000000|?|rwx|Kernal
0x90020000|?|rx|B Firmware
0xA2000000|?|rwv|A Firmware shared memory window?
