using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using CameraModels;
using System.Diagnostics;
using System.IO;

namespace WindowsFormsApplication1
{
    public partial class MainForm : Form
    {
        static void LogIt(string fmt, params object[] objs)
        {
            Debug.WriteLine(fmt, objs);
            Console.WriteLine(fmt, objs);
        }
        int state = 0;
        CameraSession session;

        public MainForm()
        {
            InitializeComponent();

            session = new CameraSession();

            Event_Failed_to_FindCamera();
        }


        public void State_0_TryFindCamera()
        {
            if (session.FindNikonCamera() == false)
            {
                Event_Failed_to_FindCamera();
                return;
            }

            // check if known model / supported
            if (session.InitilizeCamera() == false)
            {
                Event_Failed_to_InitilizeCamera();
                return;
            }

            var camera_obj = CameraModelFactory.ResolveCameraDetails(session.ModelNumber, session.FirmwareNumber);

            if(camera_obj.ModelKnown == false)
            {
                Event_Failed_to_FindKnownCamera();
                return;
            }

            Event_FindKnownCamera();
            return;
        }



        void State_0_buttions()
        {
            btRestoreEeprom.Enabled = false;
            btUnlockLanguage.Enabled = false;
            btSearchForCamera.Enabled = true;
            btBackupEeprom.Enabled = false;
            btRestoreEeprom.Enabled = false;
            btEmailDeveloper.Enabled = false;
        }

        public void Event_Failed_to_FindCamera()
        {
            LogIt(session.StatusMessage);
            model_tb.Text = session.StatusMessage;
            firmware_tb.Text = "";
            State_0_buttions();
        }


        public void Event_Failed_to_InitilizeCamera()
        {
            LogIt(session.StatusMessage);
            model_tb.Text = session.StatusMessage;
            firmware_tb.Text = "";
            State_0_buttions();
        }

        public void Event_Failed_to_FindKnownCamera()
        {
            LogIt(session.StatusMessage);
            model_tb.Text = session.ModelNumber;
            firmware_tb.Text = session.FirmwareNumber;
            State_0_buttions();
        }

        public void Event_FindKnownCamera()
        {
            LogIt("Event_FindKnownCamera");
            model_tb.Text = session.ModelNumber;
            firmware_tb.Text = session.FirmwareNumber;
            State_0_buttions();
        }

        private void button4_Click(object sender, EventArgs e)
        {
            if (state == 0)
            {
                State_0_TryFindCamera();
            }
        }
    }
}
