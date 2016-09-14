
#include <idc.idc>

static try_make_func(start, name, type)
{	
	auto addr;

	if(start == 0) 
		return;

	addr = start & 0xFFffFFfe;
	if( addr != start )
	{
		SetReg(addr, "T", 1);
	}

	if(strlen(name) > 0 ){
		MakeNameEx(addr, name, 0 );
	}
	MakeFunction(addr, BADADDR);
	
	if(strlen(type) > 0 ){
		SetType(addr, type);
	}	

}

static main() 
{
	try_make_func(0x90136173, "check_ram_valid", "int __cdecl sub_90136172(unsigned int ptr);");
	try_make_func(0x901A9EB2, "ptp_send_results", "int ptp_SendResults(ptp_obj *ptp, unsigned __int16 responceCode, int p3, int p4)")
	
}


