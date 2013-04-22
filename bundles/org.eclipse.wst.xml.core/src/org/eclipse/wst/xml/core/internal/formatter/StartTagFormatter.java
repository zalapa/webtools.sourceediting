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
import org.eclipse.wst.xml.core.internal.formatter.XMLFormatterConstants;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.w3c.dom.Node;


public class StartTagFormatter extends TagFormatter {
	
	protected StartTagFormatter(XMLFormatterStateTracker state, IDOMNode node) {
		super(state, node);
	}

	protected void preFormat() {
		setInLineContent(false);
		if (!stateTracker.getBooleanProperty(IXMLFormattingProperties.INDETATION_ADDED)){
			if (!getWhiteSpaceTracker().getCurrentWithespaceStrategy().equals(XMLFormatterConstants.PRESERVE)) {
				String indentation =  new String();
				if (isNewLineRequired()) {
					indentation += getNewLineDelimiter();
					resetAvaliableLineWidht();
					setNewLineRequired(false);
				}				
				if (isAtBeginningOfNewLine()) {
					indentation += getRegionIndentation(DOMRegionContext.XML_TAG_OPEN);
				}
				addInsetOperation(getCurrentRegion().getStart(), indentation);
				
			}
		}
	}

	protected void format() {
		setDOMNode();
		ITextRegionList regionList = getCurrentRegion().getRegions();
		int textRegionIndex = 0;
		ITextRegion textRegion = regionList.get(textRegionIndex++);
		textRegion = regionList.get(textRegionIndex++);
		String tagName = getCurrentRegion().getText(textRegion);
		removeWhiteSpaces(textRegion);
		int tagNameLength = textRegion.getTextLength();
		updateAvailableLineWidht(tagNameLength);
		textRegion = regionList.get(textRegionIndex++);

		// Looking for ATTRIBUTES
		boolean newLineAdded = false;
		boolean hasOnlyOneAttribute = true;
		while (textRegion != null && !DOMRegionContext.XML_TAG_CLOSE.equals(textRegion.getType()) && !DOMRegionContext.XML_EMPTY_TAG_CLOSE.equals(textRegion.getType())) {
			int attributeLength = 0;
			int attributeOffset = textRegion.getStart();
			if (DOMRegionContext.XML_TAG_ATTRIBUTE_NAME.equals(textRegion.getType())) {
				removeWhiteSpaces(textRegion);
				attributeLength = textRegion.getTextLength();
				textRegion = regionList.get(textRegionIndex++);
				if (textRegion != null && DOMRegionContext.XML_TAG_ATTRIBUTE_EQUALS.equals(textRegion.getType())) {
					removeWhiteSpaces(textRegion);
					attributeLength += textRegion.getTextLength();
					textRegion = regionList.get(textRegionIndex++);
					if (textRegion != null	&& DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(textRegion.getType())) {
						removeWhiteSpaces(textRegion);
						attributeLength += textRegion.getTextLength();
					}
				}
			}
			// ATTRIBUTE FOUND
			if (attributeLength > 0) {
				if (hasOnlyOneAttribute){
					ITextRegion nextRegionRegion = regionList.get(textRegionIndex);
					if (!nextRegionRegion.getType().equals(DOMRegionContext.XML_TAG_CLOSE) && !nextRegionRegion.getType().equals(DOMRegionContext.XML_EMPTY_TAG_CLOSE)){
						hasOnlyOneAttribute = false;
					}
				}
				int regionOffset = getCurrentRegion().getStartOffset();
				newLineAdded = formatAttribute(regionOffset+attributeOffset, attributeLength,hasOnlyOneAttribute);
			}
			// SOME UNDEFINED content was found
			if (textRegion != null && DOMRegionContext.UNDEFINED.equals(textRegion.getType())) {
				// TODO: implement this part
			}
			textRegion = regionList.get(textRegionIndex++);
		}
		// XML_TAG_CLOSE or XML_EMPTY_TAG_CLOSE reached
		if (textRegion != null) {
			// XML_TAG_CLOSE
			if (DOMRegionContext.XML_TAG_CLOSE.equals(textRegion.getType())) {
				formatTagClose(textRegion, tagNameLength, newLineAdded, tagName);
				updateAvailableLineWidht(textRegion.getLength());
				udpatedIndentLevel(DOMRegionContext.XML_TAG_OPEN,tagName); // update the indentation level since the start tag is properly found
				getWhiteSpaceTracker().trackWhitespaceStrategy(node);
			}
			// XML_EMPTY_TAG_CLOSE
			else {
				formatEmptyTagClose(textRegion);
			}
		}

	}

	private void setDOMNode() {
		IDOMNode referenceNode = (IDOMNode) stateTracker.getObjectProperty(IXMLFormattingProperties.REFERENCE_NODE);
		Node currentNode=null;
		if (referenceNode == null){ //the current region is the rootNode
			currentNode=getCurrentDOMNode();
		}
		else{
			currentNode = findNextElementNode(referenceNode, true);
		}
		this.node = (IDOMNode)currentNode; 
		stateTracker.setObjectProperty(IXMLFormattingProperties.REFERENCE_NODE, currentNode);
	}

