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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.model.RedmineSearch;
import org.svenk.redmine.core.model.RedmineTicket;
import org.svenk.redmine.core.model.RedmineTicketStatus;
import org.svenk.redmine.core.util.RedmineUtil;


public class RedmineRepositoryConnector extends AbstractRepositoryConnector {

	private RedmineClientManager clientManager;
	
	private RedmineTaskDataHandler taskDataHandler;
	
	private final static String CLIENT_LABEL = "Redmine (supports redmine 0.7 with rails 2.0.2 and mylyn plugin)";
	
	private final static Pattern TASK_ID_FROM_TASK_URL = Pattern.compile(IRedmineClient.TICKET_URL + "(\\d+)");

	private TaskRepositoryLocationFactory taskRepositoryLocationFactory;

	@Override
	public boolean canQuery(TaskRepository repository) {
		return true;
	}

	@Override
	public String getTaskIdPrefix() {
		return super.getTaskIdPrefix();
	}

	public RedmineRepositoryConnector() {
		super();
		taskDataHandler = new RedmineTaskDataHandler(this);
		if (RedmineCorePlugin.getDefault() != null) {
			RedmineCorePlugin.getDefault().setConnector(this);
		}
	}
	
	@Override
	public boolean canCreateNewTask(TaskRepository repository) {
		return repository.getConnectorKind().equals(getConnectorKind());
	}

	@Override
	public boolean canCreateTaskFromKey(TaskRepository arg0) {
		return true;
	}

	@Override
	public String getConnectorKind() {
		return RedmineCorePlugin.REPOSITORY_KIND;
	}

	@Override
	public String getLabel() {
		return CLIENT_LABEL;
	}

	@Override
	public String getRepositoryUrlFromTaskUrl(String taskURl) {
		int index=taskURl.indexOf(IRedmineClient.TICKET_URL);
		return (index>0) ? taskURl.substring(0, index) : null;
	}

	@Override
	public AbstractTaskAttachmentHandler getTaskAttachmentHandler() {
		return new RedmineTaskAttachmentHandler(this);
	}
	
	@Override
	public AbstractTaskDataHandler getTaskDataHandler() {
		return taskDataHandler;
	}

	@Override
	public String getTaskIdFromTaskUrl(String taskURl) {
		 Matcher m = TASK_ID_FROM_TASK_URL.matcher(taskURl);
		 return m.find() ? m.group(1): null;
	}

	@Override
	public String getTaskUrl(String repositoryUrl, String taskId) {
		return RedmineRepositoryConnector.getTaskURL(repositoryUrl, taskId);
	}
	
	public static String getTaskURL(String repositoryUrl, String taskId) {
		return repositoryUrl + IRedmineClient.TICKET_URL + taskId;
	}

	public static String getTaskURL(String repositoryUrl, int taskId) {
		return RedmineRepositoryConnector.getTaskURL(repositoryUrl, "" + taskId);
	}
	
	@Override
	public TaskData getTaskData(TaskRepository taskRepository, String taskId,
			IProgressMonitor monitor) throws CoreException {
		return taskDataHandler.getTaskData(taskRepository, taskId, monitor);
	}

	@Override
	public boolean hasTaskChanged(TaskRepository taskRepository, ITask task,
			TaskData taskData) {
		
//		RedmineTaskMapper taskMapper = getTaskMapping(taskData);
		if (taskData.isPartial()) {
			//TODO dürfte gar nicht pasieren
			System.out.println("Partial is true ???");
			throw new RuntimeException("Partial is true ???");
		} else {
			TaskAttribute attribute = taskData.getRoot().getMappedAttribute(RedmineAttribute.DATE_UPDATED.getRedmineKey());
			String repositoryDate = attribute.getValue();
			Date localeDate = task.getModificationDate();
			if (localeDate!=null) {
				return !RedmineUtil.parseDate(localeDate).equals(repositoryDate);
			}
		}
		return false;
	}


