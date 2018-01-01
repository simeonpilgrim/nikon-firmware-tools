
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
	
	SegCreate(0x40000000,0x4050ffff,0,1,1,2);
	SegRename(0x40000000,"RAM0");
	SegClass (0x40000000,"CODE");
	SetSegmentType(0x40000000,2);

	SegCreate(0x10020000,0x116F6F06,0,1,1,2);
	SegRename(0x10020000,"ROM");
	SegClass (0x10020000,"CODE");
	SetSegmentType(0x10020000,2);
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
	load_file_chunk(filename, 0x10020000, 0x10020000, 0x10020000, 0x015FFBEC );
	load_file_chunk(filename, 0x10020000, 0x1161FBEC, 0x40000000, 0x2F0B8 );
	load_file_chunk(filename, 0x10020000, 0x1164ECA4, 0x1164ECA4, 0x1C ); 
    
	load_file_chunk(filename, 0x10020000, 0x1164ECC0, 0x4002F0C0, 0x123B8 );
  	load_file_chunk(filename, 0x10020000, 0x11661078, 0x11661078, 0x3A188 );
    
 	load_file_chunk(filename, 0x10020000, 0x1169B200, 0x4007B600, 0x8 );   
	load_file_chunk(filename, 0x10020000, 0x1169B208, 0x4007BA88, 0x5B87C ); 
	load_file_chunk(filename, 0x10020000, 0x116F6A84, 0x116F6A84, 0x482 );
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
	load_file("C:\\Users\\Simeon\\Downloads\\Nikon\\D5Update\\b870_120c.bin");

    //10C3A4C8 LANG_table
    //101BAD86 code that uses LANG_table T=1
}


