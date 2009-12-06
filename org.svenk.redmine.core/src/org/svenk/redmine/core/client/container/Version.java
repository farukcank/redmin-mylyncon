package org.svenk.redmine.core.client.container;



public class Version {

	public Plugin plugin;
	
	public Redmine redmine;
	
	public enum Release {
		ZEROEIGHT(0, 8),
		ZEROEIGHTSEVEN(0, 8, 7);
		
		public final int major;
		public final int minor;
		public final int tiny;

		Release(int major, int minor) {
			this(major, minor, 0);
		}

		Release(int major, int minor, int tiny) {
			this.major = major;
			this.minor = minor;
			this.tiny = tiny;
		}
	}
	
	public static class Plugin {
		
		public int major;

		public int minor;
		
		public int tiny;

		public String version;
	}
	
	public static class Redmine implements Comparable<Release> {

		public int major;

		public int minor;
		
		public int tiny;

		public String version;
		
		public static Redmine fromString(String globalVersionString) {
			//0.8.4.stable.3069v2.6
			Redmine r = null;
			try {
				String[] parts = globalVersionString.split("\\.");
				if (parts!=null && parts.length>=3) {
					r = new Redmine();
					r.major = Integer.parseInt(parts[0]);
					r.minor = Integer.parseInt(parts[1]);
					r.tiny = Integer.parseInt(parts[2]);
					return r;
				}
			} catch (NumberFormatException e) {
				r = null;
			}
			return r;
		}

		public int compareTo(Release release) {
			if (major<release.major) {
				return -1;
			}
			if (major>release.major) {
				return 1;
			}
			if (minor<release.minor) {
				return -1;
			}
			if (minor>release.minor) {
				return 1;
			}
			if (tiny<release.tiny) {
				return -1;
			}
			if (tiny>release.tiny) {
				return 1;
			}
			return 0;
		}

		
	}
	
	
}
