package org.svenk.redmine.ui.wizard.querypage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.svenk.redmine.core.RedmineProjectData;
import org.svenk.redmine.core.model.RedmineTicketAttribute;

public class RedmineContentProvider implements IStructuredContentProvider {

	String title;
	
	public RedmineContentProvider(String title) {
		this.title = title;
	}

	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			List tmp = (List)inputElement;
			if (title!=null) {
				tmp = new ArrayList<Object>(tmp.size()+1);
				tmp.add(title);
				tmp.addAll((List)inputElement);
			}
			return tmp.toArray();
		}
		return null;
	}

	public void dispose() {
	}

	public void inputChanged(final Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput==null || newInput==null) {
			return;
		}
		
		Object o = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
		if ((o instanceof RedmineTicketAttribute || o instanceof RedmineProjectData)) {
			selectLastOrDefault(viewer, o);
		} else if (title!=null) {
			selectLastOrDefault(viewer, title);
		}
		
	}
	
	private void selectLastOrDefault(final Viewer viewer, final Object item) {
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				viewer.setSelection(new StructuredSelection(item), true);
			}
		});
	}
}
