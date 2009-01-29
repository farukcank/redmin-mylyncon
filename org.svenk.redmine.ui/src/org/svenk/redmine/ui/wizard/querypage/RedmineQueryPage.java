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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.svenk.redmine.core.IRedmineClient;
import org.svenk.redmine.core.RedmineClientData;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.RedmineProjectData;
import org.svenk.redmine.core.RedmineRepositoryConnector;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.model.RedmineCustomTicketField;
import org.svenk.redmine.core.model.RedmineSearch;
import org.svenk.redmine.core.model.RedmineSearchFilter;
import org.svenk.redmine.core.model.RedmineStoredQuery;
import org.svenk.redmine.core.model.RedmineTicketAttribute;
import org.svenk.redmine.core.model.RedmineSearchFilter.CompareOperator;
import org.svenk.redmine.core.model.RedmineSearchFilter.SearchField;

public class RedmineQueryPage extends AbstractRepositoryQueryPage {

	private static final String TITLE = "Enter query parameters";

	private static final String DESCRIPTION = "Only predefined filters are supported.";

	private static final String TITLE_QUERY_TITLE = "Query Title:";

	private static final String PROJECT_SELECT_TITLE = "Select Project";

	private static final String QUERY_SELECT_TITLE = "Select a serverside stored query or create a new";

	private static final String OPERATOR_TITLE = "Disabled";

	private IRepositoryQuery query;

	private Text titleText;

	private IRedmineClient client;

	protected Composite pageComposite;
	protected ScrolledComposite pageScroll;
	protected GridData pageLayoutData;
	protected Composite settingsComposite;
	
	protected Composite customFilterComposite;
	
	protected ComboViewer projectViewer;
	protected RedmineProjectData projectData;

	protected ComboViewer storedQueryViewer;

	protected ArrayList<SearchField> lstSearchFields;
	protected Map<SearchField, ComboViewer> lstSearchOperators;
	protected Map<SearchField, ListViewer> lstSearchValues;

	protected ArrayList<SearchField> txtSearchFields;
	protected Map<Combo, SearchField> txtSearchOperators;
	protected Map<SearchField, Text> txtSearchValues;

	protected Map<RedmineCustomTicketField, List> lstCustomSearchValues;

	protected Button updateButton;
	protected RedmineClientData data;

	public RedmineQueryPage(TaskRepository repository, IRepositoryQuery query) {
		super(TITLE, repository, query);

		this.query=query;
		
		RedmineRepositoryConnector connector = (RedmineRepositoryConnector) TasksUi.getRepositoryManager().getRepositoryConnector(RedmineCorePlugin.REPOSITORY_KIND);
		client = connector.getClientManager().getRedmineClient(getTaskRepository());

		setTitle(TITLE);
		setDescription(DESCRIPTION);

		lstSearchFields = new ArrayList<SearchField>(6);
		lstSearchFields.add(SearchField.STATUS);
		lstSearchFields.add(SearchField.PRIORITY);
		lstSearchFields.add(SearchField.TRACKER);
		lstSearchFields.add(SearchField.FIXED_VERSION);
		lstSearchFields.add(SearchField.ASSIGNED_TO);
		lstSearchFields.add(SearchField.AUTHOR);
		lstSearchFields.add(SearchField.CATEGORY);

		lstSearchOperators = new HashMap<SearchField, ComboViewer>(lstSearchFields.size());
		lstSearchValues = new HashMap<SearchField, ListViewer>(lstSearchFields.size());

		txtSearchFields = new ArrayList<SearchField>(6);
		txtSearchFields.add(SearchField.SUBJECT);
		txtSearchFields.add(SearchField.DATE_CREATED);
		txtSearchFields.add(SearchField.DATE_UPDATED);
		txtSearchFields.add(SearchField.DATE_START);
		txtSearchFields.add(SearchField.DATE_DUE);
		txtSearchFields.add(SearchField.DONE_RATIO);

		txtSearchOperators = new HashMap<Combo, SearchField>(txtSearchFields
				.size());
		txtSearchValues = new HashMap<SearchField, Text>(txtSearchFields.size());
		
		lstCustomSearchValues = new HashMap<RedmineCustomTicketField, List>();
	}

	public RedmineQueryPage(TaskRepository repository) {
		this(repository, null);
	}

