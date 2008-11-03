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

package org.svenk.redmine.ui;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.mylyn.tasks.ui.TaskRepositoryLocationUiFactory;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.osgi.framework.BundleContext;
import org.svenk.redmine.core.RedmineCorePlugin;


public class RedmineUiPlugin extends Plugin {

	public static final String PLUGIN_ID = "org.svenk.redmine.ui";

	public RedmineUiPlugin() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		RedmineCorePlugin corePlugin = RedmineCorePlugin.getDefault();
		
		corePlugin.getConnector().setTaskRepositoryLocationFactory(new TaskRepositoryLocationUiFactory());
		TasksUi.getRepositoryManager().addListener(corePlugin.getConnector().getClientManager());
	}
	
}
