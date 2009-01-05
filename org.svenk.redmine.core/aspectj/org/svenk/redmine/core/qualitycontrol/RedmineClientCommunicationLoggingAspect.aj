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
package org.svenk.redmine.core.qualitycontrol;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.util.internal.XmlRpcCommonsTransport;

public privileged aspect RedmineClientCommunicationLoggingAspect {

	private PrintWriter logWriter = null;
	
	private static boolean loggingEnabled() {
		String propVal = System.getProperty("org.svenk.redmine.core.qualitycontrol.RedmineClientCommunicationLoggingAspect.enable", "false");
		return propVal!=null && propVal.equalsIgnoreCase("true");
	}
	
	private void logCommunication(XmlRpcRequest request, HttpMethod method) {
		if (method.isRequestSent()) {
			IPath stateLocation = Platform.getStateLocation(RedmineCorePlugin.getDefault().getBundle());
			IPath cacheFile = stateLocation.append("communicationLog.txt");
			
			try {
				if (logWriter == null) {
					logWriter = new PrintWriter(new FileWriter(cacheFile.toFile()));
				}
				logWriter.println(new Date(System.currentTimeMillis()).toString());
				logWriter.println(" -REQUEST - ");
				logWriter.println(request.getMethodName());
				logWriter.println(" -RESPONSE - ");
				Header[] headers = method.getResponseHeaders();
				for (int i = 0; i < headers.length; i++) {
					logWriter.println(headers[i].getName() + " - " + headers[i].getValue());
				}
				String response = method.getResponseBodyAsString();
				if (response!=null) {
					logWriter.write(response);
				}
				logWriter.flush();
			} catch (Exception e) {
				e.printStackTrace();
				//nothing to do
			}
		}
	}
	
	pointcut getResponseInputStream(XmlRpcCommonsTransport transport) : 
		if(loggingEnabled())
		&& withincode(protected InputStream XmlRpcCommonsTransport.getInputStream())
		&& call(public InputStream HttpMethod.getResponseBodyAsStream())
		&& this(transport);
	
	before(XmlRpcCommonsTransport transport) : getResponseInputStream(transport) {
		try {
			transport.method.getResponseBody();
		} catch (Exception e) {
		}
	}
	
	
	
	pointcut executeXmlRpcMethod(XmlRpcRequest request, XmlRpcCommonsTransport transport): 
		if(loggingEnabled())
		&& execution (public Object XmlRpcCommonsTransport+.sendRequest(XmlRpcRequest) throws XmlRpcException)
		&& target(transport)
		&& args(request);
	
	after(XmlRpcRequest request, XmlRpcCommonsTransport transport) returning() : executeXmlRpcMethod(request, transport) {
		logCommunication(request, transport.method);
	}
	
	after (XmlRpcRequest request, XmlRpcCommonsTransport transport) throwing(Exception exc) : executeXmlRpcMethod(request, transport) {
		logCommunication(request, transport.method);
	}
	
}