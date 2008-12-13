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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.UnsupportedRequestException;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.model.RedmineTicket;
import org.svenk.redmine.core.model.RedmineTicket.Key;
import org.svenk.redmine.core.util.internal.RedminePartSource;



abstract public class AbstractRedmineClient implements IRedmineClient {

	protected final static double REDMINE_VERSION_7 = 0.7D;

	protected final static double REDMINE_VERSION_8 = 0.8D;

	private final HttpClient httpClient;
	
	protected AbstractWebLocation location;
	
	protected RedmineClientData data;
	
	protected String characterEncoding;
	
	protected boolean authenticated;
	
	protected double redmineVersion = 0D;

	protected RedmineTicket.Key attributeKeys[] = new RedmineTicket.Key[]{Key.ASSIGNED_TO, Key.PRIORITY, Key.VERSION, Key.CATEGORY, Key.STATUS, Key.TRACKER};

	public AbstractRedmineClient(AbstractWebLocation location, RedmineClientData clientData, TaskRepository repository) {
		this.location = location;
		this.data = clientData;
		this.httpClient = new HttpClient();
		this.characterEncoding = repository.getCharacterEncoding();
		
		this.httpClient.getParams().setContentCharset(characterEncoding);
		
		refreshRepositorySettings(repository);
	}

	public void refreshRepositorySettings(TaskRepository repository) {
		if (!repository.getVersion().equals(TaskRepository.NO_VERSION_SPECIFIED)) {
			redmineVersion = getRedmineVersion(repository.getVersion());
		}
	}
	
	public String checkClientConnection() throws RedmineException {
		String version = checkClientVersion();
		if (!(version.startsWith(""+REDMINE_VERSION_7) || version.startsWith(""+REDMINE_VERSION_8))) {
			throw new RedmineException("This connector requires Redmine version 0.7.X or 0.8.X");
		}
		return version;
	}
	
	abstract protected String checkClientVersion() throws RedmineException;
	
	private double getRedmineVersion(String version) {
		int pos = version.indexOf('.', version.indexOf('.')+1);
		return Double.parseDouble(version.substring(0, pos));
	}
	
	public InputStream getAttachmentContent(int attachmentId, IProgressMonitor monitor) throws RedmineException {
		GetMethod method = new GetMethod(IRedmineClient.ATTACHMENT_URL + attachmentId);
		try {
			int statusCode = executeMethod(method, monitor);
			return statusCode==HttpStatus.SC_OK ? method.getResponseBodyAsStream() : null;
		} catch (IOException e) {
			throw new RedmineException(e.getMessage(), e.getCause());
		}
	}
	
	public void uploadAttachment(int ticketId, String fileName, String comment, String description, AbstractTaskAttachmentSource source, IProgressMonitor monitor) throws RedmineException {
		PostMethod method = new PostMethod(TICKET_EDIT_URL + ticketId);
		
		Part[] parts = new Part[]{
				new FilePart("attachments[1][file]", new RedminePartSource(source, fileName)),
				new StringPart("attachments[1][description]", description, characterEncoding),
				new StringPart("notes", comment, characterEncoding)
		};
		
		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
		
		executeMethod(method, monitor);
	}
	
	public int createTicket(RedmineTicket ticket, IProgressMonitor monitor) throws RedmineException {
		PostMethod method = new PostMethod("/projects/" + ticket.getValue(Key.PROJECT) + TICKET_NEW_URL);
		
		List<NameValuePair> values = this.ticket2HttpData(ticket);
		method.setRequestBody(values.toArray(new NameValuePair[values.size()]));

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
	
	public void updateTicket(RedmineTicket ticket, String comment, IProgressMonitor monitor) throws RedmineException {
		PostMethod method = new PostMethod(TICKET_EDIT_URL + ticket.getId());

		List<NameValuePair> values = this.ticket2HttpData(ticket, comment);
		method.setRequestBody(values.toArray(new NameValuePair[values.size()]));

		executeMethod(method, monitor);
	}
	
	/**
	 * Create the HostConfiguaration and execute the given method. 
	 * @param method
	 * @param monitor
	 * @return
	 * @throws RedmineException
	 */
	protected int executeMethod(HttpMethod method, IProgressMonitor monitor) throws RedmineException {
		method.setFollowRedirects(false);
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
		if (!this.authenticated) {
			performLogin(hostConfiguration, monitor);
			this.authenticated = authenticated = true;
		}

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

	protected List<NameValuePair> ticket2HttpData(RedmineTicket ticket) {
		
		Map<String, String> values = ticket.getValues();
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(this.attributeKeys.length + 2);

		nameValuePair.add(new NameValuePair("issue[subject]", values.get(Key.SUBJECT.getKey())));
		nameValuePair.add(new NameValuePair("issue[description]", values.get(Key.DESCRIPTION.getKey())));
		nameValuePair.add(new NameValuePair("issue[done_ratio]", values.get(Key.DONE_RATIO.getKey())));
		
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
		
		//CustomTicketFields
		if (redmineVersion==REDMINE_VERSION_7) {
			for (Map.Entry<Integer, String> customValue : ticket.getCustomValues().entrySet()) {
				String name = "custom_fields[" + customValue.getKey() + "]";
				nameValuePair.add(new NameValuePair(name, customValue.getValue()));
			}
		}
		
		return nameValuePair;
	}
	
	protected List<NameValuePair> ticket2HttpData(RedmineTicket ticket, String comment) {
		List<NameValuePair> nameValuePair = ticket2HttpData(ticket);
		nameValuePair.add(new NameValuePair("notes", comment));
		return nameValuePair;
	}
	
	private String redmineKey2ValueName(Key redmineKey) {
		String name = redmineKey.name().toLowerCase();
		if (name.equals("version")) {
			name = "fixed_version";
		}
		return "issue[" + name + "_id]";
	}

}
