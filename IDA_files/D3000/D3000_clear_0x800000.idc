
#include <idc.idc>

static main() {
  auto ea, end;

  ea = 0x800000;
  end = 0x800000 + 0x800000;
  Message("WipeZero: Start\n");

  while( ea != BADADDR && ea < end )
  {
	MakeUnkn(ea,0);
	PatchByte(ea,0);
	MakeByte(ea);
	ea = ea +1;
  }
  
  Message("WipeZero: Done\n");
}


