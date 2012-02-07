package com.nikonhacker.gui.component.codeStructure;

import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.canvas.mxSvgCanvas;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.nikonhacker.Format;
import com.nikonhacker.dfr.*;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;
import com.nikonhacker.gui.component.PrintWriterArea;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.List;

public class CodeStructureFrame extends DocumentFrame
{
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    Object parent;
    CodeStructureMxGraph graph;
    CodeStructure codeStructure;
    Map<Integer,Object> functionObjects = new HashMap<Integer, Object>();
    Set<Jump> renderedCalls = new HashSet<Jump>();
    private final PrintWriterArea listingArea;


    public CodeStructureFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, CodeStructure codeStructure, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.codeStructure = codeStructure;

        // Create fake structure
        // createFakeStructure();

        // Create left hand graph
        graph = new CodeStructureMxGraph();
        Component graphComponent = getGraphPane();

        // Create right hand listing
        listingArea = new PrintWriterArea(50, 80);
        listingArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        Component listingComponent = getListingPane();

        // Create a left-right split pane
        getContentPane().add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphComponent, listingComponent));

        // Start with entry point
        graph.expandFunction(this.codeStructure.getFunctions().get(this.codeStructure.getEntryPoint()), this);

        pack();
    }

    /** for debugging only */
    private void createFakeStructure() {
        codeStructure = new CodeStructure(0);
        Function sourceFunction = new Function(0, "main", "comment");
        codeStructure.getFunctions().put(0, sourceFunction);
        for (int i = 1; i <= 10; i++) {
            int address = i * 10;
            Function function = new Function(address, "Function" + i, "");
            codeStructure.getFunctions().put(address, function);
            sourceFunction.getCalls().add(new Jump(0, address, false));
            for (int j = 1; i <= 10; i++) {
                int address2 = i * 10 + j;
                Function function2 = new Function(address2, "SubFunction" + j, "");
                codeStructure.getFunctions().put(address2, function2);
                function.getCalls().add(new Jump(address, address2, false));
            }
        }
    }

    public Component getGraphPane() {
        parent = graph.getDefaultParent();

        // Prevent manual cell resizing
        graph.setCellsResizable(false);
        // Prevent manual cell moving
        graph.setCellsMovable(false);

        graph.setMinimumGraphSize(new mxRectangle(0, 0, FRAME_WIDTH/2, FRAME_HEIGHT));

        mxGraphComponent graphComponent = new CodeStructureMxGraphComponent(graph, this);
        // Prevent edge drawing from UI
        graphComponent.setConnectable(false);
        graphComponent.setAutoScroll(true);
        graphComponent.setDragEnabled(false);
        return graphComponent;
    }


    private Component getListingPane() {
        return new JScrollPane(listingArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }


    public void addCall(Function sourceFunction, Jump call) {
        graph.insertEdge(parent, null, "", functionObjects.get(sourceFunction.getAddress()), functionObjects.get(call.getTarget()));
        renderedCalls.add(call);
    }


    public void addFunction(Function function) {
        Object vertex = graph.insertVertex(parent, null, function, 0, 0, 90, 30, "defaultVertex;fillColor=" + function.getColor());
        functionObjects.put(function.getAddress(), vertex);
    }


    public void printFunction(Function function) throws IOException {
        listingArea.clear(); // clear
        Writer writer = listingArea.getWriter();
        List<CodeSegment> segments = function.getCodeSegments();
        for (int i = 0; i < segments.size(); i++) {
            CodeSegment codeSegment = segments.get(i);
            if (segments.size() > 1) {
                writer.write("; Segment #" + i + "\n");
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

    private void saveSvg(String filename) throws IOException {
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
            mxUtils.writeFile(mxUtils.getXml(canvas.getDocument()), filename);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
