using System.ComponentModel;
using System.Windows.Forms;

namespace Nikon_Patch
{
    public class PatchSet : INotifyPropertyChanged
    {
        public PatchSet(PatchLevel patchLevel, string name, Patch[] _changes, params Patch[][] _incompatible)
        {
            PatchStatus = patchLevel;
            Name = name;
            Enabled = false;
            changes = _changes;
            incompatible = _incompatible;
        }

        public Patch[] changes;
        public Patch[][] incompatible;

        public string Name { get; set; }
        public string Description { get; set; }

        public PatchLevel PatchStatus;
        bool enabled;
        public bool Enabled
        {
            get { return enabled; }
            set
            {
                if (value == true && incompatible.Length > 0 && patches != null)
                {
                    int i = 0;
                    foreach (var pps in patches.Items)
                    {
                        foreach (var inc in incompatible)
                        {
                            PatchSet ps = (PatchSet)pps;
                            if (ps.changes == inc && ps.Enabled == true)
                            {
                                ps.Enabled = false;
                            }

                        }
                        i += 1;
                    }
                }
                enabled = value;

                NotifyPropertyChanged("Enabled");
            }
        }

        private void NotifyPropertyChanged(string name)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));
        }

        public event PropertyChangedEventHandler PropertyChanged;

        static ListBox patches = null; // hack
        public static void SetListBox(ListBox lb)
        {
            patches = lb;
        }
    }
}
