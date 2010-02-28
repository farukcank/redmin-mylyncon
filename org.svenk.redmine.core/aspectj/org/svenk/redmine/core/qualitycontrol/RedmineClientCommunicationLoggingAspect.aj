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

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.svenk.redmine.core.IRedmineConstants;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.client.AbstractRedmineClient;

public privileged aspect RedmineClientCommunicationLoggingAspect {

	private PrintWriter logWriter = null;
	
	private static Object lock = new Object();

	private static boolean loggingEnabled() {
		String propVal = System.getProperty("org.svenk.redmine.core.qualitycontrol.RedmineClientCommunicationLoggingAspect.enable", "false");
		return propVal!=null && propVal.equalsIgnoreCase("true");
	}
	
	pointcut methodExec(HttpMethod method) :
		if(loggingEnabled())
		&& withincode(protected int AbstractRedmineClient.performExecuteMethod(..))
		&& call(* WebUtil.execute(HttpClient, HostConfiguration, HttpMethod, IProgressMonitor))
		&& args (HttpClient, HostConfiguration, method, IProgressMonitor);
	
	after(HttpMethod method) returning() : methodExec(method) {
		synchronized (lock) {
			logRequest(method);
			logResponse(method);
		}
	}

	after(HttpMethod method) throwing(IOException e) : methodExec(method) {
		synchronized (lock) {
			PrintWriter writer = getLogWriter();
			
			logRequest(method);

			writer.print("** EXCEPTION **");
			e.printStackTrace(writer);
			writer.flush();
			
			logResponse(method);
		}
	}
	
	private void logRequest(HttpMethod method) {
		PrintWriter writer = getLogWriter();
		
		writer.print("** REQUEST - ");
		writer.print(new Date(System.currentTimeMillis()).toString());
		writer.println(" **");
		
		writer.println(" Method       : " + method.getName());
		writer.println(" Path         : " + method.getPath());
		writer.println(" QueryString  : " + method.getQueryString());
		writer.println(" Request send : " + new Boolean(method.isRequestSent()).toString());
		
		if(method instanceof PostMethod) {
			PostMethod postMethod= (PostMethod)method;
			if (postMethod.getParameters().length>=1) {
				writer.println(" Post-Params : ");
				for (NameValuePair nvp : postMethod.getParameters()) {
					if (nvp.getName().equals(IRedmineConstants.CLIENT_FIELD_CREDENTIALS_PASSWORD)) {
						writer.println("  " + nvp.getName() + " : XXXXXX");
					} else {
						writer.println("  " + nvp.getName() + " : " + nvp.getValue());
					}
				}
			} else if (postMethod.getRequestEntity()!=null) {
				writer.print(" Post-Request-Entity : ");
				try {
					ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
					postMethod.getRequestEntity().writeRequest(tmpOut);
					writer.println("");
					writer.println(new String(tmpOut.toByteArray()));
				} catch (IOException e) {
					writer.println("not available");
				}
			}
		}
		
		writer.flush();
	}

	private void logResponse(HttpMethod method) {
		if(!method.isRequestSent()) {
			return;
		}
		
		PrintWriter writer = getLogWriter();
		
		writer.print("** RESPONSE **");
		
		writer.println(" StatusCode : " + method.getStatusCode());
		writer.println(" StatusText : " + method.getStatusCode());
		writer.println(" Header     : ");

		for (Header header : method.getResponseHeaders()) {
			writer.println("  " + header.getName() + " : " + header.getValue());
		}

		writer.println(" Body : ");
		try {
			byte[] body = method.getResponseBody();
			writer.println("");
			writer.println(new String(body));
		} catch (IOException e) {
			writer.println("not available");
		}
		
		writer.flush();
	}

	private  PrintWriter getLogWriter() {
		if(logWriter==null) {
			IPath stateLocation = Platform.getStateLocation(RedmineCorePlugin.getDefault().getBundle());
			IPath cacheFile = stateLocation.append("communicationLog.txt");
			
			final StringWriter string = new StringWriter() {
				public void flush() {
					if (getBuffer().length()>0) {
						IStatus status = new Status(IStatus.INFO, RedmineCorePlugin.PLUGIN_ID, this.toString());
						StatusHandler.log(status);
						getBuffer().delete(0, getBuffer().length());
					}
				};
			};
			
			try {
				final FileWriter file = new FileWriter(cacheFile.toFile(), true);
				
				logWriter = new PrintWriter(new Writer(){
					public void write(char cbuf[], int off, int len) throws IOException {
						string.write(cbuf, off, len);
						file.write(cbuf, off, len);
					}
					public void flush() throws IOException {
						string.flush();
						file.flush();
					}
					public void close() throws IOException {
						file.close();
					}
				});
			} catch(IOException e) {
				IStatus status = new Status(IStatus.INFO, RedmineCorePlugin.PLUGIN_ID, "Communication logging is not available", e);
				StatusHandler.log(status);
				
				logWriter = new PrintWriter(string);
			}
			
		}
		return logWriter;
	}
}
