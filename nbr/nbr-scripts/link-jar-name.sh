#!/bin/bash
set -e
set -x

NBJAR_VERSION=${NBJAR_VERSION:?NBJAR_VERSION must be specified}
echo "NBJAR_VERSION: ${NBJAR_VERSION}"



cd target
if [ -e "nbr.jar" ]
then
 echo "nbr.jar link exists, skipping"
 exit 0
fi

for qualifier in jar-with-dependencies
do
  JARNAME="nbr-${NBJAR_VERSION}-${qualifier}.jar"
  if [ -e "$JARNAME" ]
  then
     echo "linking $JARNAME to nbr.jar"
     ln -s $JARNAME nbr.jar
     echo "linked $JARNAME to nbr.jar, exiting"
     exit 0
   else
     echo "$JARNAME does not exist, skipping"
  fi
done


echo "Unable to find any jar to link to nbr.jar name.."
exit 2

