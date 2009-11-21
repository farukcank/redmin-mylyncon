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
package org.svenk.redmine.core.accesscontrol;

import java.util.EnumSet;
import java.util.Map;
import java.util.HashMap;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.svenk.redmine.core.IRedmineClient;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineTaskDataHandler;
import org.svenk.redmine.core.client.RedmineProjectData;
import org.svenk.redmine.core.model.RedmineCustomField;
import org.svenk.redmine.core.model.RedmineTicket;

public aspect RedmineReadonlyAttributeAspect {
	
	/**
	 * @see redmine issue_controller
	 */
	private EnumSet<RedmineAttribute> updatableNoTransition;
	
	private Map<RedmineTicket, Boolean> editAllowed = new HashMap<RedmineTicket, Boolean>();
	private Map<RedmineTicket, Boolean> multipleStatus = new HashMap<RedmineTicket, Boolean>();
	
	public RedmineReadonlyAttributeAspect() {
		updatableNoTransition = EnumSet.noneOf(RedmineAttribute.class);
		updatableNoTransition.add(RedmineAttribute.ASSIGNED_TO);
		updatableNoTransition.add(RedmineAttribute.VERSION);
		updatableNoTransition.add(RedmineAttribute.STATUS);
		updatableNoTransition.add(RedmineAttribute.PROGRESS);
	}
	
	private void checkForReadonly(TaskAttribute taskAttribute, RedmineAttribute redmineAttribute, RedmineTicket ticket) {
		//new ticket - no change
		if (ticket.getId()<1) {
			return;
		}
		
		//read only per default - no change
		if (redmineAttribute.isReadOnly()) {
			return;
		}
		
		//closed Ticket - set all attributes to readonly
		if (ticket.isClosed()) {
			taskAttribute.getMetaData().setReadOnly(true);
			return;
		}
		
		//edit global allow - no change
		if (editAllowed.get(ticket).booleanValue()) {
			return;
		}
		
		//comments allowed - no change
		if (redmineAttribute==RedmineAttribute.COMMENT) {
			return;
		}
		
		//attribute in updatableNoTransition and changing of status possible - no change
		if (multipleStatus.get(ticket).booleanValue() && updatableNoTransition.contains(redmineAttribute)) {
			return;
		}
		
		//set all other to read only)
		taskAttribute.getMetaData().setReadOnly(true);
	}
	
	/* Default Attributes */
	pointcut createDefaultAttributes(RedmineTicket ticket, RedmineProjectData projectData) : 
		execution(private static void RedmineTaskDataHandler.createDefaultAttributes(TaskData, IRedmineClient, RedmineTicket, RedmineProjectData))
		&& args(TaskData, IRedmineClient, ticket, projectData);
	
	pointcut createDefaultAttribute(RedmineAttribute redmineAttribute, RedmineTicket ticket) :
		call(private static TaskAttribute RedmineTaskDataHandler.createAttribute(..))
		&& withincode(private static void RedmineTaskDataHandler.createDefaultAttributes(..))
		&& args(TaskData, redmineAttribute, ..)
		&& cflow(createDefaultAttributes(ticket, RedmineProjectData));
	
	
	before(RedmineTicket ticket, RedmineProjectData projectData) : createDefaultAttributes(ticket, projectData) {
		synchronized (ticket) {
			editAllowed.put(ticket, Boolean.valueOf(projectData.getProject().isIssueEditAllowed()));
			multipleStatus.put(ticket, (ticket.getStatuses()!=null && ticket.getStatuses().size()>1));
		}
	};
	
	after(RedmineAttribute redmineAttribute, RedmineTicket ticket) returning(TaskAttribute attr) : createDefaultAttribute(redmineAttribute, ticket) {
		checkForReadonly(attr, redmineAttribute, ticket);
	}
	

	/* Custom Attributes */
	pointcut createCustomAttribute(TaskData taskData) :
		call(private static TaskAttribute RedmineTaskDataHandler.createAttribute(TaskData, RedmineCustomField))
		&& withincode(private static void RedmineTaskDataHandler.createCustomAttributes(..))
		&& args(taskData, RedmineCustomField);
	
	after(TaskData taskData) returning(TaskAttribute attr) : createCustomAttribute(taskData) {
		TaskAttribute referenceAttribute = taskData.getRoot().getMappedAttribute(RedmineAttribute.CATEGORY.getRedmineKey());
		attr.getMetaData().setReadOnly(referenceAttribute.getMetaData().isReadOnly());
	}
	
}

