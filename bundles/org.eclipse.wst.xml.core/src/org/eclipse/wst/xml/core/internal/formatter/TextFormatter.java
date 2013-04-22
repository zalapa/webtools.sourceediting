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
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;

public abstract class TextFormatter extends XMLFormatter {

	protected String witheSpaceStrategy;
	private char [] textToFormat;
	private char [] newLineDelimiter = getNewLineDelimiter().toCharArray();
	
	protected int getTextToFormatLength(){
		return textToFormat.length;
	}
	
	protected abstract boolean clearNewLineDelimiters();
	
	protected abstract String getTextIndentation(boolean isLastword);
	
	protected abstract boolean allowEntityReference();
	
	protected int formatText(){
		if (getWhiteSpaceStrategy().equals(XMLFormatterConstants.DEFAULT) || getWhiteSpaceStrategy().equals(XMLFormatterConstants.NONE)){
			return formatText(0,true,clearNewLineDelimiters());
		}
		else if (getWhiteSpaceStrategy().equals(XMLFormatterConstants.REPLACE)){
				return formatText(0,false, true);
			 }
			else if (getWhiteSpaceStrategy().equals(XMLFormatterConstants.COLLAPSE)){
				return formatTextWithCollapseStrategy();
				}
				else if (getWhiteSpaceStrategy().equals(XMLFormatterConstants.PRESERVE)){
					return formatTextWithPreserveStrategy();
				}
		return 0;
	}
	
	private int formatTextWithPreserveStrategy(){
		int index=0;
		int indexOfLastNewLine = 0;
		while(index<getTextToFormatLength()){
			if (Character.isWhitespace(textToFormat[index]) && isNewLineDelimiter(textToFormat, index)){
					indexOfLastNewLine = index;
					index+=newLineDelimiter.length;
			}
			else{
				index++;
			}
		}
		int lengthOfLastLine = 0;
		if (indexOfLastNewLine > 0){
			lengthOfLastLine = getTextToFormatLength() - (indexOfLastNewLine + newLineDelimiter.length);
		}
		else{
			lengthOfLastLine = getTextToFormatLength();
		}
		updateAvailableLineWidht(lengthOfLastLine);
		return 0;
	}
	
	private int formatTextWithCollapseStrategy(){
		int offset=0;
		while(Character.isWhitespace(textToFormat[offset])){
			offset++;
		}
		if (offset != 0 ){
			addReplaceOperation(getCurrentRegion().getStartOffset(), offset, "");
		}
		return formatText(offset, true, true);
	}
	
	protected void formatRemainingWhiteSpacesIncludingIndentation(int whiteSpacesLength, String indentation, boolean newLine){
		if (getWhiteSpaceStrategy().equals(XMLFormatterConstants.DEFAULT) || getWhiteSpaceStrategy().equals(XMLFormatterConstants.NONE)){
			formatWhiteSpacesWithDefaulStrategyTE(whiteSpacesLength, indentation, newLine,clearNewLineDelimiters());
		}
	}
	
	protected boolean containsNewLineDelimiter(int startOffset){
		while(startOffset < textToFormat.length){
			if(isNewLineDelimiter(textToFormat, startOffset))
				return true;
			startOffset++;
		}
		return false;
	}
	
