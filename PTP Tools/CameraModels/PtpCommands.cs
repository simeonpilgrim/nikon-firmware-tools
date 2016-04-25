using PortableDeviceLib;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace CameraModels
{
    internal static class PtpCommands
    {
        internal const int MTP_OPERATION_GET_DEVICE_INFO = 0x1001;

        internal const int MTP_NIKON_ENTER_SERVICE = 0xFC01;
        internal const int MTP_NIKON_EXIT_SERVICE = 0xFC02;

        static bool EnterServiceMode(CameraSession session)
        {
            if (session.InServiceMode == false)
            {
                var res = session.cam.ExecuteWithNoData(MTP_NIKON_ENTER_SERVICE, (uint)0x0, (uint)0x0, (uint)0x0);
                session.InServiceMode = (res == 0x2001) || (res == 0x201E);
            }

            return session.InServiceMode;
        }

        internal static void ExitServiceMode(CameraSession session)
        {
            if (session.InServiceMode)
            {
                session.cam.ExecuteWithNoData(MTP_NIKON_EXIT_SERVICE, (uint)0x0, (uint)0x0, (uint)0x0);
                session.InServiceMode = false;
            }
        }

        internal static string Get_Version_FirmB(CameraSession session)
        {
            EnterServiceMode(session);

            var res = session.cam.ExecuteReadDataEx(0xFE01, (uint)0x0, (uint)0x80, (uint)0x0, 0, 0);
            return Helper.DataToCstring(res.Data, 0);
        }

        internal static string Get_CameraModel(CameraSession session)
        {
            EnterServiceMode(session);

            var res = session.cam.ExecuteReadDataEx(0xFE02, (uint)0x0, (uint)0x80, (uint)0x0, 0, 0);
            return Helper.DataToCstring(res.Data, 0);
        }

        internal static string Get_Version_Q_OS(CameraSession session)
        {
            EnterServiceMode(session);

            var res = session.cam.ExecuteReadDataEx(0xFE03, (uint)0x0, (uint)0x80, (uint)0x0, 0, 0);
            return Helper.DataToCstring(res.Data, 0);
        }

        internal static string Get_Version_B_OS(CameraSession session)
        {
            EnterServiceMode(session);

            var res = session.cam.ExecuteReadDataEx(0xFE04, (uint)0x0, (uint)0x80, (uint)0x0, 0, 0);
            return Helper.DataToCstring(res.Data, 0);
        }

        static MTPDataResponse FR_DumpSet(CameraSession session, uint set, uint len)
        {
            return session.cam.ExecuteReadDataEx(0xFEB1, set, len, 0, 0, 0);
        }

        static bool Tmp19_Suspend(CameraSession session)
        {
            EnterServiceMode(session);

            if (session.TMP19_Suspend == false)
            {
                byte[] data = { 0x53, 0x53 };

                session.cam.SetProperty(0xFDB2, data, 0x0, (uint)data.Length, 0x0);

                var res = session.cam.ExecuteReadDataEx(0xFEB2, 0x0, 0x1, 0x0, 0, 0);
                session.TMP19_Suspend = res.ErrorCode == 0x2001;
            }

            return session.TMP19_Suspend;
        }

        static void Tmp19_Resume(CameraSession session)
        {
            EnterServiceMode(session);
            if (session.TMP19_Suspend)
            {
                byte[] data = { 0x51, 0x51 };

                session.cam.SetProperty(0xFDB2, data, 0x0, (uint)data.Length, 0x0);

                session.cam.ExecuteReadDataEx(0xFEB2, 0x0, 0x1, 0x0, 0, 0);
                session.TMP19_Suspend = false;
            }
        }


        static MTPDataResponse Tmp19_Cmd_4155_EepromRead(CameraSession session, int addr, int readLen)
        {
            Tmp19_Suspend(session);

            byte[] data = new byte[5];
            data[0] = 0x41;
            data[1] = 0x55;
            data[2] = (byte)((addr >> 8) & 0xFF);
            data[3] = (byte)((addr >> 0) & 0xFF);
            data[4] = (byte)readLen;

            session.cam.SetProperty(0xFDB2, data, 0x0, (uint)data.Length, 0x0);

            return session.cam.ExecuteReadDataEx(0xFEB2, 0x0, (uint)readLen, 0x0, 0, 0);
        }

        static MTPDataResponse Tmp19_Cmd_4157_EepromWrite(CameraSession session, int addr, int dataLen, byte[] odata)
        {
            Tmp19_Suspend(session);
            uint newdatalen = (uint)(5 + dataLen);
            byte[] data = new byte[newdatalen];
            data[0] = 0x41;
            data[1] = 0x57;
            data[2] = (byte)((addr >> 8) & 0xFF);
            data[3] = (byte)((addr >> 0) & 0xFF);
            data[4] = (byte)newdatalen;
            System.Array.Copy(odata, 0, data, 5, dataLen);

            session.cam.SetProperty(0xFDB2, data, 0x0, newdatalen, 0x0);

            return session.cam.ExecuteReadDataEx(0xFEB2, 0x0, (uint)dataLen, 0x0, 0, 0);
        }

        static MTPDataResponse Tmp19_Cmd_4771_EepromSave(CameraSession session)
        {
            Tmp19_Suspend(session);
            byte[] data = new byte[2];
            data[0] = 0x47;
            data[1] = 0x71;

            session.cam.SetProperty(0xFDB2, data, 0x0, 2, 0x0);

            return session.cam.ExecuteReadDataEx(0xFEB2, 0x0, (uint)1, 0x0, 0, 0);
        }

        static public bool TryCheckLanguage(CameraSession session, out bool result)
        {
            EnterServiceMode(session);
            result = false;
            bool success = false;
            var set = FR_DumpSet(session, 4, 0x80);
            if (set.Data != null && set.Data.Length >= 0x18)
            {
                result = (set.Data[0x17] & 0x02) != 0;
                success = true;
            }

            ExitServiceMode(session);

            return success;
        }

        static public void SaveLanguageFromEeprom(CameraSession session)
        {
            EnterServiceMode(session);

            string filesname = string.Format("eeprom_log_{0}.txt", session.SerialNumber);
            using (TextWriter tw = new StreamWriter(filesname))
            {

                tw.WriteLine("m:{0} f:{1} sn:{2}", session.ModelNumber, session.FirmwareNumber, session.SerialNumber);
                var set = FR_DumpSet(session, 1, 0x3);
                tw.WriteLine("set01 " + Helper.DataToHexString(set.Data, 0));
                set = FR_DumpSet(session, 2, 0x80);
                tw.WriteLine("set02 " + Helper.DataToHexString(set.Data, 0));
                set = FR_DumpSet(session, 3, 0x80);
                tw.WriteLine("set03 " + Helper.DataToHexString(set.Data, 0));
                set = FR_DumpSet(session, 4, 0x80);
                tw.WriteLine("set04 " + Helper.DataToHexString(set.Data, 0));
                if (set.Data != null && set.Data.Length >= 0x18)
                {
                    tw.WriteLine(string.Format("set04.f17 {0:X2}" + set.Data[0x17]));
                }


                var res = Tmp19_Cmd_4155_EepromRead(session, 0x0, 0x80);
                tw.WriteLine(Helper.DataToHexString(res.Data, 0));
                res = Tmp19_Cmd_4155_EepromRead(session, 0x80, 0x80);
                tw.WriteLine(Helper.DataToHexString(res.Data, 0));
                res = Tmp19_Cmd_4155_EepromRead(session, 0x100, 0x80);
                tw.WriteLine(Helper.DataToHexString(res.Data, 0));
                res = Tmp19_Cmd_4155_EepromRead(session, 0x180, 0x80);
                tw.WriteLine(Helper.DataToHexString(res.Data, 0));
            }

            Tmp19_Resume(session);
            ExitServiceMode(session);
        }
    }

    internal static class Helper
    {
        internal static string DataToCstring(byte[] data, int offset)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = offset; i < data.Length && data[i] != 0; i++)
            {
                sb.Append((char)data[i]);
            }

            return sb.ToString();
        }


        internal static string DataToHexString(byte[] data, int offset)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = offset; i < data.Length; i++)
            {
                sb.Append(string.Format("{0:X2} ", data[i]));
            }

            return sb.ToString();
        }
    }
}
