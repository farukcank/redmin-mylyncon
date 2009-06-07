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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.svenk.redmine.core.model.RedmineTicketRelation.RelationType;

public class RelationTypeXmlAdapter extends XmlAdapter<String, RelationType> {

	@Override
	public String marshal(RelationType v) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RelationType unmarshal(String v) throws Exception {
		return RelationType.fromString(v);
	}

}
