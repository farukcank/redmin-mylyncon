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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.model.RedmineMember;
import org.svenk.redmine.core.util.RedmineUtil;

public class RedmineAttributeMapper extends TaskAttributeMapper {

	private RedmineClientData clientData;
	
	enum Flag {
		READ_ONLY, HIDDEN, CUSTOM_FIELD, REQUIRED, OPERATION
	}
	
	public RedmineAttributeMapper(TaskRepository taskRepository, RedmineClientData clientData) {
		super(taskRepository);
		this.clientData = clientData;
	}

	@Override
	public Date getDateValue(TaskAttribute attribute) {
		String val = attribute.getValue();
		if(val !=null && !val.equals("")) {
			return RedmineUtil.parseDate(attribute.getValue());
		}
		return null;
	}
	
	@Override
	public String mapToRepositoryKey(TaskAttribute parent, String key) {
		RedmineAttribute attribute = RedmineAttribute.fromRedmineKey(key);
		return (attribute != null) ? attribute.getTaskKey() : key;
	}
	
	@Override
	public IRepositoryPerson getRepositoryPerson(TaskAttribute taskAttribute) {
		IRepositoryPerson person =  super.getRepositoryPerson(taskAttribute);
		
		if (clientData==null || person.getPersonId()==null || !person.getPersonId().matches(IRedmineConstants.REGEX_INTEGER)) {
			return person;
		}
		
		try {
			RedmineMember member = clientData.getPerson(Integer.parseInt(person.getPersonId()));
			if (member!=null) {
				if (person.getName()==null || person.getName().equals("")) {
					person.setName(member.getName());
				}
			}
		} catch (NumberFormatException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null, "INVALID_MEMBER_ID_{0}", person.getPersonId());
			StatusHandler.log(status);
		}
		
		return person;
	}
}
