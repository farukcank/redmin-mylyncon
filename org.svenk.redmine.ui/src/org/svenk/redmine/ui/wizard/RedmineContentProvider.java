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
package org.svenk.redmine.ui.wizard;

import java.util.Collection;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

public class RedmineContentProvider implements IStructuredContentProvider {

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Collection) {
			Collection<?> collection = (Collection<?>)inputElement;
			return collection.toArray(new Object[collection.size()]);
		}
		return null;
	}

	public void dispose() {
	}

	public void inputChanged(final Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput==null || newInput==null) {
			return;
		}
		reselect(viewer, viewer.getSelection());
	}
	
	private void reselect(final Viewer viewer, final ISelection selection) {
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				viewer.setSelection(selection, true);
			}
		});
	}
}
