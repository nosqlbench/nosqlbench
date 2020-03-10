#!/bin/bash
set -x
pwd
export PATH=local/node12/bin:node_modules/nuxt/bin/:$PATH
rm -rf dist .nuxt
if ! which npm >/dev/null 2>&1
then
 printf "The npm command is not installed for this system.\n"
 printf "Attempting to install it under `pwd`/local/npm12..."
 ./install_npm
fi

npm install
npm run generate
rm -rf ../../resources/docsys-guidebook
mv dist ../../resources/docsys-guidebook


