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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDataType;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.internal.formatter.XMLFormattingConstraints;
import org.eclipse.wst.xml.core.internal.formatter.XMLFormattingPreferences;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.core.internal.ssemodelquery.ModelQueryAdapter;
import org.w3c.dom.Element;

/**
 * This class is used to track the current white space strategy to apply, based on node being processed.
 * This class uses a stack (WhitespaceStrategyStack) to hold the relation between parent and children.
 * A whitespace strategy is tracked in when a start tag (not a empty one) is found, and is untracked when the close tag is found.
 * Only strategies different from NONE are tracked. 
 *
 */
public class XMLFormattingWhitespacesTracker {	
	boolean PCDataWhitespaceStrategy;
	
	private WhitespaceStrategyStack whitespaceStack = new WhitespaceStrategyStack();	
	
	class WhitespaceNode{
		IDOMNode whitespaceNode;
		String whitespaceStrategy;
		
		String getWhitespaceStrategy() {
			return whitespaceStrategy;
		}

		IDOMNode getWithespaceNode() {
			return whitespaceNode;
		}
		
		
		public WhitespaceNode(IDOMNode whitespaceNodeValue, String whitespaceStrategyValue){
			whitespaceNode = whitespaceNodeValue;
			whitespaceStrategy = whitespaceStrategyValue;
		}
	}		
		
	class WhitespaceStrategyStack{

		private ArrayList whitepaceStack = new ArrayList();
		
		public WhitespaceStrategyStack(){
			push(new WhitespaceNode(null, XMLFormatterConstants.NONE));
		}
		
		public void pop(){
			if(!empty()){
				whitepaceStack.remove(whitepaceStack.size()-1);
			}
					
		}
		
		public WhitespaceNode peek(){
			return (WhitespaceNode)whitepaceStack.get(whitepaceStack.size()-1);
		}
		
		private boolean empty(){
			return whitepaceStack.size() <= 1 ? true : false;
		}
		
		private void push(WhitespaceNode element){
			whitepaceStack.add(element);
		}
	}
	
	XMLFormattingWhitespacesTracker(String pcDataWhitespaceStrategy) {
		if (pcDataWhitespaceStrategy.equals(XMLFormattingPreferences.PRESERVE)){
			PCDataWhitespaceStrategy = true;
		}
		else{
			PCDataWhitespaceStrategy = false;
		}
	}
	
	public void trackWhitespaceStrategy(IDOMNode currentNode){
		String whitespaceStrategy = getWithespaceStrategy(currentNode);
		if (!whitespaceStrategy.equals(XMLFormatterConstants.NONE)){
			whitespaceStack.push(new WhitespaceNode(currentNode,whitespaceStrategy));
		}
	}
	
	public void untrackWhitespaceStrategy(IStructuredDocumentRegion currentRegion, IStructuredModel model){
		if (!whitespaceStack.empty()){
			IDOMNode currentNode = (IDOMNode) model.getIndexedRegion(currentRegion.getStartOffset());
			WhitespaceNode whitespaceNode= whitespaceStack.peek();
			IDOMNode referenceNode = whitespaceNode.getWithespaceNode();
			if(referenceNode != null && referenceNode.equals(currentNode) ){
				whitespaceStack.pop();
			}
		}
	}
	
