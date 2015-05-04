using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using CameraControl.Devices;
using PortableDeviceLib;

namespace CameraModels
{
    public enum KnownAddressList
    {
        Set04Ram,
        Set26,
    }

    public interface ICamera
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

    public class CameraBase
    {
        public static ICamera GetCameraObject(string model, string version)
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

        public static string FindNikonCamera()
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
    }
}
