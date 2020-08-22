namespace Nikon_Patch
{
    class D3400_0112 : Firmware
    {
        public D3400_0112()
        {
            p = new Package();
            Model = "D3400";
            Version = "1.12";
        }
    }

    //0x28, 0xC8, 0xDB, 0x35, 0x83, 0xC1, 0x9A, 0x6A, 0x26, 0xEC, 0x21, 0xB4, 0x7D, 0x6D, 0x4E, 0xC5, 

    class D3400_0113 : Firmware
    {
        public D3400_0113()
        {
            p = new Package();
            Model = "D3400";
            Version = "1.13";
        }
    }
}