	@Override
	public void updateRepositoryConfiguration(TaskRepository repository,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTaskFromTaskData(TaskRepository repository, ITask task,
			TaskData taskData) {
		
		TaskMapper mapper = getTaskMapping(taskData);
		mapper.applyTo(task);

		task.setUrl(getTaskUrl(repository.getUrl(), task.getTaskId()));
		
		RedmineClientData clientData = getClientManager().getRedmineClient(repository).getClientData();

		//Set CompletionDate, if Closed-Status
		TaskAttribute attribute = taskData.getRoot().getMappedAttribute(RedmineAttribute.STATUS.getRedmineKey());
		RedmineTicketStatus status = clientData.getStatus(Integer.parseInt(attribute.getValue()));
		if (status.isClosed()) {
			Date date = task.getCompletionDate();
			if (date==null) {
				attribute = taskData.getRoot().getMappedAttribute(RedmineAttribute.DATE_UPDATED.getRedmineKey());
				if((date=RedmineUtil.parseDate(attribute.getValue()))==null) {
					date = new Date(0);
				}
			}
			task.setCompletionDate(date);
		} else {
			task.setCompletionDate(null);
		}
		
		
		if (!taskData.isPartial()) {
//			Date date = task.getModificationDate();
//			task.setAttribute(TASK_KEY_UPDATE_DATE, (date != null) ? TracUtil.toTracTime(date) + "" : null);
			
			String projectId = taskData.getRoot().getMappedAttribute(RedmineAttribute.PROJECT.getRedmineKey()).getValue();
			task.setAttribute(TaskAttribute.PRODUCT, projectId);

//			RedmineProjectData projectData = getClientManager().getRedmineClient(repository).getClientData().getProjectFromId(Integer.parseInt(projectId));
//			boolean issueEditAllowed = projectData.getProject().isIssueEditAllowed();
//			
//			int allowedStatusCount = taskData.getRoot().getMappedAttribute(RedmineAttribute.STATUS.getRedmineKey()).getOptions().size();
			
			
		}

	}

	
	@Override
	public void preSynchronization(ISynchronizationSession session, IProgressMonitor monitor) throws CoreException {
		
		TaskRepository repository = session.getTaskRepository();
		if (session.getTasks().isEmpty()) {
			return;
		}

		monitor = Policy.monitorFor(monitor);
		monitor.beginTask("Checking for changed tasks", IProgressMonitor.UNKNOWN);
		try {
			//Zeitpunkt der letzten Synchronisierung unbekannt - alle syncronisieren
			if (repository.getSynchronizationTimeStamp() == null) {
				for (ITask task : session.getTasks()) {
					session.markStale(task);
				}
				return;
			}

			Set<ITask> tasks = session.getTasks();

			Date changedSince = RedmineUtil.parseDate(repository.getSynchronizationTimeStamp());
			IRedmineClient client = this.getClientManager().getRedmineClient(repository);
			client.updateAttributes(monitor, false);

			//Enthaelt die ID's der geaenderten Tickets nach Project ID's zusammengefasst
			Map<Integer, List<Integer>> changedByProject = new HashMap<Integer, List<Integer>>();
			
			for (Iterator<ITask> iterator = tasks.iterator(); iterator.hasNext();) {
				ITask task = iterator.next();
				
				//Project ID des Tickets ermitteln
				String projectName = task.getAttribute(TaskAttribute.PRODUCT);
				RedmineProjectData projectData = client.getClientData().getProjectFromName(projectName);
				//projectData kann Null sein, wenn zuvor noch keine Aktion ausgeführt wurde
				if (projectData!= null) {
					Integer projectId = Integer.valueOf(projectData.getProject().getValue());
					
					//Bei Bedarf geaenderte Tickets fuer ProjectId abrufen
					if (!changedByProject.containsKey(projectId)) {
						changedByProject.put(projectId, client.getChangedTicketId(projectId, changedSince));
					}
					
					//Geaenderte Tickets markieren
					List<Integer> changed = changedByProject.get(projectId);
					if (changed != null) {
						if (changed.contains(Integer.valueOf(task.getTaskId()))) {
							session.markStale(task);
						}
					}
				}
			}
			
		} catch (RedmineException e) {
			throw new CoreException(new Status(IStatus.ERROR, RedmineCorePlugin.PLUGIN_ID,
					"Task ID must be an integer", e));
		} finally {
			monitor.done();
		}
	}

	@Override
	public void postSynchronization(ISynchronizationSession event, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask("", 1);
			if (event.isFullSynchronization() && event.getStatus() == null) {
				event.getTaskRepository().setSynchronizationTimeStamp(getSynchronizationTimestamp(event));
			}
		} finally {
			monitor.done();
		}
	}

	@Override
	public IStatus performQuery(TaskRepository repository,
			IRepositoryQuery query, TaskDataCollector resultCollector,
			ISynchronizationSession event, IProgressMonitor monitor) {
		// TODO Auto-generated method stub

		final List<RedmineTicket> tickets = new ArrayList<RedmineTicket>();
		
				IRedmineClient client;
				try {
					client = getClientManager().getRedmineClient(repository);
					client.updateAttributes(monitor, false);
					client.search(query.getAttribute(RedmineSearch.SEARCH_PARAMS), tickets);

					for (RedmineTicket ticket : tickets) {
						TaskData data = taskDataHandler.createTaskDataFromTicket(client, repository, ticket, monitor);
						resultCollector.accept(data);
					}
				} catch (Throwable e) {
					return RedmineCorePlugin.toStatus(e, repository);
				}
		
				return Status.OK_STATUS;
	}

	@Override
	public RedmineTaskMapper getTaskMapping(TaskData taskData) {
		TaskRepository taskRepository = taskData.getAttributeMapper().getTaskRepository();
		IRedmineClient client = (taskRepository != null) ? getClientManager().getRedmineClient(taskRepository) : null;
		return new RedmineTaskMapper(taskData, client);
	}

	public synchronized RedmineClientManager getClientManager() {
		if (clientManager == null) {
			IPath path = RedmineCorePlugin.getDefault().getRepostioryAttributeCachePath();
			clientManager = new RedmineClientManager(path.toFile());
		}
		clientManager.setTaskRepositoryLocationFactory(taskRepositoryLocationFactory);
		return clientManager;
	}

	public void stop() {
		if (clientManager != null) {
			clientManager.writeCache();
		}
	}
	
	public void setTaskRepositoryLocationFactory(TaskRepositoryLocationFactory factory) {
		this.taskRepositoryLocationFactory = factory;
		if (clientManager != null) {
			clientManager.setTaskRepositoryLocationFactory(factory);
		}
	}
	@Override
	public boolean canSynchronizeTask(TaskRepository taskRepository, ITask task) {
		return true;
	}
	
	private String getSynchronizationTimestamp(ISynchronizationSession event) {
		Date mostRecent = new Date(0);
		String mostRecentTimeStamp = event.getTaskRepository().getSynchronizationTimeStamp();
		if (mostRecentTimeStamp != null) {
			mostRecent = RedmineUtil.parseDate(mostRecentTimeStamp);
		}
		for (ITask task : event.getChangedTasks()) {
			Date taskModifiedDate = task.getModificationDate();
			if (taskModifiedDate != null && taskModifiedDate.after(mostRecent)) {
				mostRecent = taskModifiedDate;
			}
		}
		return RedmineUtil.parseDate(mostRecent);
	}

}
