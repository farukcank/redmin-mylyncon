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

import java.io.InputStream;
import java.util.Date;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.svenk.redmine.core.IRedmineConstants;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.client.container.Version.Release;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.exception.RedmineRemoteException;
import org.svenk.redmine.core.exception.RedmineStatusException;

public aspect RedmineAuthenticationAspect perthis(this(AbstractRedmineClient)) {
	
	private final static int LOGIN_INTERVAL = 50*60*1000; //50 Minutes in millisec
	
	private AbstractRedmineClient client;
	
	private Date lastLogin;
	
	private IRedmineResponseParser<String> tokenParser = new IRedmineResponseParser<String>() {
		private RedmineResponseReader responseReader = new RedmineResponseReader(); 

		public String parseResponse(InputStream input, int sc) throws RedmineException {
			return responseReader.readAuthenticityToken(input);
		}
	};

	
	/**
	 * Do catch every Constructor-Call on AbstractRedmineClient ...
	 * 
	 * @param client
	 */
	pointcut catchNewClient(AbstractRedmineClient client) : 
		execution(AbstractRedmineClient.new(..))
		&& this(client);
	
	/**
	 * ... and save the reference to the new instance.
	 * 
	 * @param client
	 */
	after(AbstractRedmineClient client) returning() : catchNewClient(client) {
		this.client = client;
	}

	/**
	 * Do catch all RPC-Calls ...
	 * 
	 * @param client
	 * @param monitor
	 */
	pointcut remoteCall(IProgressMonitor monitor) : 
		execution(public * AbstractRedmineClient+.*(.., IProgressMonitor) throws RedmineException)
		&& args(.., monitor)
		;

	/**
	 * ... and perform a login (if required), before the RPC-Call will be executed.
	 */
	before(IProgressMonitor monitor) throws RedmineException : remoteCall(monitor) {
		if (isLoginRequired()) {
			monitor = Policy.monitorFor(monitor);
			HostConfiguration hostConfig = WebUtil.createHostConfiguration(client.httpClient, client.location, monitor);
			performLogin(hostConfig, monitor);
		}
	}
	
	/**
	 * Do catch Constructor-Call on PostMethod ...
	 */
	pointcut createPostMethod() :
		call(PostMethod.new(..))
		&& withincode(* AbstractRedmineClient.*(..));
	
	/**
	 * ... and create and add an authenticity-token
	 * 
	 * @param method
	 */
	after() returning(PostMethod method) : createPostMethod() {
		addCsrfToken(method);
	}

	/**
	 * Do catch of missing authenticity token ...
	 */
	pointcut checkCsrfFailure() :
		execution(int AbstractRedmineClient.performExecuteMethod(..));

	/**
	 * ... and logging.
	 * 
	 * @param client
	 * @param sc
	 * @throws RedmineException
	 */
	after() returning(int sc) throws RedmineException : checkCsrfFailure() {
		if (sc==HttpStatus.SC_UNPROCESSABLE_ENTITY) {
			setRepositoryProperty(true);
			throw new RedmineRemoteException(Messages.AbstractRedmineClient_INVALID_AUTHENTICITY_TOKEN);
		}
	}

	private void performLogin(HostConfiguration hostConfig, IProgressMonitor monitor) throws RedmineException {
		NameValuePair[] credentials = new NameValuePair[]{
			new NameValuePair(IRedmineConstants.CLIENT_FIELD_CREDENTIALS_USERNAME, client.location.getCredentials(AuthenticationType.REPOSITORY).getUserName()),	
			new NameValuePair(IRedmineConstants.CLIENT_FIELD_CREDENTIALS_PASSWORD, client.location.getCredentials(AuthenticationType.REPOSITORY).getPassword())	
		};

		PostMethod method = new PostMethod(IRedmineConstants.REDMINE_URL_LOGIN);
		method.addParameters(credentials);
		addCsrfToken(method);
		
		try {
			int sc = client.performExecuteMethod(method, hostConfig, monitor);
			AuthenticationType authenticationType = null;
			
			switch (sc) {
			case HttpStatus.SC_OK:
				//Invalid Redmine Password
				authenticationType = AuthenticationType.REPOSITORY;
				break;
			case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
				//PROXY-Password required
				authenticationType = AuthenticationType.PROXY;
				break;
			default:
				lastLogin = new Date();
				break;
			}
			
			if(authenticationType!=null) {
				hostConfig = client.refreshCredentials(authenticationType, method, monitor);
				if (!monitor.isCanceled()) {
					performLogin(hostConfig, monitor);
					return;
				}
			}
			
		} finally {
			method.releaseConnection();
		}

	}
	
	private boolean isLoginRequired() {
		if (lastLogin!=null) {
			long diff = new Date().getTime() - lastLogin.getTime();
			return diff<0 || diff>LOGIN_INTERVAL;
		}
		return true;
	}

	private void addCsrfToken(PostMethod method) {
		if (isCsrfTokenRequired(method)) {
			try {
				String token = getNewCsrfToken(method);
				if (token==null) {
					setRepositoryProperty(false);
				} else {
					method.addParameter(IRedmineConstants.CLIENT_FIELD_CSRF_TOKEN, token);
				}
			} catch (RedmineStatusException e) {
			} catch (RedmineException e) {
				IStatus status = RedmineCorePlugin.toStatus(e, null);
				StatusHandler.log(status);
			}
		}
	}
	
	private boolean isCsrfTokenRequired(HttpMethod method) {
		if (client.vRedmine==null && getRepositoryProperty() || client.vRedmine!=null && client.vRedmine.compareTo(Release.ZEROEIGHTSEVEN)>=0) {
			//TODO lookup for string part mylyn  isn't a perfect solution
			return (!method.getPath().contains("mylyn")); //$NON-NLS-1$
		}
		return false;
	}

	private String getNewCsrfToken(PostMethod origMethod) throws RedmineException {
		HttpMethodBase method = new GetMethod(origMethod.getPath());
		return client.executeMethod(method, tokenParser, null);
	}
	
	private void setRepositoryProperty(boolean value) {
		client.repository.setProperty(IRedmineConstants.CLIENT_FIELD_CSRF_TOKEN, Boolean.toString(value));
	}

	private boolean getRepositoryProperty() {
		String val = client.repository.getProperty(IRedmineConstants.CLIENT_FIELD_CSRF_TOKEN);
		if(val==null) {
			setRepositoryProperty(true);
			return true;
		}
		return Boolean.parseBoolean(val);
	}

}
