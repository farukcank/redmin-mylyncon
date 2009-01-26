package org.svenk.redmine.ui.wizard.querypage;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.svenk.redmine.core.model.RedmineSearchFilter;
import org.svenk.redmine.core.model.RedmineSearchFilter.CompareOperator;
import org.svenk.redmine.core.model.RedmineSearchFilter.SearchField;

class RedmineGuiHelper {

	private static final String OPERATOR_TITLE = "Disabled";

	public static void createListGroup(final Composite parent, int columns, final java.util.List<SearchField> lstSearchFields, final Map<SearchField, List> lstSearchValues, final Map<Combo, SearchField> lstSearchOperators) {

		Composite control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(columns * 2, true);
		control.setLayout(layout);

		GridData commonGridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
		commonGridData.horizontalAlignment = SWT.FILL;

		GridData listGridData = new GridData();
		listGridData.verticalSpan = 2;
		listGridData.heightHint = 100;
		listGridData.widthHint = 85;

		for (int i = 1; i <= lstSearchFields.size(); i++) {
			SearchField searchField = lstSearchFields.get(i - 1);

			Label label = new Label(control, SWT.NONE);
			label.setText(getLabel(searchField));
			label.setLayoutData(commonGridData);

			List list = new List(control, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
			lstSearchValues.put(searchField, list);
			list.setLayoutData(listGridData);
			list.setEnabled(false);

			if (i % columns == 0 || i == lstSearchFields.size()) {
				int sv = (i % columns == 0) ? i - columns : i - i % columns;
				if (i % columns != 0) {
					listGridData = new GridData();
					listGridData.verticalSpan = 2;
					listGridData.heightHint = 100;
					listGridData.horizontalSpan = (columns-(i % columns)) * 2 +1;
					listGridData.widthHint = 85;
					list.setLayoutData(listGridData);
				}
				for (int j = sv; j < i; j++) {
					SearchField tmpSearchField = lstSearchFields.get(j);
					Combo combo = new Combo(control, SWT.READ_ONLY
							| SWT.DROP_DOWN);
					lstSearchOperators.put(combo, tmpSearchField);
					combo.setLayoutData(commonGridData);
					if (!tmpSearchField.isRequired()) {
						combo.add(OPERATOR_TITLE);
					}
					for (RedmineSearchFilter.CompareOperator operator : tmpSearchField.getCompareOperators()) {
						combo.add(operator.toString());
					}
					combo.select(0);
					combo.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							Combo combo = (Combo) e.widget;
							SearchField searchField = lstSearchOperators.get(combo);
							List list = lstSearchValues.get(searchField);
							if (combo.getSelectionIndex() == 0) {
								list.setEnabled(false);
							} else {
								String selected = combo.getItem(combo.getSelectionIndex());
								list.setEnabled(CompareOperator.fromString(selected).useValue());
							}
						}
					});
				}
			}
		}
	}
	
	private static String getLabel(Object object) {
		String label = null;
		if (object instanceof SearchField) {
			return ((SearchField)object).name();
		}
		return label;
	}

	private static java.util.List<CompareOperator> getCompareOperators(Object object) {
		if (object instanceof SearchField) {
			return ((SearchField)object).getCompareOperators();
		}
		return null;
	}
}
