
#include <idc.idc>

static getknownname(id){
	if(id == 0xFC01) return "_EnterServiceMode";
	if(id == 0xFC02) return "_ExitServiceMode";
	if(id == 0xFE01) return "_GetVersion";
	if(id == 0xFE02) return "_GetModel";
	if(id == 0xFE03) return "_GetLoaderVersion";
	if(id == 0xFE04) return "_GetMainVersion";
	if(id == 0xFD34) return "_SetRamWrite";
	if(id == 0xFE34) return "_GetRamRead";
	if(id == 0xFD31) return "_SetConfigData";
	if(id == 0xFE31) return "_GetConfigData";
	if(id == 0xFC31) return "_ConfigDataDefault";
	if(id == 0xFC32) return "_ConfigDataSave";
	if(id == 0xFC41) return "_CCdPowerOn";
	if(id == 0xFC59) return "_CCdPowerOff";
	if(id == 0xFE41) return "_GetCcdRegulation";
	if(id == 0xFE42) return "_GetCcdTemp";
	if(id == 0xFC44) return "_RawOb";
	if(id == 0xFC45) return "_WbGain";
	if(id == 0xFC46) return "_RawCompType";
	if(id == 0xFC4C) return "_GetSceneAnalyzeInfo";
	if(id == 0xFC53) return "_VideoMode";
	if(id == 0xFC55) return "_ImageDisplayTest";
	if(id == 0xFC57) return "_TftBl";
	if(id == 0xFC59) return "_HdmiConnectInvalid";
	if(id == 0xFC5C) return "_VideoMenuAbort";
	if(id == 0xFD63) return "_SetLensDataFlashWrite";
	if(id == 0xFE63) return "_GetLensDataFlashRead";
	if(id == 0xFC73) return "_DefectTblDevelopment";
	if(id == 0xFD80) return "_SetShipmentInfo";
	if(id == 0xFC82) return "_ConfigWriteCheck";
	if(id == 0xFC91) return "_CardLed";
	if(id == 0xFE91) return "_GetSwInfo";
	if(id == 0xFE99) return "_GetVideoPlugState";
	if(id == 0xFCA1) return "_SoundRec";
	if(id == 0xFCA2) return "_SoundPlay";
	if(id == 0xFCA3) return "_SoundOutputLine";
	if(id == 0xFCA4) return "_SoundOutputLevel";
	if(id == 0xFCA5) return "_SoundStop";
	if(id == 0xFCA6) return "_SoundBeep";
	if(id == 0xFCAA) return "_SoundInputLine";
	if(id == 0xFCAB) return "_SoundsOutputChannel";
	if(id == 0xFCAC) return "_SoundInputLevel";
	if(id == 0xFBB4) return "_Wavefiletransfer";
	if(id == 0xFDC1) return "_SetSysFixAdj";
	if(id == 0xFEC1) return "_GetSysFixAdj";
	if(id == 0xFCC1) return "_SysFixAdjSave";
	if(id == 0xFEB1) return "_GetMcuCmdDataRead";
	if(id == 0xFDB2) return "_SetMcuCmdInspectCommand";
	if(id == 0xFEB2) return "_GetMcuCmdInspectStatus";
	if(id == 0xFCFE) return "_DustRemovelStart";
	if(id == 0xFCFE) return "_DustRemovelStatus";
	if(id == 0xFC4F) return "_FilterNr";
	if(id == 0xFE93) return "_GetVhSensor";
	if(id == 0xFCAD) return "_WaveFileRec";
	if(id == 0xFCAE) return "_WaveFilePlay";
	if(id == 0xFCAF) return "_SoundEffect";
	return "";	
}

static try_make_func(start, name, type)
{	
	auto addr;

	if(start == 0) 
		return;

	addr = start & 0xFFffFFfe;
	if( addr != start )
	{
		SetReg(addr, "T", 1);
	}

	if(strlen(name) > 0 ){
		MakeNameEx(addr, name, 0 );
	}
	MakeFunction(addr, BADADDR);
	
	if(strlen(type) > 0 ){
		SetType(addr, type);
	}
}

