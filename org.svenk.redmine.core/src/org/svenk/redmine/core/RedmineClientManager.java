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
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.exception.RedmineException;

public class RedmineClientManager implements IRepositoryListener {

	private Map<String, IRedmineClient> clientByUrl = new HashMap<String, IRedmineClient>();

	private Map<String, RedmineClientData> dataByUrl = new HashMap<String, RedmineClientData>();

	protected TaskRepositoryLocationFactory repositoryLocationFactory = new TaskRepositoryLocationFactory();

	private File cacheFile;
	
	public RedmineClientManager(File cacheFile) {
		this.cacheFile = cacheFile;
		readCache();
	}
	
	public IRedmineClient getRedmineClient(TaskRepository taskRepository) throws RedmineException {
		String repositoryUrl = taskRepository.getRepositoryUrl();

		synchronized (clientByUrl) {
			IRedmineClient client = clientByUrl.get(repositoryUrl);
			if (client == null || !client.getClass().getName().equals(taskRepository.getProperty(RedmineClientFactory.CLIENT_IMPLEMENTATION_CLASS))) {
				
				RedmineClientData data = dataByUrl.get(repositoryUrl);
				if (data==null) {
					data = new RedmineClientData();
					dataByUrl.put(repositoryUrl, data);
				}
				
				client = RedmineClientFactory.createClient(taskRepository, data);
				clientByUrl.put(taskRepository.getRepositoryUrl(), client);
			}
			return client;
		}
	}
	
	public RedmineClientData getClientData(TaskRepository taskRepository) {
		return dataByUrl.get(taskRepository.getRepositoryUrl());
	}
	
	public void repositoryAdded(TaskRepository repository) {
	}

	public void repositoryRemoved(TaskRepository repository) {
		synchronized (clientByUrl) {
			clientByUrl.remove(repository.getRepositoryUrl());
			dataByUrl.remove(repository.getRepositoryUrl());
		}
	}

	public void repositorySettingsChanged(TaskRepository repository) {
		synchronized (clientByUrl) {
			IRedmineClient client = clientByUrl.get(repository.getRepositoryUrl());
			if (client!=null) {
				AbstractWebLocation location = repositoryLocationFactory.createWebLocation(repository);
				client.refreshRepositorySettings(repository, location);
			}
		}
	}

	public void repositoryUrlChanged(TaskRepository repository, String oldUrl) {
		synchronized (clientByUrl) {
			clientByUrl.put(repository.getRepositoryUrl(), clientByUrl.remove(oldUrl));
			dataByUrl.put(repository.getRepositoryUrl(), dataByUrl.remove(oldUrl));
		}
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
					"The Redmine respository data cache could not be read", e));
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
					"The Redmine respository data cache could not be written", e));
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
