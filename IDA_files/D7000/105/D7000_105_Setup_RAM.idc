
#include <idc.idc>


static setup_Segs()
{
	auto fp;
	
	SegCreate(0X40000,0X40000+0xA0000,0,1,1,2);
	SegRename(0X40000,"ROM_A");
	SegClass (0X40000,"CODE");
	SetSegmentType(0X40000,2);
	
	SegCreate(0XE0000,0XE0000+0X20000,0,1,1,2);
	SegRename(0XE0000,"BOOT_ROM");
	SegClass (0XE0000,"CODE");
	SetSegmentType(0XE0000,2);
	
	SegCreate(0X100000,0X100000+0X780000,0,1,1,2);
	SegRename(0X100000,"ROM_B");
	SegClass (0X100000,"CODE");
	SetSegmentType(0X100000,2);
	
	SegCreate(0X68000000,0X6800FFFF,0,1,1,2);
	SegRename(0X68000000,"STACK");
	SegClass (0X68000000,"DATA");
	SetSegmentType(0X68000000,2);
	
	SegCreate(0x8F800000,0x8F800000+0x1AC3F8+0xA50,0,1,1,2);
	SegRename(0x8F800000,"RAM");
	SegClass (0x8F800000,"DATA");
	SetSegmentType(0x8F800000,2);

	SegCreate(0x8F9ACE48, 0x8F9ACE48+0x1D3F4, 0,1,1,2);
	SegRename(0x8F9ACE48,"RAM");
	SegClass (0x8F9ACE48,"CODE");
	SetSegmentType(0x8F9ACE48,2);	
	
	LowVoids(0x20);
	HighVoids(0x8F9FFFFF);
}

static load_file_chunk(filename, mem_base, mem_src, mem_dst, length, block_name)
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
	
	if(mem_src != mem_dst)
	{
		// mark source as data.
		MakeData(mem_src, FF_BYTE, 1, 0 );
		MakeArray(mem_src, length);

		MakeNameEx(mem_src, block_name, 0);
	}
	else
	{
		MakeComm(mem_src, block_name);
	}
}

static load_file(filename)
{
	load_file_chunk(filename, 0x40000, 0x40000, 0x40000, 0xA0000, "rom code part A" );
	load_file_chunk(filename, 0x40000, 0x100000, 0x100000, 0x368B14-0x100000, "rom code part B" );
	load_file_chunk(filename, 0x40000, 0x368B14, 0x8F9ACE48, 0x1D3F4, "RAM_Code_SRC" );
	load_file_chunk(filename, 0x40000, 0x385F08, 0x8F800000, 0xA50, "RAM_Data_SRC" );
	load_file_chunk(filename, 0x40000, 0x386958, 0x386958, 0x880000-0x386958, "rom code part C");
}

static setup_int_table(base_addr)
{
	auto addr, ref;
	auto i, str, id, name;
	
	for(i = 0; i < 256; i++)
	{
		id = 255 - i;
		addr = base_addr + (id *4);
		
		ref = Dword(addr);
		MakeDword(addr);
		
		str = sprintf("int_%02X", i);
		MakeComm( addr, str);
				
		if(ref != 0)
		{
			MakeCode(ref);
			AutoMark(ref, AU_PROC);
			
			OpOff(addr, 0, 0);
			
			name = NameEx(BADADDR, ref);
			Message("name: '%s' '%s'\n", name, substr(name,0,4));
			if(substr(name,0,4) != "int_")
			{
				MakeNameEx(ref, str, 0);
			}
			//MakeNameEx(ref, "", 0); //cleanup
		}	
	}
	
}

static GenInfo(void) {

    DeleteAll();    // purge database
	SetPrcsr("fr");
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
	// set 'loading idc file' mode
	SetCharPrm(INF_GENFLAGS, INFFL_LOADIDC|GetCharPrm(INF_GENFLAGS));

	GenInfo();
	setup_Segs();

	load_file("b750105a.bin");

	// clear 'loading idc file' mode
	SetCharPrm(INF_GENFLAGS, ~INFFL_LOADIDC&GetCharPrm(INF_GENFLAGS));

	setup_int_table(0xDFC00); // set via 'mov     r0, tbr' 
	MakeCode	(0X40000);
}


