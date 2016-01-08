
#include <idc.idc>

static make_strucs()
{
    auto id, mid;
	
	id = AddStrucEx(-1,"Menu",0);
	id = GetStrucIdByName("Menu");
	mid = AddStrucMember(id,"field_0",	0,	0x10000400,	-1,	2);
	//SetMemberComment(id,	0,	"Language Table Index (0x6xxx)",	1);
	mid = AddStrucMember(id,"field_2",	0X2,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_4",	0X4,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_6",	0X6,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_8",	0X8,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_A",	0XA,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_C",	0XC,	0x10000400,	-1,	2);
	//SetMemberComment(id,	0XC,	"selection offset",	1);
	mid = AddStrucMember(id,"field_E",	0XE,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_10",	0X10,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"bft_idx",	0X12,	0x10000400,	-1,	2);
	//SetMemberComment(id,	0X12,	"BigFuncTable Index",	1);
	mid = AddStrucMember(id,"parent_ptr",	0X14,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_18",	0X18,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	//SetMemberComment(id,	0X18,	"WORD* - list count",	1);
	mid = AddStrucMember(id,"element_ptr",	0X1C,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	
	id = AddStrucEx(-1,"MenuEl",0);
	id = GetStrucIdByName("MenuEl");
	mid = AddStrucMember(id,"field_0",	0,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_2",	0X2,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_4",	0X4,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_6",	0X6,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_8",	0X8,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_A",	0XA,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"menu_ptr",	0XC,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	
	id = AddStrucEx(-1,"MenuState",0);
	id = GetStrucIdByName("MenuState");
	mid = AddStrucMember(id,"field_0",	0,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_2",	0X2,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_4",	0X4,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	
	id = AddStrucEx(-1,"MenuScroll",0);
	id = GetStrucIdByName("MenuScroll");
	mid = AddStrucMember(id,"field_0",	0,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_2",	0X2,	0x10000400,	-1,	2);
}

static MenuStatesEntry(loc)
{
	auto count, sub;
	auto str, i, ea;
	
	count = Word(loc);
	sub = Dword(loc+4);	
	
	str = sprintf("MS_%02d_%08X", count, loc );
	MakeUnknown(loc, 0x8, 0 );
	MakeStructEx(loc, 0x8, "MenuState");
	MakeNameEx(loc, str, 0);
	
	if( count != 0 && sub != 0)
	{
		i = 0;
		while( i < count)
		{
			ea= sub + (i*4);
			MakeUnknown(sub + (i*4), 0x4, 0 );
			MakeStructEx(sub + (i*4), 0x4, "MenuScroll");
			i = i + 1;
		}
	}
}

static MenuStates(start, end)
{
	auto ea;
	
	ea= start;
	while( ea != BADADDR && ea < end )
	{
		MenuStatesEntry(ea);
		ea = ea +8;
	}
}

static main() 
{
	make_strucs();
	
	MenuStates(0x8F9BD67C, 0x8F9BD92C);
	
	Message("Menu Base: Done\n");
}


