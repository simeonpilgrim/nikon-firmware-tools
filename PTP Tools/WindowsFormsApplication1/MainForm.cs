using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using CameraModels;

namespace WindowsFormsApplication1
{
    public partial class MainForm : Form
    {
        public MainForm()
        {
            InitializeComponent();
        }

        public void State_0_TryFindCamera()
        {
            // try find USB Camera
            string camid = CameraBase.FindNikonCamera();

            if (camid == "")
            {
                LogIt("Failed to find Nikon Camera");
                return;
            }
        }

        public
    }
}