	public void createControl(final Composite parent) {
		pageScroll = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);

		pageComposite = new Composite(pageScroll, SWT.NONE);
		pageLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		pageComposite.setLayoutData(pageLayoutData);
		GridLayout layout = new GridLayout(1, false);
		pageComposite.setLayout(layout);

		createTitleGroup(pageComposite);

		projectViewer = new ComboViewer(pageComposite, SWT.READ_ONLY);
		projectViewer.setContentProvider(new RedmineContentProvider(PROJECT_SELECT_TITLE));
		projectViewer.setLabelProvider(new RedmineLabelProvider());
		projectViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		projectViewer.addSelectionChangedListener(new ProjectSelectionListener());

		if (client.supportServersideStoredQueries()) {
			storedQueryViewer = new ComboViewer(pageComposite, SWT.READ_ONLY);
			storedQueryViewer.setContentProvider(new RedmineContentProvider(QUERY_SELECT_TITLE));
			storedQueryViewer.setLabelProvider(new RedmineLabelProvider());
			storedQueryViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			storedQueryViewer.addSelectionChangedListener(new StoredQuerySelectionListener());
		}

//		TabFolder tabFolder = new TabFolder(pageComposite, SWT.NONE);
//		TabItem mainItem = new TabItem(tabFolder, SWT.NONE);
//		TabItem customItem = new TabItem(tabFolder, SWT.NONE);
//		mainItem.setText("MAIN FILTER - RENAME");
//		customItem.setText("CUSTOM FILTER - RENAME");
		
//		settingsComposite = new Composite(tabFolder, SWT.NONE);
//		mainItem.setControl(settingsComposite);
		settingsComposite = new Composite(pageComposite, SWT.NONE);
		settingsComposite.setLayout(layout);
		
//		customFilterComposite = new Composite(tabFolder, SWT.NONE);
//		customFilterComposite.setLayout(layout);
//		customItem.setControl(customFilterComposite);

		createListGroup(settingsComposite);
		createTextGroup(settingsComposite);
		
		createUpdateButton(pageComposite);

		pageScroll.setContent(pageComposite);
		pageScroll.setExpandHorizontal(true);
		pageScroll.setExpandVertical(true);
		pageScroll.setMinSize(pageComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(pageScroll);
	}

