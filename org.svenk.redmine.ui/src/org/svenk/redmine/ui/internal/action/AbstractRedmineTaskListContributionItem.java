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
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.swt.widgets.Menu;
import org.svenk.redmine.ui.RedmineUiPlugin;

abstract public class AbstractRedmineTaskListContributionItem extends ContributionItem {

	abstract protected MenuManager getSubMenuManager();
	
	private List<ITask> taskList;
	
	@Override
	public void fill(Menu menu, int index) {
		MenuManager subMenuManager = getSubMenuManager();
		if(subMenuManager!=null) {
			for (IContributionItem item : subMenuManager.getItems()) {
				item.fill(menu, index++);
			}
		}
	}
	
	protected List<ITask> getSelectedTasks() {
		if(taskList==null) {
			IStructuredSelection selection = RedmineUiPlugin.getDefault().getLastSelection();
			taskList = new ArrayList<ITask>();
			
			if (selection!=null) {
				for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
					Object selectedElem = iterator.next();
					
					if(selectedElem instanceof ITask) {
						collectTask((ITask)selectedElem, taskList);
					} else if (selectedElem instanceof ITaskContainer) {
						collectTasks((ITaskContainer)selectedElem, taskList);
					}
				}
			}
			
		}
		
		return taskList;
	}
	
	protected void collectTask(ITask task, List<ITask> collected) {
		if(!collected.contains(task)) {
			collected.add(task);

			if (task instanceof ITaskContainer) {
				collectTasks((ITaskContainer)task, collected);
			}
		}
	}

	protected void collectTasks(ITaskContainer container, List<ITask> collected) {
		for (ITask task : container.getChildren()) {
			collectTask(task, collected);
		}
	}
}
