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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.client.RedmineRestfulClient;

public privileged aspect RedmineClientCommunicationLoggingAspect {

	private PrintWriter logWriter = null;
	
	private HttpMethod method;
	
	private static Object lock = new Object();
	
	private static boolean loggingEnabled() {
		String propVal = System.getProperty("org.svenk.redmine.core.qualitycontrol.RedmineClientCommunicationLoggingAspect.enable", "false");
		return propVal!=null && propVal.equalsIgnoreCase("true");
	}
	
	private void logCommunication(HttpMethod method, Exception exc) {
		if (method.isRequestSent()) {
			synchronized (lock) {
				IPath stateLocation = Platform.getStateLocation(RedmineCorePlugin.getDefault().getBundle());
				IPath cacheFile = stateLocation.append("communicationLog.txt");
				
				try {
					if (logWriter == null) {
						logWriter = new PrintWriter(new FileWriter(cacheFile.toFile(), true));
					}
					logWriter.println(new Date(System.currentTimeMillis()).toString());
					logWriter.println(" -REQUEST - ");
					logWriter.println(method.getPath());
					logWriter.println(" -RESPONSE - ");
					Header[] headers = method.getResponseHeaders();
					for (int i = 0; i < headers.length; i++) {
						logWriter.println(headers[i].getName() + " - " + headers[i].getValue());
					}
					String response = method.getResponseBodyAsString();
					if (response!=null) {
						logWriter.write(response);
					}
					if(exc != null) {
						exc.printStackTrace(logWriter);
					}
					logWriter.flush();
				} catch (Exception e) {
					e.printStackTrace();
					//nothing to do
				}
			}
		}
	}

	pointcut restfulAction() :
		execution(public * RedmineRestfulClient.*(..));

	//Sämtliche Kommunikation protokollieren
	pointcut methodExec(HttpMethod method) :
		if(loggingEnabled())
		&& cflow(restfulAction())
		&& call(* WebUtil.execute(HttpClient, HostConfiguration, HttpMethod, IProgressMonitor))
		&& args (HttpClient, HostConfiguration, method, IProgressMonitor);
	
	after(HttpMethod method) returning() : methodExec(method) {
		try {
			method.getResponseBody();
			logCommunication(method, null);
		} catch (IOException e) {
			System.out.println("Logging failed");
			System.out.println(e);
		}
	}
}
