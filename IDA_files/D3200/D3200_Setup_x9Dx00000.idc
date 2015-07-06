
#include <idc.idc>

	static idc_memcpy(source, dest, byte_count, desc)
	{
		auto i, val;

		SetCharPrm(INF_GENFLAGS, INFFL_LOADIDC|GetCharPrm(INF_GENFLAGS));
		Message("Copy %a: Start\n", dest);

		for(i = 0; i < byte_count; i = i + 2 )
		{
			val = Word(source + i);
			PatchWord(dest + i, val);
		}
		
		SetCharPrm(INF_GENFLAGS, ~INFFL_LOADIDC&GetCharPrm(INF_GENFLAGS));
		
		MakeUnknown(source,byte_count,DOUNK_EXPAND+DOUNK_DELNAMES);
		MakeByte(source);
		MakeArray(source, byte_count);
		
		HideArea(source, source+byte_count, desc, "", "", -1);
		SetHiddenArea(source, 0 );
		Message("Copy %a: End\n", dest);
	}

static main() 
{
	auto handle;
	auto val, source, dest, count, i;
	
	//DelSeg(0x9D300000,0);
	//SegCreate(0x9D300000,0x9D3FFFFF,0,1,1,2);
	//SegRename(0x9D300000,"RAM");
	//SetSegmentType(0x9D300000,2);

	//SegCreate(0x9DA00000,0x9DAFFFFF,0,1,1,2);
	//SegRename(0x9DA00000,"RAM");
	//SetSegmentType(0x9DA00000,2);
	
	idc_memcpy(0x4356D4, 0x9D313830, 0x127B8, "RAM Data source"); // RAM data
	idc_memcpy(0x454704, 0x9DA8EE6C, 0x1160, "RAM Code Source");
	idc_memcpy(0x447E8C, 0x9D325FE8, 0xC878, "RAM Data2 Source"); // Tsk32 copy

}


