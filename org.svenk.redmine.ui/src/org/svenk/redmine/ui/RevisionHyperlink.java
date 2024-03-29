package org.svenk.redmine.ui;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.svenk.redmine.core.IRedmineConstants;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineRepositoryConnector;
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.client.RedmineProjectData;

public class RevisionHyperlink implements IHyperlink {

	private IRegion region;
	private TaskRepository taskRepository;
	private ITask task;
	private int revision;
	
	public RevisionHyperlink(IRegion region, TaskRepository taskRepository, ITask task, int revision) {
		this.region = region;
		this.taskRepository = taskRepository;
		this.task = task;
		this.revision = revision;
	}
	
	public IRegion getHyperlinkRegion() {
		return region;
	}

	public String getHyperlinkText() {
		return String.format(Messages.RevisionHyperlink_OPEN_REVISION_INTEGER_STRING, revision, taskRepository.getRepositoryLabel());
	}

	public String getTypeLabel() {
		return null;
	}

	public void open() {
		//TODO use unique Project-Identifier instead of Project-ID
		String product = task.getAttribute(TaskAttribute.PRODUCT);
		//if new task
		if(product==null) {
			try {
				TaskData taskData = TasksUi.getTaskDataManager().getTaskData(task);
				product = taskData.getRoot().getMappedAttribute(RedmineAttribute.PROJECT.getTaskKey()).getValue();
				
				AbstractRepositoryConnector reposConn = TasksUi.getRepositoryConnector(taskRepository.getConnectorKind());
				if (reposConn instanceof RedmineRepositoryConnector) {
					RedmineClientData clientData = ((RedmineRepositoryConnector)reposConn).getClientManager().getClientData(taskRepository);
					if(clientData != null) {
						RedmineProjectData project = clientData.getProjectFromName(product);
						if(project != null) {
							product = ""+project.getProject().getValue(); //$NON-NLS-1$
						}
					}
					
				}
			} catch (Exception e) {
				product = ""; //$NON-NLS-1$
			}
			
		}
		
		StringBuilder builder = new StringBuilder(taskRepository.getRepositoryUrl());
		builder.append(IRedmineConstants.REDMINE_URL_REVISION);
		builder.append(product);
		builder.append("?rev="); //$NON-NLS-1$
		builder.append(revision);
		TasksUiUtil.openUrl(builder.toString());
	}

}
