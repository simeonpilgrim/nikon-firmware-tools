
#include <idc.idc>


static main() 
{
	auto handle;
	auto val, source, dest, count, i;
	
	DelSeg(0xBEB00000,0);
	SegCreate(0xBEB00000,0xBFFFFFFF,0,1,1,2);
	SegRename(0xBEB00000,"RAM");
	SetSegmentType(0xBEB00000,2);
	
	Message("Copy 502F0C: Start\n");
	
	count = 0x19618;
	source = 0x502F0C;
	dest = 0xBEBA2E64;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 502F0C: End\n");
	
	Message("Copy 52EC0C: Start\n");
	
	count = 0xCCA;
	source = 0x52EC0C;
	dest = 0xBFC95B44;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 52EC0C: End\n");
}


