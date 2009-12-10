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
package org.svenk.redmine.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.svenk.redmine.core.IRedmineConstants;
import org.svenk.redmine.core.client.RedmineProjectData;
import org.svenk.redmine.core.model.RedmineSearchFilter.CompareOperator;
import org.svenk.redmine.core.model.RedmineSearchFilter.SearchField;


public class RedmineSearch {

	public final static String SEARCH_PARAMS = "SEARCH_PARAMS";
	public final static String PROJECT_NAME = "PROJECT_NAME";
	public final static String PROJECT_ID = "PROJECT_ID";
	public final static String STORED_QUERY_ID = "QUERY_ID";
	
	private Map<SearchField, RedmineSearchFilter> filterBySearchField = new HashMap<SearchField, RedmineSearchFilter>();
	private Map<RedmineCustomField, RedmineSearchFilter> filterByCustomField = new HashMap<RedmineCustomField, RedmineSearchFilter>();

	private RedmineProject project;
	
	private int storedQueryId;
	
	private String repositoryUrl;
	
	private String queryParam;
	
	public RedmineSearch(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	public void addFilter(IRedmineQueryField queryField, CompareOperator operator, String value) {
		if (queryField instanceof RedmineCustomField) {
			addFilter((RedmineCustomField)queryField, operator, value);
		} else if (queryField instanceof SearchField) {
			addFilter((SearchField)queryField, operator, value);
		}
	}
	
	private void addFilter(RedmineCustomField customField, CompareOperator operator, String value) {
		RedmineSearchFilter filter = filterByCustomField.get(customField);
		if (filter == null) {
			filter = new RedmineSearchFilter(customField);
			filter.setOperator(operator);
			filterByCustomField.put(customField, filter);
		}
		
		filter.addValue(value);
		
		queryParam=null;
	}
	
	private void addFilter(SearchField searchField, CompareOperator operator, String value) {
		RedmineSearchFilter filter = filterBySearchField.get(searchField);
		if (filter == null) {
			filter = new RedmineSearchFilter(searchField);
			filter.setOperator(operator);
			filterBySearchField.put(searchField, filter);
		}
		
		filter.addValue(value);
		
		queryParam=null;
	}
	
	public RedmineSearchFilter getFilter(IRedmineQueryField queryField) {
		if (queryField instanceof RedmineCustomField) {
			return filterByCustomField.get(queryField);
		} else if (queryField instanceof SearchField) {
			return filterBySearchField.get(queryField);
		}
		throw new IllegalArgumentException();
	}

	public List<RedmineSearchFilter> getCustomFilters() {
		return new ArrayList<RedmineSearchFilter>(filterByCustomField.values());
	}
	
	public String toQuery() {
		StringBuffer sb = new StringBuffer(repositoryUrl);
		sb.append(IRedmineConstants.REDMINE_URL_QUERY);
		sb.append("?");
		sb.append(toSearchQueryParam());
		return sb.toString();
	}
	
	public String toSearchQueryParam() {
		if (queryParam==null) {
			StringBuffer sb = new StringBuffer("project_id=").append(project.getValue());
			sb.append("&set_filter=1");
			for (Iterator<RedmineSearchFilter> iterator = filterBySearchField.values().iterator(); iterator.hasNext();) {
				RedmineSearchFilter filter = iterator.next();
				filter.appendUrlPart(sb);
			}
			for (Iterator<RedmineSearchFilter> iterator = filterByCustomField.values().iterator(); iterator.hasNext();) {
				RedmineSearchFilter filter = iterator.next();
				filter.appendUrlPart(sb);
			}
			queryParam =  sb.toString();
		}
		return queryParam;
	}
	
	public static RedmineSearch fromSearchQueryParam(RedmineProjectData projectData, String param, String repositoryUrl) {
		RedmineSearch search = new RedmineSearch(repositoryUrl);
		
		List<SearchField> searchFields = 
			RedmineSearchFilter.findSearchFieldsFromSearchQueryParam(param);
		List<RedmineCustomField> customFields = 
			RedmineSearchFilter.findCustomTicketFieldsFromSearchQueryParam(projectData, param);
		
		CompareOperator operator;
		List<String> values;
		for (SearchField searchField : searchFields) {
			operator = RedmineSearchFilter.findOperatorFromQueryFieldParam(param, searchField);
			values = RedmineSearchFilter.findValuesFromQueryFieldParam(param, searchField);
			if (values.size()==0) {
				search.addFilter(searchField, operator, "");
			} else {
				for (String string : values) {
					search.addFilter(searchField, operator, string);
				}
			}
		}
		for (RedmineCustomField customField : customFields) {
			operator = RedmineSearchFilter.findOperatorFromQueryFieldParam(param, customField);
			values = RedmineSearchFilter.findValuesFromQueryFieldParam(param, customField);
			if (values.size()==0) {
				search.addFilter(customField, operator, "");
			} else {
				for (String string : values) {
					search.addFilter(customField, operator, string);
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
	
	public void setStoredQueryId(int id) {
		storedQueryId = id;
	}
	
	public int getStoredQueryId() {
		return storedQueryId;
	}
	
}
