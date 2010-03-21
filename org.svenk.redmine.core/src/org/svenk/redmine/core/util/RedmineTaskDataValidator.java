/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylyn project committers
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2008 Sven Krzyzak
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sven Krzyzak - adapted Trac implementation for Redmine
 *******************************************************************************/
package org.svenk.redmine.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.svenk.redmine.core.IRedmineConstants;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.client.RedmineProjectData;
import org.svenk.redmine.core.model.RedmineCustomField;

public class RedmineTaskDataValidator {

	RedmineClientData clientData;
	
	public RedmineTaskDataValidator(RedmineClientData clientData) {
		this.clientData = clientData;
	}
	
	public RedmineTaskDataValidatorResult validateTaskData(TaskData taskData) {
		RedmineTaskDataValidatorResult result = new RedmineTaskDataValidatorResult();
		validateDefaultAttributes(taskData, result);
		validateCustomAttributes(taskData, result);
		return result;
	}
	
	/**
	 * Validates after changes on model
	 * 
	 * @param taskData
	 * @param attribute
	 * @return
	 */
	public RedmineTaskDataValidatorResult validateTaskAttribute(TaskData taskData, TaskAttribute attribute) {
		RedmineTaskDataValidatorResult result = new RedmineTaskDataValidatorResult();

		if (attribute.getId().startsWith(IRedmineConstants.TASK_KEY_PREFIX_TICKET_CF)) {
			TaskAttribute rootAttr = taskData.getRoot();
			TaskAttribute projAttr = rootAttr.getMappedAttribute(RedmineAttribute.PROJECT.getTaskKey());
			RedmineProjectData projectData = clientData.getProjectFromName(projAttr.getValue());

			String valStr = null;
			try {
				valStr = rootAttr.getMappedAttribute(RedmineAttribute.TRACKER.getTaskKey()).getValue();
				List<RedmineCustomField> ticketFields = projectData.getCustomTicketFields(Integer.parseInt(valStr));
				
				valStr = attribute.getId().substring(IRedmineConstants.TASK_KEY_PREFIX_TICKET_CF.length());
				
				try {
					int fieldId = Integer.parseInt(valStr);
					for (RedmineCustomField customField : ticketFields) {
						if (customField.getId()==fieldId) {
							validateCustomAttribute(attribute.getValue(), customField, result);
							break;
						}
					}
				} catch(NumberFormatException e1) {
					IStatus status = RedmineCorePlugin.toStatus(e1, null, "INVALID_CUSTOMFIELD_ID {0}", valStr);
					StatusHandler.log(status);
					result.addErrorMessage(status.getMessage());
				}
				
			} catch (NumberFormatException e) {
				IStatus status = RedmineCorePlugin.toStatus(e, null, "INVALID_TRACKER_ID {0}", valStr);
				StatusHandler.log(status);
				result.addErrorMessage(status.getMessage());
			} catch (NullPointerException e) {
				//TODO refresh RepositoryConfiguration
				IStatus status = RedmineCorePlugin.toStatus(e, null, "INCOMPLETE_REPOSITORY_CONFIGURATION");
				StatusHandler.log(status);
				result.addErrorMessage(status.getMessage());
			}
			
		} else {
			if (attribute.getId().equals(RedmineAttribute.ESTIMATED.getTaskKey())) {
				validateDefaultAttributeEsimatedHours(attribute.getValue(), result);
			}
		}
		return result;
	}
	
	protected void validateDefaultAttributes(TaskData taskData, RedmineTaskDataValidatorResult result) {
		validateRequiredDefaultAttributes(taskData, result);
		
		TaskAttribute estimatedAttr = taskData.getRoot().getMappedAttribute(RedmineAttribute.ESTIMATED.getTaskKey());
		if (estimatedAttr != null) {
			validateDefaultAttributeEsimatedHours(estimatedAttr.getValue(), result);
		}
	}

	protected void validateRequiredDefaultAttributes(TaskData taskData, RedmineTaskDataValidatorResult result) {
		TaskAttribute rootAttr = taskData.getRoot();
		TaskAttribute taskAttr = null;
		String attributeValue = null;
		
		for (RedmineAttribute redmineAttribute : RedmineAttribute.values()) {
			if (redmineAttribute.isRequired()) {
				if(redmineAttribute==RedmineAttribute.STATUS && taskData.isNew())  {
					redmineAttribute=RedmineAttribute.STATUS_CHG;
				}
				
				taskAttr = rootAttr.getMappedAttribute(redmineAttribute.getTaskKey());
				attributeValue = (taskAttr!=null) ? taskAttr.getValue().trim() : null;
				if (attributeValue==null || attributeValue.length()<1) {
					result.addErrorMessage(redmineAttribute.toString() + " is required");
				}
			}
		}
	}
	
	protected void validateDefaultAttributeEsimatedHours(String value, RedmineTaskDataValidatorResult result) {
		if (value.trim().length()>0) {
			try {
				Double.valueOf(value).toString();
			} catch (NumberFormatException e) {
				StringBuilder sb = new StringBuilder(RedmineAttribute.ESTIMATED.toString());
				sb.append(": must be a Float");
				result.addErrorMessage(sb.toString());
			}
		}
	}

