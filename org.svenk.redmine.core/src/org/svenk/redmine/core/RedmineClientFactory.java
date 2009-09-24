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

import java.lang.reflect.Constructor;

import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.exception.RedmineException;

public class RedmineClientFactory {

	public final static String CLIENT_IMPLEMENTATION_CLASS = "clientImplClass";
	
	public final static String CONFIGURATION_MISMATCH = "Configuration mismatch - update your Repository-Settings";
	
	public static IRedmineClient createClient(AbstractWebLocation location, RedmineClientData clientData, TaskRepository repository) throws RedmineException {
		try {
			Class<?> clazz = Class.forName(repository.getProperty(CLIENT_IMPLEMENTATION_CLASS));
			Constructor<?> constr = clazz.getConstructor(AbstractWebLocation.class, RedmineClientData.class, TaskRepository.class);
			IRedmineClient client = (IRedmineClient) constr.newInstance(location, clientData, repository);
			return client;
		} catch (ClassNotFoundException e) {
			//TODO Feedback4User: Falsche/veraltete config Class.forName
			RedmineCorePlugin.getDefault().logUnexpectedException(e);
			throw new RedmineException(CONFIGURATION_MISMATCH, e);
		} catch (Exception e) {
			RedmineCorePlugin.getDefault().logUnexpectedException(e);
			throw new RedmineException(CONFIGURATION_MISMATCH, e);
		}
	}

}
