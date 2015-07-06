
#include <idc.idc>


static main() 
{
	auto handle;
	auto val, source, dest, count, i;
	
	
	count = 0x16E98;
	source = 0x466E10;
	dest = 0x9E50BEAC;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
		//PatchDword(source + i, 0);
		//MakeData(source+ i, FF_BYTE, 1, 0 );
	}
	
	
	count = 0x116E;
	source = 0x48ED74;
	dest = 0x9F1A4C90;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
		//PatchDword(source + i, 0);
		//MakeData(source+ i, FF_BYTE, 1, 0 );
	}
}


