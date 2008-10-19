/*******************************************************************************
 *
 * Redmine-Mylyn-Connector
 * 
 * This implementation is on the basis of the implementations of Trac and 
 * Bugzilla emerged and contains parts of source code from these projects.
 * The corresponding copyright notice follows below of this.
 * Copyright (C) 2008  Sven Krzyzak and others
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *  
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.svenk.redmine.ui.wizard;

import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;

public class NewRedmineTaskWizard extends NewTaskWizard implements INewWizard {

	private RedmineProjectPage projectPage;
	
	public NewRedmineTaskWizard(TaskRepository taskRepository, ITaskMapping taskSelection) {
		super(taskRepository, taskSelection);
	}
	
	@Override
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
	}
	
	@Override
	public void addPages() {
		projectPage = new RedmineProjectPage(getTaskRepository());
		addPage(projectPage);
	}

	@Override
	protected ITaskMapping getInitializationData() {
		final String product = projectPage.getSelectedProjectName();
		return new TaskMapping() {
			@Override
			public String getProduct() {
				return product;
			}
		};
	}
}
