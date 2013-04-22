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

public class OpenTagStack {
	private ArrayList openTagStack = new ArrayList();
	private int index = 0;
	
	public void addOpenTag(String opentTagName){
		openTagStack.add(index, opentTagName);
		index ++;
	}
	
	public int getOpenTagIndex(String openTagName){
		int indexLastOpenTag = index-1;
		boolean found = false;
		while (indexLastOpenTag > 0 && !found){
			String openTag = (String)openTagStack.get(indexLastOpenTag);
			if (openTag != null){
				if (openTag.equals(openTagName)){
					found=true;
				}
			}
			indexLastOpenTag--;
		}
		int dif = 0;
		if (found){
			int foundInIndex = ++indexLastOpenTag;
			dif = index - foundInIndex;
			index = indexLastOpenTag++;
		}
		else{
			dif = index;
			index=0;
		}
		return dif;
	}
}
