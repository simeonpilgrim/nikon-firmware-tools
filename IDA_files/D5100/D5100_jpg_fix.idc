
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
	MakeJpeg(0x8A6B4B, 0x22FE);
	MakeJpeg(0x8A8E49, 0x2661);
	MakeJpeg(0x8AB4AA, 0x2C66);
	MakeJpeg(0x8B0FD7, 0xE6C);
	MakeJpeg(0x8B1E43, 0x1680);
	MakeJpeg(0x8B34C3, 0x181C);
	MakeJpeg(0x8B4CDF, 0xDB2);	
	MakeJpeg(0x8B5A91, 4280);
	MakeJpeg(0x8BEAAD, 0xECB);
	MakeJpeg(0x8BF978, 0x155F);
	MakeJpeg(0x8C0ED7, 0xFEC);
	MakeJpeg(0x8C1EC3, 0x1103);
	MakeJpeg(0x8C2FC6, 0x10dE);
	MakeJpeg(0x8C53FF, 5049);
	MakeJpeg(0x8C8235, 0xCF4);
	MakeJpeg(0x8E7930, 0xE44);
	MakeJpeg(0x8E8774, 0x162E);
	MakeJpeg(0x8E9DA2, 5801);
	MakeJpeg(0x8EB44B, 3224);
	MakeJpeg(0x8EC0E3, 0x122E);
	MakeJpeg(0x8ED311, 4820);
	MakeJpeg(0x8EE5E5, 0x12E7);
	MakeJpeg(0x8EF8CC, 4451);
	MakeJpeg(0x8F0A2F, 6335);
	MakeJpeg(0x8F22EE, 4733);
	MakeJpeg(0x8F356B, 0xE7D);
	MakeJpeg(0x8F43E8, 6595 );
	MakeJpeg(0x8F5DAB, 0x1E7C);
	MakeJpeg(0x8F7C27, 0xFBC);
	MakeJpeg(0x8F8BE3, 5284 );
	MakeJpeg(0x8FFDD9, 0xF07);
	MakeJpeg(0x900CE0, 0x16E0);
	MakeJpeg(0x9023C0, 0x100C);
	MakeJpeg(0x9033CC, 5772 );
	MakeJpeg(0x904A58, 0x117F);
	MakeJpeg(0x905BD7, 0xB8B);
	MakeJpeg(0x906762, 0xC03);
	MakeJpeg(0x907365, 0x1994);
	
	
}

static main() 
{
    Message("Jpg Fix: Start\n");

	//d5100 
	JpgTableTypeA(0x054258, 0x054260);
	JpgTableTypeA(0x055de4, 0x055EEC);
	JpgTableTypeA(0x2A5898, 0x2A59F8);
	JpgTableTypeA(0x2a66D8, 0x2A6770);
	JpgTableTypeA(0x7c5a34, 0x7c6254);
	JpgTableTypeB(0x38e7b0, 0x38e8d0);
	JpgTableTypeC(0x38e8d0, 0x38E998);
	JpgTableTypeD(0x392F90, 0x393320);
	TableOfLeftovers();

	
	Message("Jpg Fix: Done\n");
}


