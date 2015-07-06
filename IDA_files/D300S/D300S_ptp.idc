
#include <idc.idc>


static PtpEntryA(loc)
{
	auto w;
	auto s1, s2;
	auto id;
	auto id_str,get_str,set_str;
	
	w = Dword(loc);	
	s1 = Dword(loc+4);	
	s2 = Dword(loc+8);
	
	id = Byte(w)+(Byte(w+1)<<8);
	
	id_str = sprintf("ptp_%4X", id);
	get_str = sprintf("ptpGet_%4X", id);
	set_str = sprintf("ptpSet_%4X", id);
	
	
	OpOff(loc, 0, 0);
	OpOff(loc+4, 0, 0);
	OpOff(loc+8, 0, 0);
	
	MakeWord(w);
	MakeNameEx(w,id_str,0);
	
	if( s1 != 0 )
	{
		MakeCode(s1);
		MakeNameEx(s1,set_str,0);
	}
	
	if( s2 != 0 )
	{
		MakeCode(s2);
		MakeNameEx(s2,get_str,0);
	}
	
	Message("Ptp %x %x %s %s %s\n", loc, id, id_str, get_str, set_str);
}

static PtpFixA(start, end)
{
	auto ea;
	
	ea= start;
	while( ea != BADADDR && ea < end )
	{
		PtpEntryA(ea);
		ea = ea +12;
	}
}

static PtpEntryB(loc)
{
	auto w;
	auto s;
	auto fun_str;
	
	w = Word(loc);	
	s = Dword(loc+4);	
	
	fun_str = sprintf("ptp_0C_%4X", w);
	
	MakeDword(loc);
	OpOff(loc+4, 0, 0);

	if( s != 0 )
	{
		MakeCode(s);
		MakeNameEx(s,fun_str,0);
	}
		
	Message("Ptp %x %x %s\n", loc, w, fun_str);
}

static PtpFixB(start, end, tab)
{
	auto ea;
	auto str;
	
	str = sprintf("ptp_%s_tab", tab);
	MakeNameEx(start,str,0);

	ea= start;
	while( ea != BADADDR && ea < end )
	{
		PtpEntryB(ea);
		ea = ea +8;
	}
}


static main() 
{
	// PTP D300S
	PtpFixA(0x2024C8, 0x2025AC); // 5xxx table
	PtpFixA(0x2025AC, 0x202EDC); // Dxxx table

	PtpFixB(0x2014B8, 0x2014D0, "91xx");
	PtpFixB(0x2014D0, 0x2014F8, "98xx"); 
	PtpFixB(0x2014F8, 0x2015A8, "92xx"); 
	PtpFixB(0x2015A8, 0x201648, "10xx"); 
	PtpFixB(0x201B28, 0x201D50, "Fxxx"); 

	Message("Lang Fix: Done\n");
}


