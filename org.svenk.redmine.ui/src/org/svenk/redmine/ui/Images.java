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
package org.svenk.redmine.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class Images {

	private static ImageRegistry registry;
	
	public final static String FIND_CLEAR = "/icons/etool16/clear.gif";

	public static ImageDescriptor getImageDescriptor(String key) {
		ImageDescriptor imageDescriptor = RedmineUiPlugin.imageDescriptorFromPlugin("org.eclipse.mylyn.commons.ui", "/icons/etool16/clear.gif");
		return imageDescriptor;
	}

	public static Image getImage(String key) {
		ImageDescriptor descriptor = getImageDescriptor(key);
		return descriptor==null ? null : getImage(descriptor);
	}
	
	public static Image getImage(ImageDescriptor descriptor) {
		if (descriptor==null) {
			return null;
		}
		
		Image image = getRegistry().get("" + descriptor.hashCode());
		if(image==null) {
			image = descriptor.createImage();
			getRegistry().put("" + descriptor.hashCode(), image);
		}
		
		return image;
	}
	
	private static ImageRegistry getRegistry() {
		if(registry==null) {
			registry = new ImageRegistry();
		}
		return registry;
	}
}
