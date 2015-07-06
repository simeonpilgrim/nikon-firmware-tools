
#include <idc.idc>

static idc_memcpy(source, dest, count)
{
	auto i, val;

	SetCharPrm(INF_GENFLAGS, INFFL_LOADIDC|GetCharPrm(INF_GENFLAGS));
	Message("Copy %a: Start\n", dest);

	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy %a: End\n", dest);
	SetCharPrm(INF_GENFLAGS, ~INFFL_LOADIDC&GetCharPrm(INF_GENFLAGS));
}

static main() 
{

	//DelSeg(0x8F800000,0);
	//SegCreate(0x8F800000,0x8F9FFFFF,0,1,1,2);
	//SegRename(0x8F800000,"RAM");
	//SetSegmentType(0x8F800000,2);
	
	idc_memcpy(0x367E74, 0x8F9ACE44, 0x1D3F8);
	idc_memcpy(0x385268, 0x8F800000, 0xA50);
}


