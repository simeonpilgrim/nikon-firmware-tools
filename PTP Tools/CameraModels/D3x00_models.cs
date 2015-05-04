using System;
using CameraControl.Devices;

namespace CameraModels
{
    class d3000_100e : BaseModel_Camera, ICamera
    {
        uint OC_Fx31_base = 0x87DE2C44;
        uint OC_Fx31_ptr = 0x87DE8C34;

        public bool DoChecks(BaseMTPCamera cam) {
            var res = ReadBytes(cam, OC_Fx31_ptr, 0x4);

            uint OC_Fx31_base_check = (uint)((res[0] << 24) | (res[1] << 16) | (res[2] << 8) | res[3]);

            return OC_Fx31_base_check == OC_Fx31_base;
        }

        public uint GetKnownAddresss(KnownAddressList addrId)
        {
            switch (addrId)
            {
            case KnownAddressList.Set04Ram: return 0x87D164D5;
            default: return 0;
            }
        }

        public uint GetKnownLenght(KnownAddressList addrId)
        {
            switch (addrId)
            {
            case KnownAddressList.Set04Ram: return 0x17;
            default: return 0;
            }
        }

        public byte[] ReadBytes(BaseMTPCamera cam, uint addr, uint readlength)
        {
            return ReadBytes_FE31(cam, OC_Fx31_base, addr, readlength);
        }

        public void WriteBytes(BaseMTPCamera cam, uint addr, byte[] data)
        {
            WriteBytes_FD31(cam, OC_Fx31_base, addr, data);
        }
    }


    class d3100_101b : BaseModel_Camera, ICamera
    {
        uint OC_Fx31_base = 0x8FE0F1C0;
        uint OC_Fx31_ptr = 0x8FE2D608;

        public bool DoChecks(BaseMTPCamera cam)
        {
            var res = ReadBytes(cam, OC_Fx31_ptr, 0x4);

            uint OC_Fx31_base_check = (uint)((res[0] << 24) | (res[1] << 16) | (res[2] << 8) | res[3]);

            return OC_Fx31_base_check == OC_Fx31_base;
        }

        public uint GetKnownAddresss(KnownAddressList addrId)
        {
            switch (addrId)
            {
            case KnownAddressList.Set04Ram: return 0x8FA9D8B8;
            default: return 0;
            }
        }

        public uint GetKnownLenght(KnownAddressList addrId)
        {
            switch (addrId)
            {
            case KnownAddressList.Set04Ram: return 0x18;
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

}
