
#include <idc.idc>

static SetStruc(idx, len)
{
	auto id, str, field_str, i;
	
	str = sprintf("Set%02X", idx);
	id = GetStrucIdByName(str);
	if ( id == -1 )
	{
		id = AddStrucEx(-1,str,0);
		id = GetStrucIdByName(str);
			
		i = 0;
		while (i < len)
		{
			field_str = sprintf("field_%x",i);
			AddStrucMember(id, field_str,	i,	0x000400,	-1,	1);
			i = i + 1;
		}
	}
	else
	{
		if( len != GetStrucSize(id))
		{
			Message("STRUCT '%s' different sizes %x %x\n", str, len, GetStrucSize(id));
		}
	}
	
	return str;
}

static SetEntryA(loc)
{
	auto id, str;
	auto ptr, len;
	auto seta_len = 0xC;
	
	id = Dword(loc);	
	ptr = Dword(loc+4);	
	len = Dword(loc+8);
	
	MakeUnknown(loc, seta_len, 0 );
	MakeStructEx(loc, seta_len, "set_entry_A");
	
	if( ptr != 0 )
	{
		str = sprintf("set%02X", id);
		MakeNameEx(ptr, str,0);
		
		str = SetStruc(id, len);	
		MakeUnknown(ptr, len, 0 );
		MakeStructEx(ptr, len, str);
	}
	
	if(id == 0xFF)
	{
		return BADADDR;
	}
	return loc + seta_len;
}

static SetA(start, name)
{
	auto ea;
	
	ea = start;
	while( ea != BADADDR)
	{
		ea = SetEntryA(ea);
	}
	
	MakeNameEx(start, name, 0);
}

static SetEntryB(loc, count)
{
	auto id, set_ptr, size, str, func_ptr;
	
	// Fixup entry
	MakeUnknown(loc, 0x10, 0 );
	MakeStructEx(loc, 0x10, "set_entry_B");
	
	set_ptr = Dword(loc);
	size = Dword(loc+0x4);
	func_ptr = Dword(loc+0xc);
	
	if( set_ptr != 0 )
	{
		str = sprintf("set%02X", count);
		MakeNameEx(set_ptr, str,0);
		
		if ( size != 0 )
		{
			str = SetStruc(count, size);
			MakeUnknown(set_ptr, size, 0 );
			MakeStructEx(set_ptr, size, str);
		}
	}
	
	if( func_ptr != 0 )
	{
		str = sprintf("Set%02x_Sync", count);
		MakeNameEx(func_ptr,str,0);
		MakeCode(func_ptr);
		AutoMark(func_ptr, AU_PROC);	
	}
}

static SetB(start, end, name)
{
	auto ea, count;
	
	MakeNameEx(start, name, 0);

	ea= start;
	count = 0;
	while( ea != BADADDR && ea < end )
	{
		SetEntryB(ea, count);
		ea = ea + 0x10;
		count = count + 1;
	}
}


static make_strucs()
{
    auto id, mid;
	
	id = AddStrucEx(-1,"set_entry_A",0);
	id = GetStrucIdByName("set_entry_A");
	mid = AddStrucMember(id,"index",	0x00,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"mem_ptr",	0x04,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"mem_size",	0x08,	0x20000400,	-1,	4);
	
	id = AddStrucEx(-1,"set_entry_B",0);
	id = GetStrucIdByName("set_entry_B");
	mid = AddStrucMember(id,"mem_ptr",	0x00,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"mem_size",	0x04,	0x20000400,	-1,	4);	
	mid = AddStrucMember(id,"field_8",	0x08,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"func_ptr",	0x0C,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
}

static main() 
{
	make_strucs();
	
	// Sets D7000 1.05
	SetA(0x22FFDC, "set_size_tab"); 
	SetB(0x24DCF0, 0x24E1F0, "set_func_tab"); 

}


