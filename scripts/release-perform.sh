#!/bin/bash
set -e
set -x

GIT_RELEASE_BOT_NAME=${GIT_RELEASE_BOT_NAME:?GIT_RELEASE_BOT_NAME must be provided}
GITHUB_SHA=${GITHUB_SHA:?GITHUB_SHA must be provided}
GITHUB_REF=${GITHUB_REF:?GITHUB_REF must be provided}
RELEASE_BRANCH_PATTERN=${RELEASE_BRANCH_PATTERN:?RELEASE_BRANCH_PATTERN must be provided}
PRERELEASE_BRANCH_PATTERN=${PRERELEASE_BRANCH_PATTERN:?PRERELEASE_BRANCH_PATTERN must be provided}

#MAVEN_LOCAL_REPO_PATH=${MAVEN_LOCAL_REPO_PATH:?MAVEN_LOCAL_REPO_PATH must be provided}
#MAVEN_REPO_LOCAL=${MAVEN_REPO_LOCAL:?MAVEN_REPO_LOCAL must be provided}
#MAVEN_ARGS=${MAVEN_ARGS:?MAVEN_ARGS must be provided}

# avoid the release loop by checking if the latest commit is a release commit
readonly local last_release_commit_hash=$(git log --pretty=format:"%H" -1)
echo "Last $GIT_RELEASE_BOT_NAME commit: ${last_release_commit_hash}"
echo "Current commit: ${GITHUB_SHA}"
if [[ "${last_release_commit_hash}" = "${GITHUB_SHA}" ]]; then
     echo "Skipping for $GIT_RELEASE_BOT_NAME commit"
     exit 0
fi

# Filter the branch to execute the release on
readonly local current_branch=$(git rev-parse --abbrev-ref HEAD)

echo "Current branch: ${branch}"
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

echo "Do mvn release:perform..."
#mvn $MAVEN_REPO_LOCAL --batch-mode --global-settings release.xml release:perform
mvn --batch-mode --global-settings release.xml release:perform -DskipTests

