package com.nikonhacker.gui.swing;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSelectionPanel extends JPanel implements ActionListener {
    private String label;
    private JLabel jlabel;
    private JButton button;
    private JTextField textField;
    private boolean directoryMode;
    private List<DependentField> dependentFields;
    private String dialogTitle;
    private FileFilter fileFilter;

    public FileSelectionPanel(String label, JTextField textField, boolean directoryMode) {
        super();
        init(label, textField, directoryMode, new ArrayList<DependentField>(), dialogTitle);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        jlabel.setEnabled(enabled);
        button.setEnabled(enabled);
        textField.setEnabled(enabled);
    }

    /**
     * Sets the filename to use.
     * @param fileFilter if null, all files are shown.
     */
    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    /**
     * Simpler way to declare a simple case-insensitive file filter for one file type
     * @param suffix the filename suffix to use as a filter, e.g. "*.bin"
     * @param description the text to be shown in the drop down, e.g. "Binary file (*.bin)"
     */
    public void setFileFilter(final String suffix, final String description) {
        this.fileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f != null) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    return f.getName().toLowerCase().endsWith(suffix);
                }
                return false;
            }

            @Override
            public String getDescription() {
                return description;
            }
        };
    }

    /**
     *
     * @param label
     * @param textField
     * @param directoryMode
     * @param dependentFields : a list of text fields that will be filled based on this one. Each field is associated with a suffix to customize secondary field filename. If it contains a dot, it replaces the extension, otherwise it replaces the full filename
     */
    public FileSelectionPanel(String label, JTextField textField, boolean directoryMode, List<DependentField> dependentFields) {
        super();
        init(label, textField, directoryMode, dependentFields, null);
    }

    /**
     *
     * @param label
     * @param textField
     * @param directoryMode
     * @param dependentFields : a list of text fields that will be filled based on this one. Each field is associated with a suffix to customize secondary field filename. If it contains a dot, it replaces the extension, otherwise it replaces the full filename
     *   
     */
    public FileSelectionPanel(String label, JTextField textField, boolean directoryMode, List<DependentField> dependentFields, String dialogTitle) {
        super();
        init(label, textField, directoryMode, dependentFields, dialogTitle);
    }

    private void init(String label, JTextField textField, boolean directoryMode, List<DependentField> dependentFields, String dialogTitle) {
        this.label = label;
        this.textField = textField;
        this.directoryMode = directoryMode;
        this.dependentFields = dependentFields;
        this.dialogTitle = dialogTitle;

        this.setLayout(new FlowLayout(FlowLayout.RIGHT));

        if (StringUtils.isNotBlank(label)) {
            jlabel = new JLabel(label);
            this.add(jlabel);
        }

        textField.setPreferredSize(new Dimension(400, (int) textField.getPreferredSize().getHeight()));
        this.add(textField);

        button = new JButton("...");
        this.add(button);

        button.addActionListener(this);
        //setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent e) {
        final JFileChooser fc = new JFileChooser();

        fc.setDialogTitle(StringUtils.isNotBlank(dialogTitle)?dialogTitle:(StringUtils.isNotBlank(label)?("Select " + label):"Select file"));
        fc.setCurrentDirectory(new java.io.File("."));

        if (directoryMode) {
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
        }
        else {
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileFilter == null) {
                fc.setAcceptAllFileFilterUsed(true);
            }
            else {
                fc.setFileFilter(fileFilter);
            }
        }

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            textField.setText(fc.getSelectedFile().getPath());
            for (DependentField dependentField : dependentFields) {
                if (StringUtils.isBlank(dependentField.field.getText())) {
                    // We have to fill the cascading target based on just made selection and given target
                    String text = textField.getText();
                    if (directoryMode) {
                        dependentField.field.setText(text + File.separatorChar + dependentField.suffix);
                    }
                    else {
                        if (dependentField.suffix.contains(".")) {
                            // replace filename
                            dependentField.field.setText(StringUtils.substringBeforeLast(text, File.separator) + File.separator + dependentField.suffix);
                        }
                        else {
                            // only replace extension
                            dependentField.field.setText(StringUtils.substringBeforeLast(text, ".") + "." + dependentField.suffix);
                        }
                    }
                }
            }
        }
    }

    public class DependentField {
        JTextField field;
        String suffix;

        public DependentField(JTextField field, String suffix) {
            this.field = field;
            this.suffix = suffix;
        }
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }
}
