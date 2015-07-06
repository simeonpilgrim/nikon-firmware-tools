
#include <idc.idc>


static TaskEntry(loc)
{
	auto id, ea, size, str;
	
	// Fixup entry
	MakeUnknown(loc, 0x1C, 0 );
	MakeStructEx(loc, 0x1C, "sys_task_ent");
	
	id = Word(loc+4);
	ea = Dword(loc+0x10);
	
	if( ea != 0 )
	{
		str = sprintf("Task%02X", id);
		MakeNameEx(ea,str,0);
		MakeCode(ea);
		AutoMark(ea, AU_PROC);
	}
	
	
}

static Tasks(start, end)
{
	auto ea, count;
	ea = start;
	while( ea != BADADDR && ea < end )
	{
		TaskEntry(ea);
		ea = ea + 0x1C;
	}
}



static make_strucs()
{
    auto id, mid;
	
	id = AddStrucEx(-1,"sys_task_ent",0);
	id = GetStrucIdByName("sys_task_ent");
	mid = AddStrucMember(id,"field_0",	0,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_4",	0x4,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_6",	0X6,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_8",	0X8,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_C",	0XC,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_10",	0X10,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_14",	0X14,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_18",	0x18,	0x20000400,	-1,	4);
}

static main() 
{
	make_strucs();

	Tasks(0xD393C, 0xD41C4); 

}


