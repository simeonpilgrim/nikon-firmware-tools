
#include <idc.idc>


static ExifEntry(loc, text)
{
	auto id, code, ea, size, str, fun;
	
	// Fixup entry
	MakeUnknown(loc, 0x10, 0 );
	MakeStructEx(loc, 0x10, "exif_ent");
	
	code = Word(loc);
	fun = Dword(loc+0xc);
		
	if( code != 0 && fun != 0 )
	{
		str = sprintf("exiftag%s_%04X", text, code);
		MakeNameEx(fun,str,0);
		MakeCode(fun);
		AutoMark(fun, AU_PROC);	
	}
}

static ExifEntry2(loc, block)
{
	auto id, code, ea, size, str, fun;
	
	// Fixup entry
	MakeUnknown(loc, 0x08, 0 );
	MakeStructEx(loc, 0x08, "struc_12");
	
	code = Word(loc);
	fun = Dword(loc+0x4);
		
	if( fun != 0 )
	{
		str = sprintf("exiftag_%02X_%04X", block, code);
		MakeNameEx(fun,str,0);
		MakeCode(fun);
		AutoMark(fun, AU_PROC);	
	}
}

static Exif(start, end, text)
{
	auto ea;
	ea = start;
	while( ea != BADADDR && ea < end )
	{
		ExifEntry(ea,text);
		ea = ea + 0x10;
	}
}

static Exif2(start, end)
{
	auto ea, block;
	ea = start;
	block = 0;
	while( ea != BADADDR && ea < end )
	{
		ExifEntry2(ea, block);
		if( Dword(ea) == 0 && Dword(ea+4) == 0 ) {
			block = block + 1;
		}
		ea = ea + 0x8;
	}
}


static main() 
{
	Exif(0x2AE448, 0x2AE6C8, "_A"); 
	Exif(0x2AE0C8, 0x2AE448, "_B"); 
	Exif(0x2ADED8, 0x2AE0C8, "_C"); 
	Exif(0x2ADEB8, 0x2ADED8, "_D"); 
	Exif(0x2AD9E8, 0x2ADEB8, "_E"); 
	Exif(0x2AD978, 0x2AD9E8, "_F"); 

	Exif2(0x2AC85C, 0x2AD19C);
}


