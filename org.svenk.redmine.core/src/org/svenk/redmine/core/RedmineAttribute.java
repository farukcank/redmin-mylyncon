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

package org.svenk.redmine.core;

import java.util.EnumSet;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.svenk.redmine.core.RedmineAttributeMapper.Flag;
import org.svenk.redmine.core.model.RedmineTicket.Key;
import static org.svenk.redmine.core.RedmineAttributeMapper.Flag;

public enum RedmineAttribute {
	ID(Key.ID, "<used by search engine>", null, TaskAttribute.TYPE_INTEGER, Flag.HIDDEN),
	SUMMARY(Key.SUBJECT, "Summary:", TaskAttribute.SUMMARY, TaskAttribute.TYPE_SHORT_TEXT, Flag.HIDDEN),
	REPORTER(Key.AUTHOR, "Reporter:", TaskAttribute.USER_REPORTER, TaskAttribute.TYPE_PERSON),
	DESCRIPTION(Key.DESCRIPTION, "Description:", TaskAttribute.DESCRIPTION, TaskAttribute.TYPE_LONG_RICH_TEXT, Flag.HIDDEN),
	ASSIGNED_TO(Key.ASSIGNED_TO, "Assigned To:", TaskAttribute.USER_ASSIGNED, TaskAttribute.TYPE_SINGLE_SELECT),
	DATE_SUBMITTED(Key.CREATED_ON, "Submitted:", TaskAttribute.DATE_CREATION, TaskAttribute.TYPE_DATE, Flag.HIDDEN),
	DATE_UPDATED(Key.UPDATED_ON, "Last Modification:", TaskAttribute.DATE_MODIFICATION, TaskAttribute.TYPE_DATE, Flag.HIDDEN),
	PROJECT(Key.PROJECT, "Project:", TaskAttribute.PRODUCT, TaskAttribute.TYPE_SHORT_TEXT),
	PRIORITY(Key.PRIORITY, "Priority:", TaskAttribute.PRIORITY, TaskAttribute.TYPE_SINGLE_SELECT),
	CATEGORY(Key.CATEGORY, "Category:", RedmineAttribute.TASK_KEY_CATEGORY, TaskAttribute.TYPE_SINGLE_SELECT),
	VERSION(Key.VERSION, "Target version:", RedmineAttribute.TASK_KEY_VERSION, TaskAttribute.TYPE_SINGLE_SELECT),
	TRACKER(Key.TRACKER, "Tracker:", RedmineAttribute.TASK_KEY_TRACKER, TaskAttribute.TYPE_SINGLE_SELECT),
	STATUS(Key.STATUS, "Status:", TaskAttribute.STATUS, TaskAttribute.TYPE_SINGLE_SELECT),
	COMMENT(Key.COMMENT, "Comment: ", TaskAttribute.COMMENT_NEW, TaskAttribute.TYPE_LONG_RICH_TEXT, Flag.HIDDEN),
	; 

	public final static String TASK_KEY_CATEGORY = "task.redmine.category";
	public final static String TASK_KEY_VERSION = "task.redmine.version";
	public final static String TASK_KEY_TRACKER = "task.redmine.tracker";
	
	private final Key key;
	
	private final String redmineKey;
	
	private final String prettyName;

	private final String taskKey;
	
	private final String type;
	
	private final EnumSet<Flag> flags;

	public static RedmineAttribute getByTaskKey(String taskKey) {
		for (RedmineAttribute attribute : values()) {
			if (taskKey.equals(attribute.getTaskKey())) {
				return attribute;
			}
		}
		return null;
	}

	RedmineAttribute(Key key, String prettyName, String taskKey, String type, Flag... flags) {
		this.key = key;
		this.redmineKey = key.getKey();
		this.taskKey = taskKey;
		this.prettyName = prettyName;
		this.type = type;
		
		this.flags = flags.length==0 || flags[0]==null ? EnumSet.noneOf(Flag.class) : EnumSet.of(flags [0], flags);
		if (key.isReadonly() && !this.flags.contains(Flag.READ_ONLY)) {
			this.flags.add(Flag.READ_ONLY);
		}
	}
	
	RedmineAttribute(Key key, String prettyName, String taskKey, String type) {		
		this(key, prettyName, taskKey, type, (Flag)null);
	}

	public String getTaskKey() {
		return taskKey;
	}

	public String getRedmineKey() {
		return redmineKey;
	}
	
	public Key getTicketKey() {
		return key;
	}

	public static RedmineAttribute fromRedmineKey(String redmineKey) {
		for (RedmineAttribute attr : RedmineAttribute.values()) {
			if (attr.getRedmineKey().equals(redmineKey)) {
				return attr;
			}
		}
		return null;
	}
	
	public String getKind() {
		if (isHidden()) {
			return null;
		}

		switch (this) {
		case REPORTER :
		case ASSIGNED_TO :
			return TaskAttribute.KIND_PEOPLE;
		}
		return TaskAttribute.KIND_DEFAULT;
	}
	
	public String getType() {
		return type;
	}
	
	public boolean isReadOnly() {
		return flags.contains(Flag.READ_ONLY);
	}
	
	public boolean isHidden() {
		return flags.contains(Flag.HIDDEN);
	}
	
	@Override
	public String toString() {
		return prettyName;
	}
}