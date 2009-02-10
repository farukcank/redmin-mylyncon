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

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
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

	public static final String REVISION_URL = "/repositories/revision/";
//
//	public static final String MILESTONE_URL = "/milestone/";
//
//	public static final String BROWSER_URL = "/browser/";

	public static final String ATTACHMENT_URL = "/attachments/download/";

	public boolean hasAttributes();

	public RedmineClientData getClientData();
	
	public RedmineTicket getTicket(int id, IProgressMonitor monitor) throws RedmineException;
	
	public List<Integer> getChangedTicketId(Integer projectId, Date changedSince, IProgressMonitor monitor) throws RedmineException;
	
	public void search(String searchParam, String projectId, String storedQueryId, List<RedmineTicket> tickets, IProgressMonitor monitor) throws RedmineException;
	
	public void updateAttributes(boolean force, IProgressMonitor monitor) throws RedmineException;
	
	public int createTicket(RedmineTicket ticket, IProgressMonitor monitor) throws RedmineException;
	
	public void updateTicket(RedmineTicket ticket, String comment, IProgressMonitor monitor) throws RedmineException;

	public InputStream getAttachmentContent(int attachmentId, IProgressMonitor monitor) throws RedmineException;

	public void uploadAttachment(int ticketId, String fileName, String comment, String description, AbstractTaskAttachmentSource source, IProgressMonitor monitor) throws RedmineException;
	
	public String checkClientConnection() throws RedmineException;
	
	public void refreshRepositorySettings(TaskRepository repository);
	
	public boolean supportServersideStoredQueries();

	public boolean supportTaskRelations();
}
