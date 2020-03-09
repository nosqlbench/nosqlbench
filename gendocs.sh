#!/bin/bash
# update nuxt

GUIDEBOOK="nb/target"

if [ ! -d "nb/target" ]
then
 printf "You should not run this unless you have an nb/target directory.\n"
 printf "It depends on the Java components to be built first.\n"
 exit 6
fi

if [ ! -d "nb/target/guidebook" ]
then

 pushd docsys/src/main/node/docsys
 if ! ./update.sh $@
 then
   printf "Unable to update the guidebook static app\n"
   exit 2;
 fi
 
 popd
 
 printf "PWD: %s\n" $(pwd)
 
 cp -R docsys/src/main/resources/docsys-guidebook/ nb/target/guidebook/
else
 printf "nb/target/guidebook exists, not building again until mvn clean\n"
fi

JAVA=$(which java)
JAVA=${JAVA:-$JAVA_HOME/bin/java}

if [ ! -x "$JAVA" ]
then
 printf "Java was not found in the path and JAVA_HOME is not set\n"
 exit 5
fi

$JAVA -jar nb/target/nb.jar docserver generate nb/target/guidebook

#JAVA_HOME=${JAVA_HOME:-JAVA_HOME must be specified if java isn not in the path}
#
## static site for gh pages
#java -jar nb/target/nb.jar docserver generate target/guidebook
