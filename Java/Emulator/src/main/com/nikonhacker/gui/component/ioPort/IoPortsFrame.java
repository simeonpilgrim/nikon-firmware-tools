package com.nikonhacker.gui.component.ioPort;

import com.nikonhacker.emu.peripherials.ioPort.IoPort;
import com.nikonhacker.emu.peripherials.ioPort.IoPortListener;
import com.nikonhacker.emu.peripherials.ioPort.TxIoPort;
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

public class IoPortsFrame extends DocumentFrame implements IoPortListener {
    protected final IoPort[] ioPorts;

    protected JPanel[][] cells;
    protected SteelCheckBox[][] inputs;
    protected Led[][] outputs;

    public IoPortsFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, EmulatorUI ui, final IoPort[] ioPorts) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.ioPorts = ioPorts;

        // Components from left to right (from bit 7 to 0)
        cells = new JPanel[ioPorts.length][8];
        inputs = new SteelCheckBox[ioPorts.length][8];
        outputs = new Led[ioPorts.length][8];

        JPanel panel = new JPanel(new MigLayout("insets 0", "[left][center][center][center][center][center][center][center][center]"));
        // Header line - bit numbers
        for (int bitNumber = 7; bitNumber >= 0; bitNumber--) {
            panel.add(new JLabel(String.valueOf(bitNumber)), (bitNumber == 7 ? "skip 1" : "") + (bitNumber == 0 ? "wrap" : ""));
        }

        FlowLayout noMarginLayout = new FlowLayout();
        noMarginLayout.setHgap(0);
        noMarginLayout.setVgap(0);

        // Port lines - label and bits
        for (int portNumber = 0; portNumber < ioPorts.length; portNumber++) {
            // Label
            panel.add(new JLabel(ioPorts[portNumber].toString()));
            // Bits
            // Loop from left to right (from bit 7 to 0)
            for (int bitNumber = 7; bitNumber >= 0; bitNumber--) {
                // Prepare switch for input
                SteelCheckBox checkBox = new SteelCheckBox(SwingConstants.VERTICAL);
                checkBox.setColored(true);
                checkBox.setSelectedColor(ColorDef.GREEN);
                final int finalPortNumber = portNumber;
                checkBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        transmitInputValue(finalPortNumber);
                    }
                });
                //checkBox.setVisible(false);
                inputs[portNumber][7 - bitNumber] = checkBox;

                // Prepare led for output
                Led led = new Led();
                led.setLedColor(LedColor.YELLOW_LED);
                led.setLedType(LedType.ROUND);
                led.setPreferredSize(new Dimension(30, 30));
                outputs[portNumber][7 - bitNumber] = led;

                // Lay out cell container
                JPanel cell = new JPanel(noMarginLayout);
                cell.setPreferredSize(new Dimension(30, 30));
                cells[portNumber][7 - bitNumber] = cell;
                panel.add(cell, (bitNumber > 0 ? "" : "wrap"));
            }
            refreshComponents(portNumber);
        }
        getContentPane().add(panel);
    }

    private void refreshComponents(int portNumber) {
        int config = ((TxIoPort)ioPorts[portNumber]).getControlRegister();
        // Loop from left to right (from bit 7 to 0)
        for (int bitNumber = 7; bitNumber >= 0; bitNumber--) {
            JComponent comp;
            if ((config & (1<< bitNumber)) == 0) {
                // pin is configured as input
                comp = inputs[portNumber][7 - bitNumber];
            }
            else {
                // pin is configured as output
                comp = outputs[portNumber][7 - bitNumber];
            }
            cells[portNumber][7 - bitNumber].removeAll();
            cells[portNumber][7 - bitNumber].add(comp);
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
            outputs[portNumber][7 - bitNumber].setLedOn((value & (1<< bitNumber)) != 0);
            inputs[portNumber][7 - bitNumber].setSelected((value & (1<< bitNumber)) != 0) ;
        }
    }

    @Override
    public void onConfigChange(int portNumber, byte config) {
        refreshComponents(portNumber);
    }

    @Override
    public void onOutputValueChange(int portNumber, byte newValue) {
        refreshOutputValues(portNumber);
    }
}
