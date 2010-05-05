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
package org.svenk.redmine.ui.wizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.svenk.redmine.ui.wizard.messages"; //$NON-NLS-1$
	public static String RedmineRepositorySettingsPage_CLIENT_IMPL_DO_SELECT;
	public static String RedmineRepositorySettingsPage_CLIENT_IMPL_NOT_SELECTED;
	public static String RedmineRepositorySettingsPage_CLIENT_IMPL_TITLE;
	public static String RedmineRepositorySettingsPage_INVALID_CREDENTIALS;
	public static String RedmineRepositorySettingsPage_MESSAGE_SUCCESS;
	public static String RedmineRepositorySettingsPage_MESSAGE_VERSION_OUTDATED_ERROR;
	public static String RedmineRepositorySettingsPage_MESSAGE_VERSION_UNKNOWN_ERROR;
	public static String RedmineRepositorySettingsPage_MESSAGE_WIKI_WARNING;
	public static String RedmineRepositorySettingsPage_PAGE_TITLE;
	public static String RedmineRepositorySettingsPage_URL_EXAMPLE;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
