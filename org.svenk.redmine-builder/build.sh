#! /bin/bash

DIR=`echo $0 | sed -e 's/^\(.\+\/\).*$/\1/'`

# Update of build script
svn update ${DIR}

# initial checkout of sources
if [ ! -d ./svnfetch ];then
	svn co https://redmin-mylyncon.svn.sourceforge.net/svnroot/redmin-mylyncon/trunk ${DIR}svnfetch
fi

ant -propertyfile buildserver.properties

