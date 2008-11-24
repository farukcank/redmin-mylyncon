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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.svenk.redmine.core.IRedmineClient;
import org.svenk.redmine.core.RedmineProjectData;
import org.svenk.redmine.core.model.RedmineTracker;

public class RedmineTrackerPage extends WizardPage {

	private final static String PAGE_NAME = "TrackerSelection";

	private final static String PAGE_TITLE = "Select a tracker";

	private final static String PAGE_DESCRIPTION = "Please select the tracker, that will be assigned to the new ticket";
	
	private final IRedmineClient client;
	
	private List trackerList;
	
	public RedmineTrackerPage(IRedmineClient client) {
		super(PAGE_NAME);
		setTitle(PAGE_TITLE);
		setDescription(PAGE_DESCRIPTION);
		this.client = client;
	}

	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		control.setLayoutData(gd);
		GridLayout layout = new GridLayout(1, false);
		control.setLayout(layout);

		trackerList = new List(control, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		trackerList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		trackerList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RedmineTrackerPage.this.setPageComplete(RedmineTrackerPage.this.isPageComplete());
			}
		});
		
		setControl(control);

	}

	@Override
	public boolean isPageComplete() {
		return trackerList.getSelectionCount()==1;
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		RedmineProjectPage projectPage = (RedmineProjectPage)getWizard().getPage(RedmineProjectPage.PAGE_NAME);
		RedmineProjectData projectData = client.getClientData().getProjectFromName(projectPage.getSelectedProjectName());

		String selectedValue = trackerList.getSelectionIndex()>-1 ? trackerList.getItem(trackerList.getSelectionIndex()) : "";
		trackerList.removeAll();
		for (RedmineTracker tracker : projectData.getTrackers()) {
			trackerList.add(tracker.getName());
			if (tracker.getName().equals(selectedValue)) {
				trackerList.select(trackerList.getItemCount()-1);
			}
		}
		
	}

	public String getSelectedTrackerName() {
		return trackerList.getItem(trackerList.getSelectionIndex());
	}

}
