
#include <idc.idc>


static main() 
{
	auto handle;
	auto val, source, dest, count, i;
	
	DelSeg(0x84D00000,0);
	SegCreate(0x84D00000,0x85FFFFFF,0,1,1,2);
	SegRename(0x84D00000,"RAM");
	SetSegmentType(0x84D00000,2);
	
	Message("Copy 4CF960: Start\n");
	
	count = 0x1FB98;
	source = 0x4CF960;
	dest = 0x84F23B20;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 4CF960: End\n");
	
	Message("Copy 500510: Start\n");
	
	count = 0xCCA;
	source = 0x500510;
	dest = 0x85B4A04C;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 500510: End\n");
}


// 0x4CF960 + 0x1FB98 = 0x4EF4F8
// 0x84F23B20 + 0x1FB98 = 0x84F436B8
// 84F49F14 ME 0x263f4 beyound 0x84F23B20

