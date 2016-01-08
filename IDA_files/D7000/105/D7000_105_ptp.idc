
#include <idc.idc>
#include "ptp_code_names.idc"


static PtpEntryA(loc)
{
	auto w;
	auto s1, s2;
	auto id;
	auto id_str,get_str,set_str,code_str;
	
	w = Dword(loc);	
	s1 = Dword(loc+4);	
	s2 = Dword(loc+8);
	
	id = Byte(w)+(Byte(w+1)<<8);
	code_str = PtpCodeTxt(id);
	id_str = sprintf("ptp_DPC_%s", code_str);
	get_str = sprintf("ptpGet_%s", code_str);
	set_str = sprintf("ptpSet_%s", code_str);
	
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
	
	//Message("Ptp %x %x %s %s %s\n", loc, id, id_str, get_str, set_str);
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
	
	
	//MakeDword(loc);
	//OpOff(loc+4, 0, 0);

	// Fixup entry
	MakeUnknown(loc, 0x8, 0 );
	MakeStructEx(loc, 0x8, "ptp_entry");
	
	if( s != 0 )
	{
		fun_str = sprintf("ptp_OC_%s", PtpServiceCodeTxt(w));

		MakeCode(s);
		AutoMark(s, AU_PROC);
		MakeNameEx(s,fun_str,0);
	}
		
	//Message("Ptp %x %x %s\n", loc, w, fun_str);
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

static make_strucs()
{
    auto id, mid;
	
	id = AddStrucEx(-1,"ptp_entry",0);
	id = GetStrucIdByName("ptp_entry");
	mid = AddStrucMember(id,"field_0",	0,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"field_2",	0x2,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"func_ptr",	0x4,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
}

static main() 
{
	Message("PTP Fix: Start\n");
	make_strucs();
	
	// PTP Get/Set Function Table
	PtpFixA(0x231168, 0x231B40);
	
	// PTP D7000 1.05
	PtpFixB(0x22FD5C, 0x22FFDC, "Fxxx"); 

	Message("PTP Fix: Done\n");
}


