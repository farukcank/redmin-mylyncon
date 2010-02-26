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

package org.svenk.redmine.core.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.svenk.redmine.core.model.RedmineActivity;
import org.svenk.redmine.core.model.RedmineCustomField;
import org.svenk.redmine.core.model.RedmineMember;
import org.svenk.redmine.core.model.RedminePriority;
import org.svenk.redmine.core.model.RedmineTicketStatus;
import org.svenk.redmine.core.model.RedmineCustomField.CustomType;

public class RedmineClientData implements Serializable {
	
	private static final long serialVersionUID = 6L;

	List<RedminePriority> priorities = new ArrayList<RedminePriority>(); 
	
	List<RedmineTicketStatus> statuses = new ArrayList<RedmineTicketStatus>(); 

	List<RedmineActivity> activities = new ArrayList<RedmineActivity>();

	List<RedmineCustomField> customFields = new ArrayList<RedmineCustomField>();

	List<RedmineProjectData> projects = new ArrayList<RedmineProjectData>();
	
	long lastupdate=0;

	public List<RedminePriority> getPriorities() {
		Collections.sort(priorities);
		return Collections.unmodifiableList(priorities);
	}

	public List<RedmineTicketStatus> getStatuses() {
		return statuses;
	}

	public List<RedmineActivity> getActivities() {
		return activities;
	}
	
	public List<RedmineCustomField> getCustomFields() {
		return customFields;
	}
	
	public List<RedmineCustomField> getTimeEntryCustomFields() {
		//TODO optimize
		List<RedmineCustomField> fields = new ArrayList<RedmineCustomField>(customFields.size());
		
		for (RedmineCustomField customField : customFields) {
			if (customField.getCustomType()==CustomType.TimeEntryCustomField) {
				fields.add(customField);
			}
		}
		
		return fields;
	}
	
	public List<RedmineProjectData> getProjects() {
		return projects;
	}

	public RedmineProjectData getProjectFromName(String name) {
		for (RedmineProjectData project : projects) {
			if (project.getProject().getName().equals(name)) {
				return project;
			}
		}
		return null;
	}
	
	public RedmineProjectData getProjectFromId(int id) {
		for (RedmineProjectData project : projects) {
			if (project.getProject().getValue()==id) {
				return project;
			}
		}
		return null;
	}
	
	public RedminePriority getPriority(String name) {
		for (RedminePriority priority : priorities) {
			if (priority.getName().equals(name)) {
				return priority;
			}
		}
		return null;
	}
	
	public RedminePriority getPriority(int value) {
		for (RedminePriority priority : priorities) {
			if (priority.getValue()==value) {
				return priority;
			}
		}
		return null;
	}
	
	public RedmineTicketStatus getStatus(String name) {
		for (RedmineTicketStatus status : statuses) {
			if (status.getName().equals(name)) {
				return status;
			}
		}
		return null;
	}
	
	public RedmineTicketStatus getStatus(int value) {
		for (RedmineTicketStatus status : statuses) {
			if (status.getValue()==value) {
				return status;
			}
		}
		return null;
	}

	public RedmineActivity getActivity(int value) {
		for (RedmineActivity activity : activities) {
			if (activity.getValue()==value) {
				return activity;
			}
		}
		return null;
	}
	
	public RedmineMember getPerson(int value) {
		//TODO list with all members required (project independent)
		for (RedmineProjectData projectData : projects) {
			RedmineMember member = projectData.getMember(value);
			if(member!=null) {
				return member;
			}
		}
		return null;
	}

	public boolean needsUpdate() {
		return priorities.size()==0 || statuses.size()==0 || projects.size()==0;
	}
}
