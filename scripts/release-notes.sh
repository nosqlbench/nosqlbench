#!/bin/bash
set -e
#RELEASE_NOTES_FILE=${RELEASE_NOTES_FILE:?RELEASE_NOTES_FILE must be provided}

git log --oneline --decorate --max-count=1000 master > /tmp/gitlog_master
(( release_count=0 ))

readarray lines < /tmp/gitlog_master
for line in "${lines[@]}"
do
 if [[ $line =~ \(tag:\ nosqlbench-[0-9]+\.[0-9]+\.[0-9]+\).+ ]]
  then release_count++
  if (( release_count>=2 ))
  then break
  fi
 fi
 then break
 elif [[ $line =~ \[maven-release-plugin\] ]]
 then continue
 elif [[ $line =~ \(.+Merge\ branch ]]
 then continue
 else
  printf "$line"
#  printf "$line" | tee -a ${RELEASE_NOTES_FILE}
 fi
done

