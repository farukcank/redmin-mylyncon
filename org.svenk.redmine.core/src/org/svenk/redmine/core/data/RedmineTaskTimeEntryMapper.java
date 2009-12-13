package org.svenk.redmine.core.data;

import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY;
import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY_ACTIVITY;
import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY_AUTHOR;
import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY_COMMENTS;
import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY_CUSTOMVALUE;
import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY_CUSTOMVALUES;
import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY_HOURS;
import static org.svenk.redmine.core.IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY_SPENTON;

import java.util.Collection;
import java.util.Date;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.model.RedmineCustomValue;
import org.svenk.redmine.core.model.RedmineTimeEntry;

public class RedmineTaskTimeEntryMapper {

	RedmineClientData clientData;

	private int id;

	private float hours;
	
	private int activityId;

	private int userId;
	
	private Date spentOn;
	
	private String comments;
	
	private RedmineCustomValue[] customValues;
	
	public RedmineTaskTimeEntryMapper(RedmineTimeEntry timeEntry, RedmineClientData clientData) {
		Assert.isNotNull(clientData);
		Assert.isNotNull(timeEntry);
		this.clientData = clientData;
		
		readTimeEntry(timeEntry);
		
	}
	
	private RedmineTaskTimeEntryMapper() {
		
	}

	public static RedmineTaskTimeEntryMapper createFrom(TaskAttribute taskAttribute) {
		Assert.isNotNull(taskAttribute);
		
		RedmineTaskTimeEntryMapper mapper = new RedmineTaskTimeEntryMapper();
		mapper.readTaskAttribute(taskAttribute);
		
		return mapper;
	}

	public void applyTo(TaskAttribute taskAttribute) {
		Assert.isNotNull(taskAttribute);
		
		TaskData taskData = taskAttribute.getTaskData();
		TaskAttributeMapper mapper = taskData.getAttributeMapper();
		taskAttribute.getMetaData().defaults().setType(TASK_ATTRIBUTE_TIMEENTRY);
		if (getTimeEntryId() > 0) {
			mapper.setIntegerValue(taskAttribute, getTimeEntryId());
		}
		if (getHours() > 0f) {
			TaskAttribute child = taskAttribute.createMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_HOURS);
			child.getMetaData().defaults().setType(RedmineAttribute.TIME_ENTRY_HOURS.getType());
			child.getMetaData().setLabel(RedmineAttribute.TIME_ENTRY_HOURS.toString());
			mapper.setValue(child, ""+getHours());
		}
		if (getActivityId()>0) {
			TaskAttribute child = taskAttribute.createMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_ACTIVITY);
			child.getMetaData().defaults().setType(RedmineAttribute.TIME_ENTRY_ACTIVITY.getType());
			child.getMetaData().setLabel(RedmineAttribute.TIME_ENTRY_ACTIVITY.toString());
			mapper.setIntegerValue(child, getActivityId());
		}
		if (getUserId()>0) {
			TaskAttribute child = taskAttribute.createMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_AUTHOR);
			child.getMetaData().defaults().setType(TaskAttribute.TYPE_PERSON);
			mapper.setIntegerValue(child, getUserId());
		}
		if (getSpentOn()!=null) {
			TaskAttribute child = taskAttribute.createMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_SPENTON);
			child.getMetaData().defaults().setType(TaskAttribute.TYPE_DATE);
			mapper.setDateValue(child, getSpentOn());
		}
		if (getComments()!=null) {
			TaskAttribute child = taskAttribute.createMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_COMMENTS);
			child.getMetaData().defaults().setType(RedmineAttribute.TIME_ENTRY_COMMENTS.getType());
			child.getMetaData().setLabel(RedmineAttribute.TIME_ENTRY_COMMENTS.toString());
			mapper.setValue(child, getComments());
		}
		if (getCustomValues()!=null) {
			TaskAttribute customValuesAttribute = taskAttribute.createMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_CUSTOMVALUES);
			for (RedmineCustomValue customValue : getCustomValues()) {
				TaskAttribute child = customValuesAttribute.createMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_CUSTOMVALUE + customValue.getCustomFieldId());
				child.getMetaData().defaults().setType(TaskAttribute.TYPE_SHORT_TEXT);
				child.setValue(customValue.getValue());
			}
		}
	}
	
	private void readTaskAttribute(TaskAttribute taskAttribute) {
		TaskData taskData = taskAttribute.getTaskData();
		TaskAttributeMapper mapper = taskData.getAttributeMapper();
		
		id = mapper.getIntegerValue(taskAttribute);
		try {
			hours = Float.parseFloat(mapper.getValue(taskAttribute.getMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_HOURS)));
		} catch (NumberFormatException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null, "INVALID_HOURS_FORMAT_{0}", 
					mapper.getValue(taskAttribute.getMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_HOURS)));
			StatusHandler.log(status);
		}
		activityId = mapper.getIntegerValue(taskAttribute.getMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_ACTIVITY));
		userId = mapper.getIntegerValue(taskAttribute.getMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_AUTHOR));
		spentOn = mapper.getDateValue(taskAttribute.getMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_SPENTON));
		comments = mapper.getValue(taskAttribute.getMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_COMMENTS));
		//TODO customs
	}
	
	private void readTimeEntry(RedmineTimeEntry timeEntry) {
		id = timeEntry.getId();
		hours = timeEntry.getHours();
		activityId = timeEntry.getActivityId();
		userId = timeEntry.getUserId();
		spentOn = timeEntry.getSpentOn();
		comments = timeEntry.getComments();
		customValues = timeEntry.getCustomValues();
	}

	public int getTimeEntryId() {
		return id;
	}

	public float getHours() {
		return hours;
	}

	public int getActivityId() {
		return activityId;
	}

	public int getUserId() {
		return userId;
	}

	public Date getSpentOn() {
		return spentOn;
	}

	public String getComments() {
		return comments;
	}
	
	public RedmineCustomValue[] getCustomValues() {
		return customValues;
	} 
	
	public static TaskAttribute getAuthorAttribute(TaskAttribute timeEntryAttribute) {
		return timeEntryAttribute.getMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_AUTHOR);
	}

	public static TaskAttribute getHoursAttribute(TaskAttribute timeEntryAttribute) {
		return timeEntryAttribute.getMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_HOURS);
	}

	public static TaskAttribute getActivityAttribute(TaskAttribute timeEntryAttribute) {
		return timeEntryAttribute.getMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_ACTIVITY);
	}
	
	public static TaskAttribute getCommentsAttribute(TaskAttribute timeEntryAttribute) {
		return timeEntryAttribute.getMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_COMMENTS);
	}
	
	public static Collection<TaskAttribute> getCustomAttributes(TaskAttribute timeEntryAttribute) {
		TaskAttribute customs = timeEntryAttribute.getMappedAttribute(TASK_ATTRIBUTE_TIMEENTRY_CUSTOMVALUES);
		if (customs!=null) {
			return customs.getAttributes().values();
		}
		return null;
	}

	public static TaskAttribute getCustomAttribute(TaskAttribute timeEntryAttribute, int customFieldId) {
		String[] path = new String[]{TASK_ATTRIBUTE_TIMEENTRY_CUSTOMVALUES, TASK_ATTRIBUTE_TIMEENTRY_CUSTOMVALUE + customFieldId};
		return timeEntryAttribute.getMappedAttribute(path);
	}
}
