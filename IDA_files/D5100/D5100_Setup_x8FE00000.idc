
#include <idc.idc>


static main() 
{
	auto handle;
	auto val, source, dest, count, i;
	
	DelSeg(0x8FC00000,0);
	SegCreate(0x8FC00000,0x8FEFFFFF,0,1,1,2);
	SegRename(0x8FC00000,"RAM5");
	SetSegmentType(0x8FC00000,2);
	
}


