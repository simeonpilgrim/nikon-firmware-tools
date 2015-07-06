#include <idc.idc>


static process_header(start, ind)
{	
	auto val, str, v0;
	
	//Message("ProcHead %x\n",start);
	
	OpOff(start, 0, 0);
	val = Dword(start+4);
	str = sprintf("p%s",GetString(val,-1,ASCSTR_C));
	OpOff(start+4, 0, 0);
	Message( "%s%s\n", ind, str);	
		
	val = Dword(start);
	if( val == 0x51458F20 ) // two items
	{
		// nothing todo
	}
	if( val == 0x51458F30 ) // three items
	{
		v0 = Dword( start+0x08 );
		process_header(v0, ind + "  ");
	}
	if( val == 0x51458F40 ) // header block
	{
		auto v1, v2, v3, b0, b1, w0;
				
		v0 = Dword( start+0x08 );
		MakeDword( start+0x08);
		v1 = Dword( start+0x0C );
		MakeDword( 	start+0x0C);

		if( v0 != 0 )
		{
			Message( "%s%a * %d\n", ind, start, v0);	
		}
		v2 = 0;
		while (v2 < v1)
		{
			v0 = start+0x10+(8*v2);
			v3 = Dword(v0);
			OpOff(v0, 0, 0);
			b0 = Byte(v0+4);
			b1 = Byte(v0+5);
			w0 = Word(v0+6);
			MakeByte(v0+4);
			MakeByte(v0+5);
			MakeWord(v0+6);
			
			Message( "  %s%d %d %d\n", ind, b0, b1, w0);	
			process_header(v3, ind + "  ");

			v2 = v2 + 1;
		}
	}
	
	MakeNameEx(start, str, 0);
	return str;
}


static process_func(start, str, count)
{	
	auto addr, name;
	//Message("ProcFunc %x\n",start);


	addr = start & 0xFFffFFfe;
	if( addr != start )
	{
		SetReg(start, "T", 1);
	}

	if( strlen(str) > 0 )
	{
		name = sprintf("%s_%s_f%02d", substr(str, 1, 5), substr(str, 5, -1), count);
		//Message("%s\n",name);
		MakeNameEx(addr, name, 0 );
	}
	MakeFunction(addr, BADADDR);
}


static range_scan(start, end)
{
	auto ea, state, val, str, name, count, baseaddr;
	ea = start;
	state = 0;

	while( ea < end)
	{
		val = Dword(ea);
		MakeDword(ea);
		
		if( state == 0 )
		{
			if( val > 0 )
			{
				baseaddr = val;
				OpOff(ea, 0, 0);
				///Message("hdr %x\n",ea);
				str = process_header(val, "");
				name = sprintf("%s_vtab",str);
				MakeNameEx(ea+4, name, 0);
				state = 1;
			}
		}
		else if (state == 2)
		{
			OpOff(ea, 0, 0);
			if( val != baseaddr )
			{
				Message("\nBASE NOT SAME %x\n", ea);
			}
			state = 1;
		}
		else
		{
			if( val == 0 || val < 0)
			{
				if( val < 0 ) // inherited virtual funcs
				{
					///Message("to state 2 : %x\n",ea);
					str = "";
					state = 2;
					
					// todo make neg number...??

				}
				else
				{
					///Message("to state 0 : %x\n",ea);
					state = 0;
				}
				count = 0;
			}
			else
			{
				///Message("func %x\n",val);
				process_func(val, str, count);
				count = count + 1;
				OpOff(ea, 0, 0);
			}
		}
		
		ea = ea + 0x4;
	}
}

static string_run(start, end)
{
	auto ea, first, state, val;
	
	ea = start;
	state = 0;
	while( ea < end )
	{
		val = Byte(ea);
		if( state == 0 )
		{
			if( val == 0 )
			{
				// stay in state 0
			}
			else
			{
				first = ea;
				state = 1;
			}
		}
		else
		{
			if( val == 0 )
			{
				MakeUnknown(first, (ea-first)+1, 0 );
				MakeStr(first,ea + 1);
				state = 0;
			}
			else
			{
				// stay in state 1
			}
		}
		
		ea = ea + 1;
	}
	
	if( state == 1 )
	{
		MakeStr(first,ea);
	}
}



static main() 
{
	Message("Mark - Start\n");
	
	//range_scan(0x5142FD58, 0x5142FDB8); // testing.
	
	range_scan(0x5142FCA4, 0x514320B8);
	//range_scan(0x514320DC, 0x51458F6C);
	
	//string_run(0x51458F6C, 0x5148D7CB);
	
	Message("Mark - End\n");
}


