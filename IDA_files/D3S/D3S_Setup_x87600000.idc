
#include <idc.idc>


static main() 
{
	auto handle;
	auto val, source, dest, count, i;
	
	DelSeg(0x87600000,0);
	SegCreate(0x87600000,0x877FFFFF,0,1,1,2);
	SegRename(0x87600000,"RAM");
	SetSegmentType(0x87600000,2);
	
	Message("Copy 2FF8A4: Start\n");
	
	count = 0x16FB0;
	source = 0x2FF8A4;
	dest = 0x877DD99C;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 2FF8A4: End\n");
	
	Message("Copy 316850: Start\n");
	
	count = 0x84E;
	source = 0x316850;
	dest = 0x876EA9C0;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 316850: End\n");
}


