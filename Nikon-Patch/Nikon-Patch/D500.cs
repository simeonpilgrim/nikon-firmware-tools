namespace Nikon_Patch
{
    class D500_0111 : Firmware
    {
        public D500_0111()
        {
            p = new Package();
            Model = "D500";
            Version = "1.11";
        }
    }

    class D500_0113 : Firmware
    {
        public D500_0113()
        {
            p = new Package();
            Model = "D500";
            Version = "1.13";
        }
    }
}
