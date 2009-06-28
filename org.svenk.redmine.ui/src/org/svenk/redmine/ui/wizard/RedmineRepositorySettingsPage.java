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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Composite;
import org.svenk.redmine.core.IRedmineClient;
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

	private String checkedUrl = null;
	
	private String version = null;

	public RedmineRepositorySettingsPage(TaskRepository taskRepository) {
		super(TITLE, DESCRIPTION, taskRepository);

		setNeedsAnonymousLogin(false);
		setNeedsEncoding(true);
		setNeedsTimeZone(false);
		setNeedsAdvanced(false);
		setNeedsValidation(true);
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
		return super.isPageComplete() && checkedUrl!= null && checkedUrl.equals(getRepositoryUrl());
	}

	@Override
	protected void createAdditionalControls(Composite parent) {
		// TODO Auto-generated method stub
	}

	@Override
	protected Validator getValidator(final TaskRepository repository) {
		return new Validator() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				RedmineRepositoryConnector connector = (RedmineRepositoryConnector)TasksUi.getRepositoryManager().getRepositoryConnector(RedmineCorePlugin.REPOSITORY_KIND);
				IRedmineClient client = connector.getClientManager().getRedmineClient(repository);
				try {
					RedmineRepositorySettingsPage.this.version = client.checkClientConnection();
					repository.setVersion(RedmineRepositorySettingsPage.this.version);
				} catch (RedmineException e) {
					throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
				}
				RedmineRepositorySettingsPage.this.checkedUrl = repository.getRepositoryUrl();
				
				validateEditorExtension(repository);
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
	
	

}
