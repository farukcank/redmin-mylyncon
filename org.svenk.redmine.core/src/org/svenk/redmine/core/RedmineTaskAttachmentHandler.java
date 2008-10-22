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

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

public class RedmineTaskAttachmentHandler extends AbstractTaskAttachmentHandler {

	@Override
	public boolean canGetContent(TaskRepository repository, ITask task) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canPostContent(TaskRepository repository, ITask task) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InputStream getContent(TaskRepository repository, ITask task,
			TaskAttribute attachmentAttribute, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postContent(TaskRepository repository, ITask task,
			AbstractTaskAttachmentSource source, String comment,
			TaskAttribute attachmentAttribute, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub

	}

}
