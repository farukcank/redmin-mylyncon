package org.svenk.redmine.ui.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.svenk.redmine.core.IRedmineClient;
import org.svenk.redmine.core.RedmineClientData;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.RedmineProjectData;
import org.svenk.redmine.core.RedmineRepositoryConnector;
import org.svenk.redmine.core.exception.RedmineException;

public class RedmineProjectPage extends WizardPage {

	private final static String PAGE_NAME = "ProjectSelection";

	private final static String PAGE_TITLE = "Select a project";

	private final static String PAGE_DESCRIPTION = "Please select the project, that will be assigned to the new ticket";
	
	private final static String UDDATE_LABEL = "Update attributes from repository";
	
	private TaskRepository taskRepository;
	
	private RedmineClientData clientData;
	
	private List projectList;
	
	private Button updateButton;
	
	public RedmineProjectPage(TaskRepository taskRepository) {
		super(PAGE_NAME);
		setTitle(PAGE_TITLE);
		setDescription(PAGE_DESCRIPTION);
		this.taskRepository = taskRepository;
		
	}

	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		control.setLayoutData(gd);
		GridLayout layout = new GridLayout(1, false);
		control.setLayout(layout);

		projectList = new List(control, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		projectList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		projectList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RedmineProjectPage.this.setPageComplete(RedmineProjectPage.this.isPageComplete());
			}
		});
		
		updateButton = new Button(control, SWT.PUSH);
		updateButton.setText(UDDATE_LABEL);
		updateButton.setLayoutData(new GridData());
		updateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (RedmineProjectPage.this.taskRepository != null) {
					updateProjectsFromRepository(true);
				} else {
					MessageDialog
							.openInformation(Display.getCurrent()
									.getActiveShell(),
									"Update Attributes Failed",
									"No repository available, please add one using the Task Repositories view.");
				}
			}
		});

		
		setControl(control);

	}

	@Override
	public boolean isPageComplete() {
		return projectList.getSelectionCount()==1;
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (clientData == null) {
						if (getControl() != null && !getControl().isDisposed()) {
							updateProjectsFromRepository(false);
						}
					}
				}
			});
		}
	}

	public String getSelectedProjectName() {
		return projectList.getItem(projectList.getSelectionIndex());
	}
	
	private void updateProjectsFromRepository(final boolean force) {
		RedmineRepositoryConnector connector = (RedmineRepositoryConnector) TasksUi
				.getRepositoryManager().getRepositoryConnector(
						RedmineCorePlugin.REPOSITORY_KIND);
		final IRedmineClient client = connector.getClientManager()
				.getRedmineClient(taskRepository);

		if (force || !client.hasAttributes()) {
			try {
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {
						try {
							client.updateAttributes(monitor, force);
						} catch (RedmineException e) {
							throw new InvocationTargetException(e);
						}
					}
				};
	
				if (getContainer() != null) {
					getContainer().run(true, true, runnable);
				} else {
					IProgressService service = PlatformUI.getWorkbench()
							.getProgressService();
					service.busyCursorWhile(runnable);
				}
			} catch (InvocationTargetException e) {
				setErrorMessage(RedmineCorePlugin.toStatus(e.getCause(),
						taskRepository).getMessage());
				return;
			} catch (InterruptedException e) {
				return;
			}
		}

		clientData = client.getClientData();

		String selectedValue = projectList.getSelectionIndex()>-1 ? projectList.getItem(projectList.getSelectionIndex()) : "";
		projectList.removeAll();
		for (RedmineProjectData projectData : clientData.getProjects()) {
			projectList.add(projectData.getProject().getName());
			if (projectData.getProject().getName().equals(selectedValue)) {
				projectList.select(projectList.getItemCount()-1);
			}
		}
	}

}
