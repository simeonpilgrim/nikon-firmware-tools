
#include <idc.idc>


static TaskEntry(loc)
{
	auto id, ea, size, str, state;
	
	// Fixup entry
	MakeUnknown(loc, 0x1C, 0 );
	MakeStructEx(loc, 0x1C, "sys_tcb");
	
	state = Dword(loc);
	id = Word(loc+4);
	ea = Dword(loc+0x10);
	
	if( id == 0)
	{
		MakeNameEx(loc, "task_0_tcb", 0);
	}
	
	if( state != 0) 
	{
		str = sprintf("tsk_state_%02X", id);
		MakeNameEx(state,str,0);
		MakeUnknown(state, 0x2C, 0 );
		MakeStructEx(state, 0x2C, "sys_task_state");

	}
	if( ea != 0 )
	{
		str = sprintf("Tsk%02X", id);
		MakeNameEx(ea,str,0);
		MakeCode(ea);
		AutoMark(ea, AU_PROC);
	}
	
	
}

static Tasks(start)
{
	auto size,count;
	auto ea;
	
	count = Word(start);
	size = Word(start+2);
	
	if(size == 0x1C)
	{
		MakeUnknown(start, 0x4, 0 );
		MakeStructEx(start, 0x4, "table_header");
		MakeNameEx(start,"TCB_table",0);
	
		ea = start + 4;
		while( ea != BADADDR && count >= 0 )
		{
			TaskEntry(ea);
			ea = ea + size;
			count = count - 1;
		}
	}
}



static make_task_strucs()
{
    auto id, mid;
	
	id = AddStrucEx(-1,"sys_tcb",0);
	id = GetStrucIdByName("sys_tcb");
	mid = AddStrucMember(id,"state_ptr",	0,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"id",	0x4,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"priority",	0X6,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_8",	0X8,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"flags",	0XC,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"func_ptr",	0X10,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"stack_ptr",	0X14,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_18",	0x18,	0x20000400,	-1,	4);
	
	id = AddStrucEx(-1,"sys_task_state",0);
	id = GetStrucIdByName("sys_task_state");
	mid = AddStrucMember(id,"field_0",	0x00,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_4",	0x04,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_8",	0x08,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_C",	0x0C,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_10",	0x10,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_14",	0x14,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_18",	0x18,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_1C",	0x1C,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_20",	0x20,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_24",	0x24,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_28",	0x28,	0x20000400,	-1,	4);
}

static system_tasks() 
{
	make_task_strucs();

	Tasks(0xDBAC8); 
}


