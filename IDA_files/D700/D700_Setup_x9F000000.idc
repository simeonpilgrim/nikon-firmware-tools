
#include <idc.idc>


static main() 
{
	auto handle;
	auto val, source, dest, count, i;
	
	DelSeg(0x9F000000,0);
	SegCreate(0x9F000000,0x9F3FFFFF,0,1,1,2);
	SegRename(0x9F000000,"RAM");
	SetSegmentType(0x9F000000,2);
	
	Message("Copy 2AC724: Start\n");
	
	count = 0x13498;
	source = 0x2AC724;
	dest = 0x9F049094;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 2AC724: End\n");
	
	Message("Copy 2BFBB8: Start\n");
	
	count = 0x888;
	source = 0x2BFBB8;
	dest = 0x9F0249A0;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 2BFBB8: End\n");
}


