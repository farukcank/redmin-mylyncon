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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.model.RedmineMember;
import org.svenk.redmine.core.model.RedminePriority;
import org.svenk.redmine.core.model.RedmineTicket;
import org.svenk.redmine.core.model.RedmineTicketAttribute;
import org.svenk.redmine.core.model.RedmineTicketJournal;
import org.svenk.redmine.core.model.RedmineTicketStatus;
import org.svenk.redmine.core.model.RedmineTicket.Key;


public class RedmineTaskDataHandler extends AbstractTaskDataHandler {

	private RedmineRepositoryConnector connector;

	public RedmineTaskDataHandler(RedmineRepositoryConnector connector) {
		this.connector = connector;
	}

	@Override
	public TaskAttributeMapper getAttributeMapper(TaskRepository taskRepository) {
		return new RedmineAttributeMapper(taskRepository);
	}

	public TaskData getTaskData(TaskRepository repository, String taskId, IProgressMonitor monitor)
	throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			int id = Integer.parseInt(taskId);
			monitor.beginTask("Task Download", IProgressMonitor.UNKNOWN);
			return downloadTaskData(repository, id, monitor);
		} finally {
			monitor.done();
		}
	}

	protected TaskData downloadTaskData(TaskRepository repository,
			int id, IProgressMonitor monitor) throws CoreException {

		IRedmineClient client = connector.getClientManager().getRedmineClient(repository);
		RedmineTicket ticket;
		try {
			client.updateAttributes(monitor, false);
			ticket = client.getTicket(id, monitor);
		} catch (OperationCanceledException e) {
			throw e;
		} catch (RedmineException e) {
			throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
		}
		return createTaskDataFromTicket(client, repository, ticket, monitor);
	}

	public TaskData createTaskDataFromTicket(IRedmineClient client, TaskRepository repository, RedmineTicket ticket,
			IProgressMonitor monitor) throws CoreException {

		TaskData taskData = new TaskData(getAttributeMapper(repository), RedmineCorePlugin.REPOSITORY_KIND,
				repository.getRepositoryUrl(), ticket.getId() + "");
		createDefaultAttributes(taskData, client, ticket);
		updateTaskData(repository, taskData, client, ticket);
		return taskData;
	}

	public static Set<TaskAttribute> updateTaskData(TaskRepository repository, TaskData data,
			IRedmineClient client, RedmineTicket ticket) throws CoreException {

		Set<TaskAttribute> changedAttributes = new HashSet<TaskAttribute>();

		if (ticket.getCreated() != null) {
			TaskAttribute taskAttribute = data.getRoot().getAttribute(RedmineAttribute.DATE_SUBMITTED.getRedmineKey());
			taskAttribute.setValue(ticket.getCreated().getTime() + "");
			changedAttributes.add(taskAttribute);
		}
		
		if (ticket.getLastChanged() != null) {
			TaskAttribute taskAttribute = data.getRoot().getAttribute(RedmineAttribute.DATE_UPDATED.getRedmineKey());
			taskAttribute.setValue(ticket.getLastChanged().getTime() + "");
			changedAttributes.add(taskAttribute);
		}
		
		int projectId = Integer.parseInt(ticket.getValues().get(Key.PROJECT.getKey()));
		RedmineProjectData projectData = client.getClientData().getProjectFromId(projectId);

		Map<String, String> valueByKey = ticket.getValues();
		for (String key : valueByKey.keySet()) {
			TaskAttribute taskAttribute = data.getRoot().getAttribute(key);
			
			if (Key.PROJECT.getKey().equals(key)) {
				taskAttribute.setValue(projectData.getProject().getName());
			}else if (Key.ASSIGNED_TO.getKey().equals(key)) {
				try {
					int memberId = Integer.parseInt(valueByKey.get(key));
//					RedmineMember member = projectData.getMember(memberId);
					taskAttribute.setValue(String.valueOf(memberId));
				} catch (NumberFormatException e) {
				}
			} else {
				taskAttribute.setValue(valueByKey.get(key));
			}
			
			changedAttributes.add(taskAttribute);
		}


		RedmineTicketJournal[] journals = ticket.getJournals();
		if (journals != null) {
			int count = 1;
			for (RedmineTicketJournal journal : journals) {
				TaskCommentMapper mapper = new TaskCommentMapper();
				mapper.setAuthor(repository.createPerson(journal.getAuthorName()));
				mapper.setCreationDate(journal.getCreated());
				mapper.setText(journal.getNotes());

				String commentId = ""+journal.getId();
				String commentUrl = RedmineRepositoryConnector.getTaskURL(repository.getUrl(), ticket.getId());
				mapper.setUrl(commentUrl + IRedmineClient.COMMENT_URL + commentId);
				mapper.setNumber(count++);
				mapper.setCommentId(commentId);

				TaskAttribute attribute = data.getRoot().createAttribute(TaskAttribute.PREFIX_COMMENT + mapper.getCommentId());
				mapper.applyTo(attribute);
			}
		}

		return changedAttributes;
	}

	@Override
	public RepositoryResponse postTaskData(TaskRepository repository,
			TaskData taskData, Set<TaskAttribute> oldAttributes,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		
		RedmineTicket ticket = RedmineTicket.fromTaskData(taskData);
		IRedmineClient client = connector.getClientManager().getRedmineClient(repository);
		
		try {
			if (taskData.isNew() || taskData.getTaskId().equals("")) {
				//set read-only attribute Project
				TaskAttribute projAttr = taskData.getRoot().getMappedAttribute(RedmineAttribute.PROJECT.getRedmineKey());
				RedmineProjectData projectData = client.getClientData().getProjectFromName(projAttr.getValue());
				ticket.putBuiltinValue(RedmineTicket.Key.PROJECT, projectData.getProject().getValue());
				
				int taskId = client.createTicket(ticket, monitor);
				return new RepositoryResponse(ResponseKind.TASK_CREATED, "" + taskId);
				
			} else {
				String comment = taskData.getRoot().getMappedAttribute(RedmineAttribute.COMMENT.getRedmineKey()).getValue();
				comment = (comment==null) ? "" : comment.trim();
				client.updateTicket(ticket, comment);
				return new RepositoryResponse(ResponseKind.TASK_UPDATED, "" + ticket.getId());
			}
		} catch (RedmineException e) {
			throw new CoreException(new Status(IStatus.ERROR, RedmineCorePlugin.PLUGIN_ID,
			e.getMessage(), e));
		}

		
	}
	
	
	@Override
	public boolean initializeTaskData(TaskRepository repository, TaskData data,
			ITaskMapping initializationData, IProgressMonitor monitor)
			throws CoreException {
		
		try {
			IRedmineClient client = connector.getClientManager().getRedmineClient(repository);
			client.updateAttributes(monitor, false);
			RedmineProjectData projectData = client.getClientData().getProjectFromName(initializationData.getProduct());

			RedmineTicket ticket = new RedmineTicket();
			ticket.putBuiltinValue(RedmineTicket.Key.PROJECT, projectData.getProject().getValue());
			
			createDefaultAttributes(data, client, ticket, projectData);
			
			TaskAttribute attr = data.getRoot().getMappedAttribute(RedmineAttribute.PROJECT.getRedmineKey());
			attr.setValue(projectData.getProject().getName());
			
			//set Tracker to writable and select first value as default
			attr = data.getRoot().getMappedAttribute(RedmineAttribute.TRACKER.getRedmineKey());
			attr.getMetaData().setReadOnly(false);
			for(String key : attr.getOptions().keySet()) {
				attr.setValue(key);
				break;
			}
			
			//set default value for attributes Status and Priority
			attr = data.getRoot().getMappedAttribute(RedmineAttribute.STATUS.getRedmineKey());
			for (RedmineTicketStatus status : client.getClientData().getStatuses()) {
				if (status.isDefaultStatus()) {
					attr.setValue("" + status.getValue());
				}
			}
			attr = data.getRoot().getMappedAttribute(RedmineAttribute.PRIORITY.getRedmineKey());
			for (RedminePriority priority : client.getClientData().getPriorities()) {
				if (priority.isDefaultPriority()) {
					attr.setValue("" + priority.getValue());
				}
			}

			return true;
		} catch (OperationCanceledException e) {
			throw e;
		} catch (RedmineException e) {
			throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
		}
	}
	
	public static void createDefaultAttributes(TaskData data, IRedmineClient client, RedmineTicket ticket) {
		int projectId = Integer.parseInt(ticket.getValues().get(Key.PROJECT.getKey()));
		RedmineProjectData projectData = client.getClientData().getProjectFromId(projectId);
		createDefaultAttributes(data, client, ticket, projectData);
	}
	
	private static void createDefaultAttributes(TaskData data, IRedmineClient client, RedmineTicket ticket, RedmineProjectData projectData) {
		boolean existingTask = ticket.getId()>0;
		
		createAttribute(data, RedmineAttribute.SUMMARY);
		createAttribute(data, RedmineAttribute.DESCRIPTION);
		createAttribute(data, RedmineAttribute.PROJECT);

		if (existingTask) {
			createAttribute(data, RedmineAttribute.REPORTER);
			createAttribute(data, RedmineAttribute.DATE_SUBMITTED);
			createAttribute(data, RedmineAttribute.DATE_UPDATED);
			
			createAttribute(data, RedmineAttribute.COMMENT);
			createAttribute(data, RedmineAttribute.STATUS, ticket.getStatuses(), false);
		} else {
			createAttribute(data, RedmineAttribute.STATUS, client.getClientData().getStatuses(), false);
		}

		
		createAttribute(data, RedmineAttribute.PRIORITY, client.getClientData().getPriorities());
		createAttribute(data, RedmineAttribute.CATEGORY, projectData.getCategorys(), true);
		createAttribute(data, RedmineAttribute.VERSION, projectData.getVersions(), true);
		createAttribute(data, RedmineAttribute.ASSIGNED_TO, projectData.getMembers(), !existingTask);
		createAttribute(data, RedmineAttribute.TRACKER, projectData.getTrackers(), false);
		
	}
	
	private static TaskAttribute createAttribute(TaskData data, RedmineAttribute redmineAttribute) {
		TaskAttribute attr = data.getRoot().createAttribute(redmineAttribute.getRedmineKey());
		attr.getMetaData().setType(redmineAttribute.getType());
		attr.getMetaData().setKind(redmineAttribute.getKind());
		attr.getMetaData().setLabel(redmineAttribute.toString());
		attr.getMetaData().setReadOnly(redmineAttribute.isReadOnly());
		return attr;
	}
	
	private static TaskAttribute createAttribute(TaskData data, RedmineAttribute redmineAttribute, List<? extends RedmineTicketAttribute> values) {
		return createAttribute(data, redmineAttribute, values, false);
	}

	private static TaskAttribute createAttribute(TaskData data, RedmineAttribute redmineAttribute, List<? extends RedmineTicketAttribute> values,
			boolean allowEmtpy) {
		TaskAttribute attr = createAttribute(data, redmineAttribute);

		if (values != null && values.size() > 0) {
			if (allowEmtpy) {
				attr.putOption("", "");
			}
			for (Object value : values) {
				assert value instanceof RedmineTicketAttribute;
				if (value instanceof RedmineMember && !((RedmineMember)value).isAssignable()) { 
					continue;
				}
				RedmineTicketAttribute ticketAttribute = (RedmineTicketAttribute)value;
				attr.putOption(String.valueOf(ticketAttribute.getValue()), ticketAttribute.getName());
			}
		} else {
			attr.getMetaData().setReadOnly(true);
		}
		return attr;
	}

	public static RedmineTicket populateTicket(TaskRepository repository, TaskData data) throws CoreException {
		RedmineTicket redmineTicket = null;

		if  (data.isNew()) {
			redmineTicket = new RedmineTicket();
		} else {
			try {
				int id;
				id = Integer.parseInt(data.getTaskId());
				if (id==0) {
					throw new CoreException(new Status(IStatus.ERROR, RedmineCorePlugin.PLUGIN_ID, IStatus.OK,
							"Invalid ticket id: 0", null));
				}
				redmineTicket = new RedmineTicket(id);
			} catch (NumberFormatException e) {
				throw new CoreException(new Status(IStatus.ERROR, RedmineCorePlugin.PLUGIN_ID, IStatus.OK,
						"Invalid ticket id: " + data.getTaskId(), e));
			}
		}
		
		Collection<TaskAttribute> taskAttributes = data.getRoot().getAttributes().values();
		for (TaskAttribute taskAttribute : taskAttributes) {
			if (taskAttribute.getMetaData().isReadOnly()) {
				continue;
			}
			redmineTicket.putBuiltinValue(taskAttribute.getId(), taskAttribute.getValue());
		}

		return redmineTicket;
	}


}