
#include <idc.idc>



static setup_Segs()
{
	SegCreate(0x10000000,0x11FFffff,0,1,1,2);
	SegRename(0x10000000,"RAM0");
	SegClass (0x10000000,"DATA");
	SetSegmentType(0x10000000,2);

	// 0x90020000 - 0x913AAAE2
	SegCreate(0x90020000,0x913AAAE2,0,1,1,2);
	SegRename(0x90020000,"ROM");
	SegClass (0x90020000,"DATA");
	SetSegmentType(0x90020000,2);
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
	load_file_chunk(filename, 0x90020000, 0x90020000, 0x90020000, 0x012967E0 );
	load_file_chunk(filename, 0x90020000, 0x912B67E0, 0x10000000, 0x30344 );
	load_file_chunk(filename, 0x90020000, 0x912E6B24, 0x10030344, 0xF94A );
	load_file_chunk(filename, 0x90020000, 0x91357800, 0x100A1400, 0x532CC );

}


static GenInfo(void) {

    DeleteAll();    // purge database
	SetPrcsr("ARM");
	SetCharPrm(INF_COMPILER, 6);
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
	load_file("C:\\Users\\Simeon\\Downloads\\Nikon\\D750Update\\b1010_102.bin");
		

}


