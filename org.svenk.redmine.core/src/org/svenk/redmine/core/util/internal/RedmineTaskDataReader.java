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

package org.svenk.redmine.core.util.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.svenk.redmine.core.IRedmineConstants;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.RedmineOperation;
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.client.RedmineProjectData;
import org.svenk.redmine.core.exception.RedmineStatusException;
import org.svenk.redmine.core.model.RedmineCustomField.CustomType;
import org.svenk.redmine.core.util.RedmineUtil;

public class RedmineTaskDataReader {

	public static Map<String, String> readTask(TaskData taskData, Set<TaskAttribute> oldAttributes, RedmineClientData clientData) throws RedmineStatusException {
		TaskAttribute rootAttribute = taskData.getRoot();
		try {
			Map<String, TaskAttribute> attributeValues = rootAttribute.getAttributes();
			Map<String, String> postValues = new HashMap<String, String>(attributeValues.size()); 

			//usable CustomFields
			RedmineProjectData projectData = clientData.getProjectFromName(rootAttribute.getAttribute(RedmineAttribute.PROJECT.getTaskKey()).getValue());
			Set<Integer> usableCfs = projectData.getCustomFieldIds(Integer.parseInt(rootAttribute.getAttribute(RedmineAttribute.TRACKER.getTaskKey()).getValue()));

			for (Entry<String, TaskAttribute> entry : attributeValues.entrySet()) {
				TaskAttribute taskAttribute = entry.getValue();
				String attributeId = entry.getKey();
				RedmineAttribute redmineAttribute = RedmineAttribute.fromTaskKey(attributeId);
				
				if (redmineAttribute==null) {
					//customFields

					if (taskAttribute.getMetaData().isReadOnly()) {
						continue;
					}
					
					String key;
					int cfId = -1;
					if(attributeId.startsWith(CustomType.IssueCustomField.taskKeyPrefix)) {
						cfId = Integer.parseInt(attributeId.substring(CustomType.IssueCustomField.taskKeyPrefix.length()));
						if(!usableCfs.contains(cfId)) {
							continue;
						}
						key = IRedmineConstants.CLIENT_FIELD_ISSUE_CUSTOM;
					} else if (attributeId.startsWith(CustomType.TimeEntryCustomField.taskKeyPrefix)) {
						cfId = Integer.parseInt(attributeId.substring(CustomType.TimeEntryCustomField.taskKeyPrefix.length()));
						key = IRedmineConstants.CLIENT_FIELD_TIMEENTRY_CF;
					} else {
						continue;
					}
					
					if(cfId>0) {
						key = String.format(key, cfId);
						String attributeValue = parseValue(taskAttribute.getValue(), taskAttribute.getMetaData().getType());
						postValues.put(key, attributeValue);
					}
				} else {
					//standard attributes
					if (redmineAttribute.isReadOnly() || taskAttribute.getMetaData().isReadOnly() || redmineAttribute.isOperationValue()) {
						continue;
					}
					
					String attributeValue = parseValue(taskAttribute.getValue(), taskAttribute.getMetaData().getType());
					String key = redmineAttribute.getRedmineKey();
					
					postValues.put(key, attributeValue);
				}
			}
			
			
			TaskAttribute operationAttribute = rootAttribute.getMappedAttribute(TaskAttribute.OPERATION);
			if (operationAttribute != null) {
				
				RedmineOperation redmineOperation = RedmineOperation.valueOf(operationAttribute.getValue());
				TaskAttribute selectedOperation = rootAttribute.getAttribute(TaskAttribute.PREFIX_OPERATION + operationAttribute.getValue());
				if(redmineOperation!=null && selectedOperation!=null) {
					String value = null;
					
					if(redmineOperation.isAssociated()) {
						TaskAttribute inputAttribute = rootAttribute.getAttribute(redmineOperation.getInputId());
						if(inputAttribute!=null) {
							value = inputAttribute.getValue();
						}
					} else if(redmineOperation.needsRestoreValue()) {
						value = selectedOperation.getMetaData().getValue(IRedmineConstants.TASK_ATTRIBUTE_OPERATION_RESTORE);
					}
					
					if(value!=null) {
						RedmineAttribute redmineAttribute = RedmineAttribute.fromTaskKey(redmineOperation.getInputId());
						value = parseValue(value, redmineAttribute.getType());
						postValues.put(redmineAttribute.getRedmineKey(), value);
					}
				}
			}
			
			return postValues;
		} catch (NumberFormatException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null, Messages.RedmineTaskDataReader_INVALID_ATTRIBUTE_ID);
			throw new RedmineStatusException(status);
		}
	}
	
	private static String parseValue(String value, String type) {
		if (type.equals(TaskAttribute.TYPE_DATE) && !value.equals("")) { //$NON-NLS-1$
			return RedmineUtil.toFormatedRedmineDate(RedmineUtil.parseDate(value));
		}
		
		if (type.equals(TaskAttribute.TYPE_BOOLEAN) && !value.equals("")) { //$NON-NLS-1$
			return Boolean.parseBoolean(value) ? "1" : "0"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return value;
	}
}
