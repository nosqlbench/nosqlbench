#!/usr/bin/env bash

set -x
pwd
export PATH=local/node12/bin:node_modules/nuxt/bin/:$PATH
rm -rf dist .nuxt
if [ -z "$(command -v npm)" ]
then
 echo "The npm command is not installed for this system."
 echo "Attempting to install it under $(pwd)/local/npm12..."
 ./install_npm
fi

if [ -z "$(command -v nodejs)" ] && [ -z "$(command -v node)" ]; then
    echo "Node.js is not installed!"
    exit 1
fi

npm install
npm run generate
rm -rf ../../resources/docsys-guidebook
mv dist ../../resources/docsys-guidebook


