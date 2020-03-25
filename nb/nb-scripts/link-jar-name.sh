#!/bin/bash
set -e
set -x

NBJAR_VERSION=${NBJAR_VERSION:?NBJAR_VERSION must be specified}
echo "NBJAR_VERSION: ${NBJAR_VERSION}"
JARNAME="nb-${NBJAR_VERSION}.jar"
cd target
if [ -e "nb.jar" ]
then
 echo "nb.jar link exists, skipping"
 exit 0
fi
if [ ! -e "$JARNAME" ]
then
 echo "$JARNAME does not exist, skipping"
 exit 0
fi

echo "linking $JARNAME to nb.jar"
ln -s $JARNAME nb.jar
echo "linked $JARNAME to nb.jar, exiting"

