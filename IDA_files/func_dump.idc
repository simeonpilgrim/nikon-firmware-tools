#include <idc.idc>

static FuncDump(start)
{
	auto ea, str, count, ref;
	auto end;
	auto teststr;

	ea = start;

	while( ea != BADADDR )
	{
		str = GetFunctionName(ea);
		if( str != 0 )
		{
			end = FindFuncEnd(ea);

			count = 0;
			ref = RfirstB(ea);
			while(ref != BADADDR)
			{
				count = count + 1;
				ref = RnextB(ea, ref);
			}
			
			teststr = sprintf("sub_%X", ea);
			if( teststr != str)
			{
				Message("-s 0x%08X=%s\n", ea, str );
			}
			//Message("%s, 0x%d, 0x%x, 0x%x, 0x%x, %d\n", str, count, ea, end, end-ea, end-ea   );
		}
		
		ea = NextFunction(ea);
	}
}

static main() 
{
	//Message("FuncDump: Start\n");
	
	FuncDump(0x40000);
		
	//Message("FuncDump: Done\n");
}
