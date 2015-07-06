
#include <idc.idc>

static png_chunk(start)
{
	auto ea, i, str;
	auto str_start, str_len;
	auto data_start, data_len;
	auto marker, marker_start;
	auto end = BADADDR;
	
	
	str_start = start + 4;
	str_len = Dword(start);
	data_start = str_start + str_len + 4;
	data_len = Dword(data_start - 4);
	marker_start = data_start + data_len - 4;
	marker = Dword(marker_start);
	
	Message("   %x %x %x %x\n", start, str_len, data_len, marker);

		MakeDword(start);
		MakeStr(str_start, str_start + str_len);
		MakeDword(data_start - 4);
		
		MakeUnknown(data_start, data_len, DOUNK_SIMPLE);
		MakeData(data_start, FF_BYTE, 1, 0 );
		MakeArray(data_start, data_len);
		
		str = sprintf("png_block_%x",start);
		MakeNameEx(start, str, 0);
		end = data_start + data_len;
	
	
	return end;
}

static png_block(start, length)
{
	auto ea, count, i, end;
	
	if( start == 0 || length == 0 )
	{
		return;
	}
	
	Message(" png_block %x\n", start);
	

	end = start + length;
	
	count = Dword(start);
	
	if( count > 0x100 )
	{
		Message("   %x counter > 0x1000 %x\n", start, count);
		return;
	}
	MakeDword(start);
	
	i =0;
	start = start + 4;
	while( i < count && start != BADADDR && start < end )
	{
		start = png_chunk(start);
		i = i +1;
	}
}

static pointerRun(start, count)
{
	auto i, ea, ref;
	
	if( start == 0 || count == 0 ) return;

	i = 0;
	while(i<count)
	{
		ea = start + (i * 4);
		ref = Dword(ea);
		MakeDword(ea);
		if( ref != 0 )
		{
			OpOff(ea, 0, 0);
		}
		i = i + 1;
	}

}

static struc_8(start)
{
	auto s8p, t0p, t0l, t1p, t1l, t2p, t2l;
	
	MakeUnknown(start, 0x1C, 0 );
	MakeStructEx(start, 0x1C, "struc_8");
	
	t0p = Dword(start+0x00);
	t0l = Dword(start+0x04);
	t1p = Dword(start+0x08);
	t1l = Dword(start+0x0c);
	t2p = Dword(start+0x10);
	t2l = Dword(start+0x14);

	pointerRun(t0p, t0l);
	pointerRun(t1p, t1l);
	pointerRun(t2p, t2l);

	
}

static CompDword(start, offset, exp_val)
{
	auto val;
	val = Dword(start + offset);
	if( val != exp_val)
	{
		Message("%x %x %x != %x\n", start, offset, val, exp_val);
	}
}

static block_A(start, len)
{
	auto s, slen, off;
	auto fp, i;
	if( start == 0 ) return;
	s = sprintf("blockA_0x%X.bin",start);
	

	CompDword(start, 0x00, 0x100b0004);
	CompDword(start, 0x04, 0x1);
	CompDword(start, 0x08, 0xFFFFFFFF);
	CompDword(start, 0x0c, 0x0);
	CompDword(start, 0x10, 0x0);
	CompDword(start, 0x14, 0x280);
	CompDword(start, 0x18, 0x1E0);
	CompDword(start, 0x1C, 0x1E);
	CompDword(start, 0x20, 0x0);
	CompDword(start, 0x24, 0x0);
	CompDword(start, 0x28, 0x0);
	CompDword(start, 0x2C, 0xFFFFFFFF);
	CompDword(start, 0x30, 0x2);
	CompDword(start, 0x34, 0x0);
	
	
	slen = Dword(start + 0x38);
	s = GetManyBytes( start + 0x3c, slen, 0 );
	off = 0x3c + slen;
	
	CompDword(start, off, 0x0);
	CompDword(start, off+ 4, 0x1);
	CompDword(start, off+ 8, 0xA);
	
	//Message("%x %d '%s'\n", start, slen, s);
}

static struc_10(start)
{
	auto s8p, t0p, t0l, pt1p, pt1l, pt2p, pt2l,pt3p, pt3l,pt4p, pt4l;
	
	
	MakeUnknown(start, 0x2C, 0 );
	MakeStructEx(start, 0x2C, "struc_10");

	t0p = Dword(start+0x00);
	t0l = Dword(start+0x04);
	pt1p = Dword(start+0x08);
	pt1l = Dword(start+0x0c);
	pt2p = Dword(start+0x10);
	pt2l = Dword(start+0x14);
	pt3p = Dword(start+0x18);
	pt3l = Dword(start+0x1C);
	pt4p = Dword(start+0x20);
	pt4l = Dword(start+0x24);
	s8p = Dword(start+0x28);
	
	struc_8(s8p);
	
	block_A(t0p, t0l);
	
	png_block(pt1p, pt1l);
	png_block(pt2p, pt2l);
	png_block(pt3p, pt3l);
	png_block(pt4p, pt4l);	
}

static struc_10_range(start, end)
{
	auto ea;
	ea = start;
	while( ea < end)
	{
		struc_10(ea);
		ea = ea + 0x2c;
	}
}

static main() 
{
	Message("Mark - Start\n");
	struc_10_range(0x50E54BD0, 0x50E5E67C);
	
	
	Message("Mark - End\n");
}


