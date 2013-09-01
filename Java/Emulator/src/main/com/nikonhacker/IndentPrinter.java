package com.nikonhacker;

import java.io.PrintWriter;

public class IndentPrinter {

    private String prefix = "";
    PrintWriter internalWriter;

    public IndentPrinter(PrintWriter internalWriter) {
        this.internalWriter = internalWriter;
    }

    public void indent() {
        prefix += "  ";
    }

    public void outdent() {
        if (prefix.length() > 0) {
            prefix = prefix.substring(0, prefix.length() - 2);
        }
        else {
            internalWriter.println("<< requested outdent cannot be honored");
        }
    }

    public void print(String s) {
        internalWriter.print(prefix + s);
    }

    public void println(String s) {
        internalWriter.println(prefix + s);
    }

    public void printlnNonIndented(String s) {
        internalWriter.println(s);
    }
}
