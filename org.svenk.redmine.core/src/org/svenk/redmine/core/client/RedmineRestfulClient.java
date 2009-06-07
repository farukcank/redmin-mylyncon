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
package org.svenk.redmine.core.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.svenk.redmine.core.client.container.Version;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.exception.RedmineRemoteException;
import org.svenk.redmine.core.model.RedmineProject;
import org.svenk.redmine.core.model.RedmineTicket;
import org.svenk.redmine.core.model.RedmineTicketStatus;


public class RedmineRestfulClient extends AbstractRedmineClient {

	protected final static String PLACEHOLDER = "_PARAM_";
	
	protected final static String PATH_GET_VERSION = "/mylyn/version";

	protected final static String PATH_GET_PROJECTS = "/mylyn/projects";

	protected final static String PATH_GET_PRIORITIES = "/mylyn/priorities";
	
	protected final static String PATH_GET_ISSUE_STATUS = "/mylyn/issuestatus";
	
	protected final static String PATH_GET_TICKET = "/mylyn/issue/" + PLACEHOLDER;

	protected final static String PATH_SEARCH_TICKETS = "/mylyn/" + PLACEHOLDER + "/search";
	protected final static String PATH_SEARCH_TICKETS_QUERY_STRING = "query_string";
	protected final static String PATH_SEARCH_TICKETS_QUERY_ID = "query_id";

	protected final static String PATH_GET_CHANGED_TICKETS = "/mylyn/" + PLACEHOLDER + "/updatedsince";
	protected final static String PATH_GET_CHANGED_TICKETS_PARAM = "unixtime";

	private final static double PLUGIN_VERSION_2_5 = 2.5;

	private final RedmineRestfulReader reader;
	
	private double wsVersion = 0D;

	public RedmineRestfulClient(AbstractWebLocation location, RedmineClientData clientData, TaskRepository repository) {
		super(location, clientData, repository);
		refreshRepositorySettings(repository);
		
		reader = new RedmineRestfulReader();
	}

	public void refreshRepositorySettings(TaskRepository repository) {
		super.refreshRepositorySettings(repository);
		if (!repository.getVersion().equals(TaskRepository.NO_VERSION_SPECIFIED)) {
			wsVersion = getWsVersion(repository.getVersion());
		}
	}

	//TODO VersionsSTring gegen Model austauchen !!!
	private double getWsVersion(String version) {
		double v = 0D;
		int pos = version.lastIndexOf('v');
		if (pos>0 && version.length()>pos) {
			v = Double.parseDouble(version.substring(pos+1));
		}
		return v;
	} 

	@Override
	protected String checkClientVersion() throws RedmineException {
		GetMethod method = new GetMethod(PATH_GET_VERSION);
		
		executeMethod(method, null);
		InputStream in;
		try {
			in = method.getResponseBodyAsStream();
			Version version = reader.readVersion(in);
			
			String v = version.redmine.version + "v" + version.plugin.major + "." + version.plugin.minor;
			return v;
		} catch (IOException e) {
			throw new RedmineRemoteException(e.getMessage(), e);
		}
	}

	public List<Integer> getChangedTicketId(Integer projectId, Date changedSince, IProgressMonitor monitor) throws RedmineException {
		GetMethod method = new GetMethod(PATH_GET_CHANGED_TICKETS.replace(PLACEHOLDER, projectId.toString()));
		method.setQueryString(new NameValuePair[]{new NameValuePair(PATH_GET_CHANGED_TICKETS_PARAM, ""+changedSince.getTime()/1000)});
		
		executeMethod(method, monitor);
		InputStream in;
		try {
			in = method.getResponseBodyAsStream();
			List<Integer> list = reader.readUpdatedTickets(in);
			return list;
		} catch (IOException e) {
			throw new RedmineRemoteException(e.getMessage(), e);
		}
	}

	public RedmineClientData getClientData() {
		return data;
	}

	public RedmineTicket getTicket(int id, IProgressMonitor monitor) throws RedmineException {
		GetMethod method = new GetMethod(PATH_GET_TICKET.replace(PLACEHOLDER, ""+id));

		executeMethod(method, null);
		InputStream in;
		try {
			in = method.getResponseBodyAsStream();
			RedmineTicket ticket = reader.readTicket(in);
			if (ticket!=null) {
				completeAvailableStatus(ticket, data);
			}
			return ticket;
		} catch (IOException e) {
			throw new RedmineRemoteException(e.getMessage(), e);
		}
	}

	public boolean hasAttributes() {
		return data.lastupdate!=0;
	}

	public void search(String searchParam, String projectId, String storedQueryId, List<RedmineTicket> tickets, IProgressMonitor monitor) throws RedmineException {
		PostMethod method = new PostMethod(PATH_SEARCH_TICKETS.replace(PLACEHOLDER, projectId));
		
		if (storedQueryId==null || storedQueryId.equals("0")) {
			method.setQueryString(new NameValuePair[]{new NameValuePair(PATH_SEARCH_TICKETS_QUERY_STRING, searchParam)});
		} else {
			method.setQueryString(new NameValuePair[]{new NameValuePair(PATH_SEARCH_TICKETS_QUERY_ID, storedQueryId)});
		}

		executeMethod(method, null);
		InputStream in;
		try {
			in = method.getResponseBodyAsStream();
			List<RedmineTicket> list = reader.readTickets(in);
			for (RedmineTicket redmineTicket : list) {
				completeAvailableStatus(redmineTicket, data);
				tickets.add(redmineTicket);
			}
		} catch (IOException e) {
			throw new RedmineRemoteException(e.getMessage(), e);
		}
	}

	public boolean supportServersideStoredQueries() {
		return true;
	}

	public boolean supportTaskRelations() {
		return true;
	}

	public void updateAttributes(boolean force, IProgressMonitor monitor) throws RedmineException {
		if (!force && hasAttributes()) {
			return;
		}

		data.projects.clear();
		data.priorities.clear();
		data.statuses.clear();
		
		InputStream in;
		GetMethod method;

		monitor.beginTask("Updating attributes", 3);
		try {
			method = new GetMethod(PATH_GET_PROJECTS);
			executeMethod(method, monitor);
			in = method.getResponseBodyAsStream();
			data.projects = reader.readProjects(in);
			monitor.worked(1);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			method = new GetMethod(PATH_GET_PRIORITIES);
			executeMethod(method, monitor);
			in = method.getResponseBodyAsStream();
			data.priorities = reader.readPriorities(in);
			monitor.worked(1);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			method = new GetMethod(PATH_GET_ISSUE_STATUS);
			executeMethod(method, monitor);
			in = method.getResponseBodyAsStream();
			data.statuses = reader.readTicketStatuses(in);
			monitor.worked(1);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			data.lastupdate=new Date().getTime();
		} catch (IOException e) {
			throw new RedmineRemoteException(e.getMessage(), e);
		}
	}
	
	//TODO nach Entfernung des XmlRpc Clients den Status überarbeiten 
	public void completeAvailableStatus(RedmineTicket ticket, RedmineClientData clientData) {
		List<Integer> idList = ticket.getAvailableStatusList();
		List<RedmineTicketStatus> statuses = new ArrayList<RedmineTicketStatus>(idList==null ? 0 : idList.size()); 
		for (Integer intval : idList) {
			statuses.add(clientData.getStatus(intval));
		}
		ticket.setStatuses(statuses);
	}


}