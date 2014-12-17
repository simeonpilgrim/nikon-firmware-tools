using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using CameraControl.Devices;
using CameraControl.Devices.Classes;
using PortableDeviceLib;
using System.IO;
using System.Diagnostics;
using System.Threading;

namespace NikonDump
{
    class Program
    {
        const int MTP_OPERATION_GET_DEVICE_INFO = 0x1001;

        const int MTP_NIKON_ENTER_SERVICE = 0xFC01;
        const int MTP_NIKON_EXIT_SERVICE = 0xFC02;

        static string SN = "";
        static BaseMTPCamera cam;
        static bool FR_ServiceMode = false;
        static bool TMP19_Suspend = false;
        static string path = @"c:\temp\black levels\3\";


        static void Main(string[] args)
        {
            string camid = FindNikonCamera();
            if (camid == "")
            {
                Debug.WriteLine("** Failed to find Nikon Camera");
                Console.WriteLine("Failed to find Nikon Camera");
                return;
            }

            SN = camid.Split('#')[2];

            DeviceDescriptor descriptor = new DeviceDescriptor { WpdId = camid };
            cam = new BaseMTPCamera();
            bool i = cam.Init(descriptor);
            var rep = cam.ExecuteReadDataEx(MTP_OPERATION_GET_DEVICE_INFO);

            if (rep.ErrorCode == ErrorCodes.MTP_OK)
            {
                if (Directory.Exists(path) == false)
                    Directory.CreateDirectory(path);
                
                EnterServiceMode();

                //FR_BKT_Test();

                if (false)
                //int count = 0;
                //while (Console.KeyAvailable == false)
                {
                    //FR_Dump_6700xxxx();
                    //FR_D7000_alter_blacklevel_B();
                    
                    //FR_D7000_alter_blacklevel_B();

                    //Tmp19_DisablesDefectProcessing_Set(true);

                    //cam.SetProperty(0xD015, new byte[] { 0x01 }, 0, 0);
                    //cam.SetProperty(0x5010, new byte[] { 0x00, 0x00 }, 0, 0);
                    //cam.SetProperty(0xD10B, new byte[] { 0x01 }, 0, 0);
                    //cam.SetProperty(0x5004, new byte[] { 0x04 }, 0, 0);
                    ////FR_FC46_Compression(1);

                    //cam.SetProperty(0xd149, new byte[] { 0x01 }, 0, 0);
                    //cam.SetProperty(0xd054, new byte[] { 0x00 }, 0, 0);
                    //cam.SetProperty(0xd070, new byte[] { 0x00 }, 0, 0);

                    //FR_FC44_Overscan(1);

                    //cam.SetProperty(0xFD31, new byte[] { 0x00, 0x9C, 0x00, 0x01, 0x20, 0x00 }, 0x2176, 6, 0);

                    //byte[] iso = { 0xC0, 0xEF, 0xFC, 0x0E, 0xFC, 0x0E, 0xFC, 0x0E };
                    //cam.SetProperty(0xFD31, iso, 0xC90A, 8, 0);

                    //Thread.Sleep(1 * 1000);
                    //Tmp19_CMD_475D(50, 120);
                    //Thread.Sleep(3 * 1000);
                    //Tmp19_DisablesDefectProcessing_Set(false);

                    //Tmp19_Resume();
                    //Console.WriteLine("{0}", ++count);
                }


     // Step 1
                //Console.WriteLine("Dump 0x00xxxxxx");
                //FR_Dump_00xxxxxx();
                //Console.WriteLine("Dump 0x9xxxxxxx");
                //FR_Dump_9xxxxxxx();

     // Step 2
                //Console.WriteLine("Dump All");
                //FR_Dump_all2();

                //Tmp19_DumpEeprom();



//simeon HDMI testing

                //FR_test_USB_offbit();
                FR_Set04Ram_SetBit(0x14, 0x01); // Liveview auto off timer off.
                FR_Set04Ram_SetBit(0x16, 0x02); // Liveview raw save.

                //FR_Set04Ram_SetBit(0x16, 0x08); // ??

                //FR_FindMemChange();

                //FR_FC44_Overscan(1);

                //FR_Set04Ram_SetBit(0x07, 0x01); // LiveView Clean LCD

                //FR_D7000_dump_button_bits();
                //FR_Set_LED_test();

                //FR_D7000_alter_blacklevel();
                //FR_D7000_alter_iso_tab();
                //FR_D7000_alter_vid_tab();

                FR_USB_ClearOnBit();

                //FR_Set0B_b01b08();
                //FR_Set0B_b01b08_clear();
             //FR_StartLiveView();
             //FR_StartMovie();
             //Thread.Sleep(10 * 1000);
             //FR_EndLiveView();
                
                //ScanForServiceFunctions();
                //FR_FindMemChange();
                //FR_Dump_all2();
                //FR_Dump_all_via_FE31();
                //FR_Dump_00xxxxxx();
                //FR_Dump_01xxxxxx();
                //FR_Dump_8xxxxxxx();

                //Tmp19_Suspend();

                //Tmp19_Dump_ADCTab();
                //Tmp19_Dump_FFxxxx();
                //Tmp19_Dump_FF6A5C();

                //var va = Tmp19_Cmd_4152(0xFF5FDC, 0x20);

                //FR_FDE1_test();
                //FR_StartLiveView();
                //FR_StartMovie();
               
                //FR_FDE1_test();

                //FR_LiveView_hack();

                //Tmp19_Resume();
                //ExitServiceMode();
            }
        }

