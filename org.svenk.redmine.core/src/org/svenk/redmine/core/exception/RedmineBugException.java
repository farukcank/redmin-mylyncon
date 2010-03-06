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
package org.svenk.redmine.core.exception;

public class RedmineBugException extends RedmineException {

	private static final long serialVersionUID = 1L;

	public RedmineBugException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RedmineBugException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public RedmineBugException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public RedmineBugException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
}