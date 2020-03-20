#!/usr/bin/env bash

set -x

APPDIR=target/NB.AppDir
mkdir -p ${APPDIR}

if [ ! -f target/nb.jar ]
then
 print "target/nb.jar does not exist"
 exit 2
fi

#if [ -x "target/nb" ]
#then
# printf "Removing stale target/nb...\n"
# rm target/nb
#fi

rsync -av appimage/skel/ "${APPDIR}/"
cp target/nb.jar "${APPDIR}/usr/bin/nb.jar"

if [ ! -d "cache/jre" ]
then
  printf "getting jre once into cache/jre\n";
  mkdir -p cache
  (cd cache && (
   wget -c https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jre_x64_linux_hotspot_12.0.2_10.tar.gz
   tar xf OpenJDK12U-jre_x64_linux_hotspot_12.0.2_10.tar.gz
   mv jdk-12.0.2+10-jre jre
   rm OpenJDK12U-jre_x64_linux_hotspot_12.0.2_10.tar.gz
  ))
fi

mkdir -p "${APPDIR}/usr/bin/jre"
rsync -av cache/jre/ "${APPDIR}/usr/bin/jre/"

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

