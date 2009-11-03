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
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.model.RedminePriority;

public class RedmineTaskMapper extends TaskMapper implements IRedmineTaskMapping {

	private final RedmineClientData clientData;
	
	public RedmineTaskMapper(TaskData taskData, RedmineClientData clientData) {
		super(taskData);
		this.clientData = clientData;
	}
	
	@Override
	public PriorityLevel getPriorityLevel() {
		PriorityLevel level =  super.getPriorityLevel();

		//TODO null handling + catch refresh repositoryAttributes
		if (clientData!=null) {
			RedminePriority priority = clientData.getPriority(getPriority());
			
			//some tickets references a non existing priority ?!
			if (priority==null) {
				//TODO use default priority
				priority = clientData.getPriorities().get(0);
			}
			
			if (priority==null) {
				PriorityLevel.fromLevel(1);
			} else {
				int pos = priority.getPosition();
				level = PriorityLevel.fromLevel(pos>5 ? 1 : 6-pos);
			}
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
