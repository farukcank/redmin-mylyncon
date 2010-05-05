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
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.UnsupportedRequestException;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.svenk.redmine.core.IRedmineClient;
import org.svenk.redmine.core.RedmineClientFactory;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.client.container.Version;
import org.svenk.redmine.core.exception.RedmineException;

public class RedmineRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private static final String EXTENSION_ID_TEXTILE = "org.eclipse.mylyn.wikitext.tasks.ui.editor.textileTaskEditorExtension"; //$NON-NLS-1$
	private static final String EXTENSION_ID_PLAIN = "none"; //$NON-NLS-1$
	private static final String EXTENSION_POINT_CLIENT = "org.svenk.redmine.core.clientInterface"; //$NON-NLS-1$
	
	private String checkedUrl;
	
	private String clientImplClassName;
	
	private Version requiredVersion; 

	private String detectedVersionString;

	public RedmineRepositorySettingsPage(TaskRepository taskRepository) {
		super(Messages.RedmineRepositorySettingsPage_PAGE_TITLE, Messages.RedmineRepositorySettingsPage_URL_EXAMPLE, taskRepository);

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
			setEncoding("UTF-8"); //$NON-NLS-1$
		}
	}
	
	@Override
	public boolean isPageComplete() {
		return super.isPageComplete() && checkedUrl!= null && clientImplClassName!=null && detectedVersionString != null && checkedUrl.equals(getRepositoryUrl());
	}

	@Override
	protected void createAdditionalControls(Composite parent) {
		new Label(parent, SWT.NONE).setText(Messages.RedmineRepositorySettingsPage_CLIENT_IMPL_TITLE);
		
		ComboViewer clientImplViewer = new ComboViewer(parent, SWT.READ_ONLY);
		clientImplViewer.setLabelProvider(new LabelProvider(){
			@Override
			public String getText(Object element) {
				if (element instanceof IExtension) {
					return ((IExtension)element).getLabel();
				}
				return super.getText(element);
			}
		});
		
		clientImplViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				clientImplClassName=null;
				if (getWizard() != null) {
					getWizard().getContainer().updateButtons();
				}

				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					Object obj = ((IStructuredSelection)event.getSelection()).getFirstElement();
					if (obj!=null && obj instanceof IExtension) {
						IExtension extension = (IExtension)obj;
						Class<? extends IRedmineClient> clazz = implementationFromExtension(extension);
						if (clazz!=null) {
							clientImplClassName=clazz.getName();
						}
						requiredVersion = versionFromExtension(extension);
					}
				}
			}
		});
		
		IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_CLIENT);
		clientImplViewer.add(Messages.RedmineRepositorySettingsPage_CLIENT_IMPL_DO_SELECT);
		clientImplViewer.add(extPoint.getExtensions());
		clientImplViewer.getCombo().select(0);
		
		//reselect old setting
		if (clientImplClassName != null) {
			for (int i=extPoint.getExtensions().length-1; i>=0; i--) {
				IExtension extension = extPoint.getExtensions()[i];
				Class<? extends IRedmineClient> clazz = implementationFromExtension(extension);
				if(clazz!=null && clazz.getName().equals(clientImplClassName)) {
					clientImplViewer.getCombo().select(i+1);
					requiredVersion = versionFromExtension(extension);
					break;
				}
			}
			if(clientImplViewer.getCombo().getSelectionIndex()==0) {
				clientImplClassName=null;
			}
		} else if (extPoint.getExtensions().length==1) {
			//select first implementation, if only this one exists
			IExtension extension = extPoint.getExtensions()[0];
			Class<? extends IRedmineClient> clazz = implementationFromExtension(extension);
			if(clazz!=null) {
				clientImplClassName = clazz.getName();
				clientImplViewer.getCombo().select(1);
				requiredVersion = versionFromExtension(extension);
			}
		}
		
	}

	@Override
	public void applyTo(TaskRepository repository) {
		super.applyTo(repository);
		repository.setProperty(RedmineClientFactory.CLIENT_IMPLEMENTATION_CLASS, clientImplClassName);
		repository.setVersion(detectedVersionString);
	}
	
	@Override
	protected Validator getValidator(final TaskRepository repository) {
		return new Validator() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				detectedVersionString = null;
				if  (clientImplClassName==null) {
					throw new CoreException(new Status(IStatus.ERROR, RedmineCorePlugin.PLUGIN_ID, Messages.RedmineRepositorySettingsPage_CLIENT_IMPL_NOT_SELECTED));
				}
				
				repository.setProperty(RedmineClientFactory.CLIENT_IMPLEMENTATION_CLASS, RedmineRepositorySettingsPage.this.clientImplClassName);
				Version detectedVersion = null;
				try {
					IRedmineClient client = RedmineClientFactory.createClient(repository, null);
					detectedVersion = client.checkClientConnection(monitor);
				} catch (RedmineException e) {
					if(e.getCause() instanceof UnsupportedRequestException) {
						throw new CoreException(new Status(IStatus.ERROR, RedmineCorePlugin.PLUGIN_ID, Messages.RedmineRepositorySettingsPage_INVALID_CREDENTIALS));
					}
					throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
				}
				checkedUrl = repository.getRepositoryUrl();
				
				validateVersion(requiredVersion, detectedVersion);
				validateEditorExtension(repository);

				detectedVersionString = detectedVersion.toString();

				String msg = Messages.RedmineRepositorySettingsPage_MESSAGE_SUCCESS;
				msg = String.format(msg, detectedVersion.redmine.toString(), detectedVersion.plugin.toString());
				this.setStatus(new Status(IStatus.OK, RedmineCorePlugin.PLUGIN_ID, msg));
			}
			
			@SuppressWarnings("restriction")
			protected void validateEditorExtension(TaskRepository repository) throws CoreException {
				String editorExtension = repository.getProperty(TaskEditorExtensions.REPOSITORY_PROPERTY_EDITOR_EXTENSION);
				if (!(editorExtension==null || editorExtension.equals(EXTENSION_ID_PLAIN) || editorExtension.equals(EXTENSION_ID_TEXTILE))) {
					throw new CoreException(new Status(IStatus.WARNING, RedmineCorePlugin.PLUGIN_ID, Messages.RedmineRepositorySettingsPage_MESSAGE_WIKI_WARNING));
				}
			}
			
			protected void validateVersion(Version required, Version detected) throws CoreException {
				if (detected==null || detected.redmine==null || detected.plugin==null) {
					throw new CoreException(new Status(IStatus.ERROR, RedmineCorePlugin.PLUGIN_ID, Messages.RedmineRepositorySettingsPage_MESSAGE_VERSION_UNKNOWN_ERROR));
				} else if (detected.redmine.compareTo(required.redmine)<0 || detected.plugin.compareTo(required.plugin)<0) {
					String msg = Messages.RedmineRepositorySettingsPage_MESSAGE_VERSION_OUTDATED_ERROR;
					msg = String.format(msg, required.redmine.toString(), required.plugin.toString(), detected.redmine.toString(), detected.plugin.toString());
					throw new CoreException(new Status(IStatus.ERROR, RedmineCorePlugin.PLUGIN_ID, msg));
				}
			}
		};
	}

	@Override
	protected boolean isValidUrl(String name) {
		if ((name.startsWith(URL_PREFIX_HTTPS) || name.startsWith(URL_PREFIX_HTTP)) && !name.endsWith("/")) { //$NON-NLS-1$
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
		return detectedVersionString;
	}
	
	@Override
	public String getConnectorKind() {
		return RedmineCorePlugin.REPOSITORY_KIND;
	}
	
	@SuppressWarnings("unchecked")
	private static Class<? extends IRedmineClient> implementationFromExtension(IExtension extension) {
		IConfigurationElement confElem = extension.getConfigurationElements()[0];
		if ((confElem.getAttribute("class"))!=null) { //$NON-NLS-1$
			try {
				return (Class<? extends IRedmineClient>)Class.forName(confElem.getAttribute("class")); //$NON-NLS-1$
			} catch (Exception e) {
				RedmineCorePlugin.getDefault().logUnexpectedException(e);
			}
		}
		return null;
	}

	private static Version versionFromExtension(IExtension extension) {
		Version v = new Version();
		try {
			IConfigurationElement confElem = extension.getConfigurationElements()[0];
			v.redmine = Version.Redmine.fromString(confElem.getAttribute("vRedmine")); //$NON-NLS-1$
			v.plugin = Version.Plugin.fromString(confElem.getAttribute("vPlugin")); //$NON-NLS-1$
		} catch (Exception e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null);
			StatusHandler.log(status);
			v = null;
		}
		return v;
	}
	

}
