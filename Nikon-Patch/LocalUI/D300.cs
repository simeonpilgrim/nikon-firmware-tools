namespace Nikon_Patch
{
    class D300_0111_B: Firmware
    {
        Patch[] patch_star_eater = {
            new Patch(0, 0xE6976, new byte[] { 0xED, 0x12 }, new byte[] { 0xE0, 0x12 }), 
            new Patch(0, 0xE6BAE, new byte[] { 0xED, 0x03 }, new byte[] { 0xE0, 0x03 }),
                                  };


        public D300_0111_B()
        {
            p = new OldSinglePackage();
            Model = "D300";
            Version = "1.11 B File";

            Patches.Add(new PatchSet(PatchLevel.Released, "Disable Nikon Star Eater", patch_star_eater));
        }
    }

    class D300_0110_B : Firmware
    {
        public D300_0110_B()
        {
            p = new OldSinglePackage();
            Model = "D300";
            Version = "1.10 B File";
        }
    }
}
