using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Windows.Data;
using System.Reflection;
using System.Windows.Browser;

namespace Nikon_Patch
{
    public partial class MainPage : UserControl
    {
        public MainPage()
        {
            InitializeComponent();

            HelpMap.Add("Disable Nikon Star Eater", "This patch is for astrophotography ONLY. It turns the HPS filter off, which hides hot pixels.");
            HelpMap.Add("Video 1080 HQ 36mbps Bit-rate", "This patch changes the bitrate from ~20mbps to ~36mbps. This will reduce recording time due to the 4gb limitation of the recording process. Due to the increased data rate: depending on the speed of your SD card, the record may abruptly stop and the file may not playback in camera.");
            HelpMap.Add("Video 1080 HQ 54mbps Bit-rate", "This patch changes the bitrate from ~20mbps to ~54mbps. This will reduce recording time due to the 4gb limitation of the recording process. Due to the increased data rate: depending on the speed of your SD card, the record may abruptly stop and the file may not playback in camera.");
            HelpMap.Add("Video 1080 HQ 64mbps Bit-rate", "This patch changes the bitrate from ~20mbps to ~64mbps. This will reduce recording time due to the 4gb limitation of the recording process. Due to the increased data rate: depending on the speed of your SD card, the record may abruptly stop and the file may not playback in camera.");
            HelpMap.Add("Video 1080 24fps HQ 36mbps", "This patch changes the bitrate from ~20mbps to ~36mbps. This will reduce recording time due to the 4gb limitation of the recording process. Due to the increased data rate: depending on the speed of your SD card, the record may abruptly stop and the file may not playback in camera.");
            HelpMap.Add("Video 1080 24fps HQ 49mbps", "This patch changes the bitrate from ~20mbps to ~49mbps. This will reduce recording time due to the 4gb limitation of the recording process. Due to the increased data rate: depending on the speed of your SD card, the record may abruptly stop and the file may not playback in camera.");
            HelpMap.Add("Video 1080 24fps HQ 64mbps", "This patch changes the bitrate from ~20mbps to ~64mbps. This will reduce recording time due to the 4gb limitation of the recording process. Due to the increased data rate: depending on the speed of your SD card, the record may abruptly stop and the file may not playback in camera.");
            HelpMap.Add("Video 1080 HQ 36mbps Bit-rate NQ old HQ", "This patch changes the HQ bitrate from ~20mbps to ~36mbps. This will reduce recording time due to the 4gb limitation of the recording process. Due to the increased data rate: depending on the speed of your SD card, the record may abruptly stop and the file may not playback in camera. The NQ bitrate set to the old HQ rate");
            HelpMap.Add("Video 1080 HQ 54mbps Bit-rate NQ old HQ", "This patch changes the HQ bitrate from ~20mbps to ~54mbps. This will reduce recording time due to the 4gb limitation of the recording process. Due to the increased data rate: depending on the speed of your SD card, the record may abruptly stop and the file may not playback in camera. The NQ bitrate set to the old HQ rate");
            HelpMap.Add("Video 1080 HQ 64mbps Bit-rate NQ old HQ", "This patch changes the HQ bitrate from ~20mbps to ~64mbps. This will reduce recording time due to the 4gb limitation of the recording process. Due to the increased data rate: depending on the speed of your SD card, the record may abruptly stop and the file may not playback in camera. The NQ bitrate set to the old HQ rate");
            HelpMap.Add("Video 1080 24fps HQ 36mbps NQ old HQ", "This patch changes the HQ bitrate from ~20mbps to ~36mbps. This will reduce recording time due to the 4gb limitation of the recording process. Due to the increased data rate: depending on the speed of your SD card, the record may abruptly stop and the file may not playback in camera. The NQ bitrate set to the old HQ rate");
            HelpMap.Add("Video 1080 24fps HQ 49mbps NQ old HQ", "This patch changes the HQ bitrate from ~20mbps to ~49mbps. This will reduce recording time due to the 4gb limitation of the recording process. Due to the increased data rate: depending on the speed of your SD card, the record may abruptly stop and the file may not playback in camera. The NQ bitrate set to the old HQ rate");
            HelpMap.Add("Video 1080 24fps HQ 64mbps NQ old HQ", "This patch changes the HQ bitrate from ~20mbps to ~64mbps. This will reduce recording time due to the 4gb limitation of the recording process. Due to the increased data rate: depending on the speed of your SD card, the record may abruptly stop and the file may not playback in camera. The NQ bitrate set to the old HQ rate");
            HelpMap.Add("Video 1080/720 HQ 64mbps Bit-rate NQ old HQ", "");
            HelpMap.Add("Liveview Manual Control ISO/Shutter", "This patch allows the user manual control over ISO and shutter speed in Liveview.");

            
            HelpMap.Add("Remove Time Based Video Restrictions", "This patch removed the time limitation of the Recording process, the 4gb file limit is still in place.");
            HelpMap.Add("Liveview No Display Auto Off", "This is intended for video production/streaming people. None of the UI screen will turn off due to in action.");
            HelpMap.Add("Clean HDMI & LCD Liveview", "This is intended for video production/streaming people. NONE of the live view screen UI Icons/Focus are drawn.");
            HelpMap.Add("NEF Compression Off", "The files are uncompressed raw, therefore huge, stick to 'NEF Compression Lossless'.");
            HelpMap.Add("NEF Compression Lossless", "Change from a lossy compression of RAW files to a lossless compression method used on higher end models.");
            HelpMap.Add("Jpeg Compression - Quality (vs. Space)", "Make the camera use less compression on Jpeg pictures. Therefore the jpg files are larger.");
            HelpMap.Add("Non-Brand Battery", "Allows the use of most 3rd party batteries. NEVER install new firmware while using 3rd party batteries.");

            HelpMap.Add("True Dark Current", "This Astro patch makes the camera not truncate black to zero, allowing for better calculation of very low light stars");

        }

