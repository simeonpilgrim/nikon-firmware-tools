using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace dfr2
{
    class OldCode
    {



        //#define SYMBOL_MAX  32
        //#define OPND_MAX    (4*SYMBOL_MAX)
        //#define LINE_MAX    256
        //#define DISPERBLOCK 16

        //    const int MD_OPMAX = 8;
        //    const int MD_OPLEN = 3;
        //    const ulong MD_OPMASK = ((1ul << MD_OPLEN) - 1);

        //const int MD_TYPEBIT = 0;
        //const int MD_TYPELEN = 4;
        //const ulong MD_TYPEMASK = ((1 << MD_TYPELEN) - 1);
        //const int MD_LENGTHBIT = (MD_TYPEBIT + MD_TYPELEN);
        //const int MD_LENGTHLEN = 4;
        //const ulong MD_LENGTHMASK = ((1ul << MD_LENGTHLEN) - 1);
        //const int MD_SPECBIT = (MD_LENGTHBIT + MD_LENGTHLEN);
        //const ulong MD_SPECLEN = (MD_OPLEN * MD_OPMAX);

        //#define MKDATA(n,s)     (MEMTYPE_DATA|((n)<<MD_LENGTHBIT)|((s)<<MD_SPECBIT))



        //struct flag {
        //    int           width;
        //    unsigned int value;
        //    const char   *flag;
        //    const char   *desc;
        //} outflags[] = {
        //    { 2, OF_XREF,           "crossreference",   NULL                        },
        //    { 2, OF_CSTYLE,         "cstyle",           "use C-style operands"      },
        //    { 6, OF_DEBUG,          "debug",            NULL                        },
        //    { 2, OF_ALTDMOV,        "dmov",             "use alternates for DMOV"   },
        //    { 2, OF_HEXDOLLAR,      "dollar",           "use $ for hexadecimal"     },
        //    { 2, OF_FILEMAP,        "filemap",          "generate input file map"   },
        //    { 2, OF_MEMORYMAP,      "memorymap",        "generate memory map"       },
        //    { 2, OF_ALTREG,         "register",         "use AC, FP, SP"            },
        //    { 2, OF_ALTSHIFT,       "shift",            "use large shift constants" },
        //    { 2, OF_ALTSPECIALS,    "specials",         "use special registers"     },
        //    { 2, OF_ALTSTACK,       "stack",            "use PUSH and POP"          },
        //    { 2, OF_SYMTAB,         "symbols",          NULL                        },
        //    { 1, OF_VERBOSE,        "verbose",          NULL                        },
        //    { 1, OF_XREF,           "xreference",       NULL                        },
        //    { 0 }
        //};

        //size_t
        //fmtmap(char *buf, size_t len, struct map *map, struct range *rp)
        //{
        //    const char *r;
        //    size_t n;

        //    if (!rp) {
        //        *buf = 0;
        //        return 0;
        //    }

        //    if (map) {
        //        n = sprintf_s(buf, len, "  -%c ", map.opt);
        //        buf += n;
        //        len -= n;
        //    }
        //    n = sprintf_s(buf, len, "0x%08lX-0x%08lX", rp.start, rp.end);
        //    buf += n;
        //    len -= n;
        //    if (map) {
        //        if (!map.mapname)
        //            return n + sprintf_s(buf, len, "=0x%08lX", rp.data);
        //        if ((r = (*map.mapname)(rp.data)) != NULL)
        //            return n + sprintf_s(buf, len, "=%s", r);
        //    }
        //    return n;
        //}

        //void
        //dispmap(struct map *map, struct range *rp)
        //{
        //    char buf[LINE_MAX];
        //    fmtmap(buf, LINE_MAX, map, rp);
        //    fprintf(outfp, "%s\n", buf);
        //}

        //void
        //dumpmap(struct map *map)
        //{
        //    struct range *rp;

        //    fprintf(outfp, "%s:\n", map.name);
        //    for (rp = map.head; rp; rp = rp.next) {
        //        dispmap(map, rp);
        //    }
        //    nl();
        //}

        //void
        //fixmap(struct map *map, unsigned long data)
        //{
        //    struct range *rp, *dp;
        //    unsigned long next, maxlength;

        //    if (!map.head)
        //        insmap(map, 0, 0xFFFFFFFF, data);
        //    for (rp = map.head; rp; rp = rp.next) {
        //        if (rp.next && rp.end >= rp.next.start) {
        //            error("Range 0x%08lX-0x%08lX overlaps 0x%08lX-0x%08lX\n",
        //                rp.start, rp.end, rp.next.start, rp.next.end);
        //            rp.next.start = rp.end + 1;
        //            if (rp.next.start > rp.next.end) {
        //                dp = rp.next;
        //                rp.next = rp.next.next;
        //                free(dp);
        //            }
        //        }
        //    }
        //}

        //void
        //fillmap(struct map *map, unsigned long data)
        //{
        //    struct range *np, *rp;
        //    unsigned long start, end;
        //    int fill;

        //    for (rp = map.head; rp; rp = rp.next) {
        //        fill = 0;
        //        if (rp == map.head && rp.start != 0) {
        //            np = (struct range *) myalloc(sizeof (struct range), "map range");
        //            np.next  = map.head;
        //            np.start = 0;
        //            np.end   = rp.start - 1;
        //            np.data  = data;
        //            map.head = rp = np;
        //        }
        //        if (rp.next) {
        //            if (rp.end + 1 < rp.next.start) {
        //                fill = 1;
        //                start = rp.end + 1;
        //                end = rp.next.start - 1;
        //            }
        //        }
        //        else if (rp.end != 0xFFFFFFFF) {
        //            fill = 1;
        //            start = rp.end + 1;
        //            end = 0xFFFFFFFF;
        //        }
        //        if (fill) {
        //            np = (struct range *) myalloc(sizeof (struct range), "map range");
        //            np.next  = rp.next;
        //            np.start = start;
        //            np.end   = end;
        //            np.data  = data;
        //            rp.next = np;
        //        }
        //    }
        //}

        //void
        //delmap(struct map *map, unsigned long data)
        //{
        //    struct range *rp, *dp;

        //    while (map.head && map.head.data == data) {
        //        dp = map.head;
        //        map.head = map.head.next;
        //        free(dp);
        //    }
        //    for (rp = map.head; rp.next; rp = rp.next) {
        //        if (rp.next.data == data) {
        //            dp = rp.next;
        //            rp.next = rp.next.next;
        //            free(dp);
        //        }
        //    }
        //}


        //static void insmap(MAP map, UInt64 start, UInt64 end, DATA data)
        //{
        //    //change to map.list.Add(new Range(start, end, data);
        //}


    }
}
