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

import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.client.RedmineXmlRpcClient;

public class RedmineClientFactory {

	public static IRedmineClient createClient(AbstractWebLocation location, RedmineClientData clientData, TaskRepository repository) {
//		IRedmineClient client =  new RedmineRestfulClient(location, clientData, repository);
//		try {
//			client.checkClientConnection();
//		} catch (RedmineException e) {
//			if (e.getCause() instanceof JAXBException && ((JAXBException)e.getCause()).getLinkedException() instanceof ClassNotFoundException) {
//				Throwable t = ((JAXBException)e.getCause()).getLinkedException();
//				StatusHandler.log(new Status(IStatus.WARNING, RedmineCorePlugin.PLUGIN_ID,
//						"RedmineRestfulClient not usable: " + t.getMessage(), t));
//			}
			return new RedmineXmlRpcClient(location, clientData, repository);
//		}
//		return client;
	}

}
