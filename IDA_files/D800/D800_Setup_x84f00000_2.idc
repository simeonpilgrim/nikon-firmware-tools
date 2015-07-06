
#include <idc.idc>


static main() 
{
	auto handle;
	auto val, source, dest, count, i;
	
	
	Message("Copy 4EF4F4: Start\n");
	
	count = 0x11020;
	source = 0x4EF4F4;
	dest = 0x84F436B4;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
	
	Message("Copy 4EF4F4: End\n");

}


// 0x4CF960 + 0x1FB98 = 0x4EF4F8
// 0x84F23B20 + 0x1FB98 = 0x84F436B8
// 84F49F14 ME 0x263f4 beyound 0x84F23B20