        static void pack_ISO(byte[] d, uint R, uint G1, uint B, uint G2)
        {
            d[0] = (byte)((R & 0x0F) << 4);
            d[1] = (byte)((R & 0xFF0) >> 4);

            d[2] = (byte)((G1 & 0xFF));
            d[3] = (byte)((G1 >> 8) & 0xFF);

            d[4] = (byte)((B & 0xFF));
            d[5] = (byte)((B >> 8) & 0xFF);

            d[6] = (byte)((G2 & 0xFF));
            d[7] = (byte)((G2 >> 8) & 0xFF);
        }


        static void FR_D7000_alter_blacklevel()
        {
            // 54D
            uint fps_blacklevel = 0x054D;
            uint len = 2;
            //byte[] v0 = { 0x58, 0x02 };
            //byte[] v0 = { 0x58, 0x01 };
            byte[] v0 = { 0x00, 0x00 };

            cam.SetProperty(0xFD31, v0, fps_blacklevel, len, 0);
        }

        static void FR_D7000_alter_blacklevel_B()
        {
            uint fps_blacklevel = 0xAF1C;
            uint len = 0x0c;
            uint max = (0x6C * 2);

            byte[] v0 = { 0x01, 0x3C, 0x00, 0x00, 0x00, 0x00, 0x01, 0x40, 0x00, 0x00, 0x00, 0x00 };

            for (uint i = 0; i < max; i += len)
            {
                cam.SetProperty(0xFD31, v0, fps_blacklevel + i, len, 0);
            }
        }

        static void FR_D7000_alter_iso_tab()
        {
            uint fps_iso_100 = 0xC48A;
            uint len = 8;
            byte[] v0 = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

            pack_ISO(v0, 1023, 1023,1023, 4083);
            for (uint i = 0; i < (8 * 10); i = i + 8)
            {
                cam.SetProperty(0xFD31, v0, fps_iso_100 + i, len, 0);
            }
        }


        static void FR_D7000_alter_vid_tab()
        {
            //// 30 fps idx 4 = 0x02662
            //uint addr_30 = 0x02662;
            //uint addr_10 = 0x0245E;
            //uint addr_24 = 0x0235C;
            //uint len = 0x102;
            //var res = cam.ExecuteReadDataEx(0xFE31, addr_10, len, 0, 0, 0);

            //Debug.WriteLine("data []: {0}", DataToHexString(res.Data, 0));
            //cam.SetProperty(0xFD31, res.Data, addr_24, len, 0);
            uint fps_30_a = 0x19b4 + 2;
            uint fps_30_b = 0x19bc + 2;
            uint fps_30_c = 0x1960 + 2;
            uint fps_30_d = 0x1970 + 2;
            uint len = 2;
            byte[] v0 = { 0x06, 0x66 }; // 65550 / (60/1.001)
            byte[] v1 = { 0x05, 0x14 }; // 52000 / (60/1.001)

            //cam.SetProperty(0xFD31, v0, fps_30_a, len, 0);
            //cam.SetProperty(0xFD31, v0, fps_30_b, len, 0);
            //cam.SetProperty(0xFD31, v1, fps_30_c, len, 0); 
            //cam.SetProperty(0xFD31, v1, fps_30_d, len, 0);

            uint v5 = 5;
            for (uint r10 = 0; r10 < 3; r10++) // 0,1,2
            {
                for (uint r11 = 0; r11 < 9; r11++) // < 9
                {
                    uint idx = 0xFEB0 + 0x1E6 + (r10 * 0xF30) + (0x17A * r11) + (v5 * 0x36) + 2;
                    //byte[] v3 = { 0x00, 0x01, 0x81, 0x1E }; // 98590;
                    //byte[] v4 = { 0x06 }; // 98590;

                    byte[] v3 = { 0x00, 0x09, 0x06, 0xb4 }; // 98590;
                    byte[] v4 = { 0x22 }; // 98590;

                    cam.SetProperty(0xFD31, v3, idx, 4, 0);
                    cam.SetProperty(0xFD31, v4, idx + 6 + 3, 1, 0);
                }
            }
        }


        static void FR_BKT_Test()
        {
            while (true)
            {
                var resa = ReadBytes(cam, 0x8F84BF7E + 0x2A, 0x4);
                var resb = ReadBytes(cam, 0x8F89FE84 + 0x2A, 0x4);
                var resc = ReadBytes(cam, 0x8F89FF5E + 0x2A, 0x4);
                Debug.WriteLine("Set2D.2A: {0} PackedA.2A: {1} PackedB.2A: {2}", DataToHexString(resa, 0), DataToHexString(resb, 0), DataToHexString(resc, 0));
                Thread.Sleep(500);
            }
        }

        static void FR_D7000_dump_button_bits()
        {
            while (true)
            {
                var res = cam.ExecuteReadDataEx(0xFE34, 0x8F95F6DC, 0x4, 0, 0, 0); // button_press_bits
                Debug.WriteLine("bits: {0}", DataToHexString(res.Data, 0));
                Thread.Sleep(200);
            }
        }

        static void FR_USB_ClearOnBit()
        {
            //8F84C3A2
            uint set0b_addr = 0x8F84C3A2;

            var b = ReadBytes(cam, set0b_addr + 1, 1);

            if ((b[0] & 0x40) != 0)
            {
                b[0] = (byte)((~0x40) & b[0]);
               WriteBytes(cam, set0b_addr + 1, b);

                Debug.WriteLine(" * CLEAR 0x40");
            }

            //var loop = 0;
            //while (true)
            //{
            //    loop += 1;
            //    var bb = ReadBytes(cam, set0b_addr + 1, 1);
            //    Debug.WriteLine("{1} 0x{0:X}", bb[0], loop);
            //    Thread.Sleep(500);
            //}


        }


