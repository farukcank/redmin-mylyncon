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
package org.svenk.redmine.ui.wizard;

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
