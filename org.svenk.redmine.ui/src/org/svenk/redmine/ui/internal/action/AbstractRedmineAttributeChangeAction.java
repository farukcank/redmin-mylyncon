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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.ITaskDataManager;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.ui.RedmineUiPlugin;

public abstract class AbstractRedmineAttributeChangeAction extends Action {

	private RedmineAttribute attribute;

	protected final ITask[] tasks;

	protected String value;

	AbstractRedmineAttributeChangeAction(RedmineAttribute attribute, String value, ITask[] tasks) {
		this(attribute, value, value, tasks);
	}

	AbstractRedmineAttributeChangeAction(RedmineAttribute attribute, String value, String name, ITask[] tasks) {
		super(name, SWT.NONE);
		
		Assert.isNotNull(tasks);
		Assert.isNotNull(attribute);
		Assert.isTrue(tasks.length>0);
		
		this.attribute = attribute;
		this.value = value;
		this.tasks = tasks;
	}
	
	protected void changeAttributeValue(String value) {
		
	}

	@Override
	public void run() {
		ITaskDataManager taskDataManager = TasksUi.getTaskDataManager();
		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(RedmineCorePlugin.REPOSITORY_KIND, tasks[0].getRepositoryUrl());
		
		for (ITask task : tasks) {
			if(taskDataManager.hasTaskData(task)) {
				try {
					TaskAttribute attribute = null;
					if(isTaskOpen(task)) {
						attribute = taskDataManager.getTaskData(task).getRoot().getAttribute(this.attribute.getRedmineKey());
						attribute.setValue(value);
					} else {
						ITaskDataWorkingCopy copy = taskDataManager.getWorkingCopy(task);
						TaskDataModel model = new TaskDataModel(repository, task, copy);
						TaskData taskData = model.getTaskData();
						
						attribute = taskData.getRoot().getAttribute(this.attribute.getRedmineKey());
						attribute.setValue(value);
						model.attributeChanged(attribute);
						model.save(new NullProgressMonitor());
					}
					
					RedmineUiPlugin.getDefault().notifyAttributeChanged(task, attribute);
				} catch (CoreException e) {
					IStatus status = RedmineCorePlugin.toStatus(e, null);
					StatusHandler.log(status);
				}
			}
		}
	}
	
	private boolean isTaskOpen(ITask task) {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IEditorReference[] editorReferences = window.getActivePage().getEditorReferences();
			for (IEditorReference editorReference : editorReferences) {
				try {
					if (editorReference.getEditorInput() instanceof TaskEditorInput) {
						TaskEditorInput input = (TaskEditorInput) editorReference.getEditorInput();
						if (input.getTask()!=null && input.getTask()==task) {
							return true;
						}
					}
				} catch (PartInitException e) {
					// ignore
				}
			}
		}
		return false;
	}
}
