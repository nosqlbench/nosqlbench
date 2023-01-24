#!/bin/bash
set -e
git log --oneline --decorate --max-count=1000 > /tmp/gitlog.txt

readarray lines < /tmp/gitlog.txt
for line in "${lines[@]}"
do
 printf "line: %s\n" "${line}"
 if [[ $line =~ \(tag:\ ([a-zA-z0-9]+-)?[0-9]+\.[0-9]+\.[0-9]+\)-preview.+ ]]
 then
  echo "PREVIEW"
 elif [[ $line =~ \(tag:\ ([a-zA-Z0-9]+-)?[0-9]+\.[0-9]+\.[0-9]+\).+ ]]
 then
  echo "RELEASE"
#  printf "no more lines after $line" 1>&2
  break
 elif [[ $line =~ \[maven-release-plugin\] ]]
 then
#  printf "maven release plugin, skipping: $line\n" 1>&2
  continue
 elif [[ $line =~ "Merge" ]]
  then
  printf -- "- $line"
#  printf "merge info, skipping: $line" 1>&2
  continue
 else
  printf -- "- $line"
#  printf "$line" | tee -a ${RELEASE_NOTES_FILE}
 fi
done

