#!/bin/ksh

# Para evitar error en la ejecución de GIT
LIBPATH=

SPWD=$(pwd)
BASE=/usr/local/hagent

cd $BASE


# Instalación de GIT
if [ ! -e /usr/bin/git ]
then
	/usr/lpp/bos.sysmgt/nim/methods/c_sm_nim cust  -l 'GIT' -o 'aixtools.git' -f '' -f '' -f 'Y' -f '' -f '' -f 'g' -f 'X' -f '' -f ''
	ln -fs /opt/bin/git /usr/bin/git
fi

# Instalación de MAVEN
if [ ! -e /usr/bin/mvn ]
then
	mkdir /usr/local/maven
	scp -r nim:/export/nim/lpp_source/PRODUCTS/apache-maven-3.6.3 /usr/local/maven
	ln -fs /usr/local/maven/apache-maven-3.6.3 /usr/local/maven/current
	ln -fs /usr/local/maven/current/bin/mvn /usr/bin/mvn
fi




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

cd $SPWD