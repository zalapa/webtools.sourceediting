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

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;

public class ContentFormatter extends TextFormatter {

	public ContentFormatter(XMLFormatterStateTracker state) {
		super(state);
	}

	protected void preFormat() {
		setup();
	}	
	
	protected void format() {						
		int whitespacesLength=formatText();
		if (whitespacesLength > 0){
			if (getWhiteSpaceStrategy().equals(XMLFormatterConstants.DEFAULT) || getWhiteSpaceStrategy().equals(XMLFormatterConstants.NONE)){
				preformatNextRegion(whitespacesLength);
			}
			else{
				formatRemainingWhiteSpacesWihtNoDefaultStrategy(whitespacesLength);
			}
		}
	}
	
	protected void postFormat() {
	}

	
	protected String getTextIndentation(boolean isLastWord) {
		return getRegionIndentation(DOMRegionContext.XML_CONTENT);
	}

	/**
	 * This functions does the same that the preformat() method of each formatter 
	 * @param whiteSpacesLength
	 */
	private void preformatNextRegion (int whitespacesLength){
		IStructuredDocumentRegion nextRegion = getCurrentRegion().getNext();
		if (nextRegion != null){
			if (nextRegion.getType().equals(DOMRegionContext.XML_TAG_NAME)){
				preformatTagRegion(nextRegion, whitespacesLength);
			}
			else{
				if (nextRegion.getType().equals(DOMRegionContext.XML_COMMENT_TEXT)){
					preformatCommentRegion(nextRegion, whitespacesLength);
				}
				else{
					if (nextRegion.getType().equals(DOMRegionContext.XML_ENTITY_REFERENCE) || nextRegion.getType().equals(DOMRegionContext.XML_CHAR_REFERENCE)){
						preformatReference(nextRegion, whitespacesLength);
					}
				}
			}
			stateTracker.setBooleanProperty(IXMLFormattingProperties.INDETATION_ADDED, true);
		}
		else{
			//The document ends with whitespaces, they must be formatted based on the WhiteSpaceStrategy defined.
			formatRemainingWhiteSpacesIncludingIndentation(whitespacesLength, "", false);
		}			
	}
	
	
	private void preformatTagRegion(IStructuredDocumentRegion nextRegion, int whitespacesLength){
		String indentation = new String();
		boolean needsNewLine = false;
		ITextRegion tagRegion=nextRegion.getFirstRegion();					
		if (tagRegion.getType().equals(DOMRegionContext.XML_TAG_OPEN)){ //Next is a startTAG
			needsNewLine = true;
			indentation = getRegionIndentation(DOMRegionContext.XML_TAG_OPEN); 
		}
		else{ //Next region is a ENDTAG
			ITextRegionList regionList = nextRegion.getRegions();
			ITextRegion textRegion = regionList.get(1);
			udpatedIndentLevel(DOMRegionContext.XML_END_TAG_OPEN,nextRegion.getText(textRegion));
			if (isInLineContent() && fitsInCurrentLine(nextRegion.getTextLength())){
				if (!clearNewLineDelimiters() && containsNewLineDelimiter(getTextToFormatLength()-whitespacesLength)){
						indentation = getRegionIndentation(DOMRegionContext.XML_END_TAG_OPEN);
				}
				else{
					if (whitespacesLength < getTextToFormatLength())
						indentation = XMLFormatterConstants.WITHESPACE;
					else
						indentation ="";
				}
			}
			else{
				needsNewLine = true;
				indentation = getRegionIndentation(DOMRegionContext.XML_END_TAG_OPEN);							
			}
		}
		formatRemainingWhiteSpacesIncludingIndentation(whitespacesLength, indentation, needsNewLine);			
		resetAvaliableLineWidht();
	}
	
	
	private void preformatCommentRegion(IStructuredDocumentRegion nextRegion, int whitespacesLength){
		if (shouldFormatComments() && containsNewLineDelimiter(getTextToFormatLength()-whitespacesLength)){
			resetAvaliableLineWidht();
			String indentation = getRegionIndentation(DOMRegionContext.XML_COMMENT_OPEN);
			formatRemainingWhiteSpacesIncludingIndentation(whitespacesLength, indentation, true);
		}
		
	}

	private void preformatReference(IStructuredDocumentRegion nextRegion, int whitespacesLength){
		int textOffset;
		if (whitespacesLength>0){ //before a reference, there are just whitespaces e.g.=>   &#ref;
			textOffset = getTextToFormatLength()-whitespacesLength;
			formatWhiteSpacesBeforeWord(textOffset, whitespacesLength, nextRegion.getTextLength(), clearNewLineDelimiters());
		}
	}

	protected boolean clearNewLineDelimiters() {
		return stateTracker.getBooleanProperty(IXMLFormattingProperties.CLEAR_BLANK_LINES);
	}

	protected boolean allowEntityReference() {
		return true;
	}
	
}
