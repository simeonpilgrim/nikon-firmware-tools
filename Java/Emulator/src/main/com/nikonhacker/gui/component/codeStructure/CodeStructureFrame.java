package com.nikonhacker.gui.component.codeStructure;

import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.canvas.mxSvgCanvas;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxUtils;
import com.nikonhacker.dfr.CodeStructure;
import com.nikonhacker.dfr.Function;
import com.nikonhacker.dfr.Jump;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CodeStructureFrame extends DocumentFrame
{
    Object parent;
    CodeStructureMxGraph graph;

    public CodeStructureFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, CodeStructure codeStructure, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        setSize(600, 400);

        // Create a left-right split pane
        Component listing = getListing();
        getContentPane().add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getGraph(), listing));
        pack();
    }

    public Component getGraph() {
        graph = new CodeStructureMxGraph();
        parent = graph.getDefaultParent();

        // Prevent manual cell resizing
        graph.setCellsResizable(false);
        // Prevent manual cell moving
        graph.setCellsMovable(false);

        graph.getModel().beginUpdate();
        try
        {
            // Create fake structure
            CodeStructure codeStructure = new CodeStructure(0);
            Function sourceFunction = new Function(0, "main", "comment");
            codeStructure.getFunctions().put(0, sourceFunction);
            for (int i = 1; i <= 15; i++) {
                int address = i * 10;
                Function function = new Function(address, "Function" + i, "");
                codeStructure.getFunctions().put(address, function);
                sourceFunction.getCalls().add(new Jump(0, address, false));
            }
            
            // Render it
            Map<Integer,Object> functionObjects = new HashMap<Integer, Object>();
            addFunction(functionObjects, sourceFunction);
            
            
            for (Jump call : sourceFunction.getCalls()) {
                if (!functionObjects.containsKey(call.getTarget())) {
                    Function target = codeStructure.getFunctions().get(call.getTarget());
                    addFunction(functionObjects, target);
                }
                // Add calls as edges
                graph.insertEdge(parent, null, "", functionObjects.get(sourceFunction.getAddress()), functionObjects.get(call.getTarget()));
            }

//            Map<Integer,Object> functionObjects = new HashMap<Integer, Object>();
//            Function sourceFunction = codeStructure.getFunctions().get(focusedAddress);
//            addFunction(functionObjects, sourceFunction);
//            for (Jump call : sourceFunction.getCalls()) {
//                // Add functions as nodes
//                if (!functionObjects.containsKey(call.getTarget())) {
//                    Function target = codeStructure.getFunctions().get(call.getTarget());
//                    addFunction(functionObjects, target);
//                }
//                // Add calls as edges
//                graph.insertEdge(parent, null, "", functionObjects.get(sourceFunction.getAddress()), functionObjects.get(call.getTarget()));
//            }

        }
        finally
        {
            graph.getModel().endUpdate();
        }

        // Layout
        graph.getModel().beginUpdate();
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);
//        layout.setIntraCellSpacing(layout.getIntraCellSpacing() * 0); // between elements at the same level
        layout.setInterRankCellSpacing(200);
        try {
            layout.execute(parent);
        }
        finally {
            graph.getModel().endUpdate();
        }

        mxGraphComponent graphComponent = new CodeStructureMxGraphComponent(graph);
        // Prevent edge drawing from UI
        graphComponent.setConnectable(false);
        graphComponent.setAutoScroll(true);
        graphComponent.setDragEnabled(false);
        return graphComponent;
    }

    private Component getListing() {
        JTextArea area = new JTextArea(50, 80);

        return new JScrollPane(area, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }



    private void addFunction(Map<Integer, Object> functionObjects, Function function) {
        Object vertex = graph.insertVertex(parent, null, function, 0, 0, 80, 30, "defaultVertex;fillColor=" + function.getColor());
        functionObjects.put(function.getAddress(), vertex);
    }

    private void saveSvg() throws IOException {
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
            mxUtils.writeFile(mxUtils.getXml(canvas.getDocument()), "d:\\graphtest.svg");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
