package com.nikonhacker.dfr;

import org.apache.commons.lang3.StringUtils;

public class Symbol {
    int address;
    String name;
    String comment;

    public Symbol(int address, String name, String comment) {
        this.address = address;
        this.name = name;
        this.comment = comment;
    }

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        String out = name;
        if (StringUtils.isNotBlank(comment)) {
            out += " (" + comment +")";
        }
        return out;
    }
}
