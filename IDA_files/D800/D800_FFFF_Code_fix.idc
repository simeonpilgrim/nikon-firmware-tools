
#include <idc.idc>

static fixData( start, end)
{
	auto ea, ref, last;
	auto word, called, type;

	ea = start;
	
	while( ea < end )
	{
		word = Word(ea);
		if( word = 0xFFFF && Word(ea+2) == 0xFFFF)
		{
			//Message("%a\n", ea);
			called = 0;
			ref = RfirstB0(ea);
			last = BADADDR;
			while( ref != BADADDR && ref != last )
			{
				//Message("%a ref'ed %a\n", ea, ref);
				if ( ref != ea && XrefType() != fl_F )
				{
					called = 1;
				}
				last = ref;
					
				ref = RnextB0(ea, ref);
			}
			
			MakeUnknown(ea, 2, DOUNK_EXPAND );
			if ( called )
			{
				//Message("%a called\n", ea);
				MakeWord(ea);
			}
		}

		ea = ea + 2;
	}

}

static main() 
{
	fixData( 0x8E6E7C, 0xFFFFFF);
	//fixData( 0xE0000, 0x0100000 );
	Message("End\n");
}


