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

import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ATTACHMENT_DESCRIPTION;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ATTACHMENT_FILE;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ATTACHMENT_NOTES;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_CSRF_TOKEN;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_CUSTOM_R07E;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_CUSTOM_R08L;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_DESCRIPTION;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_DONERATIO;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_ENDDATE;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_ESTIMATED;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_FIXEDVERSION;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_NOTES;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_REFERENCED_ID;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_STARTDATE;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_SUBJECT;
import static org.svenk.redmine.core.IRedmineConstants.REDMINE_URL_ATTACHMENT_DOWNLOAD;
import static org.svenk.redmine.core.IRedmineConstants.REDMINE_URL_LOGIN;
import static org.svenk.redmine.core.IRedmineConstants.REDMINE_URL_LOGIN_CALLBACK;
import static org.svenk.redmine.core.IRedmineConstants.REDMINE_URL_TICKET_EDIT;
import static org.svenk.redmine.core.IRedmineConstants.REDMINE_URL_TICKET_NEW;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.commons.net.UnsupportedRequestException;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.svenk.redmine.core.IRedmineClient;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.client.container.Version;
import org.svenk.redmine.core.exception.RedmineAuthenticationException;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.exception.RedmineRemoteException;
import org.svenk.redmine.core.exception.RedmineStatusException;
import org.svenk.redmine.core.model.RedmineTicket;
import org.svenk.redmine.core.model.RedmineTicket.Key;
import org.svenk.redmine.core.util.internal.RedminePartSource;


abstract public class AbstractRedmineClient implements IRedmineClient {

	protected final static double REDMINE_VERSION_7 = 0.7D;

	protected final static double REDMINE_VERSION_8 = 0.8D;

	protected final static double REDMINE_VERSION_9 = 0.9D;
	
	protected final static String HEADER_STATUS = "status"; //$NON-NLS-1$

	protected final static String HEADER_REDIRECT = "location"; //$NON-NLS-1$

	protected final static String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate"; //$NON-NLS-1$

	protected final static String HEADER_WWW_AUTHENTICATE_REALM = "realm"; //$NON-NLS-1$

	protected final HttpClient httpClient;
	
	protected AbstractWebLocation location;
	
	protected RedmineClientData data;
	
	protected RedmineResponseReader responseReader;
	
	protected String characterEncoding;
	
	//TODO replace completely against vRedmine
	protected double redmineVersion = 0D;
	
	protected Version.Redmine vRedmine;
	
	protected RedmineTicket.Key attributeKeys[] = new RedmineTicket.Key[]{Key.ASSIGNED_TO, Key.PRIORITY, Key.VERSION, Key.CATEGORY, Key.STATUS, Key.TRACKER};
	
	protected final TaskRepository repository;
	
	private IRedmineResponseParser<String> submitErrorParser;

	private IRedmineResponseParser<InputStream> attachmentParser;
	
	public AbstractRedmineClient(AbstractWebLocation location, RedmineClientData clientData, TaskRepository repository) {
		this.location = location;
		this.data = clientData;
		this.characterEncoding = repository.getCharacterEncoding();
		
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		this.httpClient = new HttpClient(connectionManager);
		
		this.httpClient.getParams().setContentCharset(characterEncoding);
		this.httpClient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
		
		this.repository = repository;
		refreshRepositorySettings(repository);
		
		createResponseParsers();
	}

	public void refreshRepositorySettings(TaskRepository repository) {
		if (!this.characterEncoding.equals(repository.getCharacterEncoding())) {
			this.characterEncoding = repository.getCharacterEncoding();
			this.httpClient.getParams().setContentCharset(characterEncoding);
		}
		if (!repository.getVersion().equals(TaskRepository.NO_VERSION_SPECIFIED)) {
			redmineVersion = getRedmineVersion(repository.getVersion());
		}
		vRedmine = Version.Redmine.fromString(repository.getVersion());
	}
	
	public String checkClientConnection(IProgressMonitor monitor) throws RedmineException {
		String version = checkClientVersion(monitor);
		if (!(version.startsWith(Double.toString(REDMINE_VERSION_7)) || version.startsWith(Double.toString(REDMINE_VERSION_8)) || version.startsWith(Double.toString(REDMINE_VERSION_9)))) {
			throw new RedmineException(Messages.AbstractRedmineClient_REQUIRED_REDMINE_VERSION);
		}
		return version;
	}
	
	abstract protected String checkClientVersion(IProgressMonitor monitor) throws RedmineException;
	