        static void FR_Set0B_b01b08()
        {
            //8F84C3A2
            uint set0b_addr = 0x8F84C3A2;

            var b = ReadBytes(cam, set0b_addr + 1, 1);

            Debug.WriteLine(" byte 1 {0:x}", b[0]);
            if ((b[0] & 0x80) == 0)
            {
                b[0] = (byte)(0x80 | b[0]);
                WriteBytes(cam, set0b_addr + 1, b);

                Debug.WriteLine(" * SET 0x80");
            }
        }

        static void FR_Set0B_b01b08_clear()
        {
            //8F84C3A2
            uint set0b_addr = 0x8F84C3A2;

            var b = ReadBytes(cam, set0b_addr + 1, 1);

            if ((b[0] & 0x80) != 0)
            {
                b[0] = (byte)((~0x80) & b[0]);
                WriteBytes(cam, set0b_addr + 1, b);

                Debug.WriteLine(" * CLEAR 0x80");
            }
        }

        static public byte[] ReadBytes(BaseMTPCamera cam, uint addr, uint readlength)
        {
            var res = cam.ExecuteReadDataEx(0xFE34, addr, readlength, 0, 0, 0);

            return res.Data;
        }

        static public void WriteBytes(BaseMTPCamera cam, uint addr, byte[] data)
        {
            uint OC_Fx31_base = 0x8FEDA428;
            uint datalen = (uint)data.Length;
            byte[] tmp_data = new byte[800];
            for (uint i = 0; i < datalen; i += 800)
            {
                uint tmp_len = Math.Min(0x800, datalen - i);
                Array.Copy(data, i, tmp_data, 0, tmp_len);

                cam.SetProperty(0xFD31, tmp_data, (addr + i) - OC_Fx31_base, tmp_len, 0);
            }
        }

        static void FR_Set_LED_test()
        {
            var res = cam.ExecuteReadDataEx(0xFE34, 0x50000102, 0x1, 0, 0, 0); // 50000102

            res = cam.ExecuteReadDataEx(0xFE34, 0x8FF0942C, 0x4, 0, 0, 0); // 0x8FF0942C

            uint OC_Fx31_base = (uint)((res.Data[0] << 24) | (res.Data[1] << 16) | (res.Data[2] << 8) | res.Data[3]);

            res = cam.ExecuteReadDataEx(0xFE31, 0x50000102 - OC_Fx31_base, 0x1, 0, 0, 0); // set04ram_8F84C431
            Debug.WriteLine("res []: {0}", DataToHexString(res.Data, 0));

            res.Data[0] |= (byte)0x01;
            cam.SetProperty(0xFD31, res.Data, 0x50000102 - OC_Fx31_base, 0x1, 0);

            res = cam.ExecuteReadDataEx(0xFE34, 0x50000102, 0x1, 0, 0, 0); // set04ram_8F84C431

            Debug.WriteLine("res []: {0}", DataToHexString(res.Data, 0));
        }

        static void FR_Set04Ram_SetBit(int byteIdx, int bits)
        {
            var res = cam.ExecuteReadDataEx(0xFE34, 0x8F84C431, 0x18, 0, 0, 0); // set04ram_8F84C431

            Debug.WriteLine("res [16]: {0:X}", res.Data[16]);
            Debug.WriteLine("res []: {0}", DataToHexString(res.Data, 0));

            res = cam.ExecuteReadDataEx(0xFE34, 0x8FF0942C, 0x4, 0, 0, 0); // 0x8FF0942C

            Debug.WriteLine("res []: {0}", DataToHexString(res.Data, 0));
            uint OC_Fx31_base = (uint)((res.Data[0] << 24) | (res.Data[1] << 16) | (res.Data[2] << 8) | res.Data[3]);
            Debug.WriteLine("OC_Fx31_base {0:X}", OC_Fx31_base);

            res = cam.ExecuteReadDataEx(0xFE31, 0x8F84C431 - OC_Fx31_base, 0x18, 0, 0, 0); // set04ram_8F84C431
            Debug.WriteLine("res [16]: {0:X}", res.Data[16]);
            Debug.WriteLine("res []: {0}", DataToHexString(res.Data, 0));

            res.Data[byteIdx] |= (byte)bits;
            cam.SetProperty(0xFD31, res.Data, 0x8F84C431 - OC_Fx31_base, 0x18, 0);

            //  res = cam.(0xFD31, 0x8F84C431 - OC_Fx31_base, 0x18, 0, 0, 0); // set04ram_8F84C431
            res = cam.ExecuteReadDataEx(0xFE34, 0x8F84C431, 0x18, 0, 0, 0); // set04ram_8F84C431

            Debug.WriteLine("res [16]: {0:X}", res.Data[16]);
            Debug.WriteLine("res []: {0}", DataToHexString(res.Data, 0));

        }

