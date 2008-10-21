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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.IRepositoryListener;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;

public class RedmineClientManager implements IRepositoryListener {

	private Map<String, IRedmineClient> clientByUrl = new HashMap<String, IRedmineClient>();

	private Map<String, RedmineClientData> dataByUrl = new HashMap<String, RedmineClientData>();

	private TaskRepositoryLocationFactory taskRepositoryLocationFactory;
	
	private File cacheFile;
	
	public RedmineClientManager(File cacheFile) {
		this.cacheFile = cacheFile;
		readCache();
	}

	public synchronized IRedmineClient getRedmineClient(TaskRepository taskRepository){
		String repositoryUrl = taskRepository.getRepositoryUrl();
		IRedmineClient repository = clientByUrl.get(repositoryUrl);
		if (repository == null) {
			AbstractWebLocation location = taskRepositoryLocationFactory.createWebLocation(taskRepository);
			
			RedmineClientData data = dataByUrl.get(repositoryUrl);
			if (data==null) {
				data = new RedmineClientData();
				dataByUrl.put(repositoryUrl, data);
			}
			
			repository = RedmineClientFactory.createClient(location, data, taskRepository);
			clientByUrl.put(taskRepository.getRepositoryUrl(), repository);
		}
		return repository;
	}
	
	public TaskRepositoryLocationFactory getTaskRepositoryLocationFactory() {
		return taskRepositoryLocationFactory;
	}

	public void setTaskRepositoryLocationFactory(
			TaskRepositoryLocationFactory taskRepositoryLocationFactory) {
		this.taskRepositoryLocationFactory = taskRepositoryLocationFactory;
	}

	public void repositoryAdded(TaskRepository repository) {
		// TODO Auto-generated method stub
		
	}

	public void repositoryRemoved(TaskRepository repository) {
		clientByUrl.remove(repository.getRepositoryUrl());
		dataByUrl.remove(repository.getRepositoryUrl());
	}

	public void repositorySettingsChanged(TaskRepository repository) {
		// TODO Auto-generated method stub
		
	}

	public void repositoryUrlChanged(TaskRepository repository, String oldUrl) {
		clientByUrl.put(repository.getRepositoryUrl(), clientByUrl.remove(oldUrl));
		dataByUrl.put(repository.getRepositoryUrl(), dataByUrl.remove(oldUrl));
	}
	
	private void readCache() {
		if (cacheFile==null) {
			return;
		}
		
		ObjectInputStream in = null;
		try {
			 in = new ObjectInputStream(new FileInputStream(cacheFile));
			 for(int count=in.readInt();count>0;count--) {
				 dataByUrl.put(in.readObject().toString(), (RedmineClientData)in.readObject());
			 }
		} catch (Throwable e) {
			StatusHandler.log(new Status(IStatus.WARNING, RedmineCorePlugin.PLUGIN_ID,
					"The Redmine respository data cache could not be written", e));
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					//do nothing
				}
			}
		}
	}
	
	void writeCache() {
		if (cacheFile==null) {
			return;
		}

		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(cacheFile));
			
			out.writeInt(dataByUrl.size());
			for(Entry<String, RedmineClientData>  entry : dataByUrl.entrySet()) {
				out.writeObject(entry.getKey());
				out.writeObject(entry.getValue());
			}
			
			out.flush();
		} catch (Throwable e) {
			StatusHandler.log(new Status(IStatus.WARNING, RedmineCorePlugin.PLUGIN_ID,
					"The Redmine respository data cache could not be read", e));
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e1) {
					//do nothing
				}
			}
		}
	}

	
}
