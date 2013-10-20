package com.nikonhacker.gui.swing;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * This is the code of the standard "JButton",
 * modified to be right-clickable to remain depressed
 */
public class JToggleableButton extends JButton implements Accessible {

    private Action whenOnAction;
    private Action whenOffAction;

    public JToggleableButton() {
        this(null, null);
    }

    public JToggleableButton(Icon icon) {
        this(null, icon);
    }

    public JToggleableButton(String text) {
        this(text, null);
    }

    /**
     * @deprecated use JToggleableButton(whenOn, whenOff) for JToggleableButton
     */
    public JToggleableButton(Action a) {
        setOnAction(a);
    }

//    public JToggleableButton(Action whenOnAction, Action whenOffAction) {
//        this();
//        setOnAction(whenOnAction);
////        setOffAction(whenOffAction);
//    }

    public JToggleableButton(String text, Icon icon) {
        // Create the model
//        TODO Is that the right thing to do ?
//        Or just add a "when Off Action ?" > releaseAction
        setModel(new RightClickAwareButtonModel());

        // initialize
        init(text, icon);

        // add right-click functionality
        for (MouseListener mouseListener : getMouseListeners()) {
            System.out.println(mouseListener);
        }
        removeMouseListener(getMouseListeners()[0]);
        addMouseListener(new RightClickAwareButtonListener(this));
    }

    public void setOnAction(Action whenOnAction) {
        Action oldValue = getAction();
        if (whenOnAction==null || !whenOnAction.equals(whenOnAction)) {
            this.whenOnAction = whenOnAction;
            if (oldValue!=null) {
                removeActionListener(oldValue);
//                oldValue.removePropertyChangeListener(actionPropertyChangeListener);
//                actionPropertyChangeListener = null;
            }
            configurePropertiesFromAction(whenOnAction);
            if (whenOnAction!=null) {
                // Don't add if it is already a listener
//                if (!isListener(ActionListener.class, whenOnAction)) {
//                    addActionListener(whenOnAction);
//                }
//                // Reverse linkage:
//                actionPropertyChangeListener = createActionPropertyChangeListener(whenOnAction);
//                whenOnAction.addPropertyChangeListener(actionPropertyChangeListener);
            }
            firePropertyChange("action", oldValue, whenOnAction);
        }
    }

    private class RightClickAwareButtonListener extends BasicButtonListener {
        // Cannot access them in parent class as they are private. So duplicating here...
        private long lastPressedTimestamp = -1;
        private boolean shouldDiscardRelease = false;

        public RightClickAwareButtonListener(AbstractButton b) {
            super(b);
        }

        /**
         * Overridden to handle right click
         * @param e
         */
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)
                    || (SwingUtilities.isRightMouseButton(e) && !model.isPressed())) {
                buttonOn(e);
            }
            else if (SwingUtilities.isRightMouseButton(e) && model.isPressed()) {
                buttonOff(e);
            }
        }

        /**
         * Overridden to factor out button off functionality
         */
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                buttonOff(e);
            }
        }

        /**
         * Overridden to not change the armed status
         * @param e
         */
        public void mouseEntered(MouseEvent e) {
            AbstractButton b = (AbstractButton) e.getSource();
            ButtonModel model = b.getModel();
            if (b.isRolloverEnabled() && !SwingUtilities.isLeftMouseButton(e)) {
                model.setRollover(true);
            }
        }

        /**
         * Overridden to not change the armed status
         * @param e
         */
        public void mouseExited(MouseEvent e) {
            AbstractButton b = (AbstractButton) e.getSource();
            ButtonModel model = b.getModel();
            if(b.isRolloverEnabled()) {
                model.setRollover(false);
            }
        }


        private void buttonOn(MouseEvent e) {
            AbstractButton b = (AbstractButton) e.getSource();

            if(b.contains(e.getX(), e.getY())) {
                long multiClickThreshhold = b.getMultiClickThreshhold();
                long lastTime = lastPressedTimestamp;
                long currentTime = lastPressedTimestamp = e.getWhen();
                if (lastTime != -1 && currentTime - lastTime < multiClickThreshhold) {
                    shouldDiscardRelease = true;
                    return;
                }

                ButtonModel model = b.getModel();
                if (!model.isEnabled()) {
                    // Disabled buttons ignore all input...
                    return;
                }
                if (!model.isArmed()) {
                    // button not armed, should be
                    model.setArmed(true);
                }
                model.setPressed(true);
                if(!b.hasFocus() && b.isRequestFocusEnabled()) {
                    b.requestFocus();
                }
            }
        }

        private void buttonOff(MouseEvent e) {
            // Support for multiClickThreshhold
            if (shouldDiscardRelease) {
                shouldDiscardRelease = false;
                return;
            }
            AbstractButton b = (AbstractButton) e.getSource();
            ButtonModel model = b.getModel();
            model.setPressed(false);
            model.setArmed(false);
        }

    }

    private class RightClickAwareButtonModel extends DefaultButtonModel {
        // TODO: handle two events
    }
}
