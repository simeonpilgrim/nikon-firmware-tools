
#include <idc.idc>



static setup_Segs()
{
	auto fp;

	SegCreate(0x10000000,0x11FFffff,0,1,1,2);
	SegRename(0x10000000,"RAM0");
	SegClass (0x10000000,"DATA");
	SetSegmentType(0x10000000,2);

	// 0x90020000 - 0x913B3062
	SegCreate(0x90020000,0x913B3062,0,1,1,2);
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
	//913B3062 (file length 1393062)
	load_file_chunk(filename, 0x90020000, 0x90020000, 0x90020000, 0x0129EC08 );
	load_file_chunk(filename, 0x90020000, 0x912BEC08, 0x10000000, 0x30344 ); // 912BEC08 -> 912EEF4C
	load_file_chunk(filename, 0x90020000, 0x912EEF4C, 0x10030344, 0xF95A ); // 912EEF4C -> 912FE8A6
	load_file_chunk(filename, 0x90020000, 0x9135FC34, 0x100A1014, 0x4 );
	load_file_chunk(filename, 0x90020000, 0x9135FC40, 0x100A1400, 0x5340C );

	load_file_chunk(filename, 0x90020000, 0x90280000, 0x10500000, 0x4000 );
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
	load_file("C:\\Users\\Simeon Pilgrim\\Downloads\\D750Update\\b1010_110d.bin");
		

}


