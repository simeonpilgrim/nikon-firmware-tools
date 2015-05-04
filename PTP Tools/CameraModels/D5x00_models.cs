using System;
using CameraControl.Devices;

namespace CameraModels
{
    class d5100_101b : BaseModel_Camera, ICamera
    {
        uint OC_Fx31_base = 0x8FECC1FC;
        uint OC_Fx31_ptr = 0x8FEFB200;

        public bool DoChecks(BaseMTPCamera cam) {
            var res = ReadBytes(cam, OC_Fx31_ptr, 0x4);

            uint OC_Fx31_base_check = (uint)((res[0] << 24) | (res[1] << 16) | (res[2] << 8) | res[3]);

            return OC_Fx31_base_check == OC_Fx31_base;
        }

        public uint GetKnownAddresss(KnownAddressList addrId)
        {
            switch (addrId)
            {
            case KnownAddressList.Set04Ram: return 0x8F85159D;
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
            WriteBytes_FD31(cam, OC_Fx31_base, addr, data);
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
