/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.jsp.ui.internal.autoedit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jst.jsp.core.internal.provisional.JSP11Namespace;
import org.eclipse.jst.jsp.ui.internal.JSPUIPlugin;
import org.eclipse.jst.jsp.ui.internal.Logger;
import org.eclipse.jst.jsp.ui.internal.preferences.JSPUIPreferenceNames;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.ui.internal.text.StructuredAutoEditStrategy;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;

public class StructuredAutoEditStrategyJSPJava extends StructuredAutoEditStrategy {

	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		if (!supportsSmartInsert(document)) {
			return;
		}
		
		IStructuredModel model = null;
		try {
			// Auto edit for JSP Comments
			if ("-".equals(command.text) && isPreferenceEnabled(JSPUIPreferenceNames.TYPING_COMPLETE_COMMENTS)) { //$NON-NLS-1$
				if (prefixedWith(document, command.offset, "<%-")) { //$NON-NLS-1$
					
					model = StructuredModelManager.getModelManager().getExistingModelForRead(document);
					if (model != null) {
						IDOMNode node = (IDOMNode) model.getIndexedRegion(command.offset);
						IDOMNode parent = (node != null) ? (IDOMNode) node.getParentNode() : null;
						// Parent is the scriptlet tag
						if (parent != null && JSP11Namespace.ElementName.SCRIPTLET.equals(parent.getNodeName()) && !parent.getSource().endsWith("--%>")) { //$NON-NLS-1$
							IStructuredDocumentRegion end = parent.getEndStructuredDocumentRegion();
							if (end != null) {
								try {
									document.replace(end.getStartOffset(), 0, "--"); //$NON-NLS-1$
								} catch (BadLocationException e) {
									Logger.logException(e);
								}
							}
						}
					}
				}
			}
			else{
				//if inserted string represents hitting the Tab key, it is going to be marked to not be customized for JavaAutoIndentStrategy
				//bug338254
				if (command.text.trim().length() == 0){
					if (isSpacesForTabsEnabled()){ 
						if (command.text.length() <= getEditorTabSize()){
							boolean isTabConverted= true;
							for(int i =0 ; i< command.text.length() && isTabConverted; i++){
								if (command.text.charAt(i) != ' '){
									isTabConverted=false;
								}
							}
							if (isTabConverted){
								command.doit=false;
							}
						}
					}
				}
			}
		}
		finally {
			if (model != null)
				model.releaseFromRead();
		}
	}
	
	private boolean isPreferenceEnabled(String key) {
		return (key != null && JSPUIPlugin.getDefault().getPreferenceStore().getBoolean(key));
	}
	
	private boolean isSpacesForTabsEnabled(){
		return EditorsUI.getPreferenceStore().getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);
	}
	
	private int getEditorTabSize(){
		return EditorsUI.getPreferenceStore().getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
	}

	private boolean prefixedWith(IDocument document, int offset, String string) {

		try {
			return document.getLength() >= string.length() && document.get(offset - string.length(), string.length()).equals(string);
		}
		catch (BadLocationException e) {
			Logger.logException(e);
			return false;
		}
	}

}
