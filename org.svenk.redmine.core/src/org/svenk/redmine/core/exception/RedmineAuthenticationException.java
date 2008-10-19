/*******************************************************************************
 *
 * Redmine-Mylyn-Connector
 * 
 * This implementation is on the basis of the implementations of Trac and 
 * Bugzilla emerged and contains parts of source code from these projects.
 * The corresponding copyright notice follows below of this.
 * Copyright (C) 2008  Sven Krzyzak and others
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *  
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
