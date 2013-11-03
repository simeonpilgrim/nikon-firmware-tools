/*
 * Copyright (c) 2011 Robert Futrell
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
package org.fife.ui.hex.event;

import java.util.EventObject;


/**
 * Occurs when the cell selection within the hex editor becomes changed.
 * 
 * @author PAX
 * @version 1.0
 */
public class SelectionChangedEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	/**
	 * The previous selection start index.
	 */
	private int previousSelecStart;

	/**
	 * The new selection start index.
	 */
	private int newSelecStart;

	/**
	 * The previous selection end index.
	 */
	private int previousSelecEnd;

	/**
	 * The new selection end index.
	 */
	private int newSelecEnd;


	/**
	 * Constructor.
	 * 
	 * @param source The instance which creates this event.
	 * @param previousSelecStart The previous selection start index.
	 * @param previousSelecEnd The previous selection end index.
	 * @param newSelecStart The new selection start index.
	 * @param newSelecEnd The new selection end index.
	 */
	public SelectionChangedEvent(Object source, int previousSelecStart,
			int previousSelecEnd, int newSelecStart, int newSelecEnd) {
		super(source);
		this.previousSelecStart = previousSelecStart;
		this.previousSelecEnd = previousSelecEnd;
		this.newSelecStart = newSelecStart;
		this.newSelecEnd = newSelecEnd;
	}


	/**
	 * @return The new selection end index.
	 */
	public int getNewSelecEnd() {
		return this.newSelecEnd;
	}


	/**
	 * @return The new selection start index.
	 */
	public int getNewSelecStart() {
		return this.newSelecStart;
	}


	/**
	 * @return The previous selection end index.
	 */
	public int getPreviousSelecEnd() {
		return this.previousSelecEnd;
	}


	/**
	 * @return The previous selection start index.
	 */
	public int getPreviousSelecStart() {
		return this.previousSelecStart;
	}


	public String toString() {
		StringBuffer result = new StringBuffer("Old selection: [");
		result.append(getPreviousSelecStart()).append(", ");
		result.append(getPreviousSelecEnd()).append("]; New selection: [");
		result.append(getNewSelecStart()).append(", ");
		result.append(getNewSelecEnd()).append(']');
		return result.toString();
	}


}