package com.nikonhacker.gui.component.serialInterface;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;
import com.nikonhacker.emu.peripherials.serialInterface.util.PrintWriterLoggerSerialWire;
import com.nikonhacker.emu.peripherials.serialInterface.util.SerialWire;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.swing.DocumentFrame;
import com.nikonhacker.gui.swing.JValueButton;
import com.nikonhacker.gui.swing.PrintWriterArea;
import com.nikonhacker.gui.swing.VerticalLayout;
import org.apache.commons.lang3.StringUtils;

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

    private final Insets buttonInsets = new Insets(1, 1, 1, 1);
    private SerialInterface[] serialInterfaces;
    private final JTabbedPane tabbedPane;

    public SerialInterfaceFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, final EmulatorUI ui, final SerialInterface[] serialInterfaces) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.serialInterfaces = serialInterfaces;

        tabbedPane = new JTabbedPane();

        if (ui.getPrefs().isLogSerialMessages(chip))
            System.out.println("================ Connecting " + Constants.CHIP_LABEL[chip] + " serial interface loggers... ================");
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

            if (ui.getPrefs().isLogSerialMessages(chip)) System.out.println("Before CONN: " + formatSerialChain(serialInterface));

            // The logic is to insert two loggers (Rx and Tx) between the serialInterface and the device connected to it.
            // To do so:
            // 1. we insert a wire between this serialInterface and this serialInterface's former target
            serialInterface.connectTargetDevice(new PrintWriterLoggerSerialWire("Tx of " + serialInterface.getName(), serialInterface.getTargetDevice(), txTextArea.getPrintWriter()));
            // 1. we insert another wire between this serialInterface's former source and this serialInterface
            serialInterface.getSourceDevice().connectTargetDevice(new PrintWriterLoggerSerialWire("Rx of " + serialInterface.getName(), serialInterface, rxTextArea.getPrintWriter()));

            if (ui.getPrefs().isLogSerialMessages(chip)) System.out.println("After  CONN: " + formatSerialChain(serialInterface));

            prepareButtonGrid(buttonGrid, valueButtonListener, serialInterface.getNumBits());
            serialInterfacePanel.add(new JLabel("Click to send data to SerialInterface:"));
            serialInterfacePanel.add(buttonGrid);
            serialInterfacePanel.add(new JLabel("Microcontroller => External device"));
            serialInterfacePanel.add(new JScrollPane(txTextArea));
            serialInterfacePanel.add(new JLabel("External device => Microcontroller"));
            serialInterfacePanel.add(new JScrollPane(rxTextArea));

            tabbedPane.addTab(serialInterface.getName(), null, serialInterfacePanel);
        }

        tabbedPane.setSelectedIndex(ui.getPrefs().getSerialInterfaceFrameSelectedTab(chip));

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
        if (ui.getPrefs().isLogSerialMessages(chip)) System.out.println("============= Disconnecting " + Constants.CHIP_LABEL[chip] + " serial interface loggers... ================");
        for (SerialInterface serialInterface : serialInterfaces) {
            // Remove the Tx wire and reconnect the real target
            if (ui.getPrefs().isLogSerialMessages(chip)) System.out.println("Before DISC: " + formatSerialChain(serialInterface));
            ((SerialWire)serialInterface.getTargetDevice()).remove();
            // Remove the Rx wire and reconnect the real source
            ((SerialWire)serialInterface.getSourceDevice()).remove();
            if (ui.getPrefs().isLogSerialMessages(chip)) System.out.println("After  DISC: " + formatSerialChain(serialInterface));
        }

        // Remember the selected Index
        ui.getPrefs().setSerialInterfaceFrameSelectedTab(chip, tabbedPane.getSelectedIndex());
    }

    private String formatSerialChain(SerialInterface serialInterface) {
        String result = StringUtils.center(serialInterface.toString(), 22);

        SerialDevice source = serialInterface;
        do {
            source = source.getSourceDevice();
            result = StringUtils.center(source.toString(), 22) + " S> " + result;
        }
        while (!(source instanceof SerialInterface));

        result = StringUtils.leftPad(result, 130);

        SerialDevice target = serialInterface;
        do {
            target = target.getTargetDevice();
            result = result + " T> " + StringUtils.center(target.toString(), 22);
        }
        while (!(target instanceof SerialInterface));

        return Constants.CHIP_LABEL[chip] + "#" + serialInterface.getSerialInterfaceNumber() + ": " + result;
    }
}
