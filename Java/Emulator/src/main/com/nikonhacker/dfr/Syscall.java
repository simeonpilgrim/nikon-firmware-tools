package com.nikonhacker.dfr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.Memory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Syscall extends Symbol {

    private static final int INTERRUPT_VECTOR_BASE_ADDRESS = 0xDFC00;

    static List<Syscall> syscallList;

    public Syscall(Integer address, String rawText) throws ParsingException {
        super(address, rawText);
    }

    public static List<Syscall> getList(Memory memory) throws ParsingException {
        if (syscallList == null) {
            syscallList = new ArrayList<Syscall>();
            try {
                System.out.println("Assuming interrupt vector at 0x" + Format.asHex(INTERRUPT_VECTOR_BASE_ADDRESS, 8) + "...");
                int int40address = memory.load32(INTERRUPT_VECTOR_BASE_ADDRESS + 0x3FC - 0x40 * 4);
                System.out.println("INT 0x40 is at 0x" + Format.asHex(int40address, 8) + "...");
                System.out.println("Assuming the layout of D5100 is standard, the base address for system calls computation is stored at 0x" + Format.asHex(int40address + 64, 8) + "...");
                int baseAddress = memory.loadInstruction32(int40address + 64);
                System.out.println("Base address is thus 0x" + Format.asHex(baseAddress, 8) + "...");

                Properties properties = new Properties() ;
                URL url = Syscall.class.getResource("realos-systemcalls.properties");
                properties.load(url.openStream());

                for (Object o : properties.keySet()) {
                    int offset = Format.parseUnsigned((String) o);
                    Syscall syscall = new Syscall(baseAddress + Dfr.signExtend(16, memory.loadInstruction16(baseAddress + offset * 2)), (String) properties.get(o));
                    syscallList.add(syscall);
                }
            } catch (IOException e) {
                throw new ParsingException(e);
            } catch (ParsingException e) {
                throw new ParsingException(e);
            }
        }
        return syscallList;
    }
}
