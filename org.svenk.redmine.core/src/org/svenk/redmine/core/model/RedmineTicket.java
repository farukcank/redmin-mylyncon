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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.client.RedmineProjectData;
import org.svenk.redmine.core.model.RedmineCustomField.FieldType;
import org.svenk.redmine.core.util.RedmineUtil;

public class RedmineTicket {

	public enum Key {
		ID("id", true),
//		ADDITIONAL_INFO("additional_information"),
		ASSIGNED_TO("assigned_to"),
		CATEGORY("category"), 
		CREATED_ON("created_on", true), 
		DESCRIPTION("description"), 
		ESTIMATED_HOURS("estimated_hours"), 
		UPDATED_ON("updated_on", true), 
		DUE_DATE("due_date"), 
		START_DATE("start_date"), 
		PRIORITY("priority"),
		PROJECT("project", true), 
//		PROJECTION("projection"),
		RELATIONSHIPS("relationships"),
		AUTHOR("autor", true),  
//		REPRODUCIBILITY("reproducibility"), 
//		RESOLUTION("resolution"), 
		TRACKER("tracker", true), 
		STATUS("status"), 
		DONE_RATIO("done_ratio"), 
//		STEPS_TO_REPRODUCE("steps_to_reproduce"),
		SUBJECT("subject"),
		VERSION("version"), 
		COMMENT("notes"), 
//		VIEW_STATE("view_state"), 
		
		TIME_ENTRY_HOURS("time_entry[hours]"),
		TIME_ENTRY_COMMENTS("time_entry[comments]"),
		TIME_ENTRY_ACTIVITY("time_entry[activity_id]")
		
		;

//		public static Key fromKey(String name) {
//			for (Key key : Key.values()) {
//				if (key.getKey().equals(name)) {
//					return key;
//				}
//			}
//			return null;
//		}

		private String key;
		private boolean readonly;

		Key(String key) {
			this(key, false);
		}

		Key(String key, boolean readonly) {
			this.key = key;
			this.readonly = readonly;
		}
		
		@Override
		public String toString() {
			return key;
		}

		public String getKey() {
			return key;
		}
		
		public boolean isReadonly() {
			return readonly;
		}
		
		public static Key fromString(String string) {
			for (Key key : Key.values()) {
				if (key.toString().equals(string)) {
					return key;
				}
			}
			return null;
		}
		
		public static Key fromTagName(String tagName) {
			String string = null;
			if (tagName.equals("author")) {
				string = "autor";
			} else if(tagName.equals("fixedVersionId")) {
				string = "version";
			} else {
				string = tagName.replaceFirst("Id$", "").replaceAll("([A-Z])", "_$1").toLowerCase();
			}
			return fromString(string);
		}
	}

	private int id;
	
	private Date created;

	private Date lastChanged;
	
	private Map<Key, String> valueByKey = new HashMap<Key, String>();

	private HashMap<Integer, String> valueByCustomFieldId = new HashMap<Integer, String>();
	
	private List<RedmineTicketJournal> journals;

	private List<RedmineAttachment> attachments;
	
	private List<RedmineTicketStatus> statuses;

	private List<RedmineTimeEntry> timeEntries;
	
	private List<Integer> availableStatus;

	private List<RedmineTicketRelation> relations;
	

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
	
	public int getIntValue(Key key) {
		return Integer.parseInt(getValue(key));
	}
	
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
	
	public static RedmineTicket fromTaskData(TaskData taskData, RedmineClientData clientData) {
		RedmineTicket ticket = taskData.getTaskId().equals("") 
			? new RedmineTicket() 
			: new RedmineTicket(Integer.parseInt(taskData.getTaskId()));
		
		Map<String, TaskAttribute> attributeValues = taskData.getRoot().getAttributes();
		
		//default attributes
		for (RedmineAttribute redmineAttribute : RedmineAttribute.values()) {
			if (redmineAttribute.isReadOnly()) {
				continue;
			}
			
			TaskAttribute taskAttribute = attributeValues.get(redmineAttribute.getRedmineKey());
			if (taskAttribute != null) {
				
				String attributeValue = taskAttribute.getValue();
				String type = taskAttribute.getMetaData().getType();

				if (type.equals(TaskAttribute.TYPE_DATE) && !attributeValue.equals("")) {
					attributeValue = RedmineUtil.toFormatedRedmineDate(RedmineUtil.parseDate(attributeValue));
				}

				ticket.putBuiltinValue(redmineAttribute.getTicketKey(), attributeValue);
			}
		}
	
		ticket.completeCustomFields(taskData, clientData);
		
		return ticket;
	}
	
	private void completeCustomFields(TaskData taskData, RedmineClientData clientData) {
		TaskAttribute rootAttribute = taskData.getRoot();
		
		TaskAttribute projAttr = rootAttribute.getMappedAttribute(RedmineAttribute.PROJECT.getRedmineKey());
		RedmineProjectData projectData = clientData.getProjectFromName(projAttr.getValue());

		String attributeValue = null;
		int trackerId = Integer.parseInt(rootAttribute.getMappedAttribute(RedmineAttribute.TRACKER.getRedmineKey()).getValue());
		List<RedmineCustomField> ticketFields = projectData.getCustomTicketFields(trackerId); 
		for (RedmineCustomField customField :ticketFields) {
			//AttributeValue
			TaskAttribute taskAttribute = rootAttribute.getMappedAttribute(RedmineCustomField.TASK_KEY_PREFIX + customField.getId());
			attributeValue = (taskAttribute==null) ? "" : taskAttribute.getValue().trim();
			
			if (customField.getType()==FieldType.DATE && attributeValue.length()>0) {
				attributeValue = RedmineUtil.toFormatedRedmineDate(RedmineUtil.parseDate(attributeValue));
			} else if (customField.getType()==FieldType.BOOL && attributeValue.length()>0) {
				attributeValue = Boolean.parseBoolean(attributeValue) ? "1" : "0";
			}
			this.putCustomFieldValue(Integer.valueOf(customField.getId()), attributeValue);
		}
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setLastChanged(Date lastChanged) {
		this.lastChanged = lastChanged;
	}

}
