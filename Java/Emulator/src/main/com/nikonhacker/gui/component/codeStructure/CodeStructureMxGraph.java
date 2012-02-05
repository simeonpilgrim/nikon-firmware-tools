package com.nikonhacker.gui.component.codeStructure;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class CodeStructureMxGraph extends mxGraph {

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
}
