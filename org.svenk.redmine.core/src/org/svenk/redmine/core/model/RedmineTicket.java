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

import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_AUTHOR;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_CREATED;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_DESCRIPTION;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_DONERATIO;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_ENDDATE;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_ESTIMATED;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_REFERENCE_ASSIGNED;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_REFERENCE_CATEGORY;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_REFERENCE_PRIORITY;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_REFERENCE_PROJECT;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_REFERENCE_STATUS;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_REFERENCE_TRACKER;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_REFERENCE_VERSION;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_STARTDATE;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_SUBJECT;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_ISSUE_UPDATED;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_NOTES;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_TIMEENTRY_ACTIVITY;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_TIMEENTRY_COMMENTS;
import static org.svenk.redmine.core.IRedmineConstants.CLIENT_FIELD_TIMEENTRY_HOURS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.svenk.redmine.core.accesscontrol.internal.RedmineAcl;

public class RedmineTicket {

	public enum Key {
		ID("id"),
		ASSIGNED_TO(CLIENT_FIELD_ISSUE_REFERENCE_ASSIGNED),
		CATEGORY(CLIENT_FIELD_ISSUE_REFERENCE_CATEGORY), 
		CREATED_ON(CLIENT_FIELD_ISSUE_CREATED), 
		DESCRIPTION(CLIENT_FIELD_ISSUE_DESCRIPTION), 
		ESTIMATED_HOURS(CLIENT_FIELD_ISSUE_ESTIMATED), 
		UPDATED_ON(CLIENT_FIELD_ISSUE_UPDATED), 
		DUE_DATE(CLIENT_FIELD_ISSUE_ENDDATE), 
		START_DATE(CLIENT_FIELD_ISSUE_STARTDATE), 
		PRIORITY(CLIENT_FIELD_ISSUE_REFERENCE_PRIORITY),
		PROJECT(CLIENT_FIELD_ISSUE_REFERENCE_PROJECT), 
		RELATIONSHIPS("relationships"),
		AUTHOR(CLIENT_FIELD_ISSUE_AUTHOR),  
		TRACKER(CLIENT_FIELD_ISSUE_REFERENCE_TRACKER), 
		STATUS(CLIENT_FIELD_ISSUE_REFERENCE_STATUS), 
		DONE_RATIO(CLIENT_FIELD_ISSUE_DONERATIO), 
		SUBJECT(CLIENT_FIELD_ISSUE_SUBJECT),
		VERSION(CLIENT_FIELD_ISSUE_REFERENCE_VERSION), 
		COMMENT(CLIENT_FIELD_NOTES), 
		
		TIME_ENTRY_TOTAL("spenttime"),
		TIME_ENTRY_HOURS(CLIENT_FIELD_TIMEENTRY_HOURS),
		TIME_ENTRY_COMMENTS(CLIENT_FIELD_TIMEENTRY_COMMENTS),
		TIME_ENTRY_ACTIVITY(CLIENT_FIELD_TIMEENTRY_ACTIVITY)
		
		;

		private String key;

		Key(String key) {
			this.key = key;
		}

		@Override
		public String toString() {
			return key;
		}

		public String getKey() {
			return key;
		}
		
		public static Key fromString(String string) {
			for (Key key : Key.values()) {
				if (key.name().equals(string.toUpperCase())) {
					return key;
				}
			}
			return null;
		}
		
		public static Key fromTagName(String tagName) {
			String string = null;
			if(tagName.equals("fixedVersionId")) {
				string = "version";
			} else {
				string = tagName.replaceFirst("Id$", "").replaceAll("([A-Z])", "_$1").toLowerCase();
			}
			return  fromString(string);
		}
	}

	private int id;
	
	private Date created;

	private Date lastChanged;
	
	private boolean useDonerationField = true;
	
	private Map<Key, String> valueByKey = new HashMap<Key, String>();

	private HashMap<Integer, String> valueByCustomFieldId = new HashMap<Integer, String>();
	
	private List<RedmineTicketJournal> journals;

	private List<RedmineAttachment> attachments;
	
	private List<RedmineTicketStatus> statuses;

	private List<RedmineTimeEntry> timeEntries;
	
	private List<Integer> availableStatus;

	private List<RedmineTicketRelation> relations;
	
	private Map<RedmineAcl, Boolean> accesscontrol = new HashMap<RedmineAcl, Boolean>(3);

	public RedmineTicket() {
	}

