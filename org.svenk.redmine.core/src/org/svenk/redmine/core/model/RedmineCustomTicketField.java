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

import java.io.Serializable;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

public class RedmineCustomTicketField implements Serializable, IRedmineQueryField {

	private static final long serialVersionUID = 1L;

	public final static String TASK_KEY_PREFIX = "task.redmine.custom.";

	public enum FieldType implements Serializable {
		STRING(TaskAttribute.TYPE_SHORT_TEXT),
		TEXT(TaskAttribute.TYPE_LONG_TEXT),
		INT(TaskAttribute.TYPE_SHORT_TEXT),
		FLOAT(TaskAttribute.TYPE_SHORT_TEXT),
		LIST(TaskAttribute.TYPE_SINGLE_SELECT),
		DATE(TaskAttribute.TYPE_DATE),
		BOOL(TaskAttribute.TYPE_BOOLEAN);
	
		private String taskAttributeType;
		
		private FieldType(String taskAttributeType) {
			this.taskAttributeType = taskAttributeType;
		}
		
		public String getTaskAttributeType() {
			return taskAttributeType;
		}

		public static FieldType fromString(String name) {
			for (FieldType type : FieldType.values()) {
				if (type.name().equalsIgnoreCase(name)) {
					return type;
				}
			}
			
			return null;
		}
		
	}
	
	private int id;
	
	private FieldType type;
	
	private String name;
	
	private int min, max;
	
	private String validationRegex;
	
	private String defaultValue;
	
	private boolean required;
	
	private boolean supportFilter;
	
	private int[] trackerId;
	
	private String [] listValues;
	
	public RedmineCustomTicketField(int id, String type) {
		setId(id);
		setType(type);
	}

	public  boolean usableForTracker(int trackerId) {
		for (int i=this.trackerId.length-1; i>=0; i--) {
			if (this.trackerId[i]==trackerId) {
				return true;
			}
		}
		return false;
	}
	
	public int getId() {
		return id;
	}

	private void setId(int id) {
		this.id = id;
	}

	public FieldType getType() {
		return type;
	}

	private void setType(String type) {
		this.type = FieldType.fromString(type);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public String getValidationRegex() {
		return validationRegex;
	}

	public void setValidationRegex(String validationRegex) {
		this.validationRegex = validationRegex;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isSupportFilter() {
		return supportFilter;
	}

	public void setSupportFilter(boolean supportFilter) {
		this.supportFilter = supportFilter;
	}

	public int[] getTrackerId() {
		return trackerId;
	}

	public void setTrackerId(int[] trackerId) {
		this.trackerId = trackerId;
	}

	public String[] getListValues() {
		return listValues;
	}

	public void setListValues(String[] listValues) {
		this.listValues = listValues;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass().equals(getClass())) {
			RedmineCustomTicketField custom = (RedmineCustomTicketField)obj;
			return this.id==custom.id && this.type==custom.type;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + id;
		hash = 31 * hash + (null == type ? 0 : type.hashCode());
		return hash;
	}

	public String getQueryValue() {
		return "cf_" + id;
	}
	
	public String getLabel() {
		return getName();
	}
}