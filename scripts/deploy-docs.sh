#!/bin/bash
set -e
set -x

GIT_RELEASE_BOT_NAME=${GIT_RELEASE_BOT_NAME:?GIT_RELEASE_BOT_NAME must be provided}
GITHUB_SHA=${GITHUB_SHA:?GITHUB_SHA must be provided}
GITHUB_REF=${GITHUB_REF:?GITHUB_REF must be provided}
RELEASE_BRANCH_NAME=${RELEASE_BRANCH_NAME:?RELEASE_BRANCH_NAME must be provided}

git clone https://github.com/nosqlbench/nosqlbench-docs.git nosqlbench-docs
pushd nosqlbench-docs
rm -rf docs
unzip ../docs.zip 
git add docs
git commit -m'docs update'
git push

exit 0

