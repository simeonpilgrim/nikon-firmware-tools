
#include <idc.idc>


static EntryA(loc)
{
	auto s1, s2;
	
	s1 = Dword(loc+8);	
	s2 = Dword(loc+12);
	
	MakeUnknown(loc, 0x10, 0 );
	MakeStructEx(loc, 0x10, "struc_1");
		
	if( s1 != 0 )
	{
		MakeCode(s1);
		AutoMark(s1, AU_PROC);
		//MakeNameEx(s1,set_str,0);
	}
	
	if( s2 != 0 )
	{
		MakeCode(s2);
		AutoMark(s2, AU_PROC);
		//MakeNameEx(s2,get_str,0);
	}
}

static EntryB(loc)
{
	auto s1;
	
	s1 = Dword(loc+8);	
	
	MakeUnknown(loc, 0xC, 0 );
	MakeStructEx(loc, 0xC, "struc_2");
		
	if( s1 != 0 )
	{
		MakeCode(s1);
		AutoMark(s1, AU_PROC);
		//MakeNameEx(s1,set_str,0);
	}
	
}

static FixA(start, end)
{
	auto ea;
	
	ea= start;
	while( ea != BADADDR && ea < end )
	{
		EntryA(ea);
		ea = ea + 16;
	}
}

static FixB(start, end)
{
	auto ea;
	
	ea= start;
	while( ea != BADADDR && ea < end )
	{
		EntryB(ea);
		ea = ea + 12;
	}
}



static main() 
{
	// PTP D3000
	FixA(0x87DB2854, 0x87DB3D74); 

	FixB(0x87DAFA64, 0x87DB2854); 

	Message("Lang Fix: Done\n");
}


