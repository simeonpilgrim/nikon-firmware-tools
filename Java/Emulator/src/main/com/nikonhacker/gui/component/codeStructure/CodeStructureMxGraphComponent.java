package com.nikonhacker.gui.component.codeStructure;


import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.nikonhacker.Format;
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
                        Object value = vertex.getValue();
                        if (value instanceof Function) {
                            codeStructureFrame.writeFunction((Function) value);
                        }
                        else if (value instanceof Integer) {
                            codeStructureFrame.writeText("; The function at address 0x" + Format.asHex((Integer) value, 8) + " was not part of a CODE segment and was not disassembled");
                        }
                        else {
                            codeStructureFrame.writeText("; The target for this jump could not be determined in a static way");
                        }
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
                            Object value = vertex.getValue();
                            if (value instanceof Function) {
                                ((CodeStructureMxGraph)graph).expandFunction((Function) value, codeStructureFrame);
                            }
                        }
                    }
                }
            }

        });
    }


}
