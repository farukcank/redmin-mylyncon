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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.mylyn.commons.core.StatusHandler;
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
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.RedmineRepositoryConnector;
import org.svenk.redmine.core.client.RedmineClientData;
import org.svenk.redmine.core.client.RedmineProjectData;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.model.IRedmineQueryField;
import org.svenk.redmine.core.model.RedmineCustomField;
import org.svenk.redmine.core.model.RedmineProject;
import org.svenk.redmine.core.model.RedmineSearch;
import org.svenk.redmine.core.model.RedmineSearchFilter;
import org.svenk.redmine.core.model.RedmineStoredQuery;
import org.svenk.redmine.core.model.RedmineTicketAttribute;
import org.svenk.redmine.core.model.RedmineCustomField.FieldType;
import org.svenk.redmine.core.model.RedmineSearchFilter.CompareOperator;
import org.svenk.redmine.core.model.RedmineSearchFilter.SearchField;
import org.svenk.redmine.core.search.internal.IRedmineSearchData;
import org.svenk.redmine.ui.wizard.RedmineLabelProvider;

public class RedmineQueryPage extends AbstractRepositoryQueryPage {

	private static final String DATA_KEY_VALUE_CROSS_PROJECT = "_CROSS_PROJECT_"; 
	
	private static final String TITLE = "Enter query parameters";

	private static final String DESCRIPTION = "Only predefined filters are supported.";

	private static final String TITLE_QUERY_TITLE = "Query Title:";

	private static final String PROJECT_SELECT_TITLE = "Select a project or create a Cross-Project-Query";

	private static final String QUERY_SELECT_TITLE = "Select a serverside stored query or create a new";

	private static final String OPERATOR_TITLE = "Disabled";
	
	private static final String OPERATOR_BOOLEAN_TRUE = "true";

	private static final String TAB_STANDARD = "Master data";

	private static final String TAB_CUSTOM = "Custom fields";

	private IRepositoryQuery query;

	private Text titleText;

	private IRedmineClient client;

	protected Composite pageComposite;
	protected ScrolledComposite pageScroll;
	protected GridData pageLayoutData;
	protected TabFolder settingsFolder;
	protected Composite customComposite;
	
	protected ComboViewer projectViewer;

	protected ComboViewer storedQueryViewer;

	protected Map<IRedmineQueryField, ComboViewer> searchOperators;
	protected Map<IRedmineQueryField, ListViewer> lstSearchValues;
	protected Map<IRedmineQueryField, Text> txtSearchValues;

	protected final Map<IRedmineQueryField, ComboViewer> customSearchOperators;
	protected final Map<IRedmineQueryField, ListViewer> lstCustomSearchValues;
	protected final Map<IRedmineQueryField, Text> txtCustomSearchValues;

	protected Map<String, IRedmineSearchData> listData;
	
	protected Button updateButton;
	protected RedmineClientData data;

	public RedmineQueryPage(TaskRepository repository, IRepositoryQuery query) {
		super(TITLE, repository, query);

		this.query=query;
		
		RedmineRepositoryConnector connector = (RedmineRepositoryConnector) TasksUi.getRepositoryManager().getRepositoryConnector(RedmineCorePlugin.REPOSITORY_KIND);
		try {
			client = connector.getClientManager().getRedmineClient(getTaskRepository());
		} catch (RedmineException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, repository);
			StatusHandler.fail(status);
		}
		Assert.isNotNull(client);

		setTitle(TITLE);
		setDescription(DESCRIPTION);


		searchOperators = new HashMap<IRedmineQueryField, ComboViewer>();
		lstSearchValues = new HashMap<IRedmineQueryField, ListViewer>();
		txtSearchValues = new HashMap<IRedmineQueryField, Text>();
		
		customSearchOperators = new HashMap<IRedmineQueryField, ComboViewer>();
		lstCustomSearchValues = new HashMap<IRedmineQueryField, ListViewer>();
		txtCustomSearchValues = new HashMap<IRedmineQueryField, Text>();

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

