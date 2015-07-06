
#include <idc.idc>


static main() 
{
	auto start;
	auto end;
	auto ea;
	auto ref;
	auto str;
	auto last;

	start = 0x394794;
	end = 0x397718;
	last = 0;
	ea = start;
	while( ea != BADADDR && ea < end )
	{
		ref = Dword(ea);
		if( ref != 0 )
		{
			MakeUnknown(ref, 0x08, 0 );
			MakeStructEx(ref, 0x08, "struc_227");
			
			str = sprintf("sym_%03x", (ea-start)/4);
			MakeNameEx(ref,str,0);
			
			if( last != 0 )
			{
				last = last + 8;
				
				while (last < ref)
				{
					MakeUnknown(last, 0x08, 0 );
					MakeStructEx(last, 0x08, "struc_227");

					last = last + 8;
				}
			}
		}
		last = ref;
		ea = ea + 4;
		
	}


}