	public RedmineTicket(int id) {
		this.id = id;
	}
	
	public void putBuiltinValue(Key key, String value) {
		if (value!=null) {
			valueByKey.put(key, value);
		}
	}

	public void putBuiltinValue(String name, String value) {
		Key key = Key.fromString(name);
		if (key != null) {
			putBuiltinValue(key, value);
		}
	}
	
	public void putBuiltinValue(Key key, Integer value) {
		if (value!=null) {
			putBuiltinValue(key, value.toString());
		}
	}
	
	public void putCustomFieldValue(Integer customFieldId, String value) {
		if (valueByCustomFieldId==null) {
			valueByCustomFieldId = new HashMap<Integer, String>();
		}
		valueByCustomFieldId.put(customFieldId, value);
	}
	
	public Map<String, String> getValues() {
		Map<String, String> result = new HashMap<String, String>();
		for (Key key : valueByKey.keySet()) {
			result.put(key.getKey(), valueByKey.get(key));
		}
		return result;
	}
	
	public Map<Integer, String> getCustomValues() {
		if (valueByCustomFieldId==null) {
			valueByCustomFieldId = new HashMap<Integer, String>(0);
		}
		return Collections.unmodifiableMap(valueByCustomFieldId);
	}

	public void putRight(RedmineAcl right, boolean value) {
		accesscontrol.put(right, value);
	}
	
	public boolean getRight(RedmineAcl right) {
		return accesscontrol.containsKey(right) && accesscontrol.get(right);
	}


	public int getId() {
		return id;
	}

	public Date getCreated() {
		return created;
	}

	public Date getLastChanged() {
		return lastChanged;
	}

	public String getValue(Key key) {
		return valueByKey.get(key);
	}
	
	//TODO unused
//	public int getIntValue(Key key) {
//		//TODO handle NumberFormatException
//		return Integer.parseInt(getValue(key));
//	}
	
	public boolean isClosed() {
		String statusId = valueByKey.get(Key.STATUS);
		if (statusId != null) {
			for (RedmineTicketStatus status : statuses) {
				if (statusId.equals(""+status.getValue())) {
					return status.isClosed();
				}
			}
		}
		return false;
	}
	
	public void addJournal(RedmineTicketJournal journal) {
		if (journals==null) {
			journals = new ArrayList<RedmineTicketJournal>();
		}
		journals.add(journal);
	}
	
	public RedmineTicketJournal[] getJournals() {
		return journals==null ? null : journals.toArray(new RedmineTicketJournal[0]);
	}

	public void addAttachment(RedmineAttachment attachment) {
		if (attachments==null) {
			attachments = new ArrayList<RedmineAttachment>();
		}
		attachments.add(attachment);
	}
	
	public void addRelation(RedmineTicketRelation relation) {
		if (relations==null) {
			relations = new ArrayList<RedmineTicketRelation>();
		}
		relations.add(relation);
	}
	
	public RedmineAttachment[] getAttachments() {
		return attachments==null ? null : attachments.toArray(new RedmineAttachment[0]);
	}

	public RedmineTimeEntry[] getTimeEntries() {
		return timeEntries==null ? null : timeEntries.toArray(new RedmineTimeEntry[0]);
	}

	public void addTimeEntry(RedmineTimeEntry timeEntry) {
		if (timeEntries==null) {
			timeEntries = new ArrayList<RedmineTimeEntry>();
		}
		timeEntries.add(timeEntry);
	}

	public List<RedmineTicketStatus> getStatuses() {
		return statuses==null ? null : Collections.unmodifiableList(statuses);
	}

	public void setStatuses(List<RedmineTicketStatus> statuses) {
		this.statuses = statuses;
	}
	
	public List<RedmineTicketRelation> getRelations() {
		return relations==null ? null : Collections.unmodifiableList(relations);
	}
	
	//TODO nach Entfernung des XmlRpc Clients den Status überarbeiten 
	public List<Integer> getAvailableStatusList() {
		if (availableStatus==null) {
			availableStatus =  new ArrayList<Integer>();
		}
		return availableStatus;
	}
	
	public void setCreated(Date created) {
		this.created = created;
	}

	public void setLastChanged(Date lastChanged) {
		this.lastChanged = lastChanged;
	}

	public boolean getUseDoneratioField() {
		return useDonerationField;
	}

	public void setUseDoneratioField(boolean val) {
		useDonerationField = val;
	}

}
