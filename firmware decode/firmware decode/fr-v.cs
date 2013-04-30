using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Diagnostics;

namespace Nikon_Decode
{
    class FR_V
    {
        static string[] insttab = new string[] {
"add|add$pack $GRi,$GRj,$GRk|31,pack;25-30,GRk;18-24,0x0;12-17,GRi;10-11,0x0;6-9,0x0;0-5,GRj",
"addcc|addcc$pack $GRi,$GRj,$GRk,$ICCi_1|31,pack;25-30,GRk;18-24,0x0;12-17,GRi;10-11,ICCi_1;6-9,0x1;0-5,GRj",
"addi|addi$pack $GRi,$s12,$GRk|31,pack;25-30,GRk;18-24,0x10;12-17,GRi;0-11,s12",
"addicc|addicc$pack $GRi,$s12,$GRk,$ICCi_1|31,pack;25-30,GRk;18-24,0x11;12-17,GRi;10-11,ICCi_1;0-9,s12",
"addx|addx$pack $GRi,$GRj,$GRk,$ICCi_1|31,pack;25-30,GRk;18-24,0x0;12-17,GRi;10-11,ICCi_1;6-9,0x2;0-5,GRj",
"addxcc|addxcc$pack $GRi,$GRj,$GRk,$ICCi_1|31,pack;25-30,GRk;18-24,0x0;12-17,GRi;10-11,ICCi_1;6-9,0x3;0-5,GRj",
"addxi|addxi$pack $GRi,$s10,$GRk,$ICCi_1|31,pack;25-30,GRk;18-24,0x12;12-17,GRi;10-11,ICCi_1;0-9,s10",
"addxicc|addxicc$pack $GRi,$s10,$GRk,$ICCi_1|31,pack;25-30,GRk;18-24,0x13;12-17,GRi;10-11,ICCi_1;0-9,s10",
"and|and$pack $GRi,$GRj,$GRk|31,pack;25-30,GRk;18-24,0x1;12-17,GRi;10-11,0x0;6-9,0x0;0-5,GRj",
"andcc|andcc$pack $GRi,$GRj,$GRk,$ICCi_1|31,pack;25-30,GRk;18-24,0x1;12-17,GRi;10-11,ICCi_1;6-9,0x1;0-5,GRj",
"andcr|andcr$pack $CRi,$CRj,$CRk|31,pack;28-30,0x0;25-27,CRk;18-24,0xA;15-17,0x0;12-14,CRi;6-11,0x8;3-5,0x0;0-2,CRj",
"andi|andi$pack $GRi,$s12,$GRk|31,pack;25-30,GRk;18-24,0x20;12-17,GRi;0-11,s12",
"andicc|andicc$pack $GRi,$s10,$GRk,$ICCi_1|31,pack;25-30,GRk;18-24,0x21;12-17,GRi;10-11,ICCi_1;0-9,s10",
"andncr|andncr$pack $CRi,$CRj,$CRk|31,pack;28-30,0x0;25-27,CRk;18-24,0xA;15-17,0x0;12-14,CRi;6-11,0x10;3-5,0x0;0-2,CRj",
"bar|bar$pack|31,pack;25-30,0x0;18-24,0x3;12-17,0x0;6-11,0x3E;0-5,0x0",
"bc|bc$pack $ICCi_2,$hint,$label16|31,pack;27-30,0x1;25-26,ICCi_2;18-24,0x6;16-17,hint;0-15,label16",
"bcclr|bcclr$pack $ICCi_2,$ccond,$hint|31,pack;27-30,0x1;25-26,ICCi_2;18-24,0xe;16-17,hint;13-15,0x3;12,ccond,0-11,0x0",
"bceqlr|bceqlr$pack $ICCi_2,$ccond,$hint|31,pack;27-30,0x4;25-26,ICCi_2;18-24,0xe;16-17,hint;13-15,0x3;12,ccond,0-11,0x0",
"bcgelr|bcgelr$pack $ICCi_2,$ccond,$hint|31,pack;27-30,0xB;25-26,ICCi_2;18-24,0xe;16-17,hint;13-15,0x3;12,ccond,0-11,0x0",
"bcgtlr|bcgtlr$pack $ICCi_2,$ccond,$hint|31,pack;27-30,0xF;25-26,ICCi_2;18-24,0xe;16-17,hint;13-15,0x3;12,ccond,0-11,0x0",
"bchilr|bchilr$pack $ICCi_2,$ccond,$hint|31,pack;27-30,0xD;25-26,ICCi_2;18-24,0xe;16-17,hint;13-15,0x3;12,ccond,0-11,0x0",
"bclelr|bclelr$pack $ICCi_2,$ccond,$hint|31,pack;27-30,0x7;25-26,ICCi_2;18-24,0xe;16-17,hint;13-15,0x3;12,ccond,0-11,0x0",
"bclr|bclr$pack $ICCi_2,$hint|31,pack;27-30,0x1;25-26,ICCi_2;18-24,0xE;16-17,hint;13-15,0x2;12,0x0;0-11,0x0",
"bclslr|bclslr$pack $ICCi_2,$ccond,$hint|31,pack;27-30,0x5;25-26,ICCi_2;18-24,0xE;16-17,hint;13-15,0x3;12,ccond;0-11,0x0",
"bcltlr|bcltr$pack $ICCi_2,$ccond,$hint|31,pack;27-30,0x3;25-26,ICCi_2;18-24,0xE;16-17,hint;13-15,0x3;12,ccond;0-11,0x0",
"bcnclr|bcnclr$pack $ICCi_2,$ccond,$hint|31,pack;27-30,0x9;25-26,ICCi_2;18-24,0xE;16-17,hint;13-15,0x3;12,ccond;0-11,0x0",
"bcnelr|bcnelr$pack $ICCi_2,$ccond,$hint|31,pack;27-30,0xc;25-26,ICCi_2;18-24,0xE;16-17,hint;13-15,0x3;12,ccond;0-11,0x0",
"bcnlr|bcnlr$pack $ICCi_2,$ccond,$hint|31,pack;27-30,0x6;25-26,ICCi_2;18-24,0xE;16-17,hint;13-15,0x3;12,ccond;0-11,0x0",
"bcnolr|ncnolr$pack$hint_not_taken|31,pack;27-30,0x0;25-26,0x0;18-24,0xE;16-17,hint_not_taken;13-15,0x3;12,0x0;0-11,0x0",
//"bcnvlr$pack $ICCi_2,$ccond,$hint",
//"bcplr$pack $ICCi_2,$ccond,$hint",
//"bcralr$pack $ccond$hint_taken",
//"bctrlr$pack $ccond,$hint",
//"bcvlr$pack $ICCi_2,$ccond,$hint",
//"beq$pack $ICCi_2,$hint,$label16",
//"beqlr$pack $ICCi_2,$hint",
//"bge$pack $ICCi_2,$hint,$label16",
//"bgelr$pack $ICCi_2,$hint",
//"bgt$pack $ICCi_2,$hint,$label16",
//"bgtlr$pack $ICCi_2,$hint",
//"bhi$pack $ICCi_2,$hint,$label16",
//"bhilr$pack $ICCi_2,$hint",
//"ble$pack $ICCi_2,$hint,$label16",
//"blelr$pack $ICCi_2,$hint",
//"bls$pack $ICCi_2,$hint,$label16",
//"blslr$pack $ICCi_2,$hint",
//"blt$pack $ICCi_2,$hint,$label16",
//"bltlr$pack $ICCi_2,$hint",
//"bn$pack $ICCi_2,$hint,$label16",
//"bnc$pack $ICCi_2,$hint,$label16",
//"bnclr$pack $ICCi_2,$hint",
//"bne$pack $ICCi_2,$hint,$label16",
//"bnelr$pack $ICCi_2,$hint",
//"bnlr$pack $ICCi_2,$hint",
"bno|bno$pack$hint_not_taken|31,pack;27-30,0x0;25-26,0x0;18-24,0x6;16-17,hint_not_taken;0-15,0x0",
"bnolr|bnolr$pack$hint_not_taken|31,pack;27-30,0x0;25-26,0x0;18-24,0xE;16-17,hint_not_taken;13-15,0x2;12,0x0;0-11,0x0",
//"bnv$pack $ICCi_2,$hint,$label16",
//"bnvlr$pack $ICCi_2,$hint",
//"bp$pack $ICCi_2,$hint,$label16",
//"bplr$pack $ICCi_2,$hint",
//"bra$pack $hint_taken$label16",
//"bralr$pack$hint_taken",
"break|break$pack|31,pack;25-30,0x0;18-24,0x4;12-17,0x0;8-11,0x0;6-7,0x3;0-5,0x0",
//"bv$pack $ICCi_2,$hint,$label16",
//"bvlr$pack $ICCi_2,$hint",
//"cadd$pack $GRi,$GRj,$GRk,$CCi,$cond",
//"caddcc$pack $GRi,$GRj,$GRk,$CCi,$cond",
"call|call$pack $label24|31,pack;24-30,0xF;0-23,label24",
"callil|callil$pack @($GRi,$s12)|31,pack;26-30,0x0;25,0x1,18-24,0xd;12-17,GRi,0-11,s12",
"calll|calll$pack $callann($GRi,$GRj)|31,pack;26-30,0x0;25,0x1,18-24,0xc;12-17,GRi;6-11,0x0;0-5,GRj",
//"cand$pack $GRi,$GRj,$GRk,$CCi,$cond",
//"candcc$pack $GRi,$GRj,$GRk,$CCi,$cond",
//"ccalll$pack @($GRi,$GRj),$CCi,$cond",
//"cckc$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"cckeq$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"cckge$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"cckgt$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"cckhi$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"cckle$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"cckls$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"ccklt$pack $ICCi_3,$CRj_int,$CCi,$cond"
//"cckn$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"ccknc$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"cckne$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"cckno$pack $CRj_int,$CCi,$cond",
//"ccknv$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"cckp$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"cckra$pack $CRj_int,$CCi,$cond",
//"cckv$pack $ICCi_3,$CRj_int,$CCi,$cond",
//"cfabss$pack $FRj,$FRk,$CCi,$cond",
//"cfadds$pack $FRi,$FRj,$FRk,$CCi,$cond",
//"cfckeq$pack $FCCi_3,$CRj_float,$CCi,$cond",
//"cfckge$pack $FCCi_3,$CRj_float,$CCi,$cond",
//"cfckgt$pack $FCCi_3,$CRj_float,$CCi,$cond",
//"cfckle$pack $FCCi_3,$CRj_float,$CCi,$cond",
//"cfcklg$pack $FCCi_3,$CRj_float,$CCi,$cond",
//"cfcklt$pack $FCCi_3,$CRj_float,$CCi,$cond",
//"cfckne$pack $FCCi_3,$CRj_float,$CCi,$cond",
//"cfckno$pack $CRj_float,$CCi,$cond",
//"cfcko$pack $FCCi_3,$CRj_float,$CCi,$cond",
//"cfckra$pack $CRj_float,$CCi,$cond",
//"cfcku$pack $FCCi_3,$CRj_float,$CCi,$cond",
//"cfckue$pack $FCCi_3,$CRj_float,$CCi,$cond"
//"cfckug$pack $FCCi_3,$CRj_float,$CCi,$cond",
//"cfckuge$pack $FCCi_3,$CRj_float,$CCi,$cond",
"cfckul|cfckul$pack $FCCi_3,$CRj_float,$CCi,$cond|31,pack;27-30,0x5;25-26,CRj_float;18-24,0x6A;12-17,0x0;9-11,CCi;8,cond;6-7,0x1;2-5,0x0;0-1,FCCi_3",
"cfckule|cfckule$pack $FCCi_3,$CRj_float,$CCi,$cond|31,pack;27-30,0xd;25-26,CRj_float;18-24,0x6A;12-17,0x0;9-11,CCi;8,cond;6-7,0x1;2-5,0x0;0-1,FCCi_3",
"cfcmps|cfcmps$pack $FRi,$FRj,$FCCi_2,$CCi,$cond|31,pack;27-30,0x0;25-26,FCCi_2;18-24,0x6D;12-17,FRi;9-11,CCi;8,cond;6-7,0x2;0-5,FRj",
"cfdivs|cfdivs$pack $FRi,$FRj,$FRk,$CCi,$cond|31,pack;25-30,FRk;18-24,0x6E;12-17,FRi;9-11,CCi;8,cond;6-7,0x1;0-5,FRj",
//"cfitos$pack $FRintj,$FRk,$CCi,$cond",
//"cfmas$pack $FRi,$FRj,$FRk,$CCi,$cond",
//"cfmovs$pack $FRj,$FRk,$CCi,$cond",
//"cfmss$pack $FRi,$FRj,$FRk,$CCi,$cond",
//"cfmuls$pack $FRi,$FRj,$FRk,$CCi,$cond",






































        };

