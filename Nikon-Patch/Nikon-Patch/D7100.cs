using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Linq.Expressions;
using System.Diagnostics;
using System.Text;


namespace Nikon_Patch
{
    class D7100_0101 : Firmware
    {
        public D7100_0101()
        {
            p = new Package();
            Model = "D7100";
            Version = "1.01";

        }
    }
}
