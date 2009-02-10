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

import java.util.StringTokenizer;

public class RedmineTicketRelation extends RedmineTicketAttribute {

	private static final long serialVersionUID = 1L;
	
	public enum RelationType {
		BLOCKS, PRECEDES, DUPLICATES, RELATES;
		
		public static RelationType fromString(String string) {
			for(RelationType type : RelationType.values()) {
				if (type.name().equalsIgnoreCase(string)) {
					return type;
				}
			}
			return null;
		}
	} 

	private int fromTicket;

	private int toTicket;
	
	private RelationType type;

	public RedmineTicketRelation(int id, int fromTicket, int toTicket, String type) {
		super(buildAttributeName(fromTicket, toTicket, type), id);
		this.fromTicket = fromTicket;
		this.toTicket = toTicket;
		this.type = RelationType.fromString(type);
	}

	private static String buildAttributeName(int fromTicket, int toTicket, String type) {
		StringBuilder sb = new StringBuilder();
		sb.append(fromTicket);
		sb.append(';').append(toTicket);
		sb.append(';').append(RelationType.fromString(type).name());
		return sb.toString();
	}
	
	public static RedmineTicketRelation fromAttributeName(int id, String value) {
		RedmineTicketRelation relation = null;
		
		int fromTicket = 0;
		int toTicket = 0;
		String type = null;
		StringTokenizer tok = new StringTokenizer(value, ";");
		try {
			while(tok.hasMoreTokens()) {
				if (fromTicket==0) {
					fromTicket=Integer.parseInt(tok.nextToken());
				} else if (toTicket==0) {
					toTicket=Integer.parseInt(tok.nextToken());
				} else if (type==null) {
					type=tok.nextToken();
					break;
				}
			}
		} catch (NumberFormatException ex) {
		} finally {
			if (type!=null) {
				relation = new RedmineTicketRelation(id, fromTicket, toTicket, type);
			}
		}

		return relation;
	}

	public int getFromTicket() {
		return fromTicket;
	}

	public int getToTicket() {
		return toTicket;
	}

	public RelationType getType() {
		return type;
	} 
}
