
#include <idc.idc>

static FixFunc(loc, idx, count)
{
	auto ea;
	auto str, old;
	
	ea = Dword(loc + ( idx * 4));
	if( ea != 0 )
	{
		MakeCode(ea);
		AutoMark(ea, AU_PROC);

		str = sprintf("BFT%02d_s%d", count, idx);
		old = NameEx( BADADDR, ea );
		
		if( str[0] != old[0] ||
		str[1] != old[1] ||
		str[2] != old[2] ||
		(str[3] != old[3] && old[3] != 'x') ||
		(str[4] != old[4] && old[4] != 'x') ||
		(str[5] != old[5] && old[4] != 'x')  ) 
		{
			if( hasUserName( GetFlags(ea) ) == 0 )
			{
				MakeNameEx(ea, str, 0);
			}
			else
			{
				// make new BFTxx version
				str = sprintf("BFTxx_s%d_%X", idx, ea);
				MakeNameEx(ea,str,0);
			}
		}
	}
}


static BFTEntry(loc, count)
{
	auto str;
	
	// Fixup entry
	MakeUnknown(loc, 0x2C, 0 );
	MakeStructEx(loc, 0x2C, "unknown_xx");
	if( count > 0 )
	{
		str = sprintf("BFT%02d", count);
		MakeNameEx(loc,str,0);
	}
	
	FixFunc(loc, 1, count);
	FixFunc(loc, 2, count);
	FixFunc(loc, 3, count);
	FixFunc(loc, 4, count);
	FixFunc(loc, 5, count);
	FixFunc(loc, 6, count);
	FixFunc(loc, 7, count);
	FixFunc(loc, 8, count);
	FixFunc(loc, 9, count);

	
}

static BFTFix(start, end)
{
	auto ea, count;
	
	ea = start;
	count = 0;
	while( ea != BADADDR && ea < end )
	{
		BFTEntry(ea, count);
		count = count + 1;
		ea = ea + 0x2C;
	}
}

static main() 
{
	BFTFix(0x8F9D1934, 0x8F9D555C); 

	Message("BFT Fix: Done\n");
}


