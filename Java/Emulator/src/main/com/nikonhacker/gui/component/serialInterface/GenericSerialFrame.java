package com.nikonhacker.gui.component.serialInterface;

import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This component gives access to emulated Serial Interfaces
 * @see com.nikonhacker.emu.peripherials.serialInterface.SerialInterface
 */
public class GenericSerialFrame extends DocumentFrame {

    private List <SerialDevicePanel> serialDevicePanels = new ArrayList<>();

    public GenericSerialFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, final EmulatorUI ui, final List<SerialDevice> serialDevices) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);

        JTabbedPane tabbedPane = new JTabbedPane();

        for (final SerialDevice serialDevice : serialDevices) {
            SerialDevicePanel serialDevicePanel = SerialPanelFactory.getSerialDevicePanel(serialDevice);
            serialDevicePanels.add(serialDevicePanel);
            tabbedPane.addTab(serialDevice.toString(), null, serialDevicePanel);
        }

        // Add tab panel
        getContentPane().add(tabbedPane);
    }

    @Override
    public void dispose() {
        super.dispose();

        // Give a chance to un-hook the loggers
        for (SerialDevicePanel serialDevicePanel : serialDevicePanels) {
            serialDevicePanel.dispose();
        }
    }
}
