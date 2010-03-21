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
package org.svenk.redmine.core;

public enum RedmineOperation {

	none(RedmineAttribute.STATUS, Messages.RedmineOperation_LEAVE_AS_STRING, false, true),
	markas(RedmineAttribute.STATUS_CHG, Messages.RedmineOperation_MARK_AS, true, false);
	
	private RedmineAttribute attribute;
	
	private String label;
	
	private boolean assiciated;
	
	private boolean restore;
	
	RedmineOperation(RedmineAttribute attribute, String label, boolean associated, boolean restoreDefault) {
		this.attribute = attribute;
		this.label = label;
		this.assiciated  = associated;
		this.restore = associated==false && restoreDefault;
	}
	
	public String getInputId() {
		return attribute.getTaskKey();
	}

	public String getLabel(Object... args) {
		if(args.length>0) {
			return String.format(label, args);
		}
		return label;
	}
	
	public String getType() {
		return attribute.getType();
	}

	public boolean isAssociated() {
		return assiciated;
	}
	
	public boolean needsRestoreValue() {
		return restore;
	}
	
}
