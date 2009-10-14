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
package org.svenk.redmine.core.qualitycontrol;

import org.apache.commons.httpclient.HttpMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.client.AbstractRedmineClient;
import org.svenk.redmine.core.exception.RedmineException;

public aspect RedmineClientProgressMonitorAspect {

	
	pointcut executeMethod(HttpMethod method, IProgressMonitor monitor) : 
		execution(protected int AbstractRedmineClient.executeMethod(HttpMethod, IProgressMonitor) throws RedmineException)
		&& args (method, monitor);
	
	int around (HttpMethod method, IProgressMonitor monitor) throws RedmineException : executeMethod(method, monitor) {
		if (monitor==null) {
			monitor = new NullProgressMonitor();

			NullPointerException npe = new NullPointerException("IProgressMonitor is null");
			npe.setStackTrace(Thread.currentThread().getStackTrace());
			RedmineCorePlugin.getDefault().logUnexpectedException(npe);
		}
		return proceed(method, monitor);
	}
}
