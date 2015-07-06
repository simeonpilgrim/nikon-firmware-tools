
#include <idc.idc>

static MakeJpeg(ref, len)
{
	auto str;
	
	MakeUnknown(ref, len, 0 );
	MakeData(ref, FF_BYTE, 1, 0 );
	MakeArray(ref, len);
	str = sprintf("jpg_%x",ref);
	//Message(str);
	MakeNameEx(ref, str, 0);
}


static JpgTableTypeA(start, end) 
{
	auto ea, ref, len;
	
	ea = start;
	while( ea != BADADDR && ea < end )
	{
		ref = Dword(ea);
		len = Dword(ea +4);
		if( ref != 0 && len != 0 )
		{
			Message("%x %x %x\n", ea, ref, len);
			MakeJpeg(ref, len);
			OpOff(ea, 0, 0);	
		}
		OpNumber(ea+4, -1); //len

		ea = ea + 8;
	}
}

static JpgTableTypeB(start, end) 
{
	auto ea, ref, len, dummy;

	ea = start;
	while( ea != BADADDR && ea < end )
	{
		dummy = Dword(ea);
		ref = Dword(ea +4);
		len = Dword(ea +8);
		if( ref != 0 && len != 0 )
		{
			Message("%x %x %x %x\n", ea, dummy, ref, len);
			MakeJpeg(ref, len);
			OpOff(ea + 4, 0, 0);	
		}
		OpNumber(ea, -1); //dummy
		OpNumber(ea+8, -1); //len
		ea = ea + 12;
	}
}

static JpgTableTypeC(start, end) 
{
	auto ea, ref, len, dummy;

	ea = start;
	while( ea != BADADDR && ea < end )
	{
		dummy = Dword(ea);
		ref = Dword(ea +4);
		len = Dword(ea +8);
		if( ref != 0 && len != 0 )
		{
			Message("%x %x %x %x\n", ea, dummy, ref, len);
			MakeJpeg(ref, len);
			OpOff(ea + 4, 0, 0);	
		}

		OpNumber(ea, -1); //dummy
		OpNumber(ea+8, -1); //len
		OpNumber(ea+12, -1); //dummy2
		OpNumber(ea+16, -1); //dummy3

		ea = ea + 20;
	}
}



static JpgTableTypeD(start, end) 
{
	auto ea, ref, len, dum1, dum2;

	ea = start;
	while( ea != BADADDR && ea < end )
	{
		ref = Dword(ea);
		len = Dword(ea +4);
		dum1 = Dword(ea +8);
		dum2 = Dword(ea +12);
		if( ref != 0 && len != 0 )
		{
			Message("%x %x %x %x %x\n", ea, ref, len, dum1, dum2);
			MakeJpeg(ref, len);
			OpOff(ea + 4, 0, 0);	
		}

		OpNumber(ea+4, -1); //len
		OpNumber(ea+8, -1); //dum1
		OpNumber(ea+12, -1); //dum2

		ea = ea + 16;
	}
}

static TableOfLeftovers()
{
	//MakeJpeg(0x5429BC, ?? );

	MakeJpeg(0x54821B, 0x29AF);
	MakeJpeg(0x543332, 0x2383);
	MakeJpeg(0x5456B5, 0x2B66);
	MakeJpeg(0x54821B, 0x29AF);
	MakeJpeg(0x54ABCA, 0x19F1);
	
	MakeJpeg(0x571711, 0x1A67);
	MakeJpeg(0x56FD4F, 0x19C2);
	MakeJpeg(0x56EC02, 0x114D);
}

static main() 
{
    Message("Jpg Fix: Start\n");

	//D3000 
	JpgTableTypeA(0x50E67C, 0x50E784);
	JpgTableTypeA(0x5949D8, 0x594F60);
	
	JpgTableTypeD(0x5F9D24, 0x5F9E44);

	TableOfLeftovers();

	
	Message("Jpg Fix: Done\n");
}


