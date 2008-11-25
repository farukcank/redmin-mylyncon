package org.svenk.redmine.ui;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.svenk.redmine.core.IRedmineClient;

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
		return "Open revision " + revision + " in " + taskRepository.getRepositoryLabel();
	}

	public String getTypeLabel() {
		return null;
	}

	public void open() {
		StringBuilder builder = new StringBuilder(taskRepository.getRepositoryUrl());
		builder.append(IRedmineClient.REVISION_URL);
		builder.append(task.getAttribute(TaskAttribute.PRODUCT));
		builder.append("?rev=");
		builder.append(revision);
		TasksUiUtil.openUrl(builder.toString());
	}

}
