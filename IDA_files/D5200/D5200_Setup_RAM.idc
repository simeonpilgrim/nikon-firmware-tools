
#include <idc.idc>


static mem_cpy(source, dest, count)
{
	auto i, val;
	
	for(i = 0; i < count; i = i + 4 )
	{
		val = Dword(source + i);
		PatchDword(dest + i, val);
	}
}

static setup_Segs()
{
	auto fp;
	
	SegCreate(0x10000000,0x1013ffff,0,1,1,2);
	SegRename(0x10000000,"RAM0");
	SegClass (0x10000000,"CODE");
	SetSegmentType(0x10000000,2);

	// 0x50020000 - 0x515B9CEA
	SegCreate(0x50020000,0x515B9CEA,0,1,1,2);
	SegRename(0x50020000,"ROM");
	SegClass (0x50020000,"CODE");
	SetSegmentType(0x50020000,2);
	
	//SegCreate(0x5159F65C,0x515B9CEA,0,1,1,2);
	//SegRename(0x5159F65C,"ROM1");
	//SegClass (0x5159F65C,"CODE");
	//SetSegmentType(0x5159F65C,2);
}

static load_file_chunk(filename, mem_base, mem_src, mem_dst, length)
{
	auto fp, file_off, res;
	fp = fopen(filename, "rb");
	if( fp != 0 )
	{
	file_off = mem_src - mem_base;
	
	res = loadfile(fp, file_off, mem_dst, length );
	
	Message("Load: S: %x d: %x l: %x %d\n", file_off, mem_dst, length, res);
	
	fclose(fp);
	}
	else
	{
		Message("File open failed: '%s'\n", filename);
	}
}

static load_file(filename)
{
	load_file_chunk(filename, 0x50020000, 0x50020000, 0x50020000, 0x0146DAC0 );
	load_file_chunk(filename, 0x50020000, 0x5148DAC0, 0x10000000, 0x3C758 );
	load_file_chunk(filename, 0x50020000, 0x514CA218, 0x1003C758, 0x10E4 );
	load_file_chunk(filename, 0x50020000, 0x5159F660, 0x10111F20, 0x1A5EC );
	load_file_chunk(filename, 0x50020000, 0x514CB300, 0x1003D840, 0xD435C );
	load_file_chunk(filename, 0x50020000, 0x510BDB3C, 0x102FEA9C, 0xCE );
}


static GenInfo(void) {

    DeleteAll();    // purge database
	SetPrcsr("ARM");
	SetCharPrm(INF_COMPILER, 0);
	StringStp(0xA);
	Tabs(1);
	Comments(0);
	Voids(0);
	XrefShow(2);
	AutoShow(1);
	Indent(16);
	CmtIndent(40);
	TailDepth(0x10);
}

static main() 
{
	GenInfo();
	setup_Segs();
	load_file("C:\\Users\\spilgrim\\Downloads\\Nikon\\D5200Update\\b970101.bin");
	

	//mem_cpy(0x5148DAC0, 0x10000000, 0x3C758);
	//mem_cpy(0x514CA218, 0x1003C758, 0x10E4);
	//mem_cpy(0x5159F660, 0x10111F20, 0x1A5EC);
	
	//?mem_cpy(0x514CB300, 0x1003D840, 0xD435C);
	//?mem_cpy(0x510BDB3C, 0x102FEA9C, 0xCE);
	
	//mem_cpy(0x502C0000, 0x12700000, 0x4000);
	

}