	private double getRedmineVersion(String version) {
		int pos = version.indexOf('.', version.indexOf('.')+1);
		return Double.parseDouble(version.substring(0, pos));
	}
	
	public boolean supportStartDueDate() {
		return false;
	}

	protected boolean supportAdditionalHttpAuth() {
		return false;
	}
	
	public InputStream getAttachmentContent(int attachmentId, IProgressMonitor monitor) throws RedmineException {
		GetMethod method = new GetMethod(REDMINE_URL_ATTACHMENT_DOWNLOAD + attachmentId);
		return executeMethod(method, attachmentParser, monitor);
	}
	
	public void uploadAttachment(int ticketId, String fileName, String comment, String description, AbstractTaskAttachmentSource source, IProgressMonitor monitor) throws RedmineException {
		PostMethod method = new PostMethod(REDMINE_URL_TICKET_EDIT + ticketId);

		//assigned by RedmineAuthenticityTokenAspect
		NameValuePair tokenValue = method.getParameter(CLIENT_FIELD_CSRF_TOKEN);
		
		Part[] parts = new Part[]{
				new FilePart(CLIENT_FIELD_ATTACHMENT_FILE, new RedminePartSource(source, fileName), source.getContentType(), this.httpClient.getParams().getContentCharset()),
				new StringPart(CLIENT_FIELD_ATTACHMENT_DESCRIPTION, description, characterEncoding),
				new StringPart(CLIENT_FIELD_ATTACHMENT_NOTES, comment, characterEncoding),
				new StringPart(CLIENT_FIELD_CSRF_TOKEN, tokenValue==null ? "" : tokenValue.getValue(), characterEncoding), //$NON-NLS-1$
		};
		
		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
		
		String errorMsg = executeMethod(method, submitErrorParser, monitor);
		if (errorMsg!=null) {
			throw new RedmineStatusException(IStatus.INFO, errorMsg);
		}
	}
	
	public int createTicket(RedmineTicket ticket, IProgressMonitor monitor) throws RedmineException {
		PostMethod method = new PostMethod(String.format(REDMINE_URL_TICKET_NEW, ticket.getValue(Key.PROJECT)));
		
		List<NameValuePair> values = this.ticket2HttpData(ticket);
		method.addParameters(values.toArray(new NameValuePair[values.size()]));

		String errorMsg = executeMethod(method, submitErrorParser, monitor, HttpStatus.SC_OK, HttpStatus.SC_MOVED_TEMPORARILY);
		if(errorMsg==null) {
			//TODO PRÜFEN !!!
			Header respHeader = method.getResponseHeader(HEADER_REDIRECT);
			if (respHeader != null) {
				String location = respHeader.getValue();
				
				Matcher m = Pattern.compile("(\\d+)$").matcher(location); //$NON-NLS-1$
				if (m.find()) {
					try {
						return Integer.parseInt(m.group(1));
					} catch (NumberFormatException e) {
						throw new RedmineException(Messages.AbstractRedmineClient_INVALID_TASK_ID);
					}
				} else {
					throw new RedmineException(Messages.AbstractRedmineClient_MISSING_TASK_ID_IN_RESPONSE);
				}
			}
		} else {
			throw new RedmineStatusException(IStatus.INFO, errorMsg);
		}
		
		throw new RedmineException(Messages.AbstractRedmineClient_UNHANDLED_SUBMIT_ERROR);
	}
	
	public void updateTicket(RedmineTicket ticket, String comment, IProgressMonitor monitor) throws RedmineException {
		PostMethod method = new PostMethod(REDMINE_URL_TICKET_EDIT + ticket.getId());

		List<NameValuePair> values = this.ticket2HttpData(ticket, comment);
		method.addParameters(values.toArray(new NameValuePair[values.size()]));

		String errorMsg = executeMethod(method, submitErrorParser, monitor, HttpStatus.SC_OK, HttpStatus.SC_MOVED_TEMPORARILY);
		if (errorMsg!=null) {
			throw new RedmineStatusException(IStatus.INFO, errorMsg);
		}
	}

	protected <T extends Object> T executeMethod(HttpMethodBase method, IRedmineResponseParser<T> parser, IProgressMonitor monitor) throws RedmineException {
		return executeMethod(method, parser, monitor, HttpStatus.SC_OK);
	}
	
	protected <T extends Object> T executeMethod(HttpMethodBase method, IRedmineResponseParser<T> parser, IProgressMonitor monitor, int... expectedSC) throws RedmineException {
		monitor = Policy.monitorFor(monitor);
		method.setFollowRedirects(false);
		HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);

