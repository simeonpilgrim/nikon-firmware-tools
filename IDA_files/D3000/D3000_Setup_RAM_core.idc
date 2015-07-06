
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
	
	DelSeg(0x80000000,0);
	SegCreate(0x80000000,0x800FFFFF,0,1,1,2);
	SegRename(0x80000000,"RAM");
	SetSegmentType(0x80000000,2);
	
	DelSeg(0x80200000,0);
	SegCreate(0x80200000,0x802FFFFF,0,1,1,2);
	SegRename(0x80200000,"RAM");
	SetSegmentType(0x80200000,2);
	
	copy_func(0xF0478, 0x80200000, 0x1CC);
	copy_func(0xF24C4, 0x80082280, 0xD542);
	copy_func(0xF1924, 0x800812E0, 0xB9E);
	copy_func(0xF0730, 0x800800EC, 0x11F2);
	copy_func(0xF0724, 0x800800E0, 0xC);
	copy_func(0xFFC00, 0x80081E80, 0x400);
	

}