	private void createTitleGroup(Composite control) {
		if (inSearchContainer()) {
			return;
		}

		Label titleLabel = new Label(control, SWT.NONE);
		titleLabel.setText(TITLE_QUERY_TITLE);

		titleText = new Text(control, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 2;
		titleText.setLayoutData(gd);
		titleText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				getContainer().updateButtons();
			}
		});
	}

	private void createTextGroup(final Composite parent) {

		Composite control = new Composite(parent, SWT.NONE);
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(3, false);
		control.setLayout(layout);

		GridData commonGridData = new GridData();
		GridData textGridData = new GridData(SWT.FILL, SWT.CENTER, false, true);
		textGridData.widthHint=300;

		for (int i = 1; i <= txtSearchFields.size(); i++) {
			SearchField searchField = txtSearchFields.get(i - 1);

			Label label = new Label(control, SWT.NONE);
			label.setText(searchField.name());
			label.setLayoutData(commonGridData);

			Combo combo = new Combo(control, SWT.READ_ONLY | SWT.DROP_DOWN);
			txtSearchOperators.put(combo, searchField);
			combo.setLayoutData(commonGridData);
			combo.add(OPERATOR_TITLE);
			combo.select(0);
			for (RedmineSearchFilter.CompareOperator operator : searchField
					.getCompareOperators()) {
				combo.add(operator.toString());
			}
			combo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Combo combo = (Combo) e.widget;
					SearchField searchField = txtSearchOperators.get(combo);
					Text text = txtSearchValues.get(searchField);
					if (combo.getSelectionIndex() == 0) {
						text.setEnabled(false);
					} else {
						String selected = combo.getItem(combo
								.getSelectionIndex());
						text.setEnabled(CompareOperator.fromString(selected)
								.useValue());
					}
				}
			});

			Text text = new Text(control, SWT.BORDER);
			txtSearchValues.put(searchField, text);
			text.setLayoutData(textGridData);
			text.setEnabled(false);
		}
	}

	private void createListGroup(final Composite control) {
		LabelProvider labelProvider = new RedmineLabelProvider();

		for (int i = 1; i <= lstSearchFields.size(); i++) {
			SearchField searchField = lstSearchFields.get(i - 1);

			ListViewer list = new ListViewer(control, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
			list.setLabelProvider(labelProvider);
			list.setContentProvider(new RedmineContentProvider());
			list.getControl().setEnabled(false);
			lstSearchValues.put(searchField, list);

			ComboViewer combo = new ComboViewer(control, SWT.READ_ONLY | SWT.DROP_DOWN);
			String defaultValue = searchField.isRequired()?null:OPERATOR_TITLE;
			combo.setContentProvider(new RedmineContentProvider(defaultValue));
			combo.setLabelProvider(labelProvider);
			lstSearchOperators.put(searchField, combo);
			combo.setInput(searchField.getCompareOperators());
			combo.setSelection(new StructuredSelection(combo.getElementAt(0)));
			
			combo.addSelectionChangedListener(
					new RedmineCompareOperatorSelectionListener(
							lstSearchValues.get(searchField)));
		}
		
		RedmineGuiHelper.placeElements(control, 4, lstSearchFields, lstSearchValues, lstSearchOperators);
	}

//	private void createListGroup(final Composite parent) {
//		int columns = 4;
//		
//		
//		Composite control = new Composite(parent, SWT.NONE);
//		GridLayout layout = new GridLayout(columns * 2, true);
//		control.setLayout(layout);
//		
//		GridData commonGridData = new GridData(GridData.BEGINNING,
//				GridData.BEGINNING, false, false);
//		commonGridData.horizontalAlignment = SWT.FILL;
//		
//		GridData listGridData = new GridData();
//		listGridData.verticalSpan = 2;
//		listGridData.heightHint = 100;
//		listGridData.widthHint = 85;
//		
//		LabelProvider labelProvider = new RedmineLabelProvider();
//		
//		for (int i = 1; i <= lstSearchFields.size(); i++) {
//			SearchField searchField = lstSearchFields.get(i - 1);
//			
//			Label label = new Label(control, SWT.NONE);
//			label.setText(searchField.name());
//			label.setLayoutData(commonGridData);
//			
//			ListViewer list = new ListViewer(control, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
//			list.setLabelProvider(labelProvider);
//			list.setContentProvider(new RedmineContentProvider());
//			lstSearchValues.put(searchField, list);
//			list.getControl().setLayoutData(listGridData);
//			list.getControl().setEnabled(false);
//			
//			if (i % columns == 0 || i == lstSearchFields.size()) {
//				int sv = (i % columns == 0) ? i - columns : i - i % columns;
//				if (i % columns != 0) {
//					listGridData = new GridData();
//					listGridData.verticalSpan = 2;
//					listGridData.heightHint = 100;
//					listGridData.horizontalSpan = (columns-(i % columns)) * 2 +1;
//					listGridData.widthHint = 85;
//					list.getControl().setLayoutData(listGridData);
//				}
//				for (int j = sv; j < i; j++) {
//					SearchField tmpSearchField = lstSearchFields.get(j);
//					ComboViewer combo = new ComboViewer(control, SWT.READ_ONLY | SWT.DROP_DOWN);
//					combo.setContentProvider(new RedmineContentProvider(tmpSearchField.isRequired()?null:OPERATOR_TITLE));
//					combo.setLabelProvider(labelProvider);
//					lstSearchOperators.put(tmpSearchField, combo);
//					combo.getControl().setLayoutData(commonGridData);
//					combo.setInput(tmpSearchField.getCompareOperators());
//					combo.setSelection(new StructuredSelection(combo.getElementAt(0)));
//					
//					combo.addSelectionChangedListener(
//							new RedmineCompareOperatorSelectionListener(
//									lstSearchValues.get(tmpSearchField)));
//				}
//			}
//		}
//	}
//	
	protected Control createUpdateButton(final Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		control.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalAlignment = GridData.END;
		control.setLayoutData(gridData);

		updateButton = new Button(control, SWT.PUSH);
		updateButton.setText("Update Attributes from Repository");
		updateButton.setLayoutData(new GridData());
		updateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getTaskRepository() != null) {
					updateAttributesFromRepository(true);
				} else {
					MessageDialog
							.openInformation(Display.getCurrent()
									.getActiveShell(),
									"Update Attributes Failed",
									"No repository available, please add one using the Task Repositories view.");
				}
			}
		});

		return control;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (data == null) {
						if (getControl() != null && !getControl().isDisposed()) {
							updateAttributesFromRepository(false);
						}
					}
					if (query != null && query.getAttribute(RedmineSearch.SEARCH_PARAMS) != null) {
						restoreQuery(query);
					}
				}
			});
		}
	}

	private void updateAttributesFromRepository(final boolean force) {

		if (force || !client.hasAttributes()) {
			try {
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {
						try {
							client.updateAttributes(force, monitor);
						} catch (RedmineException e) {
							throw new InvocationTargetException(e);
						}
					}
				};
	
				if (getContainer() != null) {
					getContainer().run(true, true, runnable);
				} else if (getSearchContainer() != null) {
					getSearchContainer().getRunnableContext().run(true, true,
							runnable);
				} else {
					IProgressService service = PlatformUI.getWorkbench()
							.getProgressService();
					service.busyCursorWhile(runnable);
				}
			} catch (InvocationTargetException e) {
				setErrorMessage(RedmineCorePlugin.toStatus(e.getCause(),
						getTaskRepository()).getMessage());
				return;
			} catch (InterruptedException e) {
				return;
			}
		}

		data = client.getClientData();

		/* Projects */
		projectViewer.setInput(data.getProjects());

		/* Status */
		ListViewer list = lstSearchValues.get(SearchField.STATUS);
		list.setInput(data.getStatuses());

		/* Priority */
		list = lstSearchValues.get(SearchField.PRIORITY);
		list.setInput(data.getPriorities());
	}

	protected void updateProjectAttributes(RedmineProjectData projectData) {
		if (client.supportServersideStoredQueries()) {
			/* Stored queries */
			storedQueryViewer.setInput(projectData.getStoredQueries());
		}
		
		/* Author, AssignedTo */
		ListViewer list = lstSearchValues.get(SearchField.ASSIGNED_TO);
		list.setInput(projectData.getAssignableMembers());
		
		list = lstSearchValues.get(SearchField.AUTHOR);
		list.setInput(projectData.getMembers());

		/* Version */
		list = lstSearchValues.get(SearchField.FIXED_VERSION);
		list.setInput(projectData.getVersions());

		/* Tracker */
		list = lstSearchValues.get(SearchField.TRACKER);
		list.setInput(projectData.getTrackers());

		/* Category */
		list = lstSearchValues.get(SearchField.CATEGORY);
		list.setInput(projectData.getCategorys());

	}
	
