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

package org.svenk.redmine.ui;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.ui.wizard.NewRedmineTaskWizard;
import org.svenk.redmine.ui.wizard.RedmineQueryPage;
import org.svenk.redmine.ui.wizard.RedmineRepositorySettingsPage;


public class RedmineConnectorUi extends AbstractRepositoryConnectorUi {

	public RedmineConnectorUi() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getConnectorKind() {
		return RedmineCorePlugin.REPOSITORY_KIND;
	}

	@Override
	public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
		return new RedmineRepositorySettingsPage(taskRepository);
	}

	@Override
	public boolean hasSearchPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
		RepositoryQueryWizard wizard = new RepositoryQueryWizard(repository);
		wizard.addPage(new RedmineQueryPage(repository, query));
		return wizard;
	}
	
//	@Override
//	public IWizard getQueryWizard(TaskRepository repository, AbstractRepositoryQuery query) {
//		return new RedmineQueryWizard(repository, (RedmineRepositoryQuery)query);
//	}

	@Override
	public IWizard getNewTaskWizard(TaskRepository taskRepository, ITaskMapping selection) {
		return new NewRedmineTaskWizard(taskRepository, selection);
	}

}
