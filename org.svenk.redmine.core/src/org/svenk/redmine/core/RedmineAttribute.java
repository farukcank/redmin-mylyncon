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

import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY_ACTIVITY;
import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY_COMMENTS;
import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY_HOURS;
import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY_TOTAL;
import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_STATUS_CHANGE;

import java.util.EnumSet;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.svenk.redmine.core.RedmineAttributeMapper.Flag;
import org.svenk.redmine.core.model.RedmineTicket.Key;

public enum RedmineAttribute {

	ID(Key.ID, "<used by search engine>", null, TaskAttribute.TYPE_INTEGER, Flag.HIDDEN, Flag.READ_ONLY),
	SUMMARY(Key.SUBJECT, "Summary:", TaskAttribute.SUMMARY, TaskAttribute.TYPE_SHORT_TEXT, Flag.HIDDEN, Flag.REQUIRED),
	REPORTER(Key.AUTHOR, "Reporter:", TaskAttribute.USER_REPORTER, TaskAttribute.TYPE_PERSON, Flag.READ_ONLY),
	DESCRIPTION(Key.DESCRIPTION, "Description:", TaskAttribute.DESCRIPTION, TaskAttribute.TYPE_LONG_RICH_TEXT, Flag.HIDDEN, Flag.REQUIRED),
	ASSIGNED_TO(Key.ASSIGNED_TO, "Assigned To:", TaskAttribute.USER_ASSIGNED, TaskAttribute.TYPE_SINGLE_SELECT),
	DATE_SUBMITTED(Key.CREATED_ON, "Submitted:", TaskAttribute.DATE_CREATION, TaskAttribute.TYPE_DATE, Flag.HIDDEN, Flag.READ_ONLY),
	DATE_UPDATED(Key.UPDATED_ON, "Last Modification:", TaskAttribute.DATE_MODIFICATION, TaskAttribute.TYPE_DATE, Flag.HIDDEN, Flag.READ_ONLY),
	DATE_START(Key.START_DATE, "Start Date:", RedmineAttribute.TASK_KEY_STARTDATE, TaskAttribute.TYPE_DATE, Flag.HIDDEN),
	DATE_DUE(Key.DUE_DATE, "Due Date:", TaskAttribute.DATE_DUE, TaskAttribute.TYPE_DATE, Flag.HIDDEN),
	PROJECT(Key.PROJECT, "Project:", TaskAttribute.PRODUCT, TaskAttribute.TYPE_SHORT_TEXT, Flag.REQUIRED, Flag.READ_ONLY),
	PRIORITY(Key.PRIORITY, "Priority:", TaskAttribute.PRIORITY, TaskAttribute.TYPE_SINGLE_SELECT, Flag.HIDDEN, Flag.REQUIRED),
	CATEGORY(Key.CATEGORY, "Category:", RedmineAttribute.TASK_KEY_CATEGORY, TaskAttribute.TYPE_SINGLE_SELECT),
	VERSION(Key.VERSION, "Target version:", TaskAttribute.VERSION, TaskAttribute.TYPE_SINGLE_SELECT),
	TRACKER(Key.TRACKER, "Tracker:", RedmineAttribute.TASK_KEY_TRACKER, TaskAttribute.TYPE_SINGLE_SELECT, Flag.REQUIRED),
	STATUS(Key.STATUS, "Status:", TaskAttribute.STATUS, TaskAttribute.TYPE_SINGLE_SELECT, Flag.REQUIRED, Flag.HIDDEN),
	STATUS_CHG(Key.STATUS, "Status:", TASK_ATTRIBUTE_STATUS_CHANGE, TaskAttribute.TYPE_SINGLE_SELECT, Flag.OPERATION),
	COMMENT(Key.COMMENT, "Comment: ", TaskAttribute.COMMENT_NEW, TaskAttribute.TYPE_LONG_RICH_TEXT, Flag.HIDDEN),
	PROGRESS(Key.DONE_RATIO, "Done ratio: ", RedmineAttribute.TASK_KEY_PROGRESS, TaskAttribute.TYPE_SINGLE_SELECT),
	ESTIMATED(Key.ESTIMATED_HOURS, "Estimated hours: ", RedmineAttribute.TASK_KEY_ESTIMATE, IRedmineConstants.EDITOR_TYPE_ESTIMATED, Flag.HIDDEN),
	RELATION(Key.RELATIONSHIPS, "Relationship", RedmineAttribute.TASK_KEY_RELATION, null, Flag.HIDDEN, Flag.READ_ONLY),
	
	TIME_ENTRY_TOTAL(Key.TIME_ENTRY_TOTAL, "Total (hours):", TASK_ATTRIBUTE_TIMEENTRY_TOTAL, TaskAttribute.TYPE_SHORT_TEXT, Flag.HIDDEN, Flag.READ_ONLY),
	TIME_ENTRY_HOURS(Key.TIME_ENTRY_HOURS, "Spent time (hours):", TASK_ATTRIBUTE_TIMEENTRY_HOURS, TaskAttribute.TYPE_SHORT_TEXT, Flag.HIDDEN),
	TIME_ENTRY_ACTIVITY(Key.TIME_ENTRY_ACTIVITY, "Activity:", TASK_ATTRIBUTE_TIMEENTRY_ACTIVITY, TaskAttribute.TYPE_SINGLE_SELECT, Flag.HIDDEN),
	TIME_ENTRY_COMMENTS(Key.TIME_ENTRY_COMMENTS, "Comment:", TASK_ATTRIBUTE_TIMEENTRY_COMMENTS, TaskAttribute.TYPE_LONG_TEXT, Flag.HIDDEN)
	; 

	public final static String TASK_KEY_CATEGORY = "task.redmine.category";
	public final static String TASK_KEY_VERSION = "task.redmine.version";
	public final static String TASK_KEY_TRACKER = "task.redmine.tracker";
	public final static String TASK_KEY_PROGRESS = "task.redmine.progress";
	public final static String TASK_KEY_ESTIMATE = "task.redmine.estimate";
	public final static String TASK_KEY_RELATION = "task.redmine.relation";
	public final static String TASK_KEY_STARTDATE = "task.redmine.startdate";
	
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
	
	public static RedmineAttribute fromTaskKey(String taskKey) {
		for (RedmineAttribute attr : RedmineAttribute.values()) {
			if (attr.getTaskKey()!=null && attr.getTaskKey().equals(taskKey)) {
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
	
	public boolean isRequired() {
		return flags.contains(Flag.REQUIRED);
	}
	
	public boolean isOperationValue() {
		return flags.contains(Flag.OPERATION);
	}
	
	@Override
	public String toString() {
		return prettyName;
	}

}