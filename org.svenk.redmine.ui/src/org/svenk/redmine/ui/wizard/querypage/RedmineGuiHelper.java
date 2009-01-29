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

import java.util.Map;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.svenk.redmine.core.model.RedmineSearchFilter.SearchField;

class RedmineGuiHelper {


	public static void placeElements(final Composite parent, int columns, final java.util.List<SearchField> searchFields, final Map<SearchField, ListViewer> lstSearchValues, final Map<SearchField, ComboViewer> lstSearchOperators) {

		Composite control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(columns * 2, true);
		control.setLayout(layout);

		GridData commonGridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
		commonGridData.horizontalAlignment = SWT.FILL;

		GridData listGridData = new GridData();
		listGridData.verticalSpan = 2;
		listGridData.heightHint = 100;
		listGridData.widthHint = 85;
		
		for(int i=1; i<=searchFields.size(); i++) {
			SearchField searchField = searchFields.get(i-1);

			Label label = new Label(control, SWT.NONE);
			label.setText(searchField.name());
			label.setLayoutData(commonGridData);
			
			ListViewer list = lstSearchValues.get(searchField);
			list.getControl().setParent(control);
			list.getControl().setLayoutData(listGridData);

			if (i % columns == 0 || i == searchFields.size()) {
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
					SearchField tmpSearchField = searchFields.get(j);
					
					ComboViewer combo = lstSearchOperators.get(tmpSearchField);
					combo.getControl().setParent(control);
					combo.getControl().setLayoutData(commonGridData);
				}
			}
		}
	}
	
}
