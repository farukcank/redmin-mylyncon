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
package org.svenk.redmine.core.search.internal;

import java.util.List;

import org.svenk.redmine.core.model.RedmineCustomField;
import org.svenk.redmine.core.model.RedmineIssueCategory;
import org.svenk.redmine.core.model.RedmineMember;
import org.svenk.redmine.core.model.RedminePriority;
import org.svenk.redmine.core.model.RedmineStoredQuery;
import org.svenk.redmine.core.model.RedmineTicketStatus;
import org.svenk.redmine.core.model.RedmineTracker;
import org.svenk.redmine.core.model.RedmineVersion;

public interface IRedmineSearchData {
	
	List<RedmineTracker> getTrackers();

	RedmineTracker getTracker(int id);
	
	List<RedmineTicketStatus> getStatus();

	RedmineTicketStatus getStatus(int id);
	
	List<RedmineMember> getMembers();

	RedmineMember getMember(int id);
	
	List<RedmineMember> getPersons();

	RedmineMember getPerson(int id);
	
	List<RedmineVersion> getVersions();

	RedmineVersion getVersion(int id);
	
	List<RedmineIssueCategory> getCategories();

	RedmineIssueCategory getCategory(int id);
	
	List<RedminePriority> getPriorities();

	RedminePriority getPriority(int id);

	List<RedmineStoredQuery> getQueries();

	RedmineStoredQuery getQuery(int id);

	List<RedmineCustomField> getCustomTicketFields();

	RedmineCustomField getCustomTicketField(int id);
}
