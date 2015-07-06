
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

	f2_ea = Dword(loc+0x0c);
	f3_ea = Dword(loc+0x10);
	
	if( f0_ea != 0 )
	{
		str = sprintf("ptp_OC_A_%04X", id);
		MakeNameEx(f0_ea,str,0);
		MakeCode(f0_ea);
		AutoMark(f0_ea, AU_PROC);
	}
	if( f2_ea != 0 )
	{
		str = sprintf("ptp_OC_B_%04X", id);
		MakeNameEx(f2_ea,str,0);
		MakeCode(f2_ea);
		AutoMark(f2_ea, AU_PROC);
	}
	if( f3_ea != 0 )
	{
		str = sprintf("ptp_OC_%04X", id);
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




static main() 
{

	PTP_B(0x3836B0, 0x383D90); // Fxxx
	

	//PTP_B(0x3EF350, 0x3EF5BC); // 90xx
	//PTP_A(0x3EF0AC, 0x3EF23C); // 10xx
	

}


