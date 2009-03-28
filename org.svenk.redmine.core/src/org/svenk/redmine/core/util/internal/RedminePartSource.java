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
