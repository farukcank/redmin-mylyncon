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
package org.svenk.redmine.core.client;


import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.svenk.redmine.core.client.container.Priorities;
import org.svenk.redmine.core.client.container.ProjectsData;
import org.svenk.redmine.core.client.container.TicketStatuses;
import org.svenk.redmine.core.client.container.Tickets;
import org.svenk.redmine.core.client.container.UpdatedSince;
import org.svenk.redmine.core.client.container.Version;
import org.svenk.redmine.core.exception.RedmineException;
import org.svenk.redmine.core.model.RedminePriority;
import org.svenk.redmine.core.model.RedmineTicket;
import org.svenk.redmine.core.model.RedmineTicketStatus;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class RedmineRestfulReader {
	
	private JAXBContext ctx;
	
	private SAXParserFactory parserFactory;
	
	public RedmineTicket readTicket(InputStream in) throws RedmineException {
		try {
			Source source = new SAXSource(newReader(), new InputSource(in));
			Object o = newUnmarshallerContext().unmarshal(source);
			return (RedmineTicket)o;
		} catch (Exception e) {
			throw new RedmineException(e.getMessage(), e);
		}
	}

	public List<RedmineTicket> readTickets(InputStream in) throws RedmineException {
		try {
			Source source = new SAXSource(newReader(), new InputSource(in));
			Object o = newUnmarshallerContext().unmarshal(source);
			return ((Tickets)o).tickets;
		} catch (Exception e) {
			throw new RedmineException(e.getMessage(), e);
		}
	}

	public List<Integer> readUpdatedTickets(InputStream in) throws RedmineException {
		try {
			Source source = new SAXSource(newReader(), new InputSource(in));
			Object o = newUnmarshallerContext().unmarshal(source);
			return ((UpdatedSince)o).idList;
		} catch (Exception e) {
			throw new RedmineException(e.getMessage(), e);
		}
	}

	public List<RedmineTicketStatus> readTicketStatuses(InputStream in) throws RedmineException {
		try {
			Source source = new SAXSource(newReader(), new InputSource(in));
			Object o = newUnmarshallerContext().unmarshal(source);
			return ((TicketStatuses)o).statuses;
		} catch (Exception e) {
			throw new RedmineException(e.getMessage(), e);
		}
	}

	public List<RedminePriority> readPriorities(InputStream in) throws RedmineException {
		try {
			Source source = new SAXSource(newReader(), new InputSource(in));
			Object o = newUnmarshallerContext().unmarshal(source);
			return ((Priorities)o).priorities;
		} catch (Exception e) {
			throw new RedmineException(e.getMessage(), e);
		}
	}

	public List<RedmineProjectData> readProjects(InputStream in) throws RedmineException {
		try {
			Source source = new SAXSource(newReader(), new InputSource(in));
			Object o = newUnmarshallerContext().unmarshal(source);
			return ((ProjectsData)o).project;
		} catch (Exception e) {
			throw new RedmineException(e.getMessage(), e);
		}
	}

	public Version readVersion(InputStream in) throws RedmineException {
		try {
			Source source = new SAXSource(newReader(), new InputSource(in));
			return (Version)newUnmarshallerContext().unmarshal(source);
		} catch (Exception e) {
			throw new RedmineException(e.getMessage(), e);
		}
	}
	
	protected Unmarshaller newUnmarshallerContext() throws JAXBException {
		if (ctx==null) {
			ctx = JAXBContext.newInstance(RedmineTicket.class, Tickets.class, UpdatedSince.class, TicketStatuses.class, Priorities.class, ProjectsData.class, Version.class);
		}
		return ctx.createUnmarshaller();
	}
	
	protected XMLReader newReader() throws SAXException, ParserConfigurationException {
		if (parserFactory==null) {
			parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(false);
		}
		return parserFactory.newSAXParser().getXMLReader();
	}
}
