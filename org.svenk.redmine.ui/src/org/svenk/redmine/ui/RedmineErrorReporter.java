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

package org.svenk.redmine.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.mylyn.commons.core.AbstractErrorReporter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.svenk.redmine.core.RedmineCorePlugin;

public class RedmineErrorReporter extends AbstractErrorReporter {

	private boolean errorDialogOpen;

	@Override
	public int getPriority(IStatus status) {
		if (status.getPlugin().equals(RedmineCorePlugin.PLUGIN_ID)) {
			return AbstractErrorReporter.PRIORITY_HIGH;
		}
		return AbstractErrorReporter.PRIORITY_NONE;
	}

	@Override
	/**
	 * @see org.eclipse.mylyn.internal.tasks.ui.DialogErrorReporter
	 */
	public void handle(final IStatus status) {
		if (Platform.isRunning()) {
			final IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) {
				Display display = workbench.getDisplay();
				if (display != null && !display.isDisposed()) {
					display.asyncExec(new Runnable() {
						public void run() {
							try {
								if (!errorDialogOpen) {
									errorDialogOpen = true;
									Shell shell = Display.getDefault().getActiveShell();
									ErrorDialog.openError(shell, Messages.RedmineErrorReporter_ERROR, Messages.RedmineErrorReporter_PLEASE_REPORT_THE_ERROR, status);
								}
							} finally {
								errorDialogOpen = false;
							}
						}
					});
				}
			}
		}
	}

}