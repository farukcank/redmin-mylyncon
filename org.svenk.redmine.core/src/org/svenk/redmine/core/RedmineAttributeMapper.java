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

import java.util.Date;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.svenk.redmine.core.util.RedmineUtil;

public class RedmineAttributeMapper extends TaskAttributeMapper {

	enum Flag {
		READ_ONLY, HIDDEN, CUSTOM_FIELD, REQUIRED
	}
	
	public RedmineAttributeMapper(TaskRepository taskRepository) {
		super(taskRepository);
	}

	@Override
	public Date getDateValue(TaskAttribute attribute) {
		return RedmineUtil.parseDate(attribute.getValue());
	}

	@Override
	public String mapToRepositoryKey(TaskAttribute parent, String key) {
		RedmineAttribute attribute = RedmineAttribute.getByTaskKey(key);
		return (attribute != null) ? attribute.getRedmineKey() : key;
	}
}
