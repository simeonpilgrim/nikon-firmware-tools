using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Diagnostics;


namespace dfr2
{
    partial class Program
    {
        static RANGE parsemap(int opt, string s, out string rp)
        {
            ulong start;
            ulong end;

            if (parserange(opt, s, out rp, out start, out end))
                return null;

            s = rp;
            if (s[0] == ':' || s[0] == '=')
            {
                var s1 = s.Substring(1);
                ulong offset = parseul(s1, out rp);
                if (s1 == rp)
                {
                    rp = s;
                    Log("{0}: -{1} expected a number at \"{2}\"\n", cmdname, opt, rp);
                    return null;
                }

                return new RANGE(start, end, offset);
            }
            Log("{0}: -{1} missing map value", cmdname, opt);
            return null;
        }

        static void showhelp(string[,] msgs)
        {
            for (int i = 0; i < msgs.GetLength(0); i++)
            {
                string a = msgs[i, 0];
                string b = msgs[i, 1];

                string fmt = "";

                if (a != "")
                {
                    if (b != "")
                        fmt = "  {0:-18}-- {1}";
                    else
                        fmt = "{0}{1}";
                }
                else
                {
                    fmt = "  {0:-18}   {1}";
                }

                Log(fmt, a, b);
            }
        }

        static void usage()
        {
            string[,] help = {
                {"-d range",         "disassemble only specified range"},
                //{ "-e address=name",  "define entry point symbol"}, 
                {"-f range=address", "map range of input file to memory address"},
                {"-h",               "display this message"},
                { "-i range=offset",  "map range of memory to input file offset"},
                { "-l",               "little-endian input file"},
                {"-m range=type",    "describe memory range (use -m? to list types)"},
                { "-o filename",      "output file"},
                {"-r",               "separate output file for each memory range"},
                //{ "-s address=name",  "define symbol"},
                {"-t address",       "equivalent to -m address,0x400=DATA:V"},
                {"-v",               "verbose"},
                {"-w options",       "output options (use -w? to list options)"},
                {"-x file",          "read options from file"},
                {"Numbers are C-style. A range is start-end or start,length.", ""},
            };

            Log("Usage: {0} [options] filename", cmdname);
            Log("Options:");
            showhelp(help);
        }

        static UInt64 parseul(string s, out string rp)
        {
            bool hex = (s.Length > 2 && s[0] == '0' && (s[1] == 'x' || s[1] == 'X'));

            UInt64 v = 0;
            int i = hex ? 2 : 0;
            for (; i < s.Length; i++)
            {
                Char ch = s[i];
                if (hex)
                {
                    if (ch >= '0' && ch <= '9')
                        v = (v * 0x10) + ch - '0';
                    else if (ch >= 'a' && ch <= 'f')
                        v = (v * 0x10) + ch - 'a' + 0x0a;
                    else if (ch >= 'A' && ch <= 'F')
                        v = (v * 0x10) + ch - 'A' + 0x0a;
                    else
                        break;
                }
                else
                {
                    if (ch >= '0' && ch <= '9')
                        v = (v * 10) + ch - '0';
                    else
                        break;
                }
            }

            if (i != s.Length)
            {
                switch (s[i])
                {
                    case 'k':
                    case 'K':
                        v *= 1024; i++; break;
                    case 'm':
                    case 'M':
                        v *= 1048576; i++; break;
                }
            }

            rp = s.Substring(i);

            return v;
        }

        static bool parserange(int opt, string s, out string r, out UInt64 start, out UInt64 end)
        {
            r = s;
            start = 0;
            end = 0;

            UInt64 v = parseul(s, out r);
            if (r == s)
            {
                Log("{0}: -{1} expected a number at \"{2}\"\n", cmdname, opt, s);
                return true;
            }

            start = v;
            end = 0xFFFFFFFF;

            if (r.Length > 0)
            {
                char c = r[0];
                if (c == ',' || c == '-')
                {
                    string s2 = r.Substring(1);
                    string r2;
                    v = parseul(s2, out r2);

                    if (r2 == s2)
                    {
                        Log("{0}: -{1} expected a number at \"{2}\"", cmdname, opt, s2);
                        return true;
                    }

                    if (c == '-')
                        end = v;
                    else
                        end = start + v - 1;
                    r = r2;
                }
            }

            if (end < start)
            {
                Log("{0}: -{1} range has end less than start 0x{2:x8}", cmdname, opt, start);
                return true;
            }

            return false;
        }


        //int
        //parseflags(int opt, const char *arg, uint32_t *flagsp, struct flag *flag)
        //{
        //    char *s;
        //    int i, on;

