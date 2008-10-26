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
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.svenk.redmine.core.exception.RedmineException;

public class RedmineTaskAttachmentHandler extends AbstractTaskAttachmentHandler {

	private RedmineRepositoryConnector connector;
	
	RedmineTaskAttachmentHandler(RedmineRepositoryConnector connector) {
		this.connector = connector;
	}
	
	@Override
	public boolean canGetContent(TaskRepository repository, ITask task) {
		return true;
	}

	@Override
	public boolean canPostContent(TaskRepository repository, ITask task) {
		return true;
	}

	@Override
	public InputStream getContent(TaskRepository repository, ITask task,
			TaskAttribute attachmentAttribute, IProgressMonitor monitor)
			throws CoreException {
		
		IRedmineClient client = connector.getClientManager().getRedmineClient(repository);
		try {
			TaskAttachmentMapper attachment = TaskAttachmentMapper.createFrom(attachmentAttribute);
			return client.getAttachmentContent(Integer.parseInt(attachment.getAttachmentId()), monitor);
		} catch (RedmineException e) {
			throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
		}
	}

	@Override
	public void postContent(TaskRepository repository, ITask task, AbstractTaskAttachmentSource source, String comment, TaskAttribute attachmentAttribute, IProgressMonitor monitor) throws CoreException {
		String fileName = source.getName();
		String description = "";
		
		if (attachmentAttribute!=null) {
			TaskAttachmentMapper mapper = TaskAttachmentMapper.createFrom(attachmentAttribute);
			if (mapper.getFileName() != null) {
				fileName = mapper.getFileName();
			}
			if (mapper.getComment() != null) {
				comment = mapper.getComment();
			}
			if (mapper.getDescription() != null) {
				description = mapper.getDescription();
			}
		}
		
		IRedmineClient client = connector.getClientManager().getRedmineClient(repository);
		try {
			client.uploadAttachment(Integer.parseInt(task.getTaskId()), fileName, comment, description, source, monitor);
//		} catch (NumberFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (RedmineException e) {
			throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
		}

	}

}
