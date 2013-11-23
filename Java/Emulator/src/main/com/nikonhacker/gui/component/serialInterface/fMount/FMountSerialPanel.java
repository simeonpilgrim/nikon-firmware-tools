package com.nikonhacker.gui.component.serialInterface.fMount;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.serialInterface.fMount.FMountCircuit;
import com.nikonhacker.emu.peripherials.serialInterface.fMount.LensPrototype;
import com.nikonhacker.emu.Platform;
import com.nikonhacker.gui.EmulatorUI;

import com.nikonhacker.gui.component.serialInterface.SerialDevicePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FMountSerialPanel extends SerialDevicePanel {
    final Platform platform;

    public FMountSerialPanel(final FMountCircuit fMountCircuit, EmulatorUI ui) {
        super();
        this.platform = ui.getFramework().getPlatform(Constants.CHIP_TX);
        JTabbedPane tabbedPane = new JTabbedPane();

        // Hex editor
        JPanel editorPanel = new JPanel(new BorderLayout());
        JPanel selectionPanel = new JPanel();

        JToggleButton loadButton = new JToggleButton("Plugin...",(fMountCircuit.getLensPlugin()==null ? false : true));
        loadButton.setToolTipText("Plugin lens into F-Mount");
        selectionPanel.add(loadButton);
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((JToggleButton)e.getSource()).isSelected()) {
                    fMountCircuit.setLensPlugin(new LensPrototype(fMountCircuit, platform.getMasterClock()));
                } else {
                    fMountCircuit.setLensPlugin(null);
                }
            }
        });

        editorPanel.add(selectionPanel, BorderLayout.NORTH);


        tabbedPane.addTab("Contents", editorPanel);


        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

    }

    public void dispose() {
    }
}