        //    while (*arg) {
        //        if (!_strnicmp(arg, "no", 2)) {
        //            on = 0;
        //            arg += 2;
        //        }
        //        else {
        //            on = 1;
        //        }
        //        for (i = 0; flag[i].width; ++i) {
        //            if (!_strnicmp(arg, flag[i].flag, flag[i].width)) {
        //                if (flag[i].value & OF_INVERT)
        //                    on = !on;
        //                if (on)
        //                    *flagsp |= flag[i].value;
        //                else
        //                    *flagsp &= ~flag[i].value;
        //                break;
        //            }
        //        }
        //        if (!flag[i].width) {
        //            if (*arg != '?')
        //                fprintf(stderr, "%s: unknown flag at \"%s\"\n", cmdname, arg);
        //            for (i = 0; flag[i].width; ++i)
        //                if (flag[i].desc)
        //                    fprintf(stderr, "  -%c %-15s-- %s\n",
        //                            opt, flag[i].flag, flag[i].desc);
        //            return 1;
        //        }
        //        if ((s = strchr(arg, ',')) != NULL)
        //            arg = s + 1;
        //        else
        //            break;
        //    }
        //    return 0;
        //}

        static string[,] memtypehelp = {
            { "Memtypes are:",    ""},
            { "NONE",           "do not disassemble"},
            { "UNKNOWN",        "unknown contents"},
            { "CODE",           "disassemble as code where possible"},
            { "DATA[:spec]",    "disassemble as data; spec is up to 8 of:"},
            { "",               "  L -- long (32-bit) data"},
            { "",               "  N -- long (32-bit) data, no labels"},
            { "",               "  R -- rational"},
            { "",               "  V -- vector"},
            { "",               "  W -- word (16-bit) data"},
        };


        static bool parsememtype(string arg, out DATA memp)
        {
            memp = new DATA();

            string wtf = "memory type";
            if (arg != null && arg.Length > 0)
            {
                if (arg != null && arg != "")
                {
                    switch (arg[0])
                    {
                        case 'C':
                        case 'c':
                            memp.mem = MemType.MEMTYPE_CODE;
                            break;

                        case 'D':
                        case 'd':
                            wtf = "data type";
                            memp.mem = MemType.MEMTYPE_DATA;
                            memp.spec.Add(SpecType.MD_WORD);

                            int sp = arg.LastIndexOf(':');
                            if (sp != -1)
                            {
                                memp.spec.Clear(); // remove above default of MD_WORD
                                sp++;
                                while (sp < arg.Length)
                                {
                                    char c = arg[sp++];

                                    SpecType md;
                                    switch (Char.ToLower(c))
                                    {
                                        case 'l': md = SpecType.MD_LONG; break;
                                        case 'n': md = SpecType.MD_LONGNUM; break;
                                        case 'r': md = SpecType.MD_RATIONAL; break;
                                        case 'v': md = SpecType.MD_VECTOR; break;
                                        case 'w': md = SpecType.MD_WORD; break;
                                        default:
                                            Log("{0}: unrecognized {1} at \"{2}\"\n", cmdname, wtf, arg);
                                            showhelp(memtypehelp);
                                            return true;
                                    }
                                    memp.spec.Add(md);
                                }
                            }
                            break;

                        case 'n':
                        case 'N':
                            memp.mem = MemType.MEMTYPE_NONE;
                            break;

                        case 'u':
                        case 'U':
                            memp.mem = MemType.MEMTYPE_UNKNOWN;
                            break;

                        case 'v':
                        case 'V':
                            memp.mem = MemType.MEMTYPE_DATA;
                            memp.spec.Add(SpecType.MD_VECTOR);
                            break;

                        default:
                            Log("{0}: unrecognized {1} at \"{2}\"\n", cmdname, wtf, arg);
                            showhelp(memtypehelp);
                            return true;
                    }
                }
            }

            return false;
        }


        ///* reinventing the wheel because resetting getopt() isn't portable */
        struct parseopt
        {
            int index;
            string[] args;
            string cur;

            internal parseopt(string[] _args)
            {
                index = 0;
                args = _args;
                cur = null;
            }

            internal void end() { }
            internal int next()
            {
                if (cur == null || cur == "")
                {
                    if (index >= args.Length)
                        return -1;
                    cur = args[index++];
                    
                    if (cur[0] != '-')
                        return 0;
                    cur = cur.Substring(1);
                }
                char c = cur[0];
                cur = cur.Substring(1);
                return c;
            }

            internal string arg()
            {
                if (cur == null || cur == "")
                {
                    if (index >= args.Length)
                        return "";

                    cur = args[index++];
                }
                var r = cur;
                cur = null;
                return r;
            }
        };






