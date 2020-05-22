using System;
using System.IO;
using System.Collections.Generic;
using System.Text;
using System.Collections.ObjectModel;
using System.Linq;

namespace Nikon_Patch
{
    public class PatchControl
    {
        static bool HashSame(byte[] h1, byte[] h2)
        {
            for (int i = 0; i < 16; i++)
                if (h1[i] != h2[i])
                    return false;

            return true;
        }

        static Dictionary<byte[], Firmware> hashMap = new Dictionary<byte[], Firmware>();
        static PatchControl()
        {
            hashMap.Add(new byte[] { 0x16, 0xA0, 0x50, 0x50, 0xCA, 0xB2, 0x60, 0x2F, 0x99, 0x63, 0x36, 0xAC, 0x86, 0x9A, 0xD8, 0xE0 }, new D3100_0101());
            hashMap.Add(new byte[] { 0x30, 0xB1, 0x12, 0x1F, 0x22, 0x22, 0x11, 0x20, 0x95, 0xFF, 0xD2, 0x34, 0x31, 0xD4, 0x97, 0x15 }, new D3100_0102());
            hashMap.Add(new byte[] { 0xAB, 0x1C, 0x12, 0x9D, 0x37, 0xDC, 0x53, 0xF6, 0x92, 0x60, 0xA4, 0x53, 0xD5, 0x95, 0x62, 0x02 }, new D3200_0101());
            hashMap.Add(new byte[] { 0xA8, 0xC3, 0x87, 0x3A, 0x41, 0x24, 0x25, 0x64, 0x27, 0xED, 0x5B, 0x12, 0x10, 0x77, 0x87, 0x30 }, new D3200_0102());
            hashMap.Add(new byte[] { 0x48, 0xCF, 0x8E, 0x2F, 0x20, 0xFA, 0xE9, 0x6D, 0x5A, 0x35, 0xB9, 0xC3, 0x01, 0xFF, 0xA3, 0x12 }, new D3200_0103());
            hashMap.Add(new byte[] { 0x4a, 0xa1, 0x5d, 0x2a, 0x09, 0x2a, 0x0a, 0xe8, 0xa8, 0x46, 0x72, 0x39, 0x4a, 0x55, 0x3f, 0xc2 }, new D3200_0104());
            hashMap.Add(new byte[] { 0x24, 0x36, 0x1a, 0x3d, 0x44, 0x66, 0xae, 0x01, 0xb8, 0x4a, 0xb6, 0x21, 0x00, 0x97, 0x3e, 0xf8 }, new D3300_0101());
            hashMap.Add(new byte[] { 0xe0, 0xd3, 0x97, 0x7a, 0xf7, 0xba, 0xac, 0xb4, 0xc9, 0x55, 0x4b, 0xb4, 0xdb, 0xe1, 0x72, 0x02 }, new D3300_0102());
            hashMap.Add(new byte[] { 0x89, 0xd9, 0x47, 0x09, 0x16, 0x26, 0x66, 0x81, 0x0a, 0x29, 0x5a, 0x1f, 0xd2, 0x3d, 0x8c, 0xe5 }, new D3400_0112());
            hashMap.Add(new byte[] { 0x21, 0x84, 0xF8, 0x65, 0x82, 0xB2, 0x7A, 0x80, 0x49, 0xDC, 0x8C, 0x7D, 0x91, 0x8A, 0xDA, 0x50 }, new D5100_0101());
            hashMap.Add(new byte[] { 0x22, 0x14, 0x21, 0x0A, 0xD2, 0xC6, 0x5B, 0x5E, 0x85, 0x78, 0x99, 0xCA, 0x79, 0xF3, 0xDA, 0x19 }, new D5100_0102());
            hashMap.Add(new byte[] { 0x84, 0x53, 0x41, 0xEC, 0x3D, 0x92, 0xEF, 0x46, 0x39, 0xB0, 0x29, 0xD7, 0xEE, 0x16, 0x2A, 0x2C }, new D5200_0101());
            hashMap.Add(new byte[] { 0x81, 0x4B, 0x8A, 0x99, 0x29, 0x07, 0x55, 0x38, 0x93, 0xFE, 0x76, 0x52, 0x3A, 0xA4, 0x82, 0x19 }, new D5200_0102());
            hashMap.Add(new byte[] { 0xC4, 0xC7, 0x25, 0x70, 0x02, 0x41, 0x16, 0xC7, 0x68, 0xBE, 0x68, 0x71, 0xFF, 0xAA, 0x82, 0x7C }, new D5200_0103());
            hashMap.Add(new byte[] { 0x95, 0x8e, 0xda, 0x99, 0xab, 0xec, 0xf8, 0x55, 0xff, 0x84, 0x6d, 0xc3, 0xcd, 0x90, 0x79, 0x63 }, new D5300_0101());
            hashMap.Add(new byte[] { 0x84, 0x8f, 0x4c, 0x42, 0x93, 0x93, 0xf7, 0x3d, 0xf4, 0xed, 0xc2, 0x44, 0x9d, 0xc4, 0x9f, 0xe4 }, new D5300_0102());
            hashMap.Add(new byte[] { 0xD9, 0x58, 0x21, 0xAA, 0x3C, 0x57, 0x3B, 0xB6, 0x3C, 0x32, 0x35, 0x63, 0x7E, 0x81, 0x5C, 0x75 }, new D5300_0103());
            hashMap.Add(new byte[] { 0x15, 0xb1, 0xc2, 0xf8, 0x33, 0xb2, 0x17, 0x27, 0xfe, 0xd4, 0xec, 0x25, 0x84, 0xa4, 0x86, 0xee }, new D5500_0101());
            hashMap.Add(new byte[] { 0x3d, 0xea, 0xe3, 0x3c, 0xa9, 0x5e, 0x00, 0x2f, 0xdb, 0x90, 0xd6, 0xff, 0x60, 0x34, 0x8e, 0xa7 }, new D5500_0102());
            hashMap.Add(new byte[] { 0xe0, 0xed, 0xff, 0x5a, 0x75, 0xc1, 0x47, 0x1b, 0xc3, 0x17, 0x3c, 0x59, 0xad, 0x7d, 0x9a, 0x83 }, new D5600_0102());
            //hashMap.Add(new byte[] { 0x8F, 0x34, 0xC7, 0x53, 0xBF, 0x51, 0x99, 0xFA, 0x63, 0x06, 0x63, 0xE3, 0xC1, 0x2D, 0x58, 0x76 }, new D7000_0101());
            //hashMap.Add(new byte[] { 0x10, 0x16, 0x78, 0x3D, 0x86, 0x15, 0xFF, 0x61, 0x70, 0xFA, 0xA9, 0x92, 0x60, 0x6A, 0x89, 0xE1 }, new D7000_0102());
            hashMap.Add(new byte[] { 0x60, 0x6C, 0x50, 0x98, 0xC4, 0x41, 0x4A, 0x91, 0x98, 0x47, 0x83, 0x3D, 0xAC, 0x45, 0x63, 0x2C }, new D7000_0103());
            hashMap.Add(new byte[] { 0x76, 0xDE, 0xFC, 0x08, 0xF0, 0x0A, 0xCE, 0x3D, 0x62, 0xBD, 0x77, 0x00, 0x9F, 0x97, 0x4E, 0xC7 }, new D7000_0104());
            hashMap.Add(new byte[] { 0xb9, 0x0f, 0x26, 0xbb, 0x81, 0xaf, 0xc7, 0x0c, 0x24, 0xc7, 0xab, 0x26, 0x6c, 0x9d, 0x49, 0x2f }, new D7000_0105());
            hashMap.Add(new byte[] { 0xE6, 0xD5, 0x42, 0x68, 0x09, 0xFE, 0x3C, 0x64, 0xE9, 0xA3, 0x5B, 0x9A, 0x3A, 0xBD, 0xBA, 0x7D }, new D7100_0101());
            hashMap.Add(new byte[] { 0xAB, 0x4F, 0x00, 0xE4, 0x4B, 0x43, 0x5C, 0x2B, 0xE4, 0x2D, 0xB3, 0x97, 0x5E, 0xEC, 0x7F, 0x91 }, new D7100_0102());
            hashMap.Add(new byte[] { 0x7e, 0x80, 0x60, 0x71, 0x5d, 0x97, 0x6c, 0x97, 0xe6, 0x50, 0x24, 0xd6, 0xe3, 0xad, 0x8f, 0xc5 }, new D7100_0103());
            hashMap.Add(new byte[] { 0xc7, 0x82, 0xa3, 0x07, 0x72, 0xfa, 0xf4, 0xc0, 0xa8, 0xf3, 0xa7, 0xb0, 0xa2, 0xb9, 0x39, 0x29 }, new D7200_0102());
            hashMap.Add(new byte[] { 0xA0, 0xCB, 0xD0, 0x36, 0x5F, 0x00, 0x24, 0xA3, 0x15, 0x1D, 0x0F, 0x5D, 0x6E, 0x60, 0xC2, 0xDB }, new D300s_0101());
            hashMap.Add(new byte[] { 0x7F, 0x55, 0x03, 0xB7, 0x95, 0xFC, 0x41, 0xC8, 0x3A, 0x26, 0xE9, 0x8D, 0x83, 0x4D, 0x48, 0xFF }, new D300s_0102());
            hashMap.Add(new byte[] { 0x77, 0xE5, 0x42, 0x1A, 0xFF, 0x01, 0x1E, 0xA7, 0x32, 0x3F, 0x63, 0xDB, 0xD2, 0xC6, 0x0E, 0x93 }, new D300_0111_B());
            hashMap.Add(new byte[] { 0x5b, 0x6b, 0x75, 0x2f, 0x12, 0x59, 0x6f, 0x70, 0xa1, 0xbf, 0x93, 0x55, 0x3b, 0x45, 0xbe, 0x8d }, new D500_0111());
            hashMap.Add(new byte[] { 0x84, 0xd7, 0xfc, 0xcf, 0x37, 0xe5, 0x8c, 0xf7, 0xae, 0x15, 0x7e, 0xf9, 0xa7, 0xaa, 0x3d, 0x77 }, new D500_0113());
            hashMap.Add(new byte[] { 0x74, 0x0B, 0x38, 0x22, 0x33, 0x58, 0x17, 0xAC, 0xA6, 0x46, 0xF8, 0xB9, 0x2C, 0x3C, 0xF6, 0xC9 }, new D600_0101());
            hashMap.Add(new byte[] { 0x0D, 0xCC, 0xFD, 0x43, 0xD7, 0xFB, 0x8A, 0xC4, 0x86, 0xA4, 0xF1, 0x90, 0x81, 0x52, 0x03, 0x3D }, new D600_0102());
            hashMap.Add(new byte[] { 0xD5, 0x29, 0x2A, 0x20, 0x0A, 0x98, 0x4E, 0x15, 0xCB, 0x8B, 0x5B, 0x9C, 0xD7, 0x8A, 0xE3, 0x25 }, new D610_0101());
            hashMap.Add(new byte[] { 0x6a, 0x85, 0x03, 0x6b, 0x95, 0x1c, 0x27, 0x7d, 0xaf, 0x2f, 0xfd, 0xda, 0xae, 0xd7, 0x1d, 0xc4 }, new D610_0102());
            hashMap.Add(new byte[] { 0x50, 0x64, 0x0A, 0x6B, 0xA9, 0xEC, 0x2F, 0x70, 0x46, 0xA0, 0x27, 0x64, 0xAC, 0x3A, 0x67, 0x6B }, new D750_0101());
            hashMap.Add(new byte[] { 0x06, 0x12, 0x4f, 0x7a, 0x87, 0x2f, 0x1b, 0x66, 0xc0, 0x8a, 0x83, 0xa6, 0x1b, 0x19, 0xca, 0x1b }, new D750_0102());
            hashMap.Add(new byte[] { 0xf9, 0x49, 0x3e, 0x73, 0xc1, 0x88, 0x02, 0x42, 0x6e, 0xa4, 0x35, 0xca, 0x6d, 0x84, 0x98, 0x3f }, new D750_0110());
            hashMap.Add(new byte[] { 0x99, 0xD7, 0x8D, 0xEB, 0xE5, 0x04, 0x51, 0x03, 0x4A, 0x32, 0x83, 0x6E, 0x7F, 0xDF, 0x77, 0x88 }, new D800_0101());
            hashMap.Add(new byte[] { 0xFD, 0xD8, 0x91, 0xE9, 0xFC, 0xFE, 0xAF, 0x30, 0x44, 0x74, 0xE2, 0xDC, 0x72, 0x43, 0x70, 0x44 }, new D800_0102());
            hashMap.Add(new byte[] { 0x61, 0xEB, 0xBE, 0x3C, 0xD1, 0x6D, 0x6A, 0x33, 0x55, 0x2A, 0x05, 0x79, 0x2C, 0xAF, 0x91, 0x27 }, new D800_0110());
            hashMap.Add(new byte[] { 0x2b, 0xc4, 0xa7, 0x48, 0x81, 0xb0, 0x8d, 0xc0, 0xce, 0x67, 0xb4, 0xf7, 0x48, 0xa0, 0xd9, 0x47 }, new D800_0111());
            hashMap.Add(new byte[] { 0x9C, 0x16, 0xE0, 0x6D, 0x85, 0x69, 0x78, 0x71, 0x22, 0x44, 0x35, 0x40, 0x89, 0x33, 0x22, 0xC0 }, new D800E_0101());
            hashMap.Add(new byte[] { 0xB4, 0x25, 0xC8, 0x79, 0x71, 0x8B, 0xC7, 0xF9, 0x48, 0xFD, 0x05, 0xA6, 0x1D, 0x7D, 0x0A, 0x24 }, new D800E_0102());
            hashMap.Add(new byte[] { 0xA6, 0xA6, 0xC6, 0xA7, 0x74, 0x8D, 0x5A, 0xCC, 0x97, 0xE8, 0x59, 0xAD, 0x50, 0x31, 0xAC, 0xE5 }, new D800E_0110());
            hashMap.Add(new byte[] { 0x0E, 0xED, 0x2B, 0xB2, 0x48, 0x75, 0x61, 0x5B, 0x5F, 0x40, 0x8D, 0x6D, 0x90, 0x3A, 0x66, 0xC6 }, new D810_0102());
            hashMap.Add(new byte[] { 0xf6, 0x20, 0xac, 0xd9, 0x86, 0x30, 0xcd, 0x78, 0x39, 0x5e, 0x47, 0x3a, 0xf0, 0x42, 0x10, 0x87 }, new D810_0112());
            hashMap.Add(new byte[] { 0x55, 0x05, 0x0d, 0x9a, 0xe3, 0x71, 0xd2, 0xb5, 0x6b, 0xb0, 0x94, 0x95, 0x26, 0xcc, 0xb5, 0x0e }, new D810A_0102());
            hashMap.Add(new byte[] { 0xB0, 0x1F, 0xD3, 0xDF, 0xE5, 0x74, 0x2F, 0xB7, 0x49, 0xA0, 0x85, 0xD3, 0xE4, 0x14, 0x52, 0x7D }, new D4_0105());
            hashMap.Add(new byte[] { 0x80, 0x9D, 0xBB, 0xE0, 0x40, 0x95, 0x4E, 0x02, 0x24, 0xF0, 0x95, 0xB9, 0xC2, 0xF6, 0xA8, 0xC0 }, new D4_0110());
            hashMap.Add(new byte[] { 0xBB, 0xB0, 0x44, 0x5F, 0x58, 0x18, 0x57, 0xCD, 0x0A, 0xF7, 0x76, 0x9D, 0xEF, 0xBD, 0x09, 0x51 }, new D4S_0101());
            hashMap.Add(new byte[] { 0x56, 0xcb, 0xd3, 0xf0, 0xba, 0x4b, 0x2b, 0x74, 0x27, 0x4f, 0xc3, 0xea, 0xe4, 0x4f, 0xd7, 0x9b }, new D4S_0132());
            hashMap.Add(new byte[] { 0x09, 0xa6, 0x90, 0xce, 0x56, 0x06, 0xf2, 0x46, 0xcb, 0x1b, 0x51, 0x45, 0xfd, 0x27, 0x6e, 0xd5 }, new D5_0120());
        }

