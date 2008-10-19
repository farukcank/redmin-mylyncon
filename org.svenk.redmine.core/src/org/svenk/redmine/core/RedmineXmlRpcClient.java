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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.exception.RedmineRemoteException;
import org.svenk.redmine.core.model.RedmineIssueCategory;
import org.svenk.redmine.core.model.RedmineMember;
import org.svenk.redmine.core.model.RedminePriority;
import org.svenk.redmine.core.model.RedmineProject;
import org.svenk.redmine.core.model.RedmineTicket;
import org.svenk.redmine.core.model.RedmineTicketJournal;
import org.svenk.redmine.core.model.RedmineTicketStatus;
import org.svenk.redmine.core.model.RedmineTracker;
import org.svenk.redmine.core.model.RedmineVersion;
import org.svenk.redmine.core.model.RedmineTicket.Key;
import org.svenk.redmine.core.util.RedmineTransportFactory;

public class RedmineXmlRpcClient extends AbstractRedmineClient implements IRedmineClient {

	private final static String URL_ENDPOINT = "/eclipse_mylyn_connector/api";

	private final static String RPC_TICKET_BY_ID = "Ticket.FindTicketById";

	private final static String RPC_TICKET_SEARCH = "Ticket.SearchTickets";

	private final static String RPC_TICKET_UPDATE = "Ticket.UpdateTicket";

	private final static String RPC_GET_TICKET_JOURNALS = "Ticket.FindJournalsForIssue";

	private final static String RPC_GET_TICKET_ALLOWED_STATUS = "Ticket.FindAllowedStatusesForIssue";

	private final static String RPC_GET_TICKET_UPDATED_ID = "Ticket.FindTicketsByLastUpdate";
	
	private final static String RPC_PROJECT_FIND_ALL = "Project.FindAll";

	private final static String RPC_STATUS_FIND_ALL = "Status.GetAll";

	private final static String RPC_PRIORITY_FIND_ALL = "Priority.GetAll";

	private final static String RPC_GET_PROJECT_TRACKERS = "ProjectBased.GetTrackersForProject";
	
	private final static String RPC_GET_PROJECT_MEMBERS = "ProjectBased.GetMembersForProject";

	private final static String RPC_GET_PROJECT_VERSIONS = "ProjectBased.GetVersionsForProject";

	private final static String RPC_GET_PROJECT_ISSUE_CATEGORYS = "ProjectBased.GetIssueCategorysForProject";

	private XmlRpcClientConfigImpl config;

	private XmlRpcClient client;
	
	public RedmineXmlRpcClient(AbstractWebLocation location, RedmineClientData clientData) {
		super(location, clientData);
	}

	private XmlRpcClient getClient() throws RedmineException {
		if (client == null) {
			config = new XmlRpcClientConfigImpl();

			client = new XmlRpcClient();
			client.setConfig(config);
			
			RedmineTransportFactory factory = new RedmineTransportFactory(client, location, URL_ENDPOINT);
			client.setTransportFactory(factory);
		}
		return client;
	}

	public RedmineClientData getClientData() {
		return data;
	}
	
	public RedmineTicket getTicket(int id, IProgressMonitor monitor) throws RedmineException {
		RedmineTicket ticket =  parseResponse2Ticket(execute(RPC_TICKET_BY_ID, new Integer(id)));
		completeTicket(ticket);
		return ticket;
	}

	protected List<RedmineTicketStatus> getStatusesByTicket(int id) throws RedmineException {
		return parseResponse2Statuses(execute(RPC_GET_TICKET_ALLOWED_STATUS, new Integer(id)));
	}
	
	protected List<RedmineTicketJournal> getJournalsByTicket(int id) throws RedmineException {
		return parseResponse2Journals(execute(RPC_GET_TICKET_JOURNALS, new Integer(id)));
	}
	
