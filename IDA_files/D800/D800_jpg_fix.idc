
#include <idc.idc>

static MakeJpeg(ref, len)
{
	auto str;
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
	MakeJpeg(0x3EDDD8,0x233);
}

static main() 
{
    Message("Jpg Fix: Start\n");

	//D800 1.01
	
	JpgTableTypeA(0x5BA08C, 0x5BA194);
	JpgTableTypeA(0x56EA68, 0x56EBA0);
	JpgTableTypeD(0x84F34EAC,0x84F3523C );
	TableOfLeftovers();

	
	Message("Jpg Fix: Done\n");
}


