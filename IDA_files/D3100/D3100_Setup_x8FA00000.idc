
#include <idc.idc>


static main() 
{
	auto handle;
	auto val, source, dest, count, i;
	
	DelSeg(0x8FA00000,0);
	SegCreate(0x8FA00000,0x8FBFFFFF,0,1,1,2);
	SegRename(0x8FA00000,"RAM");
	SetSegmentType(0x8FA00000,2);
	
	Message("Copy 4CF960: Start\n");
	
	count = 0x16CA0;
	source = 0x3395D8;
	dest = 0x8FBECFA0;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
		PatchDword(source + i, 0);
	}
	
	Message("Copy 4CF960: End\n");
	
	Message("Copy 500510: Start\n");
	
	count = 0xae4;
	source = 0x350278;
	dest = 0x8FA00000;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
		PatchDword(source + i, 0);
	}
	
	Message("Copy 500510: End\n");
}