        Firmware firm = null;

        bool canSave = false;
        public bool CanSave
        {
            get { return canSave; }
            set { }
        }

        public void PatchSetChanged(object sender, PropertyChangedEventArgs e)
        {
            if (sender is PatchSet)
            {
                if (e.PropertyName == "Enabled")
                {
                    canSave = firm != null && firm.Patches.Any(p => p.Enabled);
                    bSaveFirmware.IsEnabled = canSave;
                }
            }
        }

        private void SendEvent(string cat, string action)
        {
            try
            {
                HtmlPage.Window.Invoke("EventFunction", new[] { cat, action, "" });
            }
            catch (Exception) { };
        }

        private void SendEvent(string cat, string action, string label)
        {
            try
            {
                HtmlPage.Window.Invoke("EventFunction", new[] { cat, action, label });
            }
            catch (Exception) { };
        }

        private void bOpenFileDialog_Click(object sender, RoutedEventArgs e)
        {
            // Create an instance of the open file dialog box.
            OpenFileDialog openFileDialog1 = new OpenFileDialog();

            // Set filter options and filter index.
            openFileDialog1.Filter = "Binary Files (.bin)|*.bin|All Files (*.*)|*.*";
            openFileDialog1.FilterIndex = 1;
            openFileDialog1.Multiselect = false;

            // Call the ShowDialog method to show the dialog box.
            bool? userClickedOK = openFileDialog1.ShowDialog();

            // Process input if the user clicked OK.
            if (userClickedOK == true)
            {
                SendEvent("TryFile", openFileDialog1.File.Name);

                var ext = openFileDialog1.File.Extension.ToLower();
                if (ext == ".dmg")
                {
                    MessageBox.Show("Please open the DMG file and select the .BIN file inside");
                    return;
                }
                if (ext == ".exe")
                {
                    MessageBox.Show("Please open the .EXE file and select the .BIN file inside");
                    return;
                }
                if (ext != ".bin")
                {
                    MessageBox.Show("The files to be a Nikon Firmware .BIN file");
                    return;
                }

                Clear();

                // Open the selected file to read.
                System.IO.Stream fileStream = openFileDialog1.File.OpenRead();

                SendEvent( "OpenFile", openFileDialog1.File.Name);

                if (fileStream.Length > (48 * 1024 * 1024))
                {
                    fileStream.Close();
                    return;
                }

                byte[] data = new byte[fileStream.Length];

                if (data == null)
                {
                    fileStream.Close();
                    return;
                }

                fileStream.Read(data, 0, (int)fileStream.Length);
                fileStream.Close();

                // Test in valid
                firm = PatchControl.FirmwareMatch(data, App.AllowedPatchLevel);

                if (firm != null)
                {
                    tFirmwareName.Text = firm.Model;
                    tFirmwareVersion.Text = firm.Version;
                    firm.ModelVersion = string.Format("{0}_{1}", firm.Model, firm.Version);

                    if (firm.Patches.Count == 0)
                    {
                        tFeature.Text = "No Patches presently exist for this 'Model/Firmware Version'";
                    }
                    else
                    {
                        var text = firm.TestPatch();
                        if (text == "")
                        {
                            lstFeatures.ItemsSource = firm.Patches;
                            PatchSet.SetListBox(lstFeatures);
                            tFeature.Text = "";
                        }
                        else
                        {
                            // hash matched, but patches did not match
                            tFeature.Text = "A sub-patch failed to map to this 'Model/Firmware Version' - Please Report " + text;
                        }
                    }

                    foreach (var p in firm.Patches)
                    {
                        p.PropertyChanged += PatchSetChanged;
                    }
                }
                else
                {
                    tFeature.Text = "No matching 'Model/Firmware Versions' were found";
                }
            }
        }

