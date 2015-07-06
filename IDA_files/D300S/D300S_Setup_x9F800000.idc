
#include <idc.idc>


static main() 
{
	auto handle;
	auto val, source, dest, count, i;
	
	DelSeg(0x9F800000,0);
	SegCreate(0x9F800000,0x9F9FFFFF,0,1,1,2);
	SegRename(0x9F800000,"RAM");
	SetSegmentType(0x9F800000,2);
	
	Message("Copy 2DB304: Start\n");
	
	count = 0x3C528;
	source = 0x2DB304;
	dest = 0x9F92DCF8;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 2DB304: End\n");
	
	Message("Copy 31782C: Start\n");
	
	count = 0x888;
	source = 0x31782C;
	dest = 0x9F8707A0;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 31782C: End\n");
}


