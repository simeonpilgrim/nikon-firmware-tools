package com.nikonhacker.gui.swing.monitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Inspired by http://www.coderanch.com/t/323868/java/java/Detecting-JInternalFrame-movement
 * Author: saager mhatre
 */
public class InternalMonitor extends ComponentAdapter {
    JFrame         frame         = new JFrame("Monitoring JInternalFrame movements");
    JDesktopPane   desktop       = new JDesktopPane();
    JInternalFrame internalFrame = new JInternalFrame("Move Me!", false, false, false, false);
    JLabel         txtPos        = new JLabel("Location & size appears here!", JLabel.CENTER);

    public InternalMonitor() {
        internalFrame.getContentPane().add(txtPos);
        internalFrame.pack();
        internalFrame.addComponentListener(this);
        desktop.add(internalFrame);
        frame.setContentPane(desktop);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 400);
        frame.show();
        internalFrame.show();
    }

    public void componentMoved(ComponentEvent e){
        updateLabel(e);
    }

    public void componentResized(ComponentEvent e){
        updateLabel(e);
    }

    private void updateLabel(ComponentEvent e) {
        Point loc =e.getComponent().getLocation();
        Dimension s =e.getComponent().getSize();
        txtPos.setText("From " + loc.x + "," + loc.y + " to " + (loc.x + s.width) + " X " + (loc.y + s.height));
    }
    public static void main(String arg[]){
        new InternalMonitor();
    }
}

