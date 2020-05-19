using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Nikon_Patch
{
    public class PatchSet
    {
        public PatchSet(PatchLevel patchLevel, string name, Patch[] _changes, params Patch[][] _incompatible)
        {
            PatchStatus = patchLevel;
            Name = name;
            changes = _changes; 
            incompatible = _incompatible;
        }

        public Patch[] changes;
        public Patch[][] incompatible;

        public string Name { get; set; }
        public string Description { get; set; }
        public bool Enabled { get { return false; } }

        public PatchLevel PatchStatus;
    }
}
