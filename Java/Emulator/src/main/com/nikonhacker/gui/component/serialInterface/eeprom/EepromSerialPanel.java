package com.nikonhacker.gui.component.serialInterface.eeprom;

import com.nikonhacker.emu.peripherials.serialInterface.eeprom.St950x0;
import com.nikonhacker.gui.component.serialInterface.RxTxSerialPanel;
import com.nikonhacker.gui.component.serialInterface.SerialDevicePanel;

import javax.swing.*;

/**
 * This file is part of NikonEmulator, a NikonHacker.com project.
 */
public class EepromSerialPanel extends SerialDevicePanel {

    private final RxTxSerialPanel rxTxSerialPanel;

    public EepromSerialPanel(St950x0 eeprom) {
        super();

        JTabbedPane tabbedPane = new JTabbedPane();
        rxTxSerialPanel = new RxTxSerialPanel(eeprom);
        tabbedPane.add("Rx/Tx interface", rxTxSerialPanel);
        JPanel eepromContentsPanel = new JPanel();
        tabbedPane.add("Contents", eepromContentsPanel);
        add(tabbedPane);
    }

    public void dispose() {
        rxTxSerialPanel.dispose();
    }
}
