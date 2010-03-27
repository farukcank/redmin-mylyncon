package org.svenk.redmine.ui.editor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.svenk.redmine.ui.editor.messages"; //$NON-NLS-1$
	public static String RedmineEstimatedEditor_CLEAR;
	public static String RedmineEstimatedEditor_INVALID_HOURS_FORMAT;
	public static String RedmineNewTimeEntryEditorPart_TIME_ENTRY_NEW_SECTION_TITLE;
	public static String RedminePlanningEditorPart_PLANNING_SECTION_TITLE;
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
