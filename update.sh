#!/bin/bash
# update nuxt
pushd docsys/src/main/node/docsys
./update.sh
popd

pwd
cp -R docsys/src/main/resources/docsys-guidebook/* docs/

# static site for gh pages
java -jar nb/target/nb.jar docserver generate