		storedQueryViewer = new ComboViewer(pageComposite, SWT.READ_ONLY);
		storedQueryViewer.setContentProvider(new RedmineContentProvider(QUERY_SELECT_TITLE));
		storedQueryViewer.setLabelProvider(new RedmineLabelProvider());
		storedQueryViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		storedQueryViewer.addSelectionChangedListener(new StoredQuerySelectionListener());

		settingsFolder = new TabFolder(pageComposite, SWT.NONE);
		settingsFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final TabItem mainItem = new TabItem(settingsFolder, SWT.NONE);
		mainItem.setText(TAB_STANDARD);
		
		final Composite commonComposite = new Composite(settingsFolder, SWT.NONE);
		commonComposite.setLayout(layout);
		mainItem.setControl(commonComposite);
		
		final TabItem customItem = new TabItem(settingsFolder, SWT.NONE);
		customItem.setText(TAB_CUSTOM);

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

	private void createTextGroup(final Composite parent) {
		
		LabelProvider labelProvider = new RedmineLabelProvider();
		Collection<SearchField> searchFields = new ArrayList<SearchField>();

		for (SearchField searchField : SearchField.values()) {
			if (searchField.isListType() || searchField.isGeneric()) {
				continue;
			}
			searchFields.add(searchField);

			Text text = new Text(parent, SWT.BORDER);
			text.setEnabled(false);
			txtSearchValues.put(searchField, text);
			
			ComboViewer combo = new ComboViewer(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
			String defaultValue = searchField.isRequired()?null:OPERATOR_TITLE;
			combo.setContentProvider(new RedmineContentProvider(defaultValue));
			combo.setLabelProvider(labelProvider);
			searchOperators.put(searchField, combo);
			combo.setInput(searchField.getCompareOperators());
			combo.setSelection(new StructuredSelection(combo.getElementAt(0)));
			
			combo.addSelectionChangedListener(
					new RedmineCompareOperatorSelectionListener(
							txtSearchValues.get(searchField)));
		}
		
		RedmineGuiHelper.placeTextElements(parent, searchFields, txtSearchValues, searchOperators);
	}
	
	private void createListGroup(final Composite parent) {
		
		LabelProvider labelProvider = new RedmineLabelProvider();
		Collection<SearchField> searchFields = new ArrayList<SearchField>();
		
		for (SearchField searchField : SearchField.values()) {
			if (!searchField.isListType() || searchField.isGeneric()) {
				continue;
			}
			searchFields.add(searchField);

			ListViewer list = new ListViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
			list.setLabelProvider(labelProvider);
			list.setContentProvider(new RedmineContentProvider());
			list.getControl().setEnabled(false);
			lstSearchValues.put(searchField, list);

			ComboViewer combo = new ComboViewer(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
			String defaultValue = searchField.isRequired()?null:OPERATOR_TITLE;
			combo.setContentProvider(new RedmineContentProvider(defaultValue));
			combo.setLabelProvider(labelProvider);
			searchOperators.put(searchField, combo);
			combo.setInput(searchField.getCompareOperators());
			combo.setSelection(new StructuredSelection(combo.getElementAt(0)));
			
			combo.addSelectionChangedListener(
					new RedmineCompareOperatorSelectionListener(
							lstSearchValues.get(searchField).getControl()));
		}
		
		RedmineGuiHelper.placeListElements(parent, 4, searchFields, lstSearchValues, searchOperators);
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
					} else {
						projectViewer.setSelection(new StructuredSelection(PROJECT_SELECT_TITLE));
						storedQueryViewer.setInput(new String[]{QUERY_SELECT_TITLE});
						storedQueryViewer.setSelection(new StructuredSelection(QUERY_SELECT_TITLE));
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

		List<RedmineProject> projects = new ArrayList<RedmineProject>(data.getProjects().size());
		listData = new HashMap<String, IRedmineSearchData>(projects.size()+1);

		listData.put(DATA_KEY_VALUE_CROSS_PROJECT, new RedmineCrossProjectQueryListData(data));
		for (RedmineProjectData projectData : data.getProjects()) {
			projects.add(projectData.getProject());
			listData.put(projectData.getProject().getName(), new RedmineProjectQueryListData(projectData, data));
		}
		
		/* Projects */
		projectViewer.setInput(projects);
			
	}

	protected void updateProjectAttributes(String listDataKey) {
		IRedmineSearchData listData = this.listData.get(listDataKey);
		
		/* Stored queries */
		if(listData.getQueries()==null) {
			storedQueryViewer.setInput(QUERY_SELECT_TITLE);
		} else {
			storedQueryViewer.setInput(listData.getQueries());
		}
		
		/* Author, AssignedTo */
		ListViewer list = lstSearchValues.get(SearchField.ASSIGNED_TO);
		list.setInput(listData.getMembers());
		
		list = lstSearchValues.get(SearchField.AUTHOR);
		list.setInput(listData.getPersons());
		
		/* Version */
		list = lstSearchValues.get(SearchField.FIXED_VERSION);
		list.setInput(listData.getVersions());
		
		/* Tracker */
		list = lstSearchValues.get(SearchField.TRACKER);
		list.setInput(listData.getTrackers());
		
		/* Category */
		list = lstSearchValues.get(SearchField.CATEGORY);
		list.setInput(listData.getCategories());

		/* Status */
		list = lstSearchValues.get(SearchField.STATUS);
		list.setInput(listData.getStatus());

		/* Priority */
		list = lstSearchValues.get(SearchField.PRIORITY);
		list.setInput(listData.getPriorities());
	}
	
	private void updateCustomFieldFilter(String listDataKey) {
		LabelProvider labelProvider = new RedmineLabelProvider();
		List<RedmineCustomField> customFields = listData.get(listDataKey).getCustomTicketFields();
		

		List<IRedmineQueryField> lstKeys = new ArrayList<IRedmineQueryField>(lstCustomSearchValues.keySet());
		List<IRedmineQueryField> txtKeys = new ArrayList<IRedmineQueryField>(txtCustomSearchValues.keySet());

		Collection<Composite> oldComposites = new ArrayList<Composite>(2);
		for (Control child : customComposite.getChildren()) {
			if (child instanceof Composite) {
				oldComposites.add((Composite)child);
			}
		}

		if (customFields!=null) {
			for (RedmineCustomField customField : customFields) {
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
					case BOOL : {
						if (txtKeys.remove(customField)) {
							control = txtCustomSearchValues.get(customField);
						} else {
							txtKeys.remove(customField);
							Text text = new Text(customComposite, SWT.NONE);
							text.setText(OPERATOR_BOOLEAN_TRUE);
							text.setEditable(false);
							txtCustomSearchValues.put(customField, text);
							control = text;
							searchfield = SearchField.fromCustomTicketField(customField);
						}
						break;
					}
					case STRING :
					case TEXT :
					case INT :
					case DATE :
					case FLOAT : {
						if (txtKeys.remove(customField)) {
							control = txtCustomSearchValues.get(customField);
						} else {
							txtKeys.remove(customField);
							Text text = new Text(customComposite, SWT.BORDER);
							txtCustomSearchValues.put(customField, text);
							control = text;
							searchfield = SearchField.fromCustomTicketField(customField);
						}
						break;
					}
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
		}
		
		for (Composite old : oldComposites) {
			old.dispose();
		}
		
		for (IRedmineQueryField field : lstKeys) {
			lstCustomSearchValues.remove(field);
			customSearchOperators.remove(field);
		}
		for (IRedmineQueryField field : txtKeys) {
			txtCustomSearchValues.remove(field);
			customSearchOperators.remove(field);
		}
		
		RedmineGuiHelper.placeListElements(customComposite, 4, lstCustomSearchValues.keySet(), lstCustomSearchValues, customSearchOperators);
		RedmineGuiHelper.placeTextElements(customComposite, txtCustomSearchValues.keySet(), txtCustomSearchValues, customSearchOperators);
	}
	

	private void restoreQuery(IRepositoryQuery query) {
		titleText.setText(query.getSummary());

		String listDataKey = query.getAttribute(RedmineSearch.DATA_KEY);
		if(listDataKey==null || !this.listData.containsKey(listDataKey)) {
			return;
		}
		
		IRedmineSearchData queryData = this.listData.get(listDataKey);
		RedmineSearch search = RedmineSearch.fromSearchQueryParam(queryData, query.getAttribute(RedmineSearch.SEARCH_PARAMS), getTaskRepository().getRepositoryUrl());
	
		RedmineProjectData projectData = data.getProjectFromName(listDataKey);

		//NOTE : Don't call updateProjectAttributes() - projectViewer's SeletionListener call this method !!!
		if (projectData==null) {
			//Cross-Project-Query
			projectViewer.setSelection(new StructuredSelection(PROJECT_SELECT_TITLE));
		} else {
			//Project-Query
			projectViewer.setSelection(new StructuredSelection(projectData.getProject()));
			search.setProjectId(projectData.getProject().getValue());

			//Check StoredQuery usage
			String sqIdVal = query.getAttribute(RedmineSearch.STORED_QUERY_ID);
			int sqId = (sqIdVal==null || !sqIdVal.matches("^\\d+$")) ? 0 : Integer.parseInt(sqIdVal);
			search.setStoredQueryId(sqId);
			RedmineStoredQuery storedQuery = null;
			if(sqId>0) {
				storedQuery = queryData.getQuery(sqId);
			}
			storedQueryViewer.setSelection(new StructuredSelection(storedQuery==null ? QUERY_SELECT_TITLE : storedQuery));
		}
		
		restoreListValues(queryData, search, searchOperators, lstSearchValues);
		restoreTextValues(queryData, search, searchOperators, txtSearchValues);
		restoreListValues(queryData, search, customSearchOperators, lstCustomSearchValues);
		restoreTextValues(queryData, search, customSearchOperators, txtCustomSearchValues);
		
		getContainer().updateButtons();
	}
	
	private void restoreListValues(IRedmineSearchData queryData, RedmineSearch search, Map<? extends IRedmineQueryField, ComboViewer> operators, Map<? extends IRedmineQueryField, ListViewer> lists) {
		for (Entry<? extends IRedmineQueryField, ListViewer> entry : lists.entrySet()) {
			IRedmineQueryField queryField = entry.getKey();
			RedmineSearchFilter searchFilter = search.getFilter(queryField);
			if (searchFilter != null) {
				ListViewer list = entry.getValue();
				ComboViewer combo = operators.get(queryField);
				CompareOperator operator = searchFilter.getOperator();
				
				combo.setSelection(new StructuredSelection(operator));
				if (operator.useValue()) {
					list.getControl().setEnabled(true);
					
					java.util.List<String> oldValues = searchFilter.getValues();
					Object[] selected = new Object[oldValues.size()];
					for (int i=oldValues.size()-1; i>=0; i--) {
						try {
							selected[i] = attributeValue2Attribute(queryData, queryField, oldValues.get(i));
						} catch (RuntimeException e) {}
					}
					list.setSelection(new StructuredSelection(selected));
				}
			}
		}
	}
	
	private void restoreTextValues(IRedmineSearchData queryData, RedmineSearch search, Map<? extends IRedmineQueryField, ComboViewer> operators, Map<? extends IRedmineQueryField, Text> controls) {
		for (Entry<? extends IRedmineQueryField, Text> entry : controls.entrySet()) {
			IRedmineQueryField queryField = entry.getKey();
			RedmineSearchFilter searchFilter = search.getFilter(queryField);
			if (searchFilter != null) {
				Text text = entry.getValue();
				ComboViewer combo = operators.get(queryField);
				CompareOperator operator = searchFilter.getOperator();

				combo.setSelection(new StructuredSelection(operator));
				if (operator.useValue()) {
					text.setEnabled(true);

					if (searchFilter.getValues().size() > 0 && text.getEditable()) {
						String oldValue = searchFilter.getValues().get(0);
						text.setText(oldValue);
					}
				}
			}
		}
	}
	
	/**
	 * Deselect / clear all Settings / Attributes
	 */
	protected void clearSettings() {
		clearListSettings(searchOperators, lstSearchValues);
		clearTextSettings(searchOperators, txtSearchValues);
		clearListSettings(customSearchOperators, lstCustomSearchValues);
		clearTextSettings(customSearchOperators, txtCustomSearchValues);
	}
	
	private void clearListSettings(Map<IRedmineQueryField, ComboViewer> operators, Map<IRedmineQueryField, ListViewer> listValues) {
		for (Entry<IRedmineQueryField, ListViewer> entry : listValues.entrySet()) {
			entry.getValue().setSelection(new StructuredSelection());
			entry.getValue().getControl().setEnabled(false);
			ComboViewer operator = operators.get(entry.getKey());
			operator.setSelection(new StructuredSelection(operator.getElementAt(0)));
		}
	}

	private void clearTextSettings(Map<IRedmineQueryField, ComboViewer> operators, Map<IRedmineQueryField, Text> textValues) {
		for (Entry<IRedmineQueryField, Text> entry : textValues.entrySet()) {
			if (entry.getValue().getEditable()) {
				entry.getValue().setText("");
			}
			entry.getValue().setEnabled(false);
			ComboViewer operator = operators.get(entry.getKey());
			operator.setSelection(new StructuredSelection(operator.getElementAt(0)));
		}
	}
	
	protected void switchOperatorState(boolean state, boolean crossProjectOnly) {
		List<Entry<IRedmineQueryField, ComboViewer>> operatorViewer = new ArrayList<Entry<IRedmineQueryField, ComboViewer>>();
		operatorViewer.addAll(searchOperators.entrySet());
		operatorViewer.addAll(customSearchOperators.entrySet());
		
		for (Entry<IRedmineQueryField, ComboViewer> entry : operatorViewer) {
			if(state) {
				entry.getValue().getControl().setEnabled(true);
			} else if (!(crossProjectOnly && entry.getKey().crossProjectUsable())) {
				entry.getValue().getControl().setEnabled(false);
				
			}
		}
		
		storedQueryViewer.getControl().setEnabled(state || !crossProjectOnly);
	}
	
	private Object attributeValue2Attribute(IRedmineSearchData queryData, IRedmineQueryField field, String txtValue) {
		if (field instanceof RedmineCustomField) {
			return txtValue;
		} else if (field instanceof SearchField) {
			if (txtValue.matches("^\\d+$")) {
				SearchField searchField = (SearchField)field;
				int value = Integer.parseInt(txtValue);
				switch (searchField) {
				case STATUS:
					return queryData.getStatus(value);
				case PRIORITY:
					return queryData.getPriority(value);
				case TRACKER:
					return queryData.getTracker(value);
				case FIXED_VERSION:
					return queryData.getVersion(value);
				case AUTHOR:
					return queryData.getPerson(value);
				case ASSIGNED_TO:
					return queryData.getMember(value);
				case CATEGORY:
					return queryData.getCategory(value);
				}
			}
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
		return (titleText != null && titleText.getText().length() > 0);
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
		query.setAttribute(RedmineSearch.PROJECT_ID, "" + search.getProjectId());
		query.setAttribute(RedmineSearch.SEARCH_PARAMS, search.toSearchQueryParam());
		query.setAttribute(RedmineSearch.STORED_QUERY_ID, "" + search.getStoredQueryId());
		if (getFirstSelectedEntry(projectViewer)==null) {
			query.setAttribute(RedmineSearch.DATA_KEY, DATA_KEY_VALUE_CROSS_PROJECT);
		} else {
			query.setAttribute(RedmineSearch.DATA_KEY, getFirstSelectedEntry(projectViewer).getName());
		}
		query.setUrl(search.toQuery());
	}

	private RedmineSearch buildSearch() {
		RedmineSearch search = new RedmineSearch(getTaskRepository().getRepositoryUrl());

		RedmineTicketAttribute project = getFirstSelectedEntry(projectViewer);
		if(project!=null) {
			search.setProjectId(project.getValue());
			
			RedmineTicketAttribute storedQuery = getFirstSelectedEntry(storedQueryViewer);
			if (storedQuery != null) {
				search.setStoredQueryId(storedQuery.getValue());
			}
		}

		
		buildListValueSearchPart(search, searchOperators, lstSearchValues);
		buildTextValueSearchPart(search, searchOperators, txtSearchValues);
		buildListValueSearchPart(search, customSearchOperators, lstCustomSearchValues);
		buildTextValueSearchPart(search, customSearchOperators, txtCustomSearchValues);
		
		
		return search;
	}
	
	protected void buildListValueSearchPart(RedmineSearch search, Map<? extends IRedmineQueryField, ComboViewer> operators, Map<? extends IRedmineQueryField, ListViewer> values) {
		for (Entry<? extends IRedmineQueryField, ListViewer> entry : values.entrySet()) {
			IRedmineQueryField queryField = entry.getKey();
			ListViewer valList = entry.getValue();
			ComboViewer opCombo = operators.get(queryField);
			IStructuredSelection selection = (IStructuredSelection)opCombo.getSelection();
			if (selection.getFirstElement() instanceof CompareOperator) {
				CompareOperator operator = (CompareOperator)selection.getFirstElement();
				
				selection = (IStructuredSelection)valList.getSelection();
				if (selection.isEmpty()) {
					search.addFilter(queryField, operator, "");
				} else {
					Iterator<?> valIterator = selection.iterator();
					while(valIterator.hasNext()) {
						Object obj = valIterator.next();
						if  (obj instanceof RedmineTicketAttribute) {
							RedmineTicketAttribute attribute = 
								(RedmineTicketAttribute)obj;
							search.addFilter(queryField, operator, ""+attribute.getValue());
						} else {
							search.addFilter(queryField, operator, obj.toString());
						}
					}
				}
			}
		}
	}

	protected void buildTextValueSearchPart(RedmineSearch search, Map<? extends IRedmineQueryField, ComboViewer> operators, Map<? extends IRedmineQueryField, Text> values) {
		for (Entry<? extends IRedmineQueryField, Text> entry : values.entrySet()) {
			IRedmineQueryField queryField = entry.getKey();
			Text text = entry.getValue();
			ComboViewer opCombo = operators.get(queryField);
			IStructuredSelection selection = (IStructuredSelection)opCombo.getSelection();
			if (selection.getFirstElement() instanceof CompareOperator) {
				CompareOperator operator = (CompareOperator)selection.getFirstElement();
				if (queryField instanceof RedmineCustomField 
						&& ((RedmineCustomField)queryField).getType()==FieldType.BOOL) {
					search.addFilter(queryField, operator, "1");
				} else {
					search.addFilter(queryField, operator, text.getText().trim());
				}
			}
		}
	}

	@Override
	public String getQueryTitle() {
		return (titleText != null) ? titleText.getText() : "<search>";
	}

	private class ProjectSelectionListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			if (!event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection) {
				
				Object selected = ((IStructuredSelection)event.getSelection()).getFirstElement();
				if (selected instanceof RedmineProject) {
					RedmineProject project = (RedmineProject)selected;

					RedmineQueryPage.this.clearSettings();
					switchOperatorState(true, false);
					RedmineQueryPage.this.updateProjectAttributes(project.getName());
					RedmineQueryPage.this.updateCustomFieldFilter(project.getName());
				} else {
					RedmineQueryPage.this.clearSettings();
					switchOperatorState(false, true);
					RedmineQueryPage.this.updateProjectAttributes(DATA_KEY_VALUE_CROSS_PROJECT);
					RedmineQueryPage.this.updateCustomFieldFilter(DATA_KEY_VALUE_CROSS_PROJECT);
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
