package org.svenk.redmine.core;

import org.eclipse.mylyn.tasks.core.ITaskMapping;

public interface IRedmineTaskMapping extends ITaskMapping {

	public String getTracker();
}
