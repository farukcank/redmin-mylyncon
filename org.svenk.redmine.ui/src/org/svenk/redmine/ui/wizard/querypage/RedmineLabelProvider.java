package org.svenk.redmine.ui.wizard.querypage;

import org.eclipse.jface.viewers.LabelProvider;
import org.svenk.redmine.core.RedmineProjectData;
import org.svenk.redmine.core.model.RedmineTicketAttribute;

public class RedmineLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof RedmineProjectData) {
			return ((RedmineProjectData)element).getProject().getName();
		} else if (element instanceof RedmineTicketAttribute) {
			return ((RedmineTicketAttribute)element).getName();
		}
		return super.getText(element);
	}

}
