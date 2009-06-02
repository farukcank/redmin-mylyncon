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

import java.util.HashMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.svenk.redmine.core.client.container.CustomValues;
import org.svenk.redmine.core.model.RedmineCustomValue;

public class CustomValueXmlAdapter extends XmlAdapter<CustomValues, HashMap<Integer, String>> {

	@Override
	public CustomValues marshal(HashMap<Integer, String> v) throws Exception {
		return null;
	}

	@Override
	public HashMap<Integer, String> unmarshal(CustomValues v) throws Exception {
		HashMap<Integer, String> map = new HashMap<Integer, String>(v.customValue.size());
		for (RedmineCustomValue value : v.customValue) {
			map.put(value.getCustomFieldId(), value.getValue());
		}
		return map;
	}

}
