
#include <idc.idc>

static PtpCodeTxt(code)
{
	auto str;
	if( code == 0x5001 )
		str = "BatteryLevel";
	else if( code == 0x5003 )
		str = "ImageSize";
	else if( code == 0x5004 )
		str = "CompressionSetting";
	else if( code == 0x5005 )
		str = "WhiteBalance";
	else if( code == 0x5007 )
		str = "FNumber";
	else if( code == 0x5008 )
		str = "FocalLength";
	else if( code == 0x500A )
		str = "FocusMode";
	else if( code == 0x500B )
		str = "ExposureMeteringMode";
	else if( code == 0x500C )
		str = "FlashMode";
	else if( code == 0x500D )
		str = "ExposureTime";
	else if( code == 0x500E )
		str = "ExposureProgramMode";
	else if( code == 0x500F )
		str = "ExposureIndex";
	else if( code == 0x5010 )
		str = "ExposureBiasCompensation";
	else if( code == 0x5011 )
		str = "DateTime";
	else if( code == 0x5013 )
		str = "StillCaptureMode";
	else if( code == 0x5018 )
		str = "BurstNumber";
	else if( code == 0x501C )
		str = "FocusMeteringMode";
	else if( code == 0x501E )
		str = "Artist";
	else if( code == 0x501F )
		str = "CopyrightInfo";
	else if( code == 0xD015 )
		str = "NIKON_ResetBank0";
	else if( code == 0xD016 )
		str = "NIKON_RawCompression";
	else if( code == 0xD017 )
		str = "NIKON_WhiteBalanceAutoBias";
	else if( code == 0xD018 )
		str = "NIKON_WhiteBalanceTungstenBias";
	else if( code == 0xD019 )
		str = "NIKON_WhiteBalanceFluorescentBias";
	else if( code == 0xD01A )
		str = "NIKON_WhiteBalanceDaylightBias";
	else if( code == 0xD01B )
		str = "NIKON_WhiteBalanceFlashBias";
	else if( code == 0xD01C )
		str = "NIKON_WhiteBalanceCloudyBias";
	else if( code == 0xD01D )
		str = "NIKON_WhiteBalanceShadeBias";
	else if( code == 0xD01E )
		str = "NIKON_WhiteBalanceColorTemperature";
	else if( code == 0xD01F )
		str = "NIKON_WhiteBalancePresetNo";
	else if( code == 0xD020 )
		str = "NIKON_WhiteBalancePresetName0";
	else if( code == 0xD021 )
		str = "NIKON_WhiteBalancePresetName1";
	else if( code == 0xD022 )
		str = "NIKON_WhiteBalancePresetName2";
	else if( code == 0xD023 )
		str = "NIKON_WhiteBalancePresetName3";
	else if( code == 0xD024 )
		str = "NIKON_WhiteBalancePresetName4";
	else if( code == 0xD025 )
		str = "NIKON_WhiteBalancePresetVal0";
	else if( code == 0xD026 )
		str = "NIKON_WhiteBalancePresetVal1";
	else if( code == 0xD027 )
		str = "NIKON_WhiteBalancePresetVal2";
	else if( code == 0xD028 )
		str = "NIKON_WhiteBalancePresetVal3";
	else if( code == 0xD029 )
		str = "NIKON_WhiteBalancePresetVal4";
	else if( code == 0xD02E )
		str = "NIKON_NonCPULensDataFocalLength";
	else if( code == 0xD02F )
		str = "NIKON_NonCPULensDataMaximumAperture";
	else if( code == 0xD031 )
		str = "NIKON_JPEG_Compression_Policy";
	else if( code == 0xD032 )
		str = "NIKON_ColorSpace";
	else if( code == 0xD034 )
		str = "NIKON_FlickerReduction";
	else if( code == 0xD035 )
		str = "NIKON_RemoteMode";
	else if( code == 0xD036 )
		str = "NIKON_VideoMode";
	else if( code == 0xD045 )
		str = "NIKON_ResetBank";
	else if( code == 0xD048 )
		str = "NIKON_A1AFCModePriority";
	else if( code == 0xD049 )
		str = "NIKON_A2AFSModePriority";
	else if( code == 0xD04F )
		str = "NIKON_FocusAreaWrap";
	else if( code == 0xD050 )
		str = "NIKON_VerticalAFON";
	else if( code == 0xD051 )
		str = "NIKON_AFLockOn";
	else if( code == 0xD053 )
		str = "NIKON_EnableCopyright";
	else if( code == 0xD054 )
		str = "NIKON_ISOAuto";
	else if( code == 0xD055 )
		str = "NIKON_EVISOStep";
	else if( code == 0xD056 )
		str = "NIKON_EVStep";
	else if( code == 0xD058 )
		str = "NIKON_ExposureCompensation";
	else if( code == 0xD059 )
		str = "NIKON_CenterWeightArea";
	else if( code == 0xD05A )
		str = "NIKON_ExposureBaseMatrix";
	else if( code == 0xD05B )
		str = "NIKON_ExposureBaseCenter";
	else if( code == 0xD05C )
		str = "NIKON_ExposureBaseSpot";
	else if( code == 0xD05D )
		str = "NIKON_LiveViewAF";
	else if( code == 0xD05E )
		str = "NIKON_AELockMode";
	else if( code == 0xD05F )
		str = "NIKON_AELAFLMode";
	else if( code == 0xD061 )
		str = "NIKON_LiveViewAFFocus";
	else if( code == 0xD062 )
		str = "NIKON_MeterOff";
	else if( code == 0xD063 )
		str = "NIKON_SelfTimer";
	else if( code == 0xD065 )
		str = "NIKON_ImgConfTime";
	else if( code == 0xD067 )
		str = "NIKON_AngleLevel";
	else if( code == 0xD068 )
		str = "NIKON_D1ShootingSpeed";
	else if( code == 0xD069 )
		str = "NIKON_D2MaximumShots";
	else if( code == 0xD06A )
		str = "NIKON_ExposureDelayMode";
	else if( code == 0xD06B )
		str = "NIKON_LongExposureNoiseReduction";
	else if( code == 0xD06C )
		str = "NIKON_FileNumberSequence";
	else if( code == 0xD06F )
		str = "NIKON_D7Illumination";
	else if( code == 0xD070 )
		str = "NIKON_NrHighISO";
	else if( code == 0xD071 )
		str = "NIKON_SHSET_CH_GUID_DISP";
	else if( code == 0xD072 )
		str = "NIKON_ArtistName";
	else if( code == 0xD073 )
		str = "NIKON_CopyrightInfo";
	else if( code == 0xD074 )
		str = "NIKON_FlashSyncSpeed";
	else if( code == 0xD075 )
		str = "NIKON_FlashShutterSpeed";
	else if( code == 0xD077 )
		str = "NIKON_E4ModelingFlash";
	else if( code == 0xD078 )
		str = "NIKON_BracketSet";
	else if( code == 0xD07A )
		str = "NIKON_BracketOrder";
	else if( code == 0xD080 )
		str = "NIKON_F1CenterButtonShootingMode";
	else if( code == 0xD084 )
		str = "NIKON_F4AssignFuncButton";
	else if( code == 0xD085 )
		str = "NIKON_F5CustomizeCommDials";
	else if( code == 0xD086 )
		str = "NIKON_ReverseCommandDial";
	else if( code == 0xD087 )
		str = "NIKON_ApertureSetting";
	else if( code == 0xD088 )
		str = "NIKON_MenusAndPlayback";
	else if( code == 0xD089 )
		str = "NIKON_F6ButtonsAndDials";
	else if( code == 0xD08A )
		str = "NIKON_NoCFCard";
	else if( code == 0xD08D )
		str = "NIKON_AFAreaPoint";
	else if( code == 0xD08F )
		str = "NIKON_CleanImageSensor";
	else if( code == 0xD090 )
		str = "NIKON_ImageCommentString";
	else if( code == 0xD091 )
		str = "NIKON_ImageCommentEnable";
	else if( code == 0xD092 )
		str = "NIKON_ImageRotation";
	else if( code == 0xD093 )
		str = "NIKON_ManualSetLensNo";
	else if( code == 0xD0A0 )
		str = "NIKON_MovScreenSize";
	else if( code == 0xD0A2 )
		str = "NIKON_MovMicrophone";
	else if( code == 0xD0A3 )
		str = "NIKON_MovieFileSlot";
	else if( code == 0xD0A4 )
		str = "NIKON_MovieRecProhibitionCondition";
	else if( code == 0xD0A6 )
		str = "NIKON_ManualMovieSettings";
	else if( code == 0xD0B3 )
		str = "NIKON_MonitorOffDelay";
	else if( code == 0xD0C0 )
		str = "NIKON_Bracketing";
	else if( code == 0xD0C1 )
		str = "NIKON_AutoExposureBracketStep";
	else if( code == 0xD0C2 )
		str = "NIKON_AutoExposureBracketProgram";
	else if( code == 0xD0C3 )
		str = "NIKON_AutoExposureBracketCount";
	else if( code == 0xD0C4 )
		str = "NIKON_WhiteBalanceBracketStep";
	else if( code == 0xD0C5 )
		str = "NIKON_WhiteBalanceBracketProgram";
	else if( code == 0xD0C6 )
		str = "NIKON_ADLBracketingPattern";
	else if( code == 0xD0E0 )
		str = "NIKON_LensID";
	else if( code == 0xD0E1 )
		str = "NIKON_LensSort";
	else if( code == 0xD0E2 )
		str = "NIKON_LensType";
	else if( code == 0xD0E3 )
		str = "NIKON_FocalLengthMin";
	else if( code == 0xD0E4 )
		str = "NIKON_FocalLengthMax";
	else if( code == 0xD0E5 )
		str = "NIKON_MaxApAtMinFocalLength";
	else if( code == 0xD0E6 )
		str = "NIKON_MaxApAtMaxFocalLength";
	else if( code == 0xD0F0 )
		str = "NIKON_FinderISODisp";
	else if( code == 0xD0F2 )
		str = "NIKON_AutoOffPhoto";
	else if( code == 0xD0F3 )
		str = "NIKON_AutoOffMenu";
	else if( code == 0xD0F4 )
		str = "NIKON_AutoOffInfo";
	else if( code == 0xD0F5 )
		str = "NIKON_SelfTimerShootNum";
	else if( code == 0xD0F8 )
		str = "NIKON_AutoDistortionControl";
	else if( code == 0xD0F9 )
		str = "NIKON_SceneMode";
	else if( code == 0xD0FC )
		str = "NIKON_D0FC";
	else if( code == 0xD0FD )
		str = "NIKON_D0FD";
	else if( code == 0xD0FE )
		str = "NIKON_SelfTimerInterval";
	else if( code == 0xD100 )
		str = "NIKON_ExposureTime";
	else if( code == 0xD101 )
		str = "NIKON_ACPower";
	else if( code == 0xD102 )
		str = "NIKON_WarningStatus";
	else if( code == 0xD103 )
		str = "NIKON_MaximumShots";
	else if( code == 0xD104 )
		str = "NIKON_AFLockStatus";
	else if( code == 0xD105 )
		str = "NIKON_AELockStatus";
	else if( code == 0xD106 )
		str = "NIKON_FVLockStatus";
	else if( code == 0xD108 )
		str = "NIKON_AutofocusArea";
	else if( code == 0xD109 )
		str = "NIKON_FlexibleProgram";
	else if( code == 0xD10B )
		str = "NIKON_RecordingMedia";
	else if( code == 0xD10C )
		str = "NIKON_USBSpeed";
	else if( code == 0xD10D )
		str = "NIKON_CCDNumber";
	else if( code == 0xD10E )
		str = "NIKON_CameraOrientation";
	else if( code == 0xD114 )
		str = "NIKON_IllumSetting";
	else if( code == 0xD120 )
		str = "NIKON_ExternalFlashAttached";
	else if( code == 0xD121 )
		str = "NIKON_ExternalFlashStatus";
	else if( code == 0xD122 )
		str = "NIKON_ExternalFlashSort";
	else if( code == 0xD124 )
		str = "NIKON_ExternalFlashCompensation";
	else if( code == 0xD125 )
		str = "NIKON_NewExternalFlashMode";
	else if( code == 0xD126 )
		str = "NIKON_FlashExposureCompensation";
	else if( code == 0xD141 )
		str = "NIKON_D141";
	else if( code == 0xD148 )
		str = "NIKON_Slot2SaveMode";
	else if( code == 0xD149 )
		str = "NIKON_RawBitMode";
	else if( code == 0xD14E )
		str = "NIKON_ActiveDLighting";
	else if( code == 0xD14F )
		str = "NIKON_FlourescentType";
	else if( code == 0xD150 )
		str = "NIKON_TuneColourTemperature";
	else if( code == 0xD151 )
		str = "NIKON_TunePreset0";
	else if( code == 0xD152 )
		str = "NIKON_TunePreset1";
	else if( code == 0xD153 )
		str = "NIKON_TunePreset2";
	else if( code == 0xD154 )
		str = "NIKON_TunePreset3";
	else if( code == 0xD155 )
		str = "NIKON_TunePreset4";
	else if( code == 0xD160 )
		str = "NIKON_BeepOff";
	else if( code == 0xD161 )
		str = "NIKON_AutofocusMode";
	else if( code == 0xD162 )
		str = "NIKON_D162";
	else if( code == 0xD163 )
		str = "NIKON_AFAssist";
	else if( code == 0xD164 )
		str = "NIKON_PADVPMode";
	else if( code == 0xD166 )
		str = "NIKON_AFAreaIllumination";
	else if( code == 0xD167 )
		str = "NIKON_FlashMode";
	else if( code == 0xD169 )
		str = "NIKON_FlashSign";
	else if( code == 0xD16A )
		str = "NIKON_ISO_Auto";
	else if( code == 0xD16B )
		str = "NIKON_RemoteTimeout";
	else if( code == 0xD16C )
		str = "NIKON_GridDisplay";
	else if( code == 0xD16D )
		str = "NIKON_ContinuousSpeedHigh";
	else if( code == 0xD181 )
		str = "NIKON_WarningDisplay";
	else if( code == 0xD182 )
		str = "NIKON_BatteryCellKind";
	else if( code == 0xD183 )
		str = "NIKON_ISOAutoHiLimit";
	else if( code == 0xD187 )
		str = "NIKON_InfoDispSetting";
	else if( code == 0xD189 )
		str = "NIKON_PreviewButton";
	else if( code == 0xD18D )
		str = "NIKON_IndicatorDisp";
	else if( code == 0xD18E )
		str = "NIKON_CellKindPriority";
	else if( code == 0xD1A2 )
		str = "NIKON_LiveViewStatus";
	else if( code == 0xD1A3 )
		str = "NIKON_LiveViewImageZoomRatio";
	else if( code == 0xD1A4 )
		str = "NIKON_LiveViewProhibitCondition";
	else if( code == 0xD1B0 )
		str = "NIKON_ExposureDisplayStatus";
	else if( code == 0xD1B1 )
		str = "NIKON_ExposureIndicateStatus";
	else if( code == 0xD1B2 )
		str = "NIKON_InfoDispErrStatus";
	else if( code == 0xD1B3 )
		str = "NIKON_ExposureIndicateLightup";
	else if( code == 0xD1B4 )
		str = "NIKON_DynamicAFArea";
	else if( code == 0xD1C0 )
		str = "NIKON_FlashOpen";
	else if( code == 0xD1C1 )
		str = "NIKON_FlashCharged";
	else if( code == 0xD1D0 )
		str = "NIKON_FlashMRepeatValue";
	else if( code == 0xD1D1 )
		str = "NIKON_FlashMRepeatCount";
	else if( code == 0xD1D2 )
		str = "NIKON_FlashMRepeatInterval";
	else if( code == 0xD1D3 )
		str = "NIKON_FlashCommandChannel";
	else if( code == 0xD1D4 )
		str = "NIKON_FlashCommandSelfMode";
	else if( code == 0xD1D5 )
		str = "NIKON_FlashCommandSelfCompensation";
	else if( code == 0xD1D6 )
		str = "NIKON_FlashCommandSelfValue";
	else if( code == 0xD1D7 )
		str = "NIKON_FlashCommandAMode";
	else if( code == 0xD1D8 )
		str = "NIKON_FlashCommandACompensation";
	else if( code == 0xD1D9 )
		str = "NIKON_FlashCommandAValue";
	else if( code == 0xD1DA )
		str = "NIKON_FlashCommandBMode";
	else if( code == 0xD1DB )
		str = "NIKON_FlashCommandBCompensation";
	else if( code == 0xD1DC )
		str = "NIKON_FlashCommandBValue";
	else if( code == 0xD1F0 )
		str = "NIKON_D1F0";
	else if( code == 0xD1F1 )
		str = "NIKON_D1F1";
	else if( code == 0xD1F2 )
		str = "NIKON_D1F2";
	else if( code == 0xD200 )
		str = "NIKON_ActivePicCtrlItem";
	else if( code == 0xD201 )
		str = "NIKON_ChangePicCtrlItem";
	else if( code == 0xD303 )
		str = "NIKON_D303";
	else if( code == 0xD406 )
		str = "MTP_SessionInitiatorInfo";
	else if( code == 0xD407 )
		str = "MTP_PerceivedDeviceType";
	else
	{
		str = sprintf("%04X", code);
		Message("ptp code %s missing\n", str);
}
	
	return str;
}


