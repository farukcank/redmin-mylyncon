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
package org.svenk.redmine.ui.wizard;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.svenk.redmine.core.AbstractRedmineTaskMapping;
import org.svenk.redmine.core.IRedmineClient;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.RedmineRepositoryConnector;
import org.svenk.redmine.core.exception.RedmineException;

public class NewRedmineTaskWizard extends NewTaskWizard implements INewWizard {

	private RedmineProjectPage projectPage;
	
	private RedmineTrackerPage trackerPage;
	
	public NewRedmineTaskWizard(TaskRepository taskRepository, ITaskMapping taskSelection) {
		super(taskRepository, taskSelection);
	}
	
	@Override
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
	}
	
	@Override
	public void addPages() {
		RedmineRepositoryConnector connector = (RedmineRepositoryConnector) TasksUi.getRepositoryManager().getRepositoryConnector(RedmineCorePlugin.REPOSITORY_KIND);
		
		try {
			final IRedmineClient client = connector.getClientManager().getRedmineClient(getTaskRepository());
			Assert.isNotNull(client);
			
			projectPage = new RedmineProjectPage(client, getTaskRepository());
			trackerPage = new RedmineTrackerPage(client);
			addPage(projectPage);
			addPage(trackerPage);
		} catch (RedmineException e) {}

	}

	@Override
	protected ITaskMapping getInitializationData() {
		final String product = projectPage.getSelectedProjectName();
		final int trackerId = trackerPage.getSelectedTracker().getValue();
		
		return new AbstractRedmineTaskMapping() {
			@Override
			public String getProduct() {
				return product;
			}
			public String getTracker() {
				return ""+trackerId;
			}
			
		};
	}
}
