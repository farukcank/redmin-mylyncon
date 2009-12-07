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


public interface IRedmineConstants {

	public final static double PLUGIN_VERSION_2_2 = 2.2;
	public final static double PLUGIN_VERSION_2_3 = 2.3;
	public final static double PLUGIN_VERSION_2_5 = 2.5;
	public final static double PLUGIN_VERSION_2_6 = 2.6;

	public final static String EDITOR_TYPE_ESTIMATED = "estimated";
	
	public final static String TASK_ATTRIBUTE_TIMEENTRY = "task.redmine.timeentry";
	public final static String TASK_ATTRIBUTE_TIMEENTRY_AUTHOR = "task.redmine.timeentry.author";
	public final static String TASK_ATTRIBUTE_TIMEENTRY_ACTIVITY = "task.redmine.timeentry.activity";
	public final static String TASK_ATTRIBUTE_TIMEENTRY_HOURS = "task.redmine.timeentry.hours";
	public final static String TASK_ATTRIBUTE_TIMEENTRY_SPENTON = "task.redmine.timeentry.spenton";
	public final static String TASK_ATTRIBUTE_TIMEENTRY_COMMENTS = "task.redmine.timeentry.comments";
	public final static String TASK_ATTRIBUTE_TIMEENTRY_CUSTOMVALUE = "task.redmine.timeentry.customvalue.";
	public final static String TASK_ATTRIBUTE_TIMEENTRY_CUSTOMVALUES = "task.redmine.timeentry.customvalues";
	
	public final static String CLIENT_FIELD_CREDENTIALS_USERNAME = "username";
	public final static String CLIENT_FIELD_CREDENTIALS_PASSWORD = "password";
	public final static String CLIENT_FIELD_ATTACHMENT_FILE = "attachments[1][file]";
	public final static String CLIENT_FIELD_ATTACHMENT_DESCRIPTION = "attachments[1][description]";
	public final static String CLIENT_FIELD_ATTACHMENT_NOTES = "notes";
	public final static String CLIENT_FIELD_CSRF_TOKEN = "authenticity_token";
	
	public final static String CLIENT_FIELD_ISSUE_SUBJECT = "issue[subject]";
	public final static String CLIENT_FIELD_ISSUE_DESCRIPTION = "issue[description]";
	public final static String CLIENT_FIELD_ISSUE_NOTES = "notes";
	public final static String CLIENT_FIELD_ISSUE_DONERATIO = "issue[done_ratio]";
	public final static String CLIENT_FIELD_ISSUE_ESTIMATED = "issue[estimated_hours]";
	public final static String CLIENT_FIELD_ISSUE_STARTDATE = "issue[start_date]";
	public final static String CLIENT_FIELD_ISSUE_ENDDATE = "issue[due_date]";
	public final static String CLIENT_FIELD_ISSUE_CUSTOM_R07E = "custom_fields[%d]";
	public final static String CLIENT_FIELD_ISSUE_CUSTOM_R08L = "issue[custom_field_values][%d]";
	public final static String CLIENT_FIELD_ISSUE_FIXEDVERSION = "fixed_version";
	public final static String CLIENT_FIELD_ISSUE_REFERENCED_ID = "issue[%s_id]";

}
