
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
		AutoMark(s1, AU_PROC);
		MakeNameEx(s1,set_str,0);
	}
	
	if( s2 != 0 )
	{
		MakeCode(s2);
		AutoMark(s2, AU_PROC);
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
	
	fun_str = sprintf("ptp_OC_%4X", w);
	
	MakeDword(loc);
	OpOff(loc+4, 0, 0);

	if( s != 0 )
	{
		MakeCode(s);
		AutoMark(s, AU_PROC);
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


static PtpEntryC(loc)
{
	auto w;
	auto s1, s2;
	auto id;
	auto id_str,fun_str;
	
	w = Dword(loc);	
	s1 = Dword(loc+4);	
	
	id = Byte(w)+(Byte(w+1)<<8);
	
	id_str = sprintf("ptp_%4X", id);
	fun_str = sprintf("ptpGet_%4X", id);
	
	
	OpOff(loc, 0, 0);
	OpOff(loc+4, 0, 0);
	
	MakeWord(w);
	MakeNameEx(w,id_str,0);
	
	if( s1 != 0 )
	{
		MakeCode(s1);
		AutoMark(s1, AU_PROC);
		MakeNameEx(s1,fun_str,0);
	}	
}

static PtpFixC(start, end)
{
	auto ea;
	
	ea= start;
	while( ea != BADADDR && ea < end )
	{
		PtpEntryC(ea);
		ea = ea +12;
	}
}

static main() 
{
	// PTP D3000
	//PtpFixA(0xD0CE0, 0xD0DAC); // 5xxx table
	//PtpFixA(0xD0DAC, 0xD0E78); // Dxxx table

	//PtpFixB(0xD0A9C, 0xD0AF4, "90xx");
	//PtpFixB(0xD0A74, 0xD0A9C, "98xx"); 
	//PtpFixB(0xD0AF4, 0xD0B94, "10xx"); 
	//PtpFixB(0xCFEF8, 0xD0070, "Fxxx"); 

	PtpFixC(0xD061C, 0xD0754); 

	Message("Lang Fix: Done\n");
}