//	private void updateCustomFieldFilter(RedmineProjectData projectData) {
//		java.util.List<RedmineCustomTicketField> customFields = projectData.getCustomTicketFields();
//		
//		Composite control = new Composite(customFilterComposite, SWT.NONE);
//		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		GridLayout layout = new GridLayout(3, false);
//		control.setLayout(layout);
//		
//		GridData listGridData = new GridData();
//		listGridData.verticalSpan = 2;
//		listGridData.heightHint = 100;
//		listGridData.widthHint = 85;
//
//		java.util.List<RedmineCustomTicketField> keys 
//			= new ArrayList<RedmineCustomTicketField>(lstCustomSearchValues.keySet());
//
//		
//		for (RedmineCustomTicketField customField : customFields) {
//			if (!customField.isSupportFilter()) {
//				continue;
//			}
//			
//			if (customField.getType()==FieldType.LIST) {
//				if (keys.contains(customField)) {
//					keys.remove(customField);
//				} else {
//
//					List list = new List(control, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
//					list.setLayoutData(listGridData);
//					lstCustomSearchValues.put(customField, list);
//					list.setEnabled(false);
//				}
//			}
//		}
//		
//		for (RedmineCustomTicketField customField : keys) {
//			lstCustomSearchValues.remove(customField).dispose();
//		}
//
//		customFilterComposite.setSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
//		customFilterComposite.layout(true, true);
//	}
	

	private void restoreQuery(IRepositoryQuery query) {
		titleText.setText(query.getSummary());

		projectData = data.getProjectFromName(query.getAttribute(RedmineSearch.PROJECT_NAME));

		RedmineSearch search = RedmineSearch.fromSearchQueryParam(query.getAttribute(RedmineSearch.SEARCH_PARAMS), getTaskRepository().getRepositoryUrl());
		search.setProject(projectData.getProject());
		
		projectViewer.setSelection(new StructuredSelection(projectData));

		if (client.supportServersideStoredQueries()) {
			String storedQueryIdString = query.getAttribute(RedmineSearch.STORED_QUERY_ID);
			int storedQueryId = (storedQueryIdString==null) ? 0 : Integer.parseInt(storedQueryIdString);
			search.setStoredQueryId(storedQueryId);
			RedmineStoredQuery storedQuery = (storedQueryId>0) ? projectData.getStoredQuery(storedQueryId) : null;
			storedQueryViewer.setSelection(new StructuredSelection(storedQuery==null ? QUERY_SELECT_TITLE : storedQuery));
		}
		
		updateProjectAttributes(projectData);
		
		for (RedmineSearchFilter filter : search.getFilters()) {
			SearchField field = filter.getSearchField();
			CompareOperator compOp = filter.getOperator();

			if (lstSearchValues.containsKey(field)) {
				ListViewer list = lstSearchValues.get(field);
				java.util.List<String> oldValues = filter.getValues();
				RedmineTicketAttribute[] selected = new RedmineTicketAttribute[oldValues.size()];
				for (int i=oldValues.size()-1; i>=0; i--) {
					try {
						selected[i] = attributeValue2Attribute(projectData, field, Integer.parseInt(oldValues.get(i)));
					} catch (RuntimeException e) {
						;
					}
				}
				list.setSelection(new StructuredSelection(selected));
				ComboViewer combo = lstSearchOperators.get(field);
					combo.setSelection(new StructuredSelection(compOp));
					list.getControl().setEnabled(compOp.useValue());
			} else if (txtSearchValues.containsKey(field)) {
				Text text = txtSearchValues.get(field);
				if (filter.getValues().size() > 0) {
					text.setText(filter.getValues().get(0));
				}
				for (Map.Entry<Combo, SearchField> entry : txtSearchOperators
						.entrySet()) {
					if (entry.getValue() == field) {
						Combo combo = entry.getKey();
						combo.select(combo.indexOf(compOp.toString()));
						text.setEnabled(compOp.useValue());
						break;
					}
				}

			}
		}
	
		getContainer().updateButtons();
	}
	
	/**
	 * Deselect / clear all Settings / Attributes
	 */
	protected void clearSettings() {
		for (ComboViewer combo : lstSearchOperators.values()) {
			combo.setSelection(new StructuredSelection(combo.getElementAt(0)));
		}
		for (ListViewer list : lstSearchValues.values()) {
			list.setSelection(new StructuredSelection());
			list.getControl().setEnabled(false);
		}
		for (Entry<Combo, SearchField> entry : txtSearchOperators.entrySet()) {
			entry.getKey().select(0);
			Text text = txtSearchValues.get(entry.getValue());
			text.setText("");
			text.setEnabled(false);
		}
	}

	private RedmineTicketAttribute attributeValue2Attribute(RedmineProjectData projectData, SearchField field, int value) {
		switch (field) {
		case STATUS:
			return data.getStatus(value);
		case PRIORITY:
			return data.getPriority(value);
		case TRACKER:
			return projectData.getTracker(value);
		case FIXED_VERSION:
			return projectData.getVersion(value);
		case AUTHOR:
			return projectData.getMember(value);
		case ASSIGNED_TO:
			return projectData.getMember(value);
		case CATEGORY:
			return projectData.getCategory(value);
		}
		throw new IllegalArgumentException();
	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}

	@Override
	public boolean isPageComplete() {
		return validate();
	}

	private boolean validate() {
		boolean returnsw = (titleText != null && titleText.getText().length() > 0);
		returnsw &= projectData != null;
		return returnsw;
	}

	private RedmineTicketAttribute getFirstSelectedEntry(Viewer viewer) {
		if (!viewer.getSelection().isEmpty() && viewer.getSelection() instanceof StructuredSelection) {
			Object selected = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
			if (selected instanceof RedmineTicketAttribute) {
				return (RedmineTicketAttribute) selected;
			}
		}
		return null;
	}
	
	@Override
	public void applyTo(IRepositoryQuery query) {
		query.setSummary(getQueryTitle());
		
		RedmineSearch search = buildSearch();
		query.setAttribute(RedmineSearch.PROJECT_NAME, projectData.getProject().getName());
		query.setAttribute(RedmineSearch.PROJECT_ID, "" + projectData.getProject().getValue());
		query.setAttribute(RedmineSearch.SEARCH_PARAMS, search.toSearchQueryParam());
		query.setAttribute(RedmineSearch.STORED_QUERY_ID, "" + search.getStoredQueryId());
		query.setUrl(search.toQuery());
	}

	private RedmineSearch buildSearch() {
		RedmineSearch search = new RedmineSearch(getTaskRepository().getRepositoryUrl());
		search.setProject(projectData.getProject());
		
		if (client.supportServersideStoredQueries()) {
			RedmineTicketAttribute storedQuery = getFirstSelectedEntry(storedQueryViewer);
			if (storedQuery != null) {
				search.setStoredQueryId(storedQuery.getValue());
			}
		}
		
		for (Entry<SearchField, ComboViewer> entry : lstSearchOperators.entrySet()) {
			ComboViewer opCombo = entry.getValue();
			SearchField field = entry.getKey();
			IStructuredSelection selection = (IStructuredSelection)opCombo.getSelection();
			if (selection.getFirstElement() instanceof CompareOperator) {
				CompareOperator operator = (CompareOperator)selection.getFirstElement();
				ListViewer valList = lstSearchValues.get(field);
				
				selection = (IStructuredSelection)valList.getSelection();
				if (selection.isEmpty()) {
					search.addFilter(field, (CompareOperator)operator, "");
				} else {
					Iterator valIterator = selection.iterator();
					while(valIterator.hasNext()) {
						RedmineTicketAttribute attribute = (RedmineTicketAttribute)valIterator.next();
						search.addFilter(field, (CompareOperator)operator, ""+attribute.getValue());
					}
				}
			}
		}
		for (Iterator<Combo> iterator = txtSearchOperators.keySet().iterator(); iterator
				.hasNext();) {
			Combo opCombo = iterator.next();
			if (opCombo.getSelectionIndex() > 0) {
				SearchField field = txtSearchOperators.get(opCombo);
				String opName = opCombo.getItem(opCombo.getSelectionIndex());
				Text text = txtSearchValues.get(field);
				search.addFilter(field, opName, text.getText().trim());
			}
		}
		return search;
	}

	@Override
	public String getQueryTitle() {
		return (titleText != null) ? titleText.getText() : "<search>";
	}

	private class ProjectSelectionListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			if (!event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection) {
				
				Object selected = ((IStructuredSelection)event.getSelection()).getFirstElement();
				if (selected instanceof RedmineProjectData) {
					RedmineProjectData selProjectData = (RedmineProjectData)selected;
					if (!(projectData != null && projectData.getProject().equals(selProjectData.getProject()))) {
						RedmineQueryPage.this.clearSettings();
					}
					projectData = (RedmineProjectData)selected;
					RedmineQueryPage.this.updateProjectAttributes(projectData);
//					RedmineQueryPage.this.updateCustomFieldFilter(projectData);
				} else {
					RedmineQueryPage.this.clearSettings();
				}
				
				if (RedmineQueryPage.this.getContainer()!=null) {
					RedmineQueryPage.this.getContainer().updateButtons();
				}
			}
		}
	}
	
	private class StoredQuerySelectionListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			if (!event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection) {
				Object selected = ((IStructuredSelection)event.getSelection()).getFirstElement();
				if (selected instanceof RedmineStoredQuery) {
					RedmineQueryPage.this.settingsComposite.setVisible(false);
					RedmineQueryPage.this.settingsComposite.setLayoutData(new GridData(0,0));
				} else {
					RedmineQueryPage.this.settingsComposite.setVisible(true);
					RedmineQueryPage.this.settingsComposite.setLayoutData(RedmineQueryPage.this.pageLayoutData);
				}
				RedmineQueryPage.this.pageScroll.setMinSize(RedmineQueryPage.this.pageComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				RedmineQueryPage.this.pageComposite.layout(true, true);
			}
		}
	}
}
