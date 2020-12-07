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
JAVA_VERSION="14"

mkdir -p "${APPDIR}/usr/bin/jre"

if [ "$JAVA_VERSION" == "15" ]
then
  if [ ! -d "cache/jre15" ] ; then
    printf "getting jre once into cache/jre15\n";
    mkdir -p cache
    (cd cache && (
     if [ "$BUILD_OPENJ9" = "true" ]
     then
      wget -c wget -c https://github.com/AdoptOpenJDK/openjdk15-binaries/releases/download/jdk15u-2020-11-19-07-04/OpenJDK15U-jre_x64_linux_openj9_linuxXL_2020-11-19-07-04.tar.gz
      tar -xf OpenJDK15U-jre_x64_linux_openj9_linuxXL_2020-11-19-07-04.tar.gz
      mv jdk-15.0.1+9-jre jre15
      rm OpenJDK15U-jre_x64_linux_openj9_linuxXL_2020-11-19-07-04.tar.gz
     else
      wget -c https://github.com/AdoptOpenJDK/openjdk15-binaries/releases/download/jdk15u-2020-11-19-07-04/OpenJDK15U-jre_x64_linux_hotspot_2020-11-19-07-04.tar.gz
      tar xf OpenJDK15U-jre_x64_linux_hotspot_2020-11-19-07-04.tar.gz
      mv jdk-15.0.1+9-jre jre15
     fi
    ))
  fi
  rsync -av cache/jre15/ "${APPDIR}/usr/bin/jre/"
# Java 14 should run binaries targeted to Java 11 bytecode
elif [ "$JAVA_VERSION" == "14" ] ; then
  if [ ! -d "cache/jre14" ] ; then
    printf "getting jre once into cache/jre14\n";
    mkdir -p cache
    (cd cache && (
     if [ "$BUILD_OPENJ9" = "true" ]
     then
      wget -c https://github.com/AdoptOpenJDK/openjdk14-binaries/releases/download/jdk14u-2020-04-27-07-27/OpenJDK14U-jre_x64_linux_openj9_linuxXL_2020-04-27-07-27.tar.gz
      tar xf OpenJDK14U-jre_x64_linux_openj9_linuxXL_2020-04-27-07-27.tar.gz
      mv jdk-14.0.1+7-jre jre14
      rm OpenJDK14U-jre_x64_linux_openj9_linuxXL_2020-04-27-07-27.tar.gz
     else
      wget -c https://github.com/AdoptOpenJDK/openjdk14-binaries/releases/download/jdk14u-2020-04-27-07-27/OpenJDK14U-jre_x64_linux_hotspot_2020-04-27-07-27.tar.gz
      tar xf OpenJDK14U-jre_x64_linux_hotspot_2020-04-27-07-27.tar.gz
      mv jdk-14.0.1+7-jre jre14
     fi
    ))
  fi
  rsync -av cache/jre14/ "${APPDIR}/usr/bin/jre/"
else
  printf "Unknown java version indicated in $0"
  exit 2
   # wget -c https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jre_x64_linux_hotspot_12.0.2_10.tar.gz
   # tar xf OpenJDK12U-jre_x64_linux_hotspot_12.0.2_10.tar.gz
   # mv jdk-12.0.2+10-jre jre
   # rm OpenJDK12U-jre_x64_linux_hotspot_12.0.2_10.tar.gz
fi



if [ ! -f "${APPDIR}/AppRun" ]
  then
  ( cd ${APPDIR} && (
   printf "Linking AppRun...\n";
   ln -s usr/bin/nb AppRun
  ))
fi

#( cd ${APPDIR} && (
#  rsync -av ..
#  if [ ! -d "usr/bin/jre" ]
#  then
#   printf "getting jre...\n";
#
#   # JRE 12
#   wget -c https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jre_x64_linux_hotspot_12.0.2_10.tar.gz
#   tar xf OpenJDK12U-jre_x64_linux_hotspot_12.0.2_10.tar.gz
#   mv jdk-12.0.2+10-jre usr/bin/jre
#   rm OpenJDK12U-jre_x64_linux_hotspot_12.0.2_10.tar.gz
#
#   # JRE 13
#   # wget -c https://github.com/AdoptOpenJDK/openjdk13-binaries/releases/download/jdk-13%2B33/OpenJDK13U-jre_x64_linux_hotspot_13_33.tar.gz
#   # tar xf OpenJDK13U-jre_x64_linux_hotspot_13_33.tar.gz
#   #mv jdk-13+33-jre usr/bin/jre
#   #rm OpenJDK13U-jre_x64_linux_hotspot_13_33.tar.gz
#  else
#   printf "jre directory present, skipping...\n";
#  fi
#
#  if [ -f "AppRun" ]
#  then
#   printf "Removing stale AppRun...\n";
#   rm AppRun
#  fi
#
#  if [ ! -f "AppRun" ]
#  then
#   printf "Linking AppRun...\n";
#   ln -s usr/bin/nb AppRun
#  fi
#
# )
#)

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

