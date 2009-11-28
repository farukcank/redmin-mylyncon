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

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
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
		
		TaskAttachmentMapper attachment = TaskAttachmentMapper.createFrom(attachmentAttribute);
		try {
			IRedmineClient client = connector.getClientManager().getRedmineClient(repository);
			return client.getAttachmentContent(Integer.parseInt(attachment.getAttachmentId()), monitor);
		} catch (NumberFormatException e) {
			throw new CoreException(RedmineCorePlugin.toStatus(e, repository, "INVALID_ATTACHMENT_ID {0}", ""+attachment.getAttachmentId()));
		} catch (RedmineException e) {
			throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
		}
	}

	@Override
	public void postContent(TaskRepository repository, ITask task, AbstractTaskAttachmentSource source, String comment, TaskAttribute attachmentAttribute, IProgressMonitor monitor) throws CoreException {
		String fileName = source.getName();
		String description = source.getDescription();
		
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
		
		try {
			IRedmineClient client = connector.getClientManager().getRedmineClient(repository);
			client.uploadAttachment(Integer.parseInt(task.getTaskId()), fileName, comment, description, source, monitor);
		} catch (NumberFormatException e) {
			throw new CoreException(RedmineCorePlugin.toStatus(e, repository, "INVALID_TASK_ID {0}", task.getTaskId()));
		} catch (RedmineException e) {
			throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
		}

	}

}
