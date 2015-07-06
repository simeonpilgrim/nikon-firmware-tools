
#include <idc.idc>


static main() 
{
	auto handle;
	auto val, source, dest, count, i;
	
	DelSeg(0x8F800000,0);
	SegCreate(0x8F800000,0x8F9FFFFF,0,1,1,2);
	SegRename(0x8F800000,"RAM");
	SetSegmentType(0x8F800000,2);

	
	Message("Copy 37BDE4: Start\n");
	
	count = 0x17F18;
	source = 0x37BDE4;
	dest = 0x8F9C4E78;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 37BDE4: End\n");
}


