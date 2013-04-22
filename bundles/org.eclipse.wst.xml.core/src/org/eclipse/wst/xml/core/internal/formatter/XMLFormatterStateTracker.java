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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.wst.xml.core.internal.formatter.XMLFormattingPreferences;

/**
 * This class is used to access to the properties that define the state of the formatter.
 * 
 *
 */
class XMLFormatterStateTracker implements IXMLFormattingProperties {
	
	private Map propertiesMap;
	
	public XMLFormatterStateTracker(XMLFormattingPreferences preferences, int start, int length, String lineDelimiterValue){
		propertiesMap = new HashMap();
		
		//Preferences properties( these do not change during the formatting )
		setObjectProperty(IXMLFormattingProperties.INDENTATION_STRING, preferences.getOneIndent());
		setBooleanProperty(IXMLFormattingProperties.INDENT_MULTIPLE_ATTRIBUTES, preferences.getIndentMultipleAttributes());
		setIntegerProperty(IXMLFormattingProperties.LINE_WIDTH, preferences.getMaxLineWidth());
		setObjectProperty(IXMLFormattingProperties.LINE_DELIMITER, lineDelimiterValue);
		setBooleanProperty(IXMLFormattingProperties.ALIGN_FINAL_BRACKET, preferences.getAlignFinalBracket());
		setBooleanProperty(IXMLFormattingProperties.SPACE_BEFORE_EMPTY_CLOSE_TAG, preferences.getSpaceBeforeEmptyCloseTag());		setBooleanProperty(IXMLFormattingProperties.CLEAR_BLANK_LINES, preferences.getClearAllBlankLines());
		setBooleanProperty(IXMLFormattingProperties.FORMAT_COMMENT_TEXT, preferences.getFormatCommentText());
		setBooleanProperty(IXMLFormattingProperties.FORMAT_COMMENT_JOIN_LINES, preferences.getJoinCommentLines());
		setIntegerProperty(IXMLFormattingProperties.START, start);
		setIntegerProperty(IXMLFormattingProperties.LENGTH, length);
		//Dynamic properties
		setIntegerProperty(IXMLFormattingProperties.AVAILABLE_LINE_WIDHT, preferences.getMaxLineWidth());		
		setIntegerProperty(IXMLFormattingProperties.INDENT_LEVEL, 0);		
		setBooleanProperty(IXMLFormattingProperties.INLINE_CONTENT, false);
		setBooleanProperty(IXMLFormattingProperties.INDETATION_ADDED, false);
		setObjectProperty(IXMLFormattingProperties.OPEN_TAG_STACK, new OpenTagStack());
		setObjectProperty(IXMLFormattingProperties.REFERENCE_NODE, null);
		setIntegerProperty(IXMLFormattingProperties.CURRENT_TAG_SIZE, 0);
		setObjectProperty(IXMLFormattingProperties.WITHE_SPACE_TRACKER, new XMLFormattingWhitespacesTracker(preferences.getPCDataWhitespaceStrategy()));
	}

	public void setBooleanProperty(String propertyName, boolean value){		
		propertiesMap.put(propertyName,  Boolean.valueOf(value));
	}
	
	public void setIntegerProperty(String propertyName, int value){		
		propertiesMap.put(propertyName,  Integer.valueOf(value));
	}
	
	public void setObjectProperty(String propertyName, Object value){		
		propertiesMap.put(propertyName,  value);
	}
		
	public boolean getBooleanProperty(String property){
		Object entry = propertiesMap.get(property);
		if (entry == null)
			return false;
		return ((Boolean)entry).booleanValue();
	}
	
	public Object getObjectProperty(String property){
		Object entry = propertiesMap.get(property);
		return entry;
	}
	
	public int getIntProperty(String property){
		Object entry = propertiesMap.get(property);
		if (entry == null)
			return -1;
		return ((Integer)entry).intValue();
	}
	
	public String getStringProperty(String property){
		Object entry = propertiesMap.get(property);
		if (entry == null)
			return "";
		return (String)entry;
	}
}
