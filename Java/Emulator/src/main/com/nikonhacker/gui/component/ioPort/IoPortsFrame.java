package com.nikonhacker.gui.component.ioPort;

import com.nikonhacker.Constants;
import com.nikonhacker.emu.peripherials.ioPort.*;
import com.nikonhacker.emu.peripherials.ioPort.util.FixedSourceComponent;
import com.nikonhacker.emu.peripherials.ioPort.util.FixedSourcePin;
import com.nikonhacker.emu.peripherials.ioPort.util.ForwardingPin;
import com.nikonhacker.emu.peripherials.ioPort.util.ValueChangeListenerIoWire;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IoPortsFrame extends DocumentFrame implements IoPortConfigListener {
    // Size for all components
    private static final Dimension PREFERRED_SIZE    = new Dimension(55, 30);
    private static final int       COLOR_BORDER_SIZE = 2;
    private static final Border    BORDER_HI         = new MatteBorder(COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, Constants.COLOR_HI);
    private static final Border    BORDER_HIZ        = new MatteBorder(COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, Constants.COLOR_HIZ);
    private static final Border    BORDER_LO         = new MatteBorder(COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, Constants.COLOR_LO);

    private final IoPort[] ioPorts;

    private       JLabel[][]                    labels;
    private       JPanel[][]                    pinCells;
    private final JComboBox[][]                 pinInputs;
    private final JLabel[][]                    pinDisabledInputs;
    private final JLabel[][]                    pinOutputs;
    private       ValueChangeListenerIoWire[][] spyWires;


    public IoPortsFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final int chip, final EmulatorUI ui, final IoPort[] ioPorts) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.ioPorts = ioPorts;

        JTabbedPane tabbedPane = new JTabbedPane();

        // Labels from left to right (from bit 7 to 0)
        labels = new JLabel[ioPorts.length][8];

        // Pins from left to right (from bit 7 to 0)
        pinCells = new JPanel[ioPorts.length][8];
        pinInputs = new JComboBox[ioPorts.length][8];
        pinDisabledInputs = new JLabel[ioPorts.length][8];
        pinOutputs = new JLabel[ioPorts.length][8];

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

                // Prepare label for pin function name (config panel)
                JLabel label = createLabel();
                configPanel.add(label, bitNumber > 0 ? "grow" : "grow, wrap");
                labels[portNumber][7 - bitNumber] = label;

                // Prepare config cell container
                JPanel pinCell = new JPanel(new GridLayout());
                valuePanel.add(pinCell, (bitNumber > 0 ? "grow" : "grow, wrap"));
                pinCells[portNumber][7 - bitNumber] = pinCell;

                // Prepare components for pin inputs
                pinInputs[portNumber][7 - bitNumber] = createInputComboBox(portNumber, bitNumber, pin);
                pinDisabledInputs[portNumber][7 - bitNumber] = createLabel();
                pinDisabledInputs[portNumber][7 - bitNumber].setText("dis.");
                pinDisabledInputs[portNumber][7 - bitNumber].setForeground(Color.GRAY);

                // Prepare components for pin output
                pinOutputs[portNumber][7 - bitNumber] = createLabel();
                pinOutputs[portNumber][7 - bitNumber].setOpaque(true);
                pinOutputs[portNumber][7 - bitNumber].setForeground(Color.WHITE);

                // Insert a spy wire next to each pin
                spyWires[portNumber][bitNumber] = createAndInsertSpyWire(portNumber, bitNumber, pin);
            }
            refreshComponents(portNumber);
        }

        // Footer line - legend (avoid "key" as there are pin KEYs)
        addCommonLegend(configPanel, "hover to see details");
        addCommonLegend(valuePanel, "hover to see connections");
        JLabel label = new JLabel("OUT", SwingConstants.CENTER);
        label.setPreferredSize(PREFERRED_SIZE);
        label.setBackground(Color.BLACK);
        label.setOpaque(true);
        label.setForeground(Color.WHITE);
        label.setBorder(new MatteBorder(COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, Color.BLACK));
        valuePanel.add(label);
        label = new JLabel("IN", SwingConstants.CENTER);
        label.setPreferredSize(PREFERRED_SIZE);
        label.setBorder(new MatteBorder(COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, Color.BLACK));
        valuePanel.add(label);


        tabbedPane.addTab("Configuration", configPanel);
        tabbedPane.addTab("Values", valuePanel);

        getContentPane().add(tabbedPane);
    }

    private void addCommonLegend(JPanel panel, String detail) {
        JSeparator separator = new JSeparator();
        panel.add(separator, "span 9, gapleft rel, growx, wrap");
        panel.add(new JLabel("legend"), "right");
        JLabel label;
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
        label.setBorder(new MatteBorder(COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, COLOR_BORDER_SIZE, Constants.COLOR_HIZ));
        panel.add(label);
        label = new JLabel(detail, SwingConstants.CENTER);
        panel.add(label);
        panel.add(label, "span 3");
    }

    private JLabel createLabel() {
        JLabel label = new JLabel();
        label.setPreferredSize(PREFERRED_SIZE);
        label.setBorder(BORDER_HIZ);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JComboBox createInputComboBox(final int portNumber, final int bitNumber, final VariableFunctionPin pin) {
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
        comboBox.setBorder(BORDER_HIZ);
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
                        new FixedSourceComponent(1, "Fixed " + Constants.LABEL_HI + " for " + pin.getName(), ui.getPrefs().isLogPinMessages(chip)).insertAtPin(otherSideOfSpyWirePin);
                        ui.getPrefs().setPortInputValueOverride(chip, portNumber, bitNumber, 1);
                        break;
                    case 2:
                        // GND
                        new FixedSourceComponent(0, "Fixed " + Constants.LABEL_LO + " for " + pin.getName(), ui.getPrefs().isLogPinMessages(chip)).insertAtPin(otherSideOfSpyWirePin);
                        ui.getPrefs().setPortInputValueOverride(chip, portNumber, bitNumber, 0);
                        break;
                }
                updatePinState(portNumber, bitNumber, pin);
            }
        });
        return comboBox;
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
        Border border;
        if (pin.isInput()) {
            // pin is configured as input
            if (pin.isInputEnabled()) {
                // input is enabled
                pinComp = pinInputs[portNumber][7 - bitNumber];
            }
            else {
                // input is disabled
                pinComp = pinDisabledInputs[portNumber][7 - bitNumber];
            }
            Integer inputValue = pin.getInputValue();
            if (inputValue == null) {
                border = BORDER_HIZ;
            }
            else if (inputValue == 0) {
                border = BORDER_LO;
            }
            else {
                border = BORDER_HI;
            }
        }
        else {
            // pin is configured as output
            pinComp = pinOutputs[portNumber][7 - bitNumber];
            Integer outputValue = pin.getOutputValue();
            if (outputValue == null) {
                if (ui.getPrefs().isLogPinMessages(chip)) System.err.println("OutputValue is null for pin " + pin.getName());
                ((JLabel)pinComp).setText(Constants.LABEL_HIZ);
                color = Constants.COLOR_HIZ;
                border = BORDER_HIZ;
            }
            else if (outputValue == 0) {
                ((JLabel)pinComp).setText(Constants.LABEL_LO);
                color = Constants.COLOR_LO;
                border = BORDER_LO;
            }
            else {
                ((JLabel)pinComp).setText(Constants.LABEL_HI);
                color = Constants.COLOR_HI;
                border = BORDER_HI;
            }
            pinComp.setBackground(color);
        }

        String toolTipText = computeTooltip(pin);
        pinComp.setToolTipText(toolTipText);
        pinComp.setBorder(border);

        JPanel cell = pinCells[portNumber][7 - bitNumber];
        if (cell.getComponents().length == 0 || cell.getComponent(0) != pinComp) {
            // The cell does not currently contain the suitable component. Replace it.
            cell.removeAll();
            cell.add(pinComp);
        }

        // Also update config page
        labels[portNumber][7 - bitNumber].setBorder(border);
    }

    /*
     *  Compute tooltip as a concatenation of lines describing pins connected to this one in series
     */
    private String computeTooltip(VariableFunctionPin pin) {
        // Html to allow multi-line
        String toolTipText = "<html>" + pin.getFunctionFullName();
        Pin currentPin = pin.getConnectedPin();
        while (currentPin != null) {
            if (currentPin instanceof ForwardingPin) {
                // Pin is part of a two-pin component. See what's on the other side
                Pin targetPin = ((ForwardingPin)currentPin).getTargetPin();
                // This is a wire component (e.g. logging on one side or the other)
//                toolTipText += " &lt;-&gt; " + currentPin.getName() + "<br/>" + targetPin.getName();
                // And go on with the component connected to that other side
                currentPin = targetPin.getConnectedPin();
                if (currentPin == null) {
                    toolTipText += " (NC)";
                }
            }
            else {
                toolTipText += " &lt;-&gt; " + currentPin.getName();
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
