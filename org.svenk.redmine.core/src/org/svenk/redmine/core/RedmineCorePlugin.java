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
		return new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage());
	}

	protected IPath getRepostioryAttributeCachePath() {
		IPath stateLocation = Platform.getStateLocation(getBundle());
		IPath cacheFile = stateLocation.append("repositoryClientDataCache");
		return cacheFile;
	}

}
