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

package org.svenk.redmine.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.svenk.redmine.core.model.RedmineIssueCategory;
import org.svenk.redmine.core.model.RedmineMember;
import org.svenk.redmine.core.model.RedmineProject;
import org.svenk.redmine.core.model.RedmineTracker;
import org.svenk.redmine.core.model.RedmineVersion;

public class RedmineProjectData implements Serializable {
	

	private static final long serialVersionUID = -7263785468252846028L;

	RedmineProject project;
	
	List<RedmineTracker> trackers = new ArrayList<RedmineTracker>(); 
	
	List<RedmineIssueCategory> categorys = new ArrayList<RedmineIssueCategory>(); 
	
	List<RedmineVersion> versions = new ArrayList<RedmineVersion>(); 
	
	List<RedmineMember> members = new ArrayList<RedmineMember>(); 
	
	RedmineProjectData(RedmineProject project) {
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
	

}
