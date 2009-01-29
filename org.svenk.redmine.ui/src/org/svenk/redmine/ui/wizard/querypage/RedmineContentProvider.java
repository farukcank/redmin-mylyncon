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
package org.svenk.redmine.ui.wizard.querypage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.svenk.redmine.core.RedmineProjectData;
import org.svenk.redmine.core.model.RedmineStoredQuery;
import org.svenk.redmine.core.model.RedmineTicketAttribute;

public class RedmineContentProvider implements IStructuredContentProvider {

	String title;

	public RedmineContentProvider() {
		this(null);
	}

	public RedmineContentProvider(String title) {
		this.title = title;
	}

	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			List tmp = (List)inputElement;
			if (title!=null) {
				tmp = new ArrayList<Object>(tmp.size()+1);
				tmp.add(title);
				tmp.addAll((List)inputElement);
			}
			return tmp.toArray();
		}
		return null;
	}

	public void dispose() {
	}

	public void inputChanged(final Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput==null || newInput==null) {
			return;
		}

		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		Object o = selection.getFirstElement();
		
		if (o instanceof RedmineTicketAttribute) {
			if (o instanceof RedmineStoredQuery) {
				selectLastOrDefault(viewer, o);
			} else {
				reselect(viewer, selection);
			}
		} else if (o instanceof RedmineProjectData) {
			selectLastOrDefault(viewer, o);
		} else if (title!=null) {
			selectLastOrDefault(viewer, title);
		}
		
	}
	
	private void selectLastOrDefault(final Viewer viewer, final Object item) {
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				viewer.setSelection(new StructuredSelection(item), true);
			}
		});
	}

	private void reselect(final Viewer viewer, final IStructuredSelection selection) {
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				viewer.setSelection(selection, true);
			}
		});
	}
}