        static public void ExportToC(string filename)
        {
            StringBuilder sb = new StringBuilder();

            // build patches
            //struct Patch D5100_0102_patches[] = {
            //    {.id=1, .level=Released, .name="Remove Time Based Video Restrictions", .blocks={2}} /*patch_1*/
            //};
            sb.AppendLine("#include <stdint.h>");
            sb.AppendLine("#include \"patches.h\"");

            foreach (var pm in hashMap)
            {
                var type = pm.Value.GetType().ToString().Split('.')[1];

                StringBuilder psb = new StringBuilder();

                psb.AppendLine($"struct Patch {type}_patches[] = {{");
                var patches = pm.Value.Patches.AsEnumerable().ToList();
                int p_id = 0;
                foreach (var p in patches)
                {
                    int c_id = 0;
                    var patch_name = $"{type}_{p_id:000}";
                    var change_names = new List<string>();
                    foreach(var c in p.changes)
                    {
                        var change_name = $"{patch_name}_change_{c_id:000}";
                        change_names.Add("&"+change_name);
                        var bl = $"uint8_t {change_name}_b[] = {{{string.Join(",", c.orig.Select(v => string.Format("0x{0:X2}", v)))}}};";
                        var al = $"uint8_t {change_name}_a[] = {{{string.Join(",", c.patch.Select(v => string.Format("0x{0:X2}", v)))}}};";
                        var cl = $"struct Change {change_name} = CHANGE({c.block}, 0x{c.start:X6}, {change_name}_b, {change_name}_a);";
                        sb.AppendLine(bl);
                        sb.AppendLine(al);
                        sb.AppendLine(cl);
                        c_id++;
                    }
                    sb.AppendLine($"struct Change* {patch_name}[] = {{{string.Join(",", change_names)}}};");
                    sb.AppendLine();

                    int idx = patches.IndexOf(p) + 1;
                    var sep = idx > 1 ? "," : "";
                    var status = p.PatchStatus.ToString();
                    var blocksS = new List<string>();
                    foreach(var b in p.incompatible)
                    {
                        for(int x=0; x<patches.Count; x++)
                        {
                            if(patches[x].changes == b)
                            {
                                blocksS.Add((x + 1).ToString());
                            }
                        }
                    }

                    var blocks = string.Join(",", blocksS);
                    var s = $"    {sep}{{.id = {idx}, .level = {status}, .name=\"{p.Name}\", .blocks={{{blocks}}}, .changes={patch_name}, .changes_len=(sizeof({patch_name})/sizeof(struct Change*))}}";
                    psb.AppendLine(s);
                    p_id++;
                }
                psb.AppendLine("};");
                psb.AppendLine();
                sb.Append(psb.ToString());
            }

            // build patch sets
            sb.AppendLine();
            foreach (var pm in hashMap)
            {
                var type = pm.Value.GetType().ToString().Split('.')[1];
                var firmware_type = pm.Value.p is Package ? 0 : 1;
                sb.AppendLine($"struct PatchSet {type}_ps = PATCHSET(\"{pm.Value.Model}\", \"{pm.Value.Version}\", {type}_patches, {firmware_type});");
                //struct PatchSet D5100_0102_ps = PATCHSET("D5100", "1.02", D5100_0102_patches);
            }


            // build patch map
            sb.AppendLine();
            sb.AppendLine("struct PatchMap patches[] = {");
            int id = 1;
            foreach (var pm in hashMap)
            {
                var hash = string.Join(",", pm.Key.Select(k => string.Format("0x{0:X2}", k)));
                var sep = id > 1 ? "," : "";
                var type = pm.Value.GetType().ToString().Split('.')[1];
                var s = $"     {sep}{{.id = {id}, .hash = {{{hash}}}, .patches = &{type}_ps}}";
                sb.AppendLine(s);
                id+=1;
                //     {.id = 1, .hash = {0x22, 0x14, 0x21, 0x0A, 0xD2, 0xC6, 0x5B, 0x5E, 0x85, 0x78, 0x99, 0xCA, 0x79, 0xF3, 0xDA, 0x19}, .patches=&D5100_0102_ps},
            }
            sb.AppendLine("};");
            sb.AppendLine("const uint32_t patches_count = sizeof(patches)/sizeof(struct PatchMap);");


            using (var s = File.CreateText(filename))
            {
                s.Write(sb.ToString());
            }
        }

