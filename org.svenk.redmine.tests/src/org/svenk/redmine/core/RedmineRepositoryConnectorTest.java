/*******************************************************************************
 *
 * Redmine-Mylyn-Connector
 * 
 * This implementation is on the basis of the implementations of Trac and 
 * Bugzilla emerged and contains parts of source code from these projects.
 * The corresponding copyright notice follows below of this.
 * Copyright (C) 2008  Sven Krzyzak and others
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *  
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.svenk.redmine.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author sven
 *
 */
public class RedmineRepositoryConnectorTest {

	private RedmineRepositoryConnector connector;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		connector = new RedmineRepositoryConnector();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#canCreateNewTask(org.eclipse.mylyn.tasks.core.TaskRepository)}.
	 */
	@Test
	public void testCanCreateNewTask() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#canCreateTaskFromKey(org.eclipse.mylyn.tasks.core.TaskRepository)}.
	 */
	@Test
	public void testCanCreateTaskFromKey() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#canQuery(org.eclipse.mylyn.tasks.core.TaskRepository)}.
	 */
	@Test
	public void testCanQuery() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#getConnectorKind()}.
	 */
	@Test
	public void testGetConnectorKind() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#getLabel()}.
	 */
	@Test
	public void testGetLabel() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#getRepositoryUrlFromTaskUrl(java.lang.String)}.
	 */
	@Test
	public void testGetRepositoryUrlFromTaskUrl() {
		assertNull(connector.getRepositoryUrlFromTaskUrl("http://www.domain.tld"));
		assertNull(connector.getRepositoryUrlFromTaskUrl("http://www.domain.tld/path/to/uri"));
		assertEquals("http://www.domain.tld", connector.getRepositoryUrlFromTaskUrl("http://www.domain.tld" + IRedmineConstants.REDMINE_URL_TICKET));
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#getTaskData(org.eclipse.mylyn.tasks.core.TaskRepository, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testGetTaskData() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#getTaskDataHandler()}.
	 */
	@Test
	public void testGetTaskDataHandler() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#getTaskIdFromTaskUrl(java.lang.String)}.
	 */
	@Test
	public void testGetTaskIdFromTaskUrl() {
		assertNull(connector.getTaskIdFromTaskUrl("http://www.domain.tld"));
		assertEquals("2", connector.getTaskIdFromTaskUrl("http://www.domain.tld" + IRedmineConstants.REDMINE_URL_TICKET + "2"));
		assertEquals("22", connector.getTaskIdFromTaskUrl("http://www.domain.tld" + IRedmineConstants.REDMINE_URL_TICKET + "22"));
		assertEquals("22", connector.getTaskIdFromTaskUrl("http://www.domain.tld" + IRedmineConstants.REDMINE_URL_TICKET + "22/..."));
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#getTaskIdPrefix()}.
	 */
	@Test
	public void testGetTaskIdPrefix() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#getTaskUrl(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetTaskUrl() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#hasTaskChanged(org.eclipse.mylyn.tasks.core.TaskRepository, org.eclipse.mylyn.tasks.core.ITask, org.eclipse.mylyn.tasks.core.data.TaskData)}.
	 */
	@Test
	public void testHasTaskChanged() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#performQuery(org.eclipse.mylyn.tasks.core.TaskRepository, org.eclipse.mylyn.tasks.core.IRepositoryQuery, org.eclipse.mylyn.tasks.core.data.TaskDataCollector, org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testPerformQuery() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#updateRepositoryConfiguration(org.eclipse.mylyn.tasks.core.TaskRepository, org.eclipse.core.runtime.IProgressMonitor)}.
	 */
	@Test
	public void testUpdateRepositoryConfiguration() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#updateTaskFromTaskData(org.eclipse.mylyn.tasks.core.TaskRepository, org.eclipse.mylyn.tasks.core.ITask, org.eclipse.mylyn.tasks.core.data.TaskData)}.
	 */
	@Test
	public void testUpdateTaskFromTaskData() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#RedmineRepositoryConnector()}.
	 */
	@Test
	public void testRedmineRepositoryConnector() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#getTaskMapping(org.eclipse.mylyn.tasks.core.data.TaskData)}.
	 */
	@Test
	public void testGetTaskMappingTaskData() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#getClientManager()}.
	 */
	@Test
	public void testGetClientManager() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#stop()}.
	 */
	@Test
	public void testStop() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.svenk.redmine.core.RedmineRepositoryConnector#setTaskRepositoryLocationFactory(org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory)}.
	 */
	@Test
	public void testSetTaskRepositoryLocationFactory() {
		fail("Not yet implemented");
	}

}
