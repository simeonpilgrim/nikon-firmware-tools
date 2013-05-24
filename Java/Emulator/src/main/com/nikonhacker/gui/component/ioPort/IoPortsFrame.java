package com.nikonhacker.gui.component.ioPort;

import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.IoPortsListener;
import com.nikonhacker.emu.peripherials.ioPort.tx.TxIoPort;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import eu.hansolo.custom.SteelCheckBox;
import eu.hansolo.steelseries.extras.Led;
import eu.hansolo.steelseries.tools.LedColor;
import eu.hansolo.steelseries.tools.LedType;
import eu.hansolo.tools.ColorDef;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IoPortsFrame extends DocumentFrame implements IoPortsListener {
    private final IoPort[] ioPorts;

    private JLabel[][] labels;
    private JPanel[][] cells;
    private JCheckBox[][] inputs;
    private JComponent[][] outputs;

    public IoPortsFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, final IoPort[] ioPorts) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.ioPorts = ioPorts;

        JTabbedPane tabbedPane = new JTabbedPane();

        // Labels from left to right (from bit 7 to 0)
        labels = new JLabel[ioPorts.length][8];
        // Components from left to right (from bit 7 to 0)
        cells = new JPanel[ioPorts.length][8];
        inputs = new JCheckBox[ioPorts.length][8];
        outputs = new JComponent[ioPorts.length][8];

        JPanel configPanel = new JPanel(new MigLayout("insets 0", "[left][center, grow][center, grow][center, grow][center, grow][center, grow][center, grow][center, grow][center, grow]"));
        JPanel statePanel = new JPanel(new MigLayout("insets 0", "[left][center, grow][center, grow][center, grow][center, grow][center, grow][center, grow][center, grow][center, grow]"));
        configPanel.add(new JLabel("bit #"), "right");
        statePanel.add(new JLabel("bit #"), "right");
        // Header line - bit numbers
        for (int bitNumber = 7; bitNumber >= 0; bitNumber--) {
            configPanel.add(new JLabel(String.valueOf(bitNumber)), (bitNumber == 0 ? "wrap" : ""));
            statePanel.add(new JLabel(String.valueOf(bitNumber)), (bitNumber == 0 ? "wrap" : ""));
        }

        FlowLayout noMarginLayout = new FlowLayout();
        noMarginLayout.setHgap(0);
        noMarginLayout.setVgap(0);

        // Port lines - label and bits
        for (int portNumber = 0; portNumber < ioPorts.length; portNumber++) {
            // Label
            configPanel.add(new JLabel(ioPorts[portNumber].toString()));
            statePanel.add(new JLabel(ioPorts[portNumber].toString()));
            // Bits
            // Loop from left to right (from bit 7 to 0)
            for (int bitNumber = 7; bitNumber >= 0; bitNumber--) {
                final int finalPortNumber = portNumber;
                // Prepare config labels
                JLabel label = new JLabel();
                label.setPreferredSize(new Dimension(30, 30));
                labels[portNumber][7 - bitNumber] = label;
                configPanel.add(label, (bitNumber > 0 ? "" : "wrap"));

                // Prepare state inputs
                JCheckBox checkBox;
                if (ui.getPrefs().isUsePrettyIoComponents()) {
                    // Steel switch for input
                    checkBox = new SteelCheckBox(SwingConstants.VERTICAL);
                    ((SteelCheckBox)checkBox).setColored(true);
                    ((SteelCheckBox)checkBox).setSelectedColor(ColorDef.GREEN);
                }
                else {
                    // Plain checkbox for input
                    checkBox = new JCheckBox();
                }
                checkBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        transmitInputValue(finalPortNumber);
                    }
                });
                //checkBox.setVisible(false);
                inputs[portNumber][7 - bitNumber] = checkBox;

                // Prepare state ouputs
                JComponent outputComponent;
                if (ui.getPrefs().isUsePrettyIoComponents()) {
                    // Led for output
                    outputComponent = new Led();
                    ((Led)outputComponent).setLedColor(LedColor.YELLOW_LED);
                    ((Led)outputComponent).setLedType(LedType.ROUND);
                    outputComponent.setPreferredSize(new Dimension(30, 30));
                }
                else {
                    // Plain radio for output
                    outputComponent = new JRadioButton();
                    outputComponent.setEnabled(false);
                }
                outputs[portNumber][7 - bitNumber] = outputComponent;

                // Lay out cell container
                JPanel cell = new JPanel(noMarginLayout);
                cell.setPreferredSize(new Dimension(30, 30));
                cells[portNumber][7 - bitNumber] = cell;
                statePanel.add(cell, (bitNumber > 0 ? "" : "wrap"));
            }
            refreshComponents(portNumber);
        }
        tabbedPane.addTab("Configuration", configPanel);
        tabbedPane.addTab("State", statePanel);

        getContentPane().add(tabbedPane);
    }

    private void refreshComponents(int portNumber) {
        int config = ((TxIoPort)ioPorts[portNumber]).getControlRegister();
        int ie = ((TxIoPort)ioPorts[portNumber]).getInputEnableControlRegister();
        // Loop from left to right (from bit 7 to 0)
        for (int bitNumber = 7; bitNumber >= 0; bitNumber--) {
            String pinDescription = ioPorts[portNumber].getPin(bitNumber).getFunctionFullName();

            // Set symbol as text
            labels[portNumber][7 - bitNumber].setText(ioPorts[portNumber].getPin(bitNumber).getFunctionShortName());
            // Set description as tooltip
            labels[portNumber][7 - bitNumber].setToolTipText(pinDescription);

            JComponent comp;
            if ((config & (1<< bitNumber)) == 0) {
                // pin is configured as input
                if ((ie & (1<< bitNumber)) == 0) {
                    // input is disabled
                    comp = null;
                }
                else {
                    comp = inputs[portNumber][7 - bitNumber];
                }
            }
            else {
                // pin is configured as output
                comp = outputs[portNumber][7 - bitNumber];
            }
            cells[portNumber][7 - bitNumber].removeAll();
            if (comp != null) {
                cells[portNumber][7 - bitNumber].add(comp);
                // Put the tooltip on the component
                comp.setToolTipText(pinDescription);
            }
            else {
                // Put the tooltip on the blank panel
                cells[portNumber][7 - bitNumber].setToolTipText(pinDescription);
            }
            refreshOutputValues(portNumber);
        }
        revalidate();
        pack();
    }

    private void transmitInputValue(int portNumber) {
        byte value = 0;
        for (int bitNumber = 7; bitNumber >= 0; bitNumber--) {
            value |= inputs[portNumber][7 - bitNumber].isSelected()?(1<< bitNumber):0;
        }
        ((TxIoPort)ioPorts[portNumber]).setExternalValue(value);
    }

    private void refreshOutputValues(int portNumber) {
        int value = ((TxIoPort)ioPorts[portNumber]).getValue();
        for (int bitNumber = 7; bitNumber >= 0; bitNumber--) {
            JComponent component = outputs[portNumber][7 - bitNumber];
            if (component instanceof Led) {
                ((Led)component).setLedOn((value & (1 << bitNumber)) != 0);
            }
            else {
                ((JRadioButton)component).setSelected((value & (1 << bitNumber)) != 0);
            }
            inputs[portNumber][7 - bitNumber].setSelected((value & (1<< bitNumber)) != 0) ;
        }
    }

    @Override
    public void onConfigChange(int portNumber, byte config, byte inputEnable) {
        refreshComponents(portNumber);
    }

    @Override
    public void onOutputValueChange(int portNumber, byte newValue) {
        refreshOutputValues(portNumber);
    }
}
