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

import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;

public class RedmineTaskMapper extends TaskMapper implements IRedmineTaskMapping {

	private final IRedmineClient client;
	
	public RedmineTaskMapper(TaskData taskData, IRedmineClient client) {
		super(taskData);
		this.client = client;
	}
	
	@Override
	public PriorityLevel getPriorityLevel() {
		PriorityLevel level =  super.getPriorityLevel();
		if (client != null) {
			//TODO catch Nullpointer and refresh repositoryAttributes
			int priority = client.getClientData().getPriority(getPriority()).getPosition();
			level = PriorityLevel.fromLevel(priority>5 ? 1 : 6-priority);
		}
		return level;
	}

	public void setTracker(String value) {
		setValue(RedmineAttribute.TRACKER.getRedmineKey(), value);
	}
	
	public String getTracker() {
		return getValue(RedmineAttribute.TRACKER.getRedmineKey());
	}
}
