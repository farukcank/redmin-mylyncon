package org.svenk.redmine.core.qualitycontrol;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.exception.RedmineException;

public aspect RedmineExceptionWithoutMessageAspect {

	pointcut newRedmineException(String message) : 
		call(RedmineException.new(String, ..))
		&& args(message);
	
	
	after(String message) returning() : newRedmineException(message) {
		if (message==null) {
			Exception e = new NullPointerException("RedmineExcepition with Null-Message");
			e.setStackTrace(Thread.currentThread().getStackTrace());
			IStatus status = RedmineCorePlugin.toStatus(e, null);
			StatusHandler.log(status);
		}
	}
}
