
#include <idc.idc>


static ExifEntry(loc, text)
{
	auto id, code, ea, size, str, fun;
	
	// Fixup entry
	MakeUnknown(loc, 0x10, 0 );
	MakeStructEx(loc, 0x10, "exif_ent");
	
	code = Word(loc);
	fun = Dword(loc+0xc);
		
	if( code != 0 && fun != 0 )
	{
		str = sprintf("exiftag%s_%04X", text, code);
		MakeNameEx(fun,str,0);
		MakeCode(fun);
		AutoMark(fun, AU_PROC);	
	}
}

static Exif(start, end, text)
{
	auto ea;
	ea = start;
	while( ea != BADADDR && ea < end )
	{
		ExifEntry(ea,text);
		ea = ea + 0x10;
	}
}

static make_strucs()
{
    auto id, mid;
	
	id = AddStrucEx(-1,"exif_ent",0);
	id = GetStrucIdByName("exif_ent");
	mid = AddStrucMember(id,"field_0",	0,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_2",	0x2,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_4",	0x4,	0x20000400,	-1, 4);
	mid = AddStrucMember(id,"field_8",	0x8,	0x20000400,	-1, 4);
	mid = AddStrucMember(id,"field_C",  0xC,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);

}

static main() 
{
	make_strucs();
	
	Exif(0x23596C, 0x235BEC, "_A"); 
	Exif(0x2355EC, 0x23596C, "_B"); 
	Exif(0x2353FC, 0x2355EC, "_C"); 
	Exif(0x2353DC, 0x2353FC, "_D"); 
	Exif(0x234F1C, 0x2353DC, "_E"); 
	Exif(0x234EAC, 0x234F1C, "_F"); 
}



