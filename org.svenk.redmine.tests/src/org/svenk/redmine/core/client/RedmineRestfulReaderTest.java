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
package org.svenk.redmine.core.client;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.svenk.redmine.core.RedmineProjectData;
import org.svenk.redmine.core.model.RedmineAttachment;
import org.svenk.redmine.core.model.RedmineCustomTicketField;
import org.svenk.redmine.core.model.RedmineIssueCategory;
import org.svenk.redmine.core.model.RedmineMember;
import org.svenk.redmine.core.model.RedminePriority;
import org.svenk.redmine.core.model.RedmineProject;
import org.svenk.redmine.core.model.RedmineStoredQuery;
import org.svenk.redmine.core.model.RedmineTicket;
import org.svenk.redmine.core.model.RedmineTicketJournal;
import org.svenk.redmine.core.model.RedmineTicketStatus;
import org.svenk.redmine.core.model.RedmineTracker;
import org.svenk.redmine.core.model.RedmineVersion;
import org.svenk.redmine.core.model.RedmineCustomTicketField.FieldType;
import org.svenk.redmine.core.model.RedmineTicket.Key;

public class RedmineRestfulReaderTest extends TestCase {

	private RedmineRestfulReader testee;
	
	public RedmineRestfulReaderTest() {
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		testee = new RedmineRestfulReader();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testReadTicket() throws Exception  {
		InputStream in = getClass().getResourceAsStream("/xmldata/issue.xml");
		
		RedmineTicket ticket = testee.readTicket(in);
		
		assertNotNull(ticket);
		validateTicket(ticket);
		
	}
	
	public void testReadTickets() throws Exception  {
		InputStream in = getClass().getResourceAsStream("/xmldata/issues.xml");
		
		List<RedmineTicket> tickets = testee.readTickets(in);
		
		assertNotNull(tickets);
		assertEquals(3, tickets.size());
		validateTicket(tickets.get(1));
		assertEquals(7, tickets.get(0).getId());
		assertEquals(8, tickets.get(2).getId());
	}
	
	public void testReadUpdatedTickets() throws Exception  {
		InputStream in = getClass().getResourceAsStream("/xmldata/updatedIssues.xml");
		
		List<Integer> list = testee.readUpdatedTickets(in);
		
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals(1, list.get(0).intValue());
		assertEquals(7, list.get(1).intValue());
	}
	
	public void testReadTicketStatuses() throws Exception  {
		InputStream in = getClass().getResourceAsStream("/xmldata/issuestatus.xml");
		
		List<RedmineTicketStatus> list = testee.readTicketStatuses(in);
		
		assertNotNull(list);
		assertEquals(4, list.size());
		
		RedmineTicketStatus status = list.get(2);
		assertEquals(3, status.getValue());
		assertEquals("drei", status.getName());
		assertEquals(false, status.isDefaultStatus());
		assertEquals(true, status.isClosed());
	}

	public void testReadPriorities() throws Exception  {
		InputStream in = getClass().getResourceAsStream("/xmldata/priorities.xml");
		
		List<RedminePriority> list = testee.readPriorities(in);
		
		assertNotNull(list);
		assertEquals(2, list.size());
		
		RedminePriority priority = list.get(1);
		assertEquals(4, priority.getValue());
		assertEquals("vier", priority.getName());
		assertEquals(true, priority.isDefaultPriority());
	}
	
	public void testReadProjects() throws Exception  {
		InputStream in = getClass().getResourceAsStream("/xmldata/projects.xml");
		
		List<RedmineProjectData> list = testee.readProjects(in);
		
		assertNotNull(list);
		assertEquals(2, list.size());
		
//		assertEquals(2, list.get(1).getProject().getValue());
		validateProjectData(list.get(0));
	}
	
	protected void validateProjectData(RedmineProjectData data) throws Exception {
		RedmineProject project = data.getProject();
		assertEquals(1, project.getValue());
		assertEquals("Project 1", project.getName());
		assertEquals(true, project.isIssueEditAllowed());
		
		List<RedmineTracker> trackers = data.getTrackers();
		assertEquals(3, trackers.size());
		assertEquals(2, trackers.get(1).getValue());
		assertEquals("Feature", trackers.get(1).getName());
		
		List<RedmineVersion> versions = data.getVersions();
		assertEquals(2, versions.size());
		assertEquals(2, versions.get(0).getValue());
		assertEquals("version 2", versions.get(0).getName());
		
		List<RedmineMember> members = data.getMembers();
		assertEquals(1, members.size());
		assertEquals(3, members.get(0).getValue());
		assertEquals("devel FN devel SN", members.get(0).getName());
		assertEquals(true, members.get(0).isAssignable());
	
		List<RedmineIssueCategory> categories = data.getCategorys();
		assertEquals(1, categories.size());
		assertEquals(1, categories.get(0).getValue());
		assertEquals("category 1", categories.get(0).getName());
		
		List<RedmineCustomTicketField> fields = data.getCustomTicketFields();
		assertEquals(3, fields.size());
		assertEquals(1, fields.get(0).getId());
		assertSame(FieldType.STRING, fields.get(0).getType());
		assertSame(FieldType.LIST, fields.get(2).getType());
		assertEquals("cf text 5-10", fields.get(0).getName());
		assertEquals(5, fields.get(0).getMin());
		assertEquals(10, fields.get(0).getMax());
		assertEquals("^[A-Z]\\w+", fields.get(0).getValidationRegex());
		assertEquals("", fields.get(0).getDefaultValue());
		assertEquals("ff", fields.get(2).getDefaultValue());
		assertEquals(0, fields.get(0).getListValues().length);
		assertEquals(true, fields.get(0).isRequired());
		assertEquals(true, fields.get(0).isSupportFilter());
		
		int[] fieldTrackers = fields.get(0).getTrackerId();
		assertEquals(3, fieldTrackers.length);
		assertEquals(1, fieldTrackers[0]);
		assertEquals(3, fieldTrackers[1]);
		assertEquals(4, fieldTrackers[2]);

		String[] listValues = fields.get(2).getListValues();
		assertEquals(3, listValues.length);
		assertEquals("ie6", listValues[0]);
		assertEquals("ie7", listValues[1]);
		assertEquals("ff", listValues[2]);

		List<RedmineStoredQuery> querys = data.getStoredQueries();
		assertEquals(2, querys.size());
		assertEquals(3, querys.get(1).getValue());
		assertEquals("some tickets", querys.get(1).getName());
}
	
	protected void validateTicket(RedmineTicket ticket) throws Exception {
		assertEquals(5, ticket.getId());
		assertEquals("a new bug", ticket.getValue(Key.SUBJECT));
		assertEquals("short description", ticket.getValue(Key.DESCRIPTION));
		assertEquals("anonymous", ticket.getValue(Key.AUTHOR));
		assertEquals(1211376720000L, ticket.getCreated().getTime());
		assertEquals(1243116000000L, ticket.getLastChanged().getTime());
		assertEquals("1", ticket.getValue(Key.PROJECT));
		assertEquals("2", ticket.getValue(Key.TRACKER));
		assertEquals("3", ticket.getValue(Key.PRIORITY));
		assertEquals("1", ticket.getValue(Key.STATUS));
		assertEquals("2", ticket.getValue(Key.CATEGORY));
		assertEquals("1", ticket.getValue(Key.VERSION));
		assertEquals("1", ticket.getValue(Key.ASSIGNED_TO));
		assertEquals("20", ticket.getValue(Key.DONE_RATIO));
		assertEquals("5", ticket.getValue(Key.ESTIMATED_HOURS));
		
		Field field = ticket.getClass().getDeclaredField("availableStatus");
		field.setAccessible(true);
		List<Integer> availableStatus = (List<Integer>)field.get(ticket);
		assertEquals(2, availableStatus.size());
		assertEquals(1, availableStatus.get(0).intValue());
		assertEquals(3, availableStatus.get(1).intValue());
		
		
		Map<Integer, String> cstValues = ticket.getCustomValues();
		assertEquals(2, cstValues.size());
		assertEquals("ff", cstValues.get(1));
		assertEquals("23", cstValues.get(2));
		
		RedmineTicketJournal[] journals = ticket.getJournals();
		assertNotNull(journals);
		assertEquals(2, journals.length);
		assertEquals(1, journals[0].getId());
		assertEquals(4, journals[1].getId());
		assertEquals("sven k", journals[1].getAuthorName());
		assertEquals("long comment", journals[1].getNotes());
		assertEquals(1243116000000L, journals[1].getCreated().getTime());
		assertEquals(true, journals[1].isEditable());

		RedmineAttachment[] attachments = ticket.getAttachments();
		assertNotNull(attachments);
		assertEquals(2, attachments.length);
		assertEquals(2, attachments[1].getId());
		assertEquals(1, attachments[0].getId());
		assertEquals("anonymous", attachments[0].getAuthorName());
		assertEquals(1243116000000L, attachments[0].getCreated().getTime());
		assertEquals("text.txt", attachments[0].getFilename());
		assertEquals(234, attachments[0].getFilesize());
		assertEquals("123455", attachments[0].getDigest());
		assertEquals("descr", attachments[0].getDescription());
	}

}