        static bool options(params string[] args)
        {
            parseopt pos = new parseopt(args);
            ulong start, end;
            DATA map;
            int opt;
            string r;
            string arg;

            while ((opt = pos.next()) >= 0)
            {
                switch (opt)
                {
                    case 0:
                        if (opt_infile != "")
                        {
                            Log("{0}: too many input files", cmdname);
                            usage();
                            return false;
                        }
                        opt_infile = pos.arg();
                        break;


                    case 'D':
                    case 'd':
                        arg = pos.arg();
                        if (arg == null || arg == "") goto missing;

                        if (parserange(opt, arg, out r, out start, out end))
                            break;
                        rangemap.Add(new RANGE(start, end, 1));
                        break;

                    case 'E':
                    case 'e':
                        //        if (!(arg = parseopt_arg(&pos)))
                        //            goto missing;
                        //        fprintf(stderr, "%s: symbol table not implemented yet!\n");
                        return false;

                    case 'F':
                    case 'f':
                        //        if (!(arg = parseopt_arg(&pos)))
                        //            goto missing;
                        //        if (parsemap(opt, arg, &r, &start, &end, &map))
                        //            break;
                        //        insmap(&filemap, map, map + end - start, start);
                        break;

                    case 'H':
                    case 'h':
                    case '?':
                        usage();
                        return false;

                    case 'I':
                    case 'i':
                        arg = pos.arg();
                        if (arg == null || arg == "") goto missing;

                        RANGE range = parsemap(opt, arg, out r);
                        if (range == null)
                            break;

                        filemap.Add(range);
                        break;

                    case 'L':
                    case 'l':
                        opt_little = true;
                        break;

                    case 'M':
                    case 'm':
                        arg = pos.arg();
                        if (arg == null ||
                            parserange(opt, arg, out r, out start, out end))
                        {
                            showhelp(memtypehelp);
                            return false;
                        }

                        if (parsememtype(r.Substring(1), out map))
                            return false;

                        memmap.Add(new RANGE(start, end, map));
                        break;

                    case 'O':
                    case 'o':
                        opt_outfile = pos.arg();
                        if (opt_outfile == null || opt_outfile == "")
                            goto missing;
                        break;

                    case 'R':
                    case 'r':
                        opt_split = true;
                        break;

                    case 'S':
                    case 's':
                        //        if (!(arg = parseopt_arg(&pos)))
                        //            goto missing;
                        //        fprintf(stderr, "%s: symbol table not implemented yet!\n");
                        //        exit(1);
                        break;

                    case 'T':
                    case 't':
                        //        if (!(arg = parseopt_arg(&pos)))
                        //            goto missing;
                        //        start = parseul(arg, &r);
                        //        insmap(&memmap, start, start + 0x3FF, MKDATA(1, MD_VECTOR));
                        break;

                    case 'V':
                    case 'v':
                        opt_outflag |= OutOpt.OF_VERBOSE;
                        break;

                    case 'W':
                    case 'w':
                        //        if (!(arg = parseopt_arg(&pos)))
                        //            goto missing;
                        //        if (parseflags(opt, arg, &opt_outflag, outflags))
                        //            exit(1);
                        break;

                    case 'X':
                    case 'x':
                        arg = pos.arg();
                        if (arg == null ||
                            fileoptions(arg) == false)
                        {
                            Log("{0}: cannot open options file \"{1}\"", cmdname, arg);
                            usage();
                            return false;
                        }
                        break;

                    case 'Z':
                    case 'z':
                        opt_outflag |= OutOpt.OF_DEBUG;
                        break;

                    default:
                        Log("{0}: unknown option \"-{1}\"", cmdname, opt);
                        usage();
                        return false;
                }
            }

            pos.end();
            return true;

        missing:
            Log("{0}: option \"-{1}\" requires an argument", cmdname, opt);
            return false;
        }

        static bool fileoptions(string filename)
        {
            if (File.Exists(filename) == false)
                return false;

            using (var fp = File.OpenText(filename))
            {
                string buf = null;
                while ((buf = fp.ReadLine()) != null)
                {
                    buf = buf.Trim();
                    if (buf.Length == 0 || buf[0] == '#')
                        continue;

                    if ((buf[0] == '-') && (buf[1] != 0) && buf.Length > 2)
                    {
                        if (Char.IsWhiteSpace(buf[2]))
                        {
                            string ss = buf.Substring(0, 2);
                            string p = buf.Substring(2).Trim();
                            if (p != null)
                            {
                                options(ss, p);
                                continue;
                            }
                        }
                    }

                    options(buf);
                }
            }

            return true;
        }
    }
}
