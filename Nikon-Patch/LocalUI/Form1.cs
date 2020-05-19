using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Data.Odbc;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Nikon_Patch
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private void DecodeButton_Click(object sender, EventArgs e)
        {
            // Create an instance of the open file dialog box.
            OpenFileDialog ofd = new OpenFileDialog();

            ofd.Title = "Select Nikon firmware .bin file to decode";
            // Set filter options and filter index.
            ofd.Filter = "Binary Files (.bin)|*.bin|All Files (*.*)|*.*";
            ofd.FilterIndex = 1;
            ofd.Multiselect = false;

            // Call the ShowDialog method to show the dialog box.
            var userClickedOK = ofd.ShowDialog();

            // Process input if the user clicked OK.
            if (userClickedOK == DialogResult.OK)
            {
                DecodeAndExtractFirm(ofd.FileName);
            }
        }

        private static void DecodeAndExtractFirm(string filename, int decodeOffset = 0)
        {

            //SendEvent("TryFile", openFileDialog1.File.Name);

            var ext = Path.GetExtension(filename).ToLower();
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
                MessageBox.Show("The files need to be a Nikon Firmware .BIN file");
                return;
            }



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

            var p = new Package();
            if (p.LoadData(data))
            {
                BinaryWriter bw = new BinaryWriter(File.Open(filename + ".out.bin", FileMode.Create, FileAccess.Write, FileShare.ReadWrite));
                bw.Write(p.raw);
                bw.Close();
            }



            //DecodePackageFile(filename, decodeOffset);
            //ExactFirmware(filename);
        }


        private void ExportCFile_Click(object sender, EventArgs e)
        {          
            // Create an instance of the open file dialog box.
            OpenFileDialog ofd = new OpenFileDialog();

            ofd.Title = "Select the file to write the C patch code into..";

            // Set filter options and filter index.
            ofd.Filter = "Binary Files (.c)|*.c|All Files (*.*)|*.*";
            ofd.FilterIndex = 1;
            ofd.Multiselect = false;

            // Call the ShowDialog method to show the dialog box.
            var userClickedOK = ofd.ShowDialog();
            if (userClickedOK == DialogResult.OK)
            {
                PatchControl.ExportToC(ofd.FileName);
            }
        }
    }
}
