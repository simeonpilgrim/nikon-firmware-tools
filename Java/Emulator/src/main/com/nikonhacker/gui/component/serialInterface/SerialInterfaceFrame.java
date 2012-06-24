package com.nikonhacker.gui.component.serialInterface;

import com.nikonhacker.Format;
import com.nikonhacker.emu.peripherials.serialInterface.NullSerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialDevice;
import com.nikonhacker.emu.peripherials.serialInterface.SerialInterface;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
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

    public SerialInterfaceFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final SerialInterface[] serialInterfaces, final EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.serialInterfaces = serialInterfaces;

        JTabbedPane tabbedPane = new JTabbedPane();

        for (final SerialInterface serialInterface : serialInterfaces) {
            JPanel serialInterfacePanel = new JPanel(new VerticalLayout());

            final JTextArea txTextArea = new JTextArea(3, 50);
            final JTextArea rxTextArea = new JTextArea(3, 50);
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
                    serialInterface.write(value);
                    txTextArea.append(Format.asHex(value, 2) + " ");
                }
            };

            serialInterface.connect(new SerialDevice() {
                public void onValueReady(SerialInterface serialInterface) {
                    rxTextArea.append(Format.asHex(serialInterface.read(), 2) + " ");
                }

                public void onBitNumberChange(SerialInterface serialInterface, int nbBits) {
                    prepareButtonGrid(buttonGrid, valueButtonListener, nbBits);
                }
            });

            prepareButtonGrid(buttonGrid, valueButtonListener, serialInterface.getNbBits());

            serialInterfacePanel.add(new JLabel("Click to send data to FR SerialInterface:"));
            serialInterfacePanel.add(buttonGrid);
            serialInterfacePanel.add(new JLabel("External device => FR MCU"));
            serialInterfacePanel.add(new JScrollPane(txTextArea));
            serialInterfacePanel.add(new JLabel("FR MCU => External device"));
            serialInterfacePanel.add(new JScrollPane(rxTextArea));

            tabbedPane.addTab("Serial #" + serialInterface.getSerialInterfaceNumber(), null, serialInterfacePanel);
        }

        // Add tab panel

        getContentPane().add(tabbedPane);
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
        // To make sure bytes written by the MCU are consumed
        for (SerialInterface serialInterface : serialInterfaces) {
            serialInterface.connect(new NullSerialDevice());
        }
    }
}
