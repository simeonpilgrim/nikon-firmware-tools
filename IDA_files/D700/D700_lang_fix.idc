
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
			Message("Text %x %x \n", last, ref - last);
			MakeStr(last, ref);
			OpOff(ea, 0, 0);			
		}
		
		last = ref;
		ea = ea + 4;
	}
}

static LangFixOff(base, start, end)
{
	auto ea;
	auto val, ref, cm;
	auto str;
	
	ea= start;
	while( ea != BADADDR && ea < end )
	{
		val = Word(ea);
		ref = ((val & 0x1FFF) << 2) + base;
		
		//add_dref(ea, ref, dr_O);
		cm = Dword(ref);
		if( cm > 0x40000 && cm < 0xb00000)
		{
			MakeComm( ea, NameEx(BADADDR, cm));
			//Message(str);
		}
		ea = ea +2;
	}
}


static main() 
{
	// English
	LangFix(0x3FF7E0, 0x401C20);

	
	Message("Lang Fix: Done\n");
}


