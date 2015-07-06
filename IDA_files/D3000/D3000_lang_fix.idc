
#include <idc.idc>

static LangFix(start, end)
{
	auto last, ea, ref;
	
	last = 0xffffffff;
	ea = start;
	while( ea != BADADDR && ea < end )
	{
		ref = Dword(ea);
		if( ref > last )
		{
			MakeStr(last, ref);
			OpOff(ea, 0, 0);			
		}
		
		last = ref;
		ea = ea + 4;
	}
}


static main() 
{
	// English
	LangFix(0x429208, 0x42ABC8);

	LangFix(0x419158, 0x41AB18); // German
	LangFix(0x43617C, 0x437B3C); // ??
	
	Message("Lang Fix: Done\n");
}


