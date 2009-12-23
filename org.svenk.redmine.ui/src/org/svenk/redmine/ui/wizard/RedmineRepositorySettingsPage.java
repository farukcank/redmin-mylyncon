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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.svenk.redmine.core.IRedmineClient;
import org.svenk.redmine.core.RedmineClientFactory;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.RedmineRepositoryConnector;
import org.svenk.redmine.core.exception.RedmineException;

public class RedmineRepositorySettingsPage extends
		AbstractRepositorySettingsPage {

	private static final String EXTENSION_ID_TEXTILE = "org.eclipse.mylyn.wikitext.tasks.ui.editor.textileTaskEditorExtension";
	private static final String EXTENSION_ID_PLAIN = "none";
	
	private static final String TITLE = "Redmine Repository Settings";

	private static final String DESCRIPTION = "Example: www.your-domain.de/redmine";
	
	private static final String WRONG_EXTENSION = "Redmine uses Textile as Markup-Language";
	
	private static final String REPOSITORY_SETTINGS_VALID = "Test of connection was successful";

	private static final String REDMINE_PLUGIN = "Redmine plugin:";

	private static final String REDMINE_PLUGIN_SELECT = "Select installed plugin";

	private static final String REDMINE_PLUGIN_SELECT_ERROR = "You have to select the installed Redmine plugin.";

	private String checkedUrl = null;
	
	private String version = null;
	
	private String clientImplClassName = null;

	public RedmineRepositorySettingsPage(TaskRepository taskRepository) {
		super(TITLE, DESCRIPTION, taskRepository);

		setNeedsAnonymousLogin(false);
		setNeedsEncoding(true);
		setNeedsTimeZone(false);
		setNeedsAdvanced(false);
		setNeedsValidation(true);
		setNeedsHttpAuth(true);
		
		if (taskRepository != null && taskRepository.hasProperty(RedmineClientFactory.CLIENT_IMPLEMENTATION_CLASS)) {
			clientImplClassName = taskRepository.getProperty(RedmineClientFactory.CLIENT_IMPLEMENTATION_CLASS);
		}
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		checkedUrl = getRepositoryUrl();

		//Set Default Encoding
		if (getRepository()==null) {
			setEncoding("UTF-8");
		}
	}
	
	@Override
	public boolean isPageComplete() {
		return super.isPageComplete() && checkedUrl!= null && clientImplClassName!=null && checkedUrl.equals(getRepositoryUrl());
	}

	@Override
	protected void createAdditionalControls(Composite parent) {
		new Label(parent, SWT.NONE).setText(REDMINE_PLUGIN);
		
		ComboViewer clientImplViewer = new ComboViewer(parent, SWT.READ_ONLY);
		clientImplViewer.setLabelProvider(new LabelProvider(){
			@Override
			public String getText(Object element) {
				if (element instanceof IExtension) {
					return ((IExtension)element).getLabel();
				}
				// TODO Auto-generated method stub
				return super.getText(element);
			}
		});
		
		clientImplViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				RedmineRepositorySettingsPage.this.clientImplClassName=null;

				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					Object obj = ((IStructuredSelection)event.getSelection()).getFirstElement();
					if (obj!=null && obj instanceof IExtension) {
						Class<? extends IRedmineClient> clazz = implementationFromExtension((IExtension)obj);
						if (clazz!=null) {
							RedmineRepositorySettingsPage.this.clientImplClassName=clazz.getName();
						}
					}
				}
			}
		});
		
		IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint("org.svenk.redmine.core.clientInterface");
		clientImplViewer.add(REDMINE_PLUGIN_SELECT);
		clientImplViewer.add(extPoint.getExtensions());
		clientImplViewer.getCombo().select(0);
		
		//reselect old setting
		if (clientImplClassName != null) {
			for (int i=extPoint.getExtensions().length-1; i>=0; i--) {
				Class<? extends IRedmineClient> clazz = implementationFromExtension(extPoint.getExtensions()[i]);
				if(clazz!=null && clazz.getName().equals(clientImplClassName)) {
					clientImplViewer.getCombo().select(i+1);
					break;
				}
			}
			if(clientImplViewer.getCombo().getSelectionIndex()==0) {
				clientImplClassName=null;
			}
		}
		
	}

	@Override
	public void applyTo(TaskRepository repository) {
		super.applyTo(repository);
		repository.setProperty(RedmineClientFactory.CLIENT_IMPLEMENTATION_CLASS, clientImplClassName);
	}
	
	@Override
	protected Validator getValidator(final TaskRepository repository) {
		return new Validator() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				if  (RedmineRepositorySettingsPage.this.clientImplClassName==null) {
					throw new CoreException(new Status(IStatus.ERROR, RedmineCorePlugin.PLUGIN_ID, REDMINE_PLUGIN_SELECT_ERROR));
				}
				
				repository.setProperty(RedmineClientFactory.CLIENT_IMPLEMENTATION_CLASS, RedmineRepositorySettingsPage.this.clientImplClassName);
				
				RedmineRepositoryConnector connector = (RedmineRepositoryConnector)TasksUi.getRepositoryManager().getRepositoryConnector(RedmineCorePlugin.REPOSITORY_KIND);
				try {
					IRedmineClient client = connector.getClientManager().getRedmineClient(repository);
					RedmineRepositorySettingsPage.this.version = client.checkClientConnection(monitor);
					repository.setVersion(RedmineRepositorySettingsPage.this.version);
				} catch (RedmineException e) {
					throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
				}
				RedmineRepositorySettingsPage.this.checkedUrl = repository.getRepositoryUrl();
				
				validateEditorExtension(repository);

				this.setStatus(new Status(IStatus.OK, RedmineCorePlugin.PLUGIN_ID, REPOSITORY_SETTINGS_VALID));
			}
			
			@SuppressWarnings("restriction")
			protected void validateEditorExtension(TaskRepository repository) throws CoreException {
				String editorExtension = repository.getProperty(TaskEditorExtensions.REPOSITORY_PROPERTY_EDITOR_EXTENSION);
				if (!(editorExtension==null || editorExtension.equals(EXTENSION_ID_PLAIN) || editorExtension.equals(EXTENSION_ID_TEXTILE))) {
					throw new CoreException(new Status(IStatus.WARNING, RedmineCorePlugin.PLUGIN_ID, WRONG_EXTENSION));
				}
				
			}
		};
	}

	@Override
	protected boolean isValidUrl(String name) {
		if ((name.startsWith(URL_PREFIX_HTTPS) || name.startsWith(URL_PREFIX_HTTP)) && !name.contains("eclipse_mylyn_connector") && !name.endsWith("/")) {
			try {
				new URL(name);
				return true;
			} catch (MalformedURLException e) {
			}
		}
		return false;
	}

	@Override
	public String getVersion() {
		return version;
	}
	
	@Override
	public String getConnectorKind() {
		return RedmineCorePlugin.REPOSITORY_KIND;
	}
	
	@SuppressWarnings("unchecked")
	private static Class<? extends IRedmineClient> implementationFromExtension(IExtension extension) {
		IConfigurationElement[] confElements = extension.getConfigurationElements();
		for (IConfigurationElement confElem : confElements) {
			if ((confElem.getAttribute("class"))!=null) {
				try {
					return (Class<? extends IRedmineClient>)Class.forName(confElem.getAttribute("class"));
				} catch (Exception e) {
					RedmineCorePlugin.getDefault().logUnexpectedException(e);
				}
			}
		}
		return null;
	}
	

}