        static public string BuildPatchMatrix()
        {
            StringBuilder sb = new StringBuilder();
            string alpha = "ALPHA";
            string beta = "BETA";

            foreach (var hm in hashMap)
            {
                Firmware f = hm.Value;
                sb.AppendFormat("<li>{0} {1}<ul>\n", f.Model, f.Version);
                foreach (var p in f.Patches)
                {
                    switch (p.PatchStatus)
                    {
                    case PatchLevel.Alpha:
                        sb.AppendFormat("<li>{1} - {0}</li>\n", p.Name, alpha); break;
                    case PatchLevel.Beta:
                        sb.AppendFormat("<li>{1} - {0}</li>\n", p.Name, beta); break;
                    case PatchLevel.Released:
                        sb.AppendFormat("<li>{0}</li>\n", p.Name); break;
                    }
                }
                sb.AppendFormat("</ul></li>\n");
            }

            //Debug.WriteLine(sb.ToString());
            return sb.ToString();
        }

        static public Firmware FirmwareMatch(byte[] data, PatchLevel allowLevel)
        {
            var hash = MD5Core.GetHash(data);

//#if DEBUG
            string s = "";
            foreach (byte b in hash)
            {
                s += string.Format("0x{0:X2}, ", b);
            }
            System.Diagnostics.Debug.WriteLine(s);
//#endif

            Firmware firm = null;

            foreach (var h in hashMap)
            {
                if (HashSame(h.Key, hash))
                {
                    firm = h.Value;
                    break;
                }
            }

            if (firm != null)
            {
                int i = 0;
                do
                {
                    for (i = 0; i < firm.Patches.Count; i++)
                    {
                        if (firm.Patches[i].PatchStatus < allowLevel)
                        {
                            firm.Patches.RemoveAt(i);
                            break;
                        }
                    }
                } while (i < firm.Patches.Count);

                firm.LoadData(data);
            }

            return firm;
        }
    }



