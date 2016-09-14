
#include <idc.idc>


static search_and_mark() 
{
	auto start, ea, eb, last, len, str;
	
	Message("PNG Mark - Start\n");
	
	last = 0;
	start = 0x90020000;
	ea = FindBinary(start, SEARCH_DOWN, "89 50 4E 47");
	while( ea != BADADDR && ea != last)
	{
		eb = FindBinary(ea, SEARCH_DOWN, "49 45 4e 44");
		last = ea;
				
		if( eb != BADADDR )
		{
			eb = eb + 4;
			len = eb - ea;
			
			Message("%x %x %x\n", ea, eb, len);
			if( len < 0x1000)
			{
				MakeData(ea, FF_BYTE, 1, 0 );
				MakeArray(ea, len);
				str = sprintf("png_%x",ea);
				//Message(str);
				MakeNameEx(ea, str, 0);
			}
		}
	
		ea = FindBinary(ea + 4, SEARCH_DOWN, "89 50 4E 47");
	}

	Message("PNG Mark - End\n");
}

static pngentry(loc)
{
	auto len, ea, size, str;
	
	// Fixup entry
	MakeUnknown(loc, 0x10, 0 );
	MakeStructEx(loc, 0x10, "png_ent");
	
	len = Word(loc+2);
	ea = Dword(loc+0xC);
	
	if( ea != 0 )
	{
		MakeUnknown(ea, len, 0 );
		MakeData(ea, FF_BYTE, 1, 0 );
		MakeArray(ea, len);
		str = sprintf("png_%02X", ea);
		MakeNameEx(ea,str,0);
	}
}

static pnglist(start, end)
{
	auto ea;
	ea = start;
	while( ea != BADADDR && ea < end )
	{
		pngentry(ea);
		ea = ea + 0x10;
	}
}

static make_strucs()
{
    auto id, mid;

	id = AddStrucEx(-1,"png_ent",0);
	id = GetStrucIdByName("png_ent");
	mid = AddStrucMember(id,"field_0",	0x0,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_2",	0X2,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_4",	0X4,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_8",	0X8,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_C",	0XC,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
}



static main() 
{
// first time use search and mark to find the address range, then run the later two function.

	//search_and_mark();
	
	//make_strucs();
	pnglist(0x90E819B8,0x90E8D108);
}