        static List<Tuple<int, int, string>> insts;

        static public void InstTable()
        {
            insts = new List<Tuple<int, int, string>>();

            foreach (string inst in insttab)
            {
                var parts = inst.Split('|');
                string name = parts[0];
                string output = parts[1];
                string bits = parts[2];
                string[] bb = bits.Split(';');

                int value = 0;
                int mask = 0;

                foreach (string b in bb)
                {
                    int start;
                    int end;

                    string[] c = b.Split(',');
                    string idxs = c[0];
                    string na = c[1];
                    var idx = idxs.IndexOf('-');
                    if (idx != -1)
                    {
                        var s = idxs.Substring(0, idx);
                        var e = idxs.Substring(idx + 1);
                        start = int.Parse(s);
                        end = int.Parse(e);
                    }
                    else
                    {
                        start = int.Parse(idxs);
                        end = start;
                    }

                    if( na.StartsWith("0x"))
                    {
                        var m = int.Parse(na.Substring(2), System.Globalization.NumberStyles.HexNumber);

                        for(int i = start, j =0; i <= end; i++, j++)
                        {
                            value |= ((m & j) << start);
                            mask |= 1 << i;
                        }
                    }
                }

                insts.Add(new Tuple<int, int, string>(value, mask, name));
            }
        }

