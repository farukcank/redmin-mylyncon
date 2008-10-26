/*******************************************************************************
 *
 * Redmine-Mylyn-Connector
 * 
 * This implementation is on the basis of the implementations of Trac and 
 * Bugzilla emerged and contains parts of source code from these projects.
 * The corresponding copyright notice follows below of this.
 * Copyright (C) 2008  Sven Krzyzak and others
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *  
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.svenk.redmine.core.util.internal;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;

public class RedminePartSource implements PartSource {

	private AbstractTaskAttachmentSource source;
	private String fileName;
	
	public RedminePartSource(AbstractTaskAttachmentSource source, String fileName) {
		this.source = source;
		this.fileName = fileName;
	}
	
	public InputStream createInputStream() throws IOException {
		try {
			return source.createInputStream(null);
		} catch (CoreException e) {
			throw new IOException(e.getMessage());
		}
	}

	public String getFileName() {
		return fileName;
	}

	public long getLength() {
		return source.getLength();
	}

}
