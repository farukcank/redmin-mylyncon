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
}
