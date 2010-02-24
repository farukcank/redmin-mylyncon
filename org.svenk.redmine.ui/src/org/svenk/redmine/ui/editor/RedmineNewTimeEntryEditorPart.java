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

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.svenk.redmine.core.IRedmineConstants;
import org.svenk.redmine.core.RedmineAttribute;

public class RedmineNewTimeEntryEditorPart extends AbstractTaskEditorPart {

	private Section section;

	private TaskDataModelListener modelListener;
	
	public RedmineNewTimeEntryEditorPart() {
		super();
		setPartName("New Time Entry");
		setExpandVertically(true);
	}

	private List<String> attributeList = new ArrayList<String>(3);
	
	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		initialize();
		
		section = createSection(parent, toolkit, ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
		setSection(toolkit, section);
		

		GridLayout gl = new GridLayout();
		GridData gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.horizontalSpan = 4;
		section.setLayout(gl);
		section.setLayoutData(gd);

		Composite composite = toolkit.createComposite(section);
		composite.setLayout(new GridLayout(4, false));
		gd = new GridData();
		gd.horizontalSpan = 4;
		composite.setLayoutData(gd);

		AttributeEditorToolkit editorToolkit = getTaskEditorPage().getAttributeEditorToolkit();
		TaskAttribute root = getTaskData().getRoot();
		AbstractAttributeEditor attributeEditor;
		TaskAttribute attribute;

		attribute = root.getAttribute(RedmineAttribute.TIME_ENTRY_HOURS.getRedmineKey());
		if (attribute != null) {
			attributeList.add(attribute.getId());
			attributeEditor = createAttributeEditor(attribute);
			attributeEditor.createLabelControl(composite, toolkit);
			attributeEditor.createControl(composite, toolkit);
			attributeEditor.setDecorationEnabled(false);
			editorToolkit.adapt(attributeEditor);
		}

		attribute = root.getAttribute(RedmineAttribute.TIME_ENTRY_ACTIVITY.getRedmineKey());
		if (attribute != null) {
			attributeList.add(attribute.getId());
			attributeEditor = createAttributeEditor(attribute);
			attributeEditor.createLabelControl(composite, toolkit);
			attributeEditor.createControl(composite, toolkit);
			attributeEditor.setDecorationEnabled(false);
			editorToolkit.adapt(attributeEditor);
		}
		
		attribute = root.getAttribute(RedmineAttribute.TIME_ENTRY_COMMENTS.getRedmineKey());
		if (attribute != null) {
			attributeList.add(attribute.getId());
			attributeEditor = createAttributeEditor(attribute);
			attributeEditor.createLabelControl(composite, toolkit);
			attributeEditor.createControl(composite, toolkit);
			attributeEditor.setDecorationEnabled(false);
			editorToolkit.adapt(attributeEditor);
			
			gd = new GridData();
			gd.horizontalSpan = 3;
			gd.horizontalAlignment = SWT.FILL;
			gd.verticalAlignment = SWT.FILL;
			gd.heightHint = 40;
			attributeEditor.getControl().setLayoutData(gd);
		}
		
		for (TaskAttribute childAttribute : root.getAttributes().values()) {
			if(childAttribute.getId().startsWith(IRedmineConstants.TASK_KEY_PREFIX_TIMEENTRY_CF)) {
				attributeList.add(childAttribute.getId());
				attributeEditor = createAttributeEditor(childAttribute);
				attributeEditor.createLabelControl(composite, toolkit);
				attributeEditor.createControl(composite, toolkit);
				attributeEditor.setDecorationEnabled(false);
				editorToolkit.adapt(attributeEditor);
			}
		}
		
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
		setSection(toolkit, section);
	}

	private void initialize() {
		if (modelListener==null) {
			modelListener = new TaskDataModelListener() {
				@Override
				public void attributeChanged(TaskDataModelEvent event) {
					if(attributeList.contains(event.getTaskAttribute().getId())) {
						markDirty();
					}
				}
			};
			getModel().addModelListener(modelListener);
		}

	}
	
	@Override
	public void dispose() {
		if (modelListener!=null) {
			getModel().removeModelListener(modelListener);
			modelListener = null;
		}
		
		super.dispose();
	}
	
}