	private Node getNextElementNode(Node child){
		while(child != null){
			if (child.getNodeType() == Node.ELEMENT_NODE){
				break;
			}
			child = child.getNextSibling();
		}
		return child;
	}

	private Node findNextElementNode(Node referenceNode, boolean lookInChildren){
		Node currentNode=null;
		if (lookInChildren && referenceNode.hasChildNodes()){
			currentNode=getNextElementNode(referenceNode.getFirstChild());
		}
		if (currentNode == null){				
			currentNode=getNextElementNode(referenceNode.getNextSibling());
		}
		if (currentNode != null){
			return currentNode;
		}
		else{
			return findNextElementNode(referenceNode.getParentNode(),false);
		}
	}
	
	protected void postFormat() {
		stateTracker.setBooleanProperty(IXMLFormattingProperties.INDETATION_ADDED, false);
	}

	private boolean formatAttribute(int currentAttributeOffset, int length, boolean hasOnlyOneAttribute) {
		// SPLIT ATTRIBUTE due to: PREFERENCE SETTINGS or CANNOT BE CONTAINED IN THE SAME LINE
		if ((isSplitMultipleAttributes() && !hasOnlyOneAttribute) || !fitsInCurrentLine(length + 1)) {
			String indentation = getNewLineDelimiter()+getRegionIndentation(DOMRegionContext.XML_ATTLIST_DECLARATION);
			addText(currentAttributeOffset, indentation);
			resetAvaliableLineWidht();
			updateAvailableLineWidht(length);
			return true;
		}
		// FITS IN CURRENT LINE
		addText(currentAttributeOffset, XMLFormatterConstants.WITHESPACE);
		updateAvailableLineWidht(length + 1);
		return false;
	}

	private void formatTagClose(ITextRegion closeTagRegion, int tagNameLength, boolean newLineAdded, String tagName) {
		if (isAlignFinalBracket() && newLineAdded) {
			int offset = getCurrentRegion().getStartOffset()+closeTagRegion.getStart();
			String indentation = getNewLineDelimiter()+getRegionIndentation(DOMRegionContext.XML_TAG_OPEN);
			addText(offset, indentation);
			resetAvaliableLineWidht();
			updateAvailableLineWidht(1); // consider the whitespace
		}
		if (hasInLineContent(tagName)) {
			stateTracker.setIntegerProperty(IXMLFormattingProperties.CURRENT_TAG_SIZE, tagNameLength + 3);// +3 means = '<' '/' and '>' characters of and ENDTAG
			setInLineContent(true);
			setNewLineRequired(false); //if the tag formatter do not add the new line this flag is not being turned off.
		} else {
			setNewLineRequired(true);
			setInLineContent(false);
		}
	}

	/**
	 * Gets the preference boolean value of
	 * "Align final bracket in multiple-line element tags"
	 * 
	 * @return
	 */
	private boolean isAlignFinalBracket() {
		return stateTracker.getBooleanProperty(IXMLFormattingProperties.ALIGN_FINAL_BRACKET);
	}
	
	private boolean hasInLineContent(String tagName) {
		IStructuredDocumentRegion nextRegion= getCurrentRegion().getNext();
		while (nextRegion != null){
			String nextRegionType = nextRegion.getType();
			if (DOMRegionContext.XML_TAG_NAME.equals(nextRegionType)){
				 ITextRegionList regionList = nextRegion.getRegions();
				 ITextRegion textRegion = regionList.get(0);
				 if (textRegion.getType().equals(DOMRegionContext.XML_TAG_OPEN)){
					 return false;
				 }
				 else{
					 textRegion = regionList.get(1);
					 if (nextRegion.getText(textRegion).equals(tagName)){
						 return true;
					 }
					 else{
						 return false;
					 }
				 }
			}
			else{
				if (DOMRegionContext.XML_ENTITY_REFERENCE.equals(nextRegionType) || DOMRegionContext.XML_CHAR_REFERENCE.equals(nextRegionType) || DOMRegionContext.XML_CONTENT.equals(nextRegionType) || DOMRegionContext.XML_CDATA_TEXT.equals(nextRegionType)){
					nextRegion = nextRegion.getNext();
				}
				else{
					return false;
				}
			}
		}
		return false; //there is not end tag
	}

	private void formatEmptyTagClose(ITextRegion textRegion) {
		if (isSpaceBeforeEmptyCloseTag()) {
			int offset = getCurrentRegion().getStartOffset() + textRegion.getStart(); 
			addText(offset, XMLFormatterConstants.WITHESPACE);
		}
		setNewLineRequired(true);
	}
	/**
	 * Gets the preference option value
	 * 
	 * @return Value of "Insert whitespace before before closing empty end-tags"
	 *         option
	 */
	private boolean isSpaceBeforeEmptyCloseTag() {
		return stateTracker.getBooleanProperty(IXMLFormattingProperties.SPACE_BEFORE_EMPTY_CLOSE_TAG);
	}
	
	/**
	 * Gets the preference option value
	 * 
	 * @return Value of "Split multiple attributes each on a new line"
	 */
	private boolean isSplitMultipleAttributes() {
		return stateTracker.getBooleanProperty(IXMLFormattingProperties.INDENT_MULTIPLE_ATTRIBUTES);
	}
}