        public static string FindInstruction(int value)
        {
            foreach (var inst in insts)
            {
                if ((inst.Item2 & value) == inst.Item1)
                    return inst.Item3;
            }

            //return "NO_INST";
            return "";
        }

        public static void DecodeFile(string fileName)
        {
            InstTable();

            if (File.Exists(fileName))
            {
                BinaryReader br = null;
                StreamWriter tw = null;

                try
                {
                    br = new BinaryReader(File.Open(fileName, FileMode.Open, FileAccess.Read, FileShare.ReadWrite));
                    tw = new StreamWriter(File.Open(fileName + ".frv.txt", FileMode.Create, FileAccess.Write, FileShare.ReadWrite));

                    var data = br.ReadBytes((int)br.BaseStream.Length);

                    int off = 0x1000;



                    //for (int i = 0; i < (data.Length / 4); i++)
                    for (int i = 0; i <0x300; i++)
                    {
                        int word = (data[off++] << 24) + (data[off++] << 16) + (data[off++] << 8) + (data[off++] << 0);

                        if (FindInstruction(word) != "")
                        {
                            var s = string.Format("{0:X8} {1:X8} {2}", i*4, word, FindInstruction(word));
                            Debug.WriteLine(s);
                            tw.WriteLine(s);
                        }
                     }

                }
                finally
                {
                    if (br != null)
                        br.Close();
                    if (tw != null)
                        tw.Close();
                }
            }

        }
    }
}
