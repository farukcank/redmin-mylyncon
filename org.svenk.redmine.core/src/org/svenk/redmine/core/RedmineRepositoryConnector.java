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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
import org.eclipse.mylyn.tasks.core.data.TaskRelation;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.client.RedmineProjectData;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.model.RedmineSearch;
import org.svenk.redmine.core.model.RedmineTicket;
import org.svenk.redmine.core.model.RedmineTicketRelation;
import org.svenk.redmine.core.model.RedmineTicketStatus;
import org.svenk.redmine.core.model.RedmineTicketRelation.RelationType;
import org.svenk.redmine.core.util.RedmineTaskDataValidator;
import org.svenk.redmine.core.util.RedmineUtil;


public class RedmineRepositoryConnector extends AbstractRepositoryConnector {

	private RedmineClientManager clientManager;
	
	private RedmineTaskDataHandler taskDataHandler;

	private final static String CLIENT_LABEL = "Redmine (supports redmine 0.7 and 0.8 with mylyn plugin)";
	
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
	
	public RedmineTaskDataValidator createNewTaskDataValidator(TaskRepository repository) {
		IRedmineClient client = clientManager.getRedmineClient(repository);
		return new RedmineTaskDataValidator(client.getClientData());
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
		
		TaskAttribute attribute = taskData.getRoot().getMappedAttribute(RedmineAttribute.DATE_UPDATED.getRedmineKey());
		String repositoryDate = attribute.getValue();
		Date localeDate = task.getModificationDate();
		if (localeDate!=null) {
			return !RedmineUtil.parseDate(localeDate).equals(repositoryDate);
		}

		return true;
	}

	@Override
	public boolean isRepositoryConfigurationStale(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
		boolean isStale = super.isRepositoryConfigurationStale(repository, monitor);
		
		if (!isStale) {
			IRedmineClient client = clientManager.getRedmineClient(repository);
			isStale = client.getClientData().needsUpdate();
		}
		
		return isStale;
	}

	@Override
	public void updateRepositoryConfiguration(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
		IRedmineClient client = clientManager.getRedmineClient(repository);
		try {
			client.updateAttributes(true, monitor);
		} catch (RedmineException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, repository);
			RedmineCorePlugin.getDefault().logException(status, e);
			throw new CoreException(status);
		}
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
		
		
//			Date date = task.getModificationDate();
//			task.setAttribute(TASK_KEY_UPDATE_DATE, (date != null) ? TracUtil.toTracTime(date) + "" : null);
			
		String projectName = taskData.getRoot().getMappedAttribute(RedmineAttribute.PROJECT.getRedmineKey()).getValue();
		int projectId = clientData.getProjectFromName(projectName).getProject().getValue();
		task.setAttribute(TaskAttribute.PRODUCT, ""+projectId);

			//			RedmineProjectData projectData = getClientManager().getRedmineClient(repository).getClientData().getProjectFromId(Integer.parseInt(projectId));
//			boolean issueEditAllowed = projectData.getProject().isIssueEditAllowed();
//			
//			int allowedStatusCount = taskData.getRoot().getMappedAttribute(RedmineAttribute.STATUS.getRedmineKey()).getOptions().size();
			
			

	}

	@Override
	public Collection<TaskRelation> getTaskRelations(TaskData taskData) {
		TaskAttribute attr = taskData.getRoot().getMappedAttribute(
				RedmineAttribute.RELATION.getRedmineKey());
		if (attr!=null) {
			Map<String, String> options = attr.getOptions();
			Collection<TaskRelation> relations = 
				new ArrayList<TaskRelation>(options.size());
			
			int taskId = Integer.parseInt(taskData.getTaskId());
			for(Entry<String, String> option : options.entrySet()) {
				RedmineTicketRelation relation = 
					RedmineTicketRelation.fromAttributeName(
							Integer.parseInt(option.getKey()), option.getValue());
				
				if (relation!=null && relation.getType()==RelationType.BLOCKS) {
					TaskRelation taskRelation = relation.getFromTicket()==taskId 
						? TaskRelation.parentTask("" + relation.getToTicket()) 
						: TaskRelation.subtask("" + relation.getFromTicket());
					relations.add(taskRelation);
				}
			}
			return relations;
		}
		return super.getTaskRelations(taskData);
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
			client.updateAttributes(false, monitor);

			//Enthaelt die ID's der geaenderten Tickets nach Project ID's zusammengefasst
			Map<Integer, List<Integer>> changedByProject = new HashMap<Integer, List<Integer>>();
			
			for (Iterator<ITask> iterator = tasks.iterator(); iterator.hasNext();) {
				ITask task = iterator.next();
				
				//Project ID des Tickets ermitteln
				int projectId = 0;
				try {
					projectId = Integer.parseInt(task.getAttribute(TaskAttribute.PRODUCT));
				} catch (NumberFormatException e) {
					//nothing to do
				}
				RedmineProjectData projectData = client.getClientData().getProjectFromId(projectId);
				//projectData kann Null sein, wenn zuvor noch keine Aktion ausgeführt wurde
				if (projectData!= null) {
					//Bei Bedarf geaenderte Tickets fuer ProjectId abrufen
					if (!changedByProject.containsKey(projectId)) {
						changedByProject.put(projectId, client.getChangedTicketId(projectId, changedSince, monitor));
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

		final List<RedmineTicket> tickets = new ArrayList<RedmineTicket>();
		
				IRedmineClient client;
				try {
					client = getClientManager().getRedmineClient(repository);
					client.updateAttributes(false, monitor);
					client.search(query.getAttribute(RedmineSearch.SEARCH_PARAMS), query.getAttribute(RedmineSearch.PROJECT_ID), query.getAttribute(RedmineSearch.STORED_QUERY_ID), tickets, monitor);

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
