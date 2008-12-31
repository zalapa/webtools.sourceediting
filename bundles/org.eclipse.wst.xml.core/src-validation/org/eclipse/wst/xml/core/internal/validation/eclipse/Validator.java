/*******************************************************************************
 * Copyright (c) 2001, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.xml.core.internal.validation.eclipse;

import java.io.InputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.validation.ValidationResult;
import org.eclipse.wst.validation.ValidationState;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.preferences.XMLCorePreferenceNames;
import org.eclipse.wst.xml.core.internal.validation.XMLNestedValidatorContext;
import org.eclipse.wst.xml.core.internal.validation.XMLValidationConfiguration;
import org.eclipse.wst.xml.core.internal.validation.XMLValidationReport;
import org.eclipse.wst.xml.core.internal.validation.core.AbstractNestedValidator;
import org.eclipse.wst.xml.core.internal.validation.core.NestedValidatorContext;
import org.eclipse.wst.xml.core.internal.validation.core.ValidationMessage;
import org.eclipse.wst.xml.core.internal.validation.core.ValidationReport;

public class Validator extends AbstractNestedValidator
{
  private static final String XML_VALIDATOR_CONTEXT = "org.eclipse.wst.xml.core.validatorContext"; //$NON-NLS-1$
  protected int indicateNoGrammar = 0;
  
  /**
   * Set any preferences for XML validation.
   * 
   * @see org.eclipse.wst.xml.core.internal.validation.core.AbstractNestedValidator#setupValidation(org.eclipse.wst.xml.core.internal.validation.core.NestedValidatorContext)
   */
  protected void setupValidation(NestedValidatorContext context) 
  {
	super.setupValidation(context);
	indicateNoGrammar = XMLCorePlugin.getDefault().getPluginPreferences().getInt(XMLCorePreferenceNames.INDICATE_NO_GRAMMAR);
  }

  /* (non-Javadoc)
   * @see org.eclipse.wst.xml.core.internal.validation.core.AbstractNestedValidator#validate(java.lang.String, java.io.InputStream, org.eclipse.wst.xml.core.internal.validation.core.NestedValidatorContext)
   */
  public ValidationReport validate(String uri, InputStream inputstream, NestedValidatorContext context)
  {
    return validate(uri, inputstream, context, null);
  }

  public ValidationReport validate(String uri, InputStream inputstream, NestedValidatorContext context, ValidationResult result)
  {
    XMLValidator validator = XMLValidator.getInstance();

    XMLValidationConfiguration configuration = new XMLValidationConfiguration();
    try
    {
      configuration.setFeature(XMLValidationConfiguration.INDICATE_NO_GRAMMAR, indicateNoGrammar);
    }
    catch(Exception e)
    {
      // TODO: Unable to set the preference. Log this problem.
    }
    
    XMLValidationReport valreport = validator.validate(uri, inputstream, configuration, result, context);
              
    return valreport;
  }
	  
  /**
   * Store additional information in the message parameters. For XML validation there
   * are three additional pieces of information to store:
   * param[0] = the column number of the error
   * param[1] = the 'squiggle selection strategy' for which DOM part to squiggle
   * param[2] = the name or value of what is to be squiggled
   * 
   * @see org.eclipse.wst.xml.core.internal.validation.core.AbstractNestedValidator#addInfoToMessage(org.eclipse.wst.xml.core.internal.validation.core.ValidationMessage, org.eclipse.wst.validation.internal.provisional.core.IMessage)
   */
  protected void addInfoToMessage(ValidationMessage validationMessage, IMessage message)
  { 
	String key = validationMessage.getKey();
	if(key != null)
	{
      XMLMessageInfoHelper messageInfoHelper = new XMLMessageInfoHelper();
      String[] messageInfo = messageInfoHelper.createMessageInfo(key, validationMessage.getMessageArguments());
      
      message.setAttribute(COLUMN_NUMBER_ATTRIBUTE, new Integer(validationMessage.getColumnNumber()));
      message.setAttribute(SQUIGGLE_SELECTION_STRATEGY_ATTRIBUTE, messageInfo[0]);
      message.setAttribute(SQUIGGLE_NAME_OR_VALUE_ATTRIBUTE, messageInfo[1]);
	}
  }
  
  /**
   * Get the nested validation context.
   * 
   * @param state
   *          the validation state.
   * @param create
   *          when true, a new context will be created if one is not found
   * @return the nested validation context.
   */
  protected NestedValidatorContext getNestedContext(ValidationState state, boolean create)
  {
    NestedValidatorContext context = null;
    Object o = state.get(XML_VALIDATOR_CONTEXT);
    if (o instanceof XMLNestedValidatorContext)
      context = (XMLNestedValidatorContext)o;
    else if (create)
    {
      context = new XMLNestedValidatorContext();
    }
    return context;
  }
  
  public void validationStarting(IProject project, ValidationState state, IProgressMonitor monitor)
  {
    if (project != null)
    {
      NestedValidatorContext context = getNestedContext(state, false);
      if (context == null)
      {
        context = getNestedContext(state, true);
        setupValidation(context);
        state.put(XML_VALIDATOR_CONTEXT, context);
      }
      super.validationStarting(project, state, monitor);
    }
  }
  
  public void validationFinishing(IProject project, ValidationState state, IProgressMonitor monitor)
  {
    if (project != null)
    {
      super.validationFinishing(project, state, monitor);
      NestedValidatorContext context = getNestedContext(state, false);
      if (context != null)
      {
        teardownValidation(context);
        state.put(XML_VALIDATOR_CONTEXT, null);
      }
    }
  }
}
