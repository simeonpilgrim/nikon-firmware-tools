namespace Nikon_Patch
{
    class D810_0102 : Firmware
    {
        public D810_0102()
        {
            p = new Package();
            Model = "D810";
            Version = "1.02";
        }
    }

    class D810_0112 : Firmware
    {
        public D810_0112()
        {
            p = new Package();
            Model = "D810";
            Version = "1.12";
        }
    }

    class D810A_0102 : Firmware
    {
        public D810A_0102()
        {
            p = new Package();
            Model = "D810A";
            Version = "1.02";
        }
    }
}