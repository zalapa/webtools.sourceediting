/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.wst.xml.core.internal.formatter;

import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;

public class CommentFormatter extends TextFormatter {

	public CommentFormatter(XMLFormatterStateTracker state) {
		super(state);
	}

	protected void preFormat() {
		setup();
	}

	protected void format() {
		if (shouldFormatComments()){
			formatText();
		}

	}

	protected void postFormat() {		
		stateTracker.setBooleanProperty(IXMLFormattingProperties.INDETATION_ADDED, false);
		if (getWhiteSpaceStrategy().equals(XMLFormatterConstants.PRESERVE))
			setNewLineRequired(false);
		else
			setNewLineRequired(true);
	}

	protected boolean clearNewLineDelimiters() {
		
		return stateTracker.getBooleanProperty(IXMLFormattingProperties.FORMAT_COMMENT_JOIN_LINES);
	}

	protected String getTextIndentation(boolean isLastWord) {
		if(isLastWord)
			return getRegionIndentation(DOMRegionContext.XML_COMMENT_OPEN);
		return getRegionIndentation(DOMRegionContext.XML_COMMENT_TEXT);
	}

	protected boolean allowEntityReference() {
		return false;
	}

	protected boolean isInLineContent() {
		return true;
	}
}
