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

import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.formatter.XMLFormatterConstants;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
//import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;

public class EndTagFormatter extends TagFormatter {

	private long perfTime=0;
	public EndTagFormatter(XMLFormatterStateTracker state, IDOMNode node) {
		super(state, node);
	}

	protected void preFormat() {
		if (!stateTracker.getBooleanProperty(IXMLFormattingProperties.INDETATION_ADDED)){
			ITextRegionList regionList = getCurrentRegion().getRegions();
			ITextRegion textRegion = regionList.get(1);
			udpatedIndentLevel(DOMRegionContext.XML_END_TAG_OPEN,getCurrentRegion().getText(textRegion));
			if (!getWhiteSpaceTracker().getCurrentWithespaceStrategy().equals(XMLFormatterConstants.PRESERVE)){
				String indentation =  new String();
				if (isNewLineRequired() || !fitsInCurrentLine(stateTracker.getIntProperty(IXMLFormattingProperties.CURRENT_TAG_SIZE))){
					//TODO:newFunction
					indentation += getNewLineDelimiter(); //AddnewLinesMethod()
					indentation += getRegionIndentation(DOMRegionContext.XML_END_TAG_OPEN);
					resetAvaliableLineWidht();            //AddnewLinesMethod()
					
					setNewLineRequired(false);            //AddnewLinesMethod()
				}
				else{
					if (!isInLineContent())
						indentation += getRegionIndentation(DOMRegionContext.XML_END_TAG_OPEN);
				}
				addInsetOperation(getCurrentRegion().getStart(), indentation);
			}
		}
	}

	protected void format() {
		ITextRegionList regionList = getCurrentRegion().getRegions();
		int textRegionIndex = 0;
		ITextRegion textRegion = regionList.get(textRegionIndex);
		textRegion = regionList.get(++textRegionIndex);
		if (DOMRegionContext.XML_TAG_NAME.equals(textRegion.getType())){
			removeWhiteSpaces(textRegion);
			int tagNameLength = textRegion.getTextLength();
			updateAvailableLineWidht(tagNameLength+3);
		}
		
	}

	protected void postFormat() {
		long temp = System.currentTimeMillis();
		getWhiteSpaceTracker().untrackWhitespaceStrategy(getCurrentRegion(), getStucturedModel().getModel());
		temp = System.currentTimeMillis() - temp;
		perfTime+=temp;
		temp = perfTime;
		setInLineContent(false);					
		setNewLineRequired(true);
		stateTracker.setBooleanProperty(IXMLFormattingProperties.INDETATION_ADDED, false);
	}

}
