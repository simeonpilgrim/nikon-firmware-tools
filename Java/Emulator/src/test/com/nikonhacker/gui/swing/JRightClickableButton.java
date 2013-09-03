package com.nikonhacker.gui.swing;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JRightClickableButton extends JButton {
    public JRightClickableButton() {
        super();
        addRightClickFunctionality();
    }

    public JRightClickableButton(Icon icon) {
        super(icon);
        addRightClickFunctionality();
    }

    public JRightClickableButton(String text) {
        super(text);
        addRightClickFunctionality();
    }

    public JRightClickableButton(Action a) {
        super(a);
        addRightClickFunctionality();
    }

    public JRightClickableButton(String text, Icon icon) {
        super(text, icon);
        addRightClickFunctionality();
    }

    private void addRightClickFunctionality() {
        final JButton button = this;
        addMouseListener(new MouseAdapter() {
            boolean pressed;

            @Override
            public void mousePressed(MouseEvent e) {
                button.getModel().setArmed(true);
                button.getModel().setPressed(true);
                pressed = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //if(isRightButtonPressed) {underlyingButton.getModel().setPressed(true));
                button.getModel().setArmed(false);
                button.getModel().setPressed(false);

                if (pressed) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        button.setText("F");
                    }
                    else {
                        button.setText("X");
                    }
                }
                pressed = false;

            }

            @Override
            public void mouseExited(MouseEvent e) {
                pressed = false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                pressed = true;
            }
        });
    }
}
