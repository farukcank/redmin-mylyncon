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
import java.util.Set;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.svenk.redmine.core.IRedmineConstants;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.RedmineRepositoryConnector;
import org.svenk.redmine.core.util.RedmineTaskDataValidator;
import org.svenk.redmine.core.util.RedmineTaskDataValidator.RedmineTaskDataValidatorResult;
import org.svenk.redmine.ui.RedmineUiPlugin;
import org.svenk.redmine.ui.internal.IRedmineAttributeChangedListener;


public class RedmineTaskEditorPage extends AbstractTaskEditorPage {

	private final static String REQUIRED_MESSAGE_SUMMARY = "Please enter a subject before  submitting";
	private final static String REQUIRED_MESSAGE_DESCRIPTION = "Please enter a description before submitting";
	
	private final static String TASK_EDITOR_PART_PLANNING = "org.svenk.redmine.ui.editor.part.planning";
	private final static String TASK_EDITOR_PART_TIMEENTRIES = "org.svenk.redmine.ui.editor.part.timeentries";
	private final static String TASK_EDITOR_PART_NEWTIMEENTRY = "org.svenk.redmine.ui.editor.part.newtimeentry";
	
//	private final Map<TaskAttribute, AbstractAttributeEditor> attributeEditorMap;
	
	private final IRedmineAttributeChangedListener STATUS_LISTENER;

	private RedmineTaskDataValidator validator;
	
	public RedmineTaskEditorPage(TaskEditor editor) {
		super(editor, RedmineCorePlugin.REPOSITORY_KIND);

//		this.attributeEditorMap = new HashMap<TaskAttribute, AbstractAttributeEditor>();
		
		setNeedsPrivateSection(true);
		setNeedsSubmitButton(true);
		
		STATUS_LISTENER = new IRedmineAttributeChangedListener() {
			public void attributeChanged(ITask task, TaskAttribute attribute) {
				if(getTask()==task) {
					if(attribute.getId().equals(RedmineAttribute.STATUS.getRedmineKey())) {
						TaskDataModel model = getModel();
						TaskAttribute modelAttribute = model.getTaskData().getRoot().getAttribute(attribute.getId());
						
						if(!modelAttribute.getValue().equals(attribute.getValue())) {
							modelAttribute.setValue(attribute.getValue());
							model.attributeChanged(modelAttribute);
						}
					}
				}
			}
		};
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

		RedmineUiPlugin.getDefault().addAttributeChangedListener(STATUS_LISTENER);
	}
	
	@Override
	public void close() {
		super.close();
		RedmineUiPlugin.getDefault().removeAttributeChangedListener(STATUS_LISTENER);
	}

	@Override
	protected Set<TaskEditorPartDescriptor> createPartDescriptors() {
		Set<TaskEditorPartDescriptor> descriptors = super.createPartDescriptors();

		TaskAttribute rootAttribute = getModel().getTaskData().getRoot();
		
		descriptors.add(new TaskEditorPartDescriptor(TASK_EDITOR_PART_PLANNING) {
			@Override
			public AbstractTaskEditorPart createPart() {
				return new RedminePlanningEditorPart();
			}
		}.setPath(PATH_ATTRIBUTES));

		if (rootAttribute.getAttribute(RedmineAttribute.TIME_ENTRY_TOTAL.getRedmineKey())!=null) {
			//TODO change ID
			descriptors.add(new TaskEditorPartDescriptor(TASK_EDITOR_PART_TIMEENTRIES) {
				@Override
				public AbstractTaskEditorPart createPart() {
					return new RedmineTimeEntryEditorPart();
				}
			}.setPath(PATH_COMMENTS));
		}

		if (!getModel().getTask().isCompleted() && rootAttribute.getAttribute(RedmineAttribute.TIME_ENTRY_HOURS.getRedmineKey())!=null) {
			//TODO change ID
			descriptors.add(new TaskEditorPartDescriptor(TASK_EDITOR_PART_NEWTIMEENTRY) {
				@Override
				public AbstractTaskEditorPart createPart() {
					return new RedmineNewTimeEntryEditorPart();
				}
			}.setPath(PATH_COMMENTS));
		}
		
		return descriptors;
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
	protected AttributeEditorFactory createAttributeEditorFactory() {
		AttributeEditorFactory factory = new AttributeEditorFactory(getModel(), getTaskRepository(), getEditorSite()) {
			@Override
			public AbstractAttributeEditor createEditor(String type, final TaskAttribute taskAttribute) {
				AbstractAttributeEditor editor;
				if (IRedmineConstants.EDITOR_TYPE_ESTIMATED.equals(type)) {
					editor = new RedmineEstimatedEditor(getModel(), taskAttribute);
				} else {
					editor = super.createEditor(type, taskAttribute);
					if (TaskAttribute.TYPE_BOOLEAN.equals(type)) {
						editor.setDecorationEnabled(false);
					}
				}

//				RedmineTaskEditorPage.this.addToAttributeEditorMap(taskAttribute, editor);
				return editor;
			}
		};
		return factory;
	}

//	@Override
//	protected void createParts() {
//		attributeEditorMap.clear();
//		super.createParts();
//	}
//
//	private void addToAttributeEditorMap(TaskAttribute attribute, AbstractAttributeEditor editor) {
//		if (attributeEditorMap.containsKey(attribute)) {
//			attributeEditorMap.remove(attribute);
//		}
//		attributeEditorMap.put(attribute, editor);
//	}
//
//	private AbstractAttributeEditor getEditorForAttribute(TaskAttribute attribute) {
//		return attributeEditorMap.get(attribute);
//	}
//
//	private void refresh(TaskAttribute attributeComponent) {
//		AbstractAttributeEditor editor = getEditorForAttribute(attributeComponent);
//		if (editor != null) {
//			try {
//				editor.refresh();
//			} catch (UnsupportedOperationException e) {
//				// ignore
//			}
//		}
//	}

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
