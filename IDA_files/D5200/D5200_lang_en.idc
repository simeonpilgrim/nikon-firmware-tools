
#include <idc.idc>

static LangFix(start, end, base)
{
	auto ea, last, off, len;
	
	ea = start;
	last = 0;
	while( ea != BADADDR && ea < end )
	{
		off = Dword(ea);
		MakeDword	(ea);
		len = off - last;
		if( len > 0 )
		{
			MakeStr(last + base, off+base);
			OpOff(ea, 0, base);
		}
		
		last = off;
		ea = ea + 0x04;
	}
}

static main() 
{
	//LangFix(0x50376578, 0x50377BDC, 0x50377BDC); // Japanese
	//LangFix(0x50385e34, 0x50387498, 0x50387498); // English
	//LangFix(0x503A03EC, 0x503A1A50, 0x503A1A50); // German
	//LangFix(0x503C246C, 0x503C3AD0, 0x503C3AD0); // French
	
	Message("Lang Fix: Done\n");
}


