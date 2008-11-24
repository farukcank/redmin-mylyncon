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
package org.svenk.redmine.core.util;

import java.util.Date;

public class RedmineUtil {
	public static Date parseDate(String time) {
		if (time != null) {
			try {
				return RedmineUtil.parseDate(Long.valueOf(time).longValue());
			} catch (NumberFormatException e) {
			}
		}
		return null;
	}

	public static String parseDate(Date date) {
		return "" + date.getTime();
	}
	
	public static Date parseDate(long milliSeconds) {
		return new Date(milliSeconds);
	}

	public static long toRedmineTime(Date date) {
		return date.getTime();
	}
}
