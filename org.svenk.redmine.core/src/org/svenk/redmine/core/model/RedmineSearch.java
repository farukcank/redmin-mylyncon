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

package org.svenk.redmine.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.svenk.redmine.core.IRedmineClient;
import org.svenk.redmine.core.model.RedmineSearchFilter.CompareOperator;
import org.svenk.redmine.core.model.RedmineSearchFilter.SearchField;


public class RedmineSearch {

	public final static String SEARCH_PARAMS = "SEARCH_PARAMS";
	public final static String PROJECT_NAME = "PROJECT_NAME";
	
	private Map<SearchField, RedmineSearchFilter> filterByFieldField = new HashMap<SearchField, RedmineSearchFilter>();

	private RedmineProject project;
	
	private String repositoryUrl;
	
	private String queryParam;
	
	public RedmineSearch(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	public void addFilter(SearchField searchField, String operator, String value) {
		CompareOperator compareOp = CompareOperator.fromString(operator);
		addFilter(searchField, compareOp, value);
	}

	public void addFilter(SearchField searchField, CompareOperator operator, String value) {
		RedmineSearchFilter filter = filterByFieldField.get(searchField);
		if (filter == null) {
			filter = new RedmineSearchFilter(searchField);
			filter.setOperator(operator);
			filterByFieldField.put(searchField, filter);
		}
		
		filter.addValue(value);
		
		queryParam=null;
	}
	
	public List<RedmineSearchFilter> getFilters() {
		return new ArrayList<RedmineSearchFilter>(filterByFieldField.values());
	}

	public String toQuery() {
		StringBuffer sb = new StringBuffer(repositoryUrl);
		sb.append(IRedmineClient.QUERY_URL);
		sb.append("?");
		sb.append(toSearchQueryParam());
		return sb.toString();
	}
	
	public String toSearchQueryParam() {
		if (queryParam==null) {
			StringBuffer sb = new StringBuffer("project_id=").append(project.getValue());
			sb.append("&set_filter=1");
			for (Iterator<RedmineSearchFilter> iterator = filterByFieldField.values().iterator(); iterator.hasNext();) {
				RedmineSearchFilter filter = iterator.next();
				filter.appendUrlPart(sb);
			}
			queryParam =  sb.toString();
		}
		return queryParam;
	}
	
	public static RedmineSearch fromSearchQueryParam(String param, String repositoryUrl) {
		RedmineSearch search = new RedmineSearch(repositoryUrl);
		
		List<SearchField> searchFields = RedmineSearchFilter.findFieldsFromSearchQueryParam(param);
		CompareOperator operator;
		List<String> values;
		for (SearchField searchField : searchFields) {
			operator = RedmineSearchFilter.findOperatorFromSearchQueryParam(param, searchField);
			values = RedmineSearchFilter.findValuesFromSearchQueryParam(param, searchField);
			if (values.size()==0) {
				search.addFilter(searchField, operator, "");
			} else {
				for (String string : values) {
					search.addFilter(searchField, operator, string);
				}
			}
		}
		return search;
	}
	
	public RedmineProject getProject() {
		return project;
	}

	public void setProject(RedmineProject project) {
		this.project = project;
	}
	
}
