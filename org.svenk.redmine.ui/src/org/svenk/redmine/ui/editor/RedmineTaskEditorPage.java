/*******************************************************************************
 *
 * Redmine-Mylyn-Connector
 * 
 * This implementation is on the basis of the implementations of Trac and 
 * Bugzilla emerged and contains parts of source code from these projects.
 * The corresponding copyright notice follows below of this.
 * Copyright (C) 2008  Sven Krzyzak and others
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *  
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.svenk.redmine.ui.editor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.svenk.redmine.core.RedmineAttribute;
import org.svenk.redmine.core.RedmineCorePlugin;


public class RedmineTaskEditorPage extends AbstractTaskEditorPage {

	private final static String REQUIRED_MESSAGE_SUMMARY = "Please enter a subject before  submitting";
	private final static String REQUIRED_MESSAGE_DESCRIPTION = "Please enter a description before submitting";
	
	public RedmineTaskEditorPage(TaskEditor editor) {
		super(editor, RedmineCorePlugin.REPOSITORY_KIND);
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
		
		super.doSubmit();
	}
}
