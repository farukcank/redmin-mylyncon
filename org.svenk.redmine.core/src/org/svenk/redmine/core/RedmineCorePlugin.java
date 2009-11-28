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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.osgi.framework.BundleContext;

public class RedmineCorePlugin extends Plugin {

	public static final String PLUGIN_ID = "org.svenk.redmine.core";

	public final static String REPOSITORY_KIND = "redmine";
	
	private static RedmineCorePlugin plugin;
	
	private RedmineRepositoryConnector connector;
	
	private PrintWriter logWriter = null;
	
	public static RedmineCorePlugin getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (connector != null) {
			connector.stop();
			connector = null;
		}
		
		if (logWriter != null) {
			logWriter.close();
		}
		
		plugin = null;
		super.stop(context);
	}

	public RedmineRepositoryConnector getConnector() {
		if (connector==null) {
			setConnector(new RedmineRepositoryConnector());
		}
		return connector;
	}
	
	void setConnector(RedmineRepositoryConnector connector) {
		this.connector = connector;
	}

	public static IStatus toStatus(Throwable e, TaskRepository repository) {
		return new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e);
	}

	public static IStatus toStatus(Throwable e, TaskRepository repository, String message, String... params) {
		for (int i = 0; i < params.length; i++) {
			message.replace("{"+i+"}", params[i]==null ? "<NULL>" : params[i]);
		}
		
		//unused placeholders
		message = message.replaceAll("\\{\\d+\\}", "");
		
		return new Status(IStatus.ERROR, PLUGIN_ID, message, e);
	}

	public void logException(IStatus status, Exception exception) {
		IPath stateLocation = Platform.getStateLocation(getBundle());
		IPath cacheFile = stateLocation.append("connectorLog.txt");
		
		try {
			if (logWriter == null) {
				logWriter = new PrintWriter(new FileWriter(cacheFile.toFile()));
			}
			logWriter.println(new Date(System.currentTimeMillis()).toString());
			exception.printStackTrace(logWriter);
			logWriter.flush();
		} catch (NullPointerException e) {
			//nothing to do
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
	
	public void logUnexpectedException(Exception e) {
		IStatus status = RedmineCorePlugin.toStatus(e, null);
		logException(status, e);
	}
	
	protected IPath getRepostioryAttributeCachePath() {
		IPath stateLocation = Platform.getStateLocation(getBundle());
		IPath cacheFile = stateLocation.append("repositoryClientDataCache");
		return cacheFile;
	}

}