	public void search(String searchParam, List<RedmineTicket> tickets)
			throws RedmineException {
		Object response = execute(RPC_TICKET_SEARCH, searchParam);
		if (response instanceof Object[]) {
			Object[] maps = (Object[]) response;
			for (Object object : maps) {
				RedmineTicket ticket = parseResponse2Ticket(object);
				completeTicket(ticket);
				tickets.add(ticket);
			}
		}
	}
	
	protected void completeTicket(RedmineTicket ticket) throws RedmineException {
		List<RedmineTicketStatus> statuses = getStatusesByTicket(ticket.getId());
		ticket.setStatuses(statuses);

		List<RedmineTicketJournal> journals = getJournalsByTicket(ticket.getId());
		if (journals!=null) {
			for (RedmineTicketJournal journal : journals) {
				ticket.addJournal(journal);
			}
		}

	}

	public List<Integer> getChangedTicketId(Integer projectId, Date changedSince) throws RedmineException {
		Object[] params = new Object[]{projectId, changedSince};
		Object response = execute(RPC_GET_TICKET_UPDATED_ID, params);
		return parseResponse2IntegerList(response);
	}

	public boolean hasAttributes() {
		return data.lastupdate!=0;
	}

	public synchronized void updateAttributes(IProgressMonitor monitor, boolean force)
			throws RedmineException {
		
		if (!force && hasAttributes()) {
			return;
		}
		

		try {
			Object projects[] = (Object[])execute(RPC_PROJECT_FIND_ALL);
			monitor.beginTask("Updating attributes", 2+projects.length);

			data.priorities.clear();
			for (Object response : (Object[]) execute(RPC_PRIORITY_FIND_ALL)) {
				data.priorities.add(parseResponse2Priority(response));
			}
			monitor.worked(1);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			data.statuses.clear();
			for (Object response : (Object[]) execute(RPC_STATUS_FIND_ALL)) {
				data.statuses.add(parseResponse2Status(response));
			}
			monitor.worked(1);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			data.projects.clear();
			for (Object response : projects) {
				RedmineProject project = parseResponse2Project(response);
				RedmineProjectData projData = new RedmineProjectData(project);
				data.projects.add(projData);
				updateProjectAttributes(monitor, projData);
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			data.lastupdate=new Date().getTime();
		} catch (RedmineException e) {
			// TODO log exception
			throw new RedmineException(e);
		}
	}
	
	public synchronized void updateProjectAttributes(IProgressMonitor monitor, RedmineProjectData projData) throws RedmineException {
		Integer projId = new Integer(projData.getProject().getValue());

		monitor.subTask("project " + projData.project.getName());
		try {
			projData.trackers.clear();
			for (Object projResponse : (Object[]) execute(RPC_GET_PROJECT_TRACKERS, projId)) {
				projData.trackers.add(parseResponse2Tracker(projResponse));
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			projData.categorys.clear();
			for (Object projResponse : (Object[]) execute(RPC_GET_PROJECT_ISSUE_CATEGORYS, projId)) {
				projData.categorys.add(parseResponse2IssueCategory(projResponse));
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			projData.versions.clear();
			for (Object projResponse : (Object[]) execute(RPC_GET_PROJECT_VERSIONS, projId)) {
				projData.versions.add(parseResponse2Version(projResponse));
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			projData.members.clear();
			for (Object projResponse : (Object[]) execute(RPC_GET_PROJECT_MEMBERS, projId)) {
				projData.members.add(parseResponse2Member(projResponse));
			}
			monitor.worked(1);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		} catch (RedmineException e) {
			// TODO log exception
			throw new RedmineException(e);
		}
	}

	public void updateTicket(RedmineTicket ticket, String comment) throws RedmineException {
		Map<String, Object> ticketValues = parseTicket2Request(ticket);
		execute(RPC_TICKET_UPDATE, new Object[]{ticketValues, comment});
	}

	private Object execute(String rpc, Object... params)
			throws RedmineException {
		XmlRpcClient client = getClient();
		Object result = null;
		try {
			result = client.execute(rpc, params);
		} catch (XmlRpcException e) {
			if (e.linkedException instanceof RedmineRemoteException) {
				throw (RedmineRemoteException)e.linkedException;
			} else {
				throw new RedmineRemoteException(e);
			}
		}
		return result;
	}

	private RedmineTicket parseResponse2Ticket(Object response)
			throws RedmineException {
		RedmineTicket ticket = null;
		if (response instanceof HashMap) {
			HashMap<String, Object> map = (HashMap) response;
			if (map.get("id") instanceof Integer) {
				Object value;
				ticket = new RedmineTicket(((Integer) map.get("id")).intValue());
				ticket.putBuiltinValue(Key.SUBJECT, map.get("subject")
						.toString());
				ticket.putBuiltinValue(Key.DESCRIPTION, map.get("description")
						.toString());
				ticket
						.putBuiltinValue(Key.AUTHOR, map.get("author")
								.toString());

				ticket.setCreated((Date) map.get("created_on"));
				ticket.setLastChanged((Date) map.get("updated_on"));
				
				//Handle Project for Ticket
				Integer intValue = (Integer)map.get("project_id");
				if (intValue==null) {
					throw new RedmineRemoteException("Missing project ID for ticket");
				}
				RedmineProjectData projectData = data.getProjectFromId(intValue);
				if (projectData==null) {
					throw new RedmineRemoteException("Can't find project for ID: " + intValue.toString());
				}
				ticket.putBuiltinValue(Key.PROJECT, intValue);

				//Handle RedmineTicketAttributes / ProjectAttributes
				String xmlRpcKey;
				for (Key key : this.attributeKeys) {
					xmlRpcKey = redmineKey2XmlRpcKey(key);
					intValue = (Integer)map.get(xmlRpcKey);
					if (intValue!=null) {
						ticket.putBuiltinValue(key, intValue);
					}
				}
				
			}
			return ticket;
		} else {
			throw new RedmineRemoteException(
					"Invalid Response: HashMap expected");
		}
	}

	private RedmineProject parseResponse2Project(Object response)
			throws RedmineException {
		RedmineProject project = null;
		if (response instanceof HashMap) {
			HashMap<String, Object> map = (HashMap) response;
			project = new RedmineProject(map.get("name").toString(),
					((Integer) map.get("id")).intValue());
			project.setIssueEditAllowed(Boolean.parseBoolean(map.get("issue_edit_allowed").toString()));
		}
		return project;
	}

	private RedmineMember parseResponse2Member(Object response)
			throws RedmineException {
		RedmineMember member = null;
		if (response instanceof HashMap) {
			HashMap<String, Object> map = (HashMap) response;
			member = new RedmineMember(
					map.get("name").toString(),
					((Integer)map.get("id")).intValue(),
					((Boolean)map.get("assignable")).booleanValue());
		}
		return member;
	}

	private RedmineVersion parseResponse2Version(Object response)
			throws RedmineException {
		RedmineVersion version = null;
		if (response instanceof HashMap) {
			HashMap<String, Object> map = (HashMap) response;
			version = new RedmineVersion(map.get("name").toString(),
					((Integer) map.get("id")).intValue());
		}
		return version;
	}

	private RedmineTracker parseResponse2Tracker(Object response)
			throws RedmineException {
		RedmineTracker tracker = null;
		if (response instanceof HashMap) {
			HashMap<String, Object> map = (HashMap) response;
			tracker = new RedmineTracker(map.get("name").toString(),
					((Integer) map.get("id")).intValue());
		}
		return tracker;
	}

	private List<RedmineTicketStatus> parseResponse2Statuses(Object response)
			throws RedmineException {
		
		List<RedmineTicketStatus> statuses = null;
		if (response instanceof Object[]) {
			Object[] maps = (Object[]) response;
			statuses = new ArrayList<RedmineTicketStatus>(maps.length);
			for (Object object : maps) {
				RedmineTicketStatus status = parseResponse2Status(object);
				if (status!=null) {
					statuses.add(status);
				}
			}
		}
		return statuses;
	}

	private RedmineTicketStatus parseResponse2Status(Object response)
	throws RedmineException {
		RedmineTicketStatus status = null;
		if (response instanceof HashMap) {
			HashMap<String, Object> map = (HashMap) response;
			status = new RedmineTicketStatus(map.get("name").toString(),
					((Integer) map.get("id")).intValue());
			status.setClosed(Boolean.parseBoolean(map.get("is_closed").toString()));
			status.setDefaultStatus(Boolean.parseBoolean(map.get("is_default").toString()));
		}
		return status;
	}
	
	private RedminePriority parseResponse2Priority(Object response)
			throws RedmineException {
		RedminePriority priority = null;
		if (response instanceof HashMap) {
			HashMap<String, Object> map = (HashMap) response;
			priority = new RedminePriority(
					map.get("name").toString(),
					((Integer) map.get("id")).intValue(), 
					((Integer) map.get("position")).intValue());
			priority.setDefaultPriority(Boolean.parseBoolean(map.get("is_default").toString()));

		}
		return priority;
	}
	
	private RedmineIssueCategory parseResponse2IssueCategory(Object response)
	throws RedmineException {
		RedmineIssueCategory category = null;
		if (response instanceof HashMap) {
			HashMap<String, Object> map = (HashMap) response;
			category = new RedmineIssueCategory(map.get("name").toString(),
					((Integer) map.get("id")).intValue());
		}
		return category;
	}
	
	private List<RedmineTicketJournal> parseResponse2Journals(Object response) throws RedmineException {
		List<RedmineTicketJournal> journals = null;
		if (response instanceof Object[]) {
			Object[] maps = (Object[]) response;
			journals = new ArrayList<RedmineTicketJournal>(maps.length);
			
			RedmineTicketJournal journal;
			for (Object object : maps) {
				if (object instanceof HashMap) {
					HashMap<String, Object> map = (HashMap) object;
					journal = new RedmineTicketJournal();
					journal.setId(Integer.parseInt(map.get("id").toString()));
					journal.setNotes(map.get("notes").toString());
					journal.setCreated((Date)map.get("created_on"));
					journal.setEditable(Boolean.parseBoolean(map.get("editable_by_user").toString()));
					journal.setAuthorId(Integer.parseInt(map.get("author_id").toString()));
					journal.setAuthorName(map.get("author_name").toString());
					journals.add(journal);
				}
			}
		}
		return journals;
	}
	
	private List<Integer> parseResponse2IntegerList(Object response) throws RedmineException {
		List<Integer> result = null;
		if (response instanceof Object[]) {
			Object[] values = (Object[]) response;
			result = new ArrayList<Integer>(values.length);
			for (Object object : values) {
				try {
					result.add(Integer.valueOf(object.toString()));
				} catch (NumberFormatException e) {
					throw new RedmineException(e);
				}
			}
		}
		return result;
	}

	private Map<String, Object> parseTicket2Request(RedmineTicket ticket) {
		Map<String, Object> map = new HashMap<String, Object>(0);
		
		Map<String, String> values = ticket.getValues();
		map.put("id", ticket.getId());
		map.put("subject", values.get(Key.SUBJECT.getKey()));
		map.put("description", values.get(Key.DESCRIPTION.getKey()));
		
		//Handle RedmineTicketAttributes / ProjectAttributes
		String xmlRpcKey;
		for (Key key : this.attributeKeys) {
			if (key.isReadonly()) {
				continue;
			}
			xmlRpcKey = redmineKey2XmlRpcKey(key);
			String value = values.get(key.getKey());
			if (value!=null) {
				map.put(xmlRpcKey, values.get(key.getKey()));
			}
		}
		
		return map;
	}

	
	private String redmineKey2XmlRpcKey(Key redmineKey) {
		return redmineKey.name().toLowerCase() + "_id";
	}

}
