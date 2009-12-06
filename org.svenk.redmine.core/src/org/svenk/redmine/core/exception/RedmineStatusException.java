package org.svenk.redmine.core.exception;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.svenk.redmine.core.RedmineCorePlugin;

public class RedmineStatusException extends RedmineException {

	private static final long serialVersionUID = 1;
	
	private final IStatus status;

	public RedmineStatusException(IStatus status) {
		super(status.getMessage(), status.getException());
		this.status = status;
	}

	public RedmineStatusException(int severity, String message) {
		super(message);
		this.status = new Status(severity, RedmineCorePlugin.PLUGIN_ID, message);
	}
	
	public IStatus getStatus() {
		return status;
	}

}
