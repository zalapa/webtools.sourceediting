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

import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;

public abstract class XMLFormatter {
		
	
	protected XMLFormatterStateTracker stateTracker;
	
	private IStructuredDocumentRegion currentRegion;
	
	private MultiTextEdit currentRegionTextEdit;
	
	protected abstract void preFormat();
	
	protected abstract void format();
	
	protected abstract void postFormat();
	
	protected XMLFormatter(XMLFormatterStateTracker state){
		this.stateTracker = state;
		this.currentRegionTextEdit = new MultiTextEdit();  
	}
	
	
	public TextEdit[] formatRegion(IStructuredDocumentRegion currentRegion){		
		setup(currentRegion);
		preFormat();
		format();
		postFormat();
		return currentRegionTextEdit.removeChildren();		
	}
	
	protected String getRegionIndentation(String regionType){
		int indentationLevel =  stateTracker.getIntProperty(IXMLFormattingProperties.INDENT_LEVEL);
		if (regionType.equals(DOMRegionContext.XML_TAG_OPEN) || regionType.equals(DOMRegionContext.XML_CONTENT) || regionType.equals(DOMRegionContext.XML_COMMENT_OPEN) || regionType.equals(DOMRegionContext.XML_END_TAG_OPEN)){
			return getIndentationString(indentationLevel);
		}
		else if (regionType.equals(DOMRegionContext.XML_ATTLIST_DECLARATION) || regionType.equals(DOMRegionContext.XML_COMMENT_TEXT)){
			return getIndentationString(indentationLevel+1);
		}	
		return new String("");
	}
	
	protected void updateAvailableLineWidht(int length){
		int currentAvailableLineWidht = stateTracker.getIntProperty(IXMLFormattingProperties.AVAILABLE_LINE_WIDHT);
		stateTracker.setIntegerProperty(IXMLFormattingProperties.AVAILABLE_LINE_WIDHT, currentAvailableLineWidht-length);
	}
	
	protected void resetAvaliableLineWidht(){
		int lineWidht = stateTracker.getIntProperty(IXMLFormattingProperties.LINE_WIDTH);;
		stateTracker.setIntegerProperty(IXMLFormattingProperties.AVAILABLE_LINE_WIDHT, lineWidht);
	}
	
	protected boolean fitsInCurrentLine(int length){
		int currentAvailableLineWidht = stateTracker.getIntProperty(IXMLFormattingProperties.AVAILABLE_LINE_WIDHT);
		return length > currentAvailableLineWidht ? false : true;
	}
	
	private String getIndentationString(int indentLevel){
		String indentationValue=""; 		
		String indentValue = stateTracker.getStringProperty(IXMLFormattingProperties.INDENTATION_STRING);;		
		while(indentLevel>0){			
			indentationValue+=indentValue;
			--indentLevel;
		}
		return indentationValue;		
	}
	
	protected String getNewLineDelimiter(){
		return stateTracker.getStringProperty(IXMLFormattingProperties.LINE_DELIMITER);
	}
	
	protected XMLFormattingWhitespacesTracker getWhiteSpaceTracker() {
		return (XMLFormattingWhitespacesTracker) stateTracker.getObjectProperty(IXMLFormattingProperties.WITHE_SPACE_TRACKER);
	}
	
	protected void setNewLineRequired(boolean value){
		stateTracker.setBooleanProperty(IXMLFormattingProperties.NEW_LINE_REQUIRED, value);
	}

	protected boolean isNewLineRequired(){
		return stateTracker.getBooleanProperty(IXMLFormattingProperties.NEW_LINE_REQUIRED);
	}
	
	public IStructuredDocumentRegion getCurrentRegion() {
		return currentRegion;
	}

	protected void setup(IStructuredDocumentRegion currentRegion) {
		this.currentRegion = currentRegion;
	}
		
	protected boolean isAtBeginningOfNewLine(){
		int lineWidht = stateTracker.getIntProperty(IXMLFormattingProperties.LINE_WIDTH);
		int availableLineWidht = stateTracker.getIntProperty(IXMLFormattingProperties.AVAILABLE_LINE_WIDHT);
		return lineWidht == availableLineWidht;
	}
	
