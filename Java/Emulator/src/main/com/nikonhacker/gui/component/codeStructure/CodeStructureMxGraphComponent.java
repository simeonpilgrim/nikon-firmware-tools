package com.nikonhacker.gui.component.codeStructure;


import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.nikonhacker.dfr.Function;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class CodeStructureMxGraphComponent extends mxGraphComponent {
    private CodeStructureFrame codeStructureFrame;

    public CodeStructureMxGraphComponent(final mxGraph graph, final CodeStructureFrame codeStructureFrame) {
        super(graph);
        this.codeStructureFrame = codeStructureFrame;

        // This handles only mouse click events
        getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Object cell = getCellAt(e.getX(), e.getY(), false);
                if (cell != null) {
                    mxCell vertex = (mxCell) cell;
                    //JOptionPane.showMessageDialog(null, "Single click inside " + vertex.getValue(), "Done", JOptionPane.INFORMATION_MESSAGE);
                    try {
                        codeStructureFrame.printFunction((Function) vertex.getValue());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void installDoubleClickHandler() {
        graphControl.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    if (!e.isConsumed() && isEditEvent(e)) {
                        Object cell = getCellAt(e.getX(), e.getY(), false);

                        if (cell != null) {
                            mxCell vertex = (mxCell) cell;
                            //JOptionPane.showMessageDialog(null, "Dbl Click inside " + vertex.getValue(), "Done", JOptionPane.INFORMATION_MESSAGE);
                            ((CodeStructureMxGraph)graph).expandFunction((Function) vertex.getValue(), codeStructureFrame);
                        }
                    }
                }
            }

        });
    }


}
