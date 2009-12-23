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
package org.svenk.redmine.core.client;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.svenk.redmine.core.client.messages"; //$NON-NLS-1$
	public static String AbstractRedmineClient_AUTHENTICATION_CANCELED;
	public static String AbstractRedmineClient_AUTHENTICATION_REQUIRED;
	public static String AbstractRedmineClient_CREDENTIALS_REQUEST_FAILED;
	public static String AbstractRedmineClient_INVALID_AUTHENTICITY_TOKEN;
	public static String AbstractRedmineClient_INVALID_TASK_ID;
	public static String AbstractRedmineClient_MISSING_CREDENTIALS_MANUALLY_SYNC_REQUIRED;
	public static String AbstractRedmineClient_MISSING_TASK_ID_IN_RESPONSE;
	public static String AbstractRedmineClient_READ_OF_UPDATE_RESPONSE_FAILED;
	public static String AbstractRedmineClient_REQUIRED_REDMINE_VERSION;
	public static String AbstractRedmineClient_SERVER_ERROR;
	public static String AbstractRedmineClient_UNHANDLED_RUNTIME_EXCEPTION;
	public static String AbstractRedmineClient_UNHANDLED_SUBMIT_ERROR;
	public static String RedmineRestfulClient_UPDATING_ATTRIBUTES;
	public static String RedmineRestfulStaxReader_PARSING_OF_CUSTOMFIELD_FAILED;
	public static String RedmineRestfulStaxReader_PARSING_OF_CUSTOMFIELD_FAILED_UNKNOWN_TAG;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
