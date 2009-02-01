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
import java.util.Collection;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
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
	protected TabFolder settingsFolder;
	protected Composite customComposite;
	
	protected ComboViewer projectViewer;
	protected RedmineProjectData projectData;

	protected ComboViewer storedQueryViewer;

	protected Map<SearchField, ComboViewer> lstSearchOperators;
	protected Map<SearchField, ListViewer> lstSearchValues;

	protected Map<SearchField, ComboViewer> txtSearchOperators;
	protected Map<SearchField, Text> txtSearchValues;

	protected final Map<RedmineCustomTicketField, ComboViewer> customSearchOperators;
	protected final Map<RedmineCustomTicketField, ListViewer> lstCustomSearchValues;
	protected final Map<RedmineCustomTicketField, Control> txtCustomSearchValues;

	protected Button updateButton;
	protected RedmineClientData data;

	public RedmineQueryPage(TaskRepository repository, IRepositoryQuery query) {
		super(TITLE, repository, query);

		this.query=query;
		
		RedmineRepositoryConnector connector = (RedmineRepositoryConnector) TasksUi.getRepositoryManager().getRepositoryConnector(RedmineCorePlugin.REPOSITORY_KIND);
		client = connector.getClientManager().getRedmineClient(getTaskRepository());

		setTitle(TITLE);
		setDescription(DESCRIPTION);


		lstSearchOperators = new HashMap<SearchField, ComboViewer>();
		lstSearchValues = new HashMap<SearchField, ListViewer>();

		txtSearchOperators = new HashMap<SearchField, ComboViewer>();
		txtSearchValues = new HashMap<SearchField, Text>();
		
		customSearchOperators = new HashMap<RedmineCustomTicketField, ComboViewer>();
		lstCustomSearchValues = new HashMap<RedmineCustomTicketField, ListViewer>();
		txtCustomSearchValues = new HashMap<RedmineCustomTicketField, Control>();

//		lstCustomSearchValues = new HashMap<RedmineCustomTicketField, List>();
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

		settingsFolder = new TabFolder(pageComposite, SWT.NONE);
		settingsFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final TabItem mainItem = new TabItem(settingsFolder, SWT.NONE);
		final TabItem customItem = new TabItem(settingsFolder, SWT.NONE);
		mainItem.setText("MAIN FILTER - RENAME");
		customItem.setText("CUSTOM FILTER - RENAME");
		
		final Composite commonComposite = new Composite(settingsFolder, SWT.NONE);
		commonComposite.setLayout(layout);
		mainItem.setControl(commonComposite);
		
		customComposite = new Composite(settingsFolder, SWT.NONE);
		customComposite.setLayout(layout);
		customItem.setControl(customComposite);

		createListGroup(commonComposite);
		createTextGroup(commonComposite);
		
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

	private void createTextGroup(final Composite control) {
		LabelProvider labelProvider = new RedmineLabelProvider();
		Collection<SearchField> searchFields = new ArrayList<SearchField>();

		for (SearchField searchField : SearchField.values()) {
			if (searchField.isListType() || searchField==SearchField.TEXT_BASED) {
				continue;
			}
			searchFields.add(searchField);

			Text text = new Text(control, SWT.BORDER);
			text.setEnabled(false);
			txtSearchValues.put(searchField, text);
			
			ComboViewer combo = new ComboViewer(control, SWT.READ_ONLY | SWT.DROP_DOWN);
			String defaultValue = searchField.isRequired()?null:OPERATOR_TITLE;
			combo.setContentProvider(new RedmineContentProvider(defaultValue));
			combo.setLabelProvider(labelProvider);
			txtSearchOperators.put(searchField, combo);
			combo.setInput(searchField.getCompareOperators());
			combo.setSelection(new StructuredSelection(combo.getElementAt(0)));
			
			combo.addSelectionChangedListener(
					new RedmineCompareOperatorSelectionListener(
							txtSearchValues.get(searchField)));
		}
		
		RedmineGuiHelper.placeTextElements(control, searchFields, txtSearchValues, txtSearchOperators);
	}
	
	private void createListGroup(final Composite control) {
		LabelProvider labelProvider = new RedmineLabelProvider();
		Collection<SearchField> searchFields = new ArrayList<SearchField>();
		
		for (SearchField searchField : SearchField.values()) {
			if (!searchField.isListType() || searchField==SearchField.LIST_BASED) {
				continue;
			}
			searchFields.add(searchField);

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
							lstSearchValues.get(searchField).getControl()));
		}
		
		RedmineGuiHelper.placeListElements(control, 4, searchFields, lstSearchValues, lstSearchOperators);
	}

	protected void createUpdateButton(final Composite parent) {
		updateButton = new Button(parent, SWT.PUSH);
		updateButton.setText("Update Attributes from Repository");
		updateButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
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
	
	private void updateCustomFieldFilter(RedmineProjectData projectData) {
		LabelProvider labelProvider = new RedmineLabelProvider();
		java.util.List<RedmineCustomTicketField> customFields = projectData.getCustomTicketFields();
		

		java.util.List<RedmineCustomTicketField> lstKeys 
			= new ArrayList<RedmineCustomTicketField>(lstCustomSearchValues.keySet());
		java.util.List<RedmineCustomTicketField> txtKeys 
		= new ArrayList<RedmineCustomTicketField>(txtCustomSearchValues.keySet());

		for (RedmineCustomTicketField customField : customFields) {
			if (!customField.isSupportFilter()) {
				continue;
			}
			
			Control control = null;
			ComboViewer combo = null;
			SearchField searchfield = null;
			
			switch(customField.getType()) {
				case LIST : {
					ListViewer list = null;
					if (lstKeys.remove(customField)) {
						list = lstCustomSearchValues.get(customField);
						control = list.getControl();
					} else {
						list = new ListViewer(customComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
						list.setLabelProvider(labelProvider);
						list.setContentProvider(new RedmineContentProvider());
						control = list.getControl();
						lstCustomSearchValues.put(customField, list);
						searchfield = SearchField.fromCustomTicketField(customField);
					}
					list.setInput(customField.getListValues());
					break;
				}
				case STRING :
				case TEXT :
				case INT :
				case FLOAT : {
					continue;
//					if (txtCustomSearchValues.get(customField) instanceof Text) {
//						txtKeys.remove(customField);
//						control = txtCustomSearchValues.get(customField);
//					} else {
//						control = new Text(customComposite, SWT.BORDER);
//						txtCustomSearchValues.put(customField, control);
//						searchfield = SearchField.fromCustomTicketField(customField);
//					}
//					break;
				}
				case DATE : continue ;
				case BOOL : continue ;
			}
			
			if (searchfield==null) {
				combo = customSearchOperators.get(customField);
				combo.getControl().setParent(customComposite);
				control.setParent(customComposite);
			} else {
				control.setEnabled(false);
				if (customSearchOperators.containsKey(customField)) {
					customSearchOperators.remove(customField).getControl().dispose();
				}
				combo = new ComboViewer(customComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
				combo.setContentProvider(new RedmineContentProvider(OPERATOR_TITLE));
				combo.setLabelProvider(labelProvider);
				combo.setInput(searchfield.getCompareOperators());
				combo.setSelection(new StructuredSelection(combo.getElementAt(0)));
				combo.addSelectionChangedListener(
						new RedmineCompareOperatorSelectionListener(control));
				customSearchOperators.put(customField, combo);
			}
		}
		
		RedmineGuiHelper.placeListElements(customComposite, 4, lstCustomSearchValues.keySet(), lstCustomSearchValues, customSearchOperators);
	}
	

	private void restoreQuery(IRepositoryQuery query) {
		titleText.setText(query.getSummary());

		projectData = data.getProjectFromName(query.getAttribute(RedmineSearch.PROJECT_NAME));
		updateProjectAttributes(projectData);

		RedmineSearch search = RedmineSearch.fromSearchQueryParam(projectData, query.getAttribute(RedmineSearch.SEARCH_PARAMS), getTaskRepository().getRepositoryUrl());
		search.setProject(projectData.getProject());
		
		projectViewer.setSelection(new StructuredSelection(projectData));

		if (client.supportServersideStoredQueries()) {
			String storedQueryIdString = query.getAttribute(RedmineSearch.STORED_QUERY_ID);
			int storedQueryId = (storedQueryIdString==null) ? 0 : Integer.parseInt(storedQueryIdString);
			search.setStoredQueryId(storedQueryId);
			RedmineStoredQuery storedQuery = (storedQueryId>0) ? projectData.getStoredQuery(storedQueryId) : null;
			storedQueryViewer.setSelection(new StructuredSelection(storedQuery==null ? QUERY_SELECT_TITLE : storedQuery));
		}
		
		
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
				ComboViewer combo = txtSearchOperators.get(field);
				combo.setSelection(new StructuredSelection(compOp));
				text.setEnabled(compOp.useValue());

			}
		}
	
		for (RedmineSearchFilter filter : search.getCustomFilters()) {
			RedmineCustomTicketField field = filter.getCustomTicketField();
			CompareOperator compOp = filter.getOperator();
			
			if (lstCustomSearchValues.containsKey(field)) {
				ListViewer list = lstCustomSearchValues.get(field);
				java.util.List<String> oldValues = filter.getValues();
				list.setSelection(new StructuredSelection(oldValues));
				ComboViewer combo = customSearchOperators.get(field);
				combo.setSelection(new StructuredSelection(compOp));
				list.getControl().setEnabled(compOp.useValue());
			} else if (txtCustomSearchValues.containsKey(field)) {
				Control control = txtCustomSearchValues.get(field);
				if (filter.getValues().size() > 0) {
					if (control instanceof Text) {
						((Text)control).setText(filter.getValues().get(0));
					}
				}
				ComboViewer combo = txtSearchOperators.get(field);
				combo.setSelection(new StructuredSelection(compOp));
				control.setEnabled(compOp.useValue());
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
		for (ComboViewer combo : txtSearchOperators.values()) {
			combo.setSelection(new StructuredSelection(combo.getElementAt(0)));
		}
		for (Text text : txtSearchValues.values()) {
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
					search.addFilter(field, operator, "");
				} else {
					Iterator<?> valIterator = selection.iterator();
					while(valIterator.hasNext()) {
						RedmineTicketAttribute attribute = (RedmineTicketAttribute)valIterator.next();
						search.addFilter(field, operator, ""+attribute.getValue());
					}
				}
			}
		}
		for (Entry<SearchField, ComboViewer> entry : txtSearchOperators.entrySet()) {
			ComboViewer opCombo = entry.getValue();
			SearchField field = entry.getKey();
			IStructuredSelection selection = (IStructuredSelection)opCombo.getSelection();
			if (selection.getFirstElement() instanceof CompareOperator) {
				CompareOperator operator = (CompareOperator)selection.getFirstElement();
				Text text = txtSearchValues.get(field);
				search.addFilter(field, operator, text.getText().trim());
			}
		}

		for (Entry<RedmineCustomTicketField, ListViewer> entry : lstCustomSearchValues.entrySet()) {
			RedmineCustomTicketField customField = entry.getKey();
			ComboViewer opCombo = customSearchOperators.get(customField);
			ListViewer valList = entry.getValue();
//			SearchField field = entry.getKey();
			IStructuredSelection selection = (IStructuredSelection)opCombo.getSelection();
			if (selection.getFirstElement() instanceof CompareOperator) {
				CompareOperator operator = (CompareOperator)selection.getFirstElement();
				
				selection = (IStructuredSelection)valList.getSelection();
				if (selection.isEmpty()) {
					search.addFilter(customField, operator, "");
				} else {
					Iterator<?> valIterator = selection.iterator();
					while(valIterator.hasNext()) {
						search.addFilter(customField, operator, valIterator.next().toString());
					}
				}
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
					RedmineQueryPage.this.updateCustomFieldFilter(projectData);
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
					RedmineQueryPage.this.settingsFolder.setVisible(false);
					RedmineQueryPage.this.settingsFolder.setLayoutData(new GridData(0,0));
				} else {
					RedmineQueryPage.this.settingsFolder.setVisible(true);
					RedmineQueryPage.this.settingsFolder.setLayoutData(RedmineQueryPage.this.pageLayoutData);
				}
				RedmineQueryPage.this.pageScroll.setMinSize(RedmineQueryPage.this.pageComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				RedmineQueryPage.this.pageComposite.layout(true, true);
			}
		}
	}
}
