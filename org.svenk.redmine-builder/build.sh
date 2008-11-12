#! /bin/bash

DIR=`echo $0 | sed -e 's/^\(.\+\/\).*$/\1/'`

if [ ! -d ./svnfetch ];then
	svn co https://redmin-mylyncon.svn.sourceforge.net/svnroot/redmin-mylyncon/trunk ${DIR}svnfetch
fi

ant -propertyfile buildserver.properties

