#! /bin/bash


svn co https://redmin-mylyncon.svn.sourceforge.net/svnroot/redmin-mylyncon/trunk ./svnfetch

java -jar /home/sven/bin/eclipseSDK/plugins/org.eclipse.equinox.launcher_1.0.101.R34x_v20080819.jar -application org.eclipse.ant.core.antRunner -buildfile /home/sven/bin/eclipseSDK/plugins/org.eclipse.pde.build_3.4.1.R34x_v20080805/scripts/build.xml -Dbuilder=/home/sven/Workspaces/rcp/org.svenk.redmine-builder/builder