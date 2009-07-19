package org.svenk.redmine.core.client.container;


public class Version {

	public Plugin plugin;
	
	public Redmine redmine;
	
	public static class Plugin {
		
		public int major;

		public int minor;

		public String version;
	}
	
	public static class Redmine {

		public int major;

		public int minor;

		public String version;
		
	}
	
	
}
