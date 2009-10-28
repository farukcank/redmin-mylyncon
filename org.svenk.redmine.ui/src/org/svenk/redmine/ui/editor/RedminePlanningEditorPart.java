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

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskActivityManager;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.util.RedmineUtil;

/**
 * @author Rob Elves
 */
public class RedminePlanningEditorPart extends AbstractTaskEditorPart {

	private static final Set<RedmineAttribute> PLANNING_ATTRIBUTES = EnumSet
			.of(RedmineAttribute.DATE_START, RedmineAttribute.DATE_DUE,
					RedmineAttribute.ESTIMATED);

	private boolean hasIncoming;

	private AbstractAttributeEditor dueDateEditor;
	private TaskAttribute dueDateAttribute;

	private TaskDataModelListener modelListener;
	
	public RedminePlanningEditorPart() {
		super();
		setPartName("Planning");
	}

	@Override
	public void initialize(AbstractTaskEditorPage taskEditorPage) {
		super.initialize(taskEditorPage);
		
		if (modelListener==null) {
			modelListener = new TaskDataModelListener() {
				@Override
				public void attributeChanged(TaskDataModelEvent event) {
					if (event.getTaskAttribute().getId().equals(RedmineAttribute.DATE_DUE.getRedmineKey())) {
						RedminePlanningEditorPart.this.markDirty();
					}
				}
			};
			
			getModel().addModelListener(modelListener);
		}
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		initialize();
		Section timeSection = createSection(parent, toolkit, hasIncoming);

		GridLayout gl = new GridLayout();
		GridData gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.horizontalSpan = 4;
		timeSection.setLayout(gl);
		timeSection.setLayoutData(gd);

		Composite timeComposite = toolkit.createComposite(timeSection);
		gl = new GridLayout(6, false);
		timeComposite.setLayout(gl);
		gd = new GridData();
		gd.horizontalSpan = 4;
		timeComposite.setLayoutData(gd);

		AbstractAttributeEditor attributeEditor;
		TaskAttribute attribute;

		attribute = getTaskData().getRoot().getAttribute(
				RedmineAttribute.DATE_START.getRedmineKey());
		if (attribute != null) {
			attributeEditor = createAttributeEditor(attribute);
			attributeEditor.createLabelControl(timeComposite, toolkit);
			attributeEditor.createControl(timeComposite, toolkit);
			getTaskEditorPage().getAttributeEditorToolkit().adapt(
					attributeEditor);
		}

		attribute = getTaskData().getRoot().getAttribute(
				RedmineAttribute.DATE_DUE.getRedmineKey());
		if (attribute != null) {
			attributeEditor = createAttributeEditor(attribute);
			attributeEditor.createLabelControl(timeComposite, toolkit);
			attributeEditor.createControl(timeComposite, toolkit);
			getTaskEditorPage().getAttributeEditorToolkit().adapt(
					attributeEditor);
			dueDateEditor = attributeEditor;
			dueDateAttribute = attribute;
		}

		attribute = getTaskData().getRoot().getAttribute(
				RedmineAttribute.ESTIMATED.getRedmineKey());
		if (attribute != null) {
			attributeEditor = createAttributeEditor(attribute);
			attributeEditor.createLabelControl(timeComposite, toolkit);
			attributeEditor.createControl(timeComposite, toolkit);
			getTaskEditorPage().getAttributeEditorToolkit().adapt(
					attributeEditor);
		}

		toolkit.paintBordersFor(timeComposite);
		timeSection.setClient(timeComposite);
		setSection(toolkit, timeSection);

	}

	@Override
	public void commit(boolean onSave) {
		Assert.isNotNull(getTask());

		if (dueDateAttribute != null && getModel().getChangedAttributes().contains(dueDateAttribute)) {
			String dueValue = dueDateAttribute.getValue();
			ITask task = getTask();
			
			if (dueValue == null) {
				task.setDueDate(null);

				ITaskActivityManager manager = TasksUi.getTaskActivityManager();
				if(manager instanceof TaskActivityManager) {
					((TaskActivityManager)manager).removeDueTask(task);
				}
			} else {
				task.setDueDate(RedmineUtil.parseDate(dueValue));
				
				ITaskActivityManager manager = TasksUi.getTaskActivityManager();
				if(manager instanceof TaskActivityManager) {
					((TaskActivityManager)manager).addDueTask(task);
				}
			}
			
		}
		super.commit(onSave);
	}

	private void initialize() {
		hasIncoming = false;

		TaskAttribute rootAttribute = getTaskData().getRoot();
		for (RedmineAttribute redmineAttribute : PLANNING_ATTRIBUTES) {
			TaskAttribute attribute = rootAttribute
					.getAttribute(redmineAttribute.getRedmineKey());
			if (attribute != null && getModel().hasIncomingChanges(attribute)) {
				hasIncoming = true;
				break;
			}
		}
	}

	private ITask getTask() {
		return getTaskEditorPage().getTask();
	}
}
