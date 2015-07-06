
#include <idc.idc>

static wipe( start, len){
	auto ea, end;

	
	ea = start;
	end = start + len;
	
	while( ea != BADADDR && ea < end)
	{
	  MakeUnkn(ea,0);
	  PatchByte(ea,0);
	  ea = ea +1;
	}

}


static main() {
  auto ea, code, dest, call;

  Message("WipeZero: Start\n");
  
  // area that's copied to 0x8F800000
  //wipe( 0x393CF8, 0xa9c );
  
  // area that's copied to 0x8F9C4E78 _RAM_INIT
  //wipe( 0x37BDE4, 0x17F18 );
  
  wipe( 0x80000000, 0xfffff );

  
  Message("WipeZero: Done\n");
}


