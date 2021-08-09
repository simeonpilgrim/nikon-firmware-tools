D750
=====

B Firmware
---
file: b1010_nnnd.bin  
processor: ARM v7 LE  
load base: 0x90020000  

mem maps: v1.10  
Src|Dst|Len|Status
---|---|---|---
0x912bec08|0x10000000|0x30344|rwx
0x912eef4c|0x10030344|0xf95c|rw
0x9135fc40|0x100a1400|0x5340c|rw


D7100
=====

B Firmware
---
file: b760nnnn.bin  
processor: ARM v7 LE  
load base: 0x50020000  

mem maps: v1.04  
Src|Dst|Len|Status
---|---|---|---
0x516c6388|0x10000000|0x3b284|rwx
0x5170160c|0x1003b284|0x1670|rw
0x517d0c80|0x1010ac80|0x1890c|rw


D5500
=====

B Firmware
---
file: b1120_nnn.bin  
processor: ARM v7 LE  
load base: 0x90020000 

mem maps: v1.02
Src|Dst|Len|Status
---|---|---|---
0x9109dd3c|0x10000000|0x30ac4|rwx
0x910ce800|0x10030ac4|0xd718|rw
0x9113e060|0x100a0700|0x5cbfc|rw
0x9113e048|0x100a0308|0x4|rw


D5300
=====

B Firmware
---
file: b930_nnn.bin  
processor: ARM v7 LE  
load base: 0x90020000 

mem maps: v1.03
Src|Dst|Len|Status
---|---|---|---
0x91498e24|0x10000000|0x3b5b4|rwx
0x914d43d8|0x1003b5b4|0x1834|rw
0x915b26a0|0x10119c60|0x1cd4c|rw
0x915b2688|0x10119868|0x4|rw
segments:
Addr|Len|Status|Notesg
---|---|---|---
0x10000000|?|rwx|Kernal
0x90020000|?|rx|B Firmware
0xA2000000|?|rwv|A Firmware shared memory window?


D5200
=====

B Firmware
---
file: b970nnna.bin  
processor: ARM v7 LE  
load base: 0x50020000 

mem maps: v1.03
Src|Dst|Len|Status
---|---|---|---
0x51494220|0x10000000|0x3c758|rwx
0x514d0978|0x1003c758|0x10e4|rw
0x515a6280|0x101123e0|0x1a60c|rw


D5100
=====

B Firmware
---
file: b640nnnb.bin  
processor: FR BE  


D3300
=====

B Firmware
---
file: b860_nnn.bin  
processor: ARM v7 LE  
load base: 90020000  

mem maps: v1.02  

Src|Dst|Len|Status
---|---|---|---
0x9158a358|0x10000000|0x0003b5b4|rwx
0x915c590c|0x1003b5b4|0x0000179c|rw
0x916a73a0|0x1011d420|0x00028b3c|rw
0x916a739c|0x1011d03c|4|rw


D3200
=====

B Firmware
---
file: b830nnna.bin  
processor: FR BE  

