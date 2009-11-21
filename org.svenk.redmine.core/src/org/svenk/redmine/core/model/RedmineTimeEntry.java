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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RedmineTimeEntry {

	private int id;

	private float hours;
	
	private int activityId;

	private int userId;
	
	private Date spentOn;
	
	private String comments;
	
	private List<RedmineCustomValue> customValues;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public float getHours() {
		return hours;
	}

	public void setHours(float hours) {
		this.hours = hours;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Date getSpentOn() {
		return spentOn;
	}

	public void setSpentOn(Date spentOn) {
		this.spentOn = spentOn;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public void addCustomValue(RedmineCustomValue customValue) {
		if (customValues==null) {
			customValues = new ArrayList<RedmineCustomValue>();
		}
		customValues.add(customValue);
	}

	public RedmineCustomValue[] getCustomValues() {
		return customValues==null ? null : customValues.toArray(new RedmineCustomValue[customValues.size()]);
	}

}