	protected void formatWhiteSpacesWithDefaulStrategyTE(int whiteSpacesLength, String indentation, boolean newLine, boolean clearBlankLines){		
		String proposedPreTagContent;
		if (newLine)
			proposedPreTagContent = getNewLineDelimiter()+indentation;
		else
			proposedPreTagContent = indentation;
		char proposedText []= proposedPreTagContent.toCharArray();
		//Can be correct. Maybe it does not need anything.
		boolean isValid= true;
		int newLineCount = 0;
		if (whiteSpacesLength == proposedText.length){
			int offset = getTextToFormatLength() - whiteSpacesLength;
			for (int i = 0 ; i < whiteSpacesLength ;i++){
				if(proposedText[i] != textToFormat[offset+i]){
					isValid = false;
					break;
				}
			}
		}
		//May add new edit
		else{
			if (whiteSpacesLength < proposedText.length){ //something is missing so the full region must be replaced
				isValid = false;
			}
			else{ //If there are just whithespace it is posible to be ok, it depends on the clearblank flag 
				int offset = getTextToFormatLength() - whiteSpacesLength;
				isValid = true;
				while(offset < textToFormat.length){
					if (isNewLineDelimiter(textToFormat, offset)){
						if (clearBlankLines){
							isValid= false;
							break;							
						}
						newLineCount++;
						resetAvaliableLineWidht();
						offset+=newLineDelimiter.length;
						if (offset == textToFormat.length && indentation.length() > 0){ //if ends in newLineDelimiter
							isValid=false;
						}
					}
					else{						
						if (!isValid){
							offset++;
						}
						else{
							int textIndex=0;
							char indentationArray[] = indentation.toCharArray();
							while (textIndex < indentationArray.length && isValid){
								if (offset+textIndex < textToFormat.length){
									if (textToFormat[offset+textIndex] != indentationArray[textIndex]){
										isValid = false;
									}
								}
								else{
									isValid =false;
								}
								textIndex++;
							}
							offset+=textIndex;
							if (isValid && offset < getTextToFormatLength())
								isValid=false;
						}
					}
				}			
			}
		}
		if (!isValid){ //Replace the Region
			int starOffset = getCurrentRegion().getStart() + (getTextToFormatLength() - whiteSpacesLength);
			int length = whiteSpacesLength;
			String newIndentation = new String();
			if (newLineCount > 0 && newLine){
				newLineCount--;
			}
			while (newLineCount > 0){
				newIndentation+=getNewLineDelimiter();
				newLineCount--;
			}
			newIndentation+=proposedPreTagContent;
			addReplaceOperation(starOffset, length, newIndentation);
		}
	}
	
	private int getNextEntityLength(){
		IStructuredDocumentRegion nextRegion = getCurrentRegion().getNext();
		int referenceLength = 0;
		if (nextRegion != null && (nextRegion.getType().equals(DOMRegionContext.XML_ENTITY_REFERENCE) || nextRegion.getType().equals(DOMRegionContext.XML_CHAR_REFERENCE))){
			referenceLength = nextRegion.getTextLength();
			stateTracker.setBooleanProperty(IXMLFormattingProperties.INDETATION_ADDED, true);
		}
		return referenceLength;
	}
	
	protected void formatWhiteSpacesBeforeWord(int textIndex, int whiteSpacesLength, int worldLength, boolean clearBlankLines){
		boolean isValid=true;
		int topIndex = textIndex + whiteSpacesLength;
		boolean isFirstWord = textIndex == 0 ?true:false;
		boolean isLastWord = (topIndex + worldLength) == textToFormat.length?true:false;
		if (isLastWord && allowEntityReference()){
			worldLength+=getNextEntityLength();
		}
		worldLength += (isFirstWord)?0:1;
		int startOffset = textIndex;
		int newLineCount = 0;
		String proposedWhiteSpacesString = new String();
		String indentation = getTextIndentation(isLastWord);
		while (textIndex < topIndex){
			if (isNewLineDelimiter(textToFormat, textIndex)){
				if (clearBlankLines && fitsInCurrentLine(worldLength) ){//&& !isInLineContent()){
					if (!isInLineContent()){
						if (!isFirstWord){
							isValid=false;
							break;
						}
					}
					else{
						isValid=false;
						break;
					}
				}
				resetAvaliableLineWidht();
				textIndex+=newLineDelimiter.length;
				newLineCount++;
				if (textIndex == topIndex){ //if ends in newLineDelimiter
					isValid=false;
				}
			}
			else{
				if (!isValid){
					textIndex++;
				}
				else{
					if (isAtBeginningOfNewLine()){
						char indentationArray[] = indentation.toCharArray();
						int indentationIndex =0;
						while(indentationIndex < indentationArray.length && isValid){
							if (indentationArray[indentationIndex] != textToFormat[textIndex + indentationIndex]){
								isValid=false;break;
							}
							indentationIndex++;
						}
						textIndex +=indentationIndex;
						if (isValid && textIndex + indentationIndex < topIndex){
							isValid=false;
						}
					}
					else{
						if (!fitsInCurrentLine(worldLength) || (isFirstWord && !isInLineContent()) ){
							isValid=false;
							resetAvaliableLineWidht();
							newLineCount++;
						}
						else
							if (whiteSpacesLength > 1 || textToFormat[textIndex] != XMLFormatterConstants.WITHESPACE_CHAR){
								isValid=false;
							}
						textIndex++;
					}
				}
			}	
		}		
		if (!isValid){
			if (newLineCount>0){			
				while (newLineCount > 0){
					proposedWhiteSpacesString+=getNewLineDelimiter();
					newLineCount--;
				}
				proposedWhiteSpacesString+=indentation;
			}
			else{
				proposedWhiteSpacesString=XMLFormatterConstants.WITHESPACE;
			}
			startOffset = getCurrentRegion().getStart() + startOffset;
			addReplaceOperation(startOffset, whiteSpacesLength, proposedWhiteSpacesString);
		}
		updateAvailableLineWidht(worldLength);
	}

