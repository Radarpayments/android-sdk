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

# Адрес репозитория, куда должна производиться публикация.
#PUBLISH_TARGET_REPO_3DS="git@github.com:Runet-Business-Systems/android-3ds2-sdk.git"

# Ветка репозитория публикации (куда будут публиковаться изменения)
#PUBLISH_TARGET_BRANCH="master"

# Данный об авторе коммита, нуждя для репозитория публикации.
#PUBLISH_COMMIT_USER_NAME="Your Name"
#PUBLISH_COMMIT_USER_EMAIL="you@example.com"

# Сообщение коммита репозитория публикации.
# PUBLISH_COMMIT_MESSAGE="New code version"

predicateVar "PUBLISH_TARGET_REPO_3DS" $PUBLISH_TARGET_REPO_3DS
predicateVar "PUBLISH_TARGET_BRANCH" $PUBLISH_TARGET_BRANCH
predicateVar "PUBLISH_COMMIT_USER_NAME" $PUBLISH_COMMIT_USER_NAME
predicateVar "PUBLISH_COMMIT_USER_EMAIL" $PUBLISH_COMMIT_USER_EMAIL
predicateVar "PUBLISH_COMMIT_MESSAGE" $PUBLISH_COMMIT_MESSAGE

info="
Publication information:

PUBLISH_TARGET_REPO_3DS        : $PUBLISH_TARGET_REPO_3DS
PUBLISH_TARGET_BRANCH          : $PUBLISH_TARGET_BRANCH
PUBLISH_COMMIT_USER_NAME       : $PUBLISH_COMMIT_USER_NAME
PUBLISH_COMMIT_USER_EMAIL      : $PUBLISH_COMMIT_USER_EMAIL
PUBLISH_COMMIT_MESSAGE         : $PUBLISH_COMMIT_MESSAGE"

stageEcho "$info"

stageEcho 'Подготовка директории'

rm -rf payrdr-android-sdk || true
rm -rf android-3ds2-sdk || true

stageEcho 'Получаем новую версию с репозитория разработки GitLab'

mkdir payrdr-android-sdk
rsync -av --delete-after \
  --exclude 'payrdr-android-sdk' \
  --exclude 'product.zip' \
  --exclude '.gitlab-ci.yml' \
  ./* ./payrdr-android-sdk

stageEcho 'Получаем текущую версию репозитория публикации GitHub'

git clone $PUBLISH_TARGET_REPO_3DS
cd android-3ds2-sdk
git fetch origin
git checkout $PUBLISH_TARGET_BRANCH
git pull
cd ../

stageEcho 'Переносим изменения'

rsync -av --delete-after --exclude '.git' payrdr-android-sdk/sdk_threeds/ android-3ds2-sdk/

stageEcho 'Фиксируем изменения в репозитории публикации'

cd android-3ds2-sdk
git config user.name "$PUBLISH_COMMIT_USER_NAME"
git config user.email "$PUBLISH_COMMIT_USER_EMAIL"
git add .
git commit -m "$PUBLISH_COMMIT_MESSAGE"
git push --set-upstream origin $PUBLISH_TARGET_BRANCH

stageEcho 'Перенос завершен'