	protected void validateCustomAttributes(TaskData taskData, RedmineTaskDataValidatorResult result) {
		TaskAttribute rootAttr = taskData.getRoot();
		TaskAttribute projAttr = rootAttr.getMappedAttribute(RedmineAttribute.PROJECT.getTaskKey());
		RedmineProjectData projectData = clientData.getProjectFromName(projAttr.getValue());

		String valStr = null;
		try {
			valStr = rootAttr.getMappedAttribute(RedmineAttribute.TRACKER.getTaskKey()).getValue();
			List<RedmineCustomField> ticketFields = projectData.getCustomTicketFields(Integer.parseInt(valStr));
			
			String attributeValue = null;
			TaskAttribute taskAttribute = null;
			for (RedmineCustomField customField : ticketFields) {
				taskAttribute = rootAttr.getMappedAttribute(IRedmineConstants.TASK_KEY_PREFIX_TICKET_CF + customField.getId());
				attributeValue = (taskAttribute==null) ? "" : taskAttribute.getValue().trim();
				validateCustomAttribute(attributeValue, customField, result);
			}
		} catch (NumberFormatException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null, "INVALID_TRACKER_ID {0}", valStr);
			StatusHandler.log(status);
			result.addErrorMessage(status.getMessage());
		} catch (NullPointerException e) {
			//TODO refresh RepositoryConfiguration
			IStatus status = RedmineCorePlugin.toStatus(e, null, "INCOMPLETE_REPOSITORY_CONFIGURATION");
			StatusHandler.log(status);
			result.addErrorMessage(status.getMessage());
		}

	}
	
	protected void validateCustomAttribute(String attributeValue, RedmineCustomField customField, RedmineTaskDataValidatorResult result) {
		validateRequiredCustomAttribute(attributeValue, customField, result);
		validateCustomAttributeType(attributeValue, customField, result);
		validateCustomAttributeMinLength(attributeValue, customField, result);
		validateCustomAttributeMaxLength(attributeValue, customField, result);
		validateCustomAttributePattern(attributeValue, customField, result);
	}
	
	protected void validateRequiredCustomAttribute(String value, RedmineCustomField customField, RedmineTaskDataValidatorResult result) {
		if (customField.isRequired() && (value==null || value.length()<1)) {
			result.addErrorMessage(customField.getName() + " is required");
		}
	}
	
	protected void validateCustomAttributeMinLength(String value, RedmineCustomField customField, RedmineTaskDataValidatorResult result) {
		int min = customField.getMin();
		if (min>0 && value.length()<min) {
			StringBuilder sb = new StringBuilder(customField.getName());
			sb.append(": minimum length of ").append(min).append(" below");
			result.addErrorMessage(sb.toString());
		}
	}
	
	protected void validateCustomAttributeMaxLength(String value, RedmineCustomField customField, RedmineTaskDataValidatorResult result) {
		int max = customField.getMax();
		if (max>0 && value.length()>max) {
			StringBuilder sb = new StringBuilder(customField.getName());
			sb.append(": maximum length of ").append(max).append(" exceeded");
			result.addErrorMessage(sb.toString());
		}
	}
	
	protected void validateCustomAttributePattern(String value, RedmineCustomField customField, RedmineTaskDataValidatorResult result) {
		String pattern = customField.getValidationRegex();
		if (pattern!=null && pattern.length()>0 && !Pattern.matches(pattern, value)) {
			StringBuilder sb = new StringBuilder(customField.getName());
			sb.append(": ").append(value).append(" dosn't match ").append(pattern);
			result.addErrorMessage(sb.toString());
		}
	}
	
	protected void validateCustomAttributeType(String value, RedmineCustomField customField, RedmineTaskDataValidatorResult result) {
		if (value!=null && value.length()>0)  {
			try {
				switch (customField.getType()) {
				case FLOAT:
					Double.valueOf(value).toString();
					break;
				case INT:
					Integer.valueOf(value).toString();
					break;
				}
			} catch (NumberFormatException e) {
				StringBuilder sb = new StringBuilder(customField.getName());
				sb.append(": must be a ").append(customField.getType().toString());
				result.addErrorMessage(sb.toString());
			}
		}
		
	}
	
	public class RedmineTaskDataValidatorResult {
		
		public RedmineTaskDataValidatorResult() {
			
		}
		
		private List<String> errorMessages = new ArrayList<String>(1);
		
		protected void addErrorMessage(String errorMessage) {
			errorMessages.add(errorMessage);
		}

		public boolean hasErrors() {
			return errorMessages.size()>0;
		}
		
		public String getFirstErrorMessage() {
			return errorMessages.size()>0 ? errorMessages.get(0) : null;
		}
		
		public List<String> getErrorMessages() {
			return Collections.unmodifiableList(errorMessages);
		}
	}
	
}