        static void FR_LiveView_hack()
        {
            //while (true)
            //{
            //    var r = cam.ExecuteReadDataEx(0xFE34, 0x8f95f6dc, 0x4, 0, 0, 0); // set04ram_8F84C431
            //    Debug.WriteLine("res []: {0}", DataToHexString(r.Data, 0));
            //}


            var res = cam.ExecuteReadDataEx(0xFE34, 0x8F84C431, 0x18, 0, 0, 0); // set04ram_8F84C431

            Debug.WriteLine("res [16]: {0:X}", res.Data[16]);
            Debug.WriteLine("res []: {0}", DataToHexString(res.Data, 0));

            res = cam.ExecuteReadDataEx(0xFE34, 0x8FF0942C, 0x4, 0, 0, 0); // 0x8FF0942C

            Debug.WriteLine("res []: {0}", DataToHexString(res.Data, 0));
            uint OC_Fx31_base = (uint)((res.Data[0] << 24) | (res.Data[1] << 16) | (res.Data[2] << 8) | res.Data[3]);
            Debug.WriteLine("OC_Fx31_base {0:X}", OC_Fx31_base);

            res = cam.ExecuteReadDataEx(0xFE31, 0x8F84C431 - OC_Fx31_base, 0x18, 0, 0, 0); // set04ram_8F84C431
            Debug.WriteLine("res [16]: {0:X}", res.Data[16]);
            Debug.WriteLine("res []: {0}", DataToHexString(res.Data, 0));

            res.Data[16] |= 0x02;
            cam.SetProperty(0xFD31, res.Data, 0x8F84C431 - OC_Fx31_base, 0x18, 0);

          //  res = cam.(0xFD31, 0x8F84C431 - OC_Fx31_base, 0x18, 0, 0, 0); // set04ram_8F84C431
            res = cam.ExecuteReadDataEx(0xFE34, 0x8F84C431, 0x18, 0, 0, 0); // set04ram_8F84C431

            Debug.WriteLine("res [16]: {0:X}", res.Data[16]);
            Debug.WriteLine("res []: {0}", DataToHexString(res.Data, 0));

        }

        static void FR_StartLiveView()
        {
            var res = cam.ExecuteWithNoData(0x9201);
            Debug.WriteLine("res code: {0:X}", res);
        }

        static bool FR_WaitForLiveView()
        {
            for (int i = 0; i < 1000; i++)
            {
                var res = FR_DumpSet(0x34, 0x01);
                if (res.ErrorCode == 0x2001 && (res.Data[0] & 0x03) == 0x03)
                {
                    return true;
                }
            }
            return false;
        }

        static void FR_EndLiveView()
        {
            var res = cam.ExecuteWithNoData(0x9202);
            Debug.WriteLine("res code: {0:X}", res);
        }
        

        static MTPDataResponse FR_DumpSet(uint set, uint len)
        {
            return cam.ExecuteReadDataEx(0xFEB1,set,len,0,0,0);
        }

        static void FR_StartMovie()
        {
            var res = cam.ExecuteWithNoData(0x920A);
            Debug.WriteLine("FR_StartMovie res code: {0:X}", res);
        }

        static void FR_FDE1_test()
        {
            EnterServiceMode();
            FR_StartLiveView();
            FR_WaitForLiveView();

            var res = cam.ExecuteWithNoData(0xFDE1, 1, 0, 0, 0, 0);
            Debug.WriteLine("res code: {0:X}", res);

            Thread.Sleep(10 * 1000);

            res = cam.ExecuteWithNoData(0xFDE1, 0, 0, 0, 0, 0);
            Debug.WriteLine("res code: {0:X}", res);

            FR_EndLiveView();
        }


        static string FindNikonCamera()
        {
            if (PortableDeviceCollection.Instance == null)
            {
                const string AppName = "CameraControl";
                const int AppMajorVersionNumber = 1;
                const int AppMinorVersionNumber = 0;

                PortableDeviceCollection.CreateInstance(AppName, AppMajorVersionNumber, AppMinorVersionNumber);
                PortableDeviceCollection.Instance.AutoConnectToPortableDevice = false;
            }
            //_deviceEnumerator.RemoveDisconnected();

            foreach (PortableDevice portableDevice in PortableDeviceCollection.Instance.Devices)
            {
                Log.Debug("Connection device " + portableDevice.DeviceId);
                //TODO: avoid to load some mass storage in my computer need to find a general solution
                if (!portableDevice.DeviceId.StartsWith("\\\\?\\usb") && !portableDevice.DeviceId.StartsWith("\\\\?\\comp"))
                    continue;

                // find Nikon cameras
                if (portableDevice.DeviceId.Contains("vid_04b0"))
                    return portableDevice.DeviceId;

            }

            return "";
        }

        static bool EnterServiceMode()
        {
            if (FR_ServiceMode == false)
            {
                var res = cam.ExecuteWithNoData(MTP_NIKON_ENTER_SERVICE, (uint)0x0, (uint)0x0, (uint)0x0);
                FR_ServiceMode = (res == 0x2001) || (res == 0x201E);
            }

            return FR_ServiceMode;
        }

        static void ExitServiceMode()
        {
            if (FR_ServiceMode)
            {
                cam.ExecuteWithNoData(MTP_NIKON_EXIT_SERVICE, (uint)0x0, (uint)0x0, (uint)0x0);
                FR_ServiceMode = false;
            }
        }

        static string Get_Version_FirmB()
        {
            EnterServiceMode();

            var res = cam.ExecuteReadDataEx(0xFE01, (uint)0x0, (uint)0x80, (uint)0x0, 0, 0);
            return DataToCstring(res.Data, 0);
        }

        static string Get_CameraModel()
        {
            EnterServiceMode();

            var res = cam.ExecuteReadDataEx(0xFE02, (uint)0x0, (uint)0x80, (uint)0x0, 0, 0);
            return DataToCstring(res.Data, 0);
        }

