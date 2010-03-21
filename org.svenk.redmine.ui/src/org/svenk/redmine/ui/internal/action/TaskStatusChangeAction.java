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
package org.svenk.redmine.ui.internal.action;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineOperation;

public class TaskStatusChangeAction extends AbstractRedmineAttributeChangeAction {
	
	public TaskStatusChangeAction(String statusId, String statusName, ITask[] tasks) {
		super(RedmineAttribute.STATUS_CHG, statusId, statusName, tasks);
	}
	
	@Override
	protected void setClosedTaskValue(TaskAttribute attribute, String value, TaskData taskData, TaskDataModel model) {
		super.setClosedTaskValue(attribute, value, taskData, model);
		
		TaskAttribute markasOperation = taskData.getRoot().getAttribute(TaskAttribute.PREFIX_OPERATION + RedmineOperation.markas.toString());
		if(markasOperation!=null) {
			TaskAttribute operation = taskData.getRoot().getAttribute(TaskAttribute.OPERATION);
			taskData.getAttributeMapper().setValue(operation, RedmineOperation.markas.toString());
			model.attributeChanged(operation);
		}
	}
}
