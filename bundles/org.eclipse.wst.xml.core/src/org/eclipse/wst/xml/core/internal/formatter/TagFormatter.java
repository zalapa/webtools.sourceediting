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
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;


abstract class TagFormatter extends XMLFormatter {
	
	protected IDOMNode node;
	
	protected TagFormatter(XMLFormatterStateTracker state, IDOMNode node){
		super(state);
		this.node = node;
	}

	protected void removeWhiteSpaces(ITextRegion containedRegion) {
		if (containedRegion.getEnd() > containedRegion.getTextEnd()) {
			int length = containedRegion.getEnd() - containedRegion.getTextEnd();
			int startRegionOffset = getCurrentRegion().getStartOffset();
			int textRegionEnd = containedRegion.getTextEnd();
			deleteText(startRegionOffset+textRegionEnd, length);
		}
	}
	
	//TODO:Should be deleted
	protected IDOMNode getStucturedModel (){
		return node;
	}
	
	//TODO: This must be deleted
	protected IDOMNode getCurrentDOMNode(){
		return (IDOMNode) node.getModel().getIndexedRegion(getCurrentRegion().getStartOffset());
	}
	
}
