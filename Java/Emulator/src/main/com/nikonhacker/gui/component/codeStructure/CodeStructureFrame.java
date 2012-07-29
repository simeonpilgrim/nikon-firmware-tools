package com.nikonhacker.gui.component.codeStructure;

import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.canvas.mxSvgCanvas;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.fr.CodeStructure;
import com.nikonhacker.disassembly.Function;
import com.nikonhacker.disassembly.fr.Jump;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CodeStructureFrame extends DocumentFrame
{
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;

    CodeStructureMxGraph graph;
    CodeStructure codeStructure;
    private mxGraphComponent graphComponent;
    private FrCPUState cpuState;

    public enum Orientation{
        HORIZONTAL(SwingConstants.WEST),
        VERTICAL(SwingConstants.NORTH);
        
        private int swingValue;

        Orientation(int swingValue) {
            this.swingValue = swingValue;
        }

        public int getSwingValue() {
            return swingValue;
        }
    }

    public CodeStructureFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, FrCPUState cpuState, final CodeStructure codeStructure, final EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        this.cpuState = cpuState;
        this.codeStructure = codeStructure;

        setSize(FRAME_WIDTH, FRAME_HEIGHT);

        // Create fake structure
        // createFakeStructure();

        // Create toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        final JComboBox orientationCombo = new JComboBox(new Orientation[]{Orientation.HORIZONTAL, Orientation.VERTICAL});
        Orientation currentOrientation = getCurrentOrientation();
        orientationCombo.setSelectedItem(currentOrientation);
        orientationCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Orientation selOrientation = (Orientation) orientationCombo.getSelectedItem();
                ui.getPrefs().setCodeStructureGraphOrientation(selOrientation.name());
                graph.setOrientation(selOrientation.getSwingValue());
            }
        });
        toolbar.add(orientationCombo);

        toolbar.add(new JLabel("Target:"));
        final JTextField targetField = new JTextField(7);

        ActionListener exploreActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Integer address = codeStructure.getAddressFromText(targetField.getText());
                if (address == null) {
                    targetField.setBackground(Color.RED);
                }
                else {
                    targetField.setBackground(Color.WHITE);
                    Function function = codeStructure.getFunctions().get(address);
                    if (function == null) {
                        function = codeStructure.findFunctionIncluding(address);
                    }
                    if (function == null) {
                        JOptionPane.showMessageDialog(CodeStructureFrame.this, "No function found at address 0x" + Format.asHex(address, 8), "Cannot explore function", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        graph.expandFunction(function, codeStructure, true, true);
                    }
                }
            }
        };

        targetField.addActionListener(exploreActionListener);
        toolbar.add(targetField);

        final JButton exploreButton = new JButton("Explore");
        exploreButton.addActionListener(exploreActionListener);
        toolbar.add(exploreButton);
        JButton goToPcButton = new JButton("Go to PC");
        toolbar.add(goToPcButton);
        goToPcButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                targetField.setText(Format.asHex(CodeStructureFrame.this.cpuState.pc, 8));
                exploreButton.doClick(0);
            }
        });



        JButton svgButton = new JButton("Save SVG");
        svgButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();

                fc.setDialogTitle("Save SVG as...");
                fc.setCurrentDirectory(new java.io.File("."));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setAcceptAllFileFilterUsed(true);

                if (fc.showOpenDialog(CodeStructureFrame.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        saveSvg(fc.getSelectedFile());
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(CodeStructureFrame.this, e1.getMessage(), "Error saving to SVG", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        toolbar.add(svgButton);

        JButton pngButton = new JButton("Save PNG");
        pngButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();

                fc.setDialogTitle("Save PNG as...");
                fc.setCurrentDirectory(new java.io.File("."));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setAcceptAllFileFilterUsed(true);

                if (fc.showOpenDialog(CodeStructureFrame.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        savePng(fc.getSelectedFile());
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(CodeStructureFrame.this, e1.getMessage(), "Error saving to SVG", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        toolbar.add(pngButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graph.clear();
            }
        });
        toolbar.add(clearButton);

        
        // Create graph
        Component graphComponent = getGraphPane();

        // Create and fill main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(graphComponent, BorderLayout.CENTER);

        getContentPane().add(mainPanel);

        pack();
    }

    private Orientation getCurrentOrientation() {
        Orientation currentOrientation;
        try {
            currentOrientation = Orientation.valueOf(ui.getPrefs().getCodeStructureGraphOrientation());
        } catch (Exception e) {
            currentOrientation = Orientation.HORIZONTAL;
        }
        return currentOrientation;
    }

    /** for debugging only */
    private void createFakeStructure() {
        codeStructure = new CodeStructure(0);
        Function sourceFunction = new Function(0, "main", "comment", Function.Type.MAIN);
        codeStructure.getFunctions().put(0, sourceFunction);
        for (int i = 1; i <= 10; i++) {
            int address = i * 10;
            Function function = new Function(address, "Function" + i, "", Function.Type.STANDARD);
            codeStructure.getFunctions().put(address, function);
            sourceFunction.getCalls().add(new Jump(0, address, null, false));
            for (int j = 1; i <= 10; i++) {
                int address2 = i * 10 + j;
                Function function2 = new Function(address2, "SubFunction" + j, "", Function.Type.STANDARD);
                codeStructure.getFunctions().put(address2, function2);
                function.getCalls().add(new Jump(address, address2, null, false));
            }
        }
    }

    public Component getGraphPane() {
        // Create graph object
        graph = new CodeStructureMxGraph(getCurrentOrientation().getSwingValue());
        // Prevent manual cell resizing
        graph.setCellsResizable(false);
        // Prevent manual cell moving
        graph.setCellsMovable(false);

        graph.setMinimumGraphSize(new mxRectangle(0, 0, FRAME_WIDTH, FRAME_HEIGHT));

        // Create graph component
        graphComponent = new CodeStructureMxGraphComponent(graph, this, ui);
        // Prevent edge drawing from UI
        graphComponent.setConnectable(false);
        graphComponent.setAutoScroll(true);
        graphComponent.setDragEnabled(false);
        return graphComponent;
    }


    public void writeFunction(Function function) throws IOException {
        ui.jumpToSource(function);
    }


    private void saveSvg(File file) throws IOException {
        try {
            // Save as SVG
            mxSvgCanvas canvas = (mxSvgCanvas) mxCellRenderer.drawCells(graph, null, 1, null,
                    new mxCellRenderer.CanvasFactory() {
                        public mxICanvas createCanvas(int width, int height) {
                            mxSvgCanvas canvas = new mxSvgCanvas(mxUtils.createSvgDocument(width, height));
                            canvas.setEmbedded(true);
                            return canvas;
                        }
                    });
            mxUtils.writeFile(mxUtils.getXml(canvas.getDocument()), file.getAbsolutePath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void savePng(File file) throws IOException {
        // Creates the image for the PNG file
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, null, graphComponent.getCanvas());

        // Creates the URL-encoded XML data
        mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);
        param.setCompressedText(new String[] { });

        // Saves as a PNG file
        FileOutputStream outputStream = new FileOutputStream(file);
        try
        {
            mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);

            if (image != null)
            {
                encoder.encode(image);
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Error rendering image", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        finally
        {
            outputStream.close();
        }
    }

}
