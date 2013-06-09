package com.nikonhacker.gui.swing;

import com.nikonhacker.Format;

import javax.swing.*;

public class JValueButton extends JButton {
    private int value;

    public JValueButton(int value) {
        super("0x" + Format.asHex(value, 2));
        this.value = value;
    }

    public JValueButton(String text, int value) {
        super(text);
        this.value = value;
    }

    public int getValue() {
        return value;
    }


}