        static string Get_Version_Q_OS()
        {
            EnterServiceMode();

            var res = cam.ExecuteReadDataEx(0xFE03, (uint)0x0, (uint)0x80, (uint)0x0, 0, 0);
            return DataToCstring(res.Data, 0);
        }

        static string Get_Version_B_OS()
        {
            EnterServiceMode();

            var res = cam.ExecuteReadDataEx(0xFE04, (uint)0x0, (uint)0x80, (uint)0x0, 0, 0);
            return DataToCstring(res.Data, 0);
        }

        static string Get_Version_X_XX()
        {
            EnterServiceMode();

            var res = cam.ExecuteReadDataEx(0xFE05, (uint)0x0, (uint)0x80, (uint)0x0, 0, 0);
            return DataToCstring(res.Data, 0);
        }

        static uint FR_FC44_Overscan(uint val)
        {
            EnterServiceMode();

            var res = cam.ExecuteReadDataEx(0xFC44, val, (uint)0x80, (uint)0x0, 0, 0);
            return res.ErrorCode;
        }

        static uint FR_FC46_Compression(uint val)
        {
            EnterServiceMode();

            var res = cam.ExecuteReadDataEx(0xFC46, val, (uint)0x00, (uint)0x0, 0, 0);
            return res.ErrorCode;
        }

        static void FR_FindMemChange()
        {
            List<byte[]> A = new List<byte[]>();
            List<byte[]> B = new List<byte[]>();

            List<uint> W = null;

            bool bitTestNotByte = false;

            uint thebase = 0x8F000000;

            bool looping = true;
            while (looping)
            {
                Console.WriteLine("Put camera in state A or B, then press A/B or Q to quit");
                var ch = Console.ReadKey();

                if (ch.Key == ConsoleKey.A )
                {
                    var data = FR_DumpMemory_xFE34(thebase, 0x01000000);
                    A.Add(data);
                    if(bitTestNotByte)
                        CompareMemsetBit(A, B, 0x01000000);
                    else
                        W = CompareMemset(A, B, 0x01000000);
                }
                else if (ch.Key == ConsoleKey.B)
                {
                    var data = FR_DumpMemory_xFE34(thebase, 0x01000000);
                    B.Add(data);
                    if (bitTestNotByte)
                        CompareMemsetBit(A, B, 0x01000000);
                    else
                        W = CompareMemset(A, B, 0x01000000);
                }
                else if (ch.Key == ConsoleKey.Q)
                {
                    looping = false;
                }
                else if (ch.Key == ConsoleKey.W && W != null)
                {
                    var data = FR_DumpMemory_xFE34(thebase, 0x01000000);
                    foreach (var w in W)
                    {
                        var ss = string.Format("0x{0:X8} {1:X2}", thebase + w, data[w]);

                        Console.WriteLine(ss);
                        Debug.WriteLine(ss);
                    }

                }
            }
        }

        static void CompareMemsetBit(List<byte[]> A, List<byte[]> B, uint Len)
        {
            if (A.Count < 2 || B.Count < 2)
            {
                Console.WriteLine("More Data Needed");
                return;
            }

            List<int> matches = new List<int>();
            for (int ii = 0; ii < Len * 8; ii++)
            {
                int i = ii / 8;
                int bd = ii % 8;

                int mask = 1 << bd;

                bool fail = false;
                int vala = A[0][i] & mask;
                int valb = B[0][i] & mask;
                if (vala == valb) continue;

                foreach (var a in A)
                {
                    if ((a[i] & mask) != vala)
                    {
                        fail = true;
                        break;
                    }
                }

                if (fail) continue;

                foreach (var b in B)
                {
                    if ((b[i] & mask) != valb)
                    {
                        fail = true;
                        break;
                    }
                }
                if (fail) continue;

                matches.Add(i * 8 + bd);
                if (matches.Count > 100)
                {
                    Console.WriteLine("Too many matches");
                    break;
                }
            }

            if (matches.Count == 0)
            {
                Console.WriteLine("No matches");
            }
            else if (matches.Count < 15)
            {
                Console.WriteLine("{0} matches", matches.Count); 
                foreach (var m in matches)
                {
                    int bb = m % 8;
                    uint i = (uint)(m / 8);
                    Console.WriteLine("0x{0:X8} {1}", i + 0x8F000000, bb);
                    Debug.WriteLine("0x{0:X8} {1}", i+ 0x8F000000, bb);
                }
                Debug.WriteLine("");
            }
            else
            {
                Console.WriteLine("{0} matches", matches.Count); 
            }

        }


        static List<uint> CompareMemset(List<byte[]> A, List<byte[]> B, uint Len)
        {
            if (A.Count < 2 || B.Count < 2)
            {
                Console.WriteLine("More Data Needed");
                return null;
            }

            List<int> matches = new List<int>();
            for (int i = 0; i < Len; i++)
            {
                bool fail = false;
                int vala = A[0][i];
                int valb = B[0][i];
                if (vala == valb) continue;

                foreach (var a in A)
                {
                    if (a[i] != vala)
                    {
                        fail = true;
                        break;
                    }
                }

                if (fail) continue;

                foreach (var b in B)
                {
                    if (b[i] != valb)
                    {
                        fail = true;
                        break;
                    }
                }
                if (fail) continue;

                matches.Add(i);
                if (matches.Count > 100)
                {
                    Console.WriteLine("Too many matches");
                    break;
                }
            }

            if (matches.Count == 0)
            {
                Console.WriteLine("No matches");
            }
            else if (matches.Count < 15)
            {
                List<uint> l = new List<uint>();
                foreach (var m in matches)
                {
                    var ss = string.Format("0x{0:X8}", m);
                    Console.WriteLine(ss);
                    Debug.WriteLine(ss);

                    l.Add((uint)m);
                }
                return l;
            }
            else if (matches.Count < 100)
            {
                Console.WriteLine("{0} matches", matches.Count);
            }
            
            return null;

        }

