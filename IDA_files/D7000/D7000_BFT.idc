
#include <idc.idc>

static FixFunc(loc, idx, count, setname)
{
	auto ea;
	auto str, old;
	
	ea = Dword(loc + ( idx * 4));
	if( ea != 0 )
	{
		if( setname )
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
		else
		{
			MakeNameEx(ea,"",0);
		}
	}
}


static BFTEntry(loc, count, setname)
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
	
	FixFunc(loc, 1, count, setname);
	FixFunc(loc, 2, count, setname);
	FixFunc(loc, 3, count, setname);
	FixFunc(loc, 4, count, setname);
	FixFunc(loc, 5, count, setname);
	FixFunc(loc, 6, count, setname);
	FixFunc(loc, 7, count, setname);
	FixFunc(loc, 8, count, setname);
	FixFunc(loc, 9, count, setname);
}

static BFTFix(start, end)
{
	auto ea, count, size;
	
	ea = start;
	count = 0;
	size = 0x2C;
	while( ea != BADADDR && ea < end )
	{
		BFTEntry(ea, count, 0);
		count = count + 1;
		ea = ea + size;
	}
	ea = start;
	count = 0;
	while( ea != BADADDR && ea < end )
	{
		BFTEntry(ea, count, 1);
		count = count + 1;
		ea = ea + size;
	}

}

static main() 
{
	BFTFix(0x8F9BF0E8, 0x8F9C4470); 

	Message("BFT Fix: Done\n");
}