	public String getCurrentWithespaceStrategy(){
		return whitespaceStack.peek().getWhitespaceStrategy();
	}
	
	
	private String getWithespaceStrategy(IDOMNode currentNode) {
		IStructuredDocumentRegion region = currentNode.getStartStructuredDocumentRegion();
		// Search for preserve whitespace in xsl:text & xsl:attribute
		String nodeNamespaceURI = currentNode.getNamespaceURI();
		if (XMLFormatterConstants.XSL_NAMESPACE.equals(nodeNamespaceURI)) {
			String nodeName = ((Element) currentNode).getLocalName();
			if (XMLFormatterConstants.XSL_ATTRIBUTE.equals(nodeName) || XMLFormatterConstants.XSL_TEXT.equals(nodeName)) {				
				return XMLFormatterConstants.PRESERVE;
			}
		}
		else{
		// search within current tag for xml:space attribute
			ITextRegionList textRegions = region.getRegions();
			Iterator textRegionsIterator = textRegions.iterator();
			while (textRegionsIterator.hasNext()) {
				ITextRegion textRegion = (ITextRegion) textRegionsIterator.next();
				if (DOMRegionContext.XML_TAG_ATTRIBUTE_NAME.equals(textRegion.getType()) && XMLFormatterConstants.XML_SPACE.equals(region.getText(textRegion))) {
					if (textRegionsIterator.hasNext()) {
						textRegion = (ITextRegion)textRegionsIterator.next();
						if (DOMRegionContext.XML_TAG_ATTRIBUTE_EQUALS.equals(textRegion.getType()) && textRegionsIterator.hasNext()) {
							textRegion = (ITextRegion)textRegionsIterator.next();
							String regionText = region.getText(textRegion);
							if (XMLFormatterConstants.PRESERVE_L.equals(regionText) || XMLFormatterConstants.PRESERVE_QUOTED.equals(regionText)) {
								// preserve was found so set the strategy
								return XMLFormattingConstraints.PRESERVE;							
							}
							// xml:space was found but it was not preserver, so use default whitespace strategy
							return XMLFormatterConstants.DEFAULT;
						}
					}				
				}
			}
		}
//
//		// try referring to content model for information on whitespace
		ModelQueryAdapter adapter = (ModelQueryAdapter) ((IDOMDocument) currentNode.getOwnerDocument()).getAdapterFor(ModelQueryAdapter.class);
		CMElementDeclaration elementDeclaration = adapter.getModelQuery().getCMElementDeclaration((Element)currentNode);
		if (elementDeclaration != null){					
			String facetValue = null;
			if(elementDeclaration.getDataType() != null)
				facetValue = (String) elementDeclaration.getDataType().getProperty(XMLFormatterConstants.PROPERTY_WHITESPACE_FACET);
			if(facetValue != null) {
				if(XMLFormatterConstants.PRESERVE.toLowerCase().equals(facetValue)){
					return XMLFormattingConstraints.PRESERVE; 					
				}
				if(XMLFormatterConstants.COLLAPSE.toLowerCase().equals(facetValue)){ 
					return XMLFormattingConstraints.COLLAPSE;   					
				}
				if(XMLFormatterConstants.REPLACE.toLowerCase().equals(facetValue)){
					return XMLFormattingConstraints.REPLACE; 					
				}
			}
			else{
				int contentType = elementDeclaration.getContentType();
				if (contentType == CMElementDeclaration.PCDATA && PCDataWhitespaceStrategy){
					return XMLFormattingConstraints.PRESERVE;
				}
				else{
					// look for xml:space in content model
					CMNamedNodeMap cmAttributes = elementDeclaration.getAttributes();
					// Check implied values from the DTD way.
					CMAttributeDeclaration attributeDeclaration = (CMAttributeDeclaration) cmAttributes.getNamedItem(XMLFormatterConstants.XML_SPACE);
					if (attributeDeclaration != null) {
						String defaultValue = null;
						CMDataType attrType = attributeDeclaration.getAttrType();
						if (attrType != null) {
							if ((attrType.getImpliedValueKind() != CMDataType.IMPLIED_VALUE_NONE) && attrType.getImpliedValue() != null)
								defaultValue = attrType.getImpliedValue();
							else if ((attrType.getEnumeratedValues() != null) && (attrType.getEnumeratedValues().length > 0)) {
								defaultValue = attrType.getEnumeratedValues()[0];
							}
						}
						if (XMLFormatterConstants.PRESERVE_L.equals(defaultValue))
							return XMLFormatterConstants.PRESERVE;
						else
							return XMLFormatterConstants.DEFAULT;						
					}
				}
			}
		}				
		return XMLFormatterConstants.NONE;
	}
}