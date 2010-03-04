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
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.client.container.Version;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.model.RedmineTicket;


public interface IRedmineClient {

public boolean hasAttributes();

	public RedmineClientData getClientData();
	
	public RedmineTicket getTicket(int id, IProgressMonitor monitor) throws RedmineException;
	
	public List<Integer> getChangedTicketId(Integer projectId, Date changedSince, IProgressMonitor monitor) throws RedmineException;
	
	public void search(String searchParam, String projectId, String storedQueryId, List<RedmineTicket> tickets, IProgressMonitor monitor) throws RedmineException;
	
	public void updateAttributes(boolean force, IProgressMonitor monitor) throws RedmineException;
	
	public int createTicket(String project, Map<String, String> postValues, IProgressMonitor monitor) throws RedmineException;
	
	public void updateTicket(int ticketId, Map<String, String> postValues, String comment, IProgressMonitor monitor) throws RedmineException;

	public InputStream getAttachmentContent(int attachmentId, IProgressMonitor monitor) throws RedmineException;

	public void uploadAttachment(int ticketId, String fileName, String comment, String description, AbstractTaskAttachmentSource source, IProgressMonitor monitor) throws RedmineException;
	
	public Version checkClientConnection(IProgressMonitor monitor) throws RedmineException;
	
	public void refreshRepositorySettings(TaskRepository repository, AbstractWebLocation location);
	
	public boolean supportStartDueDate();

	public boolean supportTimeEntries();
}