        static byte[] FR_DumpMemory_xFE34(uint addr, uint length)
        {
            var data = new byte[length];

            uint step = 0x20000;
            for (uint i = 0; i < length; i += step)
            {
                uint readlen = (i + step) > length ? (length - i) : step;

                var res = cam.ExecuteReadDataEx(0xFE34, addr + i, readlen, 0, 0, 0);
                if (res != null && res.ErrorCode == 0x2001)
                {
                    Array.Copy(res.Data,0,data, i, res.Data.Length);
                }
            }

            return data;
        }

        static void FR_Dump_all2()
        {
            EnterServiceMode();
            for (uint block = 0x8; block < 0x10; block++)
            {
                string filename = string.Format("{0}Nikon_DumpALL_{1}_FR_{2:X}xxxxxxx.bin", path, SN, block);
                using (var bw = new BinaryWriter(File.Open(filename, FileMode.Create, FileAccess.Write, FileShare.None)))
                {
                    FR_DumpMemory_xFE34(block << 28, 0x10000000, bw);
                }
            }
        }


        static void FR_Dump_all_via_FE31()
        {
            EnterServiceMode();
            uint start = 0;
            var counter_file = Path.Combine(path, "dump.txt");

            int shift = 16;
            uint blocksize = (uint)(1 << shift);
            uint blockcount = (uint)(1 << (32-shift));

            if (File.Exists(counter_file) )
            {
                uint tmpline;
                if (uint.TryParse(File.ReadAllText(counter_file), out tmpline))
                {
                    start = tmpline + 1;
                }
            }

            // reset for new run.
            if (start >= blockcount) start = 0;

            MemoryStream ms = new MemoryStream((int)blocksize);
            var bwA = new BinaryWriter(ms);
            Console.WriteLine(string.Format("{0:X4}", start));
            for (uint block = start; block < blockcount; block++)
            {
                File.WriteAllText(counter_file, block.ToString());
                FR_DumpMemory_xFE31(block << shift, blocksize, 0, bwA);

                if (NotAllSame(ms))
                {
                    string filename = Path.Combine(path, string.Format("Nikon_DumpALL_{0}_FR_{1:X4}.bin", SN, block));
                    using (var bw = new BinaryWriter(File.Open(filename, FileMode.Create, FileAccess.Write, FileShare.None)))
                    {
                        ms.WriteTo(bw.BaseStream);

                    }
                    Console.WriteLine(string.Format("{0:X4}", block));
                }
                else
                {
                    Console.Write('.');
                }


                ms.Seek(0, SeekOrigin.Begin);
                ms.SetLength (0);
            }
        }

        static bool NotAllSame(MemoryStream ms)
        {
            ms.Seek(0, SeekOrigin.Begin);
            if (ms.Length > 0)
            {
                int a = ms.ReadByte();

                while (a == ms.ReadByte()) { }


                return ms.Length != ms.Position;
            }

            return false;
        }

        static bool Tmp19_Suspend()
        {
            EnterServiceMode();

            if (TMP19_Suspend == false)
            {
                byte[] data = { 0x53, 0x53 };

                cam.SetProperty(0xFDB2, data, 0x0, (uint)data.Length, 0x0);

                var res = cam.ExecuteReadDataEx(0xFEB2, 0x0, 0x1, 0x0, 0, 0);
                TMP19_Suspend = res.ErrorCode == 0x2001;
            }

            return TMP19_Suspend;
        }



        static void Tmp19_Resume()
        {
            EnterServiceMode();
            if (TMP19_Suspend)
            {
                byte[] data = { 0x51, 0x51 };

                cam.SetProperty(0xFDB2, data, 0x0, (uint)data.Length, 0x0);

                cam.ExecuteReadDataEx(0xFEB2, 0x0, 0x1, 0x0, 0, 0);
                TMP19_Suspend = false;
            }
        }

        static void Tmp19_DisablesDefectProcessing_Set(bool turnOff)
        {
            var res = Tmp19_Cmd_4155_EepromRead(0x43, 1);
            Debug.WriteLine(res.Data[0]);

            if( turnOff )
                res.Data[0] |= 0x08;
            else
                res.Data[0] &= 0xF7;
            

            ////Tmp19_Cmd_4154_RAMWrite(0xFF4043, 1, res.Data);
            //Tmp19_Cmd_4157_EepromWrite(0x43, 1, res.Data);
            Tmp19_Cmd_4771_EepromSave();

            var chk = Tmp19_Cmd_4155_EepromRead(0x43, 1);
            Debug.WriteLine(chk.Data[0]);
        }

        static MTPDataResponse Tmp19_CMD_475D(byte a, byte b)
        {
            Tmp19_Suspend();

            byte[] data = new byte[4];
            data[0] = 0x47;
            data[1] = 0x5D;
            data[2] = a;
            data[3] = b;

            cam.SetProperty(0xFDB2, data, 0x0, (uint)data.Length, 0x0);

            return cam.ExecuteReadDataEx(0xFEB2, 0x0, 1, 0x0, 0, 0);
        }


