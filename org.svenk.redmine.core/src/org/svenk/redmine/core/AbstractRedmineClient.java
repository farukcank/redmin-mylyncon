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
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.UnsupportedRequestException;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.model.RedmineTicket;
import org.svenk.redmine.core.model.RedmineTicket.Key;



abstract public class AbstractRedmineClient implements IRedmineClient {

	private final HttpClient httpClient;
	
	protected AbstractWebLocation location;
	
	protected RedmineClientData data;

	protected RedmineTicket.Key attributeKeys[] = new RedmineTicket.Key[]{Key.ASSIGNED_TO, Key.PRIORITY, Key.VERSION, Key.CATEGORY, Key.STATUS, Key.TRACKER};

	public AbstractRedmineClient(AbstractWebLocation location, RedmineClientData clientData) {
		this.location = location;
		this.data = clientData;
		this.httpClient = new HttpClient();
	}

	public int createTicket(RedmineTicket ticket, IProgressMonitor monitor) throws RedmineException {
		PostMethod method = new PostMethod("/projects/" + ticket.getValue(Key.PROJECT) + TICKET_NEW_URL);
		method.setRequestBody(this.ticket2HttpData(ticket));

		executeMethod(method, monitor);
		
		Header respHeader = method.getResponseHeader("location");
		if (respHeader != null) {
			String location = respHeader.getValue();
			int pos=location.indexOf(TICKET_URL);
			if (pos>-1) {
				location = location.substring(TICKET_URL.length()+pos);
				try {
					return Integer.parseInt(location);
				} catch (NumberFormatException e) {
					throw new RedmineException("Invalid Response: TicketId must be an Integer");
				}
			}
		} else {
			throw new RedmineException("Invalid Response: unhandled input error");
		}
		
		return -1;
		
	}
	
	/**
	 * Create the HostConfiguaration and execute the given method. 
	 * @param method
	 * @param monitor
	 * @return
	 * @throws RedmineException
	 */
	protected int executeMethod(HttpMethod method, IProgressMonitor monitor) throws RedmineException {
		HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
		return executeMethod(method, hostConfiguration, monitor, false);
	}
	
	/**
	 * Execute the given method - handle authentication concerns.
	 * 
	 * @param method
	 * @param hostConfiguration
	 * @param monitor
	 * @param authenticated
	 * @return
	 * @throws RedmineException
	 */
	protected int executeMethod(HttpMethod method, HostConfiguration hostConfiguration, IProgressMonitor monitor, boolean authenticated) throws RedmineException {
		int statusCode = performExecuteMethod(method, hostConfiguration, monitor);
		
		if (statusCode==HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
			hostConfiguration = refreshCredentials(AuthenticationType.PROXY, method, monitor);
			return executeMethod(method, hostConfiguration, monitor, authenticated);
		}
		
		Header respHeader = method.getResponseHeader("location");
		if (respHeader != null && respHeader.getValue().endsWith(LOGIN_URL)) {
			if (authenticated) {
				hostConfiguration = refreshCredentials(AuthenticationType.REPOSITORY, method, monitor);
			}
			performLogin(hostConfiguration, monitor);
			return executeMethod(method, hostConfiguration, monitor, true);
		}		

		return statusCode;
	}

	/**
	 * Send a POST request with name and password
	 * 
	 * @param hostConfiguration
	 * @throws RedmineException
	 */
	protected void performLogin(HostConfiguration hostConfiguration, IProgressMonitor monitor) throws RedmineException {
		NameValuePair[] credentials = new NameValuePair[]{
			new NameValuePair("username", location.getCredentials(AuthenticationType.REPOSITORY).getUserName()),	
			new NameValuePair("password", location.getCredentials(AuthenticationType.REPOSITORY).getPassword())	
		};
		PostMethod method = new PostMethod(LOGIN_URL);
		method.setRequestBody(credentials);
		
		performExecuteMethod(method, hostConfiguration, monitor);
	}

	protected int performExecuteMethod(HttpMethod method, HostConfiguration hostConfiguration, IProgressMonitor monitor) throws RedmineException {
		try {
			return WebUtil.execute(httpClient, hostConfiguration, method, monitor);
		} catch (Exception e) {
			if (e instanceof OperationCanceledException) {
				monitor.setCanceled(true);
			}
			throw new RedmineException(e.getMessage(), e.getCause());
		}
	}
	
	/**
	 * Ask user for name and password.
	 * 
	 * @param authenticationType
	 * @param method
	 * @param monitor
	 * @return
	 * @throws RedmineException
	 */
	protected HostConfiguration refreshCredentials(AuthenticationType authenticationType, HttpMethod method, IProgressMonitor monitor) throws RedmineException {
		try {
			location.requestCredentials(authenticationType, method.getStatusText(), monitor);
			return WebUtil.createHostConfiguration(httpClient, location, monitor);
		} catch (UnsupportedRequestException e) {
			throw new RedmineException(e.getMessage(), e.getCause());
		} catch (OperationCanceledException e) {
			monitor.setCanceled(true);
			throw new RedmineException(e.getMessage(), e.getCause());
		}
	}

	protected NameValuePair[] ticket2HttpData(RedmineTicket ticket) {
		
		Map<String, String> values = ticket.getValues();
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(this.attributeKeys.length + 3);
		
		nameValuePair.add(new NameValuePair("issue[subject]", values.get(Key.SUBJECT.getKey())));
		nameValuePair.add(new NameValuePair("issue[description]", values.get(Key.DESCRIPTION.getKey())));
		
		//Handle RedmineTicketAttributes / ProjectAttributes
		String xmlRpcKey;
		boolean existingTicket = ticket.getId()>0;
		for (Key key : this.attributeKeys) {
			if (key.isReadonly() && existingTicket) {
				continue;
			}
			xmlRpcKey = redmineKey2ValueName(key);
			String value = values.get(key.getKey());
			if (value!=null) {
				nameValuePair.add(new NameValuePair(xmlRpcKey, values.get(key.getKey())));
			}
		}
		
		return nameValuePair.toArray(new NameValuePair[nameValuePair.size()]);
	}
	
	private String redmineKey2ValueName(Key redmineKey) {
		String name = redmineKey.name().toLowerCase();
		if (name.equals("version")) {
			name = "fixed_version";
		}
		return "issue[" + name + "_id]";
	}

}