        private void Clear()
        {
            lstFeatures.ItemsSource = null;
            tFirmwareName.Text = "";
            tFirmwareVersion.Text = "";

        }

        CheckWindow check = null;
        private void bSaveFirmware_Click(object sender, RoutedEventArgs e)
        {
            if (CanSave)
            {
                bool hasBeta = firm.Patches.Any(p => p.Enabled && p.PatchStatus == PatchLevel.Beta);
                bool hasAlpha = firm.Patches.Any(p => p.Enabled && p.PatchStatus == PatchLevel.Alpha);

                var AlphaWarn = "\n\nAlpha patches may not have been tested by anybody, and may damage your camera in an unrecoverable way.\n\nIt is recommend You do not use those patch";
                var BetaWarn = "\n\nBeta patches have been tested but may have strange side effects, worse case the camera should be recoverable";
                if (hasAlpha && hasBeta)
                {
                    check = new CheckWindow();
                    check.TextBox.Text = "You have selected Alpha & Beta patches:" + AlphaWarn + BetaWarn;
                    check.OKButton.Click += (s,o) => { DoSave(); };
                    check.Show();
                }
                else if (hasAlpha)
                {
                    check = new CheckWindow();
                    check.TextBox.Text = "You have selected Alpha patches:" + AlphaWarn;
                    check.OKButton.Click += (s, o) => { DoSave(); };
                    check.Show();
                }
                else if (hasBeta)
                {
                    check = new CheckWindow();
                    check.TextBox.Text = "You have selected Beta patches:" + BetaWarn;
                    check.OKButton.Click += (s, o) => { DoSave(); };
                    check.Show();
                }
                else
                {
                    DoSave();
                }
            }
        }

        private void DoSave()
        {
            check = null;
            SaveFileDialog saveFileDialog1 = new SaveFileDialog();

            // Set filter options and filter index.
            saveFileDialog1.Filter = "Binary Files (.bin)|*.bin|All Files (*.*)|*.*";
            saveFileDialog1.FilterIndex = 1;

            // Call the ShowDialog method to show the dialog box.
            bool? userClickedOK = saveFileDialog1.ShowDialog();

            // Process input if the user clicked OK.
            if (userClickedOK == true)
            {
                System.IO.Stream fileStream = saveFileDialog1.OpenFile();
                firm.Patch(fileStream);
                fileStream.Close();
                SendEvent("SaveFile", firm.ModelVersion, firm.PatchesString);
            }
        }

        private void LayoutRoot_Loaded(object sender, RoutedEventArgs e)
        {
            Assembly assembly = Assembly.GetExecutingAssembly();
            String version = assembly.FullName.Split(',')[1];
            var versionparts = version.Split('=')[1].Split('.');

            tTitleText.Text = string.Format("Online Nikon Firmware Patching Tool v{0}.{1}.{2}", versionparts[0], versionparts[1], versionparts[2]);
        }


        private Dictionary<string, string> HelpMap = new Dictionary<string, string>();

        private void lstFeatures_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (e.AddedItems.Count > 0)
            {
                var item = e.AddedItems[0] as PatchSet;
                if (item != null)
                {
                    string txt;
                    if (HelpMap.TryGetValue(item.Name, out txt))
                    {
                        tFeature.Text = txt;
                        return;
                    }
                }

            }

            tFeature.Text = "";
        }
    }


    public enum PatchLevel
    {
        DevOnly,
        Alpha,
        Beta,
        Released
    }

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
                    foreach (var pps in patches.ItemsSource)
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
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(name));
        }

        public event PropertyChangedEventHandler PropertyChanged;


        static ListBox patches = null; // hack
        public static void SetListBox(ListBox lb)
        {
            patches = lb;
        }

        static Brush AlphaBkgrBrush = new SolidColorBrush(Color.FromArgb(255,240,10,10));
        static Brush BetaBkgrBrush = new SolidColorBrush(Color.FromArgb(255, 10, 240, 10));
        static Brush ReleaseBkgrBrush = new SolidColorBrush(Color.FromArgb(0, 0, 0, 0));

        //public Visibility BetaVisible { get { return (PatchStatus >= Nikon_Patch.App.AllowBeta) ? Visibility.Visible : Visibility.Collapsed; } set { } }
        public Brush BetaColor
        {
            get
            {
            switch (PatchStatus)
            {
            case PatchLevel.Released: return ReleaseBkgrBrush;
            case PatchLevel.Beta: return BetaBkgrBrush;
            default: return AlphaBkgrBrush;
            }

        } set{ } }
    }
}