        static MTPDataResponse Tmp19_Cmd_4152_RAMRead(int addr, int readLen)
        {
            Tmp19_Suspend();

            byte[] data = new byte[6];
            data[0] = 0x41;
            data[1] = 0x52;
            data[2] = (byte)((addr >> 16) & 0xFF);
            data[3] = (byte)((addr >> 8) & 0xFF);
            data[4] = (byte)((addr >> 0) & 0xFF);
            data[5] = (byte)readLen;

            cam.SetProperty(0xFDB2, data, 0x0, (uint)data.Length, 0x0);

            return cam.ExecuteReadDataEx(0xFEB2, 0x0, (uint)readLen, 0x0, 0, 0);
        }



        static MTPDataResponse Tmp19_Cmd_4154_RAMWrite(int addr, int dataLen, byte[] odata)
        {
            Tmp19_Suspend();
            uint newdatalen = (uint)(6 + dataLen);
            byte[] data = new byte[newdatalen];
            data[0] = 0x41;
            data[1] = 0x54;
            data[2] = (byte)((addr >> 16) & 0xFF);
            data[3] = (byte)((addr >> 8) & 0xFF);
            data[4] = (byte)((addr >> 0) & 0xFF);
            data[5] = (byte)newdatalen; 
            System.Array.Copy(odata, 0, data, 6, dataLen);

            cam.SetProperty(0xFDB2, data, 0x0, newdatalen, 0x0);

            return cam.ExecuteReadDataEx(0xFEB2, 0x0, (uint)dataLen, 0x0, 0, 0);
        }

        static MTPDataResponse Tmp19_Cmd_4155_EepromRead(int addr, int readLen)
        {
            Tmp19_Suspend();

            byte[] data = new byte[5];
            data[0] = 0x41;
            data[1] = 0x55;
            data[2] = (byte)((addr >> 8) & 0xFF);
            data[3] = (byte)((addr >> 0) & 0xFF);
            data[4] = (byte)readLen;

            cam.SetProperty(0xFDB2, data, 0x0, (uint)data.Length, 0x0);

            return cam.ExecuteReadDataEx(0xFEB2, 0x0, (uint)readLen, 0x0, 0, 0);
        }

        static MTPDataResponse Tmp19_Cmd_4157_EepromWrite(int addr, int dataLen, byte[] odata)
        {
            Tmp19_Suspend();
            uint newdatalen = (uint)(5 + dataLen);
            byte[] data = new byte[newdatalen];
            data[0] = 0x41;
            data[1] = 0x57;
            data[2] = (byte)((addr >> 8) & 0xFF);
            data[3] = (byte)((addr >> 0) & 0xFF);
            data[4] = (byte)newdatalen;
            System.Array.Copy(odata, 0, data, 5, dataLen);

            cam.SetProperty(0xFDB2, data, 0x0, newdatalen, 0x0);

            return cam.ExecuteReadDataEx(0xFEB2, 0x0, (uint)dataLen, 0x0, 0, 0);
        }

        static MTPDataResponse Tmp19_Cmd_4771_EepromSave()
        {
            Tmp19_Suspend();
            byte[] data = new byte[2];
            data[0] = 0x47;
            data[1] = 0x71;

            cam.SetProperty(0xFDB2, data, 0x0, 2, 0x0);

            return cam.ExecuteReadDataEx(0xFEB2, 0x0, (uint)1, 0x0, 0, 0);
        }

        static MTPDataResponse Tmp19_Cmd_47AF_ButtonState()
        {
            //Tmp19_Suspend();

            byte[] data = new byte[2];
            data[0] = 0x47;
            uint readLen = 0x0D;

            cam.SetProperty(0xFDB2, data, 0x0, (uint)data.Length, 0x0);

            return cam.ExecuteReadDataEx(0xFEB2, 0x0, readLen, 0x0, 0, 0);
        }

        static void Tmp19_DumpMem(int addr, int length, BinaryWriter sw)
        {
            int step = 0x80;
            for (int i = 0; i < length; i += step)
            {
                int readlen = (i + step) > length ? (length - i) : step;

                var res = Tmp19_Cmd_4152_RAMRead(addr + i, readlen);
                if (sw != null)
                {
                    sw.Write(res.Data);
                }
            }
        }

        static void Tmp19_Dump_ADCTab()
        {
            Tmp19_Suspend();

            string filename = path + "Nikon_Dump_" + SN + "_ADC_table.bin";
            using (var bw = new BinaryWriter(File.OpenWrite(filename)))
            {
                Tmp19_DumpMem(0xFF5FDC, 1080, bw);
            }
        }

        static byte[] Tmp19_GetCmdDialValue()
        {
            Tmp19_Suspend();

            var res = Tmp19_Cmd_4152_RAMRead(0xFF6EF4, 1);
            return res.Data;
        }

