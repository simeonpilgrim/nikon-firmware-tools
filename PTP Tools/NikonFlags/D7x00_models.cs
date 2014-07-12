using System;
using CameraControl.Devices;

namespace NikonFlags
{
    class d7000_103a : BaseModel_Camera, ICamera
    {
        uint OC_Fx31_base = 0x8FEDA428;
        uint OC_Fx31_ptr = 0x8FF0942C;

        public bool DoChecks(BaseMTPCamera cam)
        {
            var res = cam.ExecuteReadDataEx(0xFE34, OC_Fx31_ptr, 0x4, 0, 0, 0);

            uint OC_Fx31_base_check = (uint)((res.Data[0] << 24) | (res.Data[1] << 16) | (res.Data[2] << 8) | res.Data[3]);

            return OC_Fx31_base_check == OC_Fx31_base;
        }

        public uint GetKnownAddresss(KnownAddressList addrId)
        {
            switch (addrId)
            {
            case KnownAddressList.Set04Ram: return 0x8F84C431;
            case KnownAddressList.Set26: return 0x8F84C001;
            default: return 0;
            }
        }

        public uint GetKnownLenght(KnownAddressList addrId)
        {
            switch (addrId)
            {
            case KnownAddressList.Set04Ram: return 0x18;
            case KnownAddressList.Set26: return 0x1E;

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


    class d7000_104a : BaseModel_Camera, ICamera
    {
        uint OC_Fx31_base = 0x8FEDA428;
        uint OC_Fx31_ptr = 0x8FF0942C;

        public uint GetKnownAddresss(KnownAddressList addrId)
        {
            switch (addrId)
            {
            case KnownAddressList.Set04Ram: return 0x8F84C431;
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
 
        public bool DoChecks(BaseMTPCamera cam)
        {
            var res = cam.ExecuteReadDataEx(0xFE34, OC_Fx31_ptr, 0x4, 0, 0, 0);

            uint OC_Fx31_base_check = (uint)((res.Data[0] << 24) | (res.Data[1] << 16) | (res.Data[2] << 8) | res.Data[3]);

            return OC_Fx31_base_check == OC_Fx31_base;
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
