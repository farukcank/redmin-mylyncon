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

import org.svenk.redmine.core.model.RedmineCustomField;
import org.svenk.redmine.core.model.RedmineIssueCategory;
import org.svenk.redmine.core.model.RedmineMember;
import org.svenk.redmine.core.model.RedmineProject;
import org.svenk.redmine.core.model.RedmineStoredQuery;
import org.svenk.redmine.core.model.RedmineTracker;
import org.svenk.redmine.core.model.RedmineVersion;

public class RedmineProjectData implements Serializable {
	
	private static final long serialVersionUID = 5L;

	RedmineProject project;
	
	List<RedmineTracker> trackers = new ArrayList<RedmineTracker>(); 
	
	List<RedmineIssueCategory> categorys = new ArrayList<RedmineIssueCategory>(); 
	
	List<RedmineVersion> versions = new ArrayList<RedmineVersion>(); 
	
	List<RedmineMember> members = new ArrayList<RedmineMember>();
	
	List<RedmineCustomField> customTicketFields = new ArrayList<RedmineCustomField>();

	List<RedmineStoredQuery> storedQueries = new ArrayList<RedmineStoredQuery>();
	
	public RedmineProjectData(RedmineProject project) {
		this.project = project;
	}
	
	public RedmineProject getProject() {
		return project;
	}

	public List<RedmineTracker> getTrackers() {
		return Collections.unmodifiableList(trackers);
	}
	
	public List<RedmineIssueCategory> getCategorys() {
		return Collections.unmodifiableList(categorys);
	}

	public List<RedmineVersion> getVersions() {
		return Collections.unmodifiableList(versions);
	}

	public List<RedmineMember> getMembers() {
		return Collections.unmodifiableList(members);
	}
	
	public List<RedmineMember> getAssignableMembers() {
		List<RedmineMember> list = new ArrayList<RedmineMember>(members.size());
		for (RedmineMember redmineMember : members) {
			if (redmineMember.isAssignable()) {
				list.add(redmineMember);
			}
		}
		return list;
	}
	
	public List<RedmineCustomField> getCustomTicketFields(int trackerId) {
		ArrayList<RedmineCustomField> customTicketValues = new ArrayList<RedmineCustomField>();
		for (RedmineCustomField v : this.customTicketFields) {
			if (v.usableForTracker(trackerId)) {
				customTicketValues.add(v);
			}
		}
		return Collections.unmodifiableList(customTicketValues);
	}

	public RedmineCustomField getCustomTicketField(int id) {
		for (RedmineCustomField customField : customTicketFields) {
			if (customField.getId()==id) {
				return customField;
			}
		}
		return null;
	}
	
	public List<RedmineCustomField> getCustomTicketFields() {
		return Collections.unmodifiableList(this.customTicketFields);
	}
	
	public List<RedmineStoredQuery> getStoredQueries() {
		return Collections.unmodifiableList(storedQueries);
	}
	
	public RedmineTracker getTracker(String name) {
		for (RedmineTracker tracker : trackers) {
			if (tracker.getName().equals(name)) {
				return tracker;
			}
		}
		return null;
	}

	public RedmineTracker getTracker(int value) {
		for (RedmineTracker tracker : trackers) {
			if (tracker.getValue()==value) {
				return tracker;
			}
		}
		return null;
	}
	
	public RedmineIssueCategory getCategory(String name) {
		for (RedmineIssueCategory category : categorys) {
			if (category.getName().equals(name)) {
				return category;
			}
		}
		return null;
	}

	public RedmineIssueCategory getCategory(int value) {
		for (RedmineIssueCategory category : categorys) {
			if (category.getValue()==value) {
				return category;
			}
		}
		return null;
	}
	
	public RedmineMember getMember(String name) {
		for (RedmineMember member : members) {
			if (member.getName().equals(name)) {
				return member;
			}
		}
		return null;
	}

	public RedmineMember getMember(int value) {
		for (RedmineMember member : members) {
			if (member.getValue()==value) {
				return member;
			}
		}
		return null;
	}
	
	public RedmineVersion getVersion(String name) {
		for (RedmineVersion version : versions) {
			if (version.getName().equals(name)) {
				return version;
			}
		}
		return null;
	}
	
	public RedmineVersion getVersion(int value) {
		for (RedmineVersion version : versions) {
			if (version.getValue()==value) {
				return version;
			}
		}
		return null;
	}

	public RedmineStoredQuery getStoredQuery(int value) {
		for (RedmineStoredQuery query : storedQueries) {
			if (query.getValue()==value) {
				return query;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass().equals(this.getClass()) && ((RedmineProjectData)obj).getProject().equals(getProject());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (null == project ? 0 : project.hashCode());
		return hash;
	}

}
