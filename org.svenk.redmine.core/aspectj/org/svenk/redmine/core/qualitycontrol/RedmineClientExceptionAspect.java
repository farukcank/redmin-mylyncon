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

import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.RedmineXmlRpcClient;
import org.svenk.redmine.core.AbstractRedmineClient;
import org.svenk.redmine.core.exception.RedmineException;

public aspect RedmineClientExceptionAspect {

	pointcut catchRuntime() : 
		execution(public * RedmineXmlRpcClient.*(..) throws RedmineException) 
		|| execution(public * AbstractRedmineClient.*(..) throws RedmineException);

	after() throwing(Exception e) throws RedmineException : catchRuntime() {
		if (e instanceof RuntimeException) {
			RedmineCorePlugin.getDefault().logUnexpectedException(e);
			throw new RedmineException(e.getMessage(), e);
		}
	}
}
