
#include <idc.idc>

static mem_cpy(source, dest, count )
{
	auto i, val;
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
}


static main() 
{
	auto handle;

	Message("ROM Fix: Start\n");
	
	handle = fopen("C:\\Users\\spilgrim\\Downloads\\Nikon\\D5100Update\\hack9\\mem_0_0e0000.bin", "rb");
	if( handle != 0 )
	{
		Message("handle valid\n");
		
		loadfile(handle, 0xE4, 0xe0100, 0xFF00);
		fclose(handle);
	}
		
	handle = fopen("C:\\Users\\spilgrim\\Downloads\\Nikon\\D5100Update\\hack9\\mem_0_0f0000.bin", "rb");
	if( handle != 0 )
	{
		Message("handle valid\n");
		
		loadfile(handle, 0xE4, 0xf0100, 0xFF00);
		fclose(handle);
	}
	
	DelSeg(0x80000000,0);
	SegCreate(0x80000000,0x800FFFFF,0,1,1,2);
	SegRename(0x80000000,"RAM");
	SetSegmentType(0x80000000,2);

	
	mem_cpy(0xE1FA8, 0x800813D0, 0xB4D2);
	mem_cpy(0xE1A3C, 0x8008C8A4, 0x56A);
	mem_cpy(0xE066C, 0x80080000, 0xE0);
	mem_cpy(0xE074C, 0x800800E0, 0x12F0);
	mem_cpy(0xFFC00, 0x8008CE10, 0x400);
	


	Message("ROM Fix: Done\n");
}


