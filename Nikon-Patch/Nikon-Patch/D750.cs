using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Linq.Expressions;
using System.Diagnostics;
using System.Text;


namespace Nikon_Patch
{
    class D750_0101 : Firmware
    {
        public D750_0101()
        {
            p = new Package();
            Model = "D750";
            Version = "1.01";

        }
    }
 }