package com.nikonhacker.gui.component.ioPort;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.*;
import com.nikonhacker.emu.peripherials.ioPort.util.FixedSourceComponent;
import com.nikonhacker.emu.peripherials.ioPort.util.FixedSourcePin;
import com.nikonhacker.emu.peripherials.ioPort.util.ForwardingPin;
import com.nikonhacker.emu.peripherials.ioPort.util.ValueChangeListenerIoWire;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.util.DynamicMatteBorder;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IoPortsFrame extends DocumentFrame implements IoPortConfigListener {
    // Size for all components
    private static final Dimension PREFERRED_SIZE    = new Dimension(55, 30);
    private static final int       COLOR_BORDER_SIZE = 2;

    private final IoPort[] ioPorts;

    private       JLabel[][]                    labels;
    private       JPanel[][]                    pinCells;
    private final JComponent[][]                pinInputs;
    private final JComponent[][]                pinOutputs;
    private       ValueChangeListenerIoWire[][] spyWires;


    public IoPortsFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final int chip, final EmulatorUI ui, final IoPort[] ioPorts) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.ioPorts = ioPorts;

        JTabbedPane tabbedPane = new JTabbedPane();

        // Labels from left to right (from bit 7 to 0)
        labels = new JLabel[ioPorts.length][8];

        // Pins from left to right (from bit 7 to 0)
        pinCells = new JPanel[ioPorts.length][8];
        pinInputs = new JComponent[ioPorts.length][8];
        pinOutputs = new JComponent[ioPorts.length][8];

        // Listeners
        spyWires = new ValueChangeListenerIoWire[ioPorts.length][8];

        JPanel configPanel = new JPanel(new MigLayout("insets 0", "[left][center, grow][center, grow][center, grow][center, grow][center, grow][center, grow][center, grow][center, grow]"));
        JPanel valuePanel = new JPanel(new MigLayout("insets 0", "[left][center, grow][center, grow][center, grow][center, grow][center, grow][center, grow][center, grow][center, grow]"));

        // Header line - bit numbers
        configPanel.add(new JLabel("bit #"), "right");
        valuePanel.add(new JLabel("bit #"), "right");
        for (int bitNumber = 7; bitNumber >= 0; bitNumber--) {
            configPanel.add(new JLabel(String.valueOf(bitNumber)), (bitNumber == 0 ? "wrap" : ""));
            valuePanel.add(new JLabel(String.valueOf(bitNumber)), (bitNumber == 0 ? "wrap" : ""));
        }

        FlowLayout noMarginLayout = new FlowLayout();
        noMarginLayout.setHgap(0);
        noMarginLayout.setVgap(0);

        // Preparation.

        // Iterate on lines.
        for (int portNumber = 0; portNumber < ioPorts.length; portNumber++) {

            // Print port label on the left
            configPanel.add(new JLabel(ioPorts[portNumber].toString()));
            valuePanel.add(new JLabel(ioPorts[portNumber].toString()));

            // Iterate on pins
            // Loop from left to right (so from bit 7 to 0)
            for (int bitNumber = 7; bitNumber >= 0; bitNumber--) {
                // Get the corresponding pin
                final VariableFunctionPin pin = ioPorts[portNumber].getPin(bitNumber);

                // Prepare a border that will indicate the reflect value and will be used around all components representing that pin (shared)
                DynamicMatteBorder pinBorder = new DynamicMatteBorder(COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE);

                // Prepare label for pin function name (config panel)
                JLabel label = createNameLabel(pinBorder);
                configPanel.add(label, bitNumber > 0 ? "grow" : "grow, wrap");
                labels[portNumber][7 - bitNumber] = label;

                // Prepare config cell container
                JPanel pinCell = new JPanel(new GridLayout());
                valuePanel.add(pinCell, (bitNumber > 0 ? "grow" : "grow, wrap"));
                pinCells[portNumber][7 - bitNumber] = pinCell;

                // Prepare components for pin inputs
                pinInputs[portNumber][7 - bitNumber] = createInputComboBox(portNumber, bitNumber, pin, pinBorder);

                // Prepare components for pin output
                pinOutputs[portNumber][7 - bitNumber] = createOutputLabel(pinBorder);

                // Insert a spy wire next to each pin
                spyWires[portNumber][bitNumber] = createAndInsertSpyWire(portNumber, bitNumber, pin);
            }
            refreshComponents(portNumber);
        }

        // Footer line - legend (avoid "key" as there are pin KEYs)
        addLegend(configPanel);
        addLegend(valuePanel);

        tabbedPane.addTab("Configuration", configPanel);
        tabbedPane.addTab("Values", valuePanel);

        getContentPane().add(tabbedPane);
    }

    private void addLegend(JPanel panel) {
        JSeparator separator = new JSeparator();
        panel.add(separator, "span 9, gapleft rel, growx, wrap");
        panel.add(new JLabel("legend"), "right");
        JLabel label;
        label = new JLabel("", SwingConstants.CENTER);
        panel.add(label, "span 2");
        label = new JLabel(Constants.LABEL_HI, SwingConstants.CENTER);
        label.setPreferredSize(PREFERRED_SIZE);
        label.setBorder(new MatteBorder(COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, Constants.COLOR_HI));
        panel.add(label);
        label = new JLabel(Constants.LABEL_LO, SwingConstants.CENTER);
        label.setPreferredSize(PREFERRED_SIZE);
        label.setBorder(new MatteBorder(COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, Constants.COLOR_LO));
        panel.add(label);
        label = new JLabel("Hi-Z", SwingConstants.CENTER);
        label.setPreferredSize(PREFERRED_SIZE);
        label.setBorder(new MatteBorder(COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, Constants.COLOR_DANGLING));
        panel.add(label);
        label = new JLabel("Disabled", SwingConstants.CENTER);
        label.setPreferredSize(PREFERRED_SIZE);
        label.setBorder(new MatteBorder(COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, Constants.COLOR_DISABLED));
        panel.add(label, "wrap");
    }

    private JLabel createNameLabel(DynamicMatteBorder pinBorder) {
        JLabel label = new JLabel();
        label.setPreferredSize(PREFERRED_SIZE);
        label.setBorder(pinBorder);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JComboBox createInputComboBox(final int portNumber, final int bitNumber, final VariableFunctionPin pin, DynamicMatteBorder pinBorder) {
        final JComboBox comboBox = new JComboBox();
        comboBox.addItem(Constants.LABEL_HI);
        comboBox.addItem(Constants.LABEL_HIZ);
        comboBox.addItem(Constants.LABEL_LO);
        if (pin.getConnectedPin() instanceof FixedSourcePin) {
            switch (pin.getConnectedPin().getOutputValue())  {
                case 0:
                    comboBox.setSelectedIndex(2);
                    break;
                case 1:
                    comboBox.setSelectedIndex(0);
                    break;
                default:
                    comboBox.setSelectedIndex(1);
            }
        }
        else {
            comboBox.setSelectedIndex(1);
        }
        comboBox.setBorder(pinBorder);
        comboBox.setPreferredSize(PREFERRED_SIZE);
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Pin otherSideOfSpyWirePin = spyWires[portNumber][bitNumber].getPin2();
                // 1. remove fixed source component, if any
                if (otherSideOfSpyWirePin.getConnectedPin() instanceof FixedSourcePin) {
                    ((FixedSourcePin) (otherSideOfSpyWirePin.getConnectedPin())).getComponent().remove();
                }
                ui.getPrefs().removePortInputValueOverride(chip, portNumber, bitNumber);
                // 2. reinsert new fixed source, if requested
                switch (comboBox.getSelectedIndex()) {
                    case 0:
                        // VCC
                        new FixedSourceComponent(1, "Fixed " + Constants.LABEL_HI + " for " + pin.getName()).insertAtPin(otherSideOfSpyWirePin);
                        ui.getPrefs().setPortInputValueOverride(chip, portNumber, bitNumber, 1);
                        break;
                    case 2:
                        // GND
                        new FixedSourceComponent(0, "Fixed " + Constants.LABEL_LO + " for " + pin.getName()).insertAtPin(otherSideOfSpyWirePin);
                        ui.getPrefs().setPortInputValueOverride(chip, portNumber, bitNumber, 0);
                        break;
                }
                updatePinState(portNumber, bitNumber, pin);
            }
        });
        return comboBox;
    }

    private JComponent createOutputLabel(DynamicMatteBorder pinBorder) {
        JComponent label1 = new JLabel("", SwingConstants.CENTER);
        label1.setBorder(pinBorder);
        label1.setPreferredSize(PREFERRED_SIZE);
        return label1;
    }

    private ValueChangeListenerIoWire createAndInsertSpyWire(final int finalPortNumber, final int finalBitNumber, final VariableFunctionPin pin) {
        ValueChangeListenerIoWire spyWire = new ValueChangeListenerIoWire(pin.getName() + "SPY", new IoPortValueChangeListener() {
            @Override
            public void onValueChange(int newValue) {
                updatePinState(finalPortNumber, finalBitNumber, pin);
            }
        });
        spyWire.insertAtPin(pin);
        return spyWire;
    }


    private void refreshComponents(int portNumber) {
        // Loop from left to right (from bit 7 to 0)
        for (int bitNumber = 7; bitNumber >= 0; bitNumber--) {
            VariableFunctionPin pin = ioPorts[portNumber].getPin(bitNumber);

            // Config labels
            // Set symbol as text
            labels[portNumber][7 - bitNumber].setText(pin.getFunctionShortName());
            // Set description as tooltip
            labels[portNumber][7 - bitNumber].setToolTipText(pin.getFunctionFullName());

            updatePinState(portNumber, bitNumber, pin);
        }
        revalidate();
        pack();
    }

    private void updatePinState(int portNumber, int bitNumber, VariableFunctionPin pin) {
        // State inputs/outputs
        JComponent pinComp;
        Color color;
        if (pin.isInput()) {
            // pin is configured as input
            if (pin.isInputEnabled()) {
                // input is enabled
                pinComp = pinInputs[portNumber][7 - bitNumber];
                if (pin.getInputValue() == null) {
                    color = Constants.COLOR_DANGLING;
                }
                else if (pin.getInputValue() == 0) {
                    color = Constants.COLOR_LO;
                }
                else {
                    color = Constants.COLOR_HI;
                }
            }
            else {
                // input is disabled
                color = Constants.COLOR_DISABLED;
                pinComp = null;
            }
        }
        else {
            // pin is configured as output
            pinComp = pinOutputs[portNumber][7 - bitNumber];
            if (pin.getOutputValue() == null) {
                if (IoPort.DEBUG) System.err.println("OutputValue is null for pin " + pin.getName());
                ((JLabel)pinComp).setText(Constants.LABEL_HIZ);
                color = Constants.COLOR_DANGLING;
            }
            else if (pin.getOutputValue() == 0) {
                ((JLabel)pinComp).setText(Constants.LABEL_LO);
                color = Constants.COLOR_LO;
            }
            else {
                ((JLabel)pinComp).setText(Constants.LABEL_HI);
                color = Constants.COLOR_HI;
            }
        }

        pinCells[portNumber][7 - bitNumber].removeAll();

        String toolTipText = computeTooltip(pin);

        if (pinComp != null) {
            pinCells[portNumber][7 - bitNumber].add(pinComp);
            // Put the tooltip and bgColor on the component
            pinComp.setToolTipText(toolTipText);
            ((DynamicMatteBorder)(pinComp.getBorder())).setColor(color);
        }
        else {
            // Put the tooltip on the blank panel
            pinCells[portNumber][7 - bitNumber].setToolTipText(toolTipText);
        }
    }

    /*
     *  Compute tooltip as a chain describing pins connected to this one in series
     */
    private String computeTooltip(VariableFunctionPin pin) {
        // Html to allow multi-line
        String toolTipText = "<html>" + pin.getFunctionFullName();
        Pin currentPin = pin.getConnectedPin();
        while (currentPin != null) {
            toolTipText += " &lt;-&gt; " + currentPin.getName();
            if (currentPin instanceof ForwardingPin) {
                // Pin is part of a two-pin component. See what's on the other side
                Pin targetPin = ((ForwardingPin)currentPin).getTargetPin();
                toolTipText += "<br/>" + targetPin.getName();
                // And go on with the component connected to that other side
                currentPin = targetPin.getConnectedPin();
                if (currentPin == null) {
                    toolTipText += " (NC)";
                }
            }
            else {
                currentPin = null;
            }
        }
        toolTipText += "</html>";
        return toolTipText;
    }


    @Override
    public void onConfigChange(int portNumber) {
        refreshComponents(portNumber);
    }

    @Override
    public void dispose() {
        super.dispose();

        // Un-hook the loggers
        for (int portNumber = 0; portNumber < ioPorts.length; portNumber++) {
            for (int bitNumber = 0; bitNumber < 8; bitNumber++) {
                spyWires[portNumber][bitNumber].remove();
            }
        }
    }

}