		T response = null;
		try {
			int sc = executeMethod(method, hostConfiguration, monitor);
			
			if (parser!=null && expectedSC != null) {
				boolean found = false;
				for (int i : expectedSC) {
					if (i==sc) {
						InputStream input = WebUtil.getResponseBodyAsStream(method, monitor);
						try {
							found = true;
							response = parser.parseResponse(input, sc);
						} finally {
							input.close();
						}
						break;
					}
				}
				if(!found) {
					String msg = Messages.AbstractRedmineClient_UNEXPECTED_RESPONSE_CODE;
					msg = String.format(msg, sc, method.getPath(), method.getName());
					IStatus status = new Status(IStatus.ERROR, RedmineCorePlugin.PLUGIN_ID, msg);
					StatusHandler.fail(status);
					throw new RedmineStatusException(status);
				}
			}
		}catch (IOException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null);
			StatusHandler.log(status);
			throw new RedmineStatusException(status);
		} finally {
			method.releaseConnection();
		}
		
		return response;
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
	protected int executeMethod(HttpMethod method, HostConfiguration hostConfiguration, IProgressMonitor monitor) throws RedmineException {
		monitor = Policy.monitorFor(monitor);

		int statusCode = performExecuteMethod(method, hostConfiguration, monitor);

		if (statusCode==HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			Header statusHeader = method.getResponseHeader(HEADER_STATUS);
			String msg = Messages.AbstractRedmineClient_SERVER_ERROR;
			if (statusHeader != null) {
				msg += " : " + statusHeader.getValue().replace(""+HttpStatus.SC_INTERNAL_SERVER_ERROR, "").trim(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			
			throw new RedmineRemoteException(msg);
		}
		
		//TODO testen, sollte ohne gehen
//		if (statusCode==HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
//			hostConfiguration = refreshCredentials(AuthenticationType.PROXY, method, monitor);
//			return executeMethod(method, hostConfiguration, monitor, authenticated);
//		}
//		
//		if(statusCode==HttpStatus.SC_UNAUTHORIZED && supportAdditionalHttpAuth()) {
//			hostConfiguration = refreshCredentials(AuthenticationType.HTTP, method, monitor);
//			return executeMethod(method, hostConfiguration, monitor, authenticated);
//		}
//
//		if (statusCode>=400 && statusCode<=599) {
//			throw new RedmineRemoteException(method.getStatusLine().toString());
//		}
		
		Header respHeader = method.getResponseHeader(HEADER_REDIRECT);
		if (respHeader != null && (respHeader.getValue().endsWith(REDMINE_URL_LOGIN) || respHeader.getValue().indexOf(REDMINE_URL_LOGIN_CALLBACK)>=0)) {
			throw new RedmineException(Messages.AbstractRedmineClient_LOGIN_FORMALY_INEFFECTIVE);
		}		

		return statusCode;
	}
	
	synchronized protected int performExecuteMethod(HttpMethod method, HostConfiguration hostConfiguration, IProgressMonitor monitor) throws RedmineException {
		try {
			//complete URL
			String baseUrl = new URL(location.getUrl()).getPath();
			if (!method.getPath().startsWith(baseUrl)) {
				method.setPath(baseUrl + method.getPath());
			}
			
			return WebUtil.execute(httpClient, hostConfiguration, method, monitor);
		} catch (OperationCanceledException e) {
			monitor.setCanceled(true);
			throw new RedmineException(e.getMessage(), e);
		} catch (RuntimeException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null, Messages.AbstractRedmineClient_UNHANDLED_RUNTIME_EXCEPTION);
			StatusHandler.fail(status);
			throw new RedmineStatusException(status);
		} catch (IOException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null);
			StatusHandler.log(status);
			throw new RedmineStatusException(status);
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
		if (Policy.isBackgroundMonitor(monitor)) {
			throw new RedmineAuthenticationException(method.getStatusCode(),Messages.AbstractRedmineClient_MISSING_CREDENTIALS_MANUALLY_SYNC_REQUIRED);
		}
		
		try {
			String message = Messages.AbstractRedmineClient_AUTHENTICATION_REQUIRED;
			if(authenticationType.equals(AuthenticationType.HTTP)) {
				Header authHeader = method.getResponseHeader(HEADER_WWW_AUTHENTICATE);
				if(authHeader!=null) {
					for (HeaderElement headerElem : authHeader.getElements()) {
						if (headerElem.getName().contains(HEADER_WWW_AUTHENTICATE_REALM)) {
							message += ": " + headerElem.getValue(); //$NON-NLS-1$
							break;
						}
					}
				}
			}
			location.requestCredentials(authenticationType, message, monitor);
			
			return WebUtil.createHostConfiguration(httpClient, location, monitor);
		} catch (UnsupportedRequestException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null, Messages.AbstractRedmineClient_CREDENTIALS_REQUEST_FAILED);
			StatusHandler.log(status);
			throw new RedmineStatusException(status);
		} catch (OperationCanceledException e) {
			monitor.setCanceled(true);
			throw new RedmineException(Messages.AbstractRedmineClient_AUTHENTICATION_CANCELED);
		}
	}

	protected List<NameValuePair> ticket2HttpData(RedmineTicket ticket) {
		
		Map<String, String> values = ticket.getValues();
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(this.attributeKeys.length + 2);

		nameValuePair.add(new NameValuePair(CLIENT_FIELD_ISSUE_SUBJECT, values.get(Key.SUBJECT.getKey())));
		nameValuePair.add(new NameValuePair(CLIENT_FIELD_ISSUE_DESCRIPTION, values.get(Key.DESCRIPTION.getKey())));
		nameValuePair.add(new NameValuePair(CLIENT_FIELD_ISSUE_DONERATIO, values.get(Key.DONE_RATIO.getKey())));
		nameValuePair.add(new NameValuePair(CLIENT_FIELD_ISSUE_ESTIMATED, values.get(Key.ESTIMATED_HOURS.getKey())));
		nameValuePair.add(new NameValuePair(CLIENT_FIELD_ISSUE_STARTDATE, values.get(Key.START_DATE.getKey())));
		nameValuePair.add(new NameValuePair(CLIENT_FIELD_ISSUE_ENDDATE, values.get(Key.DUE_DATE.getKey())));
		
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
		for (Map.Entry<Integer, String> customValue : ticket.getCustomValues().entrySet()) {
				String name = redmineVersion<REDMINE_VERSION_8
					? CLIENT_FIELD_ISSUE_CUSTOM_R07E
					: CLIENT_FIELD_ISSUE_CUSTOM_R08L;
				name = String.format(name, customValue.getKey());
			nameValuePair.add(new NameValuePair(name, customValue.getValue()));
		}
		
		return nameValuePair;
	}
	
	protected List<NameValuePair> ticket2HttpData(RedmineTicket ticket, String comment) {
		List<NameValuePair> nameValuePair = ticket2HttpData(ticket);
		nameValuePair.add(new NameValuePair(CLIENT_FIELD_ISSUE_NOTES, comment));
		return nameValuePair;
	}
	
	private RedmineResponseReader getResponseReader() {
		if (responseReader==null) {
			responseReader = new RedmineResponseReader();
		}
		return responseReader;
	}
	
	private String redmineKey2ValueName(Key redmineKey) {
		String name = redmineKey.name().toLowerCase();
		if (name.equals(Key.VERSION.getKey())) {
			name = CLIENT_FIELD_ISSUE_FIXEDVERSION;
		}
		return String.format(CLIENT_FIELD_ISSUE_REFERENCED_ID, name);
	}

	private void createResponseParsers() {
		submitErrorParser = new IRedmineResponseParser<String>() {
			public String parseResponse(InputStream input, int sc) throws RedmineException {
				if (sc==HttpStatus.SC_OK) {
					Collection<String> messages = getResponseReader().readErrors(input);
					if (messages!=null) {
						StringBuilder sb = new StringBuilder();
						for (Iterator<String> iterator = messages.iterator(); iterator.hasNext();) {
							sb.append(iterator.next());
							sb.append(" "); //$NON-NLS-1$
						}
						return sb.toString().trim();
					}
				}
				return null;
			}
		};
		
		attachmentParser = new IRedmineResponseParser<InputStream>() {
 			public InputStream parseResponse(InputStream input, int sc) throws RedmineException {
 				InputStream response = null;
 				try {
 					ByteArrayOutputStream output = new ByteArrayOutputStream(input.available());
 					try {
 						byte[] buffer = new byte[4096];
 						int len = 0;
 						while ((len=input.read(buffer))>0) {
 							output.write(buffer, 0, len); 
 						}
 						response = new ByteArrayInputStream(output.toByteArray());
 					} finally {
 						output.close();
 					}
 				} catch (IOException e) {
 					throw new RedmineException(e.getMessage(), e);
 				}
 				
				return response;
			}
		};
	}

}
