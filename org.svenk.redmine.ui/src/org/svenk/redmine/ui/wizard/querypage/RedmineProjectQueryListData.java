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
package org.svenk.redmine.ui.wizard.querypage;

import java.util.List;

import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.client.RedmineProjectData;
import org.svenk.redmine.core.model.RedmineCustomField;
import org.svenk.redmine.core.model.RedmineIssueCategory;
import org.svenk.redmine.core.model.RedmineMember;
import org.svenk.redmine.core.model.RedminePriority;
import org.svenk.redmine.core.model.RedmineStoredQuery;
import org.svenk.redmine.core.model.RedmineTicketStatus;
import org.svenk.redmine.core.model.RedmineTracker;
import org.svenk.redmine.core.model.RedmineVersion;
import org.svenk.redmine.core.search.internal.AbstractRedmineSearchData;

public class RedmineProjectQueryListData extends AbstractRedmineSearchData {
	protected RedmineProjectData projectData;
	
	protected RedmineClientData clientData;

	public RedmineProjectQueryListData(RedmineProjectData projectData, RedmineClientData clientData) {
		this.clientData = clientData;
		this.projectData = projectData;
	}

	public List<RedminePriority> getPriorities() {
		return clientData.getPriorities();
	}
	public List<RedmineTicketStatus> getStatus() {
		return clientData.getStatuses();
	}

	public List<RedmineIssueCategory> getCategories() {
		return projectData.getCategorys();
	}

	public List<RedmineMember> getPersons() {
		return projectData.getMembers();
	}

	public List<RedmineMember> getMembers() {
		return projectData.getAssignableMembers();
	}

	public List<RedmineTracker> getTrackers() {
		return projectData.getTrackers();
	}

	public List<RedmineVersion> getVersions() {
		return projectData.getVersions();
	}

	public List<RedmineStoredQuery> getQueries() {
		return projectData.getStoredQueries();
	}
	
	public List<RedmineCustomField> getCustomTicketFields() {
		return projectData.getCustomTicketFields();
	}
	
}
