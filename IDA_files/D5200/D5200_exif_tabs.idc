#include <idc.idc>




static process_func(id, txt, idx, start)
{	
	auto addr, str;

	addr = start & 0xFFffFFfe;
	if( addr != start )
	{
		SetReg(start, "T", 1);
	}
	MakeFunction(addr, BADADDR);
	str = sprintf("exif_%04X_%d_%s", id, idx, txt);
	MakeNameEx(addr,str,0);
	
}




static exif_table_a(start, end, txt)
{
	auto ea, id, r0, r1, r2, r3, r4;
	auto slen, str;
	
	slen = 0x18;
	ea = start;
	
	str = sprintf("ExitTagTable_%s", txt);
	MakeNameEx(start, str, 0);
	
	while (ea < end)
	{
		MakeUnknown(ea, slen, 0 );
		MakeStructEx(ea, slen, "exif_tab_a");
		id = Dword(ea);
		
		process_func(id, txt, 0, Dword(ea+0x04));
		process_func(id, txt, 1, Dword(ea+0x08));
		process_func(id, txt, 2, Dword(ea+0x0C));
		process_func(id, txt, 3, Dword(ea+0x10));
		process_func(id, txt, 4, Dword(ea+0x14));

		ea = ea + slen;
	}
	
}

static main() 
{
	Message("Exif - Start\n");
	
	exif_table_a(0x10127974, 0x10127C74, "1");
	exif_table_a(0x10127C74, 0x10127DF4, "0");
	exif_table_a(0x10127DF4, 0x101281E4, "Tiff");
	exif_table_a(0x101281E4, 0x101284E4, "Gps");
	exif_table_a(0x101284E4, 0x10128514, "2");
	exif_table_a(0x10128514, 0x101285BC, "3");
	exif_table_a(0x101285BC, 0x10128BD8, "4");
	
	exif_table_a(0x10128CA8, 0x10128D68, "5");
	exif_table_a(0x10128D68, 0x10128F00, "6");
	exif_table_a(0x10128F00, 0x10128FA8, "7");
	exif_table_a(0x10128FA8, 0x10129068, "8");
	exif_table_a(0x10129068, 0x10129080, "9");
	exif_table_a(0x10129080, 0x101290b0, "A");
	exif_table_a(0x101290b0, 0x101290f8, "B");
	
	Message("Exif - End\n");
}


