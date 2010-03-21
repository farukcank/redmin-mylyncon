package org.svenk.redmine.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.svenk.redmine.ui.messages"; //$NON-NLS-1$
	public static String RedmineErrorReporter_ERROR;
	public static String RedmineErrorReporter_PLEASE_REPORT_THE_ERROR;
	public static String RevisionHyperlink_OPEN_REVISION_INTEGER_STRING;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
