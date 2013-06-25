package com.nikonhacker.gui.ioport;

import com.nikonhacker.Prefs;
import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.tx.TxIoPort;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.ioPort.IoPortsFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Original source was from http://harmoniccode.blogspot.be/2010/11/friday-fun-component-iii.html
 */
public class IoPortsFrameTest {
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("SteelCheckBoxTest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);
        JDesktopPane mdiPane = new JDesktopPane();
        IoPort[] ioPorts = new IoPort[3];
        for (int i = 0; i < ioPorts.length; i++) {
            ioPorts[i] = new TxIoPort(i, null, new Prefs());
        }
        frame.add(mdiPane);
        mdiPane.add(new IoPortsFrame("test", "io", false, true, false, false, 0, new EmulatorUI(), ioPorts));
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);
    }


    public static void main(String args[]) {

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
