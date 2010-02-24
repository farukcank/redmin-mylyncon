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
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.svenk.redmine.core.IRedmineConstants;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.exception.RedmineStatusException;
import org.svenk.redmine.core.model.RedmineCustomField.CustomType;
import org.svenk.redmine.core.util.RedmineUtil;

public class RedmineTaskDataReader {

	public static Map<String, String> readTask(TaskData taskData) throws RedmineStatusException {
		try {
			Map<String, TaskAttribute> attributeValues = taskData.getRoot().getAttributes();
			Map<String, String> postValues = new HashMap<String, String>(attributeValues.size()); 
			
			for (Entry<String, TaskAttribute> entry : attributeValues.entrySet()) {
				
				TaskAttribute taskAttribute = entry.getValue();
				String attributeId = entry.getKey();
				RedmineAttribute redmineAttribute = RedmineAttribute.fromRedmineKey(attributeId);
				
				if (redmineAttribute == null) {
					//custom
					if (taskAttribute.getMetaData().isReadOnly()) {
						continue;
					}
					
					String key;
					int len;
					if(attributeId.startsWith(CustomType.IssueCustomField.taskKeyPrefix)) {
						key = IRedmineConstants.CLIENT_FIELD_ISSUE_CUSTOM_R08L;
						len = CustomType.IssueCustomField.taskKeyPrefix.length();
					} else if (attributeId.startsWith(CustomType.TimeEntryCustomField.taskKeyPrefix)) {
						key = IRedmineConstants.CLIENT_FIELD_TIMEENTRY_CF;
						len = CustomType.TimeEntryCustomField.taskKeyPrefix.length();
					} else {
						continue;
					}
					
					key = String.format(key, Integer.parseInt(attributeId.substring(len)));
					String attributeValue = parseValue(taskAttribute.getValue(), taskAttribute.getMetaData().getType());
					
					postValues.put(key, attributeValue);
				} else {
					//standard
					if (redmineAttribute.isReadOnly() || taskAttribute.getMetaData().isReadOnly()) {
						continue;
					}
					
					String attributeValue = parseValue(taskAttribute.getValue(), taskAttribute.getMetaData().getType());
					String key = redmineAttribute.getRedmineKey();
					
					postValues.put(key, attributeValue);
				}
			}
			
			return postValues;
		} catch (NumberFormatException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null, "Invalid Integer value");
			throw new RedmineStatusException(status);
		}
	}
	
	private static String parseValue(String value, String type) {

		if (type.equals(TaskAttribute.TYPE_DATE) && !value.equals("")) {
			return RedmineUtil.toFormatedRedmineDate(RedmineUtil.parseDate(value));
		}
		
		if (type.equals(TaskAttribute.TYPE_BOOLEAN) && !value.equals("")) {
			return Boolean.parseBoolean(value) ? "1" : "0";
		}
		
		return value;
	}
}
