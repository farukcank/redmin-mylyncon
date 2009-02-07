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
package org.svenk.redmine.ui.wizard.querypage;

import java.util.Collection;
import java.util.Map;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.svenk.redmine.core.model.IRedmineQueryField;

class RedmineGuiHelper {


	public static void placeListElements(final Composite parent, int columns, final Collection<? extends IRedmineQueryField> queryFields, final Map<? extends IRedmineQueryField, ListViewer> lstSearchValues, final Map<? extends IRedmineQueryField, ComboViewer> lstSearchOperators) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(columns * 2, true);
		composite.setLayout(layout);

		GridData commonGridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
		commonGridData.horizontalAlignment = SWT.FILL;

		GridData listGridData = new GridData();
		listGridData.verticalSpan = 2;
		listGridData.heightHint = 100;
		listGridData.widthHint = 85;
		
		IRedmineQueryField[] fields = 
			queryFields.toArray(new IRedmineQueryField[queryFields.size()]);
		
		for(int i=1; i<=fields.length; i++) {
			IRedmineQueryField queryField = fields[i-1];

			Label label = new Label(composite, SWT.NONE);
			label.setText(queryField.getLabel());
			label.setLayoutData(commonGridData);
			
			ListViewer list = lstSearchValues.get(queryField);
			list.getControl().setParent(composite);
			list.getControl().setLayoutData(listGridData);

			if (i % columns == 0 || i == queryFields.size()) {
				int sv = (i % columns == 0) ? i - columns : i - i % columns;
				if (i % columns != 0) {
					listGridData = new GridData();
					listGridData.verticalSpan = 2;
					listGridData.heightHint = 100;
					listGridData.horizontalSpan = (columns-(i % columns)) * 2 +1;
					listGridData.widthHint = 85;
					list.getControl().setLayoutData(listGridData);
				}
				for (int j = sv; j < i; j++) {
					IRedmineQueryField tmpSearchField = fields[j];
					
					ComboViewer combo = lstSearchOperators.get(tmpSearchField);
					combo.getControl().setParent(composite);
					combo.getControl().setLayoutData(commonGridData);
				}
			}
		}
	}
	
	public static void placeTextElements(final Composite parent, final Collection<? extends IRedmineQueryField> queryFields, final Map<? extends IRedmineQueryField, ? extends Control> txtSearchValues, final Map<? extends IRedmineQueryField, ComboViewer> txtSearchOperators) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		composite.setLayout(layout);
		
		GridData commonGridData = new GridData();
		GridData textGridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
		textGridData.minimumWidth=300;

		for (IRedmineQueryField queryField : queryFields) {
			Label label = new Label(composite, SWT.NONE);
			label.setText(queryField.getLabel());
			label.setLayoutData(commonGridData);

			ComboViewer combo = txtSearchOperators.get(queryField);
			combo.getControl().setParent(composite);
			combo.getControl().setLayoutData(commonGridData);

			Control control = txtSearchValues.get(queryField);
			control.setParent(composite);
			control.setLayoutData(textGridData);
		}
	}
	
}
