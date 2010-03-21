package org.svenk.redmine.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.svenk.redmine.core.messages"; //$NON-NLS-1$
	public static String RedmineOperation_LEAVE_AS_STRING;
	public static String RedmineOperation_MARK_AS;
	public static String RedmineTaskDataHandler_DOWNLOAD_TASK;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
