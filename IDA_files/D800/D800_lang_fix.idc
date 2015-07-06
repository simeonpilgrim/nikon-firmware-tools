
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
	LangFix(0xD13A64, 0xD17174);


	
	Message("Lang Fix: Done\n");
}


