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
package org.svenk.redmine.core.client.adapter;

import java.lang.reflect.Field;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.svenk.redmine.core.RedmineProjectData;
import org.svenk.redmine.core.client.container.ProjectDataWrapper;
import org.svenk.redmine.core.model.RedmineProject;

public class ProjectDataXmlAdapter extends
		XmlAdapter<ProjectDataWrapper, org.svenk.redmine.core.RedmineProjectData> {

	@Override
	public ProjectDataWrapper marshal(RedmineProjectData v) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RedmineProjectData unmarshal(ProjectDataWrapper v) throws Exception {
		// TODO Auto-generated method stub
		RedmineProject project = new RedmineProject(v.name, v.id);
		project.setIssueEditAllowed(v.issueEditAllowed);
		RedmineProjectData data = new RedmineProjectData(project);
		
		String[] member = new String[]{"trackers", "categorys", "versions", "members", "customTicketFields", "storedQueries"};
		Field field;
		Class<RedmineProjectData> clazz = (Class<RedmineProjectData>)data.getClass();
		for (String m : member) {
			field = clazz.getDeclaredField(m);
			field.setAccessible(true);
			field.set(data, field.get(v));
		}
		
		
		return data;
	}

}
