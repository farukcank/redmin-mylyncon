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

import org.svenk.redmine.core.accesscontrol.internal.RedmineAcl;
import org.svenk.redmine.core.model.RedmineAttachment;
import org.svenk.redmine.core.model.RedmineCustomValue;
import org.svenk.redmine.core.model.RedmineTicket;
import org.svenk.redmine.core.model.RedmineTimeEntry;
import org.svenk.redmine.core.model.RedmineTicket.Key;

public class RedmineRestfulStaxReaderWS26Ticket extends TestCase {

	private RedmineRestfulStaxReader testee;
	
	public RedmineRestfulStaxReaderWS26Ticket() {
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		testee = new RedmineRestfulStaxReader();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testReadTicket3() throws Exception  {
		InputStream in = getClass().getResourceAsStream("/xmldata/issue_3.xml");
		
		RedmineTicket ticket = testee.readTicket(in);
		
		assertNotNull(ticket);
		validateTicket3Core(ticket);
		validateTicket3AvaliableStatus(ticket);
		validateTicket3Watchers(ticket);
		validateTicket3CustomValues(ticket);
		validateTicket3Attachment(ticket);
		validateTicket3TimeEntries(ticket);
		
		in.close();
	}
	
	public void testReadTicket4() throws Exception {
		InputStream in = getClass().getResourceAsStream("/xmldata/issue_4.xml");
		RedmineTicket ticket = testee.readTicket(in);
		assertNotNull(ticket);
		
		assertTrue(ticket.getUseDoneratioField());

		in.close();
	}
	
	protected void validateTicket3Core(RedmineTicket ticket) {
		assertEquals(3, ticket.getId());
		assertEquals("Error 281 when updating a recipe", ticket.getValue(Key.SUBJECT));
		assertEquals("Error 281 is encountered when saving a recipe", ticket.getValue(Key.DESCRIPTION));
		assertEquals("John Smith", ticket.getValue(Key.AUTHOR));
		assertEquals(1153336047000L, ticket.getCreated().getTime());
		assertEquals(1153336047000L, ticket.getLastChanged().getTime());
		assertEquals("1", ticket.getValue(Key.PROJECT));
		assertEquals("1", ticket.getValue(Key.TRACKER));
		assertEquals("4", ticket.getValue(Key.PRIORITY));
		assertEquals("1", ticket.getValue(Key.STATUS));
		//categoryId
		//targetVersionId
		assertEquals("3", ticket.getValue(Key.ASSIGNED_TO));
		assertEquals("0", ticket.getValue(Key.DONE_RATIO));
		//estimatedHours
		assertEquals("2009-11-04", ticket.getValue(Key.START_DATE));
		assertEquals("2009-09-24", ticket.getValue(Key.DUE_DATE));
		//availableStatus
		//watched
		//watchers
		//customValues
		//journals
		//attachments
		//issueRelations
		//timeEntries
		
		assertFalse(ticket.getUseDoneratioField());
	}
	
	@SuppressWarnings("unchecked")
	protected void validateTicket3AvaliableStatus(RedmineTicket ticket) throws Exception {
		Field field = ticket.getClass().getDeclaredField("availableStatus");
		field.setAccessible(true);
		List<Integer> availableStatus = (List<Integer>)field.get(ticket);
		int idx=0;
		assertEquals(6, availableStatus.size());
		assertEquals(2, availableStatus.get(idx++).intValue());
		assertEquals(3, availableStatus.get(idx++).intValue());
		assertEquals(4, availableStatus.get(idx++).intValue());
		assertEquals(5, availableStatus.get(idx++).intValue());
		assertEquals(6, availableStatus.get(idx++).intValue());
		assertEquals(1, availableStatus.get(idx++).intValue());
	}
	
	protected void validateTicket3Watchers(RedmineTicket ticket) {
		//TODO watched false
		//TODO watchers none
	}

	protected void validateTicket3CustomValues(RedmineTicket ticket) {
		Map<Integer, String> cstValues = ticket.getCustomValues();
		assertEquals(2, cstValues.size());
		assertEquals("MySQL", cstValues.get(1));
		assertEquals("125", cstValues.get(2));
	}

	protected void validateTicket3Attachment(RedmineTicket ticket) {
		RedmineAttachment[] attachments = ticket.getAttachments();
		assertNotNull(attachments);
		assertEquals(4, attachments.length);
		assertEquals(4, attachments[1].getId());
		assertEquals(1, attachments[0].getId());
		assertEquals("John Smith", attachments[0].getAuthorName());
		assertEquals(1153336047000L, attachments[0].getCreated().getTime());
		assertEquals("error281.txt", attachments[0].getFilename());
		assertEquals(28, attachments[0].getFilesize());
		assertEquals("b91e08d0cf966d5c6ff411bd8c4cc3a2", attachments[0].getDigest());
		assertEquals("", attachments[0].getDescription());
		assertEquals("text/plain", attachments[0].getContentType());

		assertEquals("application/x-ruby", attachments[1].getContentType());
		assertEquals("This is a Ruby source file", attachments[1].getDescription());
	}

	protected void validateTicket3TimeEntries(RedmineTicket ticket) {
		assertTrue(ticket.getRight(RedmineAcl.TIMEENTRY_VIEW));
		assertFalse(ticket.getRight(RedmineAcl.TIMEENTRY_NEW));
		
		RedmineTimeEntry[] timeEntries = ticket.getTimeEntries();
		assertNotNull(timeEntries);
		assertEquals(1, timeEntries.length);
		
		assertEquals(3, timeEntries[0].getId());
		assertEquals(1.0f, timeEntries[0].getHours());
		assertEquals(9, timeEntries[0].getActivityId());
		assertEquals(1, timeEntries[0].getUserId());
		assertEquals(1177106400000l, timeEntries[0].getSpentOn().getTime());
		assertEquals("", timeEntries[0].getComments());
		
		RedmineCustomValue[] customValues = timeEntries[0].getCustomValues();
		assertNotNull(customValues);
		assertEquals(2, customValues.length);
		assertEquals(2,customValues[0].getCustomFieldId());
		assertEquals("125",customValues[0].getValue());
		assertEquals(1, customValues[1].getCustomFieldId());
		assertEquals("MySQL",customValues[1].getValue());
	}

}
