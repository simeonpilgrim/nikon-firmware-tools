using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Avalonia.Controls;
using Avalonia.Interactivity;
using Avalonia.Layout;
using Avalonia.Platform.Storage;

namespace Nikon_Patch
{
    public partial class MainWindow : Window
    {
        public MainWindow()
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

            HelpMap.Add("HDMI Output 1080i", "This patch force the HDMI output to be in 1080i verse defualt 720p");
            HelpMap.Add("HDMI Output 1080i FullScreen", "This forces HDMI 1080i output to be fullscreen, and is independant of the 'HDMI Output 1080i'");
            HelpMap.Add("HDMI Output 720p FullScreen", "This forces HDMI 720p output to be fullscreen");

            HelpMap.Add("Liveview (15min) No Timeout", "When you select the 15min liveview timeout option, you will get 3 hours instead");
        }

        private Dictionary<string, string> HelpMap = new Dictionary<string, string>();

        static readonly FilePickerFileType BinFiles = new FilePickerFileType("Binary Files (.bin)") { Patterns = new[] { "*.bin" } };
        static readonly FilePickerFileType CFiles = new FilePickerFileType("C Files (.c)") { Patterns = new[] { "*.c" } };
        static readonly FilePickerFileType AllFiles = new FilePickerFileType("All Files (*.*)") { Patterns = new[] { "*" } };

        private async Task<string> OpenBinFile(string title)
        {
            var files = await StorageProvider.OpenFilePickerAsync(new FilePickerOpenOptions
            {
                Title = title,
                AllowMultiple = false,
                FileTypeFilter = new[] { BinFiles, AllFiles },
            });

            return files.Count > 0 ? files[0].TryGetLocalPath() : null;
        }

        private async Task MessageBox(string text)
        {
            var ok = new Button { Content = "OK", HorizontalAlignment = HorizontalAlignment.Right };
            var dlg = new Window
            {
                Title = Title,
                SizeToContent = SizeToContent.WidthAndHeight,
                CanResize = false,
                WindowStartupLocation = WindowStartupLocation.CenterOwner,
                Content = new StackPanel
                {
                    Margin = new Avalonia.Thickness(16),
                    Spacing = 12,
                    Children = { new TextBlock { Text = text, MaxWidth = 400, TextWrapping = Avalonia.Media.TextWrapping.Wrap }, ok }
                }
            };
            ok.Click += (s, e) => dlg.Close();
            await dlg.ShowDialog(this);
        }

        private async void DecodeButton_Click(object sender, RoutedEventArgs e)
        {
            var filename = await OpenBinFile("Select Nikon firmware .bin file to decode");
            if (filename != null)
            {
                await DecodeAndExtractFirm(filename);
            }
        }

        private async Task DecodeAndExtractFirm(string filename)
        {

            //SendEvent("TryFile", openFileDialog1.File.Name);

            var ext = Path.GetExtension(filename).ToLower();
            if (ext == ".dmg")
            {
                await MessageBox("Please open the DMG file and select the .BIN file inside");
                return;
            }
            if (ext == ".exe")
            {
                await MessageBox("Please open the .EXE file and select the .BIN file inside");
                return;
            }
            if (ext != ".bin")
            {
                await MessageBox("The files need to be a Nikon Firmware .BIN file");
                return;
            }

            // Open the selected file to read.
            System.IO.Stream fileStream = File.OpenRead(filename);
            const int headerCheckLen = 0x20;
            byte[] headerCheck = new byte[headerCheckLen];

            fileStream.Read(headerCheck, 0, headerCheckLen);

            bool v1Header = headerCheck.All(b => b == 0x20);
            bool v2Header = false;
            long readLen = fileStream.Length;
            int readOff = 0;

            if (v1Header == false) {
                fileStream.Seek(headerCheckLen, SeekOrigin.Begin);
                fileStream.Read(headerCheck, 0, headerCheckLen);
                v2Header = headerCheck.All(b => b == 0x20);
                if(v2Header)
                {
                    readLen -= headerCheckLen;
                    readOff = headerCheckLen;
                }
            }
            fileStream.Seek(readOff, SeekOrigin.Begin);


            //SendEvent("OpenFile", openFileDialog1.File.Name);

            if (readLen > (48 * 1024 * 1024))
            {
                fileStream.Close();
                return;
            }

            byte[] data = new byte[readLen];

            if (data == null)
            {
                fileStream.Close();
                return;
            }

            fileStream.Read(data, 0, (int)readLen);
            fileStream.Close();

            var p = new Package();
            if (p.LoadData(data))
            {
                BinaryWriter bw = new BinaryWriter(File.Open(filename + ".out.bin", FileMode.Create, FileAccess.Write, FileShare.ReadWrite));
                bw.Write(p.raw);
                bw.Close();
            }

            if (p.TryOpen())
            {
                for(int i = 0; i < p.header.Count; i++)
                {
                    var h = p.header[i];
                    var b = p.blocks[i];

                    BinaryWriter bw = null;

                    try
                    {
                        var new_filename = Path.Combine(Path.GetDirectoryName(filename), h.Item1);
                        bw = new BinaryWriter(File.Open(new_filename, FileMode.Create, FileAccess.Write, FileShare.ReadWrite));
                        bw.Write(b);
                    }
                    finally
                    {
                        if (bw != null)
                            bw.Close();
                    }
                }
            }
        }


