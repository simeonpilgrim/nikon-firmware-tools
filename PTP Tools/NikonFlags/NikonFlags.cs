using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using CameraControl.Devices.Classes;
using CameraControl.Devices;
using PortableDeviceLib;
using System.Globalization;

namespace NikonFlags
{
    class NikonFlags
    {
        const int MTP_OPERATION_GET_DEVICE_INFO = 0x1001;

        const int MTP_NIKON_ENTER_SERVICE = 0xFC01;
        const int MTP_NIKON_EXIT_SERVICE = 0xFC02;

        static string SN = "";
        static BaseMTPCamera cam;
        static bool FR_ServiceMode = false;

        static void LogIt(string fmt, params object[] objs)
        {
            Debug.WriteLine(fmt, objs);
            Console.WriteLine(fmt, objs);
        }

        static void Main(string[] args)
        {
            LogIt("Nikon Flags v1.1");

            string camid = FindNikonCamera();

            if (camid == "")
            {
                LogIt("Failed to find Nikon Camera");
                return;
            }


            SN = camid.Split('#')[2];

            DeviceDescriptor descriptor = new DeviceDescriptor { WpdId = camid };
            cam = new BaseMTPCamera();
            bool i = cam.Init(descriptor);
            var rep = cam.ExecuteReadDataEx(MTP_OPERATION_GET_DEVICE_INFO);

            if (rep.ErrorCode != ErrorCodes.MTP_OK)
            {
                var s = "Failed to initialise";
                LogIt(s);
                return;
            }

            var version = Get_Version_FirmB();
            var model = Get_CameraModel();

            var cam_obj = GetCameraObject(model, version);

            LogIt(model);
            LogIt(version);

            if (cam_obj == null)
            {
                var s = "Camera model/version not recognised, please email: simeon.pilgrim@gmail.com with the details";

                LogIt(s);
                return;
            }

            if (cam_obj.DoChecks(cam) == false)
            {
                var s = "Camera model/version checks failed, please email: simeon.pilgrim@gmail.com with the details";

                LogIt(s);
                return;
            }

            var flagAddr = cam_obj.GetKnownAddresss(KnownAddressList.Set04Ram);
            var flagLen = cam_obj.GetKnownLenght(KnownAddressList.Set04Ram);

            //var flagAddr = cam_obj.GetKnownAddresss(KnownAddressList.Set26);
            //var flagLen = cam_obj.GetKnownLenght(KnownAddressList.Set26);

            LogIt("");
            LogIt("Flag Opp [Value]");

            LogIt("   Flag - byte between 0 - 0x{0:X} inclusive", flagLen - 1);
            LogIt("   Opp - A and, O or, C clear, D display, Q quit (no flag needed)");
            LogIt("   Value - hex value used in A, O, C operations");
            LogIt(" '0 A 0x01' will apply AND 0x01 with currnet set04ram[0x00]");
            LogIt(" '0x12 O 0x10' will apply OR 0x10 with current set04ram[0x12]");
            LogIt(" '0x11 C 0x01' will apply AND 0xFE (~0x01), aka clear current set04ram[0x11]");
            LogIt(" '15 D' will display current set04ram[0x15]");
           
            uint offset = 0;
            while (offset < flagLen)
            {
                var line = Console.ReadLine().ToLower().Trim();
                var parts = line.Split(' ');

                if (line == "q") break;

                if (parts.Length < 2) continue;
                var a = parts[0].ToLower().Trim();
                if (a.StartsWith("0x")) a = a.Substring(2);
                if (uint.TryParse(a, NumberStyles.HexNumber, CultureInfo.InvariantCulture, out offset) &&
                    offset < flagLen)
                {
                    var b = parts[1].ToLower().Trim();
                    if (b.Length >= 1)
                    {
                        var opp = b[0];
                        if (opp != 'a' && opp != 'o' && opp != 'c' && opp != 'd')
                        {
                            LogIt("Bad operation character.");
                            break;
                        }

                        byte value = 0;
                        if (opp == 'a' || opp == 'o' || opp == 'c')
                        {
                            if (parts.Length != 3)
                            {
                                LogIt("Value missing");
                                continue;
                            }

                            var c = parts[2].ToLower().Trim();
                            if (c.StartsWith("0x")) c = c.Substring(2);

                            if (byte.TryParse(c, NumberStyles.HexNumber, CultureInfo.InvariantCulture, out value) == false)
                            {
                                LogIt("Bad value input");
                                continue;
                            }
                        }

                        var data = cam_obj.ReadBytes(cam, flagAddr + offset, 1);

                        if (data.Length != 1)
                        {
                            LogIt("Bad data read.");
                            break;
                        }

                        if (opp == 'd')
                        {
                            LogIt("Set04ram[0x{0:X}] = 0x{1:X}", offset, data[0]);
                            continue;
                        }

                        if (opp == 'a')
                            data[0] &= value;

                        if (opp == 'o')
                            data[0] |= value;

                        if (opp == 'c')
                            data[0] &= (byte)(~value);

                        LogIt("write: Set04ram[0x{0:X}] = 0x{1:X}", offset, data[0]);
                        cam_obj.WriteBytes(cam, flagAddr + offset, data);

                    }

                }
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

        static string DataToCstring(byte[] data, int offset)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = offset; i < data.Length && data[i] != 0; i++)
            {
                sb.Append((char)data[i]);
            }

            return sb.ToString();
        }

