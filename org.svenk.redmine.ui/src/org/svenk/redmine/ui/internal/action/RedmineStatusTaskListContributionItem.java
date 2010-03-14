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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.data.ITaskDataManager;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineCorePlugin;

public class RedmineStatusTaskListContributionItem extends AbstractRedmineTaskListContributionItem {

	private MenuManager subMenuManager;
	
	@Override
	protected MenuManager getSubMenuManager() {
		if(subMenuManager==null) {
			subMenuManager = new MenuManager();
			
			Map<String, String> statusMap = buildStatusMap();
			if(statusMap!=null) {
				
				for (Entry<String, String> entry : statusMap.entrySet()) {
					List<ITask> taskList = getSelectedTasks();
					ITask[] tasks = taskList.toArray(new ITask[taskList.size()]);
					
					IContributionItem item = new ActionContributionItem(new TaskStatusChangeAction(entry.getKey(), entry.getValue(), tasks));
					subMenuManager.add(item);
				}
			}
		}
		
		return subMenuManager;
	}

	private Map<String, String> buildStatusMap() {

		Map<String, String> statusMap = null;
		List<ITask> tasks = getSelectedTasks();
		
		ITaskDataManager taskDataManager = TasksUi.getTaskDataManager();
		
		try {
			if (tasks.size()>0) {
				ITask firstTask = tasks.get(0);
				String repositoryUrl = firstTask.getRepositoryUrl();
				
				if (repositoryUrl!=null && taskDataManager.hasTaskData(firstTask)) {
					TaskData taskData = taskDataManager.getTaskData(firstTask);
					TaskAttribute attribute = taskData.getRoot().getAttribute(RedmineAttribute.STATUS.getRedmineKey());
					Set<String> commonIds = new HashSet<String>(attribute.getOptions().keySet());
					
					for (ITask task : tasks) {
						if (task.getRepositoryUrl().equals(repositoryUrl)) { //Same-Repository-Policy
							if (taskDataManager.hasTaskData(task)) {
								taskData = taskDataManager.getTaskData(task);
								attribute = taskData.getRoot().getAttribute(RedmineAttribute.STATUS.getRedmineKey());
								commonIds.retainAll(attribute.getOptions().keySet());
							}
						} else {
							return null; //Same-Repository-Policy
						}
					}
					
					if(commonIds.size()>0) {
						statusMap = new HashMap<String, String>(commonIds.size());
						for (String key : commonIds) {
							statusMap.put(key, attribute.getOption(key));
						}
					}
				}
			}
			
		} catch (NullPointerException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null);
			StatusHandler.log(status);
		} catch (CoreException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null);
			StatusHandler.log(status);
		}
		
		return statusMap;
	}
	
	
}
