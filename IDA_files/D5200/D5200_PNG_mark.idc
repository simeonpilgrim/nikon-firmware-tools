
#include <idc.idc>


static main() 
{
	auto start, ea, eb, last, len, str;
	
	Message("PNG Mark - Start\n");
	
	last = 0;
	start = 0x50120000;
	ea = FindBinary(start, SEARCH_DOWN, "89 50 4E 47");
	while( ea != BADADDR && ea != last)
	{
		eb = FindBinary(ea, SEARCH_DOWN, "49 45 4e 44");
		last = ea;
				
		if( eb != BADADDR )
		{
			eb = eb + 4;
			len = eb - ea;
			
			Message("%x %x %x\n", ea, eb, len);
			if( len < 0x1000)
			{
				MakeData(ea, FF_BYTE, 1, 0 );
				MakeArray(ea, len);
				str = sprintf("png_%x",ea);
				//Message(str);
				MakeNameEx(ea, str, 0);
			}
		}
	
		ea = FindBinary(ea + 4, SEARCH_DOWN, "89 50 4E 47");
	}

	Message("PNG Mark - End\n");
}


