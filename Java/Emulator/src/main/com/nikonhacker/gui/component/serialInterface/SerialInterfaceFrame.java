package com.nikonhacker.gui.component.serialInterface;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.util.PrintWriterLoggerSerialWire;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.PrintWriterArea;
import com.nikonhacker.gui.component.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This component gives access to emulated Serial Interfaces
 * @see SerialInterface
 */
public class SerialInterfaceFrame extends DocumentFrame {

    private final Insets buttonInsets = new Insets(1,1,1,1);
    private SerialInterface[] serialInterfaces;

    public SerialInterfaceFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, final EmulatorUI ui, final SerialInterface[] serialInterfaces) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.serialInterfaces = serialInterfaces;

        JTabbedPane tabbedPane = new JTabbedPane();

        for (final SerialInterface serialInterface : serialInterfaces) {
            JPanel serialInterfacePanel = new JPanel(new VerticalLayout());

            final PrintWriterArea txTextArea = new PrintWriterArea(3, 50);
            final PrintWriterArea rxTextArea = new PrintWriterArea(3, 50);
            final JPanel buttonGrid = new JPanel();

            // Enable word-wrapping for textAreas
            txTextArea.setLineWrap(true);
            txTextArea.setWrapStyleWord(true);
            rxTextArea.setLineWrap(true);
            rxTextArea.setWrapStyleWord(true);

            final ActionListener valueButtonListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JValueButton button = (JValueButton) e.getSource();
                    int value = button.getValue();
                    send(serialInterface, txTextArea, value);
                }
            };

            // The logic is to insert two loggers (Rx and Tx) between the serialInterface and the device connected to it.
            // We assume we have a regular A <> B setup to start with, not a triangle or separate devices on Rx and Tx
            // To do so:
            // 1. we get the device originally connected to the serial port
            SerialDevice connectedSerialDevice = serialInterface.getConnectedSerialDevice();
            // 2. we replace the above device by a logger wire, forwarding data to the original device
            serialInterface.connectSerialDevice(new PrintWriterLoggerSerialWire("Rx of " + serialInterface.getName(), connectedSerialDevice, rxTextArea.getPrintWriter()));
            // 3. conversely, we connect a similar logger wire in the other direction.
            connectedSerialDevice.connectSerialDevice(new PrintWriterLoggerSerialWire("Tx of " + serialInterface.getName(), serialInterface, txTextArea.getPrintWriter()));

            prepareButtonGrid(buttonGrid, valueButtonListener, serialInterface.getNumBits());

            serialInterfacePanel.add(new JLabel("Click to send data to SerialInterface:"));
            serialInterfacePanel.add(buttonGrid);
            serialInterfacePanel.add(new JLabel("Microcontroller => External device"));
            serialInterfacePanel.add(new JScrollPane(rxTextArea));
            serialInterfacePanel.add(new JLabel("External device => Microcontroller"));
            serialInterfacePanel.add(new JScrollPane(txTextArea));

            tabbedPane.addTab(serialInterface.getName(), null, serialInterfacePanel);
        }

        // Add tab panel
        getContentPane().add(tabbedPane);
    }

    private void send(SerialInterface serialInterface, JTextArea txTextArea, int value) {
        serialInterface.write(value);
        txTextArea.append(Format.asHex(value, 2) + " ");
    }

    private JPanel prepareButtonGrid(JPanel buttonGrid, ActionListener valueButtonListener, int bits) {
        buttonGrid.removeAll();
        buttonGrid.setLayout(new GridLayout(0, 16));
        for (int value = 0; value < (1 << bits); value++) {
            JValueButton button = new JValueButton(value);
            button.setMargin(buttonInsets);
            button.addActionListener(valueButtonListener);
            buttonGrid.add(button);
        }
        return buttonGrid;
    }

    @Override
    public void dispose() {
        super.dispose();

        // Un-hook the loggers
        for (SerialInterface serialInterface : serialInterfaces) {
            // Find back the real device attached to the logging wire
            SerialDevice wire = serialInterface.getConnectedSerialDevice();
            SerialDevice realDevice = wire.getConnectedSerialDevice();
            // Reconnect the devices directly, removing the logging wires
            serialInterface.connectSerialDevice(realDevice);
            realDevice.connectSerialDevice(serialInterface);
        }
    }
}
