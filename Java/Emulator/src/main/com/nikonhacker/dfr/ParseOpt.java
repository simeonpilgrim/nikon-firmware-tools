package com.nikonhacker.dfr;

///* reinventing the wheel because resetting getopt() isn't portable */
public class ParseOpt
{
    int index;
    String[] args;
    String cur;

    public ParseOpt(String[] _args)
    {
        index = 0;
        args = _args;
        cur = null;
    }

    void end() { }

    char next()
    {
        if (cur == null || "".equals(cur))
        {
            if (index >= args.length)
                return Character.MAX_VALUE;
            cur = args[index++];

            if (cur.charAt(0) != '-')
                return 0;
            cur = cur.substring(1);
        }
        char c = cur.charAt(0);
        cur = cur.substring(1);
        return c;
    }

    String arg()
    {
        if (cur == null || "".equals(cur))
        {
            if (index >= args.length)
                return "";

            cur = args[index++];
        }
        String r = cur;
        cur = null;
        return r;
    }
}



