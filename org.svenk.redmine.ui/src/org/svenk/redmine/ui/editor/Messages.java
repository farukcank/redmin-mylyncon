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
package org.svenk.redmine.ui.editor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.svenk.redmine.ui.editor.messages"; //$NON-NLS-1$
	public static String RedmineEstimatedEditor_CLEAR;
	public static String RedmineEstimatedEditor_INVALID_HOURS_FORMAT;
	public static String RedmineNewTimeEntryEditorPart_TIME_ENTRY_NEW_SECTION_TITLE;
	public static String RedminePlanningEditorPart_PLANNING_SECTION_TITLE;
	public static String RedmineTaskEditorPage_INVALID_CF_ID_MSG_WITH_PARAM;
	public static String RedmineTaskEditorPage_INVALID_TRACKER_ID_MSG_WITH_PARAM;
	public static String RedmineTaskEditorPage_MISSING_DESCRIPTION_MSG;
	public static String RedmineTaskEditorPage_MISSING_SUBJECT_MSG;
	public static String RedmineTimeEntryEditorPart_TIME_ENTRIES_HOURS;
	public static String RedmineTimeEntryEditorPart_TIME_ENTRIES_SECTION_TITLE;
	public static String RedmineTimeEntryEditorPart_TIME_ENTRIES_TOTAL;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