        private static void ParseDeviceInfo(MTPDataResponse rep)
        {
            int offset = 0;
            int readlen;

            int StandardVersion = 0;

            readlen = GetUWord(rep.Data, offset, ref StandardVersion);
            offset += readlen;
        }


        static int GetUWord(byte[] data, int offset, ref int word)
        {
            if( data.Length >= (offset+2) )
            {
                word = data[offset] + (data[offset+1] << 8);
                return 2;
            }
            
            return 0;
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

        static ICamera GetCameraObject(string model, string version)
        {
            var cat = model + "_" + version;
            switch (cat)
            {
            case "D7000_103a":
                return new d7000_103a(); // v1.0

            case "D7000_104a":
                return new d7000_104a(); // v1.1

            case "D600_101a":
                return new d600_101a(); // v1.1

            case "Q0550_100e":
                return new d3000_100e(); // v1.0

            case "D3100_101b":
                return new d3100_101b(); // v1.0

            case "D5100_101b":
                return new d5100_101b(); // v1.0

            case "D5200_102":
                return new d5200_102(); // v1.2
            }
            return null;
        }
    }

    enum KnownAddressList
    {
        Set04Ram,
        Set26,
    }

    interface ICamera
    {
        bool DoChecks(BaseMTPCamera cam);
        byte[] ReadBytes(BaseMTPCamera cam, uint addr, uint readlength);
        void WriteBytes(BaseMTPCamera cam, uint addr, byte[] data);
        
        uint GetKnownAddresss(KnownAddressList addrId);
        uint GetKnownLenght(KnownAddressList addrId);
    }

    class BaseModel_Camera
    {
        public void WriteBytes_FD31(BaseMTPCamera cam, uint OC_Fx31_base, uint addr, byte[] data)
        {
            uint datalen = (uint)data.Length;
            byte[] tmp_data = new byte[800];
            for (uint i = 0; i < datalen; i += 800)
            {
                uint tmp_len = Math.Min(0x800, datalen - i);
                Array.Copy(data, i, tmp_data, 0, tmp_len);

                cam.SetProperty(0xFD31, tmp_data, (addr + i) - OC_Fx31_base, tmp_len, 0);
            }
        }

        public byte[] ReadBytes_FE31(BaseMTPCamera cam, uint OC_Fx31_base, uint addr, uint readlength)
        {
            uint step = 0x800;
            byte[] data = new byte[readlength];

            for (uint i = 0; i < readlength; i += step)
            {
                uint tmp_len = Math.Min(step, readlength - i);
                var res = cam.ExecuteReadDataEx(0xFE31, (addr + i) - OC_Fx31_base, tmp_len, 0, 0, 0);
                Array.Copy(res.Data, 0, data, i, tmp_len);
            }

            return data;
        }        
        
        public byte[] ReadBytes_FE34(BaseMTPCamera cam, uint addr, uint readlength)
        {
            var res = cam.ExecuteReadDataEx(0xFE34, addr, readlength, 0, 0, 0);

            return res.Data;
        }

        public void WriteBytes_FD34(BaseMTPCamera cam, uint addr, byte[] data)
        {
            cam.SetProperty(0xFD34, data, addr, (uint)data.Length, 0);
        }
    }

}