        private async void ExportCFile_Click(object sender, RoutedEventArgs e)
        {
            var file = await StorageProvider.SaveFilePickerAsync(new FilePickerSaveOptions
            {
                Title = "Select the file to write the C patch code into..",
                FileTypeChoices = new[] { CFiles, AllFiles },
                DefaultExtension = "c",
            });

            var filename = file?.TryGetLocalPath();
            if (filename != null)
            {
                PatchControl.ExportToC(filename);

                using (var s = File.CreateText(filename + ".html"))
                {

                    s.Write(PatchControl.BuildPatchMatrix());
                }
            }
        }

        void Clear()
        {
            tFirmwareName.Text = "";
            tFirmwareVersion.Text = "";
            lstFeatures.ItemsSource = null;
        }

        Firmware firm = null;
        bool canSave = false;

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

        private async void PatchFileButton_Click(object sender, RoutedEventArgs e)
        {
            var filename = await OpenBinFile("Select Nikon firmware .bin file to patch");

            // Process input if the user clicked OK.
            if (filename != null)
            {
                //SendEvent("TryFile", openFileDialog1.File.Name);

                var ext = Path.GetExtension(filename).ToLower();
                if (ext == ".dmg")
                {
                    await MessageBox("Please open the DMG file and select the .BIN file inside");
                    return;
                }
                if (ext == ".exe")
                {
                    await MessageBox("Please open the .EXE file and select the .BIN file inside");
                    return;
                }
                if (ext != ".bin")
                {
                    await MessageBox("The files to be a Nikon Firmware .BIN file");
                    return;
                }

                Clear();

                // Open the selected file to read.
                System.IO.Stream fileStream = File.OpenRead(filename);

                //SendEvent("OpenFile", openFileDialog1.File.Name);

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
                firm = PatchControl.FirmwareMatch(data, PatchLevel.DevOnly);

                if (firm != null)
                {
                    tFirmwareName.Text = firm.Model;
                    tFirmwareVersion.Text = firm.Version;
                    firm.ModelVersion = $"{firm.Model}_{firm.Version}";

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
                            PatchSet.SetPatchList(firm.Patches);
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

        private async void bSaveFirmware_Click(object sender, RoutedEventArgs e)
        {
            var file = await StorageProvider.SaveFilePickerAsync(new FilePickerSaveOptions
            {
                FileTypeChoices = new[] { BinFiles, AllFiles },
                DefaultExtension = "bin",
            });

            // Process input if the user clicked OK.
            if (file != null)
            {
                using (var fileStream = await file.OpenWriteAsync())
                {
                    firm.Patch(fileStream);
                }
                //SendEvent("SaveFile", firm.ModelVersion, firm.PatchesString);
            }
        }

        private void lstFeatures_SelectedIndexChanged(object sender, SelectionChangedEventArgs e)
        {
            if (lstFeatures.ItemCount > 0)
            {
                var item = lstFeatures.SelectedItem as PatchSet;
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
}
