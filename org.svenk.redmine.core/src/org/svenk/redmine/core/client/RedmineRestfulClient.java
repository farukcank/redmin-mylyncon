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

import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.svenk.redmine.core.AbstractRedmineClient;
import org.svenk.redmine.core.RedmineClientData;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.model.RedmineTicket;

public class RedmineRestfulClient extends AbstractRedmineClient {

	private final static double PLUGIN_VERSION_2_5 = 2.5;

	private double wsVersionMajor = 0;
	private double wsVersionMinor = 0;
	private double wsVersionTiny = 0;

	public RedmineRestfulClient(AbstractWebLocation location, RedmineClientData clientData, TaskRepository repository) {
		super(location, clientData, repository);
		refreshRepositorySettings(repository);
	}

	public void refreshRepositorySettings(TaskRepository repository) {
		super.refreshRepositorySettings(repository);
		//TODO set wsVersion
	}

	@Override
	protected String checkClientVersion() throws RedmineException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Integer> getChangedTicketId(Integer projectId,
			Date changedSince, IProgressMonitor monitor)
			throws RedmineException {
		// TODO Auto-generated method stub
		return null;
	}

	public RedmineClientData getClientData() {
		// TODO Auto-generated method stub
		return null;
	}

	public RedmineTicket getTicket(int id, IProgressMonitor monitor)
			throws RedmineException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasAttributes() {
		// TODO Auto-generated method stub
		return false;
	}

	public void search(String searchParam, String projectId,
			String storedQueryId, List<RedmineTicket> tickets,
			IProgressMonitor monitor) throws RedmineException {
		// TODO Auto-generated method stub

	}

	public boolean supportServersideStoredQueries() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportTaskRelations() {
		// TODO Not implemented yet !!!
		return false;
	}

	public void updateAttributes(boolean force, IProgressMonitor monitor)
			throws RedmineException {
		// TODO Auto-generated method stub

	}
	

}
