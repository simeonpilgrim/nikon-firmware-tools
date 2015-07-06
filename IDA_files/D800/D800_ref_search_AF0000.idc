
#include <idc.idc>


static main() 
{
	auto start, end, i;
	
	Message("Start\n");
	
	start = 0xa00000;
	end = 0xb00000;

	for(i = start; i < end; i = i + 4 )
	{
		if ( isRef(GetFlags(i)) )
		{
			Message("%a\n", i );
		}
	}
	
	Message("End\n");
	
}


