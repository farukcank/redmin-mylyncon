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

package org.svenk.redmine.ui;


public class RedmineTaskListFactory /*extends AbstractTaskListFactory*/ {

//	private static final String KEY_REDMINE = "Redmine";
//
//	private static final String KEY_REDMINE_TASK = KEY_REDMINE + KEY_TASK;
//	
//	private static final String KEY_REDMINE_QUERY = KEY_REDMINE + KEY_QUERY;

	public RedmineTaskListFactory() {
		// TODO Auto-generated constructor stub
	}

//	@Override
//	public boolean canCreate(AbstractTask task) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public AbstractTask createTask(String repositoryUrl, String taskId,
//			String label, Element element) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getTaskElementName() {
//		return KEY_REDMINE_TASK;
//	}
//
//	@Override
//	public void setAdditionalAttributes(AbstractTask task, Element element) {
//		// TODO Auto-generated method stub
//		super.setAdditionalAttributes(task, element);
//	}
//
//	/* QUERY **************************/
//	
//	@Override
//	public boolean canCreate(AbstractRepositoryQuery query) {
//		return query instanceof RedmineRepositoryQuery;
//	}
//
//	@Override
//	public AbstractRepositoryQuery createQuery(String repositoryUrl,
//			String queryString, String label, Element element) {
//
//		RedmineRepositoryQuery query = new RedmineRepositoryQuery(repositoryUrl, queryString, label);
//		query.setSearch(RedmineSearch.fromElement(repositoryUrl, element));
//		return query;
//	}
//
//	@Override
//	public Set<String> getQueryElementNames() {
//		Set<String> names = new HashSet<String>();
//		names.add(KEY_REDMINE_QUERY);
//		return names;
//	}
//
//	@Override
//	public String getQueryElementName(AbstractRepositoryQuery query) {
//		return query instanceof RedmineRepositoryQuery ? KEY_REDMINE_QUERY : "";
//	}
//	
//	@Override
//	public void setAdditionalAttributes(AbstractRepositoryQuery query,
//			Element node) {
//		if (query instanceof RedmineRepositoryQuery) {
//			RedmineSearch search = ((RedmineRepositoryQuery)query).getSearch();
//			search.appendElement(node);
//		}
//	}
}
