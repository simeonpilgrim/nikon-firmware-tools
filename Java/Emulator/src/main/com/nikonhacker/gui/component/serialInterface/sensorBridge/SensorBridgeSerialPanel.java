package com.nikonhacker.gui.component.serialInterface.sensorBridge;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.sensorBridge.Ei155;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.serialInterface.RxTxSerialPanel;
import com.nikonhacker.gui.component.serialInterface.SerialDevicePanel;
import com.nikonhacker.Prefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SensorBridgeSerialPanel extends SerialDevicePanel {

    private final Ei155 sensorBridge;

    private Timer refreshTimer;
    private final JList registerList;

    public SensorBridgeSerialPanel(final Ei155 sensorBridge, EmulatorUI ui) {
        super();
        this.sensorBridge = sensorBridge;

        registerList = new JList();
        registerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        registerList.setLayoutOrientation(JList.VERTICAL);
        registerList.setVisibleRowCount(16);
        add(registerList, BorderLayout.CENTER);

        JScrollPane listScroller = new JScrollPane(registerList);
        add(listScroller, BorderLayout.CENTER);
        refreshData();

        // Start update timer
        refreshTimer = new Timer(ui.getPrefs().getRefreshIntervalMs(), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        refreshTimer.start();
    }

    private final void refreshData() {
        final int [] mem = sensorBridge.getMemory();
        final DefaultListModel model = new DefaultListModel();
        for (int i=0; i<mem.length; i++) {
            model.addElement("0x"+Format.asHex(i, 2) + " : " + Format.asHex(mem[i],6));
        }
        registerList.setModel(model);
    }

    public void dispose() {
        refreshTimer.stop();
        refreshTimer = null;
    }

}
