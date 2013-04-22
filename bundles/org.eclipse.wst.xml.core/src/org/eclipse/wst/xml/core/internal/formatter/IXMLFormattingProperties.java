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

public interface IXMLFormattingProperties {
	//int properties
	String INDENT_LEVEL="indentLevel";
	String LINE_WIDTH = "lineWidht";
	String AVAILABLE_LINE_WIDHT = "availableLineWidth";
	String CURRENT_TAG_SIZE = "currentTagSize";
	String START = "start";
	String LENGTH = "length";
			
	//boolean properties	
	String INDENT_MULTIPLE_ATTRIBUTES = "indentMultipleAttributes";
	String INLINE_CONTENT = "inlineContent";
	String NEW_LINE_REQUIRED = "newLineRequired";
	String ALIGN_FINAL_BRACKET="alignFinalBracket";
	String SPACE_BEFORE_EMPTY_CLOSE_TAG = "spaceBeforeEmptyCloseTag";
	String CLEAR_BLANK_LINES ="clearBlankLines";
	String INDETATION_ADDED = "indentationAdded";	
    /**
     * Indicates whether or not the content of comments should be formatted
     * <p>
     * Value is of type <code>boolean</code><br />
     * Possible values: {TRUE, FALSE}
     * </p>
     */
    public static final String FORMAT_COMMENT_TEXT = "formatCommentText"; //$NON-NLS-1$

    /**
     * Indicates whether or not the lines of comments should be joined when formatting
     * <p>
     * Value is of type <code>boolean</code>
     */
    public static final String FORMAT_COMMENT_JOIN_LINES = "formatCommentJoinLines"; //$NON-NLS-1$
    
	//string properties
	String INDENTATION_STRING = "indentationString";
	String LINE_DELIMITER = "lineDelimiter";	
	
	//Object properties	
	String WITHE_SPACE_TRACKER = "whiteSpaceTracker";
	String OPEN_TAG_STACK= "openTagStack";
	String REFERENCE_NODE= "referenceNode";
}

