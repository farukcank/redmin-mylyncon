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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.junit.Test;
import org.svenk.redmine.core.model.RedmineSearchFilter.CompareOperator;
import org.svenk.redmine.core.model.RedmineSearchFilter.SearchField;

public class RedmineSearchFilterTest {

	@Test
	public void testRedmineSearchFilterString() {
		fail("Not yet implemented");
	}

	@Test
	public void testRedmineSearchFilterStringString() {
		fail("Not yet implemented");
	}

	@Test
	public void testStatusAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		RedmineSearchFilter filter = new RedmineSearchFilter(SearchField.STATUS);

		sb.setLength(0);
		filter.setOperator(CompareOperator.OPEN);
		filter.appendUrlPart(sb);
		assertEquals("&fields[]=status_id&operators[status_id]=o&values[status_id][]", sb.toString());

		sb.setLength(0);
		filter.setOperator(CompareOperator.CLOSED);
		filter.appendUrlPart(sb);
		assertEquals("&fields[]=status_id&operators[status_id]=c&values[status_id][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.addValue("3");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=status_id&operators[status_id]=" + operator + "&values[status_id][]=3", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS_NOT);
		filter.addValue("3");
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS_NOT.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=status_id&operators[status_id]=" + operator + "&values[status_id][]=3&values[status_id][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.ALL);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.ALL.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=status_id&operators[status_id]=" + operator + "&values[status_id][]", sb.toString());
	}
	
	@Test
	public void testTrackerAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		RedmineSearchFilter filter = new RedmineSearchFilter(SearchField.TRACKER);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.addValue("3");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=tracker_id&operators[tracker_id]=" + operator + "&values[tracker_id][]=3", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS_NOT);
		filter.addValue("3");
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS_NOT.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=tracker_id&operators[tracker_id]=" + operator + "&values[tracker_id][]=3&values[tracker_id][]=4", sb.toString());

		sb.setLength(0);
		filter.setOperator(CompareOperator.ALL);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}
	
	@Test
	public void testPriorityAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		RedmineSearchFilter filter = new RedmineSearchFilter(SearchField.PRIORITY);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.addValue("3");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=priority_id&operators[priority_id]=" + operator + "&values[priority_id][]=3", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS_NOT);
		filter.addValue("3");
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS_NOT.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=priority_id&operators[priority_id]=" + operator + "&values[priority_id][]=3&values[priority_id][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.ALL);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}
	
	@Test
	public void testAuthorAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		RedmineSearchFilter filter = new RedmineSearchFilter(SearchField.AUTHOR);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.addValue("3");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=author_id&operators[author_id]=" + operator + "&values[author_id][]=3", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS_NOT);
		filter.addValue("3");
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS_NOT.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=author_id&operators[author_id]=" + operator + "&values[author_id][]=3&values[author_id][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.ALL);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}
	
	@Test
	public void testAssignedAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		RedmineSearchFilter filter = new RedmineSearchFilter(SearchField.ASSIGNED_TO);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.addValue("3");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=assigned_to_id&operators[assigned_to_id]=" + operator + "&values[assigned_to_id][]=3", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS_NOT);
		filter.addValue("3");
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS_NOT.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=assigned_to_id&operators[assigned_to_id]=" + operator + "&values[assigned_to_id][]=3&values[assigned_to_id][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.ALL);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.ALL.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=assigned_to_id&operators[assigned_to_id]=" + operator + "&values[assigned_to_id][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.NONE);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.NONE.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=assigned_to_id&operators[assigned_to_id]=" + operator + "&values[assigned_to_id][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CONTAINS);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}
	
	@Test
	public void testFixedVersionAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		RedmineSearchFilter filter = new RedmineSearchFilter(SearchField.FIXED_VERSION);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.addValue("3");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=fixed_version_id&operators[fixed_version_id]=" + operator + "&values[fixed_version_id][]=3", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS_NOT);
		filter.addValue("3");
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS_NOT.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=fixed_version_id&operators[fixed_version_id]=" + operator + "&values[fixed_version_id][]=3&values[fixed_version_id][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.ALL);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.ALL.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=fixed_version_id&operators[fixed_version_id]=" + operator + "&values[fixed_version_id][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.NONE);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.NONE.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=fixed_version_id&operators[fixed_version_id]=" + operator + "&values[fixed_version_id][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CONTAINS);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}
	
	@Test
	public void testSubjectAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		RedmineSearchFilter filter = new RedmineSearchFilter(SearchField.SUBJECT);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CONTAINS);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CONTAINS);
		filter.addValue("some_text");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.CONTAINS.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=subject&operators[subject]=" + operator + "&values[subject][]=some_text", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CONTAINS_NOT);
		filter.addValue("some_text");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.CONTAINS_NOT.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=subject&operators[subject]=" + operator + "&values[subject][]=some_text", sb.toString());

		sb.setLength(0);
		filter.setOperator(CompareOperator.NONE);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());

	}
	
	@Test
	public void testDoneRatioAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		RedmineSearchFilter filter = new RedmineSearchFilter(SearchField.DONE_RATIO);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.GTE);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.GTE);
		filter.addValue("none_int");
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		filter = new RedmineSearchFilter(SearchField.DONE_RATIO);
		sb.setLength(0);
		filter.setOperator(CompareOperator.GTE);
		filter.addValue("-1");
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		filter = new RedmineSearchFilter(SearchField.DONE_RATIO);
		sb.setLength(0);
		filter.setOperator(CompareOperator.GTE);
		filter.addValue("101");
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		filter = new RedmineSearchFilter(SearchField.DONE_RATIO);
		sb.setLength(0);
		filter.setOperator(CompareOperator.GTE);
		filter.addValue("50");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.GTE.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=done_ratio&operators[done_ratio]=" + operator + "&values[done_ratio][]=50", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.LTE);
		filter.addValue("50");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.LTE.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=done_ratio&operators[done_ratio]=" + operator + "&values[done_ratio][]=50", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.NONE);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}
	
	@Test
	public void testCreatedAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		RedmineSearchFilter filter = new RedmineSearchFilter(SearchField.DATE_CREATED);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_MORE_THEN);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_LESS_THEN);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.TODAY);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.TODAY.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=created_on&operators[created_on]=" + operator + "&values[created_on][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CURRENT_WEEK);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.CURRENT_WEEK.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=created_on&operators[created_on]=" + operator + "&values[created_on][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_MORE_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO_MORE_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=created_on&operators[created_on]=" + operator + "&values[created_on][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_LESS_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO_LESS_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=created_on&operators[created_on]=" + operator + "&values[created_on][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=created_on&operators[created_on]=" + operator + "&values[created_on][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.appendUrlPart(sb);
		filter.setOperator(CompareOperator.IS);
		assertEquals("", sb.toString());
	}
	
	@Test
	public void testUpdatedAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		RedmineSearchFilter filter = new RedmineSearchFilter(SearchField.DATE_UPDATED);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_MORE_THEN);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_LESS_THEN);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.TODAY);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.TODAY.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=updated_on&operators[updated_on]=" + operator + "&values[updated_on][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CURRENT_WEEK);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.CURRENT_WEEK.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=updated_on&operators[updated_on]=" + operator + "&values[updated_on][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_MORE_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO_MORE_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=updated_on&operators[updated_on]=" + operator + "&values[updated_on][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_LESS_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO_LESS_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=updated_on&operators[updated_on]=" + operator + "&values[updated_on][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=updated_on&operators[updated_on]=" + operator + "&values[updated_on][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}
	
	@Test
	public void testStartAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		RedmineSearchFilter filter = new RedmineSearchFilter(SearchField.DATE_START);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_MORE_THEN);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_LESS_THEN);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER_LESS_THEN);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER_MORE_THEN);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.TODAY);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.TODAY.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CURRENT_WEEK);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.CURRENT_WEEK.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_MORE_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO_MORE_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_LESS_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO_LESS_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_LATER.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER_LESS_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_LATER_LESS_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER_MORE_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_LATER_MORE_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}
	
	@Test
	public void testDueAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		RedmineSearchFilter filter = new RedmineSearchFilter(SearchField.DATE_DUE);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_MORE_THEN);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_LESS_THEN);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER_LESS_THEN);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER_MORE_THEN);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.TODAY);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.TODAY.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CURRENT_WEEK);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.CURRENT_WEEK.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_MORE_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO_MORE_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_LESS_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO_LESS_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_LATER.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER_LESS_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_LATER_LESS_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER_MORE_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_LATER_MORE_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]=start_date&operators[start_date]=" + operator + "&values[start_date][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}

	@Test
	public void testFindFieldsFromSearchQueryParam() {
		String searchParam = "&fields[]=status_id&operators[status_id]&fields[]=tracker_id&operators[tracker_id]=&fields[]=author_id&operators[author_id]=";
		List<SearchField> fields = RedmineSearchFilter.findSearchFieldsFromSearchQueryParam(searchParam);
		assertEquals(3, fields.size());
		assertEquals(SearchField.STATUS, fields.get(0));
		assertEquals(SearchField.TRACKER, fields.get(1));
		assertEquals(SearchField.AUTHOR, fields.get(2));
	}
	
	@Test
	public void testFindOperatorFromSearchQueryParam() throws UnsupportedEncodingException {
		StringBuffer candidate = new StringBuffer("&fields[]=status_id");
		
		candidate.append("&operators[" + SearchField.STATUS.getQueryValue()+ "]=");
		candidate.append(URLEncoder.encode(CompareOperator.ALL.getQueryValue(), "UTF-8"));
		
		candidate.append("&operators[" + SearchField.DATE_UPDATED.getQueryValue()+ "]=");
		candidate.append(URLEncoder.encode(CompareOperator.LTE.getQueryValue(), "UTF-8"));
		
		candidate.append("&operators[" + SearchField.TRACKER.getQueryValue()+ "]=");
		candidate.append(URLEncoder.encode(CompareOperator.CONTAINS_NOT.getQueryValue(), "UTF-8"));
		
		String searchParam = candidate.toString();
		
		assertEquals(CompareOperator.ALL, RedmineSearchFilter.findOperatorFromQueryFieldParam(searchParam, SearchField.STATUS));
		assertEquals(CompareOperator.LTE, RedmineSearchFilter.findOperatorFromQueryFieldParam(searchParam, SearchField.DATE_UPDATED));
		assertEquals(CompareOperator.CONTAINS_NOT, RedmineSearchFilter.findOperatorFromQueryFieldParam(searchParam, SearchField.TRACKER));
		assertNull(RedmineSearchFilter.findOperatorFromQueryFieldParam(searchParam, SearchField.PRIORITY));
	}

	@Test
	public void testFindValuesFromSearchQueryParam() throws UnsupportedEncodingException {
		StringBuffer candidate = new StringBuffer("&fields[]=status_id");
		
		candidate.append("&values[" + SearchField.STATUS.getQueryValue()+ "][]=v1");
		candidate.append("&values[" + SearchField.STATUS.getQueryValue()+ "][]=v2");
		candidate.append("&values[" + SearchField.TRACKER.getQueryValue()+ "][]=v3");
		
		String searchParam = candidate.toString();
		
		List<String> values = RedmineSearchFilter.findValuesFromQueryFieldParam(searchParam, SearchField.STATUS);
		assertEquals(2, values.size());
		assertEquals("v1", values.get(0));
		assertEquals("v2", values.get(1));
		values = RedmineSearchFilter.findValuesFromQueryFieldParam(searchParam, SearchField.TRACKER);
		assertEquals(1, values.size());
		assertEquals("v3", values.get(0));
		values = RedmineSearchFilter.findValuesFromQueryFieldParam(searchParam, SearchField.ASSIGNED_TO);
		assertEquals(0, values.size());
	}

	@Test
	public void testCustomListValueAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		
		int idVal = 4;
		RedmineCustomTicketField customField = 
			new RedmineCustomTicketField(idVal, RedmineCustomTicketField.FieldType.LIST.name());
		String id = customField.getQueryValue();
		
		RedmineSearchFilter filter = new RedmineSearchFilter(customField);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.addValue("3");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=3", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS_NOT);
		filter.addValue("3");
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS_NOT.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=3&values["+id+"][]=4", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.ALL);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.ALL.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.NONE);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.NONE.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CONTAINS);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}

	@Test
	public void testCustomTextValueAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		
		int idVal = 4;
		RedmineCustomTicketField customField = 
			new RedmineCustomTicketField(idVal, RedmineCustomTicketField.FieldType.TEXT.name());
		String id = customField.getQueryValue();
		
		RedmineSearchFilter filter = new RedmineSearchFilter(customField);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.addValue("istext");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=istext", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS_NOT);
		filter.addValue("isnottext");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS_NOT.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=isnottext", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CONTAINS);
		filter.addValue("containstext");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.CONTAINS.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=containstext", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CONTAINS_NOT);
		filter.addValue("containsnottext");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.CONTAINS_NOT.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=containsnottext", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.NONE);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}

	@Test
	public void testCustomDateValueAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;

		int idVal = 4;
		RedmineCustomTicketField customField = 
			new RedmineCustomTicketField(idVal, RedmineCustomTicketField.FieldType.DATE.name());
		String id = customField.getQueryValue();
		
		RedmineSearchFilter filter = new RedmineSearchFilter(customField);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.TODAY);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.TODAY.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"]", sb.toString(), sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CURRENT_WEEK);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.CURRENT_WEEK.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"]", sb.toString(), sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_MORE_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO_MORE_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=4", sb.toString(), sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO_LESS_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO_LESS_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=4", sb.toString(), sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_AGO);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_AGO.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=4", sb.toString(), sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_LATER.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=4", sb.toString(), sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER_LESS_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_LATER_LESS_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=4", sb.toString(), sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.DAY_LATER_MORE_THEN);
		filter.addValue("4");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.DAY_LATER_MORE_THEN.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=4", sb.toString(), sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}

	@Test
	public void testCustomBooleanValueAppendUrlPart() throws Exception {
		StringBuffer sb = new StringBuffer();
		String operator;
		
		int idVal = 4;
		RedmineCustomTicketField customField = 
			new RedmineCustomTicketField(idVal, RedmineCustomTicketField.FieldType.BOOL.name());
		String id = customField.getQueryValue();
		
		RedmineSearchFilter filter = new RedmineSearchFilter(customField);
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.addValue("2");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS);
		filter.addValue("1");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=1", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.IS_NOT);
		filter.addValue("0");
		filter.appendUrlPart(sb);
		operator = URLEncoder.encode(CompareOperator.IS_NOT.getQueryValue(), "UTF-8");
		assertEquals("&fields[]="+id+"&operators["+id+"]=" + operator + "&values["+id+"][]=0", sb.toString());
		
		sb.setLength(0);
		filter.setOperator(CompareOperator.CONTAINS);
		filter.appendUrlPart(sb);
		assertEquals("", sb.toString());
	}
}
