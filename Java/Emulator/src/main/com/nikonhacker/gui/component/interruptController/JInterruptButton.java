package com.nikonhacker.gui.component.interruptController;

import javax.swing.*;

public class JInterruptButton extends JButton {
    private int interruptNumber;

    public JInterruptButton(String text, int interruptNumber) {
        super(text);
        this.interruptNumber = interruptNumber;
    }

    public int getInterruptNumber() {
        return interruptNumber;
    }


}
