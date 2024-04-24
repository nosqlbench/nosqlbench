#!/bin/bash
set -e
set -x

NBJAR_VERSION=${NBJAR_VERSION:?NBJAR_VERSION must be specified}
echo "NBJAR_VERSION: ${NBJAR_VERSION}"



cd target
if [ -e "nb.jar" ]
then
 echo "nb.jar link exists, skipping"
 exit 0
fi

for qualifier in jar-with-dependencies
do
  JARNAME="nb-${NBJAR_VERSION}-${qualifier}.jar"
  if [ -e "$JARNAME" ]
  then
     echo "linking $JARNAME to nb.jar"
     ln -s $JARNAME nb.jar
     echo "linked $JARNAME to nb.jar, exiting"
     exit 0
   else
     echo "$JARNAME does not exist, skipping"
  fi
done


echo "Unable to find any jar to link to nb.jar name.."
exit 2

