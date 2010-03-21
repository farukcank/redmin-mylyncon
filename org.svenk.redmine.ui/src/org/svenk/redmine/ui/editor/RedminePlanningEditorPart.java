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
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorToolkit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.util.RedmineUtil;

/**
 * @author Rob Elves
 */
public class RedminePlanningEditorPart extends AbstractTaskEditorPart {

	private static final Set<RedmineAttribute> PLANNING_ATTRIBUTES = EnumSet.of(RedmineAttribute.DATE_START, RedmineAttribute.DATE_DUE,RedmineAttribute.ESTIMATED);

	private boolean hasIncoming;

	private TaskDataModelListener modelListener;
	
	public RedminePlanningEditorPart() {
		super();
		setPartName(Messages.RedminePlanningEditorPart_PLANNING_SECTION_TITLE);
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

		AttributeEditorToolkit editorToolkit = getTaskEditorPage().getAttributeEditorToolkit();
		TaskAttribute rootAttribute = getTaskData().getRoot();
		AbstractAttributeEditor attributeEditor;
		TaskAttribute attribute;

		attribute = rootAttribute.getAttribute(RedmineAttribute.DATE_START.getTaskKey());
		if (attribute != null) {
			attributeEditor = createAttributeEditor(attribute);
			attributeEditor.createLabelControl(timeComposite, toolkit);
			attributeEditor.createControl(timeComposite, toolkit);
			editorToolkit.adapt(attributeEditor);
		}

		attribute = rootAttribute.getAttribute(RedmineAttribute.DATE_DUE.getTaskKey());
		if (attribute != null) {
			attributeEditor = createAttributeEditor(attribute);
			attributeEditor.createLabelControl(timeComposite, toolkit);
			attributeEditor.createControl(timeComposite, toolkit);
			editorToolkit.adapt(attributeEditor);
		}

		attribute = rootAttribute.getAttribute(RedmineAttribute.ESTIMATED.getTaskKey());
		if (attribute != null) {
			attributeEditor = createAttributeEditor(attribute);
			attributeEditor.createLabelControl(timeComposite, toolkit);
			attributeEditor.createControl(timeComposite, toolkit);
			editorToolkit.adapt(attributeEditor);
		}

		toolkit.paintBordersFor(timeComposite);
		timeSection.setClient(timeComposite);
		setSection(toolkit, timeSection);

	}

	@Override
	public void commit(boolean onSave) {
		ITask task = getTask();
		Assert.isNotNull(task);

		TaskAttribute rootAttribute = getTaskData().getRoot();
		TaskAttribute attribute = null;

		attribute = rootAttribute.getAttribute(RedmineAttribute.DATE_DUE.getTaskKey());
		if(getModel().getChangedAttributes().contains(attribute)) {
			String dueValue = attribute.getValue();

			if (dueValue.equals("")) { //$NON-NLS-1$
				task.setDueDate(null);
			} else {
				task.setDueDate(RedmineUtil.parseDate(dueValue));
			}
		}

		super.commit(onSave);
	}

	private void initialize() {
		hasIncoming = false;

		modelListener = new TaskDataModelListener() {
			@Override
			public void attributeChanged(TaskDataModelEvent event) {
				if (RedmineAttribute.DATE_DUE.getTaskKey().equals(event.getTaskAttribute().getId())) {						
					RedminePlanningEditorPart.this.markDirty();
				}
			}
		};
		getModel().addModelListener(modelListener);

		TaskAttribute rootAttribute = getTaskData().getRoot();
		for (RedmineAttribute redmineAttribute : PLANNING_ATTRIBUTES) {
			TaskAttribute attribute = rootAttribute.getAttribute(redmineAttribute.getTaskKey());
			if (attribute != null && getModel().hasIncomingChanges(attribute)) {
				hasIncoming = true;
				break;
			}
		}
	}

	@Override
	public void dispose() {
		getModel().removeModelListener(modelListener);
		super.dispose();
	}

	private ITask getTask() {
		return getTaskEditorPage().getTask();
	}
}