	private int formatText(int textIndex, boolean isDefaultWhiteSpaceStrategy, boolean clearBlankLines){				
		int wordLength = 0;
		int whiteSpacesLength = 0;
		while (textIndex < textToFormat.length){
			boolean isWhiteSpace = Character.isWhitespace(textToFormat[textIndex]);
			if (!isWhiteSpace){
				wordLength++;
			}			
			if (isWhiteSpace || textIndex+1 == textToFormat.length){
				if (wordLength > 0){
					int startWhiteSpacesOffset = textIndex - wordLength - whiteSpacesLength; 
					if (!isWhiteSpace){
						startWhiteSpacesOffset++;
					}
					if (isDefaultWhiteSpaceStrategy){
						if (whiteSpacesLength>0){
							formatWhiteSpacesBeforeWord(startWhiteSpacesOffset, whiteSpacesLength, wordLength, clearBlankLines);
						}
						else{
							if ((!fitsInCurrentLine(wordLength) || !isInLineContent()) && !isPreviousRegionText()){
								resetAvaliableLineWidht();
								int offset = getCurrentRegion().getStartOffset()+startWhiteSpacesOffset;
								addInsetOperation(offset, getNewLineDelimiter()+getTextIndentation(true));								
							}
							updateAvailableLineWidht(wordLength);
						}
						
					}
					else{
						formatWhiteSpacesWithReplaceStrategy(startWhiteSpacesOffset, whiteSpacesLength);
					}
//					updateAvailableLineWidht(wordLength);
					whiteSpacesLength = 0;
					wordLength=0;
				}
				if (isWhiteSpace)
					whiteSpacesLength++;
			}			
			textIndex++;
		}
		return whiteSpacesLength;
	}
	
	private boolean isPreviousRegionText(){
		IStructuredDocumentRegion nextRegion = getCurrentRegion().getPrevious();
		return (nextRegion != null && nextRegion.getType().equals(DOMRegionContext.XML_CONTENT));
	}
	
	/**
	 * Initialize the text to format and the Whitespace strategy to implement.
	 */
	protected void setup(){		
		this.textToFormat = getCurrentRegion().getFullText().toCharArray();
		setWhiteSpaceStrategy(getWhiteSpaceTracker().getCurrentWithespaceStrategy());
	}
	
	protected String getWhiteSpaceStrategy() {
		return witheSpaceStrategy;
	}

	
	protected void setWhiteSpaceStrategy(String whiteSpaceStrategy) {
		this.witheSpaceStrategy = whiteSpaceStrategy;
	}
	
	public TextFormatter(XMLFormatterStateTracker state) {
		super(state);
	}
	
	private void formatWhiteSpacesWithReplaceStrategy(int offset, int length){
		boolean isValid = true;
		int regionOffset = getCurrentRegion().getStartOffset()+offset;
		int topOffset = offset+length;
		while (offset < topOffset && isValid){
			if (textToFormat[offset] != XMLFormatterConstants.WITHESPACE_CHAR){
				isValid = false;
			}
			offset++;
		}
		if (!isValid){
			String whiteSpaceString="";
			for (int i=0;i<length;i++){
				whiteSpaceString+=XMLFormatterConstants.WITHESPACE;
			}
			addReplaceOperation(regionOffset, length, whiteSpaceString);
		}
	}
	
	protected void formatRemainingWhiteSpacesWihtNoDefaultStrategy(int length){
		if (!getWhiteSpaceStrategy().equals(XMLFormatterConstants.PRESERVE)){			
			if (getWhiteSpaceStrategy().equals(XMLFormatterConstants.REPLACE)){
				int startWhiteSpacesOffset = getTextToFormatLength() - length;
				formatWhiteSpacesWithReplaceStrategy(startWhiteSpacesOffset, length);
			}
			else{//it must be collapse strategy, so just delete them.
				addReplaceOperation(getCurrentRegion().getEndOffset()-length, length, "");
			}
		}
	}
	
	private boolean isNewLineDelimiter(char textArray[],int offset){
		if (offset < textArray.length && textArray[offset] == newLineDelimiter[0]){			
			for (int i = 0; i < newLineDelimiter.length ; i++){
				if (textArray[offset+i] != newLineDelimiter[i])
					return false;
			}
			return true;
		}
		return false;
	}
}