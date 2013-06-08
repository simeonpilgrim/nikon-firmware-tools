package com.nikonhacker.gui.component.serialInterface;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.util.PrintWriterLoggerSerialWire;
import com.nikonhacker.emu.peripherials.serialInterface.util.SerialWire;
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
 * This should share code (subclass ?) GenericSerialFrame or reuse its factory logic
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
            // Set autoscroll
            rxTextArea.setAutoScroll(true);
            txTextArea.setAutoScroll(true);


            final ActionListener valueButtonListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JValueButton button = (JValueButton) e.getSource();
                    int value = button.getValue();
                    send(serialInterface, rxTextArea, value);
                }
            };

            // The logic is to insert two loggers (Rx and Tx) between the serialInterface and the device connected to it.
            // To do so:
            // 1. we insert a wire between this serialInterface and this serialInterface's former target
            serialInterface.connectTargetDevice(new PrintWriterLoggerSerialWire("Tx of " + serialInterface.getName(), serialInterface.getTargetDevice(), txTextArea.getPrintWriter()));
            // 1. we insert another wire between this serialInterface's former source and this serialInterface
            serialInterface.getSourceDevice().connectTargetDevice(new PrintWriterLoggerSerialWire("Rx of " + serialInterface.getName(), serialInterface, rxTextArea.getPrintWriter()));

            prepareButtonGrid(buttonGrid, valueButtonListener, serialInterface.getNumBits());

            serialInterfacePanel.add(new JLabel("Click to send data to SerialInterface:"));
            serialInterfacePanel.add(buttonGrid);
            serialInterfacePanel.add(new JLabel("Microcontroller => External device"));
            serialInterfacePanel.add(new JScrollPane(txTextArea));
            serialInterfacePanel.add(new JLabel("External device => Microcontroller"));
            serialInterfacePanel.add(new JScrollPane(rxTextArea));

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

        // Reconnect the devices directly, removing the logging wires
        for (SerialInterface serialInterface : serialInterfaces) {
            // Remove the Tx wire and reconnect the real target
            ((SerialWire)serialInterface.getTargetDevice()).remove();

            // Remove the Rx wire and reconnect the real source
            ((SerialWire)serialInterface.getSourceDevice()).remove();
        }
    }
}
