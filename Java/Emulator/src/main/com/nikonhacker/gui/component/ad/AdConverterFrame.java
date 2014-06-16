package com.nikonhacker.gui.component.ad;

import com.nikonhacker.Format;
import com.nikonhacker.disassembly.ParsingException;
import com.nikonhacker.emu.peripherials.adConverter.AdConverter;
import com.nikonhacker.emu.peripherials.adConverter.AdUnit;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.swing.DocumentFrame;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdConverterFrame extends DocumentFrame {
    private AdConverter adConverter;
    private Map<String, JTextField> valueFields = new HashMap<String, JTextField>();

    public AdConverterFrame(String title, String imageName, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, int chip, final EmulatorUI ui, AdConverter adConverter) {
        super(title, imageName, resizable, closable, maximizable, iconifiable, chip, ui);
        this.adConverter = adConverter;
        JPanel mainPanel = new JPanel(new MigLayout());
        mainPanel.add(new JLabel("Channel"));
        mainPanel.add(new JLabel("List of values"), "center, wrap");
        for (AdUnit adUnit : adConverter.getUnits()) {
            for (int i = 0; i < adUnit.getNumChannels(); i++) {
                final String channelKey = "" + adUnit.getUnitName() + i;
                JLabel channelLabel = new JLabel(channelKey);
                mainPanel.add(channelLabel, "center");

                final JTextField valueListField = new JTextField();
                List<Integer> adValueList = ui.getPrefs().getAdValueList(chip, channelKey);
                if (adValueList == null) {
                    adValueList = new ArrayList<Integer>();
                    adValueList.add(0);
                }
                valueListField.setText(formatList(adValueList));
                valueListField.setMaximumSize(new Dimension(1000, getPreferredSize().height));
                valueListField.setPreferredSize(new Dimension(1000, getPreferredSize().height));
                mainPanel.add(valueListField, "growx, wrap");

                valueFields.put(channelKey, valueListField);
            }
            mainPanel.add(Box.createVerticalStrut(10) , "wrap");
        }
        JButton saveButton = new JButton("Save values");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveValueLists();
            }
        });

        mainPanel.add(saveButton, "span 2, center, wrap");
        setContentPane(mainPanel);
    }

    private void saveValueLists() {
        for (AdUnit adUnit : adConverter.getUnits()) {
            for (int i = 0; i < adUnit.getNumChannels(); i++) {
                String channelKey = "" + adUnit.getUnitName() + i;
                saveValueList(channelKey, valueFields.get(channelKey));
            }
        }
    }

    private void saveValueList(String channelKey, JTextField valueListField) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        String[] values = StringUtils.split(valueListField.getText(), ", ");
        for (String value : values) {
            try {
                list.add(Format.parseUnsigned(value));
            } catch (ParsingException e) {
                valueListField.setBackground(Color.RED);
                return;
            }
        }
        ui.getPrefs().setAdValueList(chip, channelKey, list);
        valueListField.setText(formatList(list));
        valueListField.setBackground(Color.WHITE);
    }

    private String formatList(List<Integer> adValueList) {
        String values = "";
        for (Integer value : adValueList) {
            if (values.length() != 0) {
                values += ", ";
            }
            values += "0x" + Format.asHex(value, 1);
        }
        return values;
    }
}
