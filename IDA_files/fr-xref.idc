
#include <idc.idc>

static Call32Bit()
{
  auto ea, code, dest, call;

  ea = 0x40000;
  Message("Call32Bit: Start\n");

  while( ea != BADADDR )
  {
	ea = FindBinary(ea, SEARCH_DOWN, "9F 8C");
	
	if( ea != BADADDR )
	{
		//32 load and call
		code = Word(ea);
		dest = Dword(ea+2);
		call = Word(ea+6);
		
		if( code == 0x9f8c && ( call == 0x9f1c || call == 0x971c) )
		{
			if(isCode(GetFlags(ea)) == 0 )
			{
				MakeCode(ea);
			}
			MakeCode(dest);
			AutoMark(dest, AU_PROC);
			AddCodeXref(ea, dest, fl_CN);
		}
		
		ea = ea +1;
	}	
  }
  
  Message("Call32Bit: Done\n");
}

static Call20Bit()
{
  auto ea, code, dest, call;

  ea = 0x40000;
  Message("Call20Bit: Start\n");

  while( ea != BADADDR )
  {
	ea = FindBinary(ea, SEARCH_DOWN, "9B");
	//Message(atoa(ea)+"\n");
	
	if( ea != BADADDR )
	{	
		//20bit load and call
		code = Word(ea) & 0xFF0F;
		dest = Word(ea+2) + ((Word(ea)&0x00F0)<<(16-4));
		call = Word(ea+4);
		
		if( code == 0x9b0c && ( call == 0x9f1c || call == 0x971c) )
		{
			if(isCode(GetFlags(ea)) == 0 )
			{
				MakeCode(ea);
			}
			MakeCode(dest);
			AutoMark(dest, AU_PROC);
			AddCodeXref(ea, dest, fl_CN);
		}
		
		ea = ea +1;
	}	
  }
  
  Message("Call20Bit: Done\n");
}


static Jmp32Bit()
{
  auto ea, code, dest, call;

  ea = 0x40000;
  Message("Jmp32Bit: Start\n");

  while( ea != BADADDR )
  {
	ea = FindBinary(ea, SEARCH_DOWN, "9F 8C");
	
	if( ea != BADADDR )
	{
		//32 load and call
		code = Word(ea);
		dest = Dword(ea+2);
		call = Word(ea+6);
		
		if( code == 0x9f8c && ( call == 0x9f0c || call == 0x970c) )
		{
			if(isCode(GetFlags(ea)) == 0 )
			{
				MakeCode(ea);
			}
			MakeCode(dest);
			//AutoMark(dest, AU_PROC);
			AddCodeXref(ea, dest, fl_CN);
		}
		
		ea = ea +1;
	}	
  }
  
  Message("Jmp32Bit: Done\n");
}


static main() {
  Message("Fr-Xref: Start\n");

  Call32Bit();
  Call20Bit();
  Jmp32Bit();
  
  Message("Fr-Xref: Done\n");
}


