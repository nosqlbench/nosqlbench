#!/usr/bin/env bash

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

APPDIR=target/NB.AppDir
JAR_NAME="nb5.jar"
BIN_NAME="nb5"
JAVA_VERSION="17"


mkdir -p ${APPDIR}


if [ ! -f target/${JAR_NAME} ]
then
 printf "target/${JAR_NAME} does not exist"
 exit 2
fi

rsync -av appimage/skel/ "${APPDIR}/"
cp target/${JAR_NAME} "${APPDIR}/usr/bin/${JAR_NAME}"

mkdir -p "${APPDIR}/usr/bin/jre"

if [ "$JAVA_VERSION" == "17" ]
then
  if [ ! -d "cache/jdk17" ] ; then
    printf "getting jdk17 once into cache/jdk17\n";
    mkdir -p cache
    (cd cache && (
      wget -c https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.1%2B12/OpenJDK17U-jdk_x64_linux_hotspot_17.0.1_12.tar.gz
      tar -xf OpenJDK17U-jdk_x64_linux_hotspot_17.0.1_12.tar.gz
      mv jdk-17.0.1+12 jdk17
      rm OpenJDK17U-jdk_x64_linux_hotspot_17.0.1_12.tar.gz
    ))
  fi
  rsync -av cache/jdk17/ "${APPDIR}/usr/bin/jre/"
else
  printf "Unknown java version indicated in $0"
  exit 2
fi

if [ ! -f "${APPDIR}/AppRun" ]
  then
  ( cd ${APPDIR} && (
   printf "Linking AppRun...\n";
   ln -s usr/bin/${BIN_NAME} AppRun
  ))
fi

printf "getting appimage tool and building image...\n";

( cd target && (
  if [ ! -x "appimagetool-x86_64.AppImage" ]
  then
   wget -c https://github.com/AppImage/AppImageKit/releases/download/12/appimagetool-x86_64.AppImage
   chmod +x appimagetool-x86_64.AppImage
  fi

  ARCH=x86_64 ./appimagetool-x86_64.AppImage NB.AppDir ${BIN_NAME}
  # && chmod +x ${BIN_NAME}
 )
)

if [ -x "target/${BIN_NAME}" ]
then
 printf "nosqlbench AppImage binary was built at target/${BIN_NAME}\n";
fi

