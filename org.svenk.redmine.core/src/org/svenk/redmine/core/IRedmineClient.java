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

import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.model.RedmineTicket;


public interface IRedmineClient {
	

//	public static final String CHARSET = "UTF-8";
//
//	public static final String TIME_ZONE = "UTC";
//
	public static final String LOGIN_URL = "/login";

	public static final String QUERY_URL = "/issues";

	public final static String TICKET_URL = "/issues/show/";

	public final static String COMMENT_URL = "#note-";

	public static final String TICKET_NEW_URL = "/issues/new";

	public static final String TICKET_EDIT_URL = "/issues/edit/";
//
//	public static final String CUSTOM_QUERY_URL = "/query";
//
//	public static final String DEFAULT_USERNAME = "anonymous";
//
//	public static final String WIKI_URL = "/wiki/";
//
//	public static final String REPORT_URL = "/report/";
//
//	public static final String CHANGESET_URL = "/changeset/";
//
//	public static final String REVISION_LOG_URL = "/log/";
//
//	public static final String MILESTONE_URL = "/milestone/";
//
//	public static final String BROWSER_URL = "/browser/";

	public static final String ATTACHMENT_URL = "/attachments/download/";

	public boolean hasAttributes();

	public RedmineClientData getClientData();
	
	public RedmineTicket getTicket(int id, IProgressMonitor monitor) throws RedmineException;
	
	public List<Integer> getChangedTicketId(Integer projectId, Date changedSince) throws RedmineException;
	
	public void search(String searchParam, List<RedmineTicket> tickets) throws RedmineException;
	
	public void updateAttributes(IProgressMonitor monitor, boolean force) throws RedmineException;
	public void updateProjectAttributes(IProgressMonitor monitor, RedmineProjectData projData) throws RedmineException;
	
	public int createTicket(RedmineTicket ticket, IProgressMonitor monitor) throws RedmineException;
	
	public void updateTicket(RedmineTicket ticket, String comment, IProgressMonitor monitor) throws RedmineException;

	public void checkClientConnection() throws RedmineException;
}
