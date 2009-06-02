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
package org.svenk.redmine.core.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class RedmineTicketAttribute implements Comparable<RedmineTicketAttribute>, Serializable {

	private static final long serialVersionUID = 2L;

	@XmlAttribute(name="id")
	private int value;

	@XmlElement
	private String name;

	protected RedmineTicketAttribute(){} // required for JAXB
	
	public RedmineTicketAttribute(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public int compareTo(RedmineTicketAttribute o) {
		return value - o.value;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj.getClass().equals(getClass()) && ((RedmineTicketAttribute)obj).getValue()==getValue();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + value;
		return hash;
	}

}
