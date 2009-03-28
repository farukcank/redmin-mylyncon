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
package org.svenk.redmine.ui.editor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.RedmineRepositoryConnector;
import org.svenk.redmine.core.util.RedmineTaskDataValidator;
import org.svenk.redmine.core.util.RedmineTaskDataValidator.RedmineTaskDataValidatorResult;


public class RedmineTaskEditorPage extends AbstractTaskEditorPage {

	private final static String REQUIRED_MESSAGE_SUMMARY = "Please enter a subject before  submitting";
	private final static String REQUIRED_MESSAGE_DESCRIPTION = "Please enter a description before submitting";
	
	private RedmineTaskDataValidator validator;
	
	public RedmineTaskEditorPage(TaskEditor editor) {
		super(editor, RedmineCorePlugin.REPOSITORY_KIND);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		
		TaskRepository repository = getTaskRepository();
		AbstractRepositoryConnector connector = TasksUi.getRepositoryManager().getRepositoryConnector(repository.getConnectorKind());
		if (connector instanceof RedmineRepositoryConnector) {
			validator = ((RedmineRepositoryConnector)connector).createNewTaskDataValidator(repository);

			getModel().addModelListener(new TaskDataModelListener() {
				@Override
				public void attributeChanged(TaskDataModelEvent event) {
					RedmineTaskDataValidatorResult result = validator.validateTaskAttribute(getModel().getTaskData(), event.getTaskAttribute());
					if(result.hasErrors()) {
						getTaskEditor().setMessage(result.getFirstErrorMessage(), IMessageProvider.WARNING);
					} else {
						getTaskEditor().setMessage("", IMessageProvider.NONE);
					}
				}
			});
		}
	}
	
	//WORKARAOUND Zugriff auf nicht initialisierte Page
	//TODO Ticket erstellen
	@Override
	public IManagedForm getManagedForm() {
		IManagedForm form = super.getManagedForm();
		if (form==null) {
			FormEditor editor = getTaskEditor();
			if (editor!= null && !isActive()) {
				editor.setActivePage(getId());
				form = super.getManagedForm();
			}
		}
		return form;
	}
	
	@Override
	public void doSubmit() {
		TaskAttribute attribute = getModel().getTaskData().getRoot().getMappedAttribute(RedmineAttribute.SUMMARY.getRedmineKey());
		if (attribute != null && attribute.getValue().trim().length() == 0) {
			getTaskEditor().setMessage(REQUIRED_MESSAGE_SUMMARY, IMessageProvider.ERROR);
			AbstractTaskEditorPart part = getPart(ID_PART_SUMMARY);
			if (part != null) {
				part.setFocus();
			}
			return;
		}

		attribute = getModel().getTaskData().getRoot().getMappedAttribute(RedmineAttribute.DESCRIPTION.getRedmineKey());
		if (attribute != null && attribute.getValue().trim().length() == 0) {
			getTaskEditor().setMessage(REQUIRED_MESSAGE_DESCRIPTION, IMessageProvider.ERROR);
			AbstractTaskEditorPart part = getPart(ID_PART_DESCRIPTION);
			if (part != null) {
				part.setFocus();
			}
			return;
		}

		RedmineTaskDataValidatorResult result = validator.validateTaskData(getModel().getTaskData());
		if (result.hasErrors()) {
			getTaskEditor().setMessage(result.getFirstErrorMessage(), IMessageProvider.ERROR);
			return;
		}

		getTaskEditor().setMessage("", IMessageProvider.NONE);
		super.doSubmit();
	}
}
