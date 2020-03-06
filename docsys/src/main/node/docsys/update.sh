#!/bin/bash
rm -rf dist .nuxt
npm run generate
rm -rf ../../resources/docsys-guidebook
mv dist ../../resources/docsys-guidebook
