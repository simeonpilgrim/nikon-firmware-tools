package com.nikonhacker.gui.component.serialInterface;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SpiSlaveDevice;
import com.nikonhacker.emu.peripherials.serialInterface.util.PrintWriterLoggerSerialWire;
import com.nikonhacker.emu.peripherials.serialInterface.util.PrintWriterLoggerSpiSlaveWire;
import com.nikonhacker.gui.component.PrintWriterArea;
import com.nikonhacker.gui.component.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This file is part of NikonEmulator, a NikonHacker.com project.
 */
public class RxTxSerialPanel extends SerialDevicePanel {
    private final Insets buttonInsets = new Insets(1,1,1,1);
    private SerialDevice serialDevice;

    public RxTxSerialPanel(final SerialDevice serialDevice) {
        super();
        this.serialDevice = serialDevice;
        this.setLayout(new VerticalLayout());

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
                send(serialDevice, txTextArea, value);
            }
        };

        // The logic is to insert two loggers (Rx and Tx) between the serialDevice and the device connected to it.
        // We assume we have a regular A <> B setup to start with, not a triangle or separate devices on Rx and Tx
        // To do so:
        // 1. we get the device originally connected to the serial port
        SerialDevice connectedSerialDevice = serialDevice.getTargetDevice();
        // 2. we replace the above device by a logger wire, forwarding data to the original device
        if (connectedSerialDevice instanceof SpiSlaveDevice) {
            serialDevice.connectTargetDevice(new PrintWriterLoggerSpiSlaveWire("Tx of " + serialDevice.toString(), (SpiSlaveDevice) connectedSerialDevice, rxTextArea.getPrintWriter()));
        }
        else {
            serialDevice.connectTargetDevice(new PrintWriterLoggerSerialWire("Tx of " + serialDevice.toString(), connectedSerialDevice, rxTextArea.getPrintWriter()));
        }
        // 3. conversely, we connect a similar logger wire in the other direction.
        if (serialDevice instanceof SpiSlaveDevice) {
            connectedSerialDevice.connectTargetDevice(new PrintWriterLoggerSpiSlaveWire("Rx of " + serialDevice.toString(), (SpiSlaveDevice) serialDevice, txTextArea.getPrintWriter()));
        }
        else {
            connectedSerialDevice.connectTargetDevice(new PrintWriterLoggerSerialWire("Rx of " + serialDevice.toString(), serialDevice, txTextArea.getPrintWriter()));
        }

        prepareButtonGrid(buttonGrid, valueButtonListener, 8);

        add(new JLabel("Click to send data to external device:"));
        add(buttonGrid);
        add(new JLabel("Received by external device"));
        add(new JScrollPane(txTextArea));
        add(new JLabel("Transmitted by external device"));
        add(new JScrollPane(rxTextArea));
    }

    private void send(SerialDevice serialDevice, JTextArea txTextArea, int value) {
        try {
            serialDevice.write(value);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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


    public void dispose() {
        // Find back the real device attached to the logging wire
        SerialDevice wire = serialDevice.getTargetDevice();
        SerialDevice realDevice = wire.getTargetDevice();
        // Reconnect the devices directly, removing the logging wires
        serialDevice.connectTargetDevice(realDevice);
        realDevice.connectTargetDevice(serialDevice);
    }


}
