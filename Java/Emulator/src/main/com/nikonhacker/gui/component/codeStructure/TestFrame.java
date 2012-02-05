package com.nikonhacker.gui.component.codeStructure;

import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.canvas.mxSvgCanvas;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.nikonhacker.Format;
import com.nikonhacker.dfr.CodeStructure;
import com.nikonhacker.dfr.Function;
import com.nikonhacker.dfr.Jump;
import com.nikonhacker.gui.EmulatorUI;
import com.nikonhacker.gui.component.DocumentFrame;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestFrame extends DocumentFrame
{
    private CodeStructure codeStructure;
    private int focusedAddress = 0x40000;
    Object parent;
    mxGraph graph;

    public TestFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, CodeStructure codeStructure, EmulatorUI ui) {
        super(title, resizable, closable, maximizable, iconifiable, ui);
        setSize(600, 400);
        this.codeStructure = codeStructure;

        showGraph();
        pack();
    }

    public void showGraph() {
        graph = new mxGraph();
        parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        try
        {
            Map<Integer,Object> functionObjects = new HashMap<Integer, Object>();
            Function sourceFunction = codeStructure.getFunctions().get(focusedAddress);
            addFunction(functionObjects, sourceFunction);
            for (Jump call : sourceFunction.getCalls()) {
                // Add functions as nodes
                if (!functionObjects.containsKey(call.getTarget())) {
                    Function target = codeStructure.getFunctions().get(call.getTarget());
                    addFunction(functionObjects, target);
                }
                // Add calls as edges
                graph.insertEdge(parent, null, "", functionObjects.get(sourceFunction.getAddress()), functionObjects.get(call.getTarget()));
            }
        }
        finally
        {
            graph.getModel().endUpdate();
        }
        Object cell = graph.getDefaultParent();

        // Layout
        graph.getModel().beginUpdate();
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);
//        layout.setIntraCellSpacing(layout.getIntraCellSpacing() * 0); // between elements at the same level
        layout.setInterRankCellSpacing(200);
        try {
            layout.execute(cell);
        }
        finally {
            graph.getModel().endUpdate();
        }

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        getContentPane().add(graphComponent);
    }

    private void addFunction(Map<Integer, Object> functionObjects, Function function) {
        Object vertex = graph.insertVertex(parent, null, function.getName() + "\n0x" + Format.asHex(function.getAddress(), 8), 0, 0, 80, 30, "defaultVertex;fillColor=" + function.getColor());
        functionObjects.put(function.getAddress(), vertex);
    }

    private void saveSvg() throws IOException {
        try {
            // Save as SVG
            mxSvgCanvas canvas = (mxSvgCanvas) mxCellRenderer.drawCells(graph, null, 1, null,
                    new mxCellRenderer.CanvasFactory() {
                        public mxICanvas createCanvas(int width, int height) {
                            mxSvgCanvas c = new mxSvgCanvas(mxUtils.createSvgDocument(width, height));
                            c.setEmbedded(true);
                            return c;
                        }
                    });
            mxUtils.writeFile(mxUtils.getXml(canvas.getDocument()), "d:\\graphtest.svg");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
