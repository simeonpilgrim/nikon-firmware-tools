
#include <idc.idc>


static main() 
{
	auto handle;
	auto val, source, dest, count, i;

	
	Message("Copy 393CF8: Start\n");
	
	count = 0xA9C;
	source = 0x393CF8;
	dest = 0x8F800000;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 393CF8: End\n");
}


