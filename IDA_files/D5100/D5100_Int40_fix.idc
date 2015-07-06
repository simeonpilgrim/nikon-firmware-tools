
#include <idc.idc>

static fixInt40( start, end, base)
{
	auto ea, offset, dest;
	auto str;
	
	ea = start;
	while( ea != BADADDR && ea != end)
	{
		offset = Word(ea);
		dest = base + offset;
		MakeWord(ea);
		add_dref(ea, dest, dr_I);
		
		OpOffEx(ea, 0, REF_OFF16, dest, base, 0x0);
		
		// todo make comment with address that was just linked to..
		str = sprintf("Diff %4x, R12 = %2x", (ea - base)&0xFFFF,  ((ea - base)/2)&0xFF);
		MakeComm(ea, str);
		//Message("%a - %s\n", ea, str);
		
		ea = ea + 2;
	}
}

static main() 
{
	fixInt40(0x4070A, 0x4086A, 0x4086A);
}


