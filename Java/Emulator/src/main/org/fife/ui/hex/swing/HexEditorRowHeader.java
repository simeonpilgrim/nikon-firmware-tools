/*
 * Copyright (c) 2008 Robert Futrell
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name "HexEditor" nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.fife.ui.hex.swing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;


/**
 * Header of the hex table; displays address of the first byte on the
 * row.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class HexEditorRowHeader extends JList implements TableModelListener {

	private static final long serialVersionUID = 1L;

	private HexTable table;
	private RowHeaderListModel model;

	private static final Border CELL_BORDER =
							BorderFactory.createEmptyBorder(0,5,0,5);


	/**
	 * Constructor.
	 *
	 * @param table The table displaying the hex content.
	 */
	public HexEditorRowHeader(HexTable table) {
		this.table = table;
		model = new RowHeaderListModel();
		setModel(model);
		setFocusable(false);
		setFont(table.getFont());
		setFixedCellHeight(table.getRowHeight());
		setCellRenderer(new CellRenderer());
		setBorder(new RowHeaderBorder());
		setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		syncRowCount(); // Initialize to initial size of table.
		table.getModel().addTableModelListener(this);
	}


	public void addSelectionInterval(int anchor, int lead) {
		super.addSelectionInterval(anchor, lead);
		int min = Math.min(anchor, lead);
		int max = Math.max(anchor, lead);
		table.setSelectedRows(min, max);
	}


	public void removeSelectionInterval(int index0, int index1) {
		super.removeSelectionInterval(index0, index1);
		int anchor = getAnchorSelectionIndex();
		int lead = getLeadSelectionIndex();
		table.setSelectedRows(Math.min(anchor, lead), Math.max(anchor, lead));
	}


	public void setSelectionInterval(int anchor, int lead) {
		super.setSelectionInterval(anchor, lead);
		int min = Math.min(anchor, lead);
		int max = Math.max(anchor, lead);
		// Table may be showing 0 bytes, but we're showing 1 row header
		if (max<table.getRowCount()) {
			table.setSelectedRows(min, max);
		}
	}


	private void syncRowCount() {
		if (table.getRowCount()!=model.getSize()) {
			// Always keep 1 row, even if showing 0 bytes in editor
			model.setSize(Math.max(1, table.getRowCount()));
		}
	}


	public void tableChanged(TableModelEvent e) {
		syncRowCount();
	}


	/**
	 * Renders the cells of the row header.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class CellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;

		public CellRenderer() {
			setHorizontalAlignment(JLabel.RIGHT);
		}

		public Component getListCellRendererComponent(JList list, Object value,
							int index, boolean selected, boolean hasFocus) {
			// Never paint cells as "selected."
			super.getListCellRendererComponent(list, value, index,
												false, hasFocus);
			setBorder(CELL_BORDER);
//			setBackground(table.getBackground());
			return this;
		}

	}


	/**
	 * List model used by the header for the hex table.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private static class RowHeaderListModel extends AbstractListModel {

		private static final long serialVersionUID = 1L;

		private int size;

		public Object getElementAt(int index) {
			return "0x" + Integer.toHexString(index*16);
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			int old = this.size;
			this.size = size;
			int diff = size - old;
			if (diff>0) {
				fireIntervalAdded(this, old, size-1);
			}
			else if (diff<0) {
				fireIntervalRemoved(this, size+1, old-1);
			}
		}

	}


	/**
	 * Border for the entire row header.  This draws a line to separate the
	 * header from the table contents, and gives a small amount of whitespace
	 * to separate the two.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class RowHeaderBorder extends EmptyBorder {

		private static final long serialVersionUID = 1L;

		public RowHeaderBorder() {
			super(0,0,0,2);
		}

	    public void paintBorder(Component c, Graphics g, int x, int y,
	    						int width, int height) {
	    	x = x + width - this.right;
//	    	g.setColor(table.getBackground());
//	    	g.fillRect(x,y, width,height);
	    	g.setColor(table.getGridColor());
	    	g.drawLine(x,y, x,y+height);
	    }

	}


}