    static class Sys
    {
        public static UInt32 ReadUint32(byte[] data, long pos)
        {
            return (UInt32)(data[pos + 0] << 24 | data[pos + 1] << 16 | data[pos + 2] << 8 | data[pos + 3]);
        }

        public static UInt16 ReadUint16(byte[] data, long pos)
        {
            return (UInt16)(data[pos + 0] << 8 | data[pos + 1]);
        }

        public static byte[] LittleWords(params Int32[] words)
        {
            var data = new byte[words.Length * 2];
            for (int i = 0; i < words.Length; i++)
            {
                data[(i * 2) + 0] = (byte)((words[i] >> 8) & 0xFF);
                data[(i * 2) + 1] = (byte)((words[i] >> 0) & 0xFF);
            }
            return data;
        }

        public static byte[] LittleDwords(params Int32[] dwords)
        {
            var data = new byte[dwords.Length * 4];
            for (int i = 0; i < dwords.Length; i++)
            {
                data[(i * 4) + 0] = (byte)((dwords[i] >> 24) & 0xFF);
                data[(i * 4) + 1] = (byte)((dwords[i] >> 16) & 0xFF);
                data[(i * 4) + 2] = (byte)((dwords[i] >> 8) & 0xFF);
                data[(i * 4) + 3] = (byte)((dwords[i] >> 0) & 0xFF);
            }
            return data;
        }

