#!/bin/bash
#
# Copyright (c) 2023 nosqlbench
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

export branch=$(git status --branch --porcelain | cut -d' ' -f2)

if [[ $branch =~ main ]]
then
 printf "On branch main, continuing\n" 1>&2
else
 printf "Not branch main, bailing out\n" 1>&2
 exit 2
fi

export REVISION=$(mvn help:evaluate -Dexpression=revision -q -DforceStdout)
if [[ $REVISION =~ ([0-9]+)\.([0-9]+)\.([0-9]+)-SNAPSHOT ]]
then
 printf "The revision matches the format, continuing\n" 1>&2
 set -- "${BASH_REMATCH[@]}"
 VERSION_STRING="${@:2:3}"
else
 printf "The revision format for '${REVISION}' does not match #.#.#-SNAPSHOT form. bailing out\n"
 exit 3
fi

export TAG=$(git describe --exact-match --tags)
if [[ $TAG =~ ([0-9]+)\.([0-9]+)\.([0-9]+)(-preview)? ]]
then
 printf "The tag format matches the version, continuing\n" 1>&2
 set -- "${BASH_REMATCH[@]}"
 TAG_STRING="${@:2:3}"
else
 printf "The tag format for '${TAG}' does not match #.#.#-preview form. bailing out\n" 1>&2
 exit 4
fi

printf "version(${VERSION_STRING}) tag(${TAG_STRING})\n" 1>&2

if [ "${VERSION_STRING}" == "${TAG_STRING}" ]
then
 printf "version and tag match, continuing\n" 1>&2
else
 printf "version and tag do not match: bailing out\n" 1>&2
 exit 5
fi

printf "%s.%s.%s-preview\n" "${@:2:3}"


