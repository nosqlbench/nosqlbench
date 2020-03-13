#!/bin/bash
# update nuxt

GUIDEBOOK="target/guidebook"

if [ ! -f "target/nb.jar" ]
then
 printf "You should not run this unless you have target/nb.jar\n"
 exit 6
fi

if [ ! -d "target/guidebook" ]
then

 pushd ../docsys/src/main/node/docsys
 if ! ./update.sh $@
 then
   printf "Unable to update the guidebook static app\n"
   exit 2;
 fi
 popd
 
 cp -R ../docsys/src/main/resources/docsys-guidebook/ target/guidebook/
else
 printf "target/guidebook exists, not building again until mvn clean\n"
fi

JAVA=$(which java)
JAVA=${JAVA:-$JAVA_HOME/bin/java}

if [ ! -x "$JAVA" ]
then
 printf "Java was not found in the path and JAVA_HOME is not set\n"
 exit 5
fi

$JAVA -jar target/nb.jar docserver generate target/guidebook

#JAVA_HOME=${JAVA_HOME:-JAVA_HOME must be specified if java isn not in the path}
#
## static site for gh pages
#java -jar nb/target/nb.jar docserver generate target/guidebook
