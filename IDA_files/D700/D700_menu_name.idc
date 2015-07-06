
#include <idc.idc>

static MakeMenu(ref, txt, type)
{
	MakeNameEx(ref, txt, 0 );
	if( type == 0)
	{
		MakeUnknown(ref, 0x20, 0 );
		MakeStructEx(ref, 0x20, "struc_6");
	}
	else
	{
		MakeUnknown(ref, 0x10, 0 );
		MakeStructEx(ref, 0x10, "struc_14");
	}
}


static main() 
{
    Message("Menu Name: Start\n");



	Message("Menu Name: Done\n");
}


