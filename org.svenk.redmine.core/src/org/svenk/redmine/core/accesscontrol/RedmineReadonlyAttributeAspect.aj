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

package org.svenk.redmine.core.accesscontrol;

import java.util.EnumSet;
import java.util.Map;
import java.util.HashMap;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.svenk.redmine.core.IRedmineClient;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineProjectData;
import org.svenk.redmine.core.RedmineTaskDataHandler;
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
		//TODO add done_ration
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
	
	pointcut createAttributes(RedmineTicket ticket, RedmineProjectData projectData) : 
		execution(private static void RedmineTaskDataHandler.createDefaultAttributes(TaskData, IRedmineClient, RedmineTicket, RedmineProjectData))
		&& args(TaskData, IRedmineClient, ticket, projectData);
	
	pointcut createAttribute(RedmineAttribute redmineAttribute, RedmineTicket ticket) :
		call(private static TaskAttribute RedmineTaskDataHandler.createAttribute(..))
		&& withincode(private static void RedmineTaskDataHandler.createDefaultAttributes(..))
		&& args(TaskData, redmineAttribute, ..)
		&& cflow(createAttributes(ticket, RedmineProjectData));
	
	before(RedmineTicket ticket, RedmineProjectData projectData) : createAttributes(ticket, projectData) {
		synchronized (ticket) {
			editAllowed.put(ticket, Boolean.valueOf(projectData.getProject().isIssueEditAllowed()));
			multipleStatus.put(ticket, (ticket.getStatuses()!=null && ticket.getStatuses().size()>1));
		}
	};
	
	after(RedmineAttribute redmineAttribute, RedmineTicket ticket) returning(TaskAttribute attr) : createAttribute(redmineAttribute, ticket) {
		checkForReadonly(attr, redmineAttribute, ticket);
	}
	
	
}

