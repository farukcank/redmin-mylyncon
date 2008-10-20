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

package org.svenk.redmine.core.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClientException;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.XmlRpcHttpClientConfig;
import org.apache.xmlrpc.client.XmlRpcTransport;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.UnsupportedRequestException;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.svenk.redmine.core.exception.RedmineAuthenticationException;
import org.svenk.redmine.core.util.internal.XmlRpcCommonsTransport;

public class RedmineTransportFactory extends XmlRpcCommonsTransportFactory {

	private AbstractWebLocation location;
	
	private final String endpoint;

	public RedmineTransportFactory(XmlRpcClient client,
			AbstractWebLocation location, String endpoint) {
		super(client);
		this.location = location;
		this.endpoint = endpoint;
	}

	@Override
	public XmlRpcTransport getTransport() {
		return new RedmineTransport(getClient(), location);
	}

	private class RedmineTransport extends XmlRpcCommonsTransport {
		
		protected final AbstractWebLocation location;
		
		protected final IProgressMonitor monitor = null;

		public RedmineTransport(XmlRpcClient pClient,
				AbstractWebLocation location) {
			super(pClient);
			this.location = location;
			
			WebUtil.configureHttpClient(client, getUserAgent());
		}

		@Override
		protected void setCredentials(XmlRpcHttpClientConfig config)
				throws XmlRpcClientException {
			
			AuthenticationCredentials repositoryCreds = location.getCredentials(AuthenticationType.REPOSITORY);
			AuthScope authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT);

			Credentials creds = new UsernamePasswordCredentials(repositoryCreds.getUserName(), repositoryCreds.getPassword());
			client.getState().setCredentials(authScope, creds);

			AuthenticationCredentials proxyCreds = location.getCredentials(AuthenticationType.PROXY);
			if (proxyCreds!=null) {
				creds = new UsernamePasswordCredentials(proxyCreds.getUserName(), proxyCreds.getPassword());
				client.getState().setProxyCredentials(authScope, creds);
			}
			
			
			client.getParams().setAuthenticationPreemptive(true);
		}
		
		@Override
		public Object sendRequest(XmlRpcRequest request) throws XmlRpcException {
			HostConfiguration hostConfig = WebUtil.createHostConfiguration(client, location, monitor);
			client.setHostConfiguration(hostConfig);

			try {
				XmlRpcClientConfigImpl config = (XmlRpcClientConfigImpl)request.getConfig();
				config.setServerURL(new URL(location.getUrl().toString() + endpoint));
				return super.sendRequest(request);
			} catch (MalformedURLException e1) {
				throw new XmlRpcException(e1.getMessage());
			} catch (XmlRpcException e) {
				boolean authenticated = false;
				AuthenticationType authenticationType = null;
				
				if (method.isRequestSent()) {
					switch (method.getStatusCode()) {
					case HttpStatus.SC_UNAUTHORIZED :
						authenticationType = AuthenticationType.REPOSITORY;
						break;
					case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED :
						authenticationType = AuthenticationType.PROXY;
						break;
					default :
						authenticated = true;
					}
					
					if (!authenticated) {
						try {
							location.requestCredentials(authenticationType, method.getStatusText(), monitor);
							if (!monitor.isCanceled()) {
								setCredentials(null);
								return sendRequest(request);
							}
						} catch (UnsupportedRequestException e1) {;}
						
						throw new XmlRpcException(method.getStatusText(), new RedmineAuthenticationException(method.getStatusCode()));
					}
					
				}
				throw e;
			}
		}
	}
}