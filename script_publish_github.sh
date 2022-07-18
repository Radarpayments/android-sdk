#!/bin/bash
set -e
shopt -s dotglob

stageEcho() {
echo ''
echo "$1"
echo ''
}

predicateVar() {
  if [[ -z "$2" ]]; then
    echo "Задайте значние для переменной $1" 1>&2
    exit 1
fi
}

# The address of the repository where the publication should be made.
#PUBLISH_TARGET_REPO="git@github.com:Runet-Business-Systems/android-sdk.git"

# Publish repository branch (where changes will be published)
#PUBLISH_TARGET_BRANCH="master"

# Given about the author of the commit, needing for the publish repository.
#PUBLISH_COMMIT_USER_NAME="Your Name"
#PUBLISH_COMMIT_USER_EMAIL="you@example.com"

# The publish repository commit message.
#PUBLISH_COMMIT_MESSAGE="New version"

# The commit tag of the publish repository.
#PUBLISH_COMMIT_TAG="v0.0.1"

predicateVar "PUBLISH_TARGET_REPO" $PUBLISH_TARGET_REPO
predicateVar "PUBLISH_TARGET_BRANCH" $PUBLISH_TARGET_BRANCH
predicateVar "PUBLISH_COMMIT_USER_NAME" $PUBLISH_COMMIT_USER_NAME
predicateVar "PUBLISH_COMMIT_USER_EMAIL" $PUBLISH_COMMIT_USER_EMAIL
predicateVar "PUBLISH_COMMIT_MESSAGE" $PUBLISH_COMMIT_MESSAGE
predicateVar "PUBLISH_COMMIT_TAG" $PUBLISH_COMMIT_TAG

info="
Publication information:

PUBLISH_TARGET_REPO            : $PUBLISH_TARGET_REPO
PUBLISH_TARGET_BRANCH          : $PUBLISH_TARGET_BRANCH
PUBLISH_COMMIT_USER_NAME       : $PUBLISH_COMMIT_USER_NAME
PUBLISH_COMMIT_USER_EMAIL      : $PUBLISH_COMMIT_USER_EMAIL
PUBLISH_COMMIT_MESSAGE         : $PUBLISH_COMMIT_MESSAGE
PUBLISH_COMMIT_TAG             : $PUBLISH_COMMIT_TAG"

stageEcho "$info"

stageEcho 'Directory preparation'

rm -rf payrdr-android-sdk || true
rm -rf android-sdk || true

stageEcho 'Getting a new version from the GitLab development repository'

rm -r ./.git
# You can not publish the code of the 3ds module
rm -r ./sdk_threeds

mkdir payrdr-android-sdk
rsync -av --delete-after \
  --exclude 'payrdr-android-sdk' \
  --exclude 'product.zip' \
  --exclude '.gitlab-ci.yml' \
  ./* ./payrdr-android-sdk

stageEcho 'Get the current version of the GitHub publishing repository'

git clone $PUBLISH_TARGET_REPO
cd android-sdk
git fetch
git checkout $PUBLISH_TARGET_BRANCH
git pull
cd ../

stageEcho 'Transferring Changes'

rsync -av --delete-after --exclude '.git' payrdr-android-sdk/ android-sdk/

stageEcho 'Committing changes to the publishing repository'

cd android-sdk
git config user.name "$PUBLISH_COMMIT_USER_NAME"
git config user.email "$PUBLISH_COMMIT_USER_EMAIL"
git add .
git commit -m "$PUBLISH_COMMIT_MESSAGE"
git push --set-upstream origin $PUBLISH_TARGET_BRANCH
git tag -a $PUBLISH_COMMIT_TAG -m ''
git push origin $PUBLISH_COMMIT_TAG

stageEcho 'Transfer completed'
