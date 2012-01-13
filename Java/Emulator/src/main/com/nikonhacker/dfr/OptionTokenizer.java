package com.nikonhacker.dfr;

import org.apache.commons.lang3.StringUtils;

///* reinventing the wheel because resetting getopt() isn't portable */
public class OptionTokenizer
{
    int index;
    String[] arguments;
    String currentToken;

    public OptionTokenizer(String[] arguments)
    {
        index = 0;
        this.arguments = arguments;
        currentToken = null;
    }

    void end() { }

    Character getNextOption() {
        if (StringUtils.isBlank(currentToken)) {
            if (index >= arguments.length) return null; // end of list

            currentToken = arguments[index++]; // read next

            if (currentToken.charAt(0) != '-') return 0; // not an option (input filename)
            
            currentToken = currentToken.substring(1); // remove the dash 
        }
        char c = currentToken.charAt(0); // get the option
        currentToken = currentToken.substring(1); // to allow options such as -d2 . Now currentArg = 2
        return c;
    }

    String getArgument() {
        if (StringUtils.isBlank(currentToken)) {
            if (index >= arguments.length) return null; // end of list

            currentToken = arguments[index++]; // read next
        }
        String argument = currentToken; // get the argument
        currentToken = null; // clear current to force a read at next call
        return argument;
    }
}



