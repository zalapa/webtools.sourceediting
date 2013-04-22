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

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;

public class XMLFormatterFactory {

	private Map formatters;
	private IDOMNode node;	
	protected XMLFormatterStateTracker stateTracker;
	
	private final String STARTAG_FORMATTER = "stat_tag_formatter";
	private final String ENDTAG_FORMATTER = "end_tag_formatter";
	private final String CONTENT_FORMATTER = "content_formatter";
	private final String COMMENT_FORMATTER="comment_formatter";
	private final String UNDEFINED_FORMATTER="undefined_formatter";
	
	public XMLFormatterFactory(XMLFormatterStateTracker stateTracker, IDOMNode model){
		super();
		formatters = new HashMap();
		this.node = model;
		this.stateTracker = stateTracker;
	}
	
	private Object createFormatter(String formatterName){
		XMLFormatter formatter = null;
		if (STARTAG_FORMATTER.equals(formatterName))
			formatter = new StartTagFormatter(stateTracker, node);
		else if (ENDTAG_FORMATTER.equals(formatterName))
			formatter = new EndTagFormatter(stateTracker, node);
		else if (CONTENT_FORMATTER.equals(formatterName))
			formatter = new ContentFormatter(stateTracker);
		else if (COMMENT_FORMATTER.equals(formatterName))
			formatter = new CommentFormatter(stateTracker);
		else if (UNDEFINED_FORMATTER.equals(formatterName))
			formatter = new UndefinedFormatter(stateTracker);
		formatters.put(formatterName, formatter);	
		return formatter;
	}
	
	private Object getInstance(String formatterName){
		Object formatter = formatters.get(formatterName);
		if (formatter != null)
			return formatter;
		return createFormatter(formatterName);
	}
	
	public XMLFormatter getFormatter(IStructuredDocumentRegion targetRegion){
		String regionType = targetRegion.getType();
		Object formatter=null;
		if (DOMRegionContext.XML_TAG_NAME.equals(regionType)){
			 ITextRegion tagRegion=targetRegion.getFirstRegion();
			 if (tagRegion.getType().equals(DOMRegionContext.XML_TAG_OPEN)){
				 formatter = getInstance(STARTAG_FORMATTER);
			 }
			 else{
				 formatter = getInstance(ENDTAG_FORMATTER);
			 }
		}
		else if (DOMRegionContext.XML_CONTENT.equals(regionType))
				formatter = getInstance(CONTENT_FORMATTER);
		else if(DOMRegionContext.XML_COMMENT_TEXT.equals(regionType))
				formatter = getInstance(COMMENT_FORMATTER);
		else formatter = getInstance(UNDEFINED_FORMATTER);
		return (XMLFormatter)formatter;
		
	}
}
