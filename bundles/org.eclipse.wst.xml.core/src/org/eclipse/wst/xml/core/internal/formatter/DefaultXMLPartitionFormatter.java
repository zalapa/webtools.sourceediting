/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.xml.core.internal.formatter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;


public class DefaultXMLPartitionFormatter {
	private IProgressMonitor fProgressMonitor;
	
	public TextEdit format(IDocument document, int start, int length) {
		return format(document, start, length, new XMLFormattingPreferences());
	}

	public TextEdit format(IDocument document, int start, int length, XMLFormattingPreferences preferences) {
		TextEdit edit = null;
		if (document instanceof IStructuredDocument) {
			IStructuredModel model = StructuredModelManager.getModelManager().getModelForEdit((IStructuredDocument) document);
			if (model != null ) {
				try {
					edit = format(model, start, length, preferences);
				}
				finally {
					model.releaseFromEdit();
				}
			}
		}
		return edit;
	}

	public TextEdit format(IStructuredModel model, int start, int length) {
		return format(model, start, length, new XMLFormattingPreferences());
	}
	
	public TextEdit format(IStructuredModel model, int start, int length, XMLFormattingPreferences preferences) {
		IStructuredDocument document = model.getStructuredDocument();
		IStructuredDocumentRegion currentRegion = document.getRegionAtCharacterOffset(0);
		XMLFormatterStateTracker stateTracker = new XMLFormatterStateTracker(preferences, start, length, document.getLineDelimiter());
		IDOMNode node = (IDOMNode)model.getIndexedRegion(currentRegion.getStartOffset());
		XMLFormatterFactory formatterFactory = new XMLFormatterFactory(stateTracker, node);
		MultiTextEdit textEdit = new MultiTextEdit();
		XMLFormatter formatter = null;
		while (currentRegion != null  && progressMonitorStillRunning()){
			formatter = formatterFactory.getFormatter(currentRegion);
			TextEdit children[]=formatter.formatRegion(currentRegion);
			textEdit.addChildren(children);
			currentRegion = currentRegion.getNext();
		}		
		return textEdit;
	}
	
	private boolean progressMonitorStillRunning() {
		return (fProgressMonitor == null || (fProgressMonitor != null && !fProgressMonitor.isCanceled()));
	}
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor = monitor;
	}				
}
