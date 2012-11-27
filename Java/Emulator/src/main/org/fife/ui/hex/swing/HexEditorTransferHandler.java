package org.fife.ui.hex.swing;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


/**
 * The default transfer handler for <code>HexEditor</code>s.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class HexEditorTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;


	public boolean canImport(JComponent comp, DataFlavor[] flavors) {
		HexEditor editor = (HexEditor)comp;
		if (!editor.isEnabled()) {
			return false;
		}
		return getImportFlavor(flavors, editor)!=null;
	}


	protected Transferable createTransferable(JComponent c) {
		HexEditor e = (HexEditor)c;
		int start = e.getSmallestSelectionIndex();
		int end = e.getLargestSelectionIndex();
		byte[] array = new byte[end-start+1];
		for (int i=end; i>=start; i--) {
			array[i-start] = e.getByte(i);
		}
		ByteArrayTransferable bat = new ByteArrayTransferable(start, array);
		return bat;
	}


	protected void exportDone(JComponent source, Transferable data, int action){
		if (action==MOVE) {
			ByteArrayTransferable bat = (ByteArrayTransferable)data;
			int offs = bat.getOffset();
			HexEditor e = (HexEditor)source;
			e.removeBytes(offs, bat.getLength());
		}
	}


	private DataFlavor getImportFlavor(DataFlavor[] flavors, HexEditor e) {
		for (int i=0; i<flavors.length; i++) {
			if (flavors[i].equals(DataFlavor.stringFlavor)) {
				return flavors[i];
			}
		}
		return null;
	}


	/**
	 * Returns what operations can be done on a hex editor (copy and move, or
	 * just copy).
	 *
	 * @param c The <code>HexEditor</code>.
	 * @return The permitted operations.
	 */
	public int getSourceActions(JComponent c) {
		HexEditor e = (HexEditor)c;
		return e.isEnabled() ? COPY_OR_MOVE : COPY;
	}


	/**
	 * Imports data into a hex editor component.
	 *
	 * @param c The <code>HexEditor</code> component.
	 * @param t The data to be imported.
	 * @return Whether the data was successfully imported.
	 */
	public boolean importData(JComponent c, Transferable t) {

		HexEditor e = (HexEditor)c;
		boolean imported = false;

		DataFlavor flavor = getImportFlavor(t.getTransferDataFlavors(), e);
		if (flavor!=null) {
			try {
				Object data = t.getTransferData(flavor);
				if (flavor.equals(DataFlavor.stringFlavor)) {
					String text = (String)data;
					byte[] bytes = text.getBytes();
					e.replaceSelection(bytes);
				}
			} catch (UnsupportedFlavorException ufe) {
				ufe.printStackTrace(); // Never happens.
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		return imported;

	}


}