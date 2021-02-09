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


chmod u+x $BASE/src/scripts/*.sh
ln -fs $BASE/src/scripts/hagent.sh $BASE/hagent.sh
ln -fs $BASE/src/target/hagent-jar-with-dependencies.jar $BASE/hagent.jar
ln -fs $BASE/hagent.sh /usr/bin/hagent

cp  $BASE/src/log4j2.xml $BASE/log4j2.xml

rm $BASE/apihagent.sh 2> /dev/null
rm $BASE/apihagent.jar 2> /dev/null

cd $SPWD