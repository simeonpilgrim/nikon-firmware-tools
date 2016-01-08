
#include <idc.idc>
#include "D7000_105_system_tables_tasks.idc"

static make_sys_strucs()
{
    auto id, mid;
	
	id = AddStrucEx(-1,"table_header",0);
	id = GetStrucIdByName("table_header");
	mid = AddStrucMember(id,"count",	0x0,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"size",	0x2,	0x10000400,	-1,	2);
}

static main() 
{
	make_sys_strucs();

	system_tasks(); 
}


