#!/usr/bin/env bash

#
# Copyright (c) nosqlbench
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
JAVA_VERSION="23"
UNPACKED_NAME="jdk-23.0.1"


mkdir -p ${APPDIR}


if [ ! -f target/${JAR_NAME} ]
then
 printf "target/${JAR_NAME} does not exist"
 exit 2
fi

rsync -av nb-appimage/skel/ "${APPDIR}/"
cp target/${JAR_NAME} "${APPDIR}/usr/bin/${JAR_NAME}"

mkdir -p "${APPDIR}/usr/bin/jre"
jdkname="jdk${JAVA_VERSION}"
if [ "${jdkname}" == "jdk23" ]
then
  if [ ! -d "cache/${jdkname}" ] ; then
    printf "getting ${jdkname} once into cache/${jdkname}\n";
    filename='openjdk-23.0.1_linux-x64_bin.tar.gz'
    mkdir -p cache
    (cd cache && (
      curl -O https://download.java.net/java/GA/jdk23.0.1/c28985cbf10d4e648e4004050f8781aa/11/GPL/${filename}
      tar -xf ${filename}
      mv ${UNPACKED_NAME} jdk23
      rm ${filename}
    ))
  fi
  rsync -av cache/jdk23/ "${APPDIR}/usr/bin/jre/"
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

  # note if your linux has errors with the following then see https://docs.appimage.org/user-guide/troubleshooting/fuse.html
  ARCH=x86_64 ./appimagetool-x86_64.AppImage NB.AppDir ${BIN_NAME}
  # && chmod +x ${BIN_NAME}
 )
)

if [ -x "target/${BIN_NAME}" ]
then
 printf "nosqlbench AppImage binary was built at target/${BIN_NAME}\n";
fi