static ptp_entry_A(loc)
{
	auto id, str;
	id = Word(loc);
	
	// Fixup entry
	MakeUnknown(loc, 0x1C, 0 );
	MakeStructEx(loc, 0x1C, "ptp_A");
	str = getknownname(id);
	
	try_make_func(Dword(loc+0xC), "","");
	try_make_func(Dword(loc+0x10), sprintf("ptp_%04X%s_check",id,str),"");
	try_make_func(Dword(loc+0x14), sprintf("ptp_%04X%s",id,str),"int __cdecl sub(int p1, ptp_obj* ptp);");
	try_make_func(Dword(loc+0x18), sprintf("ptp_%04X%s_end",id,str),"");
}

static ptp_entry_B(loc)
{
	auto id;
	id = Word(loc);
	
	// Fixup entry
	MakeUnknown(loc, 0x1C, 0 );
	MakeStructEx(loc, 0x1C, "ptp_B");
	
	try_make_func(Dword(loc+0x4), sprintf("ptp_prop_%04X_A",id),"");
	try_make_func(Dword(loc+0x8), sprintf("ptp_prop_%04X_B",id),"");	
	try_make_func(Dword(loc+0xC), sprintf("ptp_prop_%04X_C",id),"");
	try_make_func(Dword(loc+0x10), sprintf("ptp_prop_%04X_D",id),"");
	try_make_func(Dword(loc+0x14), sprintf("ptp_prop_%04X_E",id),"");
	try_make_func(Dword(loc+0x18), sprintf("ptp_prop_%04X_F",id),"");	
}

static ptp_func_set(start, name)
{
	auto ea, str;
	str = sprintf("ptpt_%s_tab", name);
	MakeNameEx(start,str,0);
	
	ea = start;
	while( ea != BADADDR )
	{
		ptp_entry_A(ea);
		if( Word(ea) == 0 )
			break;
			
		ea = ea + 0x1C;
	}
}

static ptp_prop_set(start, name)
{
	auto ea, str;
	str = sprintf("ptpt_%s_tab", name);
	MakeNameEx(start,str,0);
	
	ea = start;
	while( ea != BADADDR )
	{
		ptp_entry_B(ea);
		if( Word(ea) == 0 )
			break;
			
		ea = ea + 0x1C;
	}
}

static make_strucs()
{
    auto id, mid;

	id = AddStrucEx(-1,"ptp_A",0);
	id = GetStrucIdByName("ptp_A");
	mid = AddStrucMember(id,"field_0",	0x0,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_4",	0X4,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_8",	0X8,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_C",	0XC,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_10",	0X10,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_14",	0X14,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_18",	0X18,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	
	id = AddStrucEx(-1,"ptp_B",0);
	id = GetStrucIdByName("ptp_B");
	mid = AddStrucMember(id,"field_0",	0x0,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"field_4",	0X4,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_8",	0X8,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_C",	0XC,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_10",	0X10,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_14",	0X14,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	mid = AddStrucMember(id,"field_18",	0X18,	0x20500400,	0,	4,	0XFFFFFFFF,	0,	0x000002);
	
	id = AddStrucEx(-1,"ptp_obj",0);
	id = GetStrucIdByName("ptp_obj");
	mid = AddStrucMember(id,"msg_len",	0,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"type",	0X4,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"code",	0X6,	0x10000400,	-1,	2);
	mid = AddStrucMember(id,"trans_id",	0X8,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"param1",	0XC,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"param2",	0X10,	0x20000400,	-1,	4);
	mid = AddStrucMember(id,"param3",	0X14,	0x20000400,	-1,	4);
	
}



static main() 
{
// search for 34 FE 00 00, the last instance is the PTP table..

	make_strucs();
	
	ptp_func_set(0x9123BCD8, "10xx");
	ptp_func_set(0x9123C3A0, "FXxx");
	ptp_func_set(0x9123BF08, "90xx_B");
	ptp_func_set(0x9123CC28, "90xx");
	ptp_func_set(0x9123D034, "95xx");
	ptp_func_set(0x9123D184, "92xx");
	
	ptp_prop_set(0x9123D250, "50xx");
	ptp_prop_set(0x9123D4D4, "D0xx");

}