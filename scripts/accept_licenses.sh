#!/bin/bash
find . -type f -name '*.java.new' | \
while read newname
do
 original=${newname%%.new}
 printf "newname %s  original %s\n" "${newname}" "${original}"
 mv $newname $original
done
