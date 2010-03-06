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

import java.util.ArrayList;
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

public class RedmineCrossProjectQueryListData extends AbstractRedmineSearchData {

	protected RedmineClientData clientData;
	
	List<RedmineTracker> trackers;
	
	List<RedmineCustomField> customFields;
	
	List<RedmineMember> members;

	List<RedmineMember> persons;
	
	public RedmineCrossProjectQueryListData(RedmineClientData clientData) {
		this.clientData = clientData;
	}
	
	public List<RedminePriority> getPriorities() {
		return clientData.getPriorities();
	}
	public List<RedmineTicketStatus> getStatus() {
		return clientData.getStatuses();
	}
	
	public List<RedmineMember> getPersons() {
		if (persons==null) {
			persons = new ArrayList<RedmineMember>();
			for(RedmineProjectData projectData: clientData.getProjects()) {
				for(RedmineMember member : projectData.getMembers()) {
					if(!persons.contains(member)) {
						persons.add(member);
					}
				}
			}
		}
		return persons;
	}

	public List<RedmineIssueCategory> getCategories() {
		return null;
	}

	public List<RedmineMember> getMembers() {
		if (members==null) {
			members = new ArrayList<RedmineMember>();
			for(RedmineProjectData projectData: clientData.getProjects()) {
				for(RedmineMember member : projectData.getAssignableMembers()) {
					if(!members.contains(member)) {
						members.add(member);
					}
				}
			}
		}
		return members;
	}

	public List<RedmineTracker> getTrackers() {
		if(trackers==null) {
			trackers = new ArrayList<RedmineTracker>();
			for(RedmineProjectData projectData: clientData.getProjects()) {
				for(RedmineTracker tracker : projectData.getTrackers()) {
					if(!trackers.contains(tracker)) {
						trackers.add(tracker);
					}
				}
			}
		}
		return trackers;
	}

	public List<RedmineVersion> getVersions() {
		return null;
	}

	public List<RedmineStoredQuery> getQueries() {
		return null;
	}

	public List<RedmineCustomField> getCustomTicketFields() {
		if(customFields==null) {
			customFields = new ArrayList<RedmineCustomField>();
			for (RedmineCustomField cf : clientData.getIssueCustomFields()) {
				if (cf.crossProjectUsable()) {
					customFields.add(cf);
				}
			}
			
		}
		return customFields;
	}

}
