#!/bin/bash
#
# Copyright (c) 2022 nosqlbench
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e
set -x

NBJAR_VERSION=${NBJAR_VERSION:?NBJAR_VERSION must be specified}
JAR_NAME="nb5.jar"
BIN_NAME="nb5"

echo "NBJAR_VERSION: ${NBJAR_VERSION}"
echo "NBJAR_NAME: ${JAR_NAME}"


cd target
if [ -e "${JAR_NAME}" ]
then
 echo "${JAR_NAME} link exists, skipping"
 exit 0
fi

for qualifier in jar-with-dependencies
do
  FULL_JAR_NAME="${BIN_NAME}-${NBJAR_VERSION}-${qualifier}.jar"
  if [ -e "$FULL_JAR_NAME" ]
  then
     echo "linking $FULL_JAR_NAME to ${BIN_NAME}.jar"
     ln -s $FULL_JAR_NAME $JAR_NAME
     echo "linked $FULL_JAR_NAME to $JAR_NAME, exiting"
     exit 0
   else
     echo "$FULL_JAR_NAME does not exist, skipping"
  fi
done


echo "Unable to find any jar to link to ${JAR_NAME} name.."
exit 2

