
#include <idc.idc>

static copy_func(source, dest, count)
{
	auto i, val;
	
	for(i = 0; i < count; i = i + 1 )
	{
		val = Byte(source + i);
		PatchByte(dest + i, val);
		PatchByte(source + i, 0);
	}
}

static main() 
{
	auto handle;
	auto val, source, dest, count, i;
	
	DelSeg(0x87D00000,0);
	SegCreate(0x87D00000,0x87DFFFFF,0,1,1,2);
	SegRename(0x87D00000,"RAM");
	SetSegmentType(0x87D00000,2);
		
	copy_func(0x1447F8, 0x87D05F00, 0xC42);
	copy_func(0x110000, 0x87DAE44C, 0x347F8);

	

}


