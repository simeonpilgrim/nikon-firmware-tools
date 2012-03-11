package org.fife.ui.hex.event;

import org.fife.ui.hex.swing.HexEditor;

import java.util.EventObject;


/**
 * An event that is fired when certain events occur in a hex editor.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class HexEditorEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	/**
	 * The offset of the change.
	 */
	private int offset;

	/**
	 * The number of bytes added.
	 */
	private int added;

	/**
	 * The number of bytes removed.
	 */
	private int removed;


	/**
	 * Creates a new event object.
	 *
	 * @param editor The source of this event.
	 * @param offs The offset at which bytes were added or removed.
	 * @param added The number of bytes added, or <code>0</code> for none.
	 * @param removed The number of bytes removed, or <code>0</code> for none.
	 */
	public HexEditorEvent(HexEditor editor, int offs, int added, int removed) {
		super(editor);
		this.offset = offs;
		this.added = added;
		this.removed = removed;
	}


	/**
	 * Returns the number of bytes added.  If this value equals the number
	 * of bytes removed, the bytes were actually modified.
	 *
	 * @return The number of bytes added.
	 * @see #getRemovedCount()
	 */
	public int getAddedCount() {
		return added;
	}


	/**
	 * Returns the hex editor that fired this event.
	 *
	 * @return The hex editor.
	 */
	public HexEditor getHexEditor() {
		return (HexEditor)getSource();
	}


	/**
	 * Returns the offset of the change.
	 *
	 * @return The offset of the change.
	 */
	public int getOffset() {
		return offset;
	}


	/**
	 * Returns the number of bytes removed.  If this value equals the number
	 * of bytes added, then the bytes were actually modified.
	 *
	 * @return The number of bytes removed.
	 * @see #getAddedCount()
	 */
	public int getRemovedCount() {
		return removed;
	}


	/**
	 * Returns whether this was a "modification" of bytes; that is, no bytes
	 * were added or removed, bytes were only modified.  This is equivalent
	 * to <code>getAddedCount() == getRemovedCount()</code>.
	 *
	 * @return Whether this is just a "modification" of bytes.
	 */
	public boolean isModification() {
		return getAddedCount()==getRemovedCount();
	}


}