        static void Tmp19_Dump_FFxxxx()
        {
            Tmp19_Suspend();

            string filename = path + "Nikon_Dump_" + SN + "_TMP19_FFxxxx.bin";
            using (var bw = new BinaryWriter(File.OpenWrite(filename)))
            {
                Tmp19_DumpMem(0xFF0000, 0x10000, bw);
            }
        }
        static void Tmp19_Dump_FF6A5C()
        {
            Tmp19_Suspend();

            string filename = path + "Nikon_Dump_" + SN + "_TMP19_FF6A5C.bin";
            using (var bw = new BinaryWriter(File.OpenWrite(filename)))
            {
                Tmp19_DumpMem(0xFF6A5C, 0x80, bw);
            }
        }
        static void Tmp19_DumpEeprom()
        {
            Tmp19_Suspend();

            string filename = path + "Nikon_Dump_" + SN + "_TMP19_Eeprom.bin";
            using (var bw = new BinaryWriter(File.OpenWrite(filename)))
            {
                int length = 0x400; // d7000 = <-, d5100 = 0x200;
                int addr = 0x0000;
                int step = 0x80;
                for (int i = 0; i < length; i += step)
                {
                    int readlen = (i + step) > length ? (length - i) : step;

                    var res = Tmp19_Cmd_4155_EepromRead(addr + i, readlen);
                    bw.Write(res.Data);
                }
            }
        }


        static void ScanForServiceFunctions()
        {
            EnterServiceMode();

            string filename = path + "Nikon_Dump_" + SN + "_service_scan.txt";
            using (var tw = new StreamWriter(filename))
            {
                for (int code = 0xFE00; code < 0xFFFF; code++)
                {
                    var r = cam.ExecuteWithNoData(code, (uint)0x0, 0x0, 0x0, 0, 0);
                    if (r != 0x2005)
                    {
                        Console.WriteLine("code: {0:X4} res: 0x{1:X4}", code, r);
                        Debug.WriteLine("code: {0:X4} res: 0x{1:X4}", code, r);
                        tw.WriteLine("code: {0:X4} res: 0x{1:X4}", code, r);
                    }
                }
            }
        }

        static MTPDataResponse FR_ScreenState_xFE91()
        {
            uint readlen = 4;
            return cam.ExecuteReadDataEx(0xFE91, 0, readlen, 0, 0, 0);
        }

        static MTPDataResponse FR_CameraOrientation_xFE93()
        {
            uint readlen = 1;
            return cam.ExecuteReadDataEx(0xFE93, 0, readlen, 0, 0, 0);
        }

        static void FR_Dump_00xxxxxx()
        {
            EnterServiceMode();

            string filename = path + "Nikon_Dump_" + SN + "_FR_00xxxxxx.bin";
            using (var bw = new BinaryWriter(File.Open(filename, FileMode.Create, FileAccess.Write, FileShare.None)))
            {
                FR_DumpMemory_xFE34(0x000000, 0x1000000, bw);
                bw.Close();
            }
        }

        static void FR_Dump_01xxxxxx()
        {
            EnterServiceMode();

            string filename = path + "Nikon_Dump_" + SN + "_FR_01xxxxxx.bin";
            using (var bw = new BinaryWriter(File.Open(filename, FileMode.Create, FileAccess.Write, FileShare.None)))
            {
                FR_DumpMemory_xFE34(0x1000000, 0x1000000, bw);
            }
        }

        static void FR_Dump_8xxxxxxx()
        {
            EnterServiceMode();


            string filename = path + "Nikon_Dump_" + SN + "_FR_8xxxxxxx.bin";
            using (var bw = new BinaryWriter(File.Open(filename, FileMode.Create, FileAccess.Write, FileShare.None)))
            {
                FR_DumpMemory_xFE34(0x80000000, 0x10000000, bw);
            }
        }

        static void FR_Dump_9xxxxxxx()
        {
            EnterServiceMode();


            string filename = path + "Nikon_Dump_" + SN + "_FR_9xxxxxxx.bin";
            using (var bw = new BinaryWriter(File.Open(filename, FileMode.Create, FileAccess.Write, FileShare.None)))
            {
                FR_DumpMemory_xFE34(0x90000000, 0x10000000, bw);
            }
        }

        static void FR_Dump_6700xxxx()
        {
            EnterServiceMode();

            string filename = path + "Nikon_Dump_" + SN + "_FR_6700xxxx.bin";
            using (var bw = new BinaryWriter(File.Open(filename, FileMode.Create, FileAccess.Write, FileShare.None)))
            {
                FR_DumpMemory_xFE34(0x67000000, 0x10000, bw);
            }
        }

        static void FR_DumpMemory_xFE34(uint addr, uint length, BinaryWriter sw)
        {
            uint step = 0x20000;
            for (uint i = 0; i < length; i += step)
            {
                uint readlen = (i + step) > length ? (length - i) : step;

                var res = cam.ExecuteReadDataEx(0xFE34, addr + i, readlen, 0, 0 ,0);
                if (sw != null && res != null && res.ErrorCode == 0x2001)
                {
                    sw.Write(res.Data);
                }
            }
        }


        static void FR_DumpMemory_xFE31(uint addr, uint length, uint offset, BinaryWriter sw)
        {
            uint step = 0x800;
            for (uint i = 0; i < length; i += step)
            {
                uint readlen = (i + step) > length ? (length - i) : step;

                var res = cam.ExecuteReadDataEx(0xFE31, i + (addr- offset), readlen, 0, 0, 0);
                if (sw != null)
                {
                    sw.Write(res.Data);
                }
            }
        }

        static string DataToCstring(byte[] data, int offset)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = offset; i < data.Length && data[i] != 0; i++)
            {
                sb.Append((char)data[i]);
            }

            return sb.ToString();
        }


        static string DataToHexString(byte[] data, int offset)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = offset; i < data.Length; i++)
            {
                sb.Append(string.Format("{0:X2} ", data[i]));
            }

            return sb.ToString();
        }
    }
}
