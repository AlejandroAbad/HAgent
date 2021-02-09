#!/bin/ksh

BASE=/usr/local/hagent
cd $BASE

if [ -d $BASE/src ] 
then
	cd $BASE/src
	git stash >/dev/null 2>/dev/null
	git stash clear >/dev/null 2>/dev/null
	git pull
	mvn package
else
	git clone https://github.com/AlejandroAbad/hagent src
	cd $BASE/src
	mvn package
fi


chmod u+x /usr/local/hagent/src/scripts/hagent.sh
ln -fs /usr/local/hagent/src/scripts/hagent.sh /usr/local/hagent/hagent.sh
ln -fs /usr/local/hagent/src/target/hagent-jar-with-dependencies.jar /usr/local/hagent/hagent.jar
ln -fs /usr/local/hagent/hagent.sh /usr/bin/hagent

cp  /usr/local/hagent/src/log4j2.xml /usr/local/hagent/log4j2.xml

rm /usr/local/hagent/apihagent.sh 2> /dev/null
rm /usr/local/hagent/apihagent.jar 2> /dev/null
