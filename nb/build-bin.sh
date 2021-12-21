#!/usr/bin/env bash

set -e
set -x

APPDIR=target/NB.AppDir
mkdir -p ${APPDIR}


BUILD_OPENJ9="false"
if [ "$1" = "--with-openj9" ]
then
 BUILD_OPENJ9="true"
 printf "using openj9 for build\n"
 shift;
fi

if [ ! -f target/nb.jar ]
then
 printf "target/nb.jar does not exist"
 exit 2
fi

#if [ -x "target/nb" ]
#then
# printf "Removing stale target/nb...\n"
# rm target/nb
#fi

rsync -av appimage/skel/ "${APPDIR}/"
cp target/nb.jar "${APPDIR}/usr/bin/nb.jar"
JAVA_VERSION="17"

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
   ln -s usr/bin/nb AppRun
  ))
fi

printf "getting appimage tool and building image...\n";

( cd target && (
  if [ ! -x "appimagetool-x86_64.AppImage" ]
  then
   wget -c https://github.com/AppImage/AppImageKit/releases/download/12/appimagetool-x86_64.AppImage
   chmod +x appimagetool-x86_64.AppImage
  fi

  ARCH=x86_64 ./appimagetool-x86_64.AppImage NB.AppDir nb
  # && chmod +x nb
 )
)

if [ -x "target/nb" ]
then
 printf "nosqlbench AppImage binary was built at target/nb\n";
fi

