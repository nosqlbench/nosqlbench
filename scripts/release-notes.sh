#!/bin/bash
set -e
#RELEASE_NOTES_FILE=${RELEASE_NOTES_FILE:?RELEASE_NOTES_FILE must be provided}

git log --oneline --decorate --max-count=1000 master > /tmp/gitlog_master

readarray lines < /tmp/gitlog_master
for line in "${lines[@]}"
do
 if [[ $line =~ \(tag:\ nosqlbench-[0-9]+\.[0-9]+\.[0-9]+\).+ ]]
 then
#  printf "no more lines after $line" 1>&2
  break
 elif [[ $line =~ \[maven-release-plugin\] ]]
 then
#  printf "maven release plugin, skipping: $line\n" 1>&2
  continue
 elif [[ $line =~ "Merge" ]]
  then
#  printf "merge info, skipping: $line" 1>&2
  continue
 else
  printf -- "- $line"
#  printf "$line" | tee -a ${RELEASE_NOTES_FILE}
 fi
done

