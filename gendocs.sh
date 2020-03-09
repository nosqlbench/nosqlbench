#!/bin/bash
# update nuxt
pushd docsys/src/main/node/docsys
if ! ./update.sh
then
  printf "Unable to update the guidebook static app\n"
  exit 2;
fi

popd

pwd
cp -R docsys/src/main/resources/docsys-guidebook/ nb/target/guidebook/

# static site for gh pages
java -jar nb/target/nb.jar docserver generate target/guidebook