        public static byte[] BigWords(params Int32[] words)
        {
            var data = new byte[words.Length * 2];
            for (int i = 0; i < words.Length; i++)
            {
                data[(i * 2) + 0] = (byte)((words[i] >> 0) & 0xFF);
                data[(i * 2) + 1] = (byte)((words[i] >> 8) & 0xFF);
            }
            return data;
        }

        public static byte[] BigDwords(params Int32[] dwords)
        {
            var data = new byte[dwords.Length * 4];
            for (int i = 0; i < dwords.Length; i++)
            {
                data[(i * 4) + 0] = (byte)((dwords[i] >> 0) & 0xFF);
                data[(i * 4) + 1] = (byte)((dwords[i] >> 8) & 0xFF);
                data[(i * 4) + 2] = (byte)((dwords[i] >> 16) & 0xFF);
                data[(i * 4) + 3] = (byte)((dwords[i] >> 24) & 0xFF);
            }
            return data;
        }

        public static string ReadString(byte[] data, long pos, int count)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++)
            {
                byte c = data[i + pos];
                if (c != 0)
                {
                    sb.Append((char)c);
                }
                else
                {
                    break;
                }
            }

            return sb.ToString();
        }

        public static byte[] mbps64 = { 0x03, 0xd0, 0x90, 0x00 };
        public static byte[] mbps60 = { 0x03, 0x93, 0x87, 0x00 };
        public static byte[] mbps57 = { 0x03, 0x6e, 0x36, 0x00 };
        public static byte[] mbps53 = { 0x01, 0x31, 0x2d, 0x00 }; 
        public static byte[] mbps40 = { 0x02, 0x6e, 0x36, 0x00 };
        public static byte[] mbps36 = { 0x01, 0x31, 0x2d, 0x00 }; 
        public static byte[] mbps29 = { 0x01, 0xBA, 0x81, 0x40 };
        public static byte[] mbps25 = { 0x01, 0x7D, 0x78, 0x40 };
        public static byte[] mbps24 = { 0x01, 0x6e, 0x36, 0x00 };
        public static byte[] mbps22 = { 0x01, 0x4F, 0xB1, 0x80 };
        public static byte[] mbps20 = { 0x01, 0x31, 0x2d, 0x00 };
        public static byte[] mbps18 = { 0x01, 0x12, 0xA8, 0x80 };
        public static byte[] mbps12 = { 0x00, 0xb7, 0x1b, 0x00 };
        public static byte[] mbps10 = { 0x00, 0x98, 0x96, 0x80 };
        public static byte[] mbps8 = { 0x00, 0x7A, 0x12, 0x00 };
        public static byte[] mbps6 = { 0x00, 0x5B, 0x8D, 0x80 };
        public static byte[] mbps5 = { 0x00, 0x4C, 0x4B, 0x40 };
    }

    public class Firmware
    {
        public ObservableCollection<PatchSet> Patches = new ObservableCollection<PatchSet>();
        internal IPackage p = null;

        public void LoadData(byte[] data)
        {
            p.LoadData(data);
        }
        public string TestPatch()
        {
            if (p.TryOpen() == false)
            {
                return "open failed";
            }

            foreach (var ps in Patches)
            {
                foreach (var pp in ps.changes)
                {
                    if (p.PatchCheck(pp.block, pp.start, pp.orig) == false)
                    {
                        return string.Format("block: {0} start: 0x{1:X}",pp.block, pp.start);
                    }

                }
            }
            return "";
        }

        public void Patch(Stream outStream)
        {
            var sb = new List<string>();
            foreach (var ps in Patches)
            {
                if (ps.Enabled)
                {
                    foreach (var pp in ps.changes)
                    {
                        p.Patch(pp.block, pp.start, pp.patch);
                    }
                    sb.Add(ps.Name);
                }
            }
            sb.Sort();
            PatchesString = string.Join(",", sb);

            p.Repackage(outStream);
        }

        public string Model { get; set; }
        public string Version { get; set; }
        public string ModelVersion { get; set; }
        public string PatchesString { get; set; }
    }

    public class Patch
    {
        public Patch(int _block, int _start, byte[] _orig, byte[] _patch)
        {
            block = _block;
            start = _start;
            orig = _orig;
            patch = _patch;
        }
        public int block;
        public int start;
        public byte[] orig;
        public byte[] patch;
    }



    public class OldSinglePackage : IPackage
    {
        public OldSinglePackage()
        {
        }

        public bool LoadData(byte[] data)
        {
            int len = data.Length;
            raw = new byte[len];
            Array.Copy(data, raw, len);
            return true;
        }

        byte[] raw;
        List<byte[]> blocks = new List<byte[]>();

        public bool TryOpen()
        {
            if (ExactFirmware() == false)
            {
                return false;
            }

            return true;
        }

        public bool PatchCheck(int block, int start, byte[] orig)
        {
            if (block != 0) return false;

            var b = raw;
            if (start >= b.Length ||
                (start + orig.Length) > b.Length)
            {
                return false;
            }

            for (int i = 0; i < orig.Length; i++)
            {
                if (orig[i] != b[start + i])
                {
                    var b1 = orig[i];
                    var b2 = b[start + i];
                    return false;
                }
            }

            return true;
        }


        public void Patch(int block, int start, byte[] data)
        {
            for (int i = 0; i < data.Length; i++)
            {
                raw[start + i] = data[i];
            }
        }

        public void UpdateCRCs()
        {
            CRC.UpdateCRC(raw);
        }

        public void Repackage(Stream outstream)
        {
            UpdateCRCs();

            outstream.Write(raw, 0, raw.Length);
        }

        bool ExactFirmware()
        {
            if (raw.Length < 4)
            {
                return false;
            }

            return true;
        }
    }

}

