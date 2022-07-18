#!/bin/bash
set -e

devVersion="Dev"
buildCommit=$(git rev-parse --short HEAD)
devVersionDone="$devVersion($buildCommit)"

if [ -z "$CI_COMMIT_TAG" ]
then
      commonVersion=$devVersionDone
else
      commonVersion=$CI_COMMIT_TAG
fi

if [ -n "$CI_VERSION_SUFFIX" ]
then
      commonVersion+="-$CI_VERSION_SUFFIX"
fi

echo "commonVersion:$commonVersion"

versionConfig="buildSrc/src/main/java/Dependencies.kt"

sed -i "" "s+sdkPaymentVersion = \".*\"+sdkPaymentVersion = \"$commonVersion\"+g;
  s+sdkFormsVersion = \".*\"+sdkFormsVersion = \"$commonVersion\"+g;
  s+sdkCoreVersion = \".*\"+sdkCoreVersion = \"$commonVersion\"+g;
  s+sdkThreeDSVersion = \".*\"+sdkThreeDSVersion = \"$commonVersion\"+g;" $versionConfig
