package com.nikonhacker.gui.component.codeStructure;


import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.nikonhacker.Format;
import com.nikonhacker.dfr.CodeStructure;
import com.nikonhacker.dfr.Function;
import com.nikonhacker.dfr.Jump;
import com.nikonhacker.gui.EmulatorUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class CodeStructureMxGraphComponent extends mxGraphComponent {
    private CodeStructure codeStructure;
    private Function currentlySelectedFunction;

    public CodeStructureMxGraphComponent(final CodeStructureMxGraph graph, final CodeStructureFrame codeStructureFrame, final EmulatorUI ui) {
        super(graph);
        this.codeStructure = codeStructureFrame.codeStructure;

        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem findUsageMenuItem = new JMenuItem("Find usage");
        findUsageMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graph.expandFunction(currentlySelectedFunction, codeStructure, true, false);
            }
        });
        popupMenu.add(findUsageMenuItem);

        JMenuItem findCalleesMenuItem = new JMenuItem("Find callees");
        findCalleesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graph.expandFunction(currentlySelectedFunction, codeStructure, false, true);
            }
        });
        popupMenu.add(findCalleesMenuItem);

        JMenuItem removeMenuItem = new JMenuItem("Remove");
        removeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graph.removeFunction(currentlySelectedFunction);
            }
        });
        popupMenu.add(removeMenuItem);

        popupMenu.add(new JSeparator());

        final JMenuItem runMenuItem = new JMenuItem("Run then pause");
        runMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ui.playOneFunction(currentlySelectedFunction.getAddress(), false);
            }
        });
        popupMenu.add(runMenuItem);

        final JMenuItem debugMenuItem = new JMenuItem("Debug then pause");
        debugMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ui.playOneFunction(currentlySelectedFunction.getAddress(), true);
            }
        });
        popupMenu.add(debugMenuItem);


        // This handles only mouse click events
        getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Object cell = getCellAt(e.getX(), e.getY(), false);
                if (cell != null) {
                    mxCell vertex = (mxCell) cell;
                    try {
                        Object value = vertex.getValue();
                        if (value instanceof Function) {
                            Function function = (Function) value;
                            codeStructureFrame.writeFunction(function);
                        }
                        if (value instanceof Jump) {
                            Jump jump = (Jump) value;
                            ui.jumpToSource(jump.getSource());
                        }
                        else if (value instanceof Integer) {
                            ui.setStatusText("The function at address 0x" + Format.asHex((Integer) value, 8) + " was not part of a CODE segment and was not disassembled");
                        }
                        else {
                            ui.setStatusText("The target for this jump could not be determined in a static way");
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                maybePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybePopup(e);
            }

            private void maybePopup(MouseEvent e) {
                if (e.isPopupTrigger()) { // platform dependant trigger
                    Object cell = getCellAt(e.getX(), e.getY(), false);
                    if (cell != null) {
                        Object value = ((mxCell) cell).getValue();
                        if (value instanceof Function) {
                            currentlySelectedFunction = (Function) value;
                            debugMenuItem.setEnabled(ui.isEmulatorReady());
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
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
                                ((CodeStructureMxGraph)graph).expandFunction((Function) value, codeStructure, true, true);
                            }
                        }
                    }
                }
            }

        });
    }


}
