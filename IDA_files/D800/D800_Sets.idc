
#include <idc.idc>


static Set1Entry(loc, count)
{
	auto id, ea, size, str, fun;
	
	// Fixup entry
	MakeUnknown(loc, 0x10, 0 );
	MakeStructEx(loc, 0x10, "SetEnt1");
	
	ea = Dword(loc);
	size = Word(loc+4);
	fun = Dword(loc+0xc);
	
	Message("id %d ea %a size %x\n", count, ea, size);
	if( size != 0 )
	{	
		str = sprintf("Set%02X", count);
		if ( GetStrucIdByName(str) == -1 )
		{
			id = AddStrucEx(-1,str,0);
			id = GetStrucIdByName(str);
			AddStrucMember(id,"dummy_end",	size-1,	0x000400,	-1,	1);
		}
	}
	
	if( ea != 0 )
	{
		str = sprintf("set%02X", count);
		MakeNameEx(ea,str,0);
		
		if ( size != 0 ){
			str = sprintf("Set%02X", count);
			MakeUnknown(ea, size, 0 );
			MakeStructEx(ea, size, str);
		}
	}
	
	if( fun != 0 )
	{
		str = sprintf("Set%02x_Sync", count);
		MakeNameEx(fun,str,0);
		MakeCode(fun);
		AutoMark(fun, AU_PROC);	
	}
}

static Set2Entry(loc)
{
	auto id, ea, size, str;
	
	// Fixup entry
	MakeUnknown(loc, 0xC, 0 );
	MakeStructEx(loc, 0xC, "SetEnt2");
	
	id = Dword(loc);
	ea = Dword(loc+4);
	size = Dword(loc+8);
	
	Message("id %d ea %a size %x\n", id, ea, size);
	if( ea != 0 )
	{
		str = sprintf("set%02X", id);
		MakeNameEx(ea,str,0);
		
		str = sprintf("Set%02X", id);
		if ( GetStrucIdByName(str) == -1 )
		{
			id = AddStrucEx(-1,str,0);
			id = GetStrucIdByName(str);
			AddStrucMember(id,"dummy_end",	size-1,	0x000400,	-1,	1);
		}
		
		MakeUnknown(ea, size, 0 );
		MakeStructEx(ea, size, str);
	}
	
	
}

static Sets2(start, end)
{
	auto ea;
	ea = start;

	while( ea != BADADDR && ea < end )
	{
		Set2Entry(ea);
		ea = ea + 0xC;
	}
}

static Sets1(start, end)
{
	auto ea, count;
	ea = start;
	count = 0;
	while( ea != BADADDR && ea < end )
	{
		Set1Entry(ea,count);
		
		count = count + 1;
		ea = ea + 0x10;
	}
}


static make_strucs()
{
    auto id, mid;
	
	id = AddStrucEx(-1,"SetEnt1",0);
	id = GetStrucIdByName("SetEnt1");
	mid = AddStrucMember(id,"field_0",	0,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_4",	0x4,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_6",	0X6,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_8",	0X8,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_C",	0XC,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);

	id = AddStrucEx(-1,"SetEnt2",0);
	id = GetStrucIdByName("SetEnt2");
	mid = AddStrucMember(id,"index",	0,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"mem_ptr",	0x4,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"mem_size",	0X8,	0x20000400,	-1,	4);
}

static main() 
{
	//make_strucs();
	//Sets2(0x3EE3B4, 0x3EE690); 
	Sets1(0x31A96C, 0x31AF7C); 

}