	protected void setInLineContent(boolean isInLineContent){
		stateTracker.setBooleanProperty(IXMLFormattingProperties.INLINE_CONTENT, isInLineContent);
	}
	
	protected boolean isInLineContent(){
		return stateTracker.getBooleanProperty(IXMLFormattingProperties.INLINE_CONTENT);
	}
	
	protected void addText(int offset, String text){
		addEditOperation (offset,text,0);
	}
	
	protected void deleteText(int offset, int length){
		addEditOperation (offset,null,length);
	}

	private boolean shouldApplyEdit(int offset){
		int start = stateTracker.getIntProperty(IXMLFormattingProperties.START);
		int length = stateTracker.getIntProperty(IXMLFormattingProperties.LENGTH);
		return offset >= start && offset < start + length;    
	}
	
	protected void addReplaceOperation(int offset, int length, String text){
		if (shouldApplyEdit(offset)){
			ReplaceEdit child = new ReplaceEdit(offset, length, text);
			currentRegionTextEdit.addChild(child);
		}
	}
	
	protected void addInsetOperation(int offset, String text){
		if (shouldApplyEdit(offset) && text.length() > 0){
			InsertEdit child = new InsertEdit(offset, text);
			currentRegionTextEdit.addChild(child);
		}
	}
	
	private void addEditOperation(int offset, String text, int length){
		if (shouldApplyEdit(offset)){
			TextEdit newEdit = null;
			boolean isAddOperation = (text == null)?false:true;
			boolean shouldCreateEdit = true;			
			TextEdit edits []= currentRegionTextEdit.getChildren();
			for(int i=0;i<edits.length;i++){
				TextEdit currentEdit = edits[i];
				int lastModificationSize=currentEdit.getOffset()+currentEdit.getLength();
				int currentModificationSize = offset + length;
				boolean sameModificationSize = (lastModificationSize == currentModificationSize)?true:false;
				if (isAddOperation && currentEdit instanceof DeleteEdit && sameModificationSize){
					int anchor = currentEdit.getOffset()-getCurrentRegion().getStartOffset();
					String stringToRemove = getCurrentRegion().getFullText().substring(anchor,anchor+currentEdit.getLength());
					if (stringToRemove.startsWith(text)){
						currentRegionTextEdit.removeChild(currentEdit);
						int newDeleteLength = currentEdit.getLength() - text.length();
						if (newDeleteLength > 0){
							int newDeleteOffset = currentEdit.getOffset()+text.length();
							newEdit = new DeleteEdit(newDeleteOffset, newDeleteLength);
							currentRegionTextEdit.addChild(newEdit);
						}
						shouldCreateEdit = false;
						break;
					}
				}
			}
			if (edits.length < 0 || shouldCreateEdit){
				if (isAddOperation){
					newEdit = new InsertEdit(offset, text);
				}
				else
					newEdit = new DeleteEdit(offset, length);
			}
			if (newEdit != null && shouldCreateEdit){
				currentRegionTextEdit.addChild(newEdit);
			}
		}
	}
	
	protected void udpatedIndentLevel(String regionType, String tagName){		
		int indentationLevel = stateTracker.getIntProperty(IXMLFormattingProperties.INDENT_LEVEL);
		boolean isTagOpen = DOMRegionContext.XML_TAG_OPEN.equals(regionType)?true:false;
		OpenTagStack openTagStack = (OpenTagStack)stateTracker.getObjectProperty(IXMLFormattingProperties.OPEN_TAG_STACK);
	 	if (isTagOpen){	
	 			openTagStack.addOpenTag(tagName);
	 			indentationLevel++;
	 	}
	 	else{
	 		int indentDiference =openTagStack.getOpenTagIndex(tagName);
	 		indentationLevel-=indentDiference;
	 	}
	 	stateTracker.setIntegerProperty(IXMLFormattingProperties.INDENT_LEVEL, indentationLevel);
	}
	
	
	protected boolean shouldFormatComments(){
		return stateTracker.getBooleanProperty(IXMLFormattingProperties.FORMAT_COMMENT_TEXT);
	}
}
