#!/bin/bash
set -e
set -x

NBJAR_VERSION=${NBJAR_VERSION:?NBJAR_VERSION must be specified}
echo "NBJAR_VERSION: ${NBJAR_VERSION}"
JARNAME="nb-${NBJAR_VERSION}.jar"
echo "linking $JARNAME to nb.jar"
(cd target ; ln -s $JARNAME nb.jar)

