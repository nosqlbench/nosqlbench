#!/bin/bash
rm -rf dist .nuxt
npm run generate
rm -rf ../../resources/docsys-guidebook
cp -r dist ../../resources/docsys-guidebook

# static site for gh pages
mkdir dist/services
mkdir dist/services/docs/
mkdir dist/services/docs/markdown/
find ../../../../../engine-docs/src/main/resources/docs-for-nb/ -name "*.md" | sed 's:../../../../../engine-docs/src/main/resources/docs-for-nb/::g'  > dist/services/docs/markdown.csv
cp -R ../../../../../engine-docs/src/main/resources/docs-for-nb/* dist/services/docs/markdown/
