package org.svenk.redmine.core.client.container;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="version")
public class Version {

	@XmlElement
	public Plugin plugin;
	
	@XmlElement
	public Redmine redmine;
	
	public static class Plugin {
		
		@XmlAttribute
		public int major;

		@XmlAttribute
		public int minor;

		@XmlValue
		public String version;
	}
	
	public static class Redmine {

		@XmlTransient
		public int major;

		@XmlTransient
		public int minor;

		@XmlTransient
		public String version;
		
		@XmlValue
		private void setValue(String value) {
			version = value;
			String[] parts = value.split("\\.");
			
			major = Integer.parseInt(parts[0]);
			minor = Integer.parseInt(parts[1]);
		}
		
	}
	
	
}
