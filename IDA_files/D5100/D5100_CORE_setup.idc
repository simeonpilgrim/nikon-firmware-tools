
#include <idc.idc>

static memcpy(source,dest,count)
{
	auto val, i;
	
	for(i = 0; i < count; i = i + 2 )
	{
		val = Word(source + i);
		PatchWord(dest + i, val);
	}
}
static main() 
{
	auto type;
	auto ea;
	
	ea = 0x8F84C37C;
	Message("isStruct: %d\n", isStruct(GetFlags(ea)));
	type = GetTinfo(ea);
	Message("firstattr: %s\n", firstattr(type));
	Message("firstattr: %d\n", getattr(type,"typid"));
	
	//type = GetOpType(0x8F84C37C, 
	//ProcessUiAction("ClearOutput",0);
	//SetCharPrm(INF_GENFLAGS, INFFL_LOADIDC|GetCharPrm(INF_GENFLAGS));
	
	//memcpy(0xE0630, 0x80200000, 0x3C);
	//memcpy(0xE1FA8, 0x800813D0, 0xB4D2);
	//memcpy(0xE1A3C, 0x8008C8A4, 0x56A);
	//memcpy(0xE066C, 0x80080000, 0xE0);
	//memcpy(0xE074C, 0x800800E0, 0x12F0);
	//memcpy(0xFFC00, 0x8008CE10, 0x400);
	
	//SetCharPrm(INF_GENFLAGS, ~INFFL_LOADIDC&GetCharPrm(INF_GENFLAGS));
	
	Message("CORE: End\n");
}


