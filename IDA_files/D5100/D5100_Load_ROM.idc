
#include <idc.idc>


static main() 
{
	auto handle;

	Message("ROM Fix: Start\n");
	
	handle = fopen("C:\\Users\\spilgrim\\Downloads\\Nikon\\Decode\\b640101b.bin", "rb");
	if( handle != 0 )
	{
		Message("handle valid\n");
		
		loadfile(handle, 0x86ff8e, 0x86ff8e+0x40000, 0x30000);
		fclose(handle);
	}
	
	Message("ROM Fix: Done\n");
}


