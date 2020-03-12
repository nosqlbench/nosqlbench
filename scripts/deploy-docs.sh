#!/bin/bash
set -e
set -x

NBDROID_TOKEN=${NBDROID_TOKEN:?NBDROID_TOKEN must be provided}
NBDROID_NAME=${NBDROID_NAME:?NBDROID_NAME must be provided}

#GITHUB_REF=${GITHUB_REF:?GITHUB_REF must be provided}
#RELEASE_BRANCH_NAME=${RELEASE_BRANCH_NAME:?RELEASE_BRANCH_NAME must be provided}

f26313be9720eef77f85f1d384650229213ee22a

git clone https://${NBDROID_NAME}:${NBDROID_TOKEN}@github.com/nosqlbench/nosqlbench-docs.git nosqlbench-docs
pushd nosqlbench-docs
rm -rf docs
unzip ../docs.zip 
git add docs
git commit -m'docs update'
git push

