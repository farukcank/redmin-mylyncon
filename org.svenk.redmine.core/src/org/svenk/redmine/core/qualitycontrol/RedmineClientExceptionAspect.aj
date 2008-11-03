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
