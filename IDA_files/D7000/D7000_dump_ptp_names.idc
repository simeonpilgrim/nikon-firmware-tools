
#include <idc.idc>

static PtpDumpEntryA(loc)
{
	auto w;
	auto id;
	auto id_str, name,code;
	
	w = Dword(loc);	
	id = Byte(w)+(Byte(w+1)<<8);

	//Message("	0x%08X 0x%08X 0x%04X\n", loc, w, id);

	name = NameEx(BADADDR, w);
	
	id_str = substr(name,0,7);
	
	//Message("	'%s' '%s' '%s'\n", name, id_str, substr(name, 8,-1));

	if(id_str == "PTP_DPC") 
	{
		id_str = substr(name, 8,-1);
		Message("	else if( code == 0x%04X )\n		str = \"%s\";\n", id, id_str);
	}
}

static PtpDumpA(start, end)
{
	auto ea;
	
	ea= start;
	while( ea != BADADDR && ea < end )
	{
		PtpDumpEntryA(ea);
		ea = ea +12;
	}
}

static main() 
{
	PtpDumpA(0x230514, 0x230EEC);
}


