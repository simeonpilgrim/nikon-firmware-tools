package com.nikonhacker.gui.component.interruptController;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.interruptController.InterruptController;

import javax.swing.*;
import java.awt.*;

public class JInterruptButton extends JButton {
    private int interruptNumber;

    public JInterruptButton(String text, int interruptNumber) {
        super(text);
        this.interruptNumber = interruptNumber;
        setButtonTooltip(interruptNumber);
    }

    public int getInterruptNumber() {
        return interruptNumber;
    }

    private void setButtonTooltip(int interruptNumber) {
        String tooltip = "INT 0x" + Format.asHex(interruptNumber,2);
        switch (interruptNumber) {
            case 0x00:
                setForeground(Color.RED.darker());
                tooltip = "Reset";
                break;
            case 0x01:
                setForeground(Color.ORANGE.darker());
                tooltip = "System reserved or Mode Vector";
                break;
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
                setForeground(Color.ORANGE.darker());
                tooltip = "System reserved";
                break;
            case 0x09:
                setForeground(Color.BLUE);
                tooltip = "Emulator Exception";
                break;
            case 0x0A:
                setForeground(Color.BLUE);
                tooltip = "Instruction break trap";
                break;
            case 0x0B:
                setForeground(Color.BLUE);
                tooltip = "Operand break trap";
                break;
            case 0x0C:
                setForeground(Color.BLUE);
                tooltip = "Step trace break trap";
                break;
            case 0x0D:
                setForeground(Color.BLUE);
                tooltip = "Emulator exception";
                break;
            case 0x0E:
                setForeground(Color.BLUE);
                tooltip = "Undefined instruction exception";
                break;
            case 0x0F:
                tooltip += " or NMI";
                break;
            case 0x40:
            case 0x41:
                setForeground(Color.ORANGE.darker());
                tooltip = "System reserved";
                break;
        }

        if (interruptNumber >= 0x10 && interruptNumber <= 0x2F) {
            int irNumber = interruptNumber - InterruptController.INTERRUPT_NUMBER_EXTERNAL_IR_OFFSET;
            tooltip += " or IR" + (irNumber <10?"0":"") + irNumber;
        }

        setToolTipText(tooltip);
    }

}
