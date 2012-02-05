package com.nikonhacker.gui.component.codeStructure;


import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CodeStructureMxGraphComponent extends mxGraphComponent {
    public CodeStructureMxGraphComponent(mxGraph graph) {
        super(graph);
    }

    @Override
    protected void installDoubleClickHandler() {
        graphControl.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    if (!e.isConsumed() && isEditEvent(e)) {
                        Object cell = getCellAt(e.getX(), e.getY(), false);

                        if (cell != null && getGraph().isCellEditable(cell)) {
                            mxCell vertex = (mxCell) cell;
                            JOptionPane.showMessageDialog(null, "Dbl Click inside " + vertex.getValue(), "Done", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }

        });
    }


}
