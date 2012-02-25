package com.nikonhacker.gui.component.sourceCode;

import com.nikonhacker.Format;
import com.nikonhacker.dfr.*;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.PrintWriterArea;
import com.nikonhacker.gui.component.SearchableTextAreaPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class SourceCodeFrame extends DocumentFrame
{
    private static final int FRAME_WIDTH = 400;
    private static final int FRAME_HEIGHT = 600;

    CodeStructure codeStructure;
    private final PrintWriterArea listingArea;

    public SourceCodeFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, final CodeStructure codeStructure, final EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.codeStructure = codeStructure;

        // Create toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        final JTextField targetAddressField = new JTextField(7);
        toolbar.add(targetAddressField);

        JButton exploreButton = new JButton("Explore");
        exploreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int address = Format.parseUnsigned(targetAddressField.getText());
                    Function function = codeStructure.getFunctions().get(address);
                    if (function == null) {
                        function = codeStructure.findFunctionIncluding(address);
                    }
                    if (function == null) {
                        JOptionPane.showMessageDialog(SourceCodeFrame.this, "No function found at address 0x" + Format.asHex(address, 8), "Cannot explore function", JOptionPane.ERROR_MESSAGE);
                    }
                    else{
                        writeFunction(function);
                    }
                } catch (ParsingException ex) {
                    JOptionPane.showMessageDialog(SourceCodeFrame.this, ex.getMessage(), "Error parsing address", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        toolbar.add(exploreButton);

        // Create right hand listing
        listingArea = new PrintWriterArea(50, 80);
        Component listingComponent = getListingPane();

        // Create and fill main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(listingComponent, BorderLayout.CENTER);

        getContentPane().add(mainPanel);

        pack();
    }

    private Component getListingPane() {
        listingArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        return new SearchableTextAreaPanel(listingArea);
    }


    public void writeFunction(Function function) {
        try {
            listingArea.clear();
            Writer writer = listingArea.getPrintWriter();
            List<CodeSegment> segments = function.getCodeSegments();
            if (segments.size() == 0) {
                writer.write("; function at address 0x" + Format.asHex(function.getAddress(), 8) + " was not disassembled (not in CODE range)");
            }
            else {
                for (int i = 0; i < segments.size(); i++) {
                    CodeSegment codeSegment = segments.get(i);
                    if (segments.size() > 1) {
                        writer.write("; Segment " + (i+1) + "/" + segments.size() + "\n");
                    }
                    for (int address = codeSegment.getStart(); address <= codeSegment.getEnd(); address = codeStructure.getInstructions().higherKey(address)) {
                        DisassembledInstruction instruction = codeStructure.getInstructions().get(address);
                        try {
                            codeStructure.writeInstruction(writer, address, instruction, 0);
                        } catch (IOException e) {
                            writer.write("# ERROR decoding instruction at address 0x" + Format.asHex(address, 8) + " : " + e.getMessage());
                        }
                    }
                    writer.write("\n");
                }
            }
            listingArea.setCaretPosition(0);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error writing function source", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void writeText(String text) throws IOException {
        listingArea.clear();
        Writer writer = listingArea.getPrintWriter();
        writer.write(text);
    }

}
