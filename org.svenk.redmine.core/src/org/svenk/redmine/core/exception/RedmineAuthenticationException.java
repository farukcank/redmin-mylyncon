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
import static org.apache.commons.httpclient.HttpStatus.SC_UNAUTHORIZED;
import static org.apache.commons.httpclient.HttpStatus.SC_FORBIDDEN;

public class RedmineAuthenticationException extends RedmineRemoteException {


	private static final long serialVersionUID = 4430763112028334794L;

	public enum Type {UNAUTHORIZED, FORBIDDEN}
	
	private Type type;
	
	public RedmineAuthenticationException(int statusCode) {
		super();
		
		if (statusCode==SC_FORBIDDEN) {
			type = Type.FORBIDDEN;
		} else if (statusCode==SC_UNAUTHORIZED) {
			type = Type.UNAUTHORIZED;
		}
	}

	public boolean isUnauthorized() {
		return type == Type.UNAUTHORIZED;
	}

	public boolean isForbidden() {
		return type == Type.FORBIDDEN;
	}
	
}
