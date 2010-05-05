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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TaskRepositoryLocationUiFactory;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.ui.internal.IRedmineAttributeChangedListener;


public class RedmineUiPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.svenk.redmine.ui";
	
	private final ISelectionListener selectionListener;
	
	private IStructuredSelection selection;

	private List<IRedmineAttributeChangedListener> attributeListeners;
	
	private static RedmineUiPlugin plugin;
	
	
	public static RedmineUiPlugin getDefault() {
		return plugin;
	}

	public RedmineUiPlugin() {
		super();
		
		selectionListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart arg0, ISelection arg1) {
				if (arg1 instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection)arg1;
					RedmineUiPlugin.this.selection = selection.isEmpty() ? null : selection;
				}
			}
		};
		
		attributeListeners = new ArrayList<IRedmineAttributeChangedListener>();
	}

	public void addAttributeChangedListener(IRedmineAttributeChangedListener listener) {
		synchronized (attributeListeners) {
			if(!attributeListeners.contains(listener)) {
				attributeListeners.add(listener);
			}
		}
	}

	public void removeAttributeChangedListener(IRedmineAttributeChangedListener listener) {
		synchronized (attributeListeners) {
			if(attributeListeners.contains(listener)) {
				attributeListeners.remove(listener);
			}
		}
	}
	
	public void notifyAttributeChanged(ITask task, TaskAttribute attribute) {
		Assert.isNotNull(task);
		Assert.isNotNull(attribute);
		Assert.isTrue(task.getConnectorKind().equals(RedmineCorePlugin.REPOSITORY_KIND));
		
		for (IRedmineAttributeChangedListener listener : attributeListeners) {
			listener.attributeChanged(task, attribute);
		}
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		RedmineCorePlugin corePlugin = RedmineCorePlugin.getDefault();
		
		corePlugin.getConnector().setTaskRepositoryLocationFactory(new TaskRepositoryLocationUiFactory());
		TasksUi.getRepositoryManager().addListener(corePlugin.getConnector().getClientManager());
		
		try {
			ISelectionService selServive = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
			selServive.addSelectionListener(selectionListener);
		} catch (NullPointerException e) {}
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		super.stop(context);

		try {
			ISelectionService selServive = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
			selServive.removeSelectionListener(selectionListener);
		} catch(NullPointerException e) {
			//happens on shutdown
		}
	}
	
	public IStructuredSelection getLastSelection() {
		return selection;
	}
}
