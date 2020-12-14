#!/bin/bash
set -e
set -x

GIT_RELEASE_BOT_NAME=${GIT_RELEASE_BOT_NAME:?GIT_RELEASE_BOT_NAME must be provided}
GITHUB_SHA=${GITHUB_SHA:?GITHUB_SHA must be provided}
GITHUB_REF=${GITHUB_REF:?GITHUB_REF must be provided}
RELEASE_BRANCH_NAME=${RELEASE_BRANCH_NAME:?RELEASE_BRANCH_NAME must be provided}
PRERELEASE_BRANCH_PATTERN=${PRERELEASE_BRANCH_PATTERN:?PRERELEASE_BRANCH_PATTERN must be provided}

#git rev-parse --abbrev-ref HEAD

# Filter the branch to execute the release on
readonly local current_branch=$(git rev-parse --abbrev-ref HEAD)
echo "Current branch: ${current_branch}"

if   [[ -n "${current_branch}" && "${current_branch}" == *"${RELEASE_BRANCH_PATTERN}"* ]]; then
    echo "Building for release branch $RELEASE_BRANCH_NAME"
elif [[ -n "${current_branch}" && "${current_branch}" == *"${PRERELEASE_BRANCH_PATTERN}"* ]]; then
    echo "Building prerelease for branch $RELEASE_BRANCH_NAME"
else 
     echo "Skipping for ${current_branch} branch"
     exit 0
fi

# Making sure we are on top of the branch
echo "Git checkout branch ${GITHUB_REF##*/}"
git checkout ${GITHUB_REF##*/}
echo "Git reset hard to ${GITHUB_SHA}"
git reset --hard ${GITHUB_SHA}

# Do the release
echo "Do mvn release:prepare..."
#mvn $MAVEN_REPO_LOCAL --batch-mode --global-settings release.xml -Dusername=$GITHUB_ACCESS_TOKEN release:prepare
mvn --batch-mode --global-settings release.xml -Dusername=$GITHUB_ACCESS_TOKEN clean release:prepare -DdevelopmentVersion=${NEXT_SNAPSHOT} -DreleaseVersion=${RELEASE_VERSION}

echo "files after release:prepare..."
pwd
ls -l
