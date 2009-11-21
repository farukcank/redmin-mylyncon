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
package org.svenk.redmine.core.model;


public class RedmineActivity extends RedmineTicketAttribute {

	private static final long serialVersionUID = 1L;

	private int position;
	
	private boolean defaultPriority;

	public RedmineActivity(String name, int value, int position) {
		super(name, value);
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	public boolean isDefaultPriority() {
		return defaultPriority;
	}

	public void setDefaultPriority(boolean defaultPriority) {
		this.defaultPriority = defaultPriority;
	}

}
