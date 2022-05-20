#!/bin/bash
find . -type f -name '*.java.new' | \
while read newname
do
 original=${newname%%.new}
 mv $newname $original
 printf "moved %s to %s\n" "${newname}" "${original}"
done
