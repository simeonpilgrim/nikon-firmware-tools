package com.nikonhacker.gui.component.codeStructure;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import com.nikonhacker.dfr.Function;
import com.nikonhacker.dfr.Jump;

public class CodeStructureMxGraph extends mxGraph {

    private final mxHierarchicalLayout layout;

    public CodeStructureMxGraph(int orientation) {
        super();
        layout = new mxHierarchicalLayout(this, orientation);
        //        layout.setIntraCellSpacing(layout.getIntraCellSpacing() * 0); // between elements at the same level
        layout.setInterRankCellSpacing(200);
    }


    public void setOrientation(int orientation) {
        layout.setOrientation(orientation);
        executeLayout();
    }

    @Override
    public boolean isCellSelectable(Object cell) {
        // Prevent manual edge editing
        if (cell != null) {
            if (cell instanceof mxCell) {
                mxCell myCell = (mxCell) cell;
                if (myCell.isEdge())
                    return false;
            }
        }
        return super.isCellSelectable(cell);
    }

    void expandFunction(Function function, CodeStructureFrame codeStructureFrame) {
        getModel().beginUpdate();
        try
        {
            if (!codeStructureFrame.cellObjects.containsKey(function.getAddress())) {
                codeStructureFrame.addFunction(function);
            }
            
            codeStructureFrame.makeExpandedStyle(function);

            for (Jump call : function.getCalls()) {
                if (!codeStructureFrame.renderedCalls.contains(call)) {
                    Object targetCell = codeStructureFrame.cellObjects.get(call.getTarget());
                    if (targetCell == null) {
                        Function targetFunction = codeStructureFrame.codeStructure.getFunctions().get(call.getTarget());
                        if (targetFunction == null) {
                            targetCell = codeStructureFrame.addFakeFunction(call.getTarget());
                        }
                        else {
                            targetCell = codeStructureFrame.addFunction(targetFunction);
                        }
                    }
                    // Add calls as edges
                    codeStructureFrame.addCall(function, call, targetCell);
                }
            }

            for (Jump call : function.getCalledBy().keySet()) {
                if (!codeStructureFrame.renderedCalls.contains(call)) {
                    Function sourceFunction = function.getCalledBy().get(call);
                    if (!codeStructureFrame.cellObjects.containsKey(sourceFunction.getAddress())) {
                        codeStructureFrame.addFunction(sourceFunction);
                    }
                    // Add calls as edges
                    codeStructureFrame.addCall(sourceFunction, call, codeStructureFrame.cellObjects.get(function.getAddress()));
                }
            }

//            Map<Integer,Object> functionObjects = new HashMap<Integer, Object>();
//            Function function = codeStructure.getFunctions().get(focusedAddress);
//            addFunction(functionObjects, function);
//            for (Jump call : function.getCalls()) {
//                // Add functions as nodes
//                if (!functionObjects.containsKey(call.getTarget())) {
//                    Function target = codeStructure.getFunctions().get(call.getTarget());
//                    addFunction(functionObjects, target);
//                }
//                // Add calls as edges
//                graph.insertEdge(parent, null, "", functionObjects.get(function.getAddress()), functionObjects.get(call.getTarget()));
//            }

        }
        finally
        {
            getModel().endUpdate();
        }
        // Layout
        executeLayout();
    }

    public void executeLayout() {
        getModel().beginUpdate();
        try {
            layout.execute(getDefaultParent());
        }
        finally {
            getModel().endUpdate();
        }
    }


}
