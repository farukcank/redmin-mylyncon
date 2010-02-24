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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.svenk.redmine.core.IRedmineClient;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.exception.RedmineAuthenticationException;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.exception.RedmineRemoteException;
import org.svenk.redmine.core.exception.RedmineStatusException;

public aspect RedmineClientExceptionAspect {

	pointcut catchRuntime() : 
		execution(public * IRedmineClient+.*(..) throws RedmineException);
	
	Object around() throws RedmineException : catchRuntime() {
		try {
			return proceed();
		} catch (RedmineAuthenticationException e) {
			throw e;
		} catch (RedmineRemoteException e) {
			RedmineCorePlugin.getDefault().logUnexpectedException(e);
			throw e;
		} catch (RuntimeException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null, "UNHANDLED_RUNTIME_EXCEPTION");
			StatusHandler.fail(status);
			throw new RedmineStatusException(status);
		}
	}

}
