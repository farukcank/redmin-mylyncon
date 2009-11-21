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
import java.util.List;

import junit.framework.TestCase;

import org.svenk.redmine.core.model.RedmineCustomField;
import org.svenk.redmine.core.model.RedmineCustomField.FieldType;

public class RedmineRestfulStaxReaderWS26Test extends TestCase {

	private RedmineRestfulStaxReader testee;
	
	public RedmineRestfulStaxReaderWS26Test() {
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		testee = new RedmineRestfulStaxReader();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}


	public void testCustomFields() throws Exception  {
		InputStream in = getClass().getResourceAsStream("/xmldata/customfields.xml");
		
		List<RedmineCustomField> list = testee.readCustomFields(in);
		
		assertNotNull(list);
		assertEquals(3, list.size());//Nur Issue- ind TimeentryCustomFIelds
		
		RedmineCustomField field = list.get(1);
		assertEquals(2, field.getId());
		assertEquals(RedmineCustomField.CustomType.IssueCustomField, field.getCustomType());
		assertEquals(RedmineCustomField.FieldType.STRING, field.getType());
		assertEquals("Searchable field", field.getName());
		assertEquals(1, field.getMin());
		assertEquals(100, field.getMax());
		assertFalse(field.isRequired());

		in.close();
	}

	public void testReadProjects() throws Exception  {
		InputStream in = getClass().getResourceAsStream("/xmldata/projects2_6.xml");
		
		List<RedmineProjectData> list = testee.readProjects(in);
		
		assertNotNull(list);
		assertEquals(2, list.size());
		
//		assertEquals(2, list.get(1).getProject().getValue());
		validateProjectData(list.get(0));
	}
	
	protected void validateProjectData(RedmineProjectData data) throws Exception {
//		RedmineProject project = data.getProject();
//		assertEquals(1, project.getValue());
//		assertEquals("Project 1", project.getName());
//		assertEquals(true, project.isIssueEditAllowed());
//		
//		List<RedmineTracker> trackers = data.getTrackers();
//		assertEquals(3, trackers.size());
//		assertEquals(2, trackers.get(1).getValue());
//		assertEquals("Feature", trackers.get(1).getName());
//		
//		List<RedmineVersion> versions = data.getVersions();
//		assertEquals(2, versions.size());
//		assertEquals(2, versions.get(0).getValue());
//		assertEquals("version 2", versions.get(0).getName());
//		
//		List<RedmineMember> members = data.getMembers();
//		assertEquals(1, members.size());
//		assertEquals(3, members.get(0).getValue());
//		assertEquals("devel FN devel SN", members.get(0).getName());
//		assertEquals(true, members.get(0).isAssignable());
//	
//		List<RedmineIssueCategory> categories = data.getCategorys();
//		assertEquals(1, categories.size());
//		assertEquals(1, categories.get(0).getValue());
//		assertEquals("category 1", categories.get(0).getName());
		
		List<RedmineCustomField> fields = data.getCustomTicketFields();
		assertEquals(2, fields.size());
		assertEquals(1, fields.get(0).getId());
		assertSame(FieldType.LIST, fields.get(0).getType());
		assertSame(FieldType.STRING, fields.get(1).getType());
		assertEquals("Database", fields.get(0).getName());
		assertEquals(1, fields.get(1).getMin());
		assertEquals(100, fields.get(1).getMax());
		assertEquals("", fields.get(0).getValidationRegex());
		assertEquals("", fields.get(0).getDefaultValue());
		assertEquals("Default string", fields.get(1).getDefaultValue());
		assertEquals(3, fields.get(0).getListValues().length);
		assertEquals(false, fields.get(0).isRequired());
		assertEquals(true, fields.get(0).isSupportFilter());
		
		int[] fieldTrackers = fields.get(1).getTrackerId();
		assertEquals(2, fieldTrackers.length);
		assertEquals(1, fieldTrackers[0]);
		assertEquals(3, fieldTrackers[1]);

		String[] listValues = fields.get(0).getListValues();
		assertEquals(3, listValues.length);
		assertEquals("MySQL", listValues[0]);
		assertEquals("PostgreSQL", listValues[1]);
		assertEquals("Oracle", listValues[2]);

//		List<RedmineStoredQuery> querys = data.getStoredQueries();
//		assertEquals(2, querys.size());
//		assertEquals(3, querys.get(1).getValue());
//		assertEquals("some tickets", querys.get(1).getName());
}


}