static PtpServiceCodeTxt(code)
{
	auto str;
		
	if( code == 0xFC01 )
		str = "FC01_EnterServiceMode";
	else if( code == 0xFC02 )
		str = "FC02_ExitServiceMode";
		
	else if( code == 0xFE01 )
		str = "FE01_FirmBVersion";
	else if( code == 0xFE02 )
		str = "FE02_CameraModel";
	else if( code == 0xFE03 )
		str = "FE03_Q_OS_Version";
	else if( code == 0xFE04 )
		str = "FE04_B_OS_Version";
	else if( code == 0xFE05 )
		str = "FE05_x_xx_Version";
		
	else if( code == 0xFC32 )
		str = "FC32_Flash_sys_settings";
		
	else if( code == 0xFCC1 )
		str = "FCC1_Flash_Personality";
	else if( code == 0xFDC1 )
		str = "FDC1_Write_Personality";
	else if( code == 0xFCC1 )
		str = "FCC1_Read_Personality";
	
	else if( code == 0xFE34 )
		str = "FE34_DumpMem";
		
	else if( code == 0xFC42 )
		str = "FC42_set_HPS";		
	else if( code == 0xFE42 )
		str = "FE42_getTemps";

	else if( code == 0xFC44 )
		str = "FC44_NEF_oversized";

	else if( code == 0xFC46 )
		str = "FC46_NEFcomp_raw";

	else if( code == 0xFC53 )
		str = "FC53_set_VideoRegion";
		
	else if( code == 0xFC55 )
		str = "FC55_display_test_pattern";
		
	else if( code == 0xFC57 )
		str = "FC57_setup_brightness";

	else if( code == 0xFC5A )
		str = "FC5A_startHDMI";

	else if( code == 0xFC5B )
		str = "FC5B_stop_HDMI";


	else if( code == 0xFD63 )
		str = "FD63_Write_LensData";			
	else if( code == 0xFE63 )
		str = "FE63_Dump_LensData";			
			
	else if( code == 0xFC64 )
		str = "FC64_check_MvDefectTable";	
	else if( code == 0xFC65 )
		str = "FC65_MvDefectClear";	
		
	else if( code == 0xFD66 )
		str = "FD66_write_RamMvDefectTable";
	else if( code == 0xFE66 )
		str = "FE66_dump_RamMvDefectTable";
	else if( code == 0xFC66 )
		str = "FC66_reflash_MvDefectTable";
	
	else if( code == 0xFC80 )
		str = "FC80_restore_settongs";
		
	else if( code == 0xFC81 )
		str = "FC81_set_Lanuage";


	else if( code == 0xFE91 )
		str = "FE91_button_test";
		
	else if( code == 0xFE93 )
		str = "FE93_get_ScreenState";
		
	else if( code == 0xFC93 )
		str = "FC93_GetVhSensor";		
		
	else if( code == 0xFC96 )
		str = "FCA1_SetDateTime";
		
	else if( code == 0xFCA1 )
		str = "FCA1_Audio";
	
	else if( code == 0xFDB1 )
		str = "FDB1_write_SETx_NOP";
	else if( code == 0xFEB1 )
		str = "FEB1_dump_SETx";

	else if( code == 0xFDB2 )
		str = "FDB2_write_set1F";
	else if( code == 0xFEB2 )
		str = "FEB2_dump_set20";
		
	else if( code == 0xFDE1 )
		str = "FDE1_Start_Stop_MovieRec";
		

		
		

	else
	{
		str = sprintf("%04X", code);
		Message("ptp service code %s missing\n", str);
}
	
	return str;
}