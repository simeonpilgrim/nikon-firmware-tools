
#include <idc.idc>

static FixFunc(loc, idx, count)
{
	auto ea;
	auto str, old;
	
	ea = Dword(loc + ( idx * 4));
	if( ea != 0 )
	{
		MakeCode(ea);
		AutoMark(ea, AU_PROC);

		str = sprintf("BFT%02d_s%d", count, idx);
		old = NameEx( BADADDR, ea );
		
		if( str[0] != old[0] ||
		str[1] != old[1] ||
		str[2] != old[2] ||
		(str[3] != old[3] && old[3] != 'x') ||
		(str[4] != old[4] && old[4] != 'x') ||
		(str[5] != old[5] && old[4] != 'x')  ) 
		{
			if( hasUserName( GetFlags(ea) ) == 0 )
			{
				MakeNameEx(ea, str, 0);
			}
			else
			{
				// make new BFTxx version
				str = sprintf("BFTxx_s%d_%X", idx, ea);
				MakeNameEx(ea,str,0);
			}
		}
	}
}


static BFTEntry(loc, count)
{
	auto str;
	
	// Fixup entry
	MakeUnknown(loc, 0x30, 0 );
	MakeStructEx(loc, 0x30, "unknown_xx");
	if( count > 0 )
	{
		str = sprintf("BFT%02d", count);
		MakeNameEx(loc,str,0);
	}
	
	FixFunc(loc, 1, count);
	FixFunc(loc, 2, count);
	FixFunc(loc, 3, count);
	FixFunc(loc, 4, count);
	FixFunc(loc, 5, count);
	FixFunc(loc, 6, count);
	FixFunc(loc, 7, count);
	FixFunc(loc, 8, count);
	FixFunc(loc, 9, count);
	FixFunc(loc, 10, count);

	
}

static BFTFix(start, end)
{
	auto ea, count;
	ea = start;
	count = 0;
	while( ea != BADADDR && ea < end )
	{
		BFTEntry(ea, count);
		count = count + 1;
		ea = ea + 0x30;
	}
}

static BFTFixClean(start, end)
{
	auto ea;
	ea = start;
	while( ea != BADADDR && ea < end )
	{
		MakeNameEx(ea, "", 0);
		MakeUnknown(ea, 0x30, 0 );
		ea = ea + 0x30;
	}
}

static make_struc()
{
    auto id, mid;
	
	id = AddStrucEx(-1,"unknown_xx",0);
	id = GetStrucIdByName("unknown_xx");
	mid = AddStrucMember(id,"field_0",	0,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_2",	0X2,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_4",	0X4,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_8",	0X8,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_C",	0XC,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_10",	0X10,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_14",	0X14,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_18",	0X18,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_1C",	0X1C,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_20",	0X20,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_24",	0X24,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_28",	0X28,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_2C",	0X2C,	0x20000400,	-1,	4);
}

static main() 
{
	//make_struc();
	//BFTFixClean(0x4360B8, 0x43B308); 
	BFTFix(0x84F245F8, 0x84F2C158); 

	Message("BFT Fix: Done\n");
}


