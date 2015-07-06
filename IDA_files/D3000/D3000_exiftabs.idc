
#include <idc.idc>

static ExifFix(start, end, pref)
{
	auto exif, ea, fun, str;
	
	ea = start;

	while( ea != BADADDR && ea < end )
	{
		MakeUnknown(ea, 0x10, 0 );
		MakeStructEx(ea, 0x10, "exif_tab");
		
		exif = Word(ea);
		fun = Dword(ea+12);
		
		if( fun != 0 )
		{
			str = sprintf("exif_%d_%04X", pref, exif);

			MakeCode(fun);
			AutoMark(fun, AU_PROC);
			MakeNameEx(fun, str,0);		
		}
		
		ea = ea + 16;
	}
}


static main() 
{
	ExifFix(0xBD770, 0xBD7E0, 0);
	ExifFix(0xBD7E0, 0xBDC90, 1);
	ExifFix(0xBDC90, 0xBDCB0, 2);
	ExifFix(0xBDCB0, 0xBE020, 3);
	ExifFix(0xBE020, 0xBE290, 4);

	
	Message("Exif Tab: Done\n");
}


