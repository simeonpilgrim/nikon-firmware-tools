package com.nikonhacker.gui.component.codeStructure;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.nikonhacker.disassembly.CodeStructure;
import com.nikonhacker.disassembly.Function;
import com.nikonhacker.disassembly.Jump;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CodeStructureMxGraph extends mxGraph {

    private static final int FUNCTION_CELL_WIDTH = 100;
    private static final int FUNCTION_CELL_HEIGHT = 30;
    private static final int FAKE_FUNCTION_CELL_WIDTH = 50;
    private static final int FAKE_FUNCTION_CELL_HEIGHT = 20;

    private final mxHierarchicalLayout layout;

    // Map from Object (Function, or anonymous Object when calling unknown destination) to cell
    Map<Object,Object> cellObjects = new HashMap<Object, Object>();
    Set<Jump> renderedCalls = new HashSet<Jump>();

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

    public void expandFunction(Function function, CodeStructure codeStructure, boolean expandUsage, boolean expandCallees) {
        getModel().beginUpdate();
        try
        {
            if (!cellObjects.containsKey(function.getAddress())) {
                addFunction(function);
            }

            makeExpandedStyle(function);

            if (expandCallees) {
                for (Jump call : function.getCalls()) {
                    if (!renderedCalls.contains(call)) {
                        Object targetCell = cellObjects.get(call.getTarget());
                        if (targetCell == null) {
                            Function targetFunction = codeStructure.getFunctions().get(call.getTarget());
                            if (targetFunction == null) {
                                targetCell = addFakeFunction(call.getTarget());
                            }
                            else {
                                targetCell = addFunction(targetFunction);
                            }
                        }
                        // Add calls as edges
                        addCall(function, call, targetCell);
                    }
                }
            }

            if (expandUsage) {
                for (Jump call : function.getCalledBy().keySet()) {
                    if (!renderedCalls.contains(call)) {
                        Function sourceFunction = function.getCalledBy().get(call);
                        if (!cellObjects.containsKey(sourceFunction.getAddress())) {
                            addFunction(sourceFunction);
                        }
                        // Add calls as edges
                        addCall(sourceFunction, call, cellObjects.get(function.getAddress()));
                    }
                }
            }
        }
        finally
        {
            getModel().endUpdate();
        }
        // Layout
        executeLayout();
    }

    public void removeFunction(Function function) {
        getModel().beginUpdate();
        try
        {
            if (cellObjects.containsKey(function.getAddress())) {
                // Visually remove cell from graph, including call edges
                removeCells(new Object[]{cellObjects.get(function.getAddress())}, true);

                // Process memory structures

                // Remove calls to other functions
                for (Jump call : function.getCalls()) {
                    renderedCalls.remove(call);
                }

                // Remove calls to this function
                for (Jump call : function.getCalledBy().keySet()) {
                    renderedCalls.remove(call);
                }

                // Remove this function
                cellObjects.remove(function.getAddress());
            }
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


    public Object addFakeFunction(int address) {
        // Fake functions are targets that haven't been disassembled as code
        Object vertex;
        Object value;
        if (address == 0) {
            value = "??";
            vertex = insertVertex(getDefaultParent(), new Object().toString(), value, 0, 0, FAKE_FUNCTION_CELL_WIDTH, FAKE_FUNCTION_CELL_HEIGHT, "defaultVertex;" + mxConstants.STYLE_FILLCOLOR + "=#FF7700");
        }
        else {
            value = address;
            vertex = insertVertex(getDefaultParent(), "" + address, value, 0, 0, FAKE_FUNCTION_CELL_WIDTH, FAKE_FUNCTION_CELL_HEIGHT, "defaultVertex;" + mxConstants.STYLE_FILLCOLOR + "=#FF0000");
        }
        cellObjects.put(value, vertex);
        return vertex;
    }

    private Object addFunction(Function function) {
        // Function cells are created white and remain so until they are expanded
        String style = "defaultVertex;" + mxConstants.STYLE_FILLCOLOR + "=#FFFFFF;" + mxConstants.STYLE_STROKECOLOR + "=" + function.getBorderColor();
        Object vertex = insertVertex(getDefaultParent(), "" + function.getAddress(), function, 0, 0, FUNCTION_CELL_WIDTH, FUNCTION_CELL_HEIGHT, style);
        cellObjects.put(function.getAddress(), vertex);
        return vertex;
    }

    private void addCall(Function sourceFunction, Jump call, Object targetCell) {
        String style = "noLabel=1;" + mxConstants.STYLE_STROKECOLOR + "=" + getStrokeColor(call) + (call.isDynamic()?";" + mxConstants.STYLE_DASHED + "=true":"");
        insertEdge(getDefaultParent(), null, call, cellObjects.get(sourceFunction.getAddress()), targetCell, style);
        renderedCalls.add(call);
    }

    private String getStrokeColor(Jump jump) {
        if (jump.getInstruction() == null) {
            // Should not happen
            return "#FF0000";
        }
        switch (jump.getInstruction().flowType) {
            case CALL:
                return "#777777";
            case INT:
            case INTE:
                return "#00CC00";
            default:
                return "#777700";
        }
    }

    public void clear() {
        removeCells(getChildCells(getDefaultParent(), true, true));
        cellObjects = new HashMap<Object, Object>();
        renderedCalls = new HashSet<Jump>();
        executeLayout();
    }

    private void makeExpandedStyle(Function function) {
        mxCell cell = getCellById("" + function.getAddress());
        setCellStyles(mxConstants.STYLE_FILLCOLOR, function.getFillColor(), new Object[]{cell});
    }

    private mxCell getCellById(String id) {
        for (Object c : getChildCells(getDefaultParent(), true, true)) {
            mxCell cell = (mxCell) c;
            if (id.equals(cell.getId())) {
                return cell;
            }
        }
        return null;
    }

}
