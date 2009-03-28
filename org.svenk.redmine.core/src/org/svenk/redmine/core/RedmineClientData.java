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

package org.svenk.redmine.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.svenk.redmine.core.model.RedminePriority;
import org.svenk.redmine.core.model.RedmineTicketStatus;

public class RedmineClientData implements Serializable {
	
	private static final long serialVersionUID = 2411077852012039140L;

	List<RedminePriority> priorities = new ArrayList<RedminePriority>(); 
	
	List<RedmineTicketStatus> statuses = new ArrayList<RedmineTicketStatus>(); 

	List<RedmineProjectData> projects = new ArrayList<RedmineProjectData>();
	
	long lastupdate=0;

	public List<RedminePriority> getPriorities() {
		return Collections.unmodifiableList(priorities);
	}

	public List<RedmineTicketStatus> getStatuses() {
		return statuses;
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
	
	
}
