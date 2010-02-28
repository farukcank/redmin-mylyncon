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
import org.svenk.redmine.core.model.RedmineTicketAttribute;
import org.svenk.redmine.core.model.RedmineTicketStatus;
import org.svenk.redmine.core.model.RedmineTracker;
import org.svenk.redmine.core.model.RedmineVersion;

public abstract class AbstractRedmineSearchData implements IRedmineSearchData {

	public RedmineIssueCategory getCategory(int id) {
		return getById(getCategories(), id);
	}

	public RedmineCustomField getCustomTicketField(int id) {
		List<RedmineCustomField> list = getCustomTicketFields();
		if(list!=null) {
			for (RedmineCustomField cf : list) {
				if(cf.getId()==id) {
					return cf;
				}
			}
		}
		return null;
	}

	public RedmineMember getMember(int id) {
		return getById(getMembers(), id);
	}

	public RedmineMember getPerson(int id) {
		return getById(getPersons(), id);
	}

	public RedminePriority getPriority(int id) {
		return getById(getPriorities(), id);
	}

	public RedmineStoredQuery getQuery(int id) {
		return getById(getQueries(), id);
	}

	public RedmineTicketStatus getStatus(int id) {
		return getById(getStatus(), id);
	}

	public RedmineTracker getTracker(int id) {
		return getById(getTrackers(), id);
	}

	public RedmineVersion getVersion(int id) {
		return getById(getVersions(), id);
	}

	protected <T extends RedmineTicketAttribute> T getById(List<T> list, int id) {
		if(list!=null) {
			for (T t : list) {
				if(t.getValue()==id) {
					return t;
				}
			}
		}
		return null;
	}

}
