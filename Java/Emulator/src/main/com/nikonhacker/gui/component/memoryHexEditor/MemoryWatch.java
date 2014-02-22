package com.nikonhacker.gui.component.memoryHexEditor;

public class MemoryWatch {
    String name;
    int address;

    public MemoryWatch() {
    }

    public MemoryWatch(String name, int address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

}
