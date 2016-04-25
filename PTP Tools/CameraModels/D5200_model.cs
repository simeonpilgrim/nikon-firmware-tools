using System;
using CameraControl.Devices;

namespace CameraModels
{
    public class Base_Model
    {
        public bool CanRandRead { get; protected set; }
        public bool CanRandWrite { get; protected set; }
        public bool CanEmpromDump { get; protected set; }
        public bool ModelKnown { get; protected set; }
        public bool FirmwareKnown { get; protected set; }
    }

    public class Unknown_Model : Base_Model
    {
        public Unknown_Model()
        {
            CanEmpromDump = false;
            CanRandRead = false;
            CanRandWrite = false;
            ModelKnown = false;
            FirmwareKnown = false;
        }
    }

    public static class CameraModelFactory
    {
        public static Base_Model ResolveCameraDetails(string model, string firmware)
        {
            switch(model)
            {
                case "D5200":
                    return D5200_Model.ResolveCameraDetails(firmware);
                default:
                    return new Unknown_Model();
            }
        }
    }

    public class D5200_Model : Base_Model
    {
        public D5200_Model()
        {
            CanEmpromDump = true;
            CanRandRead = true;
            CanRandWrite = true;
            ModelKnown = true;
            FirmwareKnown = false;
        }

        public static Base_Model ResolveCameraDetails(string firmware)
        {
            switch (firmware)
            {
                case "102":
                    return new D5200_102();
                default:
                    return new D5200_Model();
            }
        }
    }

    public class D5200_102 : D5200_Model
    {
        public D5200_102()
        {
            FirmwareKnown = true;
        }
    }

    class d5200_102 : BaseModel_Camera, ICamera
    {
        public bool DoChecks(BaseMTPCamera cam)
        {
            return true;
        }

        public uint GetKnownAddresss(KnownAddressList addrId)
        {
            switch (addrId)
            {
                //case KnownAddressList.Set04Ram: return 0x8F85159D;
                default: return 0;
            }
        }

        public uint GetKnownLenght(KnownAddressList addrId)
        {
            switch (addrId)
            {
                case KnownAddressList.Set04Ram: return 0x1A;
                default: return 0;
            }
        }

        public byte[] ReadBytes(BaseMTPCamera cam, uint addr, uint readlength)
        {
            return ReadBytes_FE34(cam, addr, readlength);
        }

        public void WriteBytes(BaseMTPCamera cam, uint addr, byte[] data)
        {
            WriteBytes_FD34(cam, addr, data);
        }
    }
}
