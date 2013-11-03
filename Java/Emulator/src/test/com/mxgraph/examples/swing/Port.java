package com.mxgraph.examples.swing;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxMultiplicity;

import javax.swing.*;
import java.util.Arrays;

public class Port extends JFrame {

    final int PORT_DIAMETER = 20;
    final int PORT_RADIUS = PORT_DIAMETER / 2;

    public Port() {
        super("Hello, World!");

        mxGraph graph = new mxGraph() {
            // Ports are not used as terminals for edges, they are
            // only used to compute the graphical connection point
            public boolean isPort(Object cell) {
                mxGeometry geometry = getCellGeometry(cell);
                return (geometry != null) && geometry.isRelative();
            }

            // Removes the folding icon and disables any folding
            public boolean isCellFoldable(Object cell, boolean collapse) {
                return false;
            }
        };

        Object parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        try {
            mxCell v1 = (mxCell) graph.insertVertex(parent, null, "Serial", 20, 20, 100, 100, "");
            v1.setConnectable(false);
            mxGeometry geo = graph.getModel().getGeometry(v1);
            // The size of the rectangle when the minus sign is clicked
            geo.setAlternateBounds(new mxRectangle(20, 20, 100, 50));

            mxGeometry geo1 = new mxGeometry(1.0, 0.3, PORT_DIAMETER, PORT_DIAMETER);
            // Because the origin is at upper left corner, need to translate to
            // position the center of port correctly
            geo1.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
            geo1.setRelative(true);

            mxCell port1 = new mxCell(null, geo1, "shape=triangle;perimeter=trianglePerimeter;rotation=180");
            port1.setVertex(true);

            mxGeometry geo2 = new mxGeometry(1.0, 0.7, PORT_DIAMETER,
                    PORT_DIAMETER);
            geo2.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
            geo2.setRelative(true);

            mxCell port2 = new mxCell(null, geo2, "shape=triangle;perimeter=trianglePerimeter");
            port2.setVertex(true);

            graph.addCell(port1, v1);
            graph.addCell(port2, v1);

            Object v2 = graph.insertVertex(parent, null, "HSerial!", 240, 150, 80, 30);

            graph.insertEdge(parent, null, "Edge", port2, v2);

            mxMultiplicity[] multiplicities = new mxMultiplicity[3];

            // Source nodes needs 1..2 connected Targets
            multiplicities[0] = new mxMultiplicity(true, "Serial", null, null, 1, "1", Arrays.asList(new String[]{"HSerial"}), "Serial Must Have 1 HSerial", "Source Must Connect to Target", true);

            // Source node does not want any incoming connections
            multiplicities[1] = new mxMultiplicity(false, "Serial", null, null, 0, "0", null, "Source Must Have No Incoming Edge", null, true); // Type does not matter

            // Target needs exactly one incoming connection from Source
            multiplicities[2] = new mxMultiplicity(false, "HSerial", null, null, 1, "1", Arrays.asList(new String[]{"Source"}), "Target Must Have 1 Source", "Target Must Connect From Source", true);

            graph.setMultiplicities(multiplicities);
        } finally {
            graph.getModel().endUpdate();
        }

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        getContentPane().add(graphComponent);
        graphComponent.setToolTips(true);
    }

    public static void main(String[] args) {
        Port frame = new Port();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 320);
        frame.setVisible(true);
    }

}