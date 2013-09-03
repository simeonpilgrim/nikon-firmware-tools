package com.nikonhacker.gui.component.disassembly;

import com.nikonhacker.emu.AddressRange;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class DisassemblyLogger {
    private boolean logging;
    boolean includeTimestamp      = true;
    boolean includeIndent         = true;
    boolean includeInstruction    = true;
    boolean includeInterruptMarks = true;
    String  prefix                = "";
    private List<Writer> writers = new ArrayList<>();
    private List<AddressRange> ranges;

    /**
     * Basic empty constructor
     */
    public DisassemblyLogger() {
    }

    /**
     * Simple constructor used for debug
     * @param printStream
     */
    public DisassemblyLogger(PrintStream printStream) {
        writers.add(new PrintWriter(printStream));
        includeInstruction = true;
        logging = true;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public boolean isIncludeTimestamp() {
        return includeTimestamp;
    }

    public void setIncludeTimestamp(boolean includeTimestamp) {
        this.includeTimestamp = includeTimestamp;
    }

    public boolean isIncludeIndent() {
        return includeIndent;
    }

    public void setIncludeIndent(boolean includeIndent) {
        this.includeIndent = includeIndent;
    }

    public boolean isIncludeInstruction() {
        return includeInstruction;
    }

    public void setIncludeInstruction(boolean includeInstruction) {
        this.includeInstruction = includeInstruction;
    }

    public boolean isIncludeInterruptMarks() {
        return includeInterruptMarks;
    }

    public void setIncludeInterruptMarks(boolean includeInterruptMarks) {
        this.includeInterruptMarks = includeInterruptMarks;
    }

    public void indent() {
        if (includeIndent) {
            prefix += "  ";
        }
    }

    public void outdent() {
        if (includeIndent) {
            if (prefix.length() > 1) {
                prefix = prefix.substring(0, prefix.length() - 2);
            }
            else {
                println("<< requested outdent cannot be honored");
            }
        }
    }

    public void clearIndent() {
        prefix = "";
    }

    public String getIndent() {
        return prefix;
    }

    public void print(String s) {
        if (logging) {
            rawPrint(s);
        }
    }

    public void println(String s) {
        if (logging) {
            rawPrint(s + System.lineSeparator());
        }
    }

    public void rawPrint(String s) {
        if (logging) {
            for (Writer writer : writers) {
                try {
                    writer.append(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Writer> getWriters() {
        return writers;
    }

    public boolean mustLog(int pc) {
        // by convention, null means no filtering
        if (ranges == null) return true;

        for (AddressRange range : ranges) {
            if (range.includes(pc)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set ranges
     * @param ranges by convention, null means no filtering
     */
    public void setRanges(List<AddressRange> ranges) {
        this.ranges = ranges;
    }

    public List<AddressRange> getRanges() {
        return ranges;
    }
}
