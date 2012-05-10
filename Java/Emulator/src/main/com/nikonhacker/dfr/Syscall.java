package com.nikonhacker.dfr;

import com.nikonhacker.Format;
import com.nikonhacker.emu.memory.Memory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Syscall {

    private static final int INTERRUPT_VECTOR_BASE_ADDRESS = 0xDFC00;

    private int address;
    private String name;

    static List<Syscall> syscallList;

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
                    Syscall syscall = new Syscall();
                    int offset = Format.parseUnsigned((String) o);
                    syscall.setName((String) properties.get(o));
                    syscall.setAddress(baseAddress + Dfr.signExtend(16, memory.loadInstruction16(baseAddress + offset * 2)));
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
