
#include <idc.idc>

static PtpCodeTxt(code)
{
	auto str;
	if( code == 0x5003 )
		str = "ImageSize";
	else
		str = sprintf("%04X", code);

	
	return str;
}

static PTPEntryA(loc)
{
	auto id, word_ea, f0_ea, f1_ea, size, str;
	
	// Fixup entry
	MakeUnknown(loc, 0x14, 0 );
	MakeStructEx(loc, 0x14, "ptp_ent_A");
	
	id = Word(loc);
	word_ea = Dword(loc+0x04);
	f0_ea = Dword(loc+0x0c);
	f1_ea = Dword(loc+0x10);
	
	if( word_ea != 0 )
	{
		str = sprintf("PTP_OC_%s", PtpCodeTxt(id));
		MakeNameEx(word_ea,str,0);
		MakeWord(word_ea);
	}
	
	if( f0_ea != 0 )
	{
		str = sprintf("ptpSet_%s", PtpCodeTxt(id));
		MakeNameEx(f0_ea,str,0);
		MakeCode(f0_ea);
		AutoMark(f0_ea, AU_PROC);
	}
	
	if( f1_ea != 0 )
	{
		str = sprintf("ptpGet_%s", PtpCodeTxt(id));
		MakeNameEx(f1_ea,str,0);
		MakeCode(f1_ea);
		AutoMark(f1_ea, AU_PROC);
	}
}

static PTPEntryB(loc)
{
	auto id, f0_ea, f1_ea, f2_ea, f3_ea, str;
	
	// Fixup entry
	MakeUnknown(loc, 0x14, 0 );
	MakeStructEx(loc, 0x14, "ptp_ent_B");
	
	id = Word(loc);

	f0_ea = Dword(loc+0x04);
	f1_ea = Dword(loc+0x08);
	f2_ea = Dword(loc+0x0c);
	f3_ea = Dword(loc+0x10);
	
	if( f0_ea != 0 )
	{
		str = sprintf("ptp_0C_A_%04X", id);
		MakeNameEx(f0_ea,str,0);
		MakeCode(f0_ea);
		AutoMark(f0_ea, AU_PROC);
	}
	if( f1_ea != 0 )
	{
		str = sprintf("ptp_0C_B_%04X", id);
		MakeNameEx(f1_ea,str,0);
		MakeCode(f1_ea);
		AutoMark(f1_ea, AU_PROC);
	}
	if( f2_ea != 0 )
	{
		str = sprintf("ptp_0C_C_%04X", id);
		MakeNameEx(f2_ea,str,0);
		MakeCode(f2_ea);
		AutoMark(f2_ea, AU_PROC);
	}
	if( f3_ea != 0 )
	{
		str = sprintf("ptp_0C_D_%04X", id);
		MakeNameEx(f3_ea,str,0);
		MakeCode(f3_ea);
		AutoMark(f3_ea, AU_PROC);
	}
}

static PTP_A(start, end)
{
	auto ea, count;
	ea = start;
	while( ea != BADADDR && ea < end )
	{
		PTPEntryA(ea);
		ea = ea + 0x14;
	}
}

static PTP_B(start, end)
{
	auto ea, count;
	ea = start;
	while( ea != BADADDR && ea < end )
	{
		PTPEntryB(ea);
		ea = ea + 0x14;
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
	//make_strucs();

	//PTP_A(0x3F0200, 0x3F0F20); 
	PTP_B(0x3EE69C, 0x3EED40); // Fxxx
	PTP_B(0x3EF350, 0x3EF5BC); // 90xx
	PTP_A(0x3EF0AC, 0x3EF23C); // 10xx